package com.lucene.store;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Straightforward implementation of Directory as a directory of files.
 *
 * @author Doug Cutting
 * @see Directory
 */

final public class FSDirectory extends Directory {
    File directory = null;
    boolean isOpen = false;

    public FSDirectory(String path, boolean create) throws IOException {
        this(new File(path), create);
    }

    public FSDirectory(File path, boolean create) throws IOException {
        directory = path;
        if (!directory.exists() && create)
            directory.mkdir();
        if (!directory.isDirectory())
            throw new IOException(path + " not a directory");

        if (create) {                  // clear old files
            String[] files = directory.list();
            for (int i = 0; i < files.length; i++) {
                File file = new File(directory, files[i]);
                if (!file.delete())
                    throw new IOException("couldn't delete " + files[i]);
            }
        }

        isOpen = true;
    }

    /**
     * Returns an array of strings, one for each file in the directory.
     */
    public final String[] list() throws IOException {
        if (!isOpen)
            throw new IOException("store is not open");
        return directory.list();
    }

    /**
     * Returns true iff a file with the given name exists.
     */
    public final boolean fileExists(String name) throws IOException {
        if (!isOpen)
            throw new IOException("store is not open");
        File file = new File(directory, name);
        return file.exists();
    }

    /**
     * Returns the time the named file was last modified.
     */
    public final long fileModified(String name) throws IOException {
        if (!isOpen)
            throw new IOException("store is not open");
        File file = new File(directory, name);
        return file.lastModified();
    }

    /**
     * Returns the time the named file was last modified.
     */
    public static final long fileModified(File directory, String name) {
        File file = new File(directory, name);
        return file.lastModified();
    }

    /**
     * Returns the length in bytes of a file in the directory.
     */
    public final long fileLength(String name) throws IOException {
        if (!isOpen)
            throw new IOException("store is not open");

        File file = new File(directory, name);
        return file.length();
    }

    /**
     * Removes an existing file in the directory.
     */
    public final void deleteFile(String name) throws IOException {
        if (!isOpen)
            throw new IOException("store is not open");

        File file = new File(directory, name);
        if (!file.delete())
            throw new IOException("couldn't delete " + name);
    }

    /**
     * Renames an existing file in the directory.
     */
    public final void renameFile(String from, String to) throws IOException {
        if (!isOpen)
            throw new IOException("store is not open");

        File old = new File(directory, from);
        File nu = new File(directory, to);

    /* This is not atomic.  If the program crashes between the call to
       delete() and the call to renameTo() then we're screwed, but I've
       been unable to figure out how else to do this... */

        if (nu.exists())
            if (!nu.delete())
                throw new IOException("couldn't delete " + to);

        if (!old.renameTo(nu))
            throw new IOException("couldn't rename " + from + " to " + to);
    }

    /**
     * Creates a new, empty file in the directory with the given name.
     * Returns a stream writing this file.
     */
    public final OutputStream createFile(String name) throws IOException {
        if (!isOpen)
            throw new IOException("store is not open");

        return new FSOutputStream(new File(directory, name));
    }

    /**
     * Returns a stream reading an existing file.
     */
    public final InputStream openFile(String name) throws IOException {
        if (!isOpen)
            throw new IOException("store is not open");

        return new FSInputStream(new File(directory, name));
    }

    /**
     * Closes the store to future operations.
     */
    public final void close() throws IOException {
        if (!isOpen)
            throw new IOException("store is already closed");
        isOpen = false;
    }
}


final class FSInputStream extends InputStream {
    RandomAccessFile file = null;

    public FSInputStream(File path) throws IOException {
        file = new RandomAccessFile(path, "r");
        length = file.length();
    }

    /**
     * InputStream methods
     */
    public final void readInternal(byte[] b, int offset, int len)
            throws IOException {
        if (file.read(b, offset, len) != len)
            throw new IOException("refill past EOF");
    }

    public final void close() throws IOException {
        file.close();
    }

    /**
     * Random-access methods
     */
    public final void seekInternal(long pos) throws IOException {
        file.seek(pos);
    }

    protected final void finalize() throws IOException {
        close();                      // close the file
    }
}


final class FSOutputStream extends OutputStream {
    RandomAccessFile file = null;

    public FSOutputStream(File path) throws IOException {
        if (path.isFile())
            throw new IOException(path + " already exists");
        file = new RandomAccessFile(path, "rw");
    }

    /**
     * output methods:
     */
    public final void flushBuffer(byte[] b, int size) throws IOException {
        file.write(b, 0, size);
    }

    public final void close() throws IOException {
        super.close();
        file.close();
    }

    /**
     * Random-access methods
     */
    public final void seek(long pos) throws IOException {
        super.seek(pos);
        file.seek(pos);
    }

    public final long length() throws IOException {
        return file.length();
    }

    protected final void finalize() throws IOException {
        file.close();                  // close the file
    }

}
