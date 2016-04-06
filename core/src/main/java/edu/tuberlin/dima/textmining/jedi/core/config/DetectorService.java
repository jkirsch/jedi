package edu.tuberlin.dima.textmining.jedi.core.config;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import com.google.common.io.Resources;
import com.google.common.primitives.Floats;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.tuberlin.dima.textmining.jedi.core.features.ConstraintSolver;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.*;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.model.Answer;
import edu.tuberlin.dima.textmining.jedi.core.index.FreebaseTypeService;
import edu.tuberlin.dima.textmining.jedi.core.index.PatternIndexer;
import edu.tuberlin.dima.textmining.jedi.core.model.Edge;
import edu.tuberlin.dima.textmining.jedi.core.model.FreebaseRelation;
import edu.tuberlin.dima.textmining.jedi.core.model.Graph;
import edu.tuberlin.dima.textmining.jedi.core.model.Solution;
import edu.tuberlin.dima.textmining.jedi.core.util.AnnovisTransformerWriter;
import edu.tuberlin.dima.textmining.jedi.core.util.PrintCollector;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 */
@Service
public class DetectorService {

	@Autowired
	@Qualifier("NounFeatureDetector")
	AbstractShortestPathFeatureExtractor nounFeatureDetector;

	@Autowired
	@Qualifier("NamedEntityFeatureDetector")
	AbstractShortestPathFeatureExtractor namedEntityFeatureDetector;

	@Autowired
	@Qualifier("NounPhraseFeatureDetector")
	AbstractShortestPathFeatureExtractor NounPhraseFeatureDetector;

	@Autowired
	@Qualifier("NounPRFeatureDetector")
	AbstractShortestPathFeatureExtractor nounPRFeatureDetector;

	@Autowired
	@Qualifier("AllPairsFeatureDetector")
	AbstractShortestPathFeatureExtractor allPairsFeatureDetector;

	DetectorPipeline detectorPipeline;

	PatternIndexer featureIndexer;

	FreebaseTypeService freebaseTypeService;

	@Value("${experiment.maxEntropy:4}")
	float maxEntropy;

	@Value("${detector.blacklistEntitiesFile}")
	URL blacklistEntitiesFile;

	private Set<String> blacklistEntities;

	private static final Logger LOG = LoggerFactory.getLogger(DetectorService.class);

	@Autowired
	public DetectorService(DetectorPipeline detectorPipeline,
						   FreebaseTypeService freebaseTypeService,
						   PatternIndexer featureIndexer) throws Exception {

		this.detectorPipeline = detectorPipeline;
		this.featureIndexer = featureIndexer;
		this.freebaseTypeService = freebaseTypeService;
		this.blacklistEntitiesFile = Resources.getResource("freepal/non-entities.txt");
		this.maxEntropy = 4;
	}

	@PostConstruct
	public void init() throws Exception {
		// parse the non entities
		List<String> readLines = Resources.readLines(blacklistEntitiesFile, Charsets.UTF_8);
		blacklistEntities = Sets.newHashSet();

		for (String nonEntity : readLines) {
			blacklistEntities.add(nonEntity.trim());
		}

	}

	private final class SearchTask<T> implements Callable<SearchResult<T>> {

		FoundFeature<T> tuple;
		PatternIndexer featureIndexer;

		public SearchTask(FoundFeature<T> tuple, PatternIndexer featureIndexer) {
			this.tuple = tuple;
			this.featureIndexer = featureIndexer;
		}

		@Override
		public SearchResult<T> call() throws Exception {
			final String patternFeature = tuple.getPattern();
			return new SearchResult<>(tuple, featureIndexer.search(patternFeature, maxEntropy));
		}
	}

	private List<Solution<Annotation>> computeMaximumLikelihood(List<SearchResult<Annotation>> searchResults) throws ExecutionException, InterruptedException {

		List<Solution<Annotation>> solutions = Lists.newArrayList();

		int edgecounter = 0;
		for (SearchResult<Annotation> result : searchResults) {

			final PatternIndexer.PatternSearchResult search = result.getPatternSearchResult();

			if (search != null && search.getCounts() > 0 && search.getEntropy() < maxEntropy) {

				// find the highest count
				final ImmutableSortedSet<PatternIndexer.PatternSearchResult.SubRelation> relations = ImmutableSortedSet.copyOf(new Ordering<PatternIndexer.PatternSearchResult.SubRelation>() {
					@Override
					public int compare(@Nullable PatternIndexer.PatternSearchResult.SubRelation left, @Nullable PatternIndexer.PatternSearchResult.SubRelation right) {
						return Floats.compare(left.getCount(), right.getCount());
					}
				}.reverse(), search.getRelationCount());

				final PatternIndexer.PatternSearchResult.SubRelation topRelation = Iterables.getFirst(relations, null);

				final float score = topRelation.getCount() / (float) search.getCounts();

				final FreebaseRelation types = freebaseTypeService.getTypes(topRelation.getRelation());

				solutions.add(new Solution<>(
					result.getTuple().getEntity1(),
					result.getTuple().getEntity2(),
					new Edge(++edgecounter, topRelation.getRelation(), result.getTuple().getPattern(), types.getDomain(), types.getRange(), score, search.getEntropy(), search.getCounts(), score > 0.9)));
				search.getToprelation();
			}
		}

		return solutions;

	}

