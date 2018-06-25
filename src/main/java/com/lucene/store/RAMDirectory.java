package com.lucene.store;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

final public class RAMDirectory extends Directory {
    Hashtable files = new Hashtable();

    public RAMDirectory() {
    }

    /**
     * Returns an array of strings, one for each file in the directory.
     */
    public final String[] list() {
        String[] result = new String[files.size()];
        int i = 0;
        Enumeration names = files.keys();
        while (names.hasMoreElements())
            result[i++] = (String) names.nextElement();
        return result;
    }

    /**
     * Returns true iff the named file exists in this directory.
     */
    public final boolean fileExists(String name) {
        RAMFile file = (RAMFile) files.get(name);
        return file != null;
    }

    /**
     * Returns the time the named file was last modified.
     */
    public final long fileModified(String name) {
        RAMFile file = (RAMFile) files.get(name);
        return file.lastModified;
    }

    /**
     * Returns the length in bytes of a file in the directory.
     */
    public final long fileLength(String name) {
        RAMFile file = (RAMFile) files.get(name);
        return file.length;
    }

    /**
     * Removes an existing file in the directory.
     */
    public final void deleteFile(String name) {
        files.remove(name);
    }

    /**
     * Removes an existing file in the directory.
     */
    public final void renameFile(String from, String to) {
        RAMFile file = (RAMFile) files.get(from);
        files.remove(from);
        files.put(to, file);
    }

    /**
     * Creates a new, empty file in the directory with the given name.
     * Returns a stream writing this file.
     */
    public final OutputStream createFile(String name) {
        RAMFile file = new RAMFile();
        files.put(name, file);
        return new RAMOutputStream(file);
    }

    /**
     * Returns a stream reading an existing file.
     */
    public final InputStream openFile(String name) {
        RAMFile file = (RAMFile) files.get(name);
        return new RAMInputStream(file);
    }

    /**
     * Closes the store to future operations.
     */
    public final void close() {
    }
}


final class RAMInputStream extends InputStream {
    RAMFile file;
    int pointer = 0;

    public RAMInputStream(RAMFile f) {
        file = f;
        length = file.length;
    }

    /**
     * InputStream methods
     */
    public final void readInternal(byte[] dest, int destOffset, int len) {
        int bufferNumber = pointer / InputStream.BUFFER_SIZE;
        int bufferOffset = pointer % InputStream.BUFFER_SIZE;
        int bytesInBuffer = InputStream.BUFFER_SIZE - bufferOffset;
        int bytesToCopy = bytesInBuffer >= len ? len : bytesInBuffer;
        byte[] buffer = (byte[]) file.buffers.elementAt(bufferNumber);
        System.arraycopy(buffer, bufferOffset, dest, destOffset, bytesToCopy);

        if (bytesToCopy < len) {              // not all in one buffer
            destOffset += bytesToCopy;
            bytesToCopy = len - bytesToCopy;          // remaining bytes
            buffer = (byte[]) file.buffers.elementAt(bufferNumber + 1);
            System.arraycopy(buffer, 0, dest, destOffset, bytesToCopy);
        }
        pointer += len;
    }

    public final void close() {
    }

    /**
     * Random-access methods
     */
    public final void seekInternal(long pos) {
        pointer = (int) pos;
    }
}


final class RAMOutputStream extends OutputStream {
    RAMFile file;
    int pointer = 0;

    public RAMOutputStream(RAMFile f) {
        file = f;
    }

    /**
     * output methods:
     */
    public final void flushBuffer(byte[] src, int len) {
        int bufferNumber = pointer / OutputStream.BUFFER_SIZE;
        int bufferOffset = pointer % OutputStream.BUFFER_SIZE;
        int bytesInBuffer = OutputStream.BUFFER_SIZE - bufferOffset;
        int bytesToCopy = bytesInBuffer >= len ? len : bytesInBuffer;

        if (bufferNumber == file.buffers.size())
            file.buffers.addElement(new byte[OutputStream.BUFFER_SIZE]);

        byte[] buffer = (byte[]) file.buffers.elementAt(bufferNumber);
        System.arraycopy(src, 0, buffer, bufferOffset, bytesToCopy);

        if (bytesToCopy < len) {              // not all in one buffer
            int srcOffset = bytesToCopy;
            bytesToCopy = len - bytesToCopy;          // remaining bytes
            bufferNumber++;
            if (bufferNumber == file.buffers.size())
                file.buffers.addElement(new byte[OutputStream.BUFFER_SIZE]);
            buffer = (byte[]) file.buffers.elementAt(bufferNumber);
            System.arraycopy(src, srcOffset, buffer, 0, bytesToCopy);
        }
        pointer += len;
        if (pointer > file.length)
            file.length = pointer;

        file.lastModified = System.currentTimeMillis();
    }

    public final void close() throws IOException {
        super.close();
    }

    /**
     * Random-access methods
     */
    public final void seek(long pos) throws IOException {
        super.seek(pos);
        pointer = (int) pos;
    }

    public final long length() {
        return file.length;
    }
}

final class RAMFile {
    Vector buffers = new Vector();
    long length;
    long lastModified = System.currentTimeMillis();
}
