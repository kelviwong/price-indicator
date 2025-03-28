package service;

import common.MockTimeProvider;
import common.PriceStoreFactory;
import config.Config;
import data.IndicatorEvent;
import data.Price;
import data.PriceEvent;
import dispatcher.DispatcherAgent;
import dispatcher.HashSymbolDispatchStrategy;
import dispatcher.RoundRobinDispatchStrategy;
import enums.StoreType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import publisher.PricePublisher;
import publisher.PriceReader;
import queue.QueueFactory;
import queue.QueueType;
import util.Data;
import util.MockPriceEventPublisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static util.Data.generateDifferentData;
import static util.Data.setup59MinutesOldData;

class PriceServiceTest {
    static Config config;

    @BeforeAll
    public static void setup() throws IOException {
        config = Data.getConfig();
    }

    @Test
    public void testPriceServiceWhenMultipleCurrencyInDeque() throws Exception {
        testPriceServiceWhenMultipleCurrency(StoreType.DEQUE);
    }

    @Test
    public void testPriceServiceWhenMultipleCurrencyInMMF() throws Exception {
        testPriceServiceWhenMultipleCurrency(StoreType.MEM_MAP);
    }

    public void testPriceServiceWhenMultipleCurrency(StoreType storeType) throws Exception {
        MockTimeProvider timeProvider = new MockTimeProvider();
        timeProvider.setCurrentTime("10:00:00");
        assertEquals("10:00:00", timeProvider.getCurrentTime());

        BlockingQueue<IndicatorEvent> indicatorEventArrayBlockingQueue = QueueFactory.createQueue(10000, QueueType.BACKOFF);
        BlockingQueue<PriceEvent> priceEventArrayBlockingQueue = QueueFactory.createQueue(10000, QueueType.BACKOFF);
        MockPriceEventPublisher<IndicatorEvent> publisher = new MockPriceEventPublisher<>(indicatorEventArrayBlockingQueue);

        PricePublisher<PriceEvent> ignessPublisher = new PricePublisher<>(priceEventArrayBlockingQueue);
        PriceReader<PriceEvent> priceReader = new PriceReader<>(priceEventArrayBlockingQueue);
        PriceStoreFactory priceStoreFactory = new PriceStoreFactory(storeType, "test2");
        DispatcherAgent dispatcherAgent = new DispatcherAgent(config.getDispatcherConfig().getThreads(), new HashSymbolDispatchStrategy());
        PriceService priceService = new PriceService(priceReader, timeProvider, publisher, priceStoreFactory, dispatcherAgent, config);
        priceService.start();
        String currency = "AUD/USD";

        timeProvider.advanceInMinutes(-59);
        assertEquals("09:01:00", timeProvider.getCurrentTime());
        setup59MinutesOldData(ignessPublisher, timeProvider, currency, 0.0d);
        assertEquals("10:00:00", timeProvider.getCurrentTime());

        // nothing publish as there are no 1 hour long data
        assertNull(publisher.getLastEvent());

        // after 1 minutes reach 1 hour
        timeProvider.advanceInMinutes(1);
        assertEquals("10:01:00", timeProvider.getCurrentTime());
        ignessPublisher.publish(new PriceEvent(new Price(currency, timeProvider.now(), 10.4, 12000)));
        Thread.sleep(20);
        assertEquals(11.204545454545455, publisher.getLastEvent().getData().getVwap());

        // after 1 hour, any price event will keep updating vwap
        timeProvider.advanceInSecond(1);
        assertEquals("10:01:01", timeProvider.getCurrentTime());
        ignessPublisher.publish(new PriceEvent(new Price(currency, timeProvider.now(), 20.4, 12000)));
        Thread.sleep(10);
        assertEquals(currency, publisher.getLastEvent().getData().getCurrency());
        assertEquals(13.2, publisher.getLastEvent().getData().getVwap());
        publisher.clearLastEvent();

        // different ccy
        timeProvider.advanceInMinutes(-59);
        assertEquals("09:02:01", timeProvider.getCurrentTime());
        setup59MinutesOldData(ignessPublisher, timeProvider, "JPY/USD", 0.423);
        assertEquals("10:01:01", timeProvider.getCurrentTime());
        Thread.sleep(10);
        // nothing publish
        assertNull(publisher.getLastEvent());

        currency = "JPY/USD";
        timeProvider.advanceInMinutes(1);
        assertEquals("10:02:01", timeProvider.getCurrentTime());
        ignessPublisher.publish(new PriceEvent(new Price(currency, timeProvider.now(), 232.4, 12000)));

        Thread.sleep(10);
        assertEquals(currency, publisher.getLastEvent().getData().getCurrency());
        assertEquals(72.05763636363636, publisher.getLastEvent().getData().getVwap());
    }

