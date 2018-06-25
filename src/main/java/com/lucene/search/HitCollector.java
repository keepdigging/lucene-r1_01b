package com.lucene.search;

/**
 * Lower-level search API.
 *
 * @see IndexSearcher#search(Query, HitCollector)
 */
public abstract class HitCollector {
    /**
     * Called once for every non-zero scoring document, with the document number
     * and its score.
     *
     * <P>If, for example, an application wished to collect all of the hits for a
     * query in a BitSet, then it might:<pre>
     *   Searcher = new IndexSearcher(indexReader);
     *   final BitSet bits = new BitSet(indexReader.maxDoc());
     *   searcher.search(query, new HitCollector() {
     *       public void collect(int doc, float score) {
     *         bits.set(doc);
     *       }
     *     });
     * </pre>
     */
    public abstract void collect(int doc, float score);
}
