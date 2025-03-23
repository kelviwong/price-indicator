package common;

public class LocalDateTimeProviderFactory implements ITimeProviderFactory {
    @Override
    public TimeProvider get() {
        return new LocalDateTimeProvider();
    }
}
