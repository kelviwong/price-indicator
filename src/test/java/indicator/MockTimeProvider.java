package indicator;

import common.TimeProvider;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class MockTimeProvider implements TimeProvider {

    long currentTime;
    public MockTimeProvider() {
        LocalDateTime currentDate = LocalDateTime.now();
        // Convert to milliseconds since epoch
        ZonedDateTime zonedDateTime = currentDate.atZone(ZoneId.systemDefault());
        currentTime = zonedDateTime.toInstant().toEpochMilli();
    }

    @Override
    public long now() {
        return currentTime;
    }

    public void advanceInMinutes(int min) {
        currentTime += min * 60 * 1000L;
    }
}
