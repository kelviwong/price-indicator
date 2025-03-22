package indicator;

import common.TimeProvider;
import data.Price;

import java.util.ArrayDeque;
import java.util.List;

public class VwapIndicator implements Indicator {
    long previousTotalVol = 0;
    double previousPriceVol = 0;

    ArrayDeque<Price> storedPrice = new ArrayDeque<>();

    TimeProvider timeProvider;

    public VwapIndicator(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public double calculate(List<Price> priceData) {
        int size = priceData.size();
        long totalVol = 0L;
        double accumulatePriceVol = 0.0d;

        storedPrice.addAll(priceData);

        //we can use enhanced for loop here, escape analysis should optimize to put the iterator to stack
        for (int i = 0; i < size; i++) {
            Price data = priceData.get(i);
            totalVol += data.getVolume();
            accumulatePriceVol += data.getVolume() * data.getPrice();
        }

        previousTotalVol = totalVol;
        previousPriceVol = accumulatePriceVol;

        return accumulatePriceVol / totalVol;
    }

//    public void onPriceUpdate(Price newData) {
//        double vwap = calculateWithDelta(newData, 60);
//
//    }

    public double calculateWithDelta(Price newData, long timeLengthInMin) {
        Price data = storedPrice.peek();
        long now = timeProvider.now();
        long epochMilli = now - timeLengthInMin * 60 * 1000;

        while (data.getTimestamp() < epochMilli) {
            Price oldData = storedPrice.pop();
            previousTotalVol -= oldData.getVolume();
            previousPriceVol -= oldData.getVolume() * oldData.getPrice();
            data = storedPrice.peek();
        }

        previousTotalVol += newData.getVolume();
        previousPriceVol += newData.getPrice() * newData.getVolume();

        if (previousTotalVol == 0) {
            return 0;
        }

        return previousPriceVol / previousTotalVol;
    }
}
