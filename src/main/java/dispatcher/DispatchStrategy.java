package dispatcher;

import data.WritableMutableCharSequence;

public interface DispatchStrategy {
    int getThreadId(WritableMutableCharSequence symbol, int numOfThreads);
}
