package feeder;

public class CmdPriceFeeder extends AbstractQueueFeeder<String> {

    public CmdPriceFeeder() throws Exception {
    }

    @Override
    public String getData() throws InterruptedException {
        return queue.take();
    }

    @Override
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
