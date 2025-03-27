package dispatcher;

import data.WritableMutableCharSequence;

public class HashSymbolDispatchStrategy implements DispatchStrategy{
    @Override
    public int getThreadId(WritableMutableCharSequence symbol, int numOfThreads) {
        return hashed(symbol, numOfThreads);
    }

    private static int hashed(WritableMutableCharSequence symbol, int numOfThreads) {
        return Math.abs(symbol.hashCode()) % numOfThreads;
    }
}
