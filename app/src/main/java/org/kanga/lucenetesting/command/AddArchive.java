package org.kanga.lucenetesting.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.kanga.lucenetesting.App;
import org.kanga.lucenetesting.InvalidUsageException;

public class AddArchive implements Command {
    @Override
    public String getName() {
        return "add-archive";
    }

    @Override
    public void execute(App app, String[] args, int argOffset) throws Exception {
        Charset utf8 = Charset.forName("UTF-8");

        if (argOffset >= args.length) {
            throw new InvalidUsageException("No document ZIP archive(s) specified");
        }

        Path indexPath = Path.of(app.getIndexDirectory());
        File indexPathAsFile = indexPath.toFile();
        if (!indexPathAsFile.exists()) {
            indexPathAsFile.mkdirs();
            System.out.println("Created index directory " + indexPath);
        }

        Directory directory = FSDirectory.open(indexPath);
        try {
            Analyzer analyzer = new StandardAnalyzer();
            try {
                IndexWriterConfig config = new IndexWriterConfig(analyzer);
                IndexWriter iwriter = new IndexWriter(directory, config);
                try {
                    char[] buffer = new char[1 << 20];

                    for (int i = argOffset; i < args.length; i++) {
                        try (ZipFile zipFile = new ZipFile(args[i], utf8)) {
                            for (Enumeration<? extends ZipEntry> entryEnum = zipFile.entries(); entryEnum.hasMoreElements();) {
                                ZipEntry entry = entryEnum.nextElement();

                                if (entry.isDirectory() || !entry.getName().endsWith(".txt")) {
                                    continue;
                                }

                                try (InputStream is = zipFile.getInputStream(entry);
                                        InputStreamReader isr = new InputStreamReader(is, utf8);
                                        BufferedReader br = new BufferedReader(isr)) {

                                    // Read the entire document. We can't just pass the reader to Lucene
                                    // because it refuses to store the document.
                                    StringBuilder documentBuilder = new StringBuilder();

                                    while (true) {
                                        int nRead = br.read(buffer, 0, 1 << 20);
                                        if (nRead == -1) {
                                            break;
                                        }

                                        documentBuilder.append(buffer, 0, nRead);
                                    }

                                    String documentText = documentBuilder.toString();

                                    Document doc = new Document();
                                    doc.add(new Field("archive-filename", args[i], TextField.TYPE_STORED));
                                    doc.add(new Field("filename", entry.getName(), TextField.TYPE_STORED));
                                    doc.add(new Field("text", documentText, TextField.TYPE_STORED));
                                    doc.add(new StoredField("size", entry.getSize()));
                                    doc.add(new StoredField("compressed-size", entry.getCompressedSize()));

                                    String comment = entry.getComment();
                                    if (comment != null) {
                                        doc.add(new Field("comment", comment, TextField.TYPE_STORED));
                                    }

                                    addFileTimeToDoc(doc, entry.getCreationTime(), "creation-time");
                                    addFileTimeToDoc(doc, entry.getLastAccessTime(), "last-access-time");
                                    addFileTimeToDoc(doc, entry.getLastModifiedTime(), "last-modified-time");

                                    String compressionMethod = "unknown";

                                    switch (entry.getMethod()) {
                                        case ZipEntry.DEFLATED:
                                        compressionMethod = "deflated";
                                        break;

                                        case ZipEntry.STORED:
                                        compressionMethod = "stored";
                                        break;
                                    }

                                    doc.add(new Field("compression-method", compressionMethod, TextField.TYPE_STORED));

                                    iwriter.addDocument(doc);
                                    System.out.println("Added document " + entry.getName());
                                }
                            }
                        } catch (FileNotFoundException e) {
                            System.err.println("Could not open file " + args[i]);
                            throw e;
                        }
                        System.out.println("Added archive " + args[i]);
                    }
                } finally {
                    iwriter.close();
                }
            } finally {
                analyzer.close();
            }
        } finally {
            directory.close();
        }
    }

    @Override
    public String[] getUsage() {
        return new String[]{
            "<archive.zip>...",
            "Add all text documents in the archive to the index (creating it if it does not exist).",
        };
    }

    static void addFileTimeToDoc(Document doc, FileTime ft, String fieldName) {
        if (ft != null) {
            String ftStr = DateTools.timeToString(ft.toMillis(), DateTools.Resolution.SECOND);
            doc.add(new Field(fieldName, ftStr, TextField.TYPE_STORED));
        }
    }
}
