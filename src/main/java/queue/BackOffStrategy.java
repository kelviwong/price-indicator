package queue;

import java.util.concurrent.BlockingQueue;

public interface BackOffStrategy<T> {
    boolean offer(BlockingQueue<T> queue, T item);
}
