package org.kanga.lucenetesting;

import java.nio.file.Path;

import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.store.FSDirectory;

public class SegmentTest {
    public static void main(String[] args) throws Exception {
        Path dir = Path.of("/Users/dacut/projects/lucene-testing/app/indextest");
        FSDirectory segmentDir = FSDirectory.open(dir);
        SegmentInfos si = SegmentInfos.readCommit(segmentDir, "segments_1");
        System.out.println(si);
    }
}
