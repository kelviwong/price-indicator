package common;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadFactory;

import static org.junit.jupiter.api.Assertions.*;

class NamedThreadFactoryTest {

    @Test
    void testNameThreadFactoryShouldSetTheName() {
        ThreadFactory factory = new NamedThreadFactory("VwapWorker");

        Thread t1 = factory.newThread(() -> {});
        Thread t2 = factory.newThread(() -> {});

        assertEquals("VwapWorker-1", t1.getName());
        assertEquals("VwapWorker-2", t2.getName());
    }

}