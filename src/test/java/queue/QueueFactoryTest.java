package queue;

import data.PriceEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class QueueFactoryTest {

    @Test
    public void testQueueFactory() throws Exception {
        MessageQueue<PriceEvent> queue = QueueFactory.createMessageQueue(10000, QueueType.BLOCKING_BACKOFF, null);;
        MessageQueue<PriceEvent> queue2 = QueueFactory.createMessageQueue(10000, QueueType.AGRONA_BACKOFF, null);
        MessageQueue<PriceEvent> queue3 = QueueFactory.createMessageQueue(10000, QueueType.NORMAL, null);

        assertInstanceOf(BlockingQueueWriter.class, queue.getQueueWriter());
        assertInstanceOf(AgronaQueueWriter.class, queue2.getQueueWriter());
        assertInstanceOf(BlockingQueueWriter.class, queue3.getQueueWriter());
    }

}