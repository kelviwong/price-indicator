package latency;

import common.TimeProvider;
import data.Event;
import org.HdrHistogram.ConcurrentHistogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import publisher.LatencyPublisher;

public class LatencyTracker {

    private final TimeProvider timeProvider;
    private final ConcurrentHistogram histogram;
    private static final Logger logger = LoggerFactory.getLogger(LatencyPublisher.class);

    public LatencyTracker(final TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
        histogram = new ConcurrentHistogram(1, 2_000_000_000L, 3);
        logger.info("Starting latency tracker....");
    }

    public static <T extends Event<?>> void copyStartNs(T src, T target) {
        target.setStartNs(src.getStartNs());
    }

    public long latencyMicro(long startNs) {
        long latency = timeProvider.now() - startNs;
        histogram.recordValue(latency);
        return latency / 1_000;
    }

    public double latencyMills(long startNs) {
        return timeProvider.now() - startNs / 1_000_000.0;
    }

    public void showStats() {
        logger.info("=== Latency ===");
        logger.info("P99 : {} us", histogram.getValueAtPercentile(99.0) / 1_000);
        logger.info("Max : {} us", histogram.getMaxValue() / 1_000);

        histogram.reset();
    }
}
