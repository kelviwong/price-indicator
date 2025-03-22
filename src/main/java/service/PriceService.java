package service;

import common.NamedThreadFactory;
import common.TimeProvider;
import data.AnalyticData;
import data.IndicatorEvent;
import data.PriceEvent;
import indicator.Indicator;
import indicator.VwapIndicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import publisher.PriceReader;
import publisher.Publisher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PriceService implements IService {
    private static final Logger logger = LoggerFactory.getLogger(PriceService.class);
    private final ExecutorService executorService;
    private final Indicator vwapIndicator;
    private volatile boolean isStopped = false;
    private final PriceReader priceReader;
    private final Publisher<IndicatorEvent> publisher;

    public PriceService(PriceReader priceReader, TimeProvider timeProvider, Publisher<IndicatorEvent> publisher) {
        this.vwapIndicator = new VwapIndicator(timeProvider);
        this.executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("PriceService"));
        this.priceReader = priceReader;
        this.publisher = publisher;
    }

    @Override
    public void start() {
        logger.info("Start Price Service");
        executorService.submit(() -> {
            while (!isStopped) {
                PriceEvent event = null;
                try {
                    event = priceReader.poll();
                    if (event != null) {
                        AnalyticData analyticData = new AnalyticData();
                        IndicatorEvent indicatorEvent = new IndicatorEvent(analyticData);
                        double vwap = vwapIndicator.calculateWithDelta(event.getData());
                        analyticData.setVwap(vwap);
                        publisher.publish(indicatorEvent);
                    }
                } catch (InterruptedException e) {
                    logger.error("Error processing price event: " + event, e);
                }
            }
        });
    }

    @Override
    public void stop() {
        isStopped = true;
    }
}
