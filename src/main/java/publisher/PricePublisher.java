package publisher;

import java.util.concurrent.BlockingQueue;

public class PricePublisher<T> implements Publisher<T> {
    BlockingQueue<T> queue;

    public PricePublisher(BlockingQueue<T> queue) {
        this.queue = queue;
    }
    @Override
    public void publish(T data) {
        queue.offer(data);
    }
}
