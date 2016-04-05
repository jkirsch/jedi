package edu.tuberlin.dima.textmining.jedi.core.config;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NP;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.AbstractShortestPathFeatureExtractor;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.AllPairsShortestPathFeatureExtractor;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.DetectorPipeline;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.FindShortestPathFeatureExtractor;
import edu.tuberlin.dima.textmining.jedi.core.index.FreebaseTypeService;
import edu.tuberlin.dima.textmining.jedi.core.index.PatternIndexer;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 */
@Configuration
public class JediConfig {

	@Value("${detector.warmup:FALSE}")
	public boolean warmup;

	@Bean
	public DetectorPipeline createDetectorPipeline() throws Throwable {
		return new DetectorPipeline("-annotateNER -skipWrongLanguage false -lang en -testMode" + (warmup ? " -warmup" : ""));
	}

	@Bean(name = "NamedEntityFeatureDetector")
	public AbstractShortestPathFeatureExtractor createNamedEntityFeatureDetector() throws UIMAException {

		return new FindShortestPathFeatureExtractor("-lemmatize -resolveCoreferences -collapseMentions -selectionType " + NamedEntity.class.getName());
	}

	@Bean(name = "NounFeatureDetector")
	public AbstractShortestPathFeatureExtractor createNounFeatureDetector() throws UIMAException {

		return new FindShortestPathFeatureExtractor("-lemmatize -resolveCoreferences  -pickupSimilar -selectionType " + N.class.getName() + " -additionalSelectionType " + PR.class.getName());
	}

	@Bean(name = "NounPhaseFeatureDetector")
	public AbstractShortestPathFeatureExtractor createNounPhaseFeatureDetector() throws UIMAException {

		return new FindShortestPathFeatureExtractor("-lemmatize -resolveCoreferences -collapseMentions -pickupSimilar -selectionType " + NP.class.getName());
	}

	@Bean(name = "NounPRFeatureDetector")
	public AbstractShortestPathFeatureExtractor createNounPRFeatureDetector() throws UIMAException {
		return new FindShortestPathFeatureExtractor("-lemmatize -collapseMentions -selectionType " + N.class.getName());
	}

	@Bean(name = "AllPairsFeatureDetector")
	public AbstractShortestPathFeatureExtractor createAllPairDetector() throws UIMAException {
		return new AllPairsShortestPathFeatureExtractor("-lemmatize -resolveCoreferences -selectionType " + N.class.getName() + " -additionalSelectionType " + ADJ.class.getName());
	}

	@Bean
	public JCas createJCas() throws UIMAException {
		return JCasFactory.createJCas();
	}

	@Bean
	public CacheManager cacheManager() {
		// configure and return an implementation of Spring's CacheManager SPI
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(
			new ConcurrentMapCache("default"),
			new ConcurrentMapCache("features"),
			new ConcurrentMapCache("lucene"),
			new ConcurrentMapCache("parse")
		));
		return cacheManager;
	}

	@Value("${index.directory}")
	public File indexDirectory;

	@Value("${index.relationTypes}")
	public URL relationTypes;

	@Value("${index.relationHierarchyFile}")
	public URL relationHierarchyFile;

	@Value("${index.relationInverseFile}")
	public URL relationInverseFile;

	@Value("${freebase.normalizedTypesFile}")
	public URL normalizedTypesFile;

	@Bean
	public PatternIndexer featureIndexer() throws IOException {
		return new PatternIndexer(true, indexDirectory, freebaseTypeService());
	}

	@Bean
	public FreebaseTypeService freebaseTypeService() throws IOException {
		return new FreebaseTypeService(relationTypes, relationHierarchyFile, relationInverseFile, normalizedTypesFile);
	}

}
