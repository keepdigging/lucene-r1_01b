package com.lucene.index;

final class FieldInfo {
    String name;
    boolean isIndexed;
    int number;

    FieldInfo(String na, boolean tk, int nu) {
        name = na;
        isIndexed = tk;
        number = nu;
    }
}
