package com.lucene.store;

import java.io.IOException;

/**
 * Abstract class for input from a file in a Directory.
 *
 * @author Doug Cutting
 */

/**
 * A random-access input stream
 */
abstract public class InputStream {
    final static int BUFFER_SIZE = OutputStream.BUFFER_SIZE;

    private final byte[] buffer = new byte[BUFFER_SIZE];

    private long bufferStart = 0;              // position in file of buffer
    private int bufferLength = 0;              // end of valid bytes
    private int bufferPosition = 0;          // next byte to read

    protected long length;              // set by subclasses

    /**
     * InputStream-like methods @see java.io.InputStream
     */
    public final byte readByte() throws IOException {
        if (bufferPosition >= bufferLength)
            refill();
        return buffer[bufferPosition++];
    }

    public final void readBytes(byte[] b, int offset, int len)
            throws IOException {
        if (len < BUFFER_SIZE) {
            for (int i = 0; i < len; i++)          // read byte-by-byte
                b[i + offset] = readByte();
        } else {                      // read all-at-once
            long start = getFilePointer();
            seekInternal(start);
            readInternal(b, offset, len);

            bufferStart = start + len;          // adjust stream variables
            bufferPosition = 0;
            bufferLength = 0;                  // trigger refill() on read
        }
    }

    public final int readInt() throws IOException {
        return ((readByte() & 0xFF) << 24) | ((readByte() & 0xFF) << 16)
                | ((readByte() & 0xFF) << 8) | (readByte() & 0xFF);
    }

    public final int readVInt() throws IOException {
        byte b = readByte();
        int i = b & 0x7F;
        for (int shift = 7; (b & 0x80) != 0; shift += 7) {
            b = readByte();
            i |= (b & 0x7F) << shift;
        }
        return i;
    }

    public final long readLong() throws IOException {
        return (((long) readInt()) << 32) | (readInt() & 0xFFFFFFFFL);
    }

    public final long readVLong() throws IOException {
        byte b = readByte();
        long i = b & 0x7F;
        for (int shift = 7; (b & 0x80) != 0; shift += 7) {
            b = readByte();
            i |= (b & 0x7FL) << shift;
        }
        return i;
    }

    private char[] chars = new char[100];

    public final String readString() throws IOException {
        int length = readVInt();
        if (length > chars.length)
            chars = new char[length];
        readChars(chars, 0, length);
        return new String(chars, 0, length);
    }

    public final void readChars(char[] buffer, int start, int length)
            throws IOException {
        final int end = start + length;
        for (int i = start; i < end; i++) {
            byte b = readByte();
            if ((b & 0x80) == 0)
                buffer[i] = (char) (b & 0x7F);
            else if ((b & 0xE0) != 0xE0) {
                buffer[i] = (char) (((b & 0x1F) << 6)
                        | (readByte() & 0x3F));
            } else
                buffer[i] = (char) (((b & 0x0F) << 12)
                        | ((readByte() & 0x3F) << 6)
                        | (readByte() & 0x3F));
        }
    }


    protected final void refill() throws IOException {
        long start = bufferStart + bufferPosition;
        long end = start + BUFFER_SIZE;
        if (end > length)                  // don't read past EOF
            end = length;
        bufferLength = (int) (end - start);
        if (bufferLength == 0)
            throw new IOException("read past EOF");

        readInternal(buffer, 0, bufferLength);

        bufferStart = start;
        bufferPosition = 0;
    }

    abstract protected void readInternal(byte[] b, int offset, int length)
            throws IOException;

    abstract public void close() throws IOException;

    /**
     * RandomAccessFile-like methods @see java.io.RandomAccessFile
     */
    public final long getFilePointer() {
        return bufferStart + bufferPosition;
    }

    public final void seek(long pos) throws IOException {
        if (pos >= bufferStart && pos < (bufferStart + bufferLength))
            bufferPosition = (int) (pos - bufferStart);  // seek within buffer
        else {
            bufferStart = pos;
            bufferPosition = 0;
            bufferLength = 0;                  // trigger refill() on read()
            seekInternal(pos);
        }
    }

    abstract protected void seekInternal(long pos) throws IOException;

    public final long length() {
        return length;
    }

}
