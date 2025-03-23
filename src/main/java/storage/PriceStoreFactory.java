package storage;

import data.Price;

public class PriceStoreFactory {
    StoreType type;

    public PriceStoreFactory(StoreType type) {
        this.type = type;
    }
    public IStore<Price> createStore(String currency) throws Exception {
        if (type == StoreType.MEM_MAP) {
            String path = currency.replace("/", "_");
            return new PriceMemoryMapFileStore(path, 1024);
        }
        return new PriceDequeStore();
    }
}
