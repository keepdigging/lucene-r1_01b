package com.lucene.analysis;

import java.io.Reader;
import java.io.IOException;

/**
 * A Tokenizer is a TokenStream whose input is a Reader.
 * <p>
 * This is an abstract class.
 */

abstract public class Tokenizer extends TokenStream {
    /**
     * The text source for this Tokenizer.
     */
    protected Reader input;

    /**
     * By default, closes the input Reader.
     */
    public void close() throws IOException {
        input.close();
    }
}

