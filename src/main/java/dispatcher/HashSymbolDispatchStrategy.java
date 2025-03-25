package dispatcher;

public class HashSymbolDispatchStrategy implements DispatchStrategy{
    @Override
    public int getThreadId(String symbol, int numOfThreads) {
        return hashed(symbol, numOfThreads);
    }

    private static int hashed(String symbol, int numOfThreads) {
        return Math.abs(symbol.hashCode()) % numOfThreads;
    }
}
