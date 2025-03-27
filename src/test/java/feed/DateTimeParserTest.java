package feed;

import org.junit.jupiter.api.Test;
import util.DateTimeParser;

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

    @Test
    public void testDateTimeToMills(){
        long time = DateTimeParser.parseOrgiTime("12:00 AM");
        long time2 = DateTimeParser.parseTime("12:00 AM");
        assertEquals(time, time2);

        time = DateTimeParser.parseOrgiTime("12:00 PM");
        time2 = DateTimeParser.parseTime("12:00 PM");
        assertEquals(time, time2);

        time = DateTimeParser.parseOrgiTime("01:00 AM");
        time2 = DateTimeParser.parseTime("01:00 AM");
        assertEquals(time, time2);

        time = DateTimeParser.parseOrgiTime("06:00 PM");
        time2 = DateTimeParser.parseTime("06:00 PM");
        assertEquals(time, time2);

    }

}