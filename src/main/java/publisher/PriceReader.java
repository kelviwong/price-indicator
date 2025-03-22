package publisher;

import data.PriceEvent;

import java.util.concurrent.ArrayBlockingQueue;

public class PriceReader {
    ArrayBlockingQueue<PriceEvent> queue;

    public PriceReader(ArrayBlockingQueue<PriceEvent> queue) {
        this.queue = queue;
    }

    public PriceEvent poll() throws InterruptedException {
        return queue.take();
    }
}
