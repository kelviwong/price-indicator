package publisher;

import data.Event;
import data.Resettable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ObjectPool;

import java.util.function.Supplier;

public class PooledPublisher<T extends Event<?> & Resettable> implements Publisher<T>{
    Publisher<T> innerPublisher;
    ObjectPool<T> pool;

    private static final Logger logger = LoggerFactory.getLogger(PooledPublisher.class);


    public PooledPublisher(Publisher<T> innerPublisher, Supplier<T> supplier) {
        this.innerPublisher = innerPublisher;
        pool = new ObjectPool<T>(100, supplier);
        logger.info("Started Pooled Publisher");
    }

    @Override
    public void publish(T data) {
        this.innerPublisher.publish(data);
        pool.release(data);
    }

    public T acquire() {
        return pool.acquire();
    }
}
