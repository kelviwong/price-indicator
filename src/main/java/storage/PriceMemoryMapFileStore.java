package storage;

import data.Price;

import java.util.List;

public class PriceMemoryMapFileStore extends MemoryMapFileStore<Price> {
    int currentReadRecord = 0;
    int currentWriteRecord = 0;

    public PriceMemoryMapFileStore(String path, int size) throws Exception {
        super(path, size);
        logger.info("create PriceMemoryMapFileStore: {}, size:{}", path, size);
    }

    @Override
    public void write(Price data) {
        data.writeToByte(buffer);
        currentWriteRecord++;
    }

    @Override
    public void read(Price data) {
        if (currentReadRecord == currentWriteRecord) {
            data.clear();
            return;
        }
        data.readFromByte(buffer, currentReadRecord);
        currentReadRecord++;
    }

    @Override
    public void peek(Price data) {
        data.readFromByte(buffer, currentReadRecord);
    }

    @Override
    public void writeAll(List<Price> data) {
        for (Price price : data) {
            write(price);
        }
    }

    @Override
    public int size() {
        return currentWriteRecord;
    }
}
