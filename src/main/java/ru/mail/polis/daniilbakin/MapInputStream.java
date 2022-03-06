package ru.mail.polis.daniilbakin;

import ru.mail.polis.BaseEntry;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MapInputStream extends FileInputStream {

    private final int[] indexes;
    private final byte[] bytes = new byte[available()];
    private int currentFullness = -1;

    protected MapInputStream(File file, int[] indexes) throws IOException {
        super(file);
        this.indexes = indexes;
    }

    protected BaseEntry<ByteBuffer> readByKey(ByteBuffer key) throws IOException {
        return binarySearch(key);
    }

    private BaseEntry<ByteBuffer> binarySearch(ByteBuffer key) throws IOException {
        int position;
        int first = 0;
        int last = indexes.length - 1;
        position = (first + last) / 2;

        ByteBuffer currKey = readByteBuffer(indexes[position]);
        int compare = currKey.compareTo(key);
        while ((compare != 0) && (first <= last)) {
            if (compare > 0) {
                last = position - 1;
            } else {
                first = position + 1;
            }
            position = (first + last) / 2;
            currKey = readByteBuffer(indexes[position]);
            compare = currKey.compareTo(key);
        }
        if (first <= last) {
            return readEntry(indexes[position]);
        }
        return null;
    }

    /**
     * Position in bytes.
     */
    private BaseEntry<ByteBuffer> readEntry(int position) throws IOException {
        ByteBuffer key = readByteBuffer(position);
        ByteBuffer value = readByteBuffer(position + key.capacity() + Integer.BYTES);
        return new BaseEntry<>(key, value);
    }

    /**
     * Position in bytes.
     */
    private ByteBuffer readByteBuffer(int position) throws IOException {
        int length = readInt(position);
        readToPositionIfNeed(position + Integer.BYTES + length - 1);
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put(bytes, position + Integer.BYTES, length);
        buffer.flip();
        return buffer;
    }

    /**
     * Position in bytes.
     */
    private Integer readInt(int position) throws IOException {
        readToPositionIfNeed(position + Integer.BYTES - 1);
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes, position, Integer.BYTES);
        buffer.flip();
        return buffer.getInt();
    }

    private void readToPositionIfNeed(int position) throws IOException {
        if (currentFullness < position) {
            int len = position - currentFullness;
            if (read(bytes, currentFullness + 1, len) != len) {
                throw new EOFException("Read bytes error");
            }
            currentFullness = position;
        }
    }

}
