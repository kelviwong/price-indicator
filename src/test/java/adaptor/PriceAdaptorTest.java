package adaptor;

import data.PriceEvent;
import feeder.SimulatePriceFeeder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import publisher.Publisher;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceAdaptorTest {
    @Mock
    Publisher publisher;

    @Test
    public void testFeedHaveDataThenPricePublish() throws Exception {
        SimulatePriceFeeder priceFeeder = new SimulatePriceFeeder();
        PriceFeedHandler priceFeedHandler = new PriceFeedHandler();
        PriceAdaptor priceAdaptor = new PriceAdaptor(priceFeedHandler, publisher, priceFeeder);
        priceAdaptor.start();
        String data = "9:30 AM;AUD/USD;0.6905;106,198";
        priceFeeder.pushData(data);
        Thread.sleep(1);
        verify(publisher, times(1)).publish(any(PriceEvent.class));
    }

}