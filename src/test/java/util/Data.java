package util;

import common.MockTimeProvider;
import config.Config;
import data.Price;
import data.PriceEvent;
import publisher.PricePublisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Data {

    public static void setup59MinutesOldData(PricePublisher<PriceEvent> publisher, MockTimeProvider mockTimeProvider, String currency, double adjustPrice) throws InterruptedException {
        publisher.publish(new PriceEvent(new Price(currency, mockTimeProvider.now(), 12.5 + adjustPrice, 2000)));
        mockTimeProvider.advanceInMinutes(5);
        publisher.publish(new PriceEvent(new Price(currency, mockTimeProvider.now(), 10.5 + adjustPrice, 1000)));
        mockTimeProvider.advanceInMinutes(10);
        publisher.publish(new PriceEvent(new Price(currency, mockTimeProvider.now(), 14.6 + adjustPrice, 4000)));
        mockTimeProvider.advanceInMinutes(20);
        publisher.publish(new PriceEvent(new Price(currency, mockTimeProvider.now(), 11.5 + adjustPrice, 13000)));
        mockTimeProvider.advanceInMinutes(20);
        publisher.publish(new PriceEvent(new Price(currency, mockTimeProvider.now(), 10.4 + adjustPrice, 12000)));
        mockTimeProvider.advanceInMinutes(4);
    }

    public static List<PriceEvent> generateDifferentData(MockTimeProvider mockTimeProvider, String currency, double adjustPrice, long adjustVolume) throws InterruptedException {
        List<PriceEvent> prices = new ArrayList<>();
        mockTimeProvider.advanceInMinutes(1);
        prices.add(new PriceEvent(new Price(currency, mockTimeProvider.now(), 12.5 + adjustPrice, 2000 + adjustVolume)));
        prices.add(new PriceEvent(new Price(currency, mockTimeProvider.now(), 10.5 + adjustPrice, 1000 + adjustVolume)));
        prices.add(new PriceEvent(new Price(currency, mockTimeProvider.now(), 14.6 + adjustPrice, 4000 + adjustVolume)));
        prices.add(new PriceEvent(new Price(currency, mockTimeProvider.now(), 11.5 + adjustPrice, 13000 + adjustVolume)));
        prices.add(new PriceEvent(new Price(currency, mockTimeProvider.now(), 10.4 + adjustPrice, 12000 + adjustVolume)));
        return prices;
    }

    public static Config getConfig() throws IOException {
        return Config.loadConfig("config.yaml");
    }

}
