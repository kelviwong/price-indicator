package service;

import Util.ServiceUtil;
import common.NamedThreadFactory;
import common.TimeProvider;
import config.Config;
import data.*;
import dispatcher.DispatcherAgent;
import indicator.Calculator;
import indicator.VwapTask;
import indicator.VwapCalculator;
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
    private final Calculator vwapCalculator;
    private final DispatcherAgent dispatcherAgent;
    private volatile boolean isStopped = false;
    private final PriceReader<PriceEvent> priceReader;
    private final Publisher<IndicatorEvent> publisher;
    private final Map<WritableMutableCharSequence, IStore<Price>> currencyPriceVolMap;
    private final Map<WritableMutableCharSequence, AnalyticData> analyticDataMap;
    private final PriceStoreFactory priceStoreFactory;
    private final Config config;

    public PriceService(PriceReader<PriceEvent> priceReader, TimeProvider timeProvider,
                        Publisher<IndicatorEvent> publisher, PriceStoreFactory priceStoreFactory, DispatcherAgent dispatcherAgent,
                        Config config) {
        this.vwapCalculator = new VwapCalculator(config);
        this.executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("PriceService"));
        this.priceReader = priceReader;
        this.publisher = publisher;
        this.currencyPriceVolMap = new ConcurrentHashMap<>();
        this.analyticDataMap = new ConcurrentHashMap<>();
        this.priceStoreFactory = priceStoreFactory;
        this.dispatcherAgent = dispatcherAgent;
        this.config = config;
    }

    @Override
    public void start() {
        logger.info("Start Price Service");
        executorService.execute(() -> {
            while (!isStopped) {
                PriceEvent event = null;
                try {
                    event = priceReader.poll();
                    if (event != null) {
                        Price data = event.getData();
                        WritableMutableCharSequence currency = data.getCurrency();
                        IStore<Price> priceStore = currencyPriceVolMap.get(currency);
                        if (priceStore == null) {
                            priceStore = priceStoreFactory.createStore(currency);
                            currencyPriceVolMap.put(currency, priceStore);
                        }

                        AnalyticData analyticData = analyticDataMap.get(currency);
                        if (analyticData == null) {
                            analyticData = new AnalyticData(currency);
                            analyticDataMap.put(currency, analyticData);
                        }

                        //TODO: should make this better to avoid this creation of task.
                        VwapTask<Price> vwapTask = new VwapTask<>(analyticData, priceStore, publisher, vwapCalculator, config);
                        vwapTask.setData(data);

                        dispatcherAgent.dispatchTask(currency, vwapTask);
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
        if (currencyPriceVolMap != null) {
            Collection<IStore<Price>> values = currencyPriceVolMap.values();
            for (IStore<Price> value : values) {
                value.close();
            }
        }

        ServiceUtil.quietlyStop(dispatcherAgent);

        isStopped = true;
    }
}
