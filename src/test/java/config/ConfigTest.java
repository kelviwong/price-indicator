package config;

import dispatcher.DispatchType;
import enums.StoreType;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    public void testConfig() throws IOException {
        Config config = Config.loadConfig("config.yaml");
        int threads = config.getDispatcherConfig().getThreads();
        DispatchType dispatchType = config.getDispatcherConfig().getDispatchType();
        int vwapIntervalInMs = config.getVwapConfig().getVwapIntervalInMs();
        StoreType storeType = config.getPriceServiceConfig().getStoreType();

        int capacity = config.getQueueConfig().getCapacity();

        assertEquals(5, threads);
        assertEquals(3600000, vwapIntervalInMs);
        assertEquals(StoreType.DEQUE, storeType);
        assertEquals(10000, capacity);
        assertEquals(DispatchType.ROUND_ROBIN, dispatchType);
    }

}