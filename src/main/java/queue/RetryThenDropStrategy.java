package queue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
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
    public boolean offer(QueueWriter<T> writer, T item) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                if (writer.write(item)) {
                    return true;
                }

                if (!writer.isBlocking() && retryDelayMicros > 0) {
                    LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(retryDelayMicros));
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        dropConsumer.accept(item);
        return false;
    }
//    public boolean offer(BlockingQueue<T> queue, T item) {
//        for (int i = 0; i < maxRetries; i++) {
//            try {
//                if (queue.offer(item, retryDelayMicros, TimeUnit.MICROSECONDS)) {
//                    return true;
//                }
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                return false;
//            }
//        }
//
//        dropConsumer.accept(item);
//        return false;
//    }
}