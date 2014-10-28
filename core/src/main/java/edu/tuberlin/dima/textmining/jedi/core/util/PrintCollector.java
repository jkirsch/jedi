package edu.tuberlin.dima.textmining.jedi.core.util;

/**
 * Date: 11.07.2014
 * Time: 22:15
 *
 * @author Johannes Kirschnick
 */
public class PrintCollector {

    StringBuilder stringBuilder;

    final boolean printoutput;

    public PrintCollector(boolean printoutput) {
        this.printoutput = printoutput;
        stringBuilder = new StringBuilder();
    }

    public void print(String line) {

        stringBuilder.append(line);
        stringBuilder.append("\n");
        if(printoutput) {
            System.out.println(line);
        }

    }

    public String getOutput() {
        return stringBuilder.toString();
    }
}
