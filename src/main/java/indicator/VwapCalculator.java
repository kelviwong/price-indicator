package indicator;

import common.TimeProvider;
import data.AnalyticData;
import data.Price;

import java.util.ArrayDeque;
import java.util.List;

public class VwapCalculator implements Calculator {
//    long previousTotalVol = 0;
//    double previousPriceVol = 0;
    TimeProvider timeProvider;
    private final static int timeLengthInMin = 60;

//    private Map<String, AnalyticData> analyticDataMap;

    public VwapCalculator(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
//        this.analyticDataMap = new HashMap<>();
    }

    @Override
    public double calculate(List<Price> priceData, ArrayDeque<Price> arrayDeque, AnalyticData analyticData) {
        int size = priceData.size();
        long totalVol = 0L;
        double accumulatePriceVol = 0.0d;
//        AnalyticData analyticData = analyticDataMap.computeIfAbsent(data.getCurrency(), (k) -> new AnalyticData());
//        long previousTotalVol = analyticData.getTotalVol();
//        double previousPriceVol = analyticData.getTotalPriceVol();

        arrayDeque.addAll(priceData);

        //we can use enhanced for loop here, escape analysis should optimize to put the iterator to stack
        for (int i = 0; i < size; i++) {
            Price data = priceData.get(i);
            totalVol += data.getVolume();
            accumulatePriceVol += data.getVolume() * data.getPrice();
        }

        double vwap = accumulatePriceVol / totalVol;
        updateVwap(analyticData, vwap, totalVol, accumulatePriceVol);

        return analyticData.getVwap();
    }

    public double calculateWithDelta(Price newData, ArrayDeque<Price> arrayDeque, AnalyticData analyticData) {
        Price data = arrayDeque.peek();
//        AnalyticData analyticData = analyticDataMap.computeIfAbsent(data.getCurrency(), (k) -> new AnalyticData());
        long previousTotalVol = analyticData.getTotalVol();
        double previousPriceVol = analyticData.getTotalPriceVol();

        long now = timeProvider.now();
        long expiredTimeInMills = now - timeLengthInMin * 60 * 1000L;

        while (data != null && data.getTimestamp() < expiredTimeInMills) {
            Price oldData = arrayDeque.pop();
            previousTotalVol -= oldData.getVolume();
            previousPriceVol -= oldData.getVolume() * oldData.getPrice();
            data = arrayDeque.peek();
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
