package service;

import util.ServiceUtil;
import common.NamedThreadFactory;
import common.TimeProvider;
import config.Config;
import data.*;
import dispatcher.DispatcherAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import publisher.PriceReader;
import publisher.Publisher;
import storage.IStore;
import common.PriceStoreFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PriceService implements IService {
    private static final Logger logger = LoggerFactory.getLogger(PriceService.class);
    private final ExecutorService executorService;
    private final DispatcherAgent dispatcherAgent;
    private volatile boolean isStopped = false;
    private final PriceReader<Event<Price>> priceReader;
    private final Map<WritableMutableCharSequence, IStore<Price>> currencyPriceVolMap;

    public PriceService(PriceReader<Event<Price>> priceReader, DispatcherAgent dispatcherAgent) {
        this.executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("PriceService"));
        this.priceReader = priceReader;
        this.currencyPriceVolMap = new ConcurrentHashMap<>();
        this.dispatcherAgent = dispatcherAgent;
    }

    @Override
    public void start() {
        logger.info("Start Price Service");
        executorService.execute(() -> {
            while (!isStopped) {
                Event<Price> event = null;
                try {
                    event = priceReader.poll();
                    if (event != null) {
                        Price data = event.getData();
                        dispatcherAgent.dispatchQueue(data.getCurrency(), event);
                    }
                } catch (Exception e) {
                    logger.error("Error processing price event: " + event, e);
                }
            }
        });
    }

    @Override
    public void stop() {
        logger.info("Stop Price Service");
//        if (currencyPriceVolMap != null) {
//            Collection<IStore<Price>> values = currencyPriceVolMap.values();
//            for (IStore<Price> value : values) {
//                value.close();
//            }
//        }

        ServiceUtil.quietlyStop(dispatcherAgent);

        isStopped = true;
    }
}
