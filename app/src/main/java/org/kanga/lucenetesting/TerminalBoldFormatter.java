package org.kanga.lucenetesting;

import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.TokenGroup;

public class TerminalBoldFormatter implements Formatter {
    private static final String BOLD_START = "\033[1m";
    private static final String BOLD_END = "\033[0m";

    public TerminalBoldFormatter() {
    }

    @Override
    public String highlightTerm(String originalText, TokenGroup tokenGroup) {
        if (tokenGroup == null || tokenGroup.getTotalScore() <= 0.0) {
            return originalText;
        }

        StringBuilder result = new StringBuilder(originalText.length() + BOLD_START.length() + BOLD_END.length());
        result.append(BOLD_START);
        result.append(originalText);
        result.append(BOLD_END);
        return result.toString();
    }    
}
