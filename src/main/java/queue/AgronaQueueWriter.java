package queue;

import data.Event;
import org.agrona.concurrent.AbstractConcurrentArrayQueue;
import org.agrona.concurrent.IdleStrategy;

public class AgronaQueueWriter<T> implements QueueWriter<T> {
    AbstractConcurrentArrayQueue<T> queue;
    private final IdleStrategy idleStrategy;

    public AgronaQueueWriter(AbstractConcurrentArrayQueue<T> queue, IdleStrategy idleStrategy) {
        this.queue = queue;
        this.idleStrategy = idleStrategy;
    }

    @Override
    public boolean write(T item) throws Exception {
        try {
            return queue.offer(item);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public T take() {
        T item = queue.poll();
        if (item == null) {
            idleStrategy.idle(); // spin/yield/park depending on phase
        } else {
            idleStrategy.reset();
            return item;
        }

        return null;
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
