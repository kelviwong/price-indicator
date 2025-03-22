package indicator;

import data.Price;

import java.util.List;

public interface Indicator {
    double calculate(List<Price> priceData);
    double calculateWithDelta(Price newData);
}
