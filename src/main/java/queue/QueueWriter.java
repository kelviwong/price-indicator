package queue;

public interface QueueWriter<T> {
    boolean write(T data) throws Exception;

    T take() throws InterruptedException;

    boolean isBlocking();
}
