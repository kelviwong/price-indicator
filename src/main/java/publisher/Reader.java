package publisher;

public interface Reader<T>  {
    T poll() throws InterruptedException;
}
