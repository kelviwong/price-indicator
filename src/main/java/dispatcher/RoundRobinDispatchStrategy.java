package dispatcher;

import data.WritableMutableCharSequence;

public class RoundRobinDispatchStrategy implements DispatchStrategy{
    int counter;

    @Override
    public int getThreadId(WritableMutableCharSequence symbol, int numOfThreads) {
        int threadNo = counter % numOfThreads;
        counter++;
        return threadNo;
    }
}
