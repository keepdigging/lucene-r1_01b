package com.lucene.document;

import java.io.Reader;

/**
 * A field is a section of a Document.  Each field has two parts, a name and a
 * value.  Values may be free text, provided as a String or as a Reader, or they
 * may be atomic keywords, which are not further processed.  Such keywords may
 * be used to represent dates, urls, etc.  Fields are optionally stored in the
 * index, so that they may be returned with hits on the document.
 */

public final class Field {
    private String name = "body";
    private String stringValue = null;
    private Reader readerValue = null;
    private boolean isStored = false;
    private boolean isIndexed = true;
    private boolean isTokenized = true;

    /**
     * Constructs a String-valued Field that is not tokenized, but is indexed
     * and stored.  Useful for non-text fields, e.g. date or url.
     */
    public static final Field Keyword(String name, String value) {
        return new Field(name, value, true, true, false);
    }

    /**
     * Constructs a String-valued Field that is not tokenized or indexed,
     * but is stored in the index, for return with hits.
     */
    public static final Field UnIndexed(String name, String value) {
        return new Field(name, value, true, false, false);
    }

    /**
     * Constructs a String-valued Field that is tokenized and indexed,
     * and is stored in the index, for return with hits.  Useful for short text
     * fields, like "title" or "subject".
     */
    public static final Field Text(String name, String value) {
        return new Field(name, value, true, true, true);
    }

    /**
     * Constructs a String-valued Field that is tokenized and indexed,
     * but that is not stored in the index.
     */
    public static final Field UnStored(String name, String value) {
        return new Field(name, value, false, true, true);
    }

    /**
     * Constructs a Reader-valued Field that is tokenized and indexed, but is
     * not stored in the index verbatim.  Useful for longer text fields, like
     * "body".
     */
    public static final Field Text(String name, Reader value) {
        return new Field(name, value);
    }

    /**
     * The name of the field (e.g., "date", "subject", "title", "body", etc.)
     * as an interned string.
     */
    public String name() {
        return name;
    }

    /**
     * The value of the field as a String, or null.  If null, the Reader value
     * is used.  Exactly one of stringValue() and readerValue() must be set.
     */
    public String stringValue() {
        return stringValue;
    }

    /**
     * The value of the field as a Reader, or null.  If null, the String value
     * is used.  Exactly one of stringValue() and readerValue() must be set.
     */
    public Reader readerValue() {
        return readerValue;
    }

    public Field(String n, String string,
                 boolean store, boolean index, boolean token) {
        name = n.intern();                  // field names are interned
        stringValue = string;
        isStored = store;
        isIndexed = index;
        isTokenized = token;
    }

    Field(String n, Reader r) {
        name = n.intern();                  // field names are interned
        readerValue = r;
    }

    /**
     * True iff the value of the field is to be stored in the index for return
     * with search hits.  It is an error for this to be true if a field is
     * Reader-valued.
     */
    public final boolean isStored() {
        return isStored;
    }

    /**
     * True iff the value of the field is to be indexed, so that it may be
     * searched on.
     */
    public final boolean isIndexed() {
        return isIndexed;
    }

    /**
     * True iff the value of the field should be tokenized as text prior to
     * indexing.  Un-tokenized fields are indexed as a single word and may not be
     * Reader-valued.
     */
    public final boolean isTokenized() {
        return isTokenized;
    }

    /**
     * Prints a Field for human consumption.
     */
    public final String toString() {
        if (isStored && isIndexed && !isTokenized)
            return "Keyword<" + name + ":" + stringValue + ">";
        else if (isStored && !isIndexed && !isTokenized)
            return "Unindexed<" + name + ":" + stringValue + ">";
        else if (isStored && isIndexed && isTokenized && stringValue != null)
            return "Text<" + name + ":" + stringValue + ">";
        else if (!isStored && isIndexed && isTokenized && readerValue != null)
            return "Text<" + name + ":" + readerValue + ">";
        else
            return super.toString();
    }

}
