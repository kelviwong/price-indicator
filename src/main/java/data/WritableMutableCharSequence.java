package data;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

public class WritableMutableCharSequence {

    private static final int DEFAULT_SIZE = 1024;

    protected MutableDirectBuffer buffer;
    protected final int startIndex = 0;
    protected int endIndex = 0;
    protected int size;
    protected int offset = 0;

    public WritableMutableCharSequence(int size) {
        this.size = size == 0 ? DEFAULT_SIZE : size;
        this.buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(size));
    }

    public WritableMutableCharSequence(WritableMutableCharSequence src) {
        this.buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(src.size));

        buffer.byteBuffer().clear();
        clear();
        buffer.putBytes(0, src.buffer, 0, src.length());
        offset += src.length();
        endIndex = offset;
        size = src.size;
    }

    public WritableMutableCharSequence copy(String src) {
        buffer.byteBuffer().clear();
        clear();
        int length = buffer.putStringWithoutLengthAscii(0, src);
        offset += src.length();
        endIndex = trimEndIndex(0, length);
        return this;
    }

    public WritableMutableCharSequence append(String src, int start, int end) {
        clear();
        int length = end - start;
        for (int i = 0; i < length; i++) {
            char c = src.charAt(start + i);
            byte b = (byte) c; // Assumes ASCII
            buffer.putByte(offset++, b);
            endIndex++;
        }

        return this;
    }

    public WritableMutableCharSequence concat(String src) {
        buffer.putStringWithoutLengthAscii(offset, src);
        offset += src.length();
        endIndex = offset;
        return this;
    }

    public WritableMutableCharSequence concat(int src) {
        buffer.putIntAscii(offset, src);
        offset += Integer.BYTES;
        endIndex = offset;
        return this;
    }

    private int trimEndIndex(int start, int endIndex) {
        while (endIndex > startIndex && charAtPoisition(endIndex - 1) == '\u0000') {
            endIndex--;
        }

        return endIndex;
    }

    private char charAtPoisition(int i) {
        return buffer.getChar(i);
    }

    public String toString() {
        return buffer.getStringWithoutLengthAscii(startIndex, endIndex);
    }

    public void clear() {
        offset = 0;
        endIndex = 0;
    }

    public int length() {
        return offset;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        WritableMutableCharSequence other = (WritableMutableCharSequence) obj;

        if (this.length() != other.length()) return false;

        for (int i = 0; i < length(); i++) {
            if (this.buffer.getByte(this.startIndex + i) != other.buffer.getByte(other.startIndex + i)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (int i = 0; i < length(); i++) {
            h = 31 * h + buffer.getByte(startIndex + i);
        }
        return h;
    }
}
