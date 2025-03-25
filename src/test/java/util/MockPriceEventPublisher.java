package util;

import data.IndicatorEvent;
import data.Price;
import data.PriceEvent;
import lombok.Getter;
import publisher.PricePublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class MockPriceEventPublisher<T extends IndicatorEvent> extends PricePublisher<T> {
    volatile T lastEvent;

    Map<String, T> lastEvents = new ConcurrentHashMap<>();

    public MockPriceEventPublisher(BlockingQueue<T> queue) {
        super(queue);
    }

    @Override
    public void publish(T data) {
        lastEvent = data;
        lastEvents.put(data.getData().getCurrency(), data);
        super.publish(data);
    }

    public void clearLastEvent() {
        lastEvent = null;
    }

    public Map<String, T> getResult() {
        return lastEvents;
    }
}
