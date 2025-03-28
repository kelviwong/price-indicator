package service;

import common.PriceStoreFactory;
import config.Config;
import data.*;
import indicator.ICalculator;
import latency.LatencyTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import publisher.PooledPublisher;
import publisher.Publisher;
import storage.IStore;
import util.ObjectPool;

import java.util.HashMap;
import java.util.Map;

public class VwapPriceEventHandler implements EventHandler<PriceEvent> {
    private final Map<WritableMutableCharSequence, IStore<Price>> currencyPriceVolMap;
    private final Map<WritableMutableCharSequence, AnalyticData> analyticDataMap;
    private PriceStoreFactory priceStoreFactory;
    private static final Logger logger = LoggerFactory.getLogger(VwapPriceEventHandler.class);
    private final ICalculator calculator;
    private Config config;
    private Publisher<IndicatorEvent> publisher;
    private ObjectPool<IndicatorEvent> eventStore;

    public VwapPriceEventHandler(
            PriceStoreFactory priceStoreFactory, ICalculator calculator, Config config, Publisher<IndicatorEvent> publisher) {
        this.priceStoreFactory = priceStoreFactory;
        this.calculator = calculator;
        this.config = config;
        this.publisher = publisher;
        this.currencyPriceVolMap = new HashMap<>();
        this.analyticDataMap = new HashMap<>();
        this.eventStore = new ObjectPool<>(1000, () -> new IndicatorEvent());
    }

    @Override
    public void handle(PriceEvent event) throws Exception {
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
            analyticData.setFirstDataTime(data.getTimestamp());
            analyticDataMap.put(currency, analyticData);
        }

//        IndicatorEvent indicatorEvent;
//        if (publisher instanceof PooledPublisher) {
//            indicatorEvent = ((PooledPublisher<IndicatorEvent>) publisher).acquire();
//        } else {
//            indicatorEvent = new IndicatorEvent();
//        }
        IndicatorEvent indicatorEvent = eventStore.acquire();
        indicatorEvent.setData(analyticData);
        LatencyTracker.copyStartNs(event, indicatorEvent);

        calculator.calculateWithDelta(data, priceStore, analyticData);
        long now = data.getTimestamp();
        long expiredTimeInMills = now - config.getVwapConfig().getVwapIntervalInMs();
        // only publish when vwap is larger than 0 && the data have 1 hour long
        if (analyticData.getVwap() > 0 && expiredTimeInMills >= analyticData.getFirstDataTime()) {
            // TODO: should make this serialize publisher in another thread queue
            publisher.publish(indicatorEvent);
            eventStore.release(indicatorEvent);
        }
    }
}
