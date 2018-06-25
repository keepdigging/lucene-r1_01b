package com.lucene.search;

import java.io.IOException;

import com.lucene.index.TermDocs;

final class TermScorer extends Scorer {
    private TermDocs termDocs;
    private byte[] norms;
    private float weight;
    private int doc;

    private final int[] docs = new int[128];      // buffered doc numbers
    private final int[] freqs = new int[128];      // buffered term freqs
    private int pointer;
    private int pointerMax;

    private static final int SCORE_CACHE_SIZE = 32;
    private float[] scoreCache = new float[SCORE_CACHE_SIZE];

    TermScorer(TermDocs td, byte[] n, float w) throws IOException {
        termDocs = td;
        norms = n;
        weight = w;

        for (int i = 0; i < SCORE_CACHE_SIZE; i++)
            scoreCache[i] = Similarity.tf(i) * weight;

        pointerMax = termDocs.read(docs, freqs);      // fill buffers

        if (pointerMax != 0)
            doc = docs[0];
        else {
            termDocs.close();                  // close stream
            doc = Integer.MAX_VALUE;              // set to sentinel value
        }
    }

    final void score(HitCollector c, final int end) throws IOException {
        int d = doc;                  // cache doc in local
        while (d < end) {                  // for docs in window
            final int f = freqs[pointer];
            float score =                  // compute tf(f)*weight
                    f < SCORE_CACHE_SIZE              // check cache
                            ? scoreCache[f]              // cache hit
                            : Similarity.tf(f) * weight;          // cache miss

            score *= Similarity.norm(norms[d]);      // normalize for field

            c.collect(d, score);              // collect score

            if (++pointer == pointerMax) {
                pointerMax = termDocs.read(docs, freqs);  // refill buffers
                if (pointerMax != 0) {
                    pointer = 0;
                } else {
                    termDocs.close();              // close stream
                    doc = Integer.MAX_VALUE;          // set to sentinel value
                    return;
                }
            }
            d = docs[pointer];
        }
        doc = d;                      // flush cache
    }
}
