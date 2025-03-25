package publisher;

import data.Resettable;
import util.ObjectPool;

import java.util.function.Supplier;

public class PooledPublisher<T extends Resettable> implements Publisher<T>{
    Publisher<T> innerPublisher;
    ObjectPool<T> pool;

    public PooledPublisher(Publisher<T> innerPublisher, Supplier<T> supplier) {
        this.innerPublisher = innerPublisher;
        pool = new ObjectPool<T>(100, supplier);
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
