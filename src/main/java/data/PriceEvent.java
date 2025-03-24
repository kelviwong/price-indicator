package data;

public class PriceEvent extends Event<Price> {
    public PriceEvent(Price data) {
        super(data);
    }

    @Override
    public String toString() {
        return "PriceEvent{" +
                "data=" + data +
                '}';
    }
}
