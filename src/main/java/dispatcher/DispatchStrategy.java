package dispatcher;

public interface DispatchStrategy {
    int getThreadId(String symbol, int numOfThreads);
}
