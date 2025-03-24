package queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RetryThenDropStrategy<T> implements BackOffStrategy<T> {

    private final int maxRetries;
    private final long retryDelayMicros;
    private final Consumer<T> dropConsumer;

    public RetryThenDropStrategy(int maxRetries, long retryDelayMicros, Consumer<T> dropConsumer) {
        this.maxRetries = maxRetries;
        this.retryDelayMicros = retryDelayMicros;
        this.dropConsumer = dropConsumer;
    }

    @Override
    public boolean offer(BlockingQueue<T> queue, T item) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                if (queue.offer(item, retryDelayMicros, TimeUnit.MICROSECONDS)) {
                    return true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        dropConsumer.accept(item);
        return false;
    }
}