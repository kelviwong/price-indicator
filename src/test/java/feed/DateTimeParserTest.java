package feed;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeParserTest {

    @Test
    public void Given_Time_In_H_mm_s_Should_return_correct_local_time() {
        String time = "9:30 AM";
        LocalTime localTime = DateTimeParser.parseTimeManually(time);
        assertEquals(9, localTime.getHour());
        assertEquals(30, localTime.getMinute());
        time = "10:30 PM";
        localTime = DateTimeParser.parseTimeManually(time);
        assertEquals(22, localTime.getHour());
        assertEquals(30, localTime.getMinute());
        time = "12:00 AM";
        localTime = DateTimeParser.parseTimeManually(time);
        assertEquals(0, localTime.getHour());
        assertEquals(0, localTime.getMinute());
        time = "12:00 PM";
        localTime = DateTimeParser.parseTimeManually(time);
        assertEquals(12, localTime.getHour());
        assertEquals(0, localTime.getMinute());
    }

}