package service;

import common.PriceStoreFactory;
import config.Config;
import data.AnalyticData;
import data.IndicatorEvent;
import data.Price;
import data.PriceEvent;
import indicator.Calculator;
import jdk.nashorn.internal.ir.Block;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import publisher.PooledPublisher;
import publisher.Publisher;
import storage.IStore;

import java.util.*;
import java.util.concurrent.BlockingQueue;

public class PriceWorker implements Runnable, IService {

    @Getter
    BlockingQueue<PriceEvent> taskQueue;

    private final Map<String, IStore<Price>> currencyPriceVolMap;
    private final Map<String, AnalyticData> analyticDataMap;
    private Config config;

    private final List<Calculator> handlers;

    private final Publisher<IndicatorEvent> publisher;

    private PriceStoreFactory priceStoreFactory;

    private static final Logger logger = LoggerFactory.getLogger(PriceWorker.class);

    private volatile boolean isStop;

    public PriceWorker(BlockingQueue<PriceEvent> taskQueue, Publisher<IndicatorEvent> publisher, Config config, PriceStoreFactory priceStoreFactory) {
        this.taskQueue = taskQueue;
        this.currencyPriceVolMap = new HashMap<>();
        this.analyticDataMap = new HashMap<>();
        this.config = config;
        this.handlers = new ArrayList<>();
        this.publisher = publisher;
        this.priceStoreFactory = priceStoreFactory;
    }

    public void addCalculatorHandler(Calculator calculator) {
        handlers.add(calculator);
    }

    public void run() {
        while (!isStop) {
            PriceEvent task = null;
            try {
                task = taskQueue.take();
                onPriceEvent(task);
            } catch (InterruptedException e) {
//                logger.error("Error handling event, ", e);
            }
        }
    }


    public void onPriceEvent(PriceEvent event) {
        runVwap(event);
    }

    private void runVwap(PriceEvent event) {
        Price data = event.getData();
        IStore<Price> priceStore = currencyPriceVolMap.computeIfAbsent(data.getCurrency(), (k) -> {
            try {
                return priceStoreFactory.createStore(k);
            } catch (Exception e) {
                logger.error("Error price store : {}", k, e);
                throw new RuntimeException(e);
            }
        });

        AnalyticData analyticData = analyticDataMap.computeIfAbsent(data.getCurrency(), (k) -> {
            AnalyticData firstData = new AnalyticData(k);
            firstData.setFirstDataTime(data.getTimestamp());
            return firstData;
        });
        IndicatorEvent indicatorEvent;
        if (publisher instanceof PooledPublisher) {
            indicatorEvent = ((PooledPublisher<IndicatorEvent>) publisher).acquire();
        } else {
            indicatorEvent = new IndicatorEvent(null);
        }
        indicatorEvent.setData(analyticData);

        int handlerSize = handlers.size();
        for (int i = 0; i < handlerSize; i++) {
            Calculator calculator = handlers.get(i);
            calculator.calculateWithDelta(data, priceStore, analyticData);
            long now = data.getTimestamp();
            long expiredTimeInMills = now - config.getVwapConfig().getVwapIntervalInMs();
            // only publish when vwap is larger than 0 && the data have 1 hour long
            if (analyticData.getVwap() > 0 && expiredTimeInMills >= analyticData.getFirstDataTime()) {
                publisher.publish(indicatorEvent);
            }
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        Thread.currentThread().interrupt();
        isStop = true;
    }
}
