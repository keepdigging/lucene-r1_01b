package com.lucene.analysis.standard;

import com.lucene.analysis.*;

import java.io.Reader;
import java.util.Hashtable;

/**
 * Filters {@link StandardTokenizer} with {@link StandardFilter}, {@link
 * LowerCaseFilter} and {@link StopFilter}.
 */
public final class StandardAnalyzer extends Analyzer {
    private Hashtable stopTable;

    /**
     * An array containing some common English words that are not usually useful
     * for searching.
     */
    public static final String[] STOP_WORDS = {
            "a", "and", "are", "as", "at", "be", "but", "by",
            "for", "if", "in", "into", "is", "it",
            "no", "not", "of", "on", "or", "s", "such",
            "t", "that", "the", "their", "then", "there", "these",
            "they", "this", "to", "was", "will", "with"
    };

    /**
     * Builds an analyzer.
     */
    public StandardAnalyzer() {
        this(STOP_WORDS);
    }

    /**
     * Builds an analyzer with the given stop words.
     */
    public StandardAnalyzer(String[] stopWords) {
        stopTable = StopFilter.makeStopTable(stopWords);
    }

    /**
     * Constructs a {@link StandardTokenizer} filtered by a {@link
     * StandardFilter}, a {@link LowerCaseFilter} and a {@link StopFilter}.
     */
    public final TokenStream tokenStream(Reader reader) {
        TokenStream result = new StandardTokenizer(reader);
        result = new StandardFilter(result);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, stopTable);
        return result;
    }
}
