package feed;

import java.time.LocalTime;

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
}
