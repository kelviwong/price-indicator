package adaptor;

import common.NamedThreadFactory;
import data.Price;
import data.PriceEvent;
import feeder.PriceFeeder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import publisher.Publisher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PriceAdaptor {
    private final ExecutorService executorService;
    FeedHandler<String> feedHandler;
    Publisher<PriceEvent> publisher;
    PriceFeeder<String> feeder;

    volatile boolean isStop;

    private static final Logger logger = LoggerFactory.getLogger(PriceAdaptor.class);
    public PriceAdaptor(FeedHandler<String> feedHandler, Publisher<PriceEvent> publisher, PriceFeeder<String> feeder) {
        this.feedHandler = feedHandler;
        this.publisher = publisher;
        this.feeder = feeder;
        this.isStop = false;

        executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("PriceAdaptor"));
    }

    public void process() throws Exception {
        while (!isStop) {
            String data = feeder.getData();
            Price price = feedHandler.process(data);
            PriceEvent event = new PriceEvent(price);
            publisher.publish(event);
        }
    }

    public void start() {
        logger.info("Starting adaptor on Thead");
        executorService.submit(() -> {
            try {
                process();
            } catch (Exception e) {
                logger.error("Error in Price Adaptor: ", e);
            }
        });
    }
    public void stop() {
        logger.info("Stopping adaptor");
        this.isStop = true;
    }

}
