package feeder;

import service.IService;

public interface PriceFeeder<T> extends IService {
    T getData() throws InterruptedException;
    void pushData(T data);
}
