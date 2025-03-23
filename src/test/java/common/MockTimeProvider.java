package common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storage.MemoryMapFileStore;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class MockTimeProvider implements TimeProvider {
    private static final Logger logger = LoggerFactory.getLogger(MockTimeProvider.class);

    long currentTime;
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public MockTimeProvider() {
        LocalDateTime currentDate = LocalDateTime.now();
        // Convert to milliseconds since epoch
        ZonedDateTime zonedDateTime = currentDate.atZone(ZoneId.systemDefault());
        //stamp the current time when create
        currentTime = zonedDateTime.toInstant().toEpochMilli();
    }

    public void setCurrentTime(String time) {
        LocalTime specificTime = LocalTime.parse(time, timeFormatter);

        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Combine the current date with the specific time to create a LocalDateTime object
        LocalDateTime currentDateTime = LocalDateTime.of(currentDate, specificTime);

        // Convert to ZonedDateTime using the system default time zone
        ZonedDateTime zonedDateTime = currentDateTime.atZone(ZoneId.systemDefault());

        // Convert to milliseconds since epoch
        currentTime = zonedDateTime.toInstant().toEpochMilli();
    }

    @Override
    public long now() {
        return currentTime;
    }

    public void advanceInMinutes(int min) {
        currentTime += mins(min);
    }

    public void advanceInSecond(int second) {
        currentTime += seconds(second);
    }

    private long seconds(int seconds) {
        return seconds * 1000L;
    }

    private long mins(int mins) {
        return seconds(mins * 60);
    }

    public void showCurrentTime() {
        Instant instant = Instant.ofEpochMilli(currentTime);
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = dateTime.format(formatter);
        logger.info("Current Time {}", formattedDateTime);
    }

    public String getCurrentTime() {
        Instant instant = Instant.ofEpochMilli(currentTime);
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, zoneId);
        String formattedDateTime = dateTime.format(timeFormatter);
        return formattedDateTime;
    }
}
