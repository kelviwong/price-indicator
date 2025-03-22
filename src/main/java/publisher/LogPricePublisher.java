package publisher;

import data.PriceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogPricePublisher implements Publisher<PriceEvent> {
    private static final Logger logger = LoggerFactory.getLogger(LogPricePublisher.class);

    @Override
    public void publish(PriceEvent data) {
        logger.info(data.toString());
    }
}
