package feeder;

import config.Config;

public class CmdPriceFeeder extends AbstractQueueFeeder<String> {

    public CmdPriceFeeder(Config config) throws Exception {
        super(config);
    }

    @Override
    public String getData() throws InterruptedException {
        return queue.take();
    }

    @Override
    public void pushData(String data) {
        queue.publish(data);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
