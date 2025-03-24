package queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class QueueFactory {
    private static final Logger logger = LoggerFactory.getLogger(QueueFactory.class);

    public static <T> BlockingQueue<T> createQueue(int size, QueueType type, BackOffStrategy<T> strategy) throws Exception {
        if (type == QueueType.BACKOFF) {
            return new BackOffBlockingQueue<T>(size, strategy);
        }

        return new ArrayBlockingQueue<>(size);
    }

    public static <T> BlockingQueue<T> createQueue(int size, QueueType type) throws Exception {
        logger.info("Creating queue with type {}, {}", type, size);
        if (type == QueueType.BACKOFF) {
            return createQueue(size, QueueType.BACKOFF, new RetryThenDropStrategy<>(3, 100, new LogDropConsumer<T>()));
        }

        return new ArrayBlockingQueue<>(size);
    }

}
