package com.lucene.analysis;

import java.io.IOException;

/**
 * A TokenFilter is a TokenStream whose input is another token stream.
 * <p>
 * This is an abstract class.
 */

abstract public class TokenFilter extends TokenStream {
    /**
     * The source of tokens for this filter.
     */
    protected TokenStream input;

    /**
     * Close the input TokenStream.
     */
    public void close() throws IOException {
        input.close();
    }

}

