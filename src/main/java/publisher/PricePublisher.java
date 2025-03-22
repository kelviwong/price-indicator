package publisher;

import data.PriceEvent;

import java.util.concurrent.ArrayBlockingQueue;

public class PricePublisher implements Publisher<PriceEvent>{

    ArrayBlockingQueue<PriceEvent> queue;

    public PricePublisher(ArrayBlockingQueue<PriceEvent> queue) {
        this.queue = queue;
    }

    @Override
    public void publish(PriceEvent data) {
        queue.offer(data);
    }
}
