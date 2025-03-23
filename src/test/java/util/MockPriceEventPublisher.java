package util;

import lombok.Getter;
import publisher.PricePublisher;

import java.util.concurrent.ArrayBlockingQueue;

@Getter
public class MockPriceEventPublisher<T> extends PricePublisher<T> {
    volatile T lastEvent;
    public MockPriceEventPublisher(ArrayBlockingQueue<T> queue) {
        super(queue);
    }

    @Override
    public void publish(T data) {
        lastEvent = data;
        super.publish(data);
    }

    public void clearLastEvent(){
        lastEvent = null;
    }
}
