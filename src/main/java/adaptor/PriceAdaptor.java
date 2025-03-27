package adaptor;

import common.NamedThreadFactory;
import data.*;
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
    private final FeedHandler<String> feedHandler;
    private final Publisher<Event<Price>> publisher;
    private final PriceFeeder<String> feeder;
    private volatile boolean isStop;

    private static final Logger logger = LoggerFactory.getLogger(PriceAdaptor.class);

    EventFactory eventFactory;

    public PriceAdaptor(FeedHandler<String> feedHandler, Publisher<Event<Price>> publisher, PriceFeeder<String> feeder, EventFactory eventFactory) {
        this.feedHandler = feedHandler;
        this.publisher = publisher;
        this.feeder = feeder;
        this.isStop = false;
        this.eventFactory = eventFactory;

        executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("PriceAdaptor"));
    }

    @Override
    public void process() throws Exception {
        String data = feeder.getData();
        Event<Price> event = eventFactory.getEvent(EventType.PRICE);
        Price price = feedHandler.process(data);
        event.setData(price);
        publisher.publish(event);

    }

    @Override
    public void start() {
        logger.info("Starting adaptor on Thead");
        executorService.execute(() -> {
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
