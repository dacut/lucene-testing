package org.kanga.lucenetesting.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.kanga.lucenetesting.App;
import org.kanga.lucenetesting.InvalidUsageException;

public class AddDocument implements Command {
    public AddDocument() {
    }

    @Override
    public String getName() {
        return "add-document";
    }

    @Override
    public void execute(App app, String[] args, int argOffset) throws Exception {
        Charset utf8 = Charset.forName("UTF-8");

        if (argOffset >= args.length) {
            throw new InvalidUsageException("No document filename(s) specified");
        }

        Path indexPath = Path.of(app.getIndexDirectory());
        File indexPathAsFile = indexPath.toFile();
        if (!indexPathAsFile.exists()) {
            if (! indexPathAsFile.mkdirs()) {
                throw new RuntimeException("Failed to create directory " + indexPath);
            }
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
                        try (FileReader fr = new FileReader(args[i], utf8);
                                BufferedReader br = new BufferedReader(fr)) {

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
                            doc.add(new Field("filename", args[i], TextField.TYPE_STORED));
                            doc.add(new Field("text", documentText, TextField.TYPE_STORED));
                            iwriter.addDocument(doc);
                        } catch (FileNotFoundException e) {
                            System.err.println("Could not open file " + args[i]);
                            throw e;
                        }
                        System.out.println("Added document " + args[i]);
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
            "<filename> [<filename> ...]",
            "Add a document to the index (creating it if it does not exist).",
        };
    }
}
