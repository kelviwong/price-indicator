package storage;

import java.util.List;

public interface IStore<T> {
    void write(T data);
    void read(T data);
    void peek(T data);
    void writeAll(List<T> data);
    int size();
    void close();
}
