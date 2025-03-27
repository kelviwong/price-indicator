package data;

import lombok.Getter;
import lombok.Setter;

public class PriceEvent extends Event<Price> {
    public PriceEvent() {
        super();
    }

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
