package common;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LocalDateTimeProviderTest {
    @Test
    public void testLocalDateTimeProvider() {
        TimeProvider provider = new LocalDateTimeProvider();
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime zonedDateTime = now.atZone(ZoneId.systemDefault());
        long currentTimeMillis = zonedDateTime.toInstant().toEpochMilli();
        long nowFromProvider = provider.now();

        assertTrue(currentTimeMillis - nowFromProvider < 1);
    }

}