package com.lucene.search;

import java.io.IOException;

import com.lucene.index.Term;
import com.lucene.index.TermDocs;
import com.lucene.index.IndexReader;

/**
 * A Query that matches documents containing a term.
 * This may be combined with other terms with a {@link BooleanQuery}.
 */
final public class TermQuery extends Query {
    private Term term;
    private float boost = 1.0f;
    private float idf = 0.0f;
    private float weight = 0.0f;

    /**
     * Constructs a query for the term <code>t</code>.
     */
    public TermQuery(Term t) {
        term = t;
    }

    /**
     * Sets the boost for this term to <code>b</code>.  Documents containing
     * this term will (in addition to the normal weightings) have their score
     * multiplied by <code>b</code>.
     */
    public void setBoost(float b) {
        boost = b;
    }

    /**
     * Gets the boost for this term.  Documents containing
     * this term will (in addition to the normal weightings) have their score
     * multiplied by <code>b</code>.   The boost is 1.0 by default.
     */
    public float getBoost() {
        return boost;
    }

    final float sumOfSquaredWeights(Searcher searcher) throws IOException {
        idf = Similarity.idf(term, searcher);
        weight = idf * boost;
        return weight * weight;              // square term weights
    }

    final void normalize(float norm) {
        weight *= norm;                  // normalize for query
        weight *= idf;                  // factor from document
    }

    Scorer scorer(IndexReader reader)
            throws IOException {
        TermDocs termDocs = reader.termDocs(term);

        if (termDocs == null)
            return null;

        return new TermScorer(termDocs, reader.norms(term.field()), weight);
    }

    /**
     * Prints a user-readable version of this query.
     */
    public String toString(String field) {
        StringBuffer buffer = new StringBuffer();
        if (!term.field().equals(field)) {
            buffer.append(term.field());
            buffer.append(":");
        }
        buffer.append(term.text());
        if (boost != 1.0f) {
            buffer.append("^");
            buffer.append(Float.toString(boost));
        }
        return buffer.toString();
    }
}
