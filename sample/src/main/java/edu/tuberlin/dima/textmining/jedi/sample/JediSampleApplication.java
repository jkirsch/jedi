package edu.tuberlin.dima.textmining.jedi.sample;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import edu.tuberlin.dima.textmining.jedi.core.JediService;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.AbstractShortestPathFeatureExtractor;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.AllPairsShortestPathFeatureExtractor;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.AnnotationPipeline;
import edu.tuberlin.dima.textmining.jedi.core.model.RelationDetectionResults;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Sample Application.
 */
public class JediSampleApplication {

	public static void main(String[] args) throws Throwable {

		AnnotationPipeline annotationPipeline =
			AnnotationPipeline.withOptions("-lang en");

		AbstractShortestPathFeatureExtractor featureExtractor
			= new AllPairsShortestPathFeatureExtractor(
				" -lemmatize " +
				" -resolveCoreferences " +
				" -selectionType " + N.class.getName() +
				" -additionalSelectionType " + ADJ.class.getName() +
				" -name all");


		// initialize detection service with defaults
		JediService jediService = new JediService(annotationPipeline, featureExtractor);

		String sentence = "Bill Gothard received his B.A. in Biblical Studies from Wheaton College in 1957.";

		// execute relation detection
		RelationDetectionResults<Annotation> relations = jediService.detectRelations(sentence);

		System.out.println("\n ------------ Input Sentence     ------------ \n");
		System.out.println(sentence);
		System.out.println("\n ------------ Detected Relations ------------ \n");

		String tableString = relations.generateReadableTableString();

		System.out.println(tableString);



	}
}
