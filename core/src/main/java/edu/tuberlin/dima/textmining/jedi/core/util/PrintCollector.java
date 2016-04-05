package edu.tuberlin.dima.textmining.jedi.core.util;

/**
 * Print collector, to keep track of the resolution process.
 */
public class PrintCollector {

    private StringBuilder stringBuilder;

    private final boolean printToStdout;

    public PrintCollector(boolean printToStdout) {
        this.printToStdout = printToStdout;
        stringBuilder = new StringBuilder();
    }

    public void print(String line) {

        stringBuilder.append(line);
        stringBuilder.append("\n");
        if(printToStdout) {
            System.out.println(line);
        }

    }

    public String getOutput() {
        return stringBuilder.toString();
    }
}
