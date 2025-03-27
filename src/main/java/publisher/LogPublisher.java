package publisher;

import data.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogPublisher<T extends Event<T>> implements Publisher<T> {
    private static final Logger logger = LoggerFactory.getLogger(LogPublisher.class);

    public LogPublisher() {
        logger.info("Started Log Publisher");
    }

    @Override
    public void publish(T data) {
//        logger.info("Data: {}", data);
    }
}
