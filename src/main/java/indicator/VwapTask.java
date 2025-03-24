package indicator;

import config.Config;
import data.AnalyticData;
import data.IndicatorEvent;
import data.Price;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import publisher.LogPublisher;
import publisher.Publisher;
import storage.IStore;

public class VwapTask<T extends Price> implements Runnable {
    private final AnalyticData analyticData;
    private final IStore<Price> priceStore;
    private final Calculator calculator;
    private final Publisher<IndicatorEvent> publisher;
    private static final Logger logger = LoggerFactory.getLogger(LogPublisher.class);
    private final Config config;

    @Setter
    T data;

    public VwapTask(AnalyticData analyticData, IStore<Price> priceStore, Publisher<IndicatorEvent> publisher, Calculator calculator, Config config) {
        this.analyticData = analyticData;
        this.priceStore = priceStore;
        this.publisher = publisher;
        this.calculator = calculator;
        this.config = config;
    }

    @Override
    public void run() {
        logger.info("processing data: {}", data);
        IndicatorEvent indicatorEvent = new IndicatorEvent(analyticData);
        calculator.calculateWithDelta(data, priceStore, analyticData);
        long now = data.getTimestamp();
        long expiredTimeInMills = now - config.getVwapConfig().getVwapIntervalInMs();
        // only publish when vwap is larger than 0 && the data have 1 hour long
        if (analyticData.getVwap() > 0 && expiredTimeInMills >= analyticData.getFirstDataTime()) {
            publisher.publish(indicatorEvent);
        }
    }
}
