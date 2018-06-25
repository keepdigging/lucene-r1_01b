package com.lucene.index;

import java.io.IOException;

import com.lucene.store.InputStream;

final class SegmentTermPositions
        extends SegmentTermDocs implements TermPositions {
    private InputStream proxStream;
    private int proxCount = 0;

    SegmentTermPositions() {
    }

    SegmentTermPositions(SegmentReader p, TermInfo ti)
            throws IOException {
        open(p, ti);
    }

    public final void open(SegmentReader p, TermInfo ti) throws IOException {
        super.open(p, ti);
        proxStream = parent.openProxStream();
        proxStream.seek(ti.proxPointer);
    }

    public final void close() throws IOException {
        super.close();
        parent.closeProxStream(proxStream);
    }

    public final int nextPosition() throws IOException {
        proxCount--;
        return proxStream.readVInt();
    }

    protected final void skippingDoc() throws IOException {
        for (int f = freq; f > 0; f--)          // skip all positions
            proxStream.readVInt();
    }

    public final boolean next() throws IOException {
        for (int f = proxCount; f > 0; f--)          // skip unread positions
            proxStream.readVInt();

        if (super.next()) {                  // run super
            proxCount = freq;                  // note frequency
            return true;
        }
        return false;
    }

    public final int read(final int[] docs, final int[] freqs) {
        throw new RuntimeException();
    }
}
