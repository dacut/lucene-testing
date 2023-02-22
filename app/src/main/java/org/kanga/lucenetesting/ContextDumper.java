package org.kanga.lucenetesting;

import java.io.PrintStream;
import java.util.IdentityHashMap;
import java.util.List;

import org.apache.lucene.index.CompositeReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.LeafMetaData;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSetIterator;

public class ContextDumper {
    IdentityHashMap<Object, Boolean> printedContexts;
    IdentityHashMap<Object, Boolean> printedReaders;
    PrintStream out;
    String[] fields;
    int fieldsOffset;

    public ContextDumper() {
        this(System.out, null, 0);
    }

    public ContextDumper(String[] fields, int fieldsOffset) {
        this(System.out, fields, fieldsOffset);
    }

    public ContextDumper(PrintStream out, String[] fields, int fieldsOffset) {
        this.printedContexts = new IdentityHashMap<Object, Boolean>();
        this.printedReaders = new IdentityHashMap<Object, Boolean>();
        this.out = out;
        this.fields = fields;
        this.fieldsOffset = fieldsOffset;
    }

    public void printContext(IndexReaderContext context, String indent) {
        if (context == null) {
            return;
        }

        Object id = context.id();

        List<IndexReaderContext> children;
        List<LeafReaderContext> leaves;

        try {
            children = context.children();
        } catch (UnsupportedOperationException e) {
            children = null;
        }

        try {
            leaves = context.leaves();
        } catch (UnsupportedOperationException e) {
            leaves = null;
        }

        String typeName = "IndexReaderContext";

        if (context instanceof CompositeReaderContext) {
            typeName = "CompositeReaderContext";
        } else if (context instanceof LeafReaderContext) {
            typeName = "LeafReaderContext";
        }

        if (this.printedContexts.containsKey(id)) {
            out.println(indent + typeName + "(" + context.getClass().getSimpleName() + "): id=" + id.hashCode()
                    + " (already printed)");
            return;
        }
        this.printedContexts.put(id, true);

        out.print(indent + typeName + "(" + context.getClass().getSimpleName() + "): id=" + id.hashCode()
                + " docBaseInParent=" + context.docBaseInParent + " ordInParent=" + context.ordInParent + " isTopLevel="
                + context.isTopLevel);

        if (context instanceof LeafReaderContext) {
            LeafReaderContext lrc = (LeafReaderContext) context;
            out.println(" docBase=" + lrc.docBase + " ord=" + lrc.ord);
        } else {
            out.println();
        }

        if (children != null) {
            out.println(indent + "    Children: " + children.size());
            for (IndexReaderContext child : children) {
                printContext(child, indent + "        ");
            }
        } else {
            out.println(indent + "    Children: null");
        }

        if (leaves != null) {
            out.println(indent + "    Leaves: " + leaves.size());
            for (LeafReaderContext leaf : leaves) {
                printContext(leaf, indent + "        ");
            }
        } else {
            out.println(indent + "    Leaves: null");
        }

        printReader(context.reader(), indent + "    ");
    }

    public void printReader(IndexReader r, String indent) {
        if (r == null) {
            out.println(indent + "IndexReader: null");
            return;
        }

        String typeName;
        LeafReader lr = null;

        if (r instanceof LeafReader) {
            lr = (LeafReader) r;
            typeName = "LeafReader";
        } else {
            typeName = "IndexReader";
        }

        if (this.printedReaders.containsKey(r)) {
            out.println(indent + typeName + "(" + r.getClass().getSimpleName() + "): (already printed)");
            return;
        }

        this.printedReaders.put(r, true);

        out.print(indent + typeName + "(" + r.getClass().getSimpleName() + "): maxdoc=" + r.maxDoc() + " numdocs="
                + r.numDocs() + " deletions=" + r.numDeletedDocs());

        if (lr != null) {
            LeafMetaData lmd = lr.getMetaData();

            out.println(" createdMajorVersion=" + lmd.getCreatedVersionMajor() + " minVersion=" + lmd.getMinVersion());
        } else {
            out.println();
        }

        if (this.fields != null) {
            for (int i = this.fieldsOffset; i < this.fields.length; i++) {
                String origField = this.fields[i];
                String field, value;
                Term term;

                int eqPos = origField.indexOf('=');

                if (eqPos != -1) {
                    value = origField.substring(eqPos + 1);
                    field = origField.substring(0, eqPos);
                    term = new Term(field, value);
                } else {
                    term = new Term(origField);
                    field = origField;
                }

                String docFreq, docCount, sumDocFreq, sumTotalTermFreq;

                try {
                    docFreq = Integer.toString(r.docFreq(term));
                } catch (Exception e) {
                    docFreq = "<" + e.toString() + ">";
                }

                try {
                    docCount = Integer.toString(r.getDocCount(field));
                } catch (Exception e) {
                    docCount = "<" + e.toString() + ">";
                }

                try {
                    sumDocFreq = Long.toString(r.getSumDocFreq(field));
                } catch (Exception e) {
                    sumDocFreq = "<" + e.toString() + ">";
                }

                try {
                    sumTotalTermFreq = Long.toString(r.getSumTotalTermFreq(field));
                } catch (Exception e) {
                    sumTotalTermFreq = "<" + e.toString() + ">";
                }

                out.println(indent + "    Field " + origField + ": docFreq=" + docFreq + " docCount=" + docCount
                        + " sumDocFreq=" + sumDocFreq + " sumTotalTermFreq=" + sumTotalTermFreq);

                if (lr != null) {
                    try {
                        PostingsEnum pe = lr.postings(term, PostingsEnum.ALL);
                        if (pe != null) {
                            for (int docId = pe.nextDoc(); docId != DocIdSetIterator.NO_MORE_DOCS; docId = pe
                                    .nextDoc()) {
                                out.println(indent + "        Postings: docId=" + docId + " freq=" + pe.freq());
                            }
                        }
                    } catch (Exception e) {
                        out.println(indent + "        Postings: <" + e.toString() + ">");
                    }

                    try {
                        NumericDocValues ndv = lr.getNumericDocValues(field);
                        if (ndv != null) {
                            for (int docId = ndv.nextDoc(); docId != DocIdSetIterator.NO_MORE_DOCS; docId = ndv
                                    .nextDoc()) {
                                out.println(indent + "        NumericDocValues: docId=" + docId + " value="
                                        + ndv.longValue());
                            }
                        }
                    } catch (Exception e) {
                        out.println(indent + "        NumericDocValues: <" + e.toString() + ">");
                    }
                }
            }
        }
    }
}
