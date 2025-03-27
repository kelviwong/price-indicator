package service;

import data.AnalyticData;
import data.IndicatorEvent;
import data.WritableMutableCharSequence;
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
        WritableMutableCharSequence writableMutableCharSequence = new WritableMutableCharSequence(20);
        writableMutableCharSequence.copy("AUD/USD");
        event.setData(new AnalyticData(writableMutableCharSequence));

        assertEquals(writableMutableCharSequence, event.getData().getCurrency());

        objectPool.release(event);
        assertEquals(1, objectPool.size());

        event = objectPool.acquire();
        assertEquals(0, objectPool.size());
        assertEquals(0.0d, event.getData().getVwap());
        assertEquals("AUD/USD", event.getData().getCurrency().toString());
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