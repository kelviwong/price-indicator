package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

public class DateTimeParser {
    public static LocalTime parseTimeManually(String input) {
        int hour = (input.charAt(1) == ':')
                ? (input.charAt(0) - '0')
                : (input.charAt(0) - '0') * 10 + (input.charAt(1) - '0');

        int colonIndex = input.indexOf(':');
        int minute = (input.charAt(colonIndex + 1) - '0') * 10 + (input.charAt(colonIndex + 2) - '0');

        char ampmChar = input.charAt(input.length() - 2);

        if (ampmChar == 'P' && hour != 12) {
            hour += 12;
        } else if (ampmChar == 'A' && hour == 12) {
            hour = 0;
        }

        return LocalTime.of(hour, minute);
    }

    static LocalDate currentDate = LocalDate.now();
    static long dateEpochSecond = currentDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();

    public static  long parseTime(String feed) {
        LocalTime localTime = DateTimeParser.parseTimeManually(feed.trim());
        long seconds = localTime.toSecondOfDay();
        long epochMillis = (dateEpochSecond + seconds) * 1000L + localTime.getNano() / 1_000_000;
        return epochMillis;
    }

    /*
    dont use it in prod, for checking only in test case
     */
    public static long parseOrgiTime(String feed) {
        // Parse the time string
        // profiler show that this generate a bit garbage, improve by manually parsing it.
        LocalTime localTime = DateTimeParser.parseTimeManually(feed.trim());

        // Combine with the current date
        LocalDateTime dateTime = LocalDateTime.of(currentDate, localTime);

        // Convert to milliseconds since epoch
        ZoneOffset offset = ZoneOffset.UTC;
        long epochMillis = dateTime.toEpochSecond(offset) * 1000 + dateTime.getNano() / 1_000_000;
        return epochMillis;
    }
}
