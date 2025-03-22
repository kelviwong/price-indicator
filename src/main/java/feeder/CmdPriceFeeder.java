package feeder;

import java.util.concurrent.ArrayBlockingQueue;

public class CmdPriceFeeder implements PriceFeeder<String>{

    ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(10000);

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
