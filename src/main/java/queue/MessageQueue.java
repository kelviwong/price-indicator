package queue;

import lombok.Getter;

public class MessageQueue<T> {
    @Getter
    QueueWriter<T> queueWriter;
    BackOffStrategy<T> backOffStrategy;

    public MessageQueue(QueueWriter<T> queueWriter, BackOffStrategy<T> backOffStrategy) {
        this.queueWriter = queueWriter;
        this.backOffStrategy = backOffStrategy;
    }

    public boolean publish(T item) {
        return backOffStrategy.offer(queueWriter, item);
    }

    public T take() throws InterruptedException {
       return queueWriter.take();
    }
}
