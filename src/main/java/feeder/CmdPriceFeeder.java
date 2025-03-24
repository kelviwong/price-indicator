package feeder;

import queue.QueueFactory;
import queue.QueueType;

import java.util.concurrent.BlockingQueue;

public class CmdPriceFeeder implements PriceFeeder<String>{

    BlockingQueue<String> queue;

    public CmdPriceFeeder() throws Exception {
        this.queue = QueueFactory.createQueue(10000, QueueType.BACKOFF);
    }

    @Override
    public String getData() throws InterruptedException {
        return queue.take();
    }

    public void pushData(String data) {
        queue.offer(data);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
