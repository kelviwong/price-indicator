package publisher;

import java.util.concurrent.ArrayBlockingQueue;

public class PriceReader<T> {
    ArrayBlockingQueue<T> queue;

    public PriceReader(ArrayBlockingQueue<T> queue) {
        this.queue = queue;
    }

    public T poll() throws InterruptedException {
        return queue.take();
    }
}
