package queue;

public interface BackOffStrategy<T> {
    boolean offer(QueueWriter<T> writer, T item);
}
