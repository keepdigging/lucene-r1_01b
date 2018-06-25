package com.lucene.index;

import java.io.IOException;

import com.lucene.document.Document;


/**
 * TermPositions provides an interface for enumerating the &lt;document,
 * frequency, &lt;position&gt;* &gt; tuples for a term.  <p> The document and
 * frequency are as for a TermDocs.  The positions portion lists the ordinal
 * positions of each occurence of a term in a document.
 *
 * @see IndexReader#termPositions
 */

public interface TermPositions extends TermDocs {
    /**
     * Returns next position in the current document.  It is an error to call
     * this more than {@link #freq()} times
     * without calling {@link #next()}<p> This is
     * invalid until {@link #next()} is called for
     * the first time.
     */
    int nextPosition() throws IOException;
}  
