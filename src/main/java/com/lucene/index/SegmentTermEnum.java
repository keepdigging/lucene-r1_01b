package com.lucene.index;

import java.io.IOException;

import com.lucene.store.InputStream;

final class SegmentTermEnum extends TermEnum {
    private InputStream input;
    private FieldInfos fieldInfos;
    int size;
    int position = -1;

    private Term term = new Term("", "");
    private TermInfo termInfo = new TermInfo();

    boolean isIndex = false;
    long indexPointer = 0;
    Term prev;

    private char[] buffer = {};

    SegmentTermEnum(InputStream i, FieldInfos fis, boolean isi)
            throws IOException {
        input = i;
        fieldInfos = fis;
        size = input.readInt();
        isIndex = isi;
    }

    final void seek(long pointer, int p, Term t, TermInfo ti)
            throws IOException {
        input.seek(pointer);
        position = p;
        term = t;
        prev = null;
        termInfo.set(ti);
        growBuffer(term.text.length());          // copy term text into buffer
    }

    /**
     * Increments the enumeration to the next element.  True if one exists.
     */
    public final boolean next() throws IOException {
        if (position++ >= size - 1) {
            term = null;
            return false;
        }

        prev = term;
        term = readTerm();

        termInfo.docFreq = input.readVInt();      // read doc freq
        termInfo.freqPointer += input.readVLong();      // read freq pointer
        termInfo.proxPointer += input.readVLong();      // read prox pointer

        if (isIndex)
            indexPointer += input.readVLong();      // read index pointer

        return true;
    }

    private final Term readTerm() throws IOException {
        int start = input.readVInt();
        int length = input.readVInt();
        int totalLength = start + length;
        if (buffer.length < totalLength)
            growBuffer(totalLength);

        input.readChars(buffer, start, length);
        return new Term(fieldInfos.fieldName(input.readVInt()),
                new String(buffer, 0, totalLength), false);
    }

    private final void growBuffer(int length) {
        buffer = new char[length];
        for (int i = 0; i < term.text.length(); i++)  // copy contents
            buffer[i] = term.text.charAt(i);
    }

    /**
     * Returns the current Term in the enumeration.
     * Initially invalid, valid after next() called for the first time.
     */
    public final Term term() {
        return term;
    }

    /**
     * Returns the current TermInfo in the enumeration.
     * Initially invalid, valid after next() called for the first time.
     */
    final TermInfo termInfo() {
        return new TermInfo(termInfo);
    }

    /**
     * Sets the argument to the current TermInfo in the enumeration.
     * Initially invalid, valid after next() called for the first time.
     */
    final void termInfo(TermInfo ti) {
        ti.set(termInfo);
    }

    /**
     * Returns the docFreq from the current TermInfo in the enumeration.
     * Initially invalid, valid after next() called for the first time.
     */
    public final int docFreq() {
        return termInfo.docFreq;
    }

    /* Returns the freqPointer from the current TermInfo in the enumeration.
      Initially invalid, valid after next() called for the first time.*/
    final long freqPointer() {
        return termInfo.freqPointer;
    }

    /* Returns the proxPointer from the current TermInfo in the enumeration.
      Initially invalid, valid after next() called for the first time.*/
    final long proxPointer() {
        return termInfo.proxPointer;
    }

    /**
     * Closes the enumeration to further activity, freeing resources.
     */
    public final void close() throws IOException {
        input.close();
    }
}
