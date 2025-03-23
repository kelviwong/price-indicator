package indicator;

import common.MockTimeProvider;
import data.AnalyticData;
import data.Price;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VwapCalculatorTest {
    private static void setupData(List<Price> priceList, MockTimeProvider mockTimeProvider) {
        mockTimeProvider.advanceInMinutes(-61);
        priceList.add(new Price("AUD/USD", mockTimeProvider.now(), 12.5, 2000));
        mockTimeProvider.advanceInMinutes(5);
        priceList.add(new Price("AUD/USD", mockTimeProvider.now(), 10.5, 1000));
        mockTimeProvider.advanceInMinutes(10);
        priceList.add(new Price("AUD/USD", mockTimeProvider.now(), 14.6, 4000));
        mockTimeProvider.advanceInMinutes(20);
        priceList.add(new Price("AUD/USD", mockTimeProvider.now(), 11.5, 13000));
        mockTimeProvider.advanceInMinutes(20);
        priceList.add(new Price("AUD/USD", mockTimeProvider.now(), 10.4, 12000));
        mockTimeProvider.advanceInMinutes(6);
    }

    private static List<Price> setupData(MockTimeProvider mockTimeProvider) {
        List<Price> priceList = new ArrayList<>();
        setupData(priceList, mockTimeProvider);
        return priceList;
    }

    @Test
    public void testVwapPrice() {
        MockTimeProvider mockTimeProvider = new MockTimeProvider();
        ArrayDeque<Price> deque = new ArrayDeque<>();
        Calculator calculator = new VwapCalculator(mockTimeProvider);
        AnalyticData analyticData = new AnalyticData("AUD/USD");
        List<Price> priceList = setupData(mockTimeProvider);
        deque.addAll(priceList);
        double vwap = calculator.calculate(priceList, deque, analyticData);
        assertEquals(11.50625, vwap);

        // add one price that within 1 hour
        mockTimeProvider.advanceInMinutes(-1);
        Price newData = new Price("AUD/USD", mockTimeProvider.now(), 10.4, 12000);
        vwap = calculator.calculateWithDelta(newData, deque, analyticData);
        assertEquals(11.204545454545455, vwap);

        mockTimeProvider.advanceInMinutes(2);
        newData = new Price("AUD/USD", mockTimeProvider.now(), 10.4, 12000);
        vwap = calculator.calculateWithDelta(newData, deque, analyticData);
        assertEquals(10.977777777777778, vwap);
    }

}