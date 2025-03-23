package indicator;

import data.AnalyticData;
import data.Price;
import storage.IStore;

import java.util.ArrayDeque;
import java.util.List;

public interface Calculator {
    double calculate(List<Price> priceData, IStore<Price> store, AnalyticData analyticData);
    double calculateWithDelta(Price newData, IStore<Price> store, AnalyticData analyticData);
}
