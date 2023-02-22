package org.kanga.lucenetesting.command;

import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.kanga.lucenetesting.App;
import org.kanga.lucenetesting.NullFormatter;
import org.kanga.lucenetesting.TerminalBoldFormatter;

public class Query implements Command {
    public Query() {
    }

    @Override
    public String getName() {
        return "query";
    }

    @Override
    public void execute(App app, String[] args, int argOffset) throws Exception {
        if (argOffset >= args.length) {
            throw new IllegalArgumentException("No search term specified");
        }

        StringBuilder queryBuilder = new StringBuilder();
        for (int i = argOffset; i < args.length; i++) {
            if (i > argOffset) {
                queryBuilder.append(" ");
            }
            queryBuilder.append(args[i]);
        }
        String queryString = queryBuilder.toString();

        Path indexPath = Path.of(app.getIndexDirectory());
        Directory directory = FSDirectory.open(indexPath);

        Formatter formatter;
        if (!app.isForceTerminal() && System.console() == null) {
            formatter = new NullFormatter();
        } else {
            formatter = new TerminalBoldFormatter();
        }

        try {
            DirectoryReader ireader = DirectoryReader.open(directory);
            try {
                IndexSearcher isearcher = new IndexSearcher(ireader);

                Analyzer analyzer = new StandardAnalyzer();
                try {
                    QueryParser parser = new QueryParser(app.getField(), analyzer);
                    org.apache.lucene.search.Query query = parser.parse(queryString);
                    TopDocs topDocs = isearcher.search(query, 10);
                    ScoreDoc[] hits = topDocs.scoreDocs;

                    // Iterate through the results:
                    StoredFields storedFields = isearcher.storedFields();
                    for (int i = 0; i < hits.length; i++) {
                        if (i > 0) {
                            System.out.println();
                        }

                        System.out.print(
                                "Result " + (i + 1) + ": document id " + hits[i].doc + ", score " + hits[i].score);
                        Document hitDoc = storedFields.document(hits[i].doc);
                        String filename = hitDoc.get("filename");
                        if (filename != null) {
                            System.out.print(", filename " + filename);
                        }

                        for (IndexableField f: hitDoc.getFields()) {
                            String name = f.name();
                            if (name.equals("filename") || name.equals("text")) {
                                continue;
                            }

                            System.out.print(", " + name + " " + f.stringValue());
                        }

                        System.out.println();

                        String text = hitDoc.get("text");
                        if (app.getField().equals("text") && text != null) {
                            QueryScorer scorer = new QueryScorer(query, "text");
                            Highlighter highlighter = new Highlighter(formatter, scorer);
                            String fragment = highlighter.getBestFragment(analyzer, "text", text);
                            if (fragment != null) {
                                fragment.lines().forEachOrdered(line -> System.out.println("    " + line));
                            }
                        }
                    }
                } finally {
                    analyzer.close();
                }
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
            "<term>",
            "Query the index for the given term. Lucene classic query syntax is supported.",
        };
    }
}
