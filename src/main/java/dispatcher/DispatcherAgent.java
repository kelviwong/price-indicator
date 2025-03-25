package dispatcher;

import common.NamedThreadFactory;
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
    private final DispatchStrategy dispatchStrategy;

    public DispatcherAgent(int numOfThread, DispatchStrategy dispatchStrategy) {
        this.numOfThread = numOfThread;
        this.dispatchStrategy = dispatchStrategy;

        executors = new ExecutorService[numOfThread];

        for (int i = 0; i < numOfThread; i++) {
            //TODO: we can further improve here to allow bind to single cpu core
            executors[i] = Executors.newSingleThreadExecutor(new NamedThreadFactory("DispatcherAgent-" + i));
        }

        symbolMappedThread = new Object2IntHashMap<>(1000, 0.65f, -1);
        logger.info("Creating Dispatcher Agent, numOfThread: {}, dispatchType: {}", numOfThread, dispatchStrategy);
    }

    public ExecutorService dispatchTask(String symbol, Runnable task) {
        int threadNo = symbolMappedThread.getValue(symbol);
        if (threadNo == symbolMappedThread.missingValue()) {
            threadNo = dispatchStrategy.getThreadId(symbol, numOfThread);
            symbolMappedThread.put(symbol, threadNo);
        }

        ExecutorService executorService = executors[threadNo];
        executorService.execute(task);
        return executorService;
    }

    public ExecutorService[] getExecutors() {
        return executors;
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
