package queue;

import common.MockTimeProvider;
import data.Price;
import data.PriceEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class BackOffBlockingQueueTest {

    private MessageQueue<PriceEvent> backOffBlockingQueue;
    private MockTimeProvider timeProvider;

    @BeforeEach
    public void setUp() throws Exception {
        timeProvider = new MockTimeProvider();
        BackOffStrategy<PriceEvent> strategy = new RetryThenDropStrategy<>(3, 100, new LogDropConsumer<>());
        backOffBlockingQueue = QueueFactory.createMessageQueue(1, QueueType.BLOCKING_BACKOFF, null, strategy);
    }

    @Test
    public void testWhenItemThenInsertSuccessfully() throws InterruptedException {
        Price data = new Price("AUD/USD", timeProvider.now(), 11.2, 1000);
        backOffBlockingQueue.publish(new PriceEvent(data));
        PriceEvent item = backOffBlockingQueue.take();
        assertNotNull(item);
        assertEquals(data, item.getData());
    }

    @Test
    public void testWhenItemBackOffAndThenSuccessfully() throws InterruptedException {
        Price data = new Price("AUD/USD", timeProvider.now(), 11.2, 1000);
        PriceEvent priceEvent = new PriceEvent(data);
        backOffBlockingQueue.publish(priceEvent);

        PriceEvent priceEvent2 = new PriceEvent(data);
        boolean result = backOffBlockingQueue.publish(priceEvent2);
        assertFalse(result);

        // when there is something take
        new Thread(() -> {
            try {
                TimeUnit.MICROSECONDS.sleep(10);
                backOffBlockingQueue.take();
            } catch (InterruptedException ignored) {
            }
        }).start();

        TimeUnit.MICROSECONDS.sleep(20);

        PriceEvent priceEvent3 = new PriceEvent(data);
        result = backOffBlockingQueue.publish(priceEvent3);
        assertTrue(result);
    }

    @Test
    public void testWhenFailRetryThenDropped() throws Exception {
        Price data = new Price("AUD/USD", timeProvider.now(), 11.2, 1000);
        PriceEvent priceEvent = new PriceEvent(data);

        BlockingQueue<PriceEvent> arrayBlockingQueue = new ArrayBlockingQueue<>(1);
        AtomicBoolean dropped = new AtomicBoolean(false);
        BackOffStrategy<PriceEvent> strategy = new RetryThenDropStrategy<>(3, 100, item -> {
            dropped.set(true);
        });
        backOffBlockingQueue = QueueFactory.createMessageQueue(1, QueueType.BLOCKING_BACKOFF, null, strategy);

        backOffBlockingQueue.publish(priceEvent);
        // offer again but full
        boolean offer = backOffBlockingQueue.publish(priceEvent);

        assertFalse(offer);
        assertTrue(dropped.get());
    }

}