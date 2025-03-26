package common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private final String baseName;
    private final AtomicInteger counter = new AtomicInteger(1);
    private static final Logger logger = LoggerFactory.getLogger(NamedThreadFactory.class);

    public NamedThreadFactory(String baseName) {
        this.baseName = baseName;
    }

    @Override
    public Thread newThread(Runnable r) {
        //TODO: we can spin core in here
        Thread thread = new Thread(r);
        thread.setName(baseName + "-" + counter.getAndIncrement());
        thread.setUncaughtExceptionHandler((t, e) -> {
            logger.error("Uncaught exception in thread: " + t.getName());
            logger.error("Exception: " + e.getMessage());
        });
        return thread;
    }

}
