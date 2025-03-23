package util;

import publisher.PricePublisher;

import java.util.concurrent.ArrayBlockingQueue;

public class MockPriceEventPublisher<T> extends PricePublisher<T> {
    T lastEvent;
    public MockPriceEventPublisher(ArrayBlockingQueue<T> queue) {
        super(queue);
    }

    public T getLastEvent() {
        return lastEvent;
    }

    @Override
    public void publish(T data) {
        lastEvent = data;
        super.publish(data);
    }
}
