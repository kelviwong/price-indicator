package indicator;

import data.AnalyticData;
import data.Price;

import java.util.ArrayDeque;
import java.util.List;

public interface Calculator {
    double calculate(List<Price> priceData, ArrayDeque<Price> arrayDeque, AnalyticData analyticData);
    double calculateWithDelta(Price newData, ArrayDeque<Price> arrayDeque, AnalyticData analyticData);
}
