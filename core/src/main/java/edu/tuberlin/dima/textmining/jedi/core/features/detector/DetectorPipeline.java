package edu.tuberlin.dima.textmining.jedi.core.features.detector;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpDependencyParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.*;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

/**
 */
public class DetectorPipeline extends AbstractPipeline {

	@Parameter(names = {"-lang"}, description = "Language of input data", required = false)
	private String language = "en";

	@Parameter(names = {"-skipWrongLanguage"}, description = "If the text is not equal to '-lang' should it be skipped?", required = false, arity = 1)
	private Boolean skipWrongLanguage = true;

	@Parameter(names = {"-reporting"}, description = "Output performance statistics to STDOUT", required = false)
	private boolean enableReporting = false;

	@Parameter(names = {"-reportCSV"}, description = "Experimental reporting using CSV files", required = false)
	private String reportCSV = null;

	@Parameter(names = {"-annotateNER"},
		description = "annotateNER using Stanford", required = false)
	private boolean annotateNER = false;

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

			AnalysisEngineDescription parser;
					parser = createEngineDescription(
						createEngineDescription(ClearNlpDependencyParser.class,
							ClearNlpDependencyParser.PARAM_VARIANT, null,
							ClearNlpDependencyParser.PARAM_PRINT_TAGSET, true),
						createEngineDescription(StanfordParser.class,
							StanfordParser.PARAM_MODE, StanfordParser.DependenciesMode.NON_COLLAPSED,
							StanfordParser.PARAM_WRITE_CONSTITUENT, true,
							StanfordParser.PARAM_WRITE_DEPENDENCY, false,
							StanfordParser.PARAM_WRITE_PENN_TREE, false,
							StanfordParser.PARAM_WRITE_POS, false));

			AnalysisEngineDescription ner;
			if(language.equals("en")){

				ner=createEngineDescription(
					StanfordNamedEntityRecognizer.class,
					StanfordNamedEntityRecognizer.PARAM_PRINT_TAGSET, false,
					StanfordNamedEntityRecognizer.PARAM_VARIANT, "muc.7class.distsim.crf");
			}else{

				ner=createEngineDescription(
					StanfordNamedEntityRecognizer.class,
					StanfordNamedEntityRecognizer.PARAM_PRINT_TAGSET, false);
			}

			AnalysisEngineDescription aggregate;
			if(annotateNER) {
				aggregate = createEngineDescription(
					createEngineDescription(StanfordSegmenter.class),
//                                createEngineDescription(MatePosTagger.class),
//                                createEngineDescription(MateLemmatizer.class),
//                                createEngineDescription(MateParser.class),
					//        createEngineDescription(ClearNlpPosTagger.class),
					//createEngineDescription(ClearNlpLemmatizer.class),
					//createEngineDescription(ClearNlpDependencyParser.class),
					// createEngineDescription(TreeTaggerChunkerTT4J.class),
					//createEngineDescription(ExternalParserUIMA.class,
					//       ExternalParserUIMA.PARAM_PARSER_TYPE, ExternalParserUIMA.TYPE.ENJU,
					//       ExternalParserUIMA.PARAM_PARSER_LOCATION, parserLocation,
					//       ExternalParserUIMA.PARAM_MODEL_LOCATION, "none"),
					createEngineDescription(StanfordLemmatizer.class),
					createEngineDescription(StanfordPosTagger.class),
					parser,
					ner,
					createEngineDescription(StanfordCoreferenceResolver.class)
				);

			} else {
				aggregate = createEngineDescription(
					createEngineDescription(StanfordSegmenter.class),
					createEngineDescription(StanfordLemmatizer.class),
					createEngineDescription(StanfordPosTagger.class),
					parser
				);

			}



			init(
				AnalysisEngineFactory.createEngine(
					createEngineDescription(createEngineDescription(StanfordUIMAAnnotator.class),
						createEngineDescription(ClearNlpDependencyParser.class))),
				true,
				this.language,
				this.enableReporting,
				this.reportCSV,
				false);
		} catch (ResourceInitializationException e) {
			throw new IllegalArgumentException(e);
		}

	}

}
