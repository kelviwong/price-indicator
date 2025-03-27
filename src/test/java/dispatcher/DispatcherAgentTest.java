package dispatcher;

import data.WritableMutableCharSequence;
import org.junit.jupiter.api.BeforeAll;
import common.PriceStoreFactory;
import config.Config;
import data.Price;
import data.PriceEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import publisher.PricePublisher;
import service.EventWorker;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DispatcherAgentTest {
    private Supplier<EventWorker> supplier;

    @BeforeEach
    public void setUp() {
        supplier = () -> {
            BlockingQueue<PriceEvent> taskQueue = new ArrayBlockingQueue<>(1000);
            EventWorker priceWorker = new EventWorker(taskQueue);
            return priceWorker;
        };


    }

    private static WritableMutableCharSequence symbolA;
    private static WritableMutableCharSequence symbolB;
    private static WritableMutableCharSequence symbolC;

    @BeforeAll
    public static void setup() {
        symbolA = (new WritableMutableCharSequence(20)).copy("AUD/USD");
        symbolB = (new WritableMutableCharSequence(20)).copy("EUR/USD");
        symbolC = (new WritableMutableCharSequence(20)).copy("JPY/USD");
    }

    @Test
    public void testDispatcherAgentShouldAlwaysUseSameThreadWithSameCode() {
        DispatcherAgent dispatcherAgent = new DispatcherAgent(5, new HashSymbolDispatchStrategy(), supplier);
        dispatcherAgent.start();
        ExecutorService executorServiceA = dispatcherAgent.dispatchTask(symbolA, () -> {});
        ExecutorService executorServiceB = dispatcherAgent.dispatchTask(symbolB, () -> {});
        ExecutorService executorServiceC =  dispatcherAgent.dispatchTask(symbolC, () -> {});

        ExecutorService executorService = dispatcherAgent.dispatchTask(symbolB, () -> {
        });
        assertEquals(executorServiceB, executorService);
        executorService = dispatcherAgent.dispatchTask(symbolA, () -> {
        });
        assertEquals(executorServiceA, executorService);
        executorService = dispatcherAgent.dispatchTask(symbolC, () -> {
        });
        assertEquals(executorServiceC, executorService);
        dispatcherAgent.stop();
    }

    @Test
    public void testDispatcherAgentShouldAlwaysUseSameThreadWithSameCodeWorker() {
        DispatcherAgent dispatcherAgent = new DispatcherAgent(5, new HashSymbolDispatchStrategy(), supplier);
        dispatcherAgent.start();
        PriceEvent event = new PriceEvent(new Price());
        EventWorker workerA = dispatcherAgent.dispatchQueue(symbolA, event);
        EventWorker workerB = dispatcherAgent.dispatchQueue(symbolB, event);
        EventWorker workerC = dispatcherAgent.dispatchQueue(symbolC, event);

        EventWorker worker = dispatcherAgent.dispatchQueue(symbolB, event);
        assertEquals(workerB, worker);
        worker = dispatcherAgent.dispatchQueue(symbolA, event);
        assertEquals(workerA, worker);
        worker = dispatcherAgent.dispatchQueue(symbolC, event);
        assertEquals(workerC, worker);

        dispatcherAgent.stop();
    }

    @Test
    public void testDispatcherShouldRoundRobinTheThread() {
        DispatcherAgent dispatcherAgent = new DispatcherAgent(5, new RoundRobinDispatchStrategy(), supplier);
        dispatcherAgent.start();
        ExecutorService[] executors = dispatcherAgent.getExecutors();

        ExecutorService executorServiceA = dispatcherAgent.dispatchTask(symbolA, () -> {
        });
        ExecutorService executorServiceB = dispatcherAgent.dispatchTask(symbolB, () -> {
        });
        ExecutorService executorServiceC = dispatcherAgent.dispatchTask(symbolC, () -> {
        });

        //expect to be putting in-order executors
        assertEquals(executors[0], executorServiceA);
        assertEquals(executors[1], executorServiceB);
        assertEquals(executors[2], executorServiceC);

        ExecutorService executorServiceB2 = dispatcherAgent.dispatchTask(symbolB, () -> {
        });
        ExecutorService executorServiceC2 = dispatcherAgent.dispatchTask(symbolC, () -> {
        });
        ExecutorService executorServiceA2 = dispatcherAgent.dispatchTask(symbolA, () -> {
        });

        // should stick to same thread once set
        assertEquals(executors[0], executorServiceA2);
        assertEquals(executors[1], executorServiceB2);
        assertEquals(executors[2], executorServiceC2);

        executorServiceA.shutdownNow();
        executorServiceB.shutdownNow();
        executorServiceC.shutdownNow();
        dispatcherAgent.stop();
    }

    @Test
    public void testDispatcherShouldBySymbolThread() {
        HashSymbolDispatchStrategy dispatchStrategy = new HashSymbolDispatchStrategy();
        DispatcherAgent dispatcherAgent = new DispatcherAgent(5, dispatchStrategy, supplier);
        dispatcherAgent.start();
        ExecutorService[] executors = dispatcherAgent.getExecutors();

        ExecutorService executorServiceC = dispatcherAgent.dispatchTask(symbolC, () -> {
        });
        ExecutorService executorServiceA = dispatcherAgent.dispatchTask(symbolA, () -> {
        });
        ExecutorService executorServiceB = dispatcherAgent.dispatchTask(symbolB, () -> {
        });

        int threadNo = dispatchStrategy.getThreadId(symbolA, 5);
        int threadNo2 = dispatchStrategy.getThreadId(symbolB, 5);
        int threadNo3 = dispatchStrategy.getThreadId(symbolC, 5);

        //expect to be putting in-order executors
        assertEquals(executors[threadNo], executorServiceA);
        assertEquals(executors[threadNo2], executorServiceB);
        assertEquals(executors[threadNo3], executorServiceC);

        ExecutorService executorServiceA2 = dispatcherAgent.dispatchTask(symbolA, () -> {
        });
        ExecutorService executorServiceB2 = dispatcherAgent.dispatchTask(symbolB, () -> {
        });
        ExecutorService executorServiceC2 = dispatcherAgent.dispatchTask(symbolC, () -> {
        });

        assertEquals(executors[threadNo], executorServiceA2);
        assertEquals(executors[threadNo2], executorServiceB2);
        assertEquals(executors[threadNo3], executorServiceC2);

        executorServiceA.shutdownNow();
        executorServiceB.shutdownNow();
        executorServiceC.shutdownNow();
        dispatcherAgent.stop();
    }

}