	private List<Solution<Annotation>> solveConstraints(List<SearchResult<Annotation>> searchResults, PrintCollector printCollector) throws ExecutionException, InterruptedException {

		ConstraintSolver.ConstraintSolverBuilder<Annotation> solverBuilder = new ConstraintSolver.ConstraintSolverBuilder<>();

		for (SearchResult<Annotation> result : searchResults) {

			FoundFeature<Annotation> tuple = result.getTuple();
			final PatternIndexer.PatternSearchResult search = result.getPatternSearchResult();

			final String patternFeature = tuple.getPattern();

			if (search == null) continue;
			/** Heuristic .. if we have too many many connections, this would blow up the search space
			 If this is the case, don't look at the type hierarchy */
			final boolean sizeOfResolutionGraphTooLarge = search.getRelationCount().size() > 30;

			PatternIndexer.PatternSearchResult.SubRelation first = Iterables.getFirst(search.getRelationCount(), null);
			double highestScore = 0;
			if (first != null) {
				highestScore = first.getCount() / (float) search.getCounts();
			}

			for (PatternIndexer.PatternSearchResult.SubRelation subRelation : search.getRelationCount()) {
				tuple.setRelation(subRelation.getRelation());
				final FreebaseRelation mainRelation = freebaseTypeService.getTypes(subRelation.getRelation());
				double score = subRelation.getCount() / (float) search.getCounts();
				if (search.getEntropy() < maxEntropy) {//&& score > 0.01f) {

					if (sizeOfResolutionGraphTooLarge && (search.getEntropy() > 3.64f) /*&& (score < 0.01f)/* (subRelation.getCount() < 18)*/)
						continue;

					if (mainRelation == null) continue;

					// what about type inheritance .. add a new edge here as well for all mainRelation of 1 and 2
					// Build the Type Hierarchy out
					final ImmutableSet.Builder<String> toAdder = new ImmutableSet.Builder<String>().add(mainRelation.getRange());
					final ImmutableSet.Builder<String> adder = new ImmutableSet.Builder<String>().add(mainRelation.getDomain());

					// do not look into subtypes of relations that target location -> location
					if (!(mainRelation.getDomain().equals("/location") && mainRelation.getRange().equals("/location"))) {
						if (mainRelation.getRangeTypes() != null) {
							if (!sizeOfResolutionGraphTooLarge) {
								toAdder.addAll(mainRelation.getRangeTypes());
							}
						}

						if (mainRelation.getDomainTypes() != null) {
							if (!sizeOfResolutionGraphTooLarge) {
								adder.addAll(mainRelation.getDomainTypes());
							}

						}
					}

					final String output = Joiner.on("\t").useForNull("NULL").join(tuple, mainRelation, subRelation.getCount(), search.getCounts(), search.getEntropy());
					printCollector.print(output);

					final ImmutableSet<String> rangeTypes = toAdder.build();
					final ImmutableSet<String> domainTypes = adder.build();

					final Set<List<String>> lists = Sets.cartesianProduct(domainTypes, rangeTypes);

					for (List<String> list : lists) {
						// size of list is 2
						// repeat for each entry
						// TODO weight it differently?

						// discount type mismatches by 10%
						// so if we use a subtypes for range or domain it's still okay, but the original mainRelation are better

						// also incorporate the entropy .. if it is high -> bad . low good

						boolean mainType = list.get(0).equals(mainRelation.getRange()) && list.get(1).equals(mainRelation.getDomain());

						//if(score > 0.9) {
						// score = score * 1.5f;
						//}

						double scoreToUse = mainType ? score : score * 0.9f;
						// weigh pattern that have been observed way more than the others higher
						//scoreToUse = scoreToUse * ((6-search.getEntropy())/6);
						//scoreToUse = (float) (scoreToUse * (Math.tanh(search.getCounts()/ (8*Math.PI))));// * 1/search.getEntropy();

						solverBuilder.add(
							tuple.getEntity1(),
							tuple.getEntity2(),
							patternFeature,
							subRelation.getRelation(),
							list.get(0), // Domain
							list.get(1), // Range
							scoreToUse,
							search.getEntropy(),
							search.getCounts(),
							((highestScore > 0.9 && search.getEntropy() > 0.01 && search.getCounts() > 20)
								// || (highestScore > 0.49 && search.getCounts() > 4500)
								//|| (highestScore > 0.64 && Math.abs(search.getEntropy() - 1) < 0.05)
							)
						);
						// fix pattern if they are very likely and have
						// been observed with some degree of flexibility (entropy > 0.0)
						// or less likely but very common

					}

				}
			}


		}

		return solverBuilder.build().solve(printCollector);

	}

	private String typeNormalizer(NamedEntity namedEntity) {
		switch (namedEntity.getClass().getName()) {
			case "de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location":
				return "/location/location";
			case "de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization":
				return "/organization";
		}

		return "/" + StringUtils.lowerCase(StringUtils.substringAfterLast(namedEntity.getClass().getName(), "."), Locale.ENGLISH);
	}

	public Answer<Annotation> detectFeatures(String text,
											 AbstractShortestPathFeatureExtractor detector,
											 boolean resolveConstraints) throws IOException, SAXException, InterruptedException, ExecutionException {

		Stopwatch stopwatch = new Stopwatch().start();
		final JCas jCas = detectorPipeline.exec(Lists.newArrayList(text, "id"));
		LOG.info("Parsed Text in {}", stopwatch.stop().toString());

		final Map<String, AnnovisTransformerWriter.Annovis> annovisMap = AnnovisTransformerWriter.generateFormat(jCas);

		List<FoundFeature<Annotation>> tuples = detector.exec(jCas);

		LOG.info("Generated Features using " + detector);

		if (tuples == null) return null;

		// now filter
		Iterator<FoundFeature<Annotation>> iterator = tuples.iterator();
		while (iterator.hasNext()) {
			FoundFeature<Annotation> foundFeature = iterator.next();
			if (blacklistEntities.contains(foundFeature.getEntity1().getCoveredText()) ||
				blacklistEntities.contains(foundFeature.getEntity2().getCoveredText())) {
				iterator.remove();
			}
		}


		PrintCollector printCollector = new PrintCollector(false);

		// search in parallel

		List<SearchResult<Annotation>> candidates = tuples.parallelStream()
			.map(foundFeature -> new SearchResult<>(foundFeature, featureIndexer.search(foundFeature.getPattern(), maxEntropy)))
			.filter(input -> input != null && input.getPatternSearchResult() != null && input.getPatternSearchResult().getCounts() > 0)
			.collect(Collectors.toList());

		// what about equality ?
		// [X] be [Y] [1-attr-2,1-nsubj-0] .. this indicated a missing link ?

		// we now have for each entity pair a list of potential pattern
		List<Solution<Annotation>> solutionList;
		if (resolveConstraints) {
			LOG.info("Solving constraints ...");
			List<Solution<Annotation>> solutions = solveConstraints(candidates, printCollector);
			solutionList = consistencyCheck(solutions);
		} else {
			LOG.info("Take maximum likelihood approach ...");
			List<Solution<Annotation>> solutions = computeMaximumLikelihood(candidates);
			solutionList = consistencyCheck(solutions);
		}

		// add solution to annovis
		final AnnovisTransformerWriter.Annovis entities = new AnnovisTransformerWriter.Annovis(AnnovisTransformerWriter.AnnovisType.span);
		final AnnovisTransformerWriter.Annovis relations = new AnnovisTransformerWriter.Annovis(AnnovisTransformerWriter.AnnovisType.link);
		Map<Integer, AnnovisTransformerWriter.AnnovisValue> annotations = Maps.newHashMap();
		for (Solution<Annotation> solution : solutionList) {
			// add annotations to the list - avoiding any duplicates
			final Annotation left = solution.getLeft();
			final Annotation right = solution.getRight();

			final String domain = StringUtils.capitalize(StringUtils.substringAfterLast(solution.getEdge().getDomain(), "/"));
			final String range = StringUtils.capitalize(StringUtils.substringAfterLast(solution.getEdge().getRange(), "/"));

			annotations.put(left.hashCode(), new AnnovisTransformerWriter.AnnovisValue(left.hashCode(), domain, Lists.newArrayList(left.getBegin(), left.getEnd())));
			annotations.put(right.hashCode(), new AnnovisTransformerWriter.AnnovisValue(right.hashCode(), range, Lists.newArrayList(right.getBegin(), right.getEnd())));

			final String relation = StringUtils.substringAfterLast(solution.getEdge().getRelation(), ".");

			relations.getValues().add(new AnnovisTransformerWriter.AnnovisValue(null, relation, Lists.newArrayList(left.hashCode(), right.hashCode())));

		}
		entities.getValues().addAll(annotations.values());
		annovisMap.put("layer-1", entities);
		annovisMap.put("layer-2", relations);

		return new Answer<>(candidates, solutionList, detector.getName(), Graph.transform(solutionList), printCollector.getOutput(), annovisMap);
	}

