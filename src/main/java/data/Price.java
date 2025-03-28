package data;

import lombok.Data;

import java.nio.MappedByteBuffer;

@Data
public class Price implements MemoryFileAware<Price> {
    String currency;
    long timestamp;
    double price;
    long volume;

    // total size of timestamp , price and volume
    static final int RECORD_SIZE = 24;
    public Price() {
    }

    public Price(String currency, long timestamp, double price, long volume) {
        this.currency = currency;
        this.timestamp = timestamp;
        this.price = price;
        this.volume = volume;
    }

    @Override
    public void writeToByte(MappedByteBuffer buffer) {
        buffer.putLong(timestamp);
        buffer.putDouble(price);
        buffer.putLong(volume);
    }

    @Override
    public void readFromByte(MappedByteBuffer buffer, int currentReadRecord) {
        int timeTampPos = currentReadRecord * RECORD_SIZE;
        long timestamp = buffer.getLong(timeTampPos);
        double price = buffer.getDouble(timeTampPos + 8);
        long volume = buffer.getLong(timeTampPos + 16);

        setTimestamp(timestamp);
        setPrice(price);
        setVolume(volume);
    }

    public void clear() {
        this.currency = "";
        this.timestamp = 0L;
        this.price = 0.0;
        this.volume = 0L;
    }
}
