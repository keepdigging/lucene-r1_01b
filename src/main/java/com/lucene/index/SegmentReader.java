package com.lucene.index;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import com.lucene.util.BitVector;
import com.lucene.store.Directory;
import com.lucene.store.InputStream;
import com.lucene.document.Document;

final class SegmentReader extends IndexReader {
    Directory directory;
    private boolean closeDirectory = false;
    private String segment;

    FieldInfos fieldInfos;
    private FieldsReader fieldsReader;

    TermInfosReader tis;

    BitVector deletedDocs = null;
    private boolean deletedDocsDirty = false;

    private InputStream freqStream;
    private InputStream proxStream;

    private Hashtable normsCache = new Hashtable();

    SegmentReader(SegmentInfo si, boolean closeDir)
            throws IOException {
        this(si);
        closeDirectory = closeDir;
    }

    SegmentReader(SegmentInfo si)
            throws IOException {
        directory = si.dir;
        segment = si.name;

        fieldInfos = new FieldInfos(directory, segment + ".fnm");
        fieldsReader = new FieldsReader(directory, segment, fieldInfos);

        tis = new TermInfosReader(directory, segment, fieldInfos);

        if (hasDeletions(si))
            deletedDocs = new BitVector(directory, segment + ".del");
    }

    public final synchronized void close() throws IOException {
        if (deletedDocsDirty) {
            deletedDocs.write(directory, segment + ".tmp");
            directory.renameFile(segment + ".tmp", segment + ".del");
            deletedDocsDirty = false;
        }

        fieldsReader.close();
        tis.close();

        if (freqStream != null)
            freqStream.close();
        if (proxStream != null)
            proxStream.close();

        if (closeDirectory)
            directory.close();
    }

    final static boolean hasDeletions(SegmentInfo si) throws IOException {
        return si.dir.fileExists(si.name + ".del");
    }

    public final synchronized void delete(int docNum) {
        if (deletedDocs == null)
            deletedDocs = new BitVector(maxDoc());
        deletedDocsDirty = true;
        deletedDocs.set(docNum);
    }

    final Vector files() throws IOException {
        Vector files = new Vector(16);
        files.addElement(segment + ".fnm");
        files.addElement(segment + ".fdx");
        files.addElement(segment + ".fdt");
        files.addElement(segment + ".tii");
        files.addElement(segment + ".tis");
        files.addElement(segment + ".frq");
        files.addElement(segment + ".prx");

        if (directory.fileExists(segment + ".del"))
            files.addElement(segment + ".del");

        for (int i = 0; i < fieldInfos.size(); i++) {
            FieldInfo fi = fieldInfos.fieldInfo(i);
            if (fi.isIndexed)
                files.addElement(segment + ".f" + i);
        }
        return files;
    }

    public final TermEnum terms() throws IOException {
        return tis.terms();
    }

    public final TermEnum terms(Term t) throws IOException {
        return tis.terms(t);
    }

    public final synchronized Document document(int n) throws IOException {
        if (isDeleted(n))
            throw new IllegalArgumentException
                    ("attempt to access a deleted document");
        return fieldsReader.doc(n);
    }

    public final synchronized boolean isDeleted(int n) {
        return (deletedDocs != null && deletedDocs.get(n));
    }

    public final TermDocs termDocs(Term t) throws IOException {
        TermInfo ti = tis.get(t);
        if (ti != null)
            return new SegmentTermDocs(this, ti);
        else
            return null;
    }

    synchronized final InputStream openFreqStream() throws IOException {
        InputStream fs;
        if (freqStream != null) {
            fs = freqStream;
            freqStream = null;
        } else
            fs = directory.openFile(segment + ".frq");
        return fs;
    }

    synchronized final void closeFreqStream(InputStream fs)
            throws IOException {
        if (freqStream == null)
            freqStream = fs;
        else
            fs.close();
    }

    public final TermPositions termPositions(Term t) throws IOException {
        TermInfo ti = tis.get(t);
        if (ti != null)
            return new SegmentTermPositions(this, ti);
        else
            return null;
    }

    synchronized final InputStream openProxStream() throws IOException {
        InputStream ps;
        if (proxStream != null) {
            ps = proxStream;
            proxStream = null;
        } else
            ps = directory.openFile(segment + ".prx");
        return ps;
    }

    synchronized final void closeProxStream(InputStream ps)
            throws IOException {
        if (proxStream == null)
            proxStream = ps;
        else
            ps.close();
    }

    public final int docFreq(Term t) throws IOException {
        TermInfo ti = tis.get(t);
        if (ti != null)
            return ti.docFreq;
        else
            return 0;
    }

    public final int numDocs() {
        int n = maxDoc();
        if (deletedDocs != null)
            n -= deletedDocs.count();
        return n;
    }

    public final int maxDoc() {
        return fieldsReader.size();
    }

    public final synchronized byte[] norms(String field) throws IOException {
        byte[] bytes = (byte[]) normsCache.get(field);
        if (bytes != null)
            return bytes;                  // cache hit

        bytes = new byte[maxDoc()];
        norms(field, bytes, 0);
        normsCache.put(field, bytes);          // update cache
        return bytes;
    }

    final void norms(String field, byte[] bytes, int offset)
            throws IOException {
        InputStream normStream = normStream(field);      // try to open file
        if (normStream == null)              // none for this field
            return;                      // use zeros
        try {
            normStream.readBytes(bytes, offset, (int) normStream.length());
        } finally {
            normStream.close();
        }
    }

    final InputStream normStream(String fieldName) throws IOException {
        FieldInfo fi = fieldInfos.fieldInfo(fieldName);
        if (fi != null && fi.isIndexed)
            return directory.openFile(segment + ".f" + fi.number);
        else
            return null;
    }
}
