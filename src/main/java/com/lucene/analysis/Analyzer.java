package com.lucene.analysis;

import java.io.Reader;

/**
 * An Analyzer builds TokenStreams, which analyze text.  It thus represents a
 * policy for extracting index terms from text.
 * <p>
 * Typical implementations first build a Tokenizer, which breaks the stream of
 * characters from the Reader into raw Tokens.  One or more TokenFilters may
 * then be applied to the output of the Tokenizer.
 */

abstract public class Analyzer {
    /**
     * Creates a TokenStream which tokenizes all the text in the provided
     * Reader.
     */
    abstract public TokenStream tokenStream(Reader reader);
}

