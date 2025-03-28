package feeder;

import data.Event;
import data.Price;
import queue.MessageQueue;
import queue.QueueFactory;
import queue.QueueType;

import java.util.concurrent.BlockingQueue;

public abstract class AbstractQueueFeeder<T> implements PriceFeeder<T> {
    protected MessageQueue<T> queue;

    public AbstractQueueFeeder() throws Exception {
        this.queue = QueueFactory.createMessageQueue(10000, QueueType.AGRONA_BACKOFF, null);
        ;
    }
}
