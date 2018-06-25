package com.lucene.analysis;

/**
 * A Token is an occurence of a term from the text of a field.  It consists of
 * a term's text, the start and end offset of the term in the text of the field,
 * and a type string.
 * <p>
 * The start and end offsets permit applications to re-associate a token with
 * its source text, e.g., to display highlighted query terms in a document
 * browser, or to show matching text fragments in a KWIC (KeyWord In Context)
 * display, etc.
 * <p>
 * The type is an interned string, assigned by a lexical analyzer
 * (a.k.a. tokenizer), naming the lexical or syntactic class that the token
 * belongs to.  For example an end of sentence marker token might be implemented
 * with type "eos".  The default token type is "word".
 */

public final class Token {
    String termText;                  // the text of the term
    int startOffset;                  // start in source text
    int endOffset;                  // end in source text
    String type = "word";                  // lexical type

    /**
     * Constructs a Token with the given term text, and start & end offsets.
     * The type defaults to "word."
     */
    public Token(String text, int start, int end) {
        termText = text;
        startOffset = start;
        endOffset = end;
    }

    /**
     * Constructs a Token with the given text, start and end offsets, & type.
     */
    public Token(String text, int start, int end, String typ) {
        termText = text;
        startOffset = start;
        endOffset = end;
        type = typ;
    }

    /**
     * Returns the Token's term text.
     */
    public final String termText() {
        return termText;
    }

    /**
     * Returns this Token's starting offset, the position of the first character
     * corresponding to this token in the source text.
     * <p>
     * Note that the difference between endOffset() and startOffset() may not be
     * equal to termText.length(), as the term text may have been altered by a
     * stemmer or some other filter.
     */
    public final int startOffset() {
        return startOffset;
    }

    /**
     * Returns this Token's ending offset, one greater than the position of the
     * last character corresponding to this token in the source text.
     */
    public final int endOffset() {
        return endOffset;
    }

    /**
     * Returns this Token's lexical type.  Defaults to "word".
     */
    public final String type() {
        return type;
    }

}
