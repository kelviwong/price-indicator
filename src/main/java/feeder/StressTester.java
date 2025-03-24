package feeder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StressTester {
    private final ScheduledExecutorService executor;
    AbstractQueueFeeder<String> feeder;

    public StressTester(AbstractQueueFeeder<String> feeder) {
        this.feeder = feeder;
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
//        executorService.submit(() -> {
//            while (true) {
//                String input = "addprice 9:30 AM AUD/USD 0.6905 106,198";
//                feeder.pushData(input);
//                Thread.sleep(10);
//            }
//        });

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            feeder.pushData("addprice 9:30 AM AUD/USD 0.6905 106,198");
        }, 0, 1, TimeUnit.MILLISECONDS);
    }
}
