package publisher;

import data.Event;

public interface Publisher<T extends Event<?>> {
    void publish(T data);
}
