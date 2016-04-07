package edu.tuberlin.dima.textmining.jedi.core;

import edu.tuberlin.dima.textmining.jedi.core.config.DetectorService;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.AbstractShortestPathFeatureExtractor;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.TextAnnotationPipeline;
import edu.tuberlin.dima.textmining.jedi.core.index.FreebaseTypeService;
import edu.tuberlin.dima.textmining.jedi.core.index.PatternIndexer;
import edu.tuberlin.dima.textmining.jedi.core.model.RelationDetectionResults;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Jedi relation detection service, with defaults.
 */
public class JediService {

	private static final Logger LOG = LoggerFactory.getLogger(JediService.class);

	private final DetectorService service;
	private final AbstractShortestPathFeatureExtractor extractor;

	/**
	 * Initializes Jedi using defaults.
	 *
	 * @param annotationPipeline the parser to use
	 * @param extractor          the feature extractor to use
	 */
	public JediService(TextAnnotationPipeline annotationPipeline, AbstractShortestPathFeatureExtractor extractor) throws Exception {

		LOG.info("Initializing JEDI using defaults");

		this.extractor = extractor;

		// setup services
		FreebaseTypeService freebaseTypeService = new FreebaseTypeService();
		PatternIndexer patternIndexer = new PatternIndexer();

		this.service = new DetectorService(annotationPipeline, freebaseTypeService, patternIndexer);
		this.service.init();

	}

	public RelationDetectionResults<Annotation> detectRelations(String text) throws InterruptedException, SAXException, ExecutionException, IOException {

		return service.detectRelations(text, extractor, true);
	}


}