    @Test
    public void GivenMultipleThreadAndDifferentCCyDispatcherWhenPriceCalculatedThenShouldAlwaysReturnCorrectResult() throws Exception {
        MockTimeProvider timeProvider = new MockTimeProvider();
        timeProvider.setCurrentTime("10:00:00");
        assertEquals("10:00:00", timeProvider.getCurrentTime());

        BlockingQueue<IndicatorEvent> indicatorEventArrayBlockingQueue = QueueFactory.createQueue(10000, QueueType.BACKOFF);
        BlockingQueue<PriceEvent> priceEventArrayBlockingQueue = QueueFactory.createQueue(10000, QueueType.BACKOFF);
        MockPriceEventPublisher<IndicatorEvent> publisher = new MockPriceEventPublisher<>(indicatorEventArrayBlockingQueue);

        PricePublisher<PriceEvent> ignessPublisher = new PricePublisher<>(priceEventArrayBlockingQueue);
        PriceReader<PriceEvent> priceReader = new PriceReader<>(priceEventArrayBlockingQueue);
        PriceStoreFactory priceStoreFactory = new PriceStoreFactory(StoreType.DEQUE, "test2");

        // use 3 threads
        DispatcherAgent dispatcherAgent = new DispatcherAgent(3, new RoundRobinDispatchStrategy());
        PriceService priceService = new PriceService(priceReader, timeProvider, publisher, priceStoreFactory, dispatcherAgent, config);
        priceService.start();

        // put in a price from very old first to ensure we will start to publish vwap price immediately.
        timeProvider.advanceInMinutes(-60);
        setupOldData("AUD/USD", ignessPublisher, timeProvider);
        setupOldData("GBP/USD", ignessPublisher, timeProvider);
        setupOldData("EUR/USD", ignessPublisher, timeProvider);
        setupOldData("NZD/USD", ignessPublisher, timeProvider);
        setupOldData("EUR/GBP", ignessPublisher, timeProvider);
        timeProvider.advanceInMinutes(60);
        Thread.sleep(1000);
        //back to now

        List<List<PriceEvent>> list = new ArrayList<>();
        list.add(generateDifferentData(timeProvider, "AUD/USD", 0, 0));
        list.add(generateDifferentData(timeProvider, "GBP/USD", 0.1, 100));
        list.add(generateDifferentData(timeProvider, "EUR/USD", 0.2, 200));
        list.add(generateDifferentData(timeProvider, "NZD/USD", 0.3, 300));
        list.add(generateDifferentData(timeProvider, "EUR/GBP", 0.4, 400));

        int size = list.get(0).size();
        for (int i = 0; i < size; i++) {
            for (List<PriceEvent> priceEvents : list) {
                PriceEvent priceEvent = priceEvents.get(i);
                ignessPublisher.publish(priceEvent);
            }
        }

        Thread.sleep(10);

        Map<String, IndicatorEvent> results = publisher.getResult();
        IndicatorEvent resultEvent = results.get("AUD/USD");
        assertEquals(11.50625, resultEvent.getData().getVwap());
        resultEvent = results.get("GBP/USD");
        assertEquals(11.612307692307692, resultEvent.getData().getVwap());
        resultEvent = results.get("EUR/USD");
        assertEquals(11.718181818181819, resultEvent.getData().getVwap());
        resultEvent = results.get("NZD/USD");
        assertEquals(11.823880597014925, resultEvent.getData().getVwap());
        resultEvent = results.get("EUR/GBP");
        assertEquals(11.929411764705883, resultEvent.getData().getVwap());
    }

    private static void setupOldData(String currency, PricePublisher<PriceEvent> ignessPublisher, MockTimeProvider timeProvider) {
        ignessPublisher.publish(new PriceEvent(new Price(currency, timeProvider.now(), 13.5, 2000)));
    }

}