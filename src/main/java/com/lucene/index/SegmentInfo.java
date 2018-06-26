package com.lucene.index;

import com.lucene.store.Directory;

/**
 *
 * SegmentInfo对应一个document
 *
 */
final class SegmentInfo {
    public String name;                  // unique name in dir
    public int docCount;                  // number of docs in seg
    public Directory dir;                  // where segment resides

    public SegmentInfo(String name, int docCount, Directory dir) {
        this.name = name;
        this.docCount = docCount;
        this.dir = dir;
    }
}
