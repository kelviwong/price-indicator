package storage;

import data.Price;
import data.WritableMutableCharSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.List;

public class PriceDequeStore implements IStore<Price> {
    protected static final Logger logger = LoggerFactory.getLogger(PriceDequeStore.class);

    ArrayDeque<Price> store;
    public PriceDequeStore(WritableMutableCharSequence currency) {
        this.store = new ArrayDeque<>();
        logger.info("Created PriceDequeStore for {}", currency);
    }

    @Override
    public void write(Price data) {
        store.add(data);
    }

    @Override
    public void read(Price data) {
        Price pop = store.pop();
        if (pop == null) {
            data.clear();
            return;
        }
        data.setTimestamp(pop.getTimestamp());
        data.setPrice(pop.getPrice());
        data.setVolume(pop.getVolume());
    }

    @Override
    public void peek(Price data) {
        Price peek = store.peek();
        if (peek == null) {
            data.clear();
            return;
        }
        data.setTimestamp(peek.getTimestamp());
        data.setPrice(peek.getPrice());
        data.setVolume(peek.getVolume());
    }

    @Override
    public void writeAll(List<Price> data) {
        store.addAll(data);
    }

    @Override
    public int size() {
        return store.size();
    }

    @Override
    public void close() {

    }
}
