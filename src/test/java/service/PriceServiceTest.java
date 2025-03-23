package service;

import common.MockTimeProvider;
import data.IndicatorEvent;
import data.Price;
import data.PriceEvent;
import org.junit.jupiter.api.Test;
import publisher.PricePublisher;
import publisher.PriceReader;
import util.MockPriceEventPublisher;

import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceServiceTest {

    private static void setupData(PricePublisher<PriceEvent> publisher, MockTimeProvider mockTimeProvider, String currency, double adjustPrice) {
        mockTimeProvider.advanceInMinutes(-61);
        publisher.publish(new PriceEvent(new Price(currency, mockTimeProvider.now(), 12.5 + adjustPrice, 2000)));
        mockTimeProvider.advanceInMinutes(5);
        publisher.publish(new PriceEvent(new Price(currency, mockTimeProvider.now(), 10.5 + adjustPrice, 1000)));
        mockTimeProvider.advanceInMinutes(10);
        publisher.publish(new PriceEvent(new Price(currency, mockTimeProvider.now(), 14.6 + adjustPrice, 4000)));
        mockTimeProvider.advanceInMinutes(20);
        publisher.publish(new PriceEvent(new Price(currency, mockTimeProvider.now(), 11.5 + adjustPrice, 13000)));
        mockTimeProvider.advanceInMinutes(20);
        publisher.publish(new PriceEvent(new Price(currency, mockTimeProvider.now(), 10.4 + adjustPrice, 12000)));
        mockTimeProvider.advanceInMinutes(6);
    }

    @Test
    public void testPriceServiceWhenMultipleCurrency() throws InterruptedException {
        ArrayBlockingQueue<IndicatorEvent> indicatorEventArrayBlockingQueue = new ArrayBlockingQueue<>(10000);
        ArrayBlockingQueue<PriceEvent> priceEventArrayBlockingQueue = new ArrayBlockingQueue<>(10000);
        MockPriceEventPublisher<IndicatorEvent> publisher = new MockPriceEventPublisher(indicatorEventArrayBlockingQueue);
        MockTimeProvider timeProvider = new MockTimeProvider();
        PricePublisher<PriceEvent> ignessPublisher = new PricePublisher<>(priceEventArrayBlockingQueue);
        PriceReader priceReader = new PriceReader(priceEventArrayBlockingQueue);
        PriceService priceService = new PriceService(priceReader, timeProvider, publisher);
        priceService.start();
        setupData(ignessPublisher, timeProvider, "AUD/USD", 0.0d);
        Thread.sleep(2);
        assertEquals("AUD/USD", publisher.getLastEvent().getData().getCurrency());
        assertEquals(11.50625, publisher.getLastEvent().getData().getVwap());

        // calculate another currency pair
        setupData(ignessPublisher, timeProvider, "JPY/USD", 0.423);
        Thread.sleep(2);
        assertEquals("JPY/USD", publisher.getLastEvent().getData().getCurrency());
        assertEquals(11.92925, publisher.getLastEvent().getData().getVwap());
    }

}