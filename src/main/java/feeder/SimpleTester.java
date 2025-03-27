package feeder;

import latency.LatencyTracker;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleTester {
    private final ScheduledExecutorService executor;
    AbstractQueueFeeder<String> feeder;

    LatencyTracker latencyTracker;

    public SimpleTester(AbstractQueueFeeder<String> feeder, LatencyTracker latencyTracker) {
        this.feeder = feeder;
        executor = Executors.newSingleThreadScheduledExecutor();
        this.latencyTracker = latencyTracker;
    }

    StringBuilder sb = new StringBuilder();

    public ArrayList<String> generateString(int numberOfLine, long currentTime) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < numberOfLine; i++) {
            sb.append(getTime(currentTime)).append(" AUD/USD 0.6905 1,198");
            list.add(sb.toString());
            sb.setLength(0);
            sb.append(getTime(currentTime)).append(" JPY/USD 0.789 1,298");
            list.add(sb.toString());
            sb.setLength(0);
            currentTime += 1000L;
        }

        return list;
    }

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH);
    DateTimeFormatter incomingTime = DateTimeFormatter.ofPattern("H:mm a", Locale.ENGLISH);

    public long setCurrentTime(String time) {
        LocalTime specificTime = LocalTime.parse(time, timeFormatter);
        LocalDate currentDate = LocalDate.now();
        LocalDateTime currentDateTime = LocalDateTime.of(currentDate, specificTime);
        ZonedDateTime zonedDateTime = currentDateTime.atZone(ZoneId.systemDefault());
        return zonedDateTime.toInstant().toEpochMilli();
    }

    public String getTime(long time) {
        Instant instant = Instant.ofEpochMilli(time);
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, zoneId);
        return dateTime.format(incomingTime);
    }

    public void start() {
        long time = setCurrentTime("01:00:00");
        int numberOfLine = 30000;
        ArrayList<String> strings = generateString(numberOfLine, time);
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(numberOfLine);
        executor.scheduleAtFixedRate(() -> {
            try {
                String data = strings.get(counter.getAndIncrement());
                if (data == null) {
                    System.out.println("No data found");
                    throw new RuntimeException("This is the end.");
                }
                feeder.pushData(data);
                latch.countDown();
            } catch (Exception e) {
                System.out.println("No data found");
                throw new RuntimeException("This is the end.");
            }
        }, 0, 1, TimeUnit.MILLISECONDS);

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        latencyTracker.showStats();

        executor.shutdownNow();
    }
}
