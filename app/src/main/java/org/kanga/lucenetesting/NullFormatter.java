package org.kanga.lucenetesting;

import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.TokenGroup;

public class NullFormatter implements Formatter {
    public NullFormatter() {
    }

    @Override
    public String highlightTerm(String originalText, TokenGroup tokenGroup) {
        return originalText;
    }
}
