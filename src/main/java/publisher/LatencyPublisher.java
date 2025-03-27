package publisher;

import data.Event;
import latency.LatencyTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LatencyPublisher<T extends Event<T>> implements Publisher<T> {
    private static final Logger logger = LoggerFactory.getLogger(LatencyPublisher.class);
    Publisher<T> innerPublisher;
    LatencyTracker latencyTracker;

    public LatencyPublisher(Publisher<T> innerPublisher, LatencyTracker latencyTracker) {
        logger.info("Started Latency Publisher");
        this.innerPublisher = innerPublisher;
        this.latencyTracker = latencyTracker;
    }

    @Override
    public void publish(T data) {
        this.innerPublisher.publish(data);
        latencyTracker.latencyMicro(data.getStartNs());
//        logger.info("latency: {} Micro", latencyTracker.latencyMicro(data.getStartNs()));
    }
}
