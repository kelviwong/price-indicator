package queue;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import data.Event;

public class DisruptorWriter<T extends Event> implements QueueWriter<T> {
    Disruptor<T> disruptor;

    public DisruptorWriter(Disruptor<T> disruptor) {
        this.disruptor = disruptor;
    }

    @Override
    public boolean write(T item) throws Exception {
        try {
            RingBuffer<T> ringBuffer = disruptor.getRingBuffer();
            long seq = ringBuffer.tryNext();
            try {
                T event = ringBuffer.get(seq);
                event.setData(item.getData());
                event.setStartNs(item.getStartNs());
            } finally {
                ringBuffer.publish(seq);
            }
            return true;
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public T take() throws InterruptedException {
        return null;
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
