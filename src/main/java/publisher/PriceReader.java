package publisher;

import data.Event;

import java.util.concurrent.BlockingQueue;

public class PriceReader<T extends Event<?>> implements Reader<T> {
    BlockingQueue<T> queue;

    public PriceReader(BlockingQueue<T> queue) {
        this.queue = queue;
    }

    @Override
    public T poll() throws InterruptedException {
        return queue.take();
    }
}
