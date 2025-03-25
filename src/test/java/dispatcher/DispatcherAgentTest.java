package dispatcher;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DispatcherAgentTest {

    @Test
    public void testDispatcherAgentShouldAlwaysUseSameThreadWithSameCode() {
        DispatcherAgent dispatcherAgent = new DispatcherAgent(5, DispatchType.BY_SYMBOL);
        dispatcherAgent.start();
        String symbolA = "AUD/USD";
        String symbolB = "EUR/USD";
        String symbolC = "JPY/USD";
        ExecutorService executorServiceA = dispatcherAgent.dispatchTask(symbolA, () -> {});
        ExecutorService executorServiceB = dispatcherAgent.dispatchTask(symbolB, () -> {});
        ExecutorService executorServiceC =  dispatcherAgent.dispatchTask(symbolC, () -> {});

        ExecutorService executorService = dispatcherAgent.dispatchTask(symbolB, () -> {});
        assertEquals(executorServiceB, executorService);
        executorService = dispatcherAgent.dispatchTask(symbolA, () -> {});
        assertEquals(executorServiceA, executorService);
        executorService = dispatcherAgent.dispatchTask(symbolC, () -> {});
        assertEquals(executorServiceC, executorService);

        executorServiceA.shutdownNow();
        executorServiceB.shutdownNow();
        executorServiceC.shutdownNow();
        dispatcherAgent.stop();
    }

    @Test
    public void testDispatcherShouldRoundRobinTheThread() {
        DispatcherAgent dispatcherAgent = new DispatcherAgent(5, DispatchType.ROUND_ROBIN);
        dispatcherAgent.start();
        String symbolA = "AUD/USD";
        String symbolB = "EUR/USD";
        String symbolC = "JPY/USD";
        ExecutorService[] executors = dispatcherAgent.getExecutors();

        ExecutorService executorServiceA = dispatcherAgent.dispatchTask(symbolA, () -> {});
        ExecutorService executorServiceB = dispatcherAgent.dispatchTask(symbolB, () -> {});
        ExecutorService executorServiceC =  dispatcherAgent.dispatchTask(symbolC, () -> {});

        //expect to be putting in-order executors
        assertEquals(executors[0], executorServiceA);
        assertEquals(executors[1], executorServiceB);
        assertEquals(executors[2], executorServiceC);

        ExecutorService executorServiceB2 = dispatcherAgent.dispatchTask(symbolB, () -> {});
        ExecutorService executorServiceC2 =  dispatcherAgent.dispatchTask(symbolC, () -> {});
        ExecutorService executorServiceA2 = dispatcherAgent.dispatchTask(symbolA, () -> {});

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
        DispatcherAgent dispatcherAgent = new DispatcherAgent(5, DispatchType.BY_SYMBOL);
        dispatcherAgent.start();
        String symbolA = "AUD/USD";
        String symbolB = "EUR/USD";
        String symbolC = "JPY/USD";
        ExecutorService[] executors = dispatcherAgent.getExecutors();

        ExecutorService executorServiceC =  dispatcherAgent.dispatchTask(symbolC, () -> {});
        ExecutorService executorServiceA = dispatcherAgent.dispatchTask(symbolA, () -> {});
        ExecutorService executorServiceB = dispatcherAgent.dispatchTask(symbolB, () -> {});

        int threadNo = dispatcherAgent.hashThead(symbolA);
        int threadNo2 = dispatcherAgent.hashThead(symbolB);
        int threadNo3 = dispatcherAgent.hashThead(symbolC);

        //expect to be putting in-order executors
        assertEquals(executors[threadNo], executorServiceA);
        assertEquals(executors[threadNo2], executorServiceB);
        assertEquals(executors[threadNo3], executorServiceC);

        ExecutorService executorServiceA2 = dispatcherAgent.dispatchTask(symbolA, () -> {});
        ExecutorService executorServiceB2 = dispatcherAgent.dispatchTask(symbolB, () -> {});
        ExecutorService executorServiceC2 =  dispatcherAgent.dispatchTask(symbolC, () -> {});

        assertEquals(executors[threadNo], executorServiceA2);
        assertEquals(executors[threadNo2], executorServiceB2);
        assertEquals(executors[threadNo3], executorServiceC2);

        executorServiceA.shutdownNow();
        executorServiceB.shutdownNow();
        executorServiceC.shutdownNow();
        dispatcherAgent.stop();
    }

}