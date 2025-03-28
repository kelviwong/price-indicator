package publisher;

import data.Event;
import queue.MessageQueue;

import java.util.concurrent.BlockingQueue;

public class PriceReader<T extends Event<?>> implements Reader<T> {
    MessageQueue<T> queue;

    public PriceReader(MessageQueue<T> queue) {
        this.queue = queue;
    }

    @Override
    public T poll() throws InterruptedException {
        return queue.take();
    }
}
