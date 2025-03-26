package service;

import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.LockSupport;

// TODO: this worker only use when pin to CPU core, can provide ultra low latency
// need implement affinity
public class BusySpinWorker<T> implements Runnable, IService {
    private final ManyToOneConcurrentArrayQueue<T> taskQueue;
    private final int batchSize;
    private EventHandler<T> eventHandler;
    private static final Logger logger = LoggerFactory.getLogger(BusySpinWorker.class);
    private int pinToCore;

    public BusySpinWorker(ManyToOneConcurrentArrayQueue taskQueue, int batchSize, int pinToCore) {
        this.taskQueue = taskQueue;
        this.batchSize = batchSize;
        this.pinToCore = pinToCore;
    }

    public void submit(T event) {
        this.taskQueue.offer(event);
    }

    public void registerHandler(EventHandler<T> handler) {
        this.eventHandler = handler;
    }

    private volatile boolean running;

    @Override
    public void run() {
        while (running) {
            int processed = 0;
            T event;
            try {
                while (processed < batchSize && (event = taskQueue.poll()) != null) {
                    eventHandler.handle(event);
                    processed++;
                }

                if (processed == 0) {
                    // a little parking for 1 nano to avoid too busy spinning
                    LockSupport.parkNanos(1);
                }
            } catch (Exception e) {
                logger.error("Error in worker: ", e);
            }
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
