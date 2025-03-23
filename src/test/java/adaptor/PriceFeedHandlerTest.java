package adaptor;

import data.Price;
import feed.PriceFeedHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PriceFeedHandlerTest {

    @Test
    public void whenPriceFeedDataThenShouldReturnNormalizePrice() throws Exception {
        PriceFeedHandler handler = new PriceFeedHandler();
        String feed = "9:30 AM AUD/USD 0.6905 106,198";
        Price price = handler.process(feed);
        assertEquals("AUD/USD", price.getCurrency());
        assertEquals(0.6905, price.getPrice());
        assertEquals(106198, price.getVolume());
    }

}