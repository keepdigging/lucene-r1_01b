package com.lucene.index;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.io.IOException;

import com.lucene.document.Document;
import com.lucene.document.Field;

import com.lucene.store.Directory;
import com.lucene.store.OutputStream;
import com.lucene.store.InputStream;

final class FieldInfos {
    private Vector byNumber = new Vector();
    private Hashtable byName = new Hashtable();

    FieldInfos() {
        add("", false);
    }

    FieldInfos(Directory d, String name) throws IOException {
        InputStream input = d.openFile(name);
        try {
            read(input);
        } finally {
            input.close();
        }
    }

    /**
     * Adds field info for a Document.
     */
    final void add(Document doc) {
        Enumeration fields = doc.fields();
        while (fields.hasMoreElements()) {
            Field field = (Field) fields.nextElement();
            add(field.name(), field.isIndexed());
        }
    }

    /**
     * Merges in information from another FieldInfos.
     */
    final void add(FieldInfos other) {
        for (int i = 0; i < other.size(); i++) {
            FieldInfo fi = other.fieldInfo(i);
            add(fi.name, fi.isIndexed);
        }
    }

    private final void add(String name, boolean isIndexed) {
        FieldInfo fi = fieldInfo(name);
        if (fi == null)
            addInternal(name, isIndexed);
        else if (fi.isIndexed != isIndexed)
            throw new IllegalStateException("field " + name +
                    (fi.isIndexed ? " must" : " cannot") +
                    " be an indexed field.");
    }

    private final void addInternal(String name, boolean isIndexed) {
        FieldInfo fi = new FieldInfo(name, isIndexed, byNumber.size());
        byNumber.addElement(fi);
        byName.put(name, fi);
    }

    final int fieldNumber(String fieldName) {
        FieldInfo fi = fieldInfo(fieldName);
        if (fi != null)
            return fi.number;
        else
            return -1;
    }

    final FieldInfo fieldInfo(String fieldName) {
        return (FieldInfo) byName.get(fieldName);
    }

    final String fieldName(int fieldNumber) {
        return fieldInfo(fieldNumber).name;
    }

    final FieldInfo fieldInfo(int fieldNumber) {
        return (FieldInfo) byNumber.elementAt(fieldNumber);
    }

    final int size() {
        return byNumber.size();
    }

    final void write(Directory d, String name) throws IOException {
        OutputStream output = d.createFile(name);
        try {
            write(output);
        } finally {
            output.close();
        }
    }

    final void write(OutputStream output) throws IOException {
        output.writeVInt(size());
        for (int i = 0; i < size(); i++) {
            FieldInfo fi = fieldInfo(i);
            output.writeString(fi.name);
            output.writeByte((byte) (fi.isIndexed ? 1 : 0));
        }
    }

    private final void read(InputStream input) throws IOException {
        int size = input.readVInt();
        for (int i = 0; i < size; i++)
            addInternal(input.readString().intern(),
                    input.readByte() != 0);
    }
}
