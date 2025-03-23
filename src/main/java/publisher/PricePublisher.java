package publisher;

import java.util.concurrent.ArrayBlockingQueue;

public class PricePublisher<T> implements Publisher<T> {

    ArrayBlockingQueue<T> queue;

    public PricePublisher(ArrayBlockingQueue<T> queue) {
        this.queue = queue;
    }
    @Override
    public void publish(T data) {
        queue.offer(data);
    }
}
