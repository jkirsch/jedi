package edu.tuberlin.dima.textmining.jedi.sample;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import edu.tuberlin.dima.textmining.jedi.core.JediService;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.AllPairsShortestPathFeatureExtractor;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.DetectorPipeline;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.model.Answer;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Sample Application.
 */
public class JediSampleApplication {

	public static void main(String[] args) throws Throwable {

		DetectorPipeline detectorPipeline = new DetectorPipeline(
			"-annotateNER -skipWrongLanguage false -lang en -testMode");
		AllPairsShortestPathFeatureExtractor featureExtractor
			= new AllPairsShortestPathFeatureExtractor(
					    "-lemmatize " +
						"-resolveCoreferences " +
						" -selectionType " + N.class.getName() +
						" -additionalSelectionType " + ADJ.class.getName() +
						" -name all");


		// initialize detection service with defaults
		JediService jediService = new JediService(detectorPipeline, featureExtractor);

		String sentence = "Bill Gothard received his B.A. in Biblical Studies from Wheaton College in 1957.";

		// execute relation detection
		Answer<Annotation> annotationAnswer = jediService.analyzeText(sentence);

		System.out.println("\n ------------ Input Sentence     ------------ \n");
		System.out.println(sentence);
		System.out.println("\n ------------ Detected Relations ------------ \n");

		String tableString = annotationAnswer.generateReadableTableString();

		System.out.println(tableString);



	}
}
