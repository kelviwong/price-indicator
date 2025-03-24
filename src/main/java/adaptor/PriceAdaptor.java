package adaptor;

import common.NamedThreadFactory;
import data.Price;
import data.PriceEvent;
import feed.FeedHandler;
import feeder.PriceFeeder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import publisher.Publisher;
import service.IService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PriceAdaptor implements IService, IAdaptor {
    private final ExecutorService executorService;
    private FeedHandler<String> feedHandler;
    private Publisher<PriceEvent> publisher;
    private PriceFeeder<String> feeder;
    private volatile boolean isStop;

    private static final Logger logger = LoggerFactory.getLogger(PriceAdaptor.class);

    public PriceAdaptor(FeedHandler<String> feedHandler, Publisher<PriceEvent> publisher, PriceFeeder<String> feeder) {
        this.feedHandler = feedHandler;
        this.publisher = publisher;
        this.feeder = feeder;
        this.isStop = false;

        executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("PriceAdaptor"));
    }

    @Override
    public void process() throws Exception {
        String data = feeder.getData();
        Price price = feedHandler.process(data);
        PriceEvent event = new PriceEvent(price);
        publisher.publish(event);

    }

    @Override
    public void start() {
        logger.info("Starting adaptor on Thead");
        executorService.submit(() -> {
            while (!isStop) {
                try {
                    process();
                } catch (Exception e) {
                    logger.error("Error in Price Adaptor: ", e);
                }
            }
        });
    }

    @Override
    public void stop() {
        logger.info("Stopping adaptor");
        this.isStop = true;
    }

}
