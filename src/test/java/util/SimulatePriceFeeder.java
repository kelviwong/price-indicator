package util;

import feeder.PriceFeeder;

import java.util.concurrent.ArrayBlockingQueue;

public class SimulatePriceFeeder implements PriceFeeder<String> {
    ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(10000);

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
