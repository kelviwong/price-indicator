package service;

import common.MockTimeProvider;
import data.IndicatorEvent;
import data.Price;
import data.PriceEvent;
import org.junit.jupiter.api.Test;
import publisher.PricePublisher;
import publisher.PriceReader;
import storage.PriceStoreFactory;
import storage.StoreType;
import util.MockPriceEventPublisher;

import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static util.Data.setup59MinutesOldData;

class PriceServiceTest {
    @Test
    public void testPriceServiceWhenMultipleCurrencyInDeque() throws InterruptedException {
        testPriceServiceWhenMultipleCurrency(StoreType.DEQUE);
    }

    @Test
    public void testPriceServiceWhenMultipleCurrencyInMMF() throws InterruptedException {
        testPriceServiceWhenMultipleCurrency(StoreType.MEM_MAP);
    }

    public void testPriceServiceWhenMultipleCurrency(StoreType storeType) throws InterruptedException {
        ArrayBlockingQueue<IndicatorEvent> indicatorEventArrayBlockingQueue = new ArrayBlockingQueue<>(10000);
        ArrayBlockingQueue<PriceEvent> priceEventArrayBlockingQueue = new ArrayBlockingQueue<>(10000);
        MockPriceEventPublisher<IndicatorEvent> publisher = new MockPriceEventPublisher<>(indicatorEventArrayBlockingQueue);
        MockTimeProvider timeProvider = new MockTimeProvider();
        timeProvider.setCurrentTime("10:00:00");
        assertEquals("10:00:00", timeProvider.getCurrentTime());

        PricePublisher<PriceEvent> ignessPublisher = new PricePublisher<>(priceEventArrayBlockingQueue);
        PriceReader<PriceEvent> priceReader = new PriceReader<>(priceEventArrayBlockingQueue);
        PriceStoreFactory priceStoreFactory = new PriceStoreFactory(storeType, "test2");
        PriceService priceService = new PriceService(priceReader, timeProvider, publisher, priceStoreFactory);
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
        Thread.sleep(10);
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

}