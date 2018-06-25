package com.lucene.index;

import java.util.Vector;
import java.io.IOException;

import com.lucene.store.Directory;
import com.lucene.store.InputStream;
import com.lucene.store.OutputStream;

final class SegmentInfos extends Vector {

    public int counter = 0;// doc总数量

    public final SegmentInfo info(int i) {
        return (SegmentInfo) elementAt(i);
    }

    /**
     * 从segments文件读取数据
     * @param directory
     * @throws IOException
     */
    public final void read(Directory directory) throws IOException {
        InputStream input = directory.openFile("segments");
        try {
            counter = input.readInt();          // read counter
            for (int i = input.readInt(); i > 0; i--) { // read segmentInfos
                SegmentInfo si = new SegmentInfo(input.readString(), input.readInt(),
                        directory);
                addElement(si);
            }
        } finally {
            input.close();
        }
    }

    /**
     * 写入数据到segments
     * @param directory
     * @throws IOException
     */
    public final void write(Directory directory) throws IOException {
        OutputStream output = directory.createFile("segments.new");
        try {
            output.writeInt(counter);              // write counter
            output.writeInt(size());              // write infos
            for (int i = 0; i < size(); i++) {
                SegmentInfo si = info(i);
                output.writeString(si.name);
                output.writeInt(si.docCount);
            }
        } finally {
            output.close();
        }

        // install new segment info
        directory.renameFile("segments.new", "segments");
    }
}
