package adaptor;

import data.Price;
import data.WritableMutableCharSequence;
import feed.PriceFeedHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PriceFeedHandlerTest {

    @Test
    public void whenPriceFeedDataThenShouldReturnNormalizePrice() throws Exception {
        PriceFeedHandler handler = new PriceFeedHandler();
        WritableMutableCharSequence writableMutableCharSequence = new WritableMutableCharSequence(20);
        writableMutableCharSequence.copy("AUD/USD");

        String feed = "9:30 AM AUD/USD 0.6905 106,198";
        Price price = handler.process(feed);
        assertEquals(writableMutableCharSequence, price.getCurrency());
        assertEquals(0.6905, price.getPrice());
        assertEquals(106198, price.getVolume());
    }

}