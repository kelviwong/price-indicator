package service;

import data.AnalyticData;
import data.IndicatorEvent;
import org.junit.jupiter.api.Test;
import util.ObjectPool;

import static org.junit.jupiter.api.Assertions.*;

class ObjectPoolTest {
    ObjectPool<IndicatorEvent> objectPool;
    @Test
    public void testBasic() {
        objectPool = new ObjectPool<>(1, () -> new IndicatorEvent(null));
        assertEquals(1, objectPool.size());

        IndicatorEvent event = objectPool.acquire();
        assertNotNull(event);
        assertEquals(0, objectPool.size());
        event.setData(new AnalyticData("AUD/USD"));

        assertEquals("AUD/USD", event.getData().getCurrency());

        objectPool.release(event);
        assertEquals(1, objectPool.size());

        event = objectPool.acquire();
        assertEquals(0, objectPool.size());
        assertNull(event.getData());
    }
    @Test
    public void ShouldExpandWhenAcquireReachMaxSize() {
        objectPool = new ObjectPool<>(1, () -> new IndicatorEvent(null));
        objectPool.acquire();
        objectPool.acquire();
        IndicatorEvent event = objectPool.acquire();
        assertNotNull(event);
    }

}