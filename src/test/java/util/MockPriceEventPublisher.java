package util;

import data.IndicatorEvent;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import publisher.PricePublisher;
import queue.LogDropConsumer;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class MockPriceEventPublisher<T extends IndicatorEvent> extends PricePublisher<T> {
    volatile T lastEvent;

    Map<String, T> lastEvents = new ConcurrentHashMap<>();
    protected static final Logger logger = LoggerFactory.getLogger(MockPriceEventPublisher.class);

    public MockPriceEventPublisher(BlockingQueue<T> queue) {
        super(queue);
    }

    @Override
    public void publish(T data) {
        lastEvent = data;
        lastEvents.put(data.getData().getCurrency(), data);
        logger.info("Publishing: {}", data);
        super.publish(data);
    }

    public void clearLastEvent() {
        lastEvent = null;
        lastEvents.clear();
    }

    public Map<String, T> getResult() {
        return lastEvents;
    }

    public T getLastEvent() {
        return lastEvent;
    }
}
