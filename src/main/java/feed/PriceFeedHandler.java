package feed;

import data.Price;
import vaildation.PriceValidationRule;
import vaildation.ValidationRule;
import vaildation.VolumeValidationRule;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class PriceFeedHandler implements FeedHandler<String> {
    private final Set<ValidationRule<Price>> validationRules;
//    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    final StringBuilder sb = new StringBuilder();

    public PriceFeedHandler() {
        validationRules = new HashSet<>();
        validationRules.add(new PriceValidationRule());
        validationRules.add(new VolumeValidationRule());
    }

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
        Price ultimatePrice = new Price(feedSplit[2], time, price, volume);
        for (ValidationRule<Price> rule : validationRules) {
            String check = rule.check(ultimatePrice);
            if (!check.isEmpty()) {
                throw new Exception(check);
            }
        }
        return ultimatePrice;
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
        // profiler show that this generate a bit garbage, improve by manually parsing it.
//        LocalTime localTime = LocalTime.parse(feed.trim(), timeFormatter);
        LocalTime localTime = DateTimeParser.parseTimeManually(feed.trim());

        // Combine with the current date
        LocalDate currentDate = LocalDate.now();
        LocalDateTime dateTime = LocalDateTime.of(currentDate, localTime);

        // Convert to milliseconds since epoch
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());
        return zonedDateTime.toInstant().toEpochMilli();
    }

}
