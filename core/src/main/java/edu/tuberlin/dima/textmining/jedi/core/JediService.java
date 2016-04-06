package edu.tuberlin.dima.textmining.jedi.core;

import edu.tuberlin.dima.textmining.jedi.core.config.DetectorService;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.AbstractShortestPathFeatureExtractor;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.DetectorPipeline;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.model.Answer;
import edu.tuberlin.dima.textmining.jedi.core.index.FreebaseTypeService;
import edu.tuberlin.dima.textmining.jedi.core.index.PatternIndexer;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Sample without spring.
 */
public class JediService {

	private static final Logger LOG = LoggerFactory.getLogger(JediService.class);

	private final DetectorService service;
	private final AbstractShortestPathFeatureExtractor extractor;

	/**
	 * Initializes Jedi using defaults.
	 *
	 * @param detectorPipeline the parser to use
	 * @param extractor        the feature extractor to use
	 */
	public JediService(DetectorPipeline detectorPipeline, AbstractShortestPathFeatureExtractor extractor) throws Exception {

		LOG.info("Initializing JEDI using defaults");

		this.extractor = extractor;

		// setup services
		FreebaseTypeService freebaseTypeService = new FreebaseTypeService();
		PatternIndexer patternIndexer = new PatternIndexer();

		this.service = new DetectorService(detectorPipeline, freebaseTypeService, patternIndexer);
		this.service.init();

	}

	public Answer<Annotation> detectRelations(String text) throws InterruptedException, SAXException, ExecutionException, IOException {

		return service.detectRelations(text, extractor, true);
	}


}
