package service;

import common.PriceStoreFactory;
import config.Config;
import data.AnalyticData;
import data.IndicatorEvent;
import data.Price;
import data.PriceEvent;
import indicator.Calculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import publisher.PooledPublisher;
import publisher.Publisher;
import storage.IStore;

import java.util.HashMap;
import java.util.Map;

public class VwapPriceEventHandler implements EventHandler<PriceEvent> {
    private final Map<String, IStore<Price>> currencyPriceVolMap;
    private final Map<String, AnalyticData> analyticDataMap;
    private PriceStoreFactory priceStoreFactory;
    private static final Logger logger = LoggerFactory.getLogger(VwapPriceEventHandler.class);
    private final Calculator calculator;

    private Config config;

    private Publisher<IndicatorEvent> publisher;

    public VwapPriceEventHandler(
            PriceStoreFactory priceStoreFactory, Calculator calculator, Config config, Publisher<IndicatorEvent> publisher) {
        this.priceStoreFactory = priceStoreFactory;
        this.calculator = calculator;
        this.config = config;
        this.publisher = publisher;
        this.currencyPriceVolMap = new HashMap<>();
        this.analyticDataMap = new HashMap<>();
    }

    @Override
    public void handle(PriceEvent event) throws Exception {
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

        calculator.calculateWithDelta(data, priceStore, analyticData);
        long now = data.getTimestamp();
        long expiredTimeInMills = now - config.getVwapConfig().getVwapIntervalInMs();
        // only publish when vwap is larger than 0 && the data have 1 hour long
        if (analyticData.getVwap() > 0 && expiredTimeInMills >= analyticData.getFirstDataTime()) {
            publisher.publish(indicatorEvent);
        }
    }
}
