package util;

import data.Resettable;

import java.util.ArrayList;
import java.util.function.Supplier;

public class ObjectPool<T extends Resettable> {

    ArrayList<T> pool;
    Supplier<T> supplier;

    public ObjectPool(int size, Supplier<T> supplier) {
        pool = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            pool.add(supplier.get());
        }
        this.supplier = supplier;
    }

    public T acquire() {
        if (pool.isEmpty()) {
            pool.add(supplier.get());
        }

        return pool.remove(pool.size() - 1);
    }

    public int size() {
        return pool.size();
    }

    public void release(T obj) {
        obj.reset();
        pool.add(obj);
    }
}
