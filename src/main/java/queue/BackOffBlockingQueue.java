package queue;

import java.util.concurrent.ArrayBlockingQueue;

public class BackOffBlockingQueue<T> extends ArrayBlockingQueue<T> {

    private final BackOffStrategy<T> strategy;
//    private final QueueWriter<T> queueWriter;

    public BackOffBlockingQueue(int capacity, BackOffStrategy<T> strategy) {
        super(capacity);
//        queueWriter = new BlockingQueueWriter<>(this);
        this.strategy = strategy;
    }

//    public boolean offer(T item) {
//        return strategy.offer(queueWriter, item);
//    }

    public T take() throws InterruptedException {
        return super.take();
    }
}
