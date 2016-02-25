package com.epam.search.common;

import com.martiansoftware.jsap.*;

import java.util.Optional;

public class ConsoleParser {
    public static final String GOOGLE_FLAG = "google";
    public static final String WIKI_FLAG = "wiki";
    public static final String NAME_FLAG = "name";
    public static final String FROM_FLAG = "from";
    private JSAP jsap;

    public ConsoleParser() {
        init();
    }

    private void init() {
        try {
            jsap = new JSAP();
            Switch googleOpt = new Switch(GOOGLE_FLAG)
                    .setShortFlag('g')
                    .setLongFlag(GOOGLE_FLAG);
            googleOpt.setHelp("Use google processor");

            Switch wikiOpt = new Switch(WIKI_FLAG)
                    .setShortFlag('w')
                    .setLongFlag(WIKI_FLAG);
            wikiOpt.setHelp("Use wiki processor");

            Switch helpOpt = new Switch("help")
                    .setShortFlag('h')
                    .setLongFlag("help");
            helpOpt.setHelp("Show help information");

            FlaggedOption nameOpt = new FlaggedOption(NAME_FLAG)
                    .setStringParser(JSAP.STRING_PARSER)
                    .setLongFlag(NAME_FLAG)
                    .setShortFlag('n')
                    .setRequired(false);
            nameOpt.setHelp("Use to grab info for appropriate event");

            FlaggedOption fromOpt = new FlaggedOption(FROM_FLAG)
                    .setStringParser(JSAP.INTEGER_PARSER)
                    .setLongFlag(FROM_FLAG)
                    .setShortFlag('f')
                    .setRequired(false);
            fromOpt.setHelp("Use to grab info for appropriate event");

            jsap.registerParameter(googleOpt);
            jsap.registerParameter(wikiOpt);
            jsap.registerParameter(nameOpt);
            jsap.registerParameter(helpOpt);
            jsap.registerParameter(fromOpt);
        } catch (JSAPException e) {
            System.err.println("[ConsoleParser] init error : " + e.getMessage());
        }
    }

    public ParseResult parse(String[] args) {
        JSAPResult config = jsap.parse(args);
        ParseResult result = new ParseResult();
        if (!config.success()) {

            for (java.util.Iterator errs = config.getErrorMessageIterator();
                 errs.hasNext(); ) {
                System.err.println("Error: " + errs.next());
            }
            System.err.println();

            System.err.println(jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp());
            result.setSuccess(false);
        } else {
            if (config.getBoolean("help")) {
                System.out.println(jsap.getHelp());
            } else {
                if (config.contains(GOOGLE_FLAG))
                    ProcessorConfig.setGoogleProcessorEnabler(config.getBoolean(GOOGLE_FLAG));
                if (config.contains(WIKI_FLAG))
                    ProcessorConfig.setWikiProcessorEnabler(config.getBoolean(WIKI_FLAG));
                if (config.contains(NAME_FLAG))
                    result.setName(Optional.ofNullable(config.getString(NAME_FLAG)));
                if (config.contains(FROM_FLAG))
                    result.setFrom(config.getInt(FROM_FLAG));

                result.setSuccess(true);
            }
        }
        return result;
    }

    public static class ParseResult {
        private boolean success;
        private int from;
        private Optional<String> name = Optional.empty();

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public int getFrom() {
            return from;
        }

        public void setFrom(int from) {
            this.from = from;
        }

        public Optional<String> getName() {
            return name;
        }

        public void setName(Optional<String> name) {
            this.name = name;
        }
    }
}