	public Answer<Annotation> detectFeatures(String text, DetectorType detectorType, boolean resolveConstraints) throws IOException, SAXException, InterruptedException, ExecutionException {

		AbstractShortestPathFeatureExtractor detector;

		switch (detectorType) {
			case NAMED_ENTITIES:
				detector = namedEntityFeatureDetector;
				break;
			case NOUNS:
				detector = nounFeatureDetector;
				break;
			case NOUN_PHRASE:
				detector = NounPhraseFeatureDetector;
				break;
			case NOUN_PR:
				detector = nounPRFeatureDetector;
				break;
			case ALLPairs:
				detector = allPairsFeatureDetector;
				break;
			default:
				throw new IllegalArgumentException(detectorType.name() + " not set");
		}

		return detectFeatures(text, detector, resolveConstraints);

	}

	/**
	 * ordering is based on:
	 * <ul>
	 * <li>first the edge score ..
	 * <li>followed by the hashcode
	 * </ul>
	 */
	private Ordering<Solution<Annotation>> edgeByScoreOrdering = Ordering.from(new Comparator<Solution<Annotation>>() {
		@Override
		public int compare(Solution<Annotation> o1, Solution<Annotation> o2) {
			return ComparisonChain.
				start()
				.compare(o1.getEdge().getScore(), o2.getEdge().getScore()) // first score on edge score
				.compare(o2.getRight().getBegin(), o1.getRight().getBegin()) // followed by position (flipped as we sort in reverse)
				.compare(o1.hashCode(), o2.hashCode()).result();
		}
	}).reverse();

	private List<Solution<Annotation>> consistencyCheck(List<Solution<Annotation>> bestSolution) {
		if (bestSolution == null) return null;

		Table<Annotation, String, Multiset<Solution<Annotation>>> table = HashBasedTable.create();

		for (Solution<Annotation> annotationSolution : bestSolution) {
			// relation is scoped per entity
			Multiset<Solution<Annotation>> solutions = table.get(annotationSolution.getLeft(), annotationSolution.getEdge().getRelation());
			if (solutions == null) {
				solutions = TreeMultiset.create(edgeByScoreOrdering);
				table.put(annotationSolution.getLeft(), annotationSolution.getEdge().getRelation(), solutions);
			}
			solutions.add(annotationSolution);
		}
		// make sure that we only have 1 link of type
		// ns:people.person.place_of_birth
		// ns:people.person.place_of_death
		// and the reverse ones

		// TODO reverse pruning
		String[] singletonRelations = {"ns:people.person.place_of_birth", "ns:people.deceased_person.place_of_death"};

		for (String singletonRelation : singletonRelations) {

			// iterate over all rows
			for (Annotation row : table.rowKeySet()) {

				Multiset<Solution<Annotation>> solutions = table.get(row, singletonRelation);

				if (solutions != null && solutions.size() > 1) {
					// remove the ones with the least confidence .. all after the first as already sorted
					// Now also check if this maybe indicates that two things are joined
					Solution<Annotation> remains = Iterables.getFirst(solutions, null);
					Iterable<Solution<Annotation>> toDelete = Iterables.skip(solutions, 1);
					for (Solution<Annotation> annotationSolution : toDelete) {

						// check if we need to fold that into the left
						List<Token> tokens = JCasUtil.selectFollowing(Token.class, remains.getRight(), 2);
						if (tokens != null && tokens.size() == 2) {
							Token tokenAfterComma = tokens.get(1);
							Annotation right = annotationSolution.getRight();
							if (tokens.get(0).getCoveredText().equals(",") && right.getBegin() == tokenAfterComma.getBegin() && right.getEnd() == tokenAfterComma.getEnd()) {
								// fold remains
								remains.getRight().setEnd(right.getEnd());
							}
						}
						bestSolution.remove(annotationSolution);
					}
				}

			}
		}
		return bestSolution;

	}


}
