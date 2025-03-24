package vaildation;

import common.MockTimeProvider;
import data.Price;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationRuleTest {

    @Test
    public void testPriceValidationRule(){
        MockTimeProvider timeProvider = new MockTimeProvider();
        Price price = new Price("AUD/USD", timeProvider.now(), 10.0, 1000);
        PriceValidationRule priceValidationRule = new PriceValidationRule();
        String check = priceValidationRule.check(price);
        assertEquals("", check);

        price.setPrice(0.0);
        check = priceValidationRule.check(price);
        assertEquals("Price is not greater than 0", check);
    }

    @Test
    public void testVolumeValidationRule(){
        MockTimeProvider timeProvider = new MockTimeProvider();
        Price price = new Price("AUD/USD", timeProvider.now(), 10.0, 1000);
        VolumeValidationRule volumeValidationRule = new VolumeValidationRule();
        String check = volumeValidationRule.check(price);
        assertEquals("", check);

        price.setVolume(0);
        check = volumeValidationRule.check(price);
        assertEquals("Volume is not greater than 0", check);
    }

}