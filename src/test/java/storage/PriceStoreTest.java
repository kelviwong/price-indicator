package storage;

import common.MockTimeProvider;
import data.Price;
import data.WritableMutableCharSequence;
import lombok.Locked;
import org.junit.jupiter.api.Test;
import util.Data;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PriceStoreTest {

    private static void setupData(List<Price> priceList, String currency, MockTimeProvider mockTimeProvider) {
        mockTimeProvider.advanceInMinutes(-61);
        priceList.add(new Price("AUD/USD", mockTimeProvider.now(), 12.5, 2000));
        mockTimeProvider.advanceInMinutes(5);
        priceList.add(new Price("AUD/USD", mockTimeProvider.now(), 10.5, 1000));
        mockTimeProvider.advanceInMinutes(10);
        priceList.add(new Price("AUD/USD", mockTimeProvider.now(), 14.6, 4000));
        mockTimeProvider.advanceInMinutes(20);
        priceList.add(new Price("AUD/USD", mockTimeProvider.now(), 11.5, 13000));
        mockTimeProvider.advanceInMinutes(20);
        priceList.add(new Price("AUD/USD", mockTimeProvider.now(), 10.4, 12000));
        mockTimeProvider.advanceInMinutes(6);
    }

    @Test
    public void tesDequeStore() {
        MockTimeProvider mockTimeProvider = new MockTimeProvider();
        List<Price> priceList = new ArrayList<>();
        setupData(priceList, "AUD/USD", mockTimeProvider);
        PriceDequeStore store = new PriceDequeStore("AUD/USD");
        store.writeAll(priceList);
        assertEquals(5, store.size());
        Price data = new Price();
        store.peek(data);
        assertEquals(5, store.size());
        assertEquals(priceList.get(0).getPrice(), data.getPrice());
        assertEquals(priceList.get(0).getTimestamp(), data.getTimestamp());
        assertEquals(priceList.get(0).getVolume(), data.getVolume());
        store.read(data);
        assertEquals(4, store.size());
        assertEquals(priceList.get(0).getPrice(), data.getPrice());
        assertEquals(priceList.get(0).getTimestamp(), data.getTimestamp());
        assertEquals(priceList.get(0).getVolume(), data.getVolume());
        Price newPrice = new Price("AUD/USD", mockTimeProvider.now(), 22200.5, 2000);
        store.write(newPrice);
        assertEquals(5, store.size());
        store.read(data);
        store.read(data);
        store.read(data);
        store.read(data);
        store.read(data);
        assertEquals(newPrice.getPrice(), data.getPrice());
        assertEquals(newPrice.getTimestamp(), data.getTimestamp());
        assertEquals(newPrice.getVolume(), data.getVolume());
    }

    @Test
    public void testMemoryMapStore() throws Exception {
        MockTimeProvider mockTimeProvider = new MockTimeProvider();
        List<Price> priceList = new ArrayList<>();
        WritableMutableCharSequence currency = Data.getOffheapChar("AUD/USD");
        setupData(priceList, currency.toString(), mockTimeProvider);
        String path = createPath(currency.toString());
        PriceMemoryMapFileStore memoryMapFileStore = createMMFFileStore(path);
        Price newPrice = new Price(currency, mockTimeProvider.now(), 22200.5, 2000);
        Price temp = new Price();
        temp.setCurrency(currency);
        memoryMapFileStore.write(newPrice);
        assertEquals(1, memoryMapFileStore.size());
        memoryMapFileStore.peek(temp);
        assertEquals(newPrice, temp);
        // peek did not increase read pos, so it still read the first item
        memoryMapFileStore.read(temp);
        assertEquals(newPrice, temp);
        // when it read again which have no records already;
        memoryMapFileStore.read(temp);
        assertEquals(0, temp.getTimestamp());
        newPrice = new Price(currency, mockTimeProvider.now(), 3433.5, 2000);
        memoryMapFileStore.write(newPrice);
        assertEquals(2, memoryMapFileStore.size());
        memoryMapFileStore.read(temp);
        assertEquals(3433.5, temp.getPrice());
        assertEquals(2000, temp.getVolume());
        memoryMapFileStore.close();
    }

    private static String createPath(String currency) {
        return currency.replace("/", "_") + "_" + "test";
    }

    private static PriceMemoryMapFileStore createMMFFileStore(String path) throws Exception {
        PriceMemoryMapFileStore memoryMapFileStore = new PriceMemoryMapFileStore(path, 1024);
        return memoryMapFileStore;
    }

    @Test
    public void testMemoryMapStoreWriteAll() throws Exception {
        MockTimeProvider mockTimeProvider = new MockTimeProvider();
        List<Price> priceList = new ArrayList<>();
        String currency = "AUD/USD";
        setupData(priceList, currency, mockTimeProvider);
        String path = createPath(currency);
        PriceMemoryMapFileStore memoryMapFileStore = createMMFFileStore(path);
        memoryMapFileStore.writeAll(priceList);
        assertEquals(5, memoryMapFileStore.size());
        Price temp = new Price();
        memoryMapFileStore.read(temp);
        verifyPriceList(priceList.get(0), temp);
        memoryMapFileStore.read(temp);
        verifyPriceList(priceList.get(1), temp);
        memoryMapFileStore.read(temp);
        verifyPriceList(priceList.get(2), temp);
        memoryMapFileStore.read(temp);
        verifyPriceList(priceList.get(3), temp);
        memoryMapFileStore.read(temp);
        verifyPriceList(priceList.get(4), temp);
        memoryMapFileStore.close();
    }

    public void verifyPriceList(Price sourcePrice, Price targetPrice) {
        assertEquals(sourcePrice.getTimestamp(), targetPrice.getTimestamp());
        assertEquals(sourcePrice.getPrice(), targetPrice.getPrice());
        assertEquals(sourcePrice.getVolume(), targetPrice.getVolume());
    }

}