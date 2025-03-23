package adaptor;

import data.PriceEvent;
import feed.PriceFeedHandler;
import util.SimulatePriceFeeder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import publisher.Publisher;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceAdaptorTest {
    @Mock
    Publisher<PriceEvent> publisher;

    @Test
    public void testFeedHaveDataThenPricePublish() throws Exception {
        SimulatePriceFeeder priceFeeder = new SimulatePriceFeeder();
        PriceFeedHandler priceFeedHandler = new PriceFeedHandler();
        PriceAdaptor priceAdaptor = new PriceAdaptor(priceFeedHandler, publisher, priceFeeder);
        priceAdaptor.start();
        String data = "9:30 AM AUD/USD 0.6905 106,198";
        priceFeeder.pushData(data);
        Thread.sleep(10);
        verify(publisher, times(1)).publish(any(PriceEvent.class));
        priceAdaptor.stop();
    }

    @Test
    public void testWrongFeedWillNotCrashSystemAndSkipTheMessage() throws Exception {
        SimulatePriceFeeder priceFeeder = new SimulatePriceFeeder();
        PriceFeedHandler priceFeedHandler = new PriceFeedHandler();
        PriceAdaptor priceAdaptor = new PriceAdaptor(priceFeedHandler, publisher, priceFeeder);
        priceAdaptor.start();
        String data = "9:30 AM AUD/USD 0.6905 106,198";
        priceFeeder.pushData(data);
        data = "sdfaljlfsadjf;AUD/USD;0.6905;106,198";
        priceFeeder.pushData(data);
        data = "9:31 AM AUD/USD 0.3920 20,198";
        priceFeeder.pushData(data);
        Thread.sleep(10);
        verify(publisher, times(2)).publish(any(PriceEvent.class));
        priceAdaptor.stop();
    }

}