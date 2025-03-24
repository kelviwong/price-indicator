package queue;

import java.util.concurrent.ArrayBlockingQueue;

public class BackOffBlockingQueue<T> extends ArrayBlockingQueue<T> {

    private final BackOffStrategy<T> strategy;

    public BackOffBlockingQueue(int capacity, BackOffStrategy<T> strategy) {
        super(capacity);
        this.strategy = strategy;
    }

    public boolean offer(T item) {
        return strategy.offer(this, item);
    }

    public T take() throws InterruptedException {
        return super.take();
    }
}
