package data;

import java.nio.MappedByteBuffer;

public interface MemoryFileAware<T> {
    void writeToByte(MappedByteBuffer buffer);

    void readFromByte(MappedByteBuffer buffer, int currentReadRecord);
}
