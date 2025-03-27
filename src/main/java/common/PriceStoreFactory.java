package common;

import data.Price;
import data.WritableMutableCharSequence;
import dispatcher.DispatcherAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storage.IStore;
import storage.PriceDequeStore;
import storage.PriceMemoryMapFileStore;
import enums.StoreType;

public class PriceStoreFactory {
    private static final Logger logger = LoggerFactory.getLogger(DispatcherAgent.class);
    StoreType type;
    String prefix;

    public PriceStoreFactory(StoreType type, String prefix) {
        this.type = type;
        this.prefix = prefix;
        logger.info("PriceStoreFactory for " + type + " " + prefix);
    }

    public IStore<Price> createStore(WritableMutableCharSequence currency) throws Exception {
        if (type == StoreType.MEM_MAP) {
            // very limit usage , so toString is also fine
            String path = currency.toString().replace("/", "_") + "_" + prefix;
            return new PriceMemoryMapFileStore(path, 1024);
        }
        return new PriceDequeStore();
    }
}
