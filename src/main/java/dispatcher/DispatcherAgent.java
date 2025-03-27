package dispatcher;

import common.NamedThreadFactory;
import data.WritableMutableCharSequence;
import org.agrona.collections.Object2IntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.IService;
import service.EventWorker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class DispatcherAgent<T> implements IService {
    private static final Logger logger = LoggerFactory.getLogger(DispatcherAgent.class);

    int numOfThread;

    private final Object2IntHashMap<WritableMutableCharSequence> symbolMappedThread;
    private final ExecutorService[] executors;
    private final DispatchStrategy dispatchStrategy;

    private final EventWorker[] workers;

    public DispatcherAgent(int numOfThread, DispatchStrategy dispatchStrategy, Supplier<EventWorker<T>> supplier) {
        this.numOfThread = numOfThread;
        this.dispatchStrategy = dispatchStrategy;

        executors = new ExecutorService[numOfThread];
        workers = new EventWorker[numOfThread];

        for (int i = 0; i < numOfThread; i++) {
            //TODO: we can further improve here to allow bind to single cpu core
            executors[i] = Executors.newSingleThreadExecutor(new NamedThreadFactory("DispatcherAgent-" + i));
            EventWorker worker = supplier.get();
            workers[i] = worker;
            executors[i].execute(worker);
        }

        symbolMappedThread = new Object2IntHashMap<>(1000, 0.65f, -1);
        logger.info("Creating Dispatcher Agent, numOfThread: {}, dispatchType: {}", numOfThread, dispatchStrategy);
    }

    public ExecutorService dispatchTask(WritableMutableCharSequence symbol, Runnable task) {
        int threadNo = symbolMappedThread.getValue(symbol);
        if (threadNo == symbolMappedThread.missingValue()) {
            threadNo = dispatchStrategy.getThreadId(symbol, numOfThread);
            symbolMappedThread.put(symbol, threadNo);
        }

        ExecutorService executorService = executors[threadNo];
        executorService.execute(task);
        return executorService;
    }

    public EventWorker dispatchQueue(WritableMutableCharSequence symbol, T event) {
        int threadNo = symbolMappedThread.getValue(symbol);
        if (threadNo == symbolMappedThread.missingValue()) {
            threadNo = dispatchStrategy.getThreadId(symbol, numOfThread);
            symbolMappedThread.put(symbol, threadNo);
        }
        EventWorker worker = workers[threadNo];
        worker.submit(event);

        return worker;
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
        if (workers != null) {
            for (EventWorker worker : workers){
                worker.stop();
            }
        }

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
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }

    }
}
