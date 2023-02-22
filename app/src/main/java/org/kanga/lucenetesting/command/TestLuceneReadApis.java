package org.kanga.lucenetesting.command;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.SegmentCommitInfo;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.kanga.lucenetesting.App;
import org.kanga.lucenetesting.ContextDumper;

public class TestLuceneReadApis implements Command {
    public TestLuceneReadApis() {
    }

    @Override
    public String getName() {
        return "test-lucene-read-apis";
    }

    @Override
    public void execute(App app, String[] args, int argOffset) throws Exception {
        Path indexPath = Path.of(app.getIndexDirectory());
        Directory directory = FSDirectory.open(indexPath);

        SegmentInfos segmentInfos = SegmentInfos.readLatestCommit(directory);
        System.out.println("SegmentInfos:");
        System.out.println("    CommitLuceneVersion: " + segmentInfos.getCommitLuceneVersion());
        System.out.println("    Generation: " + segmentInfos.getGeneration());
        System.out.println("    Id: " + idToString(segmentInfos.getId()));
        System.out.println("    LastGeneration: " + segmentInfos.getLastGeneration());
        System.out.println("    MinSegmentLuceneVersion: " + segmentInfos.getMinSegmentLuceneVersion());
        System.out.println("    SegmentsFileName: " + segmentInfos.getSegmentsFileName());
        System.out.println("    TotalMaxDoc: " + segmentInfos.totalMaxDoc());
        System.out.println("    Size: " + segmentInfos.size());
        System.out.println("    Version: " + segmentInfos.getVersion());
        Map<String, String> userData = segmentInfos.getUserData();
        if (userData == null) {
            System.out.println("    UserData: <null>");
        } else if (userData.size() == 0) {
            System.out.println("    UserData: <empty>");
        } else {
            System.out.println("    UserData:");
            for (Map.Entry<String, String> entry : userData.entrySet()) {
                System.out.println("        " + entry.getKey() + ": " + entry.getValue());
            }
        }

        for (SegmentCommitInfo sci: segmentInfos) {
            System.out.println();
            System.out.println("    SegmentCommitInfo:");
            System.out.println("        Codec: " + sci.info.getCodec());
            System.out.println("        DelCount: " + sci.getDelCount() + " Soft=" + sci.getSoftDelCount());
            System.out.println("        DelGen: Current=" + sci.getDelGen() + " Next=" + sci.getNextDelGen());
            System.out.println("        DocValuesGen: Current=" + sci.getDocValuesGen() + " Next=" + sci.getNextDocValuesGen());
            System.out.println("        FieldInfosGen: Current" + sci.getFieldInfosGen() + " Next=" + sci.getNextFieldInfosGen());
            System.out.println("        Id: " + idToString(sci.getId()));
            System.out.println("        MinVersion: " + sci.info.getMinVersion());
            System.out.println("        MaxDoc: " + sci.info.maxDoc());
            System.out.println("        Version: " + sci.info.getVersion());
            
            Collection<String> files = sci.files();
            if (files == null) {
                System.out.println("        Files: <null>");
            } else if (files.size() == 0) {
                System.out.println("        Files: <empty>");
            } else {
                System.out.println("        Files:");
                for (String file : files) {
                    System.out.println("            " + file);
                }
            }

            Map<String, String> attributes = sci.info.getAttributes();
            if (attributes == null) {
                System.out.println("        Attributes: <null>");
            } else if (attributes.size() == 0) {
                System.out.println("        Attributes: <empty>");
            } else {
                System.out.println("        Attributes:");
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    System.out.println("            " + entry.getKey() + ": " + entry.getValue());
                }
            }

            Map<String, String> diagnostics = sci.info.getDiagnostics();
            if (diagnostics == null) {
                System.out.println("        Diagnostics: <null>");
            } else if (diagnostics.size() == 0) {
                System.out.println("        Diagnostics: <empty>");
            } else {
                System.out.println("        Diagnostics:");
                for (Map.Entry<String, String> entry : diagnostics.entrySet()) {
                    System.out.println("            " + entry.getKey() + ": " + entry.getValue());
                }
            }

            Sort indexSort = sci.info.getIndexSort();
            if (indexSort == null) {
                System.out.println("        IndexSort: <null>");
            } else {
                System.out.println("        IndexSort: " + indexSort);
            }
        }

        try {
            DirectoryReader ireader = DirectoryReader.open(directory);
            try {
                ContextDumper cd = new ContextDumper(args, argOffset);
                cd.printContext(ireader.getContext(), "");
            } finally {
                ireader.close();
            }
        } finally {
            directory.close();
        }
    }

    @Override
    public String[] getUsage() {
        return new String[] {
            "",
            "Test various Lucene read APIs.",
        };
    }

    public static String idToString(byte[] id) {
        if (id == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder(id.length * 2);
        for (byte b : id) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
