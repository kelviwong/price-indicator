package publisher;

import java.util.concurrent.ArrayBlockingQueue;

public class PriceReader<T> implements Reader<T> {
    ArrayBlockingQueue<T> queue;

    public PriceReader(ArrayBlockingQueue<T> queue) {
        this.queue = queue;
    }

    @Override
    public T poll() throws InterruptedException {
        return queue.take();
    }
}
