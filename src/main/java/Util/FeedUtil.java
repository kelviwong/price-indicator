package util;

public class FeedUtil {
    public static double parseDouble(String s, int start, int end) {
        if (s.charAt(start) == '-') {
            throw new IllegalArgumentException("negative number is not supported");
        }
        long intPart = 0;
        long fracPart = 0;
        int fracLen = 0;
        boolean seenDot = false;

        for (int i = start; i < end; i++) {
            char c = s.charAt(i);
            if (c == '.') {
                seenDot = true;
                continue;
            }
            int digit = c - '0';
            if (!seenDot) {
                intPart = intPart * 10 + digit;
            } else {
                fracPart = fracPart * 10 + digit;
                fracLen++;
            }
        }

        return intPart + (fracPart / Math.pow(10, fracLen));
    }

    public static long parseLongWithCommas(String s, int start, int end) {
        if (s.charAt(start) == '-') {
            throw new IllegalArgumentException("negative number is not supported");
        }

        long result = 0;
        for (int i = start; i < end; i++) {
            char c = s.charAt(i);
            if (c == ',') continue; // skip comma
            result = result * 10 + (c - '0');
        }
        return result;
    }
}
