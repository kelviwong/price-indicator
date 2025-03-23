package storage;

import data.Price;

public class PriceStoreFactory {
    StoreType type;
    String prefix;

    public PriceStoreFactory(StoreType type, String prefix) {
        this.type = type;
        this.prefix = prefix;
    }

    public IStore<Price> createStore(String currency) throws Exception {
        if (type == StoreType.MEM_MAP) {
            String path = currency.replace("/", "_") + "_" + prefix;
            return new PriceMemoryMapFileStore(path, 1024);
        }
        return new PriceDequeStore();
    }
}
