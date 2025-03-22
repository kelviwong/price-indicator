package common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class LocalDateTimeProvider implements TimeProvider {
    @Override
    public long now() {
        LocalDateTime currentDate = LocalDateTime.now();
        // Convert to milliseconds since epoch
        ZonedDateTime zonedDateTime = currentDate.atZone(ZoneId.systemDefault());
        return zonedDateTime.toInstant().toEpochMilli();
    }
}
