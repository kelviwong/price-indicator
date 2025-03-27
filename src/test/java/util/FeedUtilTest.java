package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeedUtilTest {

    @Test
    public void testParseDouble() {
        String doubles = "123.20";
        double value = FeedUtil.parseDouble(doubles, 0, doubles.length());
        assertEquals(123.2d, value);
        doubles = "0.343";
        value = FeedUtil.parseDouble(doubles, 0, doubles.length());
        assertEquals(0.343d, value);
        doubles = "123123123.232989";
        value = FeedUtil.parseDouble(doubles, 0, doubles.length());
        assertEquals(123123123.232989d, value);
        doubles = "0.34322342";
        value = FeedUtil.parseDouble(doubles, 0, doubles.length());
        assertEquals(0.34322342d, value);
    }

    @Test
    public void testParseDoubleShouldThrowWithNegative() {
        String doubles = "-0.34322342";
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            FeedUtil.parseDouble(doubles, 0, doubles.length());
        });

        assertEquals("negative number is not supported", ex.getMessage());
    }

    @Test
    public void testParseLongWithComma() {
        String doubles = "124,231";
        long value = FeedUtil.parseLongWithCommas(doubles, 0, doubles.length());
        assertEquals(124231, value);
        doubles = "123,123,343";
        value = FeedUtil.parseLongWithCommas(doubles, 0, doubles.length());
        assertEquals(123123343, value);
        doubles = "1,343";
        value = FeedUtil.parseLongWithCommas(doubles, 0, doubles.length());
        assertEquals(1343, value);
        doubles = "456";
        value = FeedUtil.parseLongWithCommas(doubles, 0, doubles.length());
        assertEquals(456, value);
        doubles = "1000";
        value = FeedUtil.parseLongWithCommas(doubles, 0, doubles.length());
        assertEquals(1000, value);
    }

    @Test
    public void testParseLongShouldThrowWithNegative() {
        String longs = "-123,232";
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            FeedUtil.parseLongWithCommas(longs, 0, longs.length());
        });

        assertEquals("negative number is not supported", ex.getMessage());
    }

}