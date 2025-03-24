package feeder;

import data.Price;
import queue.QueueFactory;
import queue.QueueType;

import java.util.concurrent.BlockingQueue;

public abstract class AbstractQueueFeeder<T> implements PriceFeeder<T> {
    protected BlockingQueue<String> queue;

    public AbstractQueueFeeder() throws Exception {
        this.queue = QueueFactory.createQueue(10000, QueueType.BACKOFF);;
    }
}
