package common;

public class NanoTimeProvider implements TimeProvider {
    @Override
    public long now() {
        return System.nanoTime();
    }
}
