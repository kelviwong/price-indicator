package util;

import data.IndicatorEvent;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import publisher.PricePublisher;

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
    public synchronized void publish(T data) {
        lastEvent = data;
        lastEvents.put(data.getData().getCurrency().toString(), data);
        logger.info("Publishing: {}", data);
        super.publish(data);
    }

    public synchronized void clearLastEvent() {
        lastEvent = null;
        lastEvents.clear();
    }

    public synchronized Map<String, T> getResult() {
        return lastEvents;
    }

    public synchronized T getLastEvent() {
        return lastEvent;
    }
}
