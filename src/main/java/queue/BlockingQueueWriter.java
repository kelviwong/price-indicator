package queue;

import data.Event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockingQueueWriter<T> implements QueueWriter<T> {
    BlockingQueue<T> blockingQueue;

    public BlockingQueueWriter(BlockingQueue<T> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    @Override
    public boolean write(T item) {
        try {
            return blockingQueue.offer(item, 10, TimeUnit.MICROSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public T take() throws InterruptedException {
        return blockingQueue.take();
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
