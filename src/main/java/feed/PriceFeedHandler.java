package feed;

import data.Price;
import util.DateTimeParser;
import vaildation.PriceValidationRule;
import vaildation.ValidationRule;
import vaildation.VolumeValidationRule;

import java.time.*;
import java.util.HashSet;
import java.util.Set;

import static util.FeedUtil.parseDouble;
import static util.FeedUtil.parseLongWithCommas;

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
        int space1 = feed.indexOf(' ');
        int space2 = feed.indexOf(' ', space1 + 1);
        String timeStr = feed.substring(0, space2);
        long time = parseTime(timeStr);

        int space3 = feed.indexOf(' ', space2 + 1);
        int space4 = feed.indexOf(' ', space3 + 1);

        double price = parseDouble(feed, space3 + 1, space4);
        long volume = parseLongWithCommas(feed, space4 + 1, feed.length());
        Price ultimatePrice = new Price(feed.substring(space2 + 1, space3), time, price, volume);
        for (ValidationRule<Price> rule : validationRules) {
            String check = rule.check(ultimatePrice);
            if (!check.isEmpty()) {
                throw new Exception(check);
            }
        }
        return ultimatePrice;
    }

    LocalDate currentDate = LocalDate.now();
    private long parseTime(String feed) {
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
