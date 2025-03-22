package feeder;

public interface PriceFeeder<T> {
    T getData() throws InterruptedException;
}
