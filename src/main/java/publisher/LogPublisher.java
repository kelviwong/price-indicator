package publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogPublisher<T> implements Publisher<T> {
    private static final Logger logger = LoggerFactory.getLogger(LogPublisher.class);

    @Override
    public void publish(T data) {
        logger.info(data.toString());
    }
}
