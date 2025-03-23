package indicator;

import common.TimeProvider;
import data.AnalyticData;
import data.Price;
import storage.IStore;

import java.util.ArrayDeque;
import java.util.List;

public class VwapCalculator implements Calculator {
    private TimeProvider timeProvider;
    private final static int timeLengthInMin = 60;

    public VwapCalculator(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public double calculate(List<Price> priceData, IStore<Price> store, AnalyticData analyticData) {
        int size = priceData.size();
        long totalVol = 0L;
        double accumulatePriceVol = 0.0d;

        store.writeAll(priceData);

        //we can use enhanced for loop here, escape analysis should optimize to put the iterator to stack
        for (int i = 0; i < size; i++) {
            Price data = priceData.get(i);
            totalVol += data.getVolume();
            accumulatePriceVol += data.getVolume() * data.getPrice();
        }

        if (totalVol == 0) {
            return 0;
        }

        double vwap = accumulatePriceVol / totalVol;
        updateVwap(analyticData, vwap, totalVol, accumulatePriceVol);

        return analyticData.getVwap();
    }

    Price oldData = new Price();
    Price peekData = new Price();
    public double calculateWithDelta(Price newData, IStore<Price> store, AnalyticData analyticData) {
        store.peek(peekData);
        long previousTotalVol = analyticData.getTotalVol();
        double previousPriceVol = analyticData.getTotalPriceVol();

        long now = timeProvider.now();
        long expiredTimeInMills = now - timeLengthInMin * 60 * 1000L;

        store.write(newData);

        while (peekData != null && peekData.getTimestamp() != 0 && peekData.getTimestamp() < expiredTimeInMills) {
            oldData.setCurrency(newData.getCurrency());
            store.read(oldData);
            previousTotalVol -= oldData.getVolume();
            previousPriceVol -= oldData.getVolume() * oldData.getPrice();
            store.peek(peekData);
        }

        previousTotalVol += newData.getVolume();
        previousPriceVol += newData.getPrice() * newData.getVolume();

        if (previousTotalVol == 0) {
            return 0;
        }

        double vwap = previousPriceVol / previousTotalVol;
        updateVwap(analyticData, vwap, previousTotalVol, previousPriceVol);
        return analyticData.getVwap();
    }

    private static void updateVwap(AnalyticData analyticData, double vwap, long previousTotalVol, double previousPriceVol) {
        analyticData.setVwap(vwap);
        analyticData.setTotalVol(previousTotalVol);
        analyticData.setTotalPriceVol(previousPriceVol);
    }
}
