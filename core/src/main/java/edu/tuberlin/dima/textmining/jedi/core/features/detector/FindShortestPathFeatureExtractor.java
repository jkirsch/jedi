package edu.tuberlin.dima.textmining.jedi.core.features.detector;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.google.common.primitives.Ints;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.*;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.tuberlin.dima.textmining.jedi.core.util.StringComparision;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.tcas.Annotation;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A Function that takes a JCas as input and extracts
 * its annotations in terms of the shortest path between
 * all recognized entities.
 */

public class FindShortestPathFeatureExtractor extends AbstractShortestPathFeatureExtractor {


	public FindShortestPathFeatureExtractor(String options) throws UIMAException {
		super(options);
	}


	enum Counters {
		FEATURES
	}

	private static class EntityPair {

		private Annotation entity1;
		private Annotation entity2;

		public Annotation getEntity1() {
			return entity1;
		}

		public Annotation getEntity2() {
			return entity2;
		}

		private EntityPair(Annotation entity1, Annotation entity2) {
			this.entity1 = entity1;
			this.entity2 = entity2;
		}
	}

	/**
	 * Extract the shortest paths along the dependency parse
	 * between every two named entities recognized.
	 *
	 * @param graph
	 * @return DataBag dataBag     a data bag of ( entity_pair, pattern ) tuples
	 * with the shortest path as pattern
	 */
	@Override
	public List<FoundFeature<Annotation>> getShortestPaths(final UndirectedGraph<Token, DependencyEdge> graph) {

		List<FoundFeature<Annotation>> dataBag = Lists.newArrayList();

		List<Annotation> namedEntities = Lists.newArrayList(JCasUtil.select(getjCas(), getSelectionType()));
		if (getAdditionalSelectionType() != null) {
			// add additional selection types
			namedEntities.addAll(JCasUtil.select(getjCas(), getAdditionalSelectionType()));
		}
		// remove NNS  & PRP
		final Iterator<Annotation> iterator = namedEntities.iterator();
		while (iterator.hasNext()) {
			final Annotation next = iterator.next();
			if (next instanceof NN) {
				NN c = (NN) next;
				if (c.getPosValue().equals("NNS")) {
					iterator.remove();
				}
			} else if (next instanceof PR) {
				PR pr = (PR) next;
				// only proper pronouns he she it ...
				if (pr.getPosValue().equals("PRP$")) {
					iterator.remove();
				}

			}
		}
		if (namedEntities == null || namedEntities.size() == 1) {
			return dataBag;
		}

		if (isPickupSimilar()) {

			Set<String> linkDependencyTypes = Sets.newHashSet("nn", "amod");
			Set<String> connectingWords = Sets.newHashSet("of", "de");
			Set<String> skipWords = Sets.newHashSet("son", "AP");

			// get all tokens
			Comparator<Annotation> c = new Comparator<Annotation>() {
				@Override
				public int compare(Annotation o1, Annotation o2) {
					return ComparisonChain.start().compare(o1.getBegin(), o2.getBegin()).compare(o1.getEnd(), o2.getEnd()).result();
				}
			};
			Set<Annotation> newNamedEntities = Sets.newHashSet();
			// expand ?

			for (Annotation namedEntity : namedEntities) {

				// get all tokens
				List<Token> tokens = JCasUtil.selectCovered(Token.class, namedEntity);
				if (tokens.size() == 0) {
					tokens = JCasUtil.selectCovering(Token.class, namedEntity);
				}

				List<Token> newTokens = Lists.newArrayList(tokens);

				boolean change;

				do {
					change = false;
					tokens = Lists.newArrayList(newTokens);
					for (Token token : tokens) {

						if (!graph.containsVertex(token)) {
							continue;
						}

						if (skipWords.contains(token.getCoveredText())) {
							continue;
						}

						for (final DependencyEdge dependencyEdge : graph.edgesOf(token)) {
							// check if source of edge is in the token list

							if (dependencyEdge.getTo().equals(token)) {
								// the token is the target
								// check the source
								// get the dependency for that token
								if (linkDependencyTypes.contains(dependencyEdge.getDependency())) {
									// this belong in the ring as well
									if (!newTokens.contains(dependencyEdge.getFrom())
										&& getSelectionType().isAssignableFrom(dependencyEdge.getFrom().getPos().getClass())
										&& !dependencyEdge.getTo().getPos().getClass().equals(PR.class)
										&& !dependencyEdge.getFrom().getPos().getPosValue().equals("NNS")
										&& !"AP".equals(dependencyEdge.getFrom().getCoveredText())) {
										newTokens.add(dependencyEdge.getFrom());
										change = true;
									}
								}

/*                                if (dependencyEdge.dependency.equals("pobj")) {
									if (!newTokens.contains(dependencyEdge.getFrom()) && PP.class.isAssignableFrom(dependencyEdge.getFrom().getPos().getClass()) && ("of".equals(dependencyEdge.getFrom().getCoveredText()) || "de".equals(dependencyEdge.getFrom().getCoveredText()))) {

                                        // now the
                                        // incoming arch from the dependency
                                        final Set<DependencyEdge> dependencyEdges = graph.edgesOf(dependencyEdge.getFrom());

                                        final Optional<DependencyEdge> dependencyEdgeOptional = Iterables.tryFind(dependencyEdges, new Predicate<DependencyEdge>() {
                                            @Override
                                            public boolean apply(@Nullable DependencyEdge input) {
                                                return !input.equals(dependencyEdge);
                                            }
                                        });
                                        // now the next incoming needs to be NNP not just NOUN to make any sense .. heuristic
                                        final boolean assignableFromNP = NP.class.isAssignableFrom(dependencyEdgeOptional.get().from.getPos().getClass());
                                        final boolean assignableFromNN = NN.class.isAssignableFrom(dependencyEdgeOptional.get().from.getPos().getClass());

                                        if(dependencyEdgeOptional.isPresent() && (assignableFromNP || assignableFromNN) && !dependencyEdgeOptional.get().from.getCoveredText().equalsIgnoreCase("graduate")) {
                                            newTokens.add(dependencyEdge.getFrom());
                                            newTokens.add(dependencyEdgeOptional.get().from);
                                            change = true;
                                        }
                                    }
                                }
                                */
							} else if (dependencyEdge.getFrom().equals(token)) {
								if (linkDependencyTypes.contains(dependencyEdge.getDependency())) {
									// this belong in the ring as well
									if (!newTokens.contains(dependencyEdge.getTo())
										&& getSelectionType().isAssignableFrom(dependencyEdge.getTo().getPos().getClass())
										&& !dependencyEdge.getTo().getPos().getPosValue().equals("NNS")
										&& !"AP".equals(dependencyEdge.getTo().getCoveredText())) {
										newTokens.add(dependencyEdge.getTo());
										change = true;
									}
								}
								if ("appos".equals(dependencyEdge.getDependency()) && !newTokens.contains(dependencyEdge.getTo()) && getSelectionType().isAssignableFrom(dependencyEdge.getTo().getPos().getClass())) {
									// check: He was born in Birmingham, Alabama, USA.
									// NNP , NNP
									List<Token> following = JCasUtil.selectFollowing(Token.class, token, 3);
									Token to = dependencyEdge.getTo();
									Token from = dependencyEdge.getFrom();
									if (",".equals(following.get(0).getCoveredText()) && (to.equals(following.get(1)) || to.equals(following.get(2))) && to.getPos().getPosValue().equals("NNP") && from.getPos().getPosValue().equals("NNP")) {
										newTokens.add(dependencyEdge.getTo());
										change = true;
									}
								}

								if ("conj".equals(dependencyEdge.getDependency()) && !newTokens.contains(dependencyEdge.getTo()) && getSelectionType().isAssignableFrom(dependencyEdge.getTo().getPos().getClass())) {
									// check: Niculiţă studied at the Faculty of History, University of Chişinău,
									// NNP , University
									List<Token> following = JCasUtil.selectFollowing(Token.class, token, 2);
									if (",".equals(following.get(0).getCoveredText()) && dependencyEdge.getTo().equals(following.get(1)) && "University".equals(dependencyEdge.getTo().getCoveredText())) {
										newTokens.add(dependencyEdge.getTo());
										change = true;
										// Black & Decker
									} else if ("&".equals(following.get(0).getCoveredText()) && dependencyEdge.getTo().equals(following.get(1))) {
										newTokens.add(dependencyEdge.getTo());
										change = true;
									}
								}

								// now check if there is something like this
								// University of Pennsylvania
								// but accept only "of" not anything else ...
								if ("prep".equals(dependencyEdge.getDependency())) {
									// University of Pennsylvania
									List<Token> following = JCasUtil.selectFollowing(Token.class, token, 1);
									if (connectingWords.contains(following.get(0).getCoveredText()) && dependencyEdge.getTo().equals(following.get(0)) && !newTokens.contains(dependencyEdge.getTo())) {

										// now demand that the next ones are linked via pobj

										// now the
										// incoming arch from the dependency
										final Set<DependencyEdge> dependencyEdges = graph.edgesOf(dependencyEdge.getTo());

										final Optional<DependencyEdge> dependencyEdgeOptional = Iterables.tryFind(dependencyEdges, new Predicate<DependencyEdge>() {
											@Override
											public boolean apply(@Nullable DependencyEdge input) {
												return !input.equals(dependencyEdge);
											}
										});

										if (!dependencyEdgeOptional.isPresent()) {
											continue;
										}
										DependencyEdge edge = dependencyEdgeOptional.get();

										// now the next incoming needs to be NNP not just NOUN to make any sense .. heuristic
										// let the pos agree
										final boolean posAgrees = edge.getTo().getPos().getPosValue().equals(dependencyEdge.getFrom().getPos().getPosValue());
										final boolean posAgreesSlighty = edge.getTo().getPos().getPosValue().equals("NN") && dependencyEdge.getFrom().getPos().getPosValue().equals("NNS");
										final boolean posAgreesSlighty2 = edge.getTo().getPos().getPosValue().equals("NNS") && dependencyEdge.getFrom().getPos().getPosValue().equals("NN");
										final boolean posAgreesSlighty3 = edge.getTo().getPos().getPosValue().equals("NNS") && dependencyEdge.getFrom().getPos().getPosValue().equals("NNP");

										boolean invalid = Iterables.any(graph.edgesOf(edge.getTo()), new Predicate<DependencyEdge>() {
											@Override
											public boolean apply(@Nullable DependencyEdge input) {
												// allow conjunctions and also prepositions of the form of .. of
												return !StringUtils.startsWithAny(input.getDependency(), new String[]{"pobj", "nn", "conj", "cc", "prep"});
											}
										});

										if (!invalid && "pobj".equals(edge.getDependency()) && following.get(0).equals(edge.getFrom()) && (posAgrees || posAgreesSlighty || posAgreesSlighty2 || posAgreesSlighty3)) {
											newTokens.add(following.get(0));
											newTokens.add(edge.getTo());
											change = true;
										}


									}
								}


							}

						}

					}
				} while (change);

				// now we have a new named entity
				Collections.sort(tokens, c);

				final Token first = Iterables.getFirst(tokens, null);
				final Token last = Iterables.getLast(tokens);

				final boolean any = Iterables.any(newNamedEntities, new Predicate<Annotation>() {
					@Override
					public boolean apply(@Nullable Annotation input) {
						return (input.getBegin() == first.getBegin()) && (input.getEnd() == last.getEnd());
					}
				});
				if (!any) {
					newNamedEntities.add(new Annotation(getjCas(), first.getBegin(), last.getEnd()));
				}

			}
			// now retain only the largest "spans"
			namedEntities = Lists.newArrayList(newNamedEntities);
			Collections.sort(namedEntities, c);
			for (Iterator<? extends Annotation> it = namedEntities.iterator(); it.hasNext(); ) {
				// now see if this element is contained in the following
				final Annotation next = it.next();
				boolean toDelete = false;
				for (Annotation namedEntity : namedEntities) {
					if (namedEntity != next) {
						if (next.getBegin() >= namedEntity.getBegin() &&
							next.getEnd() <= namedEntity.getEnd()) {
							toDelete = true;
							break;
						}
					}
				}
				if (toDelete) {
					it.remove();
				}
			}
		}

		// now check that none of the entities are an appos of another ..
		// this should void examples such as "A, a New York City group" .. to treat A new York city group
		List<Annotation> splittedAnnotations = Lists.newArrayList();
		for (Iterator<? extends Annotation> it = namedEntities.iterator(); it.hasNext(); ) {

			Annotation entity = it.next();
			// get the head
			final Token namedEntityHead = getNamedEntityHead(entity, graph);

			if (!graph.containsVertex(namedEntityHead)) {
				continue;
			}

			// now check if the incoming edge is of type appos
			final Optional<DependencyEdge> appos = Iterables.tryFind(graph.edgesOf(namedEntityHead), new Predicate<DependencyEdge>() {
				@Override
				public boolean apply(@Nullable DependencyEdge input) {
					// allow conjunctions and also prepositions of the form of .. of
					return input.getDependency().equals("appos") && input.getTo().equals(namedEntityHead);
				}
			});

			if (appos.isPresent()) {
				// check if the source of the edge is actually part of a named entity that we know
				final Token source = appos.get().getFrom();
				boolean any = Iterables.any(namedEntities, new Predicate<Annotation>() {
					@Override
					public boolean apply(@Nullable Annotation input) {

						final Token nextToFirst = Iterables.getFirst(JCasUtil.selectFollowing(Token.class, input, 1), null);
						if (nextToFirst != null && nextToFirst.getPos().getClass().equals(O.class)) {
							// quite likely something like Vanessa Chinitor (born 13 October 1976, Dendermonde)
							return false;
						} else {
							return source.getBegin() >= input.getBegin() && source.getEnd() <= input.getEnd();
						}
					}
				});

				if (any) {
					// remove the annotation
					//it.remove();
					continue;
				}
			}

			// now check if the annotation has a break
			List<POS> posList = JCasUtil.selectCovered(POS.class, entity);
			POS first = Iterables.getFirst(posList, null);
			POS last = first;
			Set<String> candidates = Sets.newHashSet("NNP", "NN");

			// count nnp and nn
			Multiset<String> posCounts = HashMultiset.create();
			for (POS pos : posList) {
				posCounts.add(pos.getPosValue());
			}
			// do this only for longer chains
			if (!(((posCounts.count("NNP") > 2) && posCounts.count("NN") >= 1) || ((posCounts.count("NNP") >= 1) && posCounts.count("NN") > 2))) {
				continue;
			}

			for (POS pos : posList) {
				if (!first.getPosValue().equals(pos.getPosValue()) && candidates.contains(pos.getPosValue())) {
					// potential break?
					// we need to split the annotation into two
					it.remove();
					splittedAnnotations.add(new Annotation(getjCas(), entity.getBegin(), last.getEnd()));
					splittedAnnotations.add(new Annotation(getjCas(), pos.getBegin(), entity.getEnd()));
					break; // exit loop - don't split twice
				}
				last = pos;
			}
		}
		// re-add splitted
		namedEntities.addAll(splittedAnnotations);

		DirectedGraph<Annotation, DefaultEdge> coref = new SimpleDirectedGraph<>(DefaultEdge.class);

		// check for co-reference links?

		if (isResolveCoreferences()) {

			for (CoreferenceChain coreferenceChain : JCasUtil.select(getjCas(), CoreferenceChain.class)) {

				final CoreferenceLink first = coreferenceChain.getFirst();

				// this is the token ...
				final Token entityHead = getNamedEntityHead(first, graph);
				// final Annotation sourceleft = Iterables.getFirst(annotations, null);

				// if we don't have the specific target annotation as a marker, just skip
				// this will prevent linking to pronouns that we are for example not interested in
				// check if we just have a number
				if (entityHead == null || entityHead.getPos().getClass().equals(CARD.class) || entityHead.getPos().getClass().equals(PUNC.class)) {
					continue;
				}

				// get the actual named entity, if it is already in the set - we don't want duplicates
				final Optional<Annotation> targetAnnotation = Iterables.tryFind(namedEntities, new Predicate<Annotation>() {
					@Override
					public boolean apply(@Nullable Annotation input) {
						return entityHead != null && (input.getBegin() <= entityHead.getBegin() && input.getEnd() >= entityHead.getEnd());
					}
				});

				final Annotation left = targetAnnotation.or(entityHead);


				if (first.getCoveredText().length() < left.getCoveredText().length()) {
					// check that the annotation is really covering the underlying named entity
					// we don't want to widen the scope if the mention is way shorter
					continue;
				}

				CoreferenceLink last = first;
				// traverse
				while (last.getNext() != null) {
					last = last.getNext();
					if (first != last) {


						// use the Token not the selection type when getting the underlying annotation
						final Token rightToken = getNamedEntityHead(last, graph);
						if (rightToken == null) {
							// ignore
							continue;
						}

						// get the actual named entity, if it is already in the set - we don't want duplicates
						final Optional<Annotation> targetRightAnnotation = Iterables.tryFind(namedEntities, new Predicate<Annotation>() {
							@Override
							public boolean apply(@Nullable Annotation input) {
								return rightToken != null && (input.getBegin() <= rightToken.getBegin() && input.getEnd() >= rightToken.getEnd());
							}
						});

						final Annotation right = targetRightAnnotation.or(rightToken);


						// check that the annotation is really covering the underlying named entity
						// but we also allow "HE SHE ..."

						// we don't want
						// Mention is way longer than the underlying annotation

						if (last.getCoveredText().length() > right.getCoveredText().length() || right.getCoveredText().endsWith("degree")) {
							// we don't want to widen the scope if the mention is way shorter
							continue;
						}


						if (left != right && left != null && (StringUtils.startsWithIgnoreCase(left.getCoveredText(), "mount") || !StringUtils.startsWithIgnoreCase(right.getCoveredText(), "mount"))) {
							coref.addVertex(left);
							coref.addVertex(right);
							coref.addEdge(left, right);

							// also add all token
							// now check all right annotations if they are maybe a named entity
		  /*                  for (Annotation namedEntity : namedEntities) {
                                // last the ihe coref target
                                if(namedEntity.getBegin() >= last.getBegin() && namedEntity.getEnd() <= last.getEnd()) {
                                    coref.addVertex(namedEntity);
                                    coref.addEdge(left, namedEntity);
                                }
                            }

                            List<Token> tokens = JCasUtil.selectCovered(Token.class, last);
                            for (Token token : tokens) {
                                coref.addVertex(token);
                                coref.addEdge(left, token);
                            }
             */
							// and add this now to the list of named entites if not already found
							// check if there is already an annotation that covers right
							// this is the case if an existing annotation begins before right and ends after right
							final boolean any = Iterables.any(namedEntities, new Predicate<Annotation>() {
								@Override
								public boolean apply(@Nullable Annotation existing) {
									return existing.getBegin() <= right.getBegin() && existing.getEnd() >= right.getEnd();
								}
							});

							if (!any && !(right.getCoveredText().equals("his") || right.getCoveredText().equals("her"))) {
								namedEntities.add(right);
							}
						}
					}
				}
			}

		}


    /*  // Output the coref graph for checking
        for (DefaultEdge co : coref.edgeSet()) {
            Annotation edgeSource = coref.getEdgeSource(co);
            Annotation edgeTarget = coref.getEdgeTarget(co);
            System.out.println(
                    Joiner.on(" ").join(edgeSource.getCoveredText(), edgeSource.getBegin(), edgeSource.getEnd()) + "   ->  " +
                            Joiner.on(" ").join(edgeTarget.getCoveredText(),edgeTarget.getBegin(), edgeTarget.hashCode()));
        }
        */

		// sort the named entities based on their appearance on the text
		Collections.sort(namedEntities, Ordering.from(new Comparator<Annotation>() {
			@Override
			public int compare(Annotation o1, Annotation o2) {
				return Integer.compare(o1.getBegin(), o2.getBegin());
			}
		}));

		// get all pairs of entities omitting incestuous and duplicate pairs
		List<EntityPair> entityPairs = Lists.newArrayList();
		for (int i = 0; i < namedEntities.size(); i++) {
			for (int j = i + 1; j < namedEntities.size(); j++) {

				// check that the entities do not overlap each other
				final Annotation annotation = namedEntities.get(i);
				final Annotation annotation2 = namedEntities.get(j);
				if (annotation2.getBegin() > annotation.getEnd()) {
					entityPairs.add(new EntityPair(annotation, annotation2));
				}
			}
		}


		if (isCollapseMentions()) {
			for (int i = 0; i < namedEntities.size(); i++) {
				final Annotation left = namedEntities.get(i);
				for (int j = i + 1; j < namedEntities.size(); j++) {
					final Annotation right = namedEntities.get(j);

					if (StringComparision.determineSimilar(left.getCoveredText(), right.getCoveredText())) {
						// add a co-ref link from right -> left
						coref.addVertex(left);
						coref.addVertex(right);
						coref.addEdge(left, right);
					}

				}
			}
		}

		// extract the shortest path between every two entities
		for (EntityPair pair : entityPairs) {

			final Token e1 = getNamedEntityHead(pair.getEntity1(), graph);
			final Token e2 = getNamedEntityHead(pair.getEntity2(), graph);

			try {

				// if e1 or e2 is NULL we might not have information about the dependencies in the sentence
				// which contains the NER
				// this is not a real error and can happen, as we are searching for links in the whole document, not just sentence wise, but
				// the feature extractor is limited to sentences at the moment
				if (e1 == null || e2 == null) {
					continue;
				}

				if (!(graph.containsVertex(e1) && graph.containsVertex(e2))) {
					continue;
				}
				// check if the entities are in the same sentence?
				// get sentence for #1
         /*       Sentence sentences1 = Iterables.getFirst(JCasUtil.selectCovering(jCas, Sentence.class, e1.getBegin(), e1.getEnd()), null);
                // for #2
                Sentence sentences2 = Iterables.getFirst(JCasUtil.selectCovering(jCas, Sentence.class, e2.getBegin(), e2.getEnd()), null);

                // test equivalence
                if (sentences1 != sentences2) {
                    // not in the same sentence
                    continue;
                }*/

				DijkstraShortestPath<Token, DependencyEdge> shortestPaths = new DijkstraShortestPath<>(graph, e1, e2);

				GraphPath<Token, DependencyEdge> shortestPath = shortestPaths.getPath();
				if (shortestPath == null || shortestPath.getEdgeList().size() == 0) {
					continue;
				}

				// retrieve vertices on the shortest path
				// the first and last entry are the start and end vertex -> dynamically encode as X and Y
				final List<Token> vertices = Graphs.getPathVertexList(shortestPath);
				List<DependencyEdge> edges = shortestPath.getEdgeList();

/*                boolean firstEdgeConjunction = "conj".equals(edges.get(0).dependency);
                if(firstEdgeConjunction) {
                    // remove
                    if(vertices.size() >= 2) {
                        vertices.remove(1);
                    }
                    edges.remove(0);
                    if(edges.size() == 0) {
                        // empty path .. pointless
                        continue;
                    }
                }*/

				Collections.sort(vertices, new Comparator<Token>() {
					@Override
					public int compare(Token o1, Token o2) {
						return Ints.compare(o1.getBegin(), o2.getBegin());
					}
				});


				String vertexLabels = Joiner.on(" ").skipNulls().join(Iterables.transform(vertices, new Function<Token, String>() {

					@Override
					public String apply(Token token) {
						if (e1.equals(token)) {
							return "[X]";
						} else if (e2.equals(token)) {
							return "[Y]";
						} else
							return (isLemmatize() ? token.getLemma().getValue() : token.getCoveredText().replaceAll("\n", " "));
					}
				}));

				// retrieve edges /dependencies on the shortest path

				Iterable<String> edgeListLables = Iterables.transform(edges, new Function<DependencyEdge, String>() {
					@Override
					public String apply(DependencyEdge edge) {
						return Joiner.on("-").join(vertices.indexOf(edge.getFrom()), edge.getDependency(), vertices.indexOf(edge.getTo()));
					}
				});


				String edgeLabels = Joiner.on(",").skipNulls().join(Ordering.natural().sortedCopy(edgeListLables));

				// concatenate pattern parts
				final String pattern;

				pattern = vertexLabels + " [" + edgeLabels + "]";

				Annotation ent1 = pair.entity1;
				Annotation ent2 = pair.entity2;

				if (getSelectionType().equals(NamedEntity.class)) {

					if (isResolveCoreferences()) {
						// check if annotation1 is actually at the end of a co-reference chain
						// so we ask the graph


						//noinspection ConstantConditions
						if (coref.containsVertex(pair.getEntity1()) && coref.inDegreeOf(pair.getEntity1()) > 0) {
							final DefaultEdge edge = Iterables.getFirst(coref.incomingEdgesOf(pair.getEntity1()), null);
							final Annotation edgeSource = coref.getEdgeSource(edge);
							if (!edgeSource.equals(pair.getEntity2())) {
								ent1 = edgeSource; //+ " -corf of " + e1text;
							}
						}

						if (coref.containsVertex(pair.getEntity2()) && coref.inDegreeOf(pair.getEntity2()) > 0) {
							final DefaultEdge edge = Iterables.getFirst(coref.incomingEdgesOf(pair.getEntity2()), null);
							final Annotation edgeSource = coref.getEdgeSource(edge);
							if (!edgeSource.equals(pair.getEntity1())) {
								ent2 = edgeSource; // + " -corf of " + e2text;
							}
							//ent1 = Iterables.getFirst(coref.incomingEdgesOf(pair.getEntity2()), null).getSource();
						}
					}


				} else {
					//ent1 = e1text;
					//ent2 = e2text;
					// default TEXT
					if (isResolveCoreferences()) {
						// check if annotation1 is actually at the end of a co-reference chain

						// and also make sure we don't find a path between the same entities

						// so we ask the graph
						//noinspection ConstantConditions
						boolean replace = false; // don't replace two instances
						if (coref.containsVertex(pair.getEntity1()) && coref.inDegreeOf(pair.getEntity1()) > 0) {
							final DefaultEdge edge = Iterables.getFirst(coref.incomingEdgesOf(pair.getEntity1()), null);
							final Annotation edgeSource = coref.getEdgeSource(edge);
							if (!edgeSource.equals(pair.getEntity2())) {
								ent1 = edgeSource; //+ " -corf of " + e1text;
								replace = true;
							}
						}
						// skip the replacement if we have already replace the first instance
						if (!replace && coref.containsVertex(pair.getEntity2()) && coref.inDegreeOf(pair.getEntity2()) > 0) {
							final DefaultEdge edge = Iterables.getFirst(coref.incomingEdgesOf(pair.getEntity2()), null);
							final Annotation edgeSource = coref.getEdgeSource(edge);
							if (!edgeSource.equals(pair.getEntity1())) {
								ent2 = edgeSource; // + " -corf of " + e2text;
							}
							//ent1 = Iterables.getFirst(coref.incomingEdgesOf(pair.getEntity2()), null).getSource();
						}


					}

				}

				dataBag.add(new FoundFeature<>(ent1, ent2, pattern));

			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}

		// here we could check if we have [X] be [Y] [1-attr-2,1-nsubj-0]   and "just" store X == Y -> replace the second mention according to the text with the first in the text
		final Set<String> pronouns = Sets.newHashSet("them", "their", "he", "she");
		final Optional<FoundFeature<Annotation>> potentialEquivalence = Iterables.tryFind(dataBag, new Predicate<FoundFeature<Annotation>>() {
			@Override
			public boolean apply(@Nullable FoundFeature<Annotation> input) {
				return "[X] be [Y] [1-attr-2,1-nsubj-0]".equals(input.getPattern()) && !pronouns.contains(input.entity1.getCoveredText());
			}
		});
		if (potentialEquivalence.isPresent()) {
			final FoundFeature<Annotation> equivalenceClass = potentialEquivalence.get();
			Annotation first = equivalenceClass.entity1.getBegin() < equivalenceClass.getEntity2().getBegin() ? equivalenceClass.entity1 : equivalenceClass.entity2;
			Annotation second = equivalenceClass.entity1.getBegin() < equivalenceClass.getEntity2().getBegin() ? equivalenceClass.entity2 : equivalenceClass.entity1;
			dataBag.remove(equivalenceClass);

			for (FoundFeature<Annotation> foundFeature : dataBag) {
				// now replace X or Y with the equivalence
				if (foundFeature.entity1.equals(second)) {
					foundFeature.entity1 = first;
				}
			}
		}
		// Postprocessing

		// now clean the pattern .. if it contains something like
		// sentence.replaceAll("at the age of \\d*", "");
		// [X] die at age of 82 in [Y] [1-nsubj-0,1-prep-2,2-pobj-3,3-prep-4,4-pobj-5,5-prep-6,6-pobj-7]
		// this is what we want [X] die in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]


		return dataBag;

	}

	private Set<String> getConcepts(String concepts) {

		Set<String> list = Sets.newTreeSet();

		concepts = concepts.substring(1, concepts.length() - 1);
		concepts = concepts.replaceAll("\\(", "");
		concepts = concepts.replaceAll("\\)", "");
		String[] split = concepts.split(",");

		for (String c : split) {
			addConcept(list, c);
		}

		return list;
	}


	private static void addConcept(Set<String> list,
								   String concept) {

		if (concept.startsWith("wordnet")) {
			//  return;
		}

		if (concept.startsWith("wikicategory")) {

			String orig = concept;

			List<String> prepositions = Lists.newArrayList();
			prepositions.add("born_in");
			prepositions.add("set_in");

			prepositions.add("in");
			prepositions.add("of");
			prepositions.add("from");
			prepositions.add("that");
			prepositions.add("by");
			prepositions.add("on");
			prepositions.add("over");
			prepositions.add("under");
			prepositions.add("at");
			prepositions.add("about");
			prepositions.add("who");
			prepositions.add("which");
			prepositions.add("to");
			prepositions.add("at");
			prepositions.add("involving");

			concept = concept.substring(13);

			for (String prep : prepositions) {

				String pattern = "ed_" + prep + "_";

				if (concept.contains(pattern)) {
					concept = concept.replaceAll("_[^_]+" + pattern + ".*", "");
				}

				pattern = "_" + prep + "_";

				if (concept.contains(pattern)) {
					concept = concept.replaceAll(pattern + ".*", "");
				}

			}

			concept = concept.replaceAll(".+_", "");
			concept = concept.toLowerCase();
		}


		if (concept.startsWith("yago")) {

			if (concept.equals("yagoPermanentlyLocatedEntity")) {
				list.add("locations");
				return;
			}
			if (concept.startsWith("yagoGeoEntity")) {
				list.add("locations");
				return;
			}
			return;
		}

		if (concept.startsWith("wordnet")) {
			if (concept.startsWith("wordnet_loc")) {
				list.add("locations");
			}
			if (concept.startsWith("wordnet_person")) {
				list.add("people");
			}
			if (concept.startsWith("wordnet_geographical_area")) {
				list.add("locations");
			}
			return;
		}


		if (concept.startsWith("geoent")) {
			list.add("locations");
			return;
		}
		if (concept.startsWith("geoclass")) {
			list.add("locations");
			return;
		}

		list.add(concept);
	}


	//Pattern wordPattern = Pattern.compile("^[“”'\"\\-„“‘’«»‹›¡¿]$");
	// inspired by http://stackoverflow.com/questions/1073412/javascript-validation-issue-with-international-characters
	Pattern wordPattern = Pattern.compile("^[\\w\\s\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]+$");

	private boolean isMeaningLessEntity(String text) {
		return !wordPattern.matcher(text).matches();
	}
}
