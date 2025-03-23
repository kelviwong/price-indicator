package common;

public class NanoTimeProviderFactory implements ITimeProviderFactory {
    @Override
    public TimeProvider get() {
        return new NanoTimeProvider();
    }
}
