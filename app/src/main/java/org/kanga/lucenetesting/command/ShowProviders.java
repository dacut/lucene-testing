package org.kanga.lucenetesting.command;

import org.kanga.lucenetesting.App;

public class ShowProviders implements Command {
    public ShowProviders() {
    }

    @Override
    public String getName() {
        return "show-providers";
    }

    @Override
    public void execute(App app, String[] args, int argOffset) throws Exception {
        System.out.println("Codecs:");
        for (String provider: org.apache.lucene.codecs.Codec.availableCodecs()) {
            System.out.println("    " + provider);
        }

        System.out.println("\nDocValuesFormat:");
        for (String provider: org.apache.lucene.codecs.DocValuesFormat.availableDocValuesFormats()) {
            System.out.println("    " + provider);
        }

        // System.out.println("\nKnnVectorsFormat:");
        // for (String provider: org.apache.lucene.codecs.KnnVectorsFormat.availableKnnVectorsFormats()) {
        //     System.out.println("    " + provider);
        // }

        System.out.println("\nPostingsFormat:");
        for (String provider: org.apache.lucene.codecs.PostingsFormat.availablePostingsFormats()) {
            System.out.println("    " + provider);
        }

        System.out.println("\nSortField providers:");
        for (String provider: org.apache.lucene.index.SortFieldProvider.availableSortFieldProviders()) {
            System.out.println("    " + provider);
        }
    }

    @Override
    public String[] getUsage() {
        return new String[] {
            "",
            "Show the available Lucene providers for various components.",
        };
    }
}
