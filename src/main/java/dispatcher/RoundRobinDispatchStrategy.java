package dispatcher;

public class RoundRobinDispatchStrategy implements DispatchStrategy{
    int counter;

    @Override
    public int getThreadId(String symbol, int numOfThreads) {
        int threadNo = counter % numOfThreads;
        counter++;
        return threadNo;
    }
}
