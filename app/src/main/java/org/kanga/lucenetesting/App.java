package org.kanga.lucenetesting;

import java.io.PrintStream;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

import javax.annotation.Nonnull;

import org.kanga.lucenetesting.command.Command;

public class App {
    @Nonnull
    private String indexDirectory;

    @Nonnull
    private String field;
    
    boolean forceTerminal;
    
    @Nonnull
    Map<String, Command> commands;

    public App() {
        this.indexDirectory = "indextest";
        this.field = "text";
        this.forceTerminal = false;
        this.commands = new TreeMap<String, Command>();
        this.discoverCommands();
    }

    public String getIndexDirectory() {
        return this.indexDirectory;
    }

    public String getField() {
        return this.field;
    }

    public boolean isForceTerminal() {
        return this.forceTerminal;
    }

    public static void main(@Nonnull String[] args) throws Exception {
        new App().run(args);
    }

    private void discoverCommands() {
        discoverCommands(ClassLoader.getSystemClassLoader());
    }

    private void discoverCommands(ClassLoader classLoader) {
        ServiceLoader<Command> serviceLoader = ServiceLoader.load(Command.class, classLoader);
        for (Command command : serviceLoader) {
            this.commands.put(command.getName(), command);
        }
    }

    @SuppressWarnings("null")
    public void run(@Nonnull String[] args) throws Exception {
        int i = 0;
        for (; i < args.length; i++) {
            if (!args[i].startsWith("-")) {
                break;
            }

            if (args[i].equals("--")) {
                i++;
                break;
            }

            switch (args[i]) {
                case "-c": // fallthough
                case "--color":
                    this.forceTerminal = true;
                    break;

                case "-h": // fallthough
                case "--help":
                    this.usage(System.out);
                    System.exit(0);

                case "-i": // fallthough
                case "--index-directory":
                    i++;
                    if (i >= args.length) {
                        System.err.println("No index directory specified");
                        this.usage();
                        System.exit(1);
                    }

                    this.indexDirectory = args[i];
                    break;

                case "-f": // fallthough
                case "--field":
                    i++;
                    if (i >= args.length) {
                        System.err.println("No field specified");
                        this.usage();
                        System.exit(1);
                    }

                    this.field = args[i];
                    break;

                default:
                    System.err.println("Unknown option " + args[i]);
                    this.usage();
                    System.exit(1);
            }
        }

        if (i >= args.length) {
            System.err.println("No subcommand specified");
            usage();
            System.exit(1);
        }

        Command c = this.commands.get(args[i]);
        if (c == null) {
            System.err.println("Unknown subcommand " + args[i]);
            this.usage();
            System.exit(1);
            return;
        }

        try {
            c.execute(this, args, i + 1);
        }
        catch (InvalidUsageException e) {
            String message = e.getMessage();
            if (message != null) {
                System.err.println(message);
            }
            this.usage();
            System.exit(1);
        }
    }

    public void usage() {
        usage(System.err);
    }

    public void usage(PrintStream out) {
        out.println("Usage: lucene-testing [options] <subcommand>");
        out.println();
        out.println("Options:");
        out.println("    -c, --color");
        out.println("        Force color output even if not writing to a terminal. (Java isn't smart about");
        out.println("        detecting whether it's connected to a terminal.)");
        out.println();
        out.println("    -h, --help");
        out.println("        Print this usage information.");
        out.println();
        out.println("    -i, --index-directory <path>");
        out.println("        Specify the index directory. Defaults to \"indextest\".");
        out.println();
        out.println("    -f, --field <field>");
        out.println(
                "        Specify the field to query. Defaults to \"text\". Valid options are \"filename\" and \"text\".");
        out.println();
        out.println("Subcommands:");
        boolean firstSubcommand = true;
        for (Map.Entry<String, Command> entry : this.commands.entrySet()) {
            if (firstSubcommand) {
                firstSubcommand = false;
            }
            else {
                out.println();
            }

            String[] usageLines = entry.getValue().getUsage();
            if (usageLines == null || usageLines.length == 0) {
                continue;
            }

            out.println("    " + entry.getKey() + " " + usageLines[0]);
            for (int i = 1; i < usageLines.length; i++) {
                out.println("        " + usageLines[i]);
            }
        }
    }
}
