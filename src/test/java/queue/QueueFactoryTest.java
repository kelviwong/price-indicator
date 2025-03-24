package queue;

import data.PriceEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class QueueFactoryTest {

    @Test
    public void testQueueFactory() throws Exception {
        BlockingQueue<PriceEvent> queue = QueueFactory.createQueue(1, QueueType.BACKOFF);
        BlockingQueue<PriceEvent> queue2 = QueueFactory.createQueue(1, QueueType.NORMAL);

        assertInstanceOf(BackOffBlockingQueue.class, queue);
        assertInstanceOf(ArrayBlockingQueue.class, queue2);
    }

}