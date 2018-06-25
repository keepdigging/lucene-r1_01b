package com.lucene.index;

/**
 * A TermInfo is the record of information stored for a term.
 */

final class TermInfo {
    /**
     * The number of documents which contain the term.
     */
    int docFreq = 0;

    long freqPointer = 0;
    long proxPointer = 0;

    TermInfo() {
    }

    TermInfo(int df, long fp, long pp) {
        docFreq = df;
        freqPointer = fp;
        proxPointer = pp;
    }

    TermInfo(TermInfo ti) {
        docFreq = ti.docFreq;
        freqPointer = ti.freqPointer;
        proxPointer = ti.proxPointer;
    }

    final void set(int df, long fp, long pp) {
        docFreq = df;
        freqPointer = fp;
        proxPointer = pp;
    }

    final void set(TermInfo ti) {
        docFreq = ti.docFreq;
        freqPointer = ti.freqPointer;
        proxPointer = ti.proxPointer;
    }
}
