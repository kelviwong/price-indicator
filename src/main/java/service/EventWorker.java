package service;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import queue.MessageQueue;

import java.util.concurrent.BlockingQueue;

public class EventWorker<T> implements Runnable, IService {

    @Getter
    MessageQueue<T> taskQueue;

    private EventHandler<T> eventHandler;

    private static final Logger logger = LoggerFactory.getLogger(EventWorker.class);

    private volatile boolean running;

    public EventWorker(MessageQueue<T> taskQueue) {
        this.taskQueue = taskQueue;
        this.running = true;
    }

    public void submit(T event) {
        this.taskQueue.publish(event);
    }

    public void registerHandler(EventHandler<T> handler) {
        this.eventHandler = handler;
    }

    public void run() {
        while (running) {
            try {
                T event = taskQueue.take();
                if (event != null) {
                    eventHandler.handle(event);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                logger.error("Error handling event, ", ex);
            }
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        running = true;
        Thread.currentThread().interrupt();
    }
}
