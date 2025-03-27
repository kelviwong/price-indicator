package indicator;

import common.MockTimeProvider;
import config.Config;
import data.AnalyticData;
import data.Price;
import data.WritableMutableCharSequence;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import storage.PriceDequeStore;
import util.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VwapCalculatorTest {

    static Config config;
    private static WritableMutableCharSequence AUDUSDStr;

    @BeforeAll
    public static void setup() throws IOException {
        config = Data.getConfig();
        AUDUSDStr = new WritableMutableCharSequence(20);
        AUDUSDStr.copy("AUD/USD");
    }

    private static void setupData(List<Price> priceList, MockTimeProvider mockTimeProvider) {
        mockTimeProvider.advanceInMinutes(-61);
        WritableMutableCharSequence writableMutableCharSequence = new WritableMutableCharSequence(20);
        writableMutableCharSequence.copy("AUD/USD");
        priceList.add(new Price(writableMutableCharSequence, mockTimeProvider.now(), 12.5, 2000));
        mockTimeProvider.advanceInMinutes(5);
        priceList.add(new Price(writableMutableCharSequence, mockTimeProvider.now(), 10.5, 1000));
        mockTimeProvider.advanceInMinutes(10);
        priceList.add(new Price(writableMutableCharSequence, mockTimeProvider.now(), 14.6, 4000));
        mockTimeProvider.advanceInMinutes(20);
        priceList.add(new Price(writableMutableCharSequence, mockTimeProvider.now(), 11.5, 13000));
        mockTimeProvider.advanceInMinutes(20);
        priceList.add(new Price(writableMutableCharSequence, mockTimeProvider.now(), 10.4, 12000));
        mockTimeProvider.advanceInMinutes(6);
    }

    private static List<Price> setupData(MockTimeProvider mockTimeProvider) {
        List<Price> priceList = new ArrayList<>();
        setupData(priceList, mockTimeProvider);
        return priceList;
    }

    @Test
    public void testVwapPriceWhenVolIsZero() {
        MockTimeProvider mockTimeProvider = new MockTimeProvider();
        PriceDequeStore deque = new PriceDequeStore();
        Calculator calculator = new VwapCalculator(config);
        WritableMutableCharSequence writableMutableCharSequence = new WritableMutableCharSequence(20);
        writableMutableCharSequence.copy("AUD/USD");
        AnalyticData analyticData = new AnalyticData(writableMutableCharSequence);
        List<Price> priceList = new ArrayList<>();
        priceList.add(new Price(AUDUSDStr, mockTimeProvider.now(), 12.5, 0));
        double vwap = calculator.calculate(priceList, deque, analyticData);
        assertEquals(0, vwap);
    }

    @Test
    public void testVwapPrice() {
        MockTimeProvider mockTimeProvider = new MockTimeProvider();
        PriceDequeStore deque = new PriceDequeStore();
        Calculator calculator = new VwapCalculator(config);
        WritableMutableCharSequence writableMutableCharSequence = new WritableMutableCharSequence(20);
        writableMutableCharSequence.copy("AUD/USD");
        AnalyticData analyticData = new AnalyticData(writableMutableCharSequence);
        List<Price> priceList;

        priceList = setupData(mockTimeProvider);
        double vwap = calculator.calculate(priceList, deque, analyticData);
        assertEquals(11.50625, vwap);

        // add one price that within 1 hour
        mockTimeProvider.advanceInMinutes(-1);
        mockTimeProvider.showCurrentTime();
        Price newData = new Price(AUDUSDStr, mockTimeProvider.now(), 10.4, 12000);
        vwap = calculator.calculateWithDelta(newData, deque, analyticData);
        assertEquals(11.204545454545455, vwap);
        //back to current Time
        mockTimeProvider.advanceInMinutes(1);

        // add one price after hour, it should remove the first records
        mockTimeProvider.advanceInMinutes(2);
        newData = new Price(AUDUSDStr, mockTimeProvider.now(), 10.4, 12000);
        vwap = calculator.calculateWithDelta(newData, deque, analyticData);
        assertEquals(10.977777777777778, vwap);

        mockTimeProvider.advanceInMinutes(2);
        newData = new Price(AUDUSDStr, mockTimeProvider.now(), 10.4, 12000);
        vwap = calculator.calculateWithDelta(newData, deque, analyticData);
        assertEquals(10.872727272727273, vwap);

        // even a wrong vol price feed, it should still re-calculate again, so 2nd item is removed
        mockTimeProvider.advanceInMinutes(5);
        newData = new Price(AUDUSDStr, mockTimeProvider.now(), 10.4, 0);
        vwap = calculator.calculateWithDelta(newData, deque, analyticData);
        assertEquals(10.878461538461538, vwap);

        // advanced to after 60 minutes, then it should left the newly exist data.
        mockTimeProvider.advanceInMinutes(53);
        newData = new Price(AUDUSDStr, mockTimeProvider.now(), 10.5, 12000);
        vwap = calculator.calculateWithDelta(newData, deque, analyticData);
        assertEquals(10.433333333333334, vwap);
    }

}