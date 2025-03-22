package data;

import lombok.Data;

@Data
public class Price {
    String currency;
    long timestamp;
    double price;
    long volume;

    public Price(String currency, long timestamp, double price, long volume) {
        this.currency = currency;
        this.timestamp = timestamp;
        this.price = price;
        this.volume = volume;
    }
}
