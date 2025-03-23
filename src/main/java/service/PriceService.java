package service;

import common.NamedThreadFactory;
import common.TimeProvider;
import data.AnalyticData;
import data.IndicatorEvent;
import data.Price;
import data.PriceEvent;
import indicator.Calculator;
import indicator.VwapCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import publisher.PriceReader;
import publisher.Publisher;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PriceService implements IService {
    private static final Logger logger = LoggerFactory.getLogger(PriceService.class);
    private final ExecutorService executorService;
    private final Calculator vwapCalculator;
    private volatile boolean isStopped = false;
    private final PriceReader<PriceEvent> priceReader;
    private final Publisher<IndicatorEvent> publisher;
    private Map<String, ArrayDeque> currencyPriceVolMap;
    private Map<String, AnalyticData> analyticDataMap;
    public PriceService(PriceReader<PriceEvent> priceReader, TimeProvider timeProvider, Publisher<IndicatorEvent> publisher) {
        this.vwapCalculator = new VwapCalculator(timeProvider);
        this.executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("PriceService"));
        this.priceReader = priceReader;
        this.publisher = publisher;
        this.currencyPriceVolMap = new HashMap<>();
        this.analyticDataMap = new HashMap<>();
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
                        Price data = event.getData();
                        ArrayDeque<Price> arrayDeque = currencyPriceVolMap.computeIfAbsent(data.getCurrency(), (k) -> new ArrayDeque<Price>());
                        AnalyticData analyticData = analyticDataMap.computeIfAbsent(data.getCurrency(), (k) -> new AnalyticData(k));
                        IndicatorEvent indicatorEvent = new IndicatorEvent(analyticData);
                        double vwap = vwapCalculator.calculateWithDelta(data, arrayDeque, analyticData);
                        analyticData.setVwap(vwap);
                        publisher.publish(indicatorEvent);
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
        isStopped = true;
    }
}
