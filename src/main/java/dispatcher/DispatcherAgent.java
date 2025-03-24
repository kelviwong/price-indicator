package dispatcher;

import org.agrona.collections.Object2IntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.IService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DispatcherAgent implements IService {
    private static final Logger logger = LoggerFactory.getLogger(DispatcherAgent.class);

    int numOfThread;

    private final Object2IntHashMap<String> symbolMappedThread;
    private final ExecutorService[] executors;

    public DispatcherAgent(int numOfThread) {
        this.numOfThread = numOfThread;
        executors = new ExecutorService[numOfThread];

        for (int i = 0; i < numOfThread; i++) {
            executors[i] = Executors.newSingleThreadExecutor();
        }

        symbolMappedThread = new Object2IntHashMap<>(1000, 0.65f, -1);
    }

    public ExecutorService dispatchTask(String symbol, Runnable task) {
        int hashThread = Math.abs(symbol.hashCode()) % numOfThread;
        int value = symbolMappedThread.putIfAbsent(symbol, hashThread);
        int threadId = value == -1 ? hashThread : value;

        ExecutorService executorService = executors[threadId];
        executorService.submit(task);
        return executorService;
    }

    @Override
    public void start() {
        logger.info("Starting DispatcherAgent");
    }

    @Override
    public void stop() {
        logger.info("Stopping DispatcherAgent");
        if (executors != null) {
            for (ExecutorService executorService : executors) {
                try {
                    executorService.shutdown();
                    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        logger.info("Timeout: force shutdown");
                        executorService.shutdownNow();
                    } else {
                        logger.info("task is done");
                    }
                } catch (InterruptedException e) {
                    logger.info("InterruptedException: force shutdown");
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }

    }
}
