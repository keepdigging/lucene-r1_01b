package com.lucene.search;

import java.io.IOException;

abstract class Scorer {
    abstract void score(HitCollector hc, int maxDoc) throws IOException;
}
