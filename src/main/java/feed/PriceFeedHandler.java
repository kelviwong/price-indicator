package feed;

import data.Price;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PriceFeedHandler implements FeedHandler<String> {
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    final StringBuilder sb = new StringBuilder();

    // The price feed data
    // TimeStamp currencyPair price volume
    @Override
    public Price process(String feed) throws Exception {
       // 9:30 AM AUD/USD 0.6905 106,198

        String[] feedSplit = feed.split(" ");
        if (feedSplit.length < 5) {
            throw new Exception("Incorrect feed. " + feed);
        }

        sb.setLength(0);
        long time = parseTime(sb.append(feedSplit[0]).append(" ").append(feedSplit[1]).toString());

        double price = Double.parseDouble(feedSplit[3]);
        long volume = parseLong(feedSplit[4], ',');
        return new Price(feedSplit[2], time, price, volume);
    }

    private long parseLong(String volumeStr, char removeChar) {
        int length = volumeStr.length();
        sb.setLength(0);
        for (int i = 0; i < length; i++) {
            char c = volumeStr.charAt(i);
            if (c == removeChar) {
                continue;
            }
            sb.append(c);
        }

        // this parseLong can further improve by directly consume sb
        // so as to reduce garbage
        return Long.parseLong(sb.toString());
    }

    private long parseTime(String feed) {
        // Parse the time string
        LocalTime localTime = LocalTime.parse(feed.trim(), timeFormatter);

        // Combine with the current date
        LocalDate currentDate = LocalDate.now();
        LocalDateTime dateTime = LocalDateTime.of(currentDate, localTime);

        // Convert to milliseconds since epoch
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());
        return zonedDateTime.toInstant().toEpochMilli();
    }

}
