package publisher;

import data.Event;
import queue.MessageQueue;

import java.util.concurrent.BlockingQueue;

public class PricePublisher<T extends Event<?>> implements Publisher<T> {
    MessageQueue<T> queue;

    public PricePublisher(MessageQueue<T> queue) {
        this.queue = queue;
    }
    @Override
    public void publish(T data) {
        queue.publish(data);
    }
}
