package dispatcher;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DispatcherAgentTest {

    @Test
    public void testDispatcherAgentShouldAlwaysUseSameThreadWithSameCode() {
        DispatcherAgent dispatcherAgent = new DispatcherAgent(5);
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

}