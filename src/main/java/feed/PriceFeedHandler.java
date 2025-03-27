package feed;

import data.Price;
import data.WritableMutableCharSequence;
import vaildation.PriceValidationRule;
import vaildation.ValidationRule;
import vaildation.VolumeValidationRule;

import java.util.*;

import static util.DateTimeParser.parseTime;
import static util.FeedUtil.parseDouble;
import static util.FeedUtil.parseLongWithCommas;

public class PriceFeedHandler implements FeedHandler<String> {
    private final List<ValidationRule<Price>> validationRules;
    //    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    final StringBuilder sb = new StringBuilder();

    public PriceFeedHandler() {
        validationRules = new ArrayList<>();
        validationRules.add(new PriceValidationRule());
        validationRules.add(new VolumeValidationRule());
    }

    Map<WritableMutableCharSequence,WritableMutableCharSequence> hashedSymbol = new HashMap<>();
    WritableMutableCharSequence tempCurrency = new WritableMutableCharSequence(20);

    // The price feed data
    // TimeStamp currencyPair price volume
    @Override
    public Price process(String feed) throws Exception {
        // 9:30 AM AUD/USD 0.6905 106,198
        int space1 = feed.indexOf(' ');
        int space2 = feed.indexOf(' ', space1 + 1);
//        String timeStr = feed.substring(0, space2);
//        long time = parseTime(timeStr);
        long time = parseTime(feed);

        int space3 = feed.indexOf(' ', space2 + 1);
        int space4 = feed.indexOf(' ', space3 + 1);

        double price = parseDouble(feed, space3 + 1, space4);
        long volume = parseLongWithCommas(feed, space4 + 1, feed.length());
        WritableMutableCharSequence currency = tempCurrency.append(feed, space2 + 1, space3);
        WritableMutableCharSequence symbol = hashedSymbol.get(currency);
        if (symbol == null) {
            symbol = new WritableMutableCharSequence(currency);
            hashedSymbol.put(symbol, symbol);
        }
        Price ultimatePrice = new Price(symbol, time, price, volume);

        int size = validationRules.size();
        for (int i = 0; i < size; i++) {
            String check = validationRules.get(i).check(ultimatePrice);
            if (!check.isEmpty()) {
                throw new Exception(check);
            }
        }

        return ultimatePrice;
    }
}
