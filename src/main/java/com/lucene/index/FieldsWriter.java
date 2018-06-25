package com.lucene.index;

import com.lucene.document.Document;
import com.lucene.document.Field;
import com.lucene.store.Directory;
import com.lucene.store.OutputStream;

import java.io.IOException;
import java.util.Enumeration;

final class FieldsWriter {
    private FieldInfos fieldInfos;
    private OutputStream fieldsStream;
    private OutputStream indexStream;

    FieldsWriter(Directory d, String segment, FieldInfos fn)
            throws IOException {
        fieldInfos = fn;
        fieldsStream = d.createFile(segment + ".fdt");
        indexStream = d.createFile(segment + ".fdx");
    }

    final void close() throws IOException {
        fieldsStream.close();
        indexStream.close();
    }

    final void addDocument(Document doc) throws IOException {
        indexStream.writeLong(fieldsStream.getFilePointer());

        int storedCount = 0;
        Enumeration fields = doc.fields();
        while (fields.hasMoreElements()) {
            Field field = (Field) fields.nextElement();
            if (field.isStored())
                storedCount++;
        }
        fieldsStream.writeVInt(storedCount);

        fields = doc.fields();
        while (fields.hasMoreElements()) {
            Field field = (Field) fields.nextElement();
            if (field.isStored()) {
                fieldsStream.writeVInt(fieldInfos.fieldNumber(field.name()));

                byte bits = 0;
                if (field.isTokenized())
                    bits |= 1;
                fieldsStream.writeByte(bits);

                fieldsStream.writeString(field.stringValue());
            }
        }
    }
}
