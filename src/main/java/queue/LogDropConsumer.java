package queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class LogDropConsumer<T> implements Consumer<T> {
    protected static final Logger logger = LoggerFactory.getLogger(LogDropConsumer.class);

    @Override
    public void accept(T t) {
        logger.info("Back Pressure, Dropping data: {}", t.toString());
    }
}
