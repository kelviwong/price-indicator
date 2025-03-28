package queue;

import com.lmax.disruptor.dsl.Disruptor;
import data.Event;
import org.agrona.concurrent.AbstractConcurrentArrayQueue;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;

public class QueueFactory {
    private static final Logger logger = LoggerFactory.getLogger(QueueFactory.class);
    public static <T> MessageQueue<T> createMessageQueue(int size, QueueType type, BackOffStrategy<T> strategy) throws Exception {
        QueueWriter<T> queueWriter;
//        if (type == QueueType.DISRUPTOR_BACKOFF) {
//            queueWriter = new DisruptorWriter<>(disruptor);
//        } else if (type == QueueType.AGRONA_BACKOFF) {
        if (type == QueueType.AGRONA_BACKOFF) {
            AbstractConcurrentArrayQueue<T> queue = new ManyToOneConcurrentArrayQueue<>(size);
            IdleStrategy idleStrategy = new BackoffIdleStrategy(
                    100,
                    100,
                    1,
                    10_000
            );
            queueWriter = new AgronaQueueWriter<>(queue, idleStrategy);
        } else {
            ArrayBlockingQueue<T> queue = new ArrayBlockingQueue<>(size);
            queueWriter = new BlockingQueueWriter<>(queue);
        }

        logger.info("Creating MessageQueue: {}, {},{} ,{} ", size, type, strategy, queueWriter);

        return new MessageQueue<T>(queueWriter, strategy);
    }

    public static <T> MessageQueue<T> createMessageQueue(int size, QueueType type) throws Exception {
        BackOffStrategy<T> retryThenDropStrategy = new RetryThenDropStrategy<>(3, 100, new LogDropConsumer<>());
        return createMessageQueue(size, type, retryThenDropStrategy);
    }

}
