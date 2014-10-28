package edu.tuberlin.dima.textmining.jedi.core.features.detector;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpDependencyParser;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

/**
 * Date: 22.03.13
 * Time: 14:19
 *
 * @author Johannes Kirschnick
 */
public class DetectorPipeline extends AbstractPipeline {

    @Parameter(names = {"-lang"}, description = "Language of input data", required = false)
    private String language = "en";

    @Parameter(names = {"-reporting"}, description = "Output performance statistics to STDOUT", required = false)
    private boolean enableReporting = false;

    @Parameter(names = {"-reportCSV"}, description = "Experimental reporting using CSV files", required = false)
    private String reportCSV = null;


    public DetectorPipeline() throws Throwable {
        this(""); // no options
    }

    public DetectorPipeline(String options) throws Throwable {
        JCommander jCommander = new JCommander(this);
        try {
            // parse options
            jCommander.parse(options.split(" "));


        } catch (ParameterException e) {
            StringBuilder out = new StringBuilder();
            jCommander.setProgramName(this.getClass().getSimpleName());
            jCommander.usage(out);
            // We wrap this exception in a Runtime exception so that
            // existing loaders that extend PigStorage don't break
            throw new RuntimeException(e.getMessage() + "\n" + "In: " + options + "\n" + out.toString());
        } catch (Exception e) {
            throw new RuntimeException("Error initializing GermanPipeline", e);
        }


        try {

            init(
            	AnalysisEngineFactory.createEngine(
                        createEngineDescription(createEngineDescription(StanfordUIMAAnnotator.class),
                        createEngineDescription(ClearNlpDependencyParser.class))),
            	true, 
            	this.language, 
            	this.enableReporting, 
            	this.reportCSV,
            	false);
        } catch (UIMAException e) {
            throw new IllegalArgumentException(e);
        }

    }

}
