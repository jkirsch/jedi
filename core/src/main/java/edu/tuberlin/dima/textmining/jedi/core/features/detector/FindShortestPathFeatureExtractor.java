package edu.tuberlin.dima.textmining.jedi.core.features.detector;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.CARD;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NP;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.O;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import edu.tuberlin.dima.textmining.jedi.core.util.JCommanderClassConverter;
import edu.tuberlin.dima.textmining.jedi.core.util.StringComparision;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: 4/10/13
 * Time: 3:35 PM
 *
 * @author Johannes Kirschnick
 *         <p/>
 *         Description: A Function that takes a JCas as input and extracts
 *         its annotations in terms of the shortest path between
 *         all recognized entities.
 */

public class FindShortestPathFeatureExtractor {

    private JCas jCas;

    @Parameter(names = {"-selectionType"}, description = "Class of selection Type (e.g. Named Entity)", required = true, converter = JCommanderClassConverter.class)
    private Class<? extends Annotation> selectionType;

    @Parameter(names = {"-additionalSelectionType"}, description = "AdditionalClass of selection Type (e.g. Named Entity)", required = false, converter = JCommanderClassConverter.class)
    private Class<? extends Annotation> additionalSelectionType;

    @Parameter(names = {"-resolveCoreferences"}, description = "Resolve Co-references for a given token", required = false)
    private boolean resolveCoreferences = false;

    @Parameter(names = {"-lemmatize"}, description = "Lemmatize the pattern to remove times", required = false)
    private boolean lemmatize = false;

    @Parameter(names = {"-pickupSimilar"}, description = "If set, the token is expanded left and right with tokens of the same selectionType", required = false)
    private boolean pickupSimilar = false;

    @Parameter(names = {"-collapseMentions"}, description = "If set, similar mentions are collapsed", required = false)
    private boolean collapseMentions = false;


    enum Counters {
        FEATURES
    }

    public FindShortestPathFeatureExtractor(String options) throws UIMAException {

        JCommander jCommander = new JCommander(this);
        try {
            // parse options
            jCommander.parse(options.split(" "));

        } catch (ParameterException e) {
            StringBuilder out = new StringBuilder();
            jCommander.setProgramName(this.getClass().getSimpleName());
            jCommander.usage(out);
            throw new RuntimeException(e.getMessage() + "\n" + "In: " + options + "\n" + out.toString());
        } catch (Exception e) {
            throw new RuntimeException("Error initializing FindShortestPathFeatureExtractor", e);
        }

    }

    public List<FoundFeature<Annotation>> exec(JCas xmlDocument) throws IOException {

        if (xmlDocument == null) {
            return null;
        }

        jCas = xmlDocument;

        // choose a feature extraction strategy
        // 1. insert dependencies into a graph and extract shortest path
        UndirectedGraph<Token, DependencyEdge> graph = makeDependencyGraph();
        if (graph == null || graph.vertexSet().size() == 0) {
            return Lists.newArrayList();
        }

        return getShortestPaths(graph);
    }

    /**
     * Inserts all dependencies of an annotated sentence into a graph
     * with governor and dependent as vertices and dependency type as
     * edge.
     *
     * @return UndirectedGraph of dependencies
     */
    public UndirectedGraph<Token, DependencyEdge> makeDependencyGraph() {

        UndirectedGraph<Token, DependencyEdge> graph = new SimpleGraph<>(DependencyEdge.class);

        // we can limit the graph a bit
        // as we are only interested in (selectionType) like NERs, we can skip sentences which have < 2 NERs.

        Iterator<Sentence> sentences = JCasUtil.iterator(jCas, Sentence.class);

        while (sentences.hasNext()) {
            Sentence sentence = sentences.next();
            // check the # of NERs
            List<? extends Annotation> entities = JCasUtil.selectCovered(selectionType, sentence);
            List<? extends Annotation> additional = Lists.newArrayList();
            if(additionalSelectionType != null) {
                additional = JCasUtil.selectCovered(additionalSelectionType, sentence);
            }
            if ((entities.size() + additional.size()) < 2 && !resolveCoreferences) {
                continue;
            }

            // here we are interested in the deps
            List<Dependency> dependencies = JCasUtil.selectCovered(Dependency.class, sentence);

            for (Dependency dependency : dependencies) {
                Token governor = dependency.getGovernor();
                Token dependent = dependency.getDependent();
                String dependencyType = dependency.getDependencyType();

                graph.addVertex(governor);
                graph.addVertex(dependent);
                graph.addEdge(governor, dependent, new DependencyEdge(governor, dependent, dependencyType));

            }

        }


        return graph;
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
    public List<FoundFeature<Annotation>> getShortestPaths(final UndirectedGraph<Token, DependencyEdge> graph) {

        List<FoundFeature<Annotation>> dataBag = Lists.newArrayList();

        List<Annotation> namedEntities = Lists.newArrayList(JCasUtil.select(jCas, this.selectionType));
        if(this.additionalSelectionType != null) {
            // add additional selection types
            namedEntities.addAll(JCasUtil.select(jCas, this.additionalSelectionType));
        }
        // remove NNS  & PRP
        final Iterator<Annotation> iterator = namedEntities.iterator();
        while (iterator.hasNext()) {
            final Annotation next = iterator.next();
            if (next instanceof NN) {
                NN c = (NN) next;
                if(c.getPosValue().equals("NNS")) {
                    iterator.remove();
                }
            } else if (next instanceof PR) {
                PR pr = (PR) next;
                // only proper pronouns he she it ...
                if(pr.getPosValue().equals("PRP$")) {
                    iterator.remove();
                }

            }
        }
        if (namedEntities == null || namedEntities.size() == 1) {
            return dataBag;
        }

        if (pickupSimilar) {

            Set<String> linkDependencyTypes = Sets.newHashSet("nn", "amod", "poss");
            Set<String> connectingWords = Sets.newHashSet("of","de");
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

                        if(skipWords.contains(token.getCoveredText())) {
                            continue;
                        }

                        for (final DependencyEdge dependencyEdge : graph.edgesOf(token)) {
                            // check if source of edge is in the token list

                            if (dependencyEdge.to.equals(token)) {
                                // the token is the target
                                // check the source
                                // get the dependency for that token
                                if (linkDependencyTypes.contains(dependencyEdge.dependency)) {
                                    // this belong in the ring as well
                                    if (!newTokens.contains(dependencyEdge.from)
                                            && this.selectionType.isAssignableFrom(dependencyEdge.from.getPos().getClass())
                                            && !dependencyEdge.to.getPos().getClass().equals(PR.class)
                                            && !dependencyEdge.from.getPos().getPosValue().equals("NNS")
                                            && !"AP".equals(dependencyEdge.from.getCoveredText())) {
                                        newTokens.add(dependencyEdge.from);
                                        change = true;
                                    }
                                }

/*                                if (dependencyEdge.dependency.equals("pobj")) {
                                    if (!newTokens.contains(dependencyEdge.from) && PP.class.isAssignableFrom(dependencyEdge.from.getPos().getClass()) && ("of".equals(dependencyEdge.from.getCoveredText()) || "de".equals(dependencyEdge.from.getCoveredText()))) {

                                        // now the
                                        // incoming arch from the dependency
                                        final Set<DependencyEdge> dependencyEdges = graph.edgesOf(dependencyEdge.from);

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
                                            newTokens.add(dependencyEdge.from);
                                            newTokens.add(dependencyEdgeOptional.get().from);
                                            change = true;
                                        }
                                    }
                                }
                                */
                            } else if (dependencyEdge.from.equals(token)) {
                                if (linkDependencyTypes.contains(dependencyEdge.dependency)) {
                                    // this belong in the ring as well
                                    if (!newTokens.contains(dependencyEdge.to)
                                            && this.selectionType.isAssignableFrom(dependencyEdge.to.getPos().getClass())
                                            && !dependencyEdge.to.getPos().getPosValue().equals("NNS")
                                            && !"AP".equals(dependencyEdge.to.getCoveredText())) {
                                        newTokens.add(dependencyEdge.to);
                                        change = true;
                                    }
                                }
                                if("appos".equals(dependencyEdge.dependency) && !newTokens.contains(dependencyEdge.to) && this.selectionType.isAssignableFrom(dependencyEdge.to.getPos().getClass())) {
                                    // check: He was born in Birmingham, Alabama, USA.
                                    // NNP , NNP
                                    List<Token> following = JCasUtil.selectFollowing(Token.class, token, 3);
                                    Token to = dependencyEdge.to;
                                    if(",".equals(following.get(0).getCoveredText()) && (to.equals(following.get(1)) || to.equals(following.get(2))) && to.getPos().getPosValue().equals("NNP")) {
                                        newTokens.add(dependencyEdge.to);
                                        change = true;
                                    }
                                }

                                if("conj".equals(dependencyEdge.dependency) && !newTokens.contains(dependencyEdge.to) && this.selectionType.isAssignableFrom(dependencyEdge.to.getPos().getClass())) {
                                    // check: Niculiţă studied at the Faculty of History, University of Chişinău,
                                    // NNP , University
                                    List<Token> following = JCasUtil.selectFollowing(Token.class, token, 2);
                                    if(",".equals(following.get(0).getCoveredText()) && dependencyEdge.to.equals(following.get(1)) && "University".equals(dependencyEdge.to.getCoveredText())) {
                                        newTokens.add(dependencyEdge.to);
                                        change = true;
                                    }
                                }

                                // now check if there is something like this
                                // University of Pennsylvania
                                // but accept only "of" not anything else ...
                                if ("prep".equals(dependencyEdge.dependency)) {
                                    // University of Pennsylvania
                                    List<Token> following = JCasUtil.selectFollowing(Token.class, token, 1);
                                    if(connectingWords.contains(following.get(0).getCoveredText()) && dependencyEdge.to.equals(following.get(0)) && !newTokens.contains(dependencyEdge.to)) {

                                        // now demand that the next ones are linked via pobj

                                        // now the
                                        // incoming arch from the dependency
                                        final Set<DependencyEdge> dependencyEdges = graph.edgesOf(dependencyEdge.to);

                                        final Optional<DependencyEdge> dependencyEdgeOptional = Iterables.tryFind(dependencyEdges, new Predicate<DependencyEdge>() {
                                            @Override
                                            public boolean apply(@Nullable DependencyEdge input) {
                                                return !input.equals(dependencyEdge);
                                            }
                                        });

                                        if(!dependencyEdgeOptional.isPresent()) {
                                            continue;
                                        }
                                        DependencyEdge edge = dependencyEdgeOptional.get();

                                        // now the next incoming needs to be NNP not just NOUN to make any sense .. heuristic
                                        final boolean assignableFromNP = NP.class.isAssignableFrom(edge.to.getPos().getClass());
                                        final boolean assignableFromNN = NN.class.isAssignableFrom(edge.to.getPos().getClass());

                                        boolean invalid = Iterables.any(graph.edgesOf(edge.to), new Predicate<DependencyEdge>() {
                                            @Override
                                            public boolean apply(@Nullable DependencyEdge input) {
                                                // allow conjunctions and also prepositions of the form of .. of
                                                return !StringUtils.startsWithAny(input.dependency, new String[]{"pobj", "nn", "conj", "cc", "prep"});
                                            }
                                        });

                                        if(!invalid && "pobj".equals(edge.dependency) && following.get(0).equals(edge.from) && (assignableFromNP || assignableFromNN)) {
                                            newTokens.add(following.get(0));
                                            newTokens.add(edge.to);
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
                if(!any) {
                    newNamedEntities.add(new Annotation(jCas, first.getBegin(), last.getEnd()));
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
                    if(namedEntity != next) {
                        if(next.getBegin() >= namedEntity.getBegin() &&
                                next.getEnd() <= namedEntity.getEnd()) {
                            toDelete = true;
                            break;
                        }
                    }
                }
                if(toDelete) {
                    it.remove();
                }
            }
        }

        DirectedGraph<Annotation, DefaultEdge> coref = new SimpleDirectedGraph<>(DefaultEdge.class);

        // check for co-reference links?

        if (resolveCoreferences) {

            for (CoreferenceChain coreferenceChain : JCasUtil.select(jCas, CoreferenceChain.class)) {

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


                if(first.getCoveredText().length() < left.getCoveredText().length()) {
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
                        if(rightToken == null) {
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

                        if(last.getCoveredText().length() > right.getCoveredText().length() || right.getCoveredText().endsWith("degree")) {
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

                            if(!any && !(right.getCoveredText().equals("his") || right.getCoveredText().equals("her")) ) {
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


        if(collapseMentions)  {
            for (int i = 0; i < namedEntities.size(); i++) {
                final Annotation left = namedEntities.get(i);
                for (int j = i+1; j < namedEntities.size(); j++) {
                    final Annotation right = namedEntities.get(j);

                    if(StringComparision.determineSimilar(left.getCoveredText(), right.getCoveredText())) {
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

                DijkstraShortestPath<Token, DependencyEdge> shortestPaths = new DijkstraShortestPath<Token, DependencyEdge>(graph, e1, e2);

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
                            return (lemmatize ? token.getLemma().getValue() : token.getCoveredText().replaceAll("\n", " "));
                    }
                }));

                // retrieve edges /dependencies on the shortest path

                Iterable<String> edgeListLables = Iterables.transform(edges, new Function<DependencyEdge, String>() {
                    @Override
                    public String apply(DependencyEdge edge) {
                        return Joiner.on("-").join(vertices.indexOf(edge.from), edge.dependency, vertices.indexOf(edge.to));
                    }
                });


                String edgeLabels = Joiner.on(",").skipNulls().join(Ordering.natural().sortedCopy(edgeListLables));

                // concatenate pattern parts
                final String pattern;

                pattern = vertexLabels + " [" + edgeLabels + "]";

                Annotation ent1 = pair.entity1;
                Annotation ent2 = pair.entity2;

                if (this.selectionType.equals(NamedEntity.class)) {

                    if (resolveCoreferences) {
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
                    if (resolveCoreferences) {
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
                                replace= true;
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
        final Optional<FoundFeature<Annotation>> potentialEquivalence = Iterables.tryFind(dataBag, new Predicate<FoundFeature<Annotation>>() {
            @Override
            public boolean apply(@Nullable FoundFeature<Annotation> input) {
                return "[X] be [Y] [1-attr-2,1-nsubj-0]".equals(input.getPattern());
            }
        });
        if(potentialEquivalence.isPresent()) {
            final FoundFeature<Annotation> equivalenceClass = potentialEquivalence.get();
            Annotation first = equivalenceClass.entity1.getBegin() < equivalenceClass.getEntity2().getBegin() ? equivalenceClass.entity1 : equivalenceClass.entity2;
            Annotation second = equivalenceClass.entity1.getBegin() < equivalenceClass.getEntity2().getBegin() ? equivalenceClass.entity2 : equivalenceClass.entity1;
            dataBag.remove(equivalenceClass);

            for (FoundFeature<Annotation> foundFeature : dataBag) {
                // now replace X or Y with the equivalence
                if(foundFeature.entity1.equals(second)) {
                    foundFeature.entity1 = first;
                }
            }
        }
        // Postprocessing

        // now clean the pattern .. if it contains something like
        // sentence.replaceAll("at the age of \\d*", "");
        // [X] die at age of 82 in [Y] [1-nsubj-0,1-prep-2,2-pobj-3,3-prep-4,4-pobj-5,5-prep-6,6-pobj-7]
        // this is what we want [X] die in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]


        List<FoundFeature<Annotation>> additional = Lists.newArrayList();
        final Iterator<FoundFeature<Annotation>> postProcessor = dataBag.iterator();
        while (postProcessor.hasNext()) {
            final FoundFeature<Annotation> annotationFoundFeature = postProcessor.next();
            String pattern = annotationFoundFeature.getPattern();
            if(renamer.matcher(pattern).find()) {
                annotationFoundFeature.pattern = "[X] die in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]";
                continue;
            }
            if(pattern.equals("[X] die on [Y] [1-nsubj-0,1-prep-2,2-pobj-3]")) {
                postProcessor.remove();
                continue;
            } else if(pattern.equals("die on [X] in [Y] [0-prep-1,0-prep-3,1-pobj-2,3-pobj-4]")){
                postProcessor.remove();
                continue;
            }

            final Matcher conjunctionAtTheEnd = this.conjunctionAtTheEnd.matcher(pattern);
            if (conjunctionAtTheEnd.find()) {
                pattern = annotationFoundFeature.pattern = conjunctionAtTheEnd.replaceFirst("[X]$1 [Y] [$3]");
            }

            if(dateMatcher.matcher(annotationFoundFeature.entity1.getCoveredText()).find()
                    ||
               dateMatcher.matcher(annotationFoundFeature.entity2.getCoveredText()).find()) {
                postProcessor.remove();
                continue;
            }

            Matcher deathMatcher = deathMatcherPattern.matcher(pattern);
            if(deathMatcher.find()) {
                annotationFoundFeature.pattern = deathMatcher.replaceFirst("[X] die $3 [Y] [1-nsubj-0,1-prep-2,2-pobj-3]");
                continue;
            }

            final Matcher matcher = birthMatcher.matcher(pattern);
            if(matcher.find()) {
                // [1-nsubjpass-0,1-prep-2,2-pobj-3]
                annotationFoundFeature.pattern = matcher.replaceFirst("[X] bear $3 [Y] [1-nsubjpass-0,1-prep-2,2-pobj-3]");
                continue;
            }

            // check if right next to Token is a bracket (parataxis) (problem in parsing)
            final Token nextToFirst = Iterables.getFirst(JCasUtil.selectFollowing(Token.class, annotationFoundFeature.entity1, 1), null);

            if(nextToFirst != null && nextToFirst.getPos().getClass().equals(O.class)) {
                final Token namedEntityHead = getNamedEntityHead(annotationFoundFeature.entity1, graph);
                if(namedEntityHead == null) {
                    continue;
                }
                final Set<DependencyEdge> dependencyEdges = graph.edgesOf(namedEntityHead);

                Optional<DependencyEdge> dependencyEdge = Iterables.tryFind(dependencyEdges, new Predicate<DependencyEdge>() {
                    @Override
                    public boolean apply(@Nullable DependencyEdge input) {
                        return input.from.equals(namedEntityHead) && ("parataxis".equals(input.dependency) || ("punct".equals(input.dependency) && "(".equals(input.to.getCoveredText())));
                    }
                });

                if(dependencyEdge.isPresent()) {
                    // also make sure that that the dependency is not that far from the (
                    // should find the closing ) and check that it's in there ? -- but could be mixed with coref .. heuristic here
                    List<Token> inBetween = JCasUtil.selectBetween(Token.class, annotationFoundFeature.getEntity1(), annotationFoundFeature.getEntity2());
                    if(inBetween.size() > 6) {
                        continue;
                    }
                    final Matcher parataxisCheck1 = parataxisCheckPattern.matcher(pattern);
                    if(parataxisCheck1.find()) {
                        String to = lemmatize?dependencyEdge.get().to.getLemma().getValue():dependencyEdge.get().to.getCoveredText();
                        annotationFoundFeature.pattern = parataxisCheck1.replaceFirst("[X] "+ to +" $2 [Y] [1-nsubj-0,1-prep-2,2-pobj-3]");
                        continue;
                    }
                    Matcher parataxisCheck2 = parataxisCheckPattern2.matcher(pattern);
                    if(parataxisCheck2.find()) {
                        // if we are linked directly via the "parataxtis" take the match
                        // otherwise look one token further
                        List<Token> following = JCasUtil.selectFollowing(Token.class, annotationFoundFeature.getEntity1(), 2);
                        // this assumes that there will always be as the first token after entity 1 a ( ...
                        //int pos = "parataxis".equals(dependencyEdge.get().dependency) ? 1 : 1;
                        Token afterBracket = following.get(1);
                        String to = lemmatize?afterBracket.getLemma().getValue():afterBracket.getCoveredText();
                        annotationFoundFeature.pattern = "[X] "+ to +" in [Y] [1-npadvmod-0,1-prep-2,2-pobj-3]";
                        continue;
                    }
                }

            }

/*            // flip receive
            if(annotationFoundFeature.getPattern().equals("[X] receive [Y] [1-dobj-2,1-nsubj-0]")) {
                additional.add(new FoundFeature<Annotation>(annotationFoundFeature.getEntity2(), annotationFoundFeature.getEntity1(), annotationFoundFeature.getPattern()));
            }*/

        }

        dataBag.addAll(additional);

        return dataBag;

    }

    Pattern renamer = Pattern.compile("\\[X\\] die at age of \\d+ in \\[Y\\] \\[1-nsubj-0,1-prep-2,2-pobj-3,3-prep-4,4-pobj-5,5-prep-6,6-pobj-7\\]");
    Pattern dateMatcher = Pattern.compile("(?i)^(-|\"|“|”|'|„|“|‘|’|«|»|‹|›|¡|¿||who|[\\d]+|year|post|life|age|January|February|March|April|May|June|July|August|September|October|November|December)$");
    Pattern deathMatcherPattern = Pattern.compile("\\[X] die(,|\\s|on )+(January|February|March|April|May|June|July|August|September|October|November|December){0,1}[\\s\\d]* (in|at) \\[Y\\] \\[.*\\d+-prep-\\d+,\\d+-pobj-\\d+\\]");
    static Pattern birthMatcher = Pattern.compile("\\[X\\] bear(,|\\s|on |in )+(January|February|March|April|May|June|July|August|September|October|November|December){0,1}[,\\s\\d]* (to|in|at) \\[Y\\] \\[(\\d)*-(\\w*)-(\\d)*.*\\d+-(\\w+)-\\d+\\]");

    // Günter Theodor Netzer (born 14 September 1944 in Mönchengladbach)
    // Parataxis check
    static Pattern parataxisCheckPattern = Pattern.compile("\\[X\\] (January|February|March|April|May|June|July|August|September|October|November|December){0,1}[\\s\\d]* (in|at) \\[Y\\] \\[0-appos-1,1-prep-2,2-pobj-3\\]");
    static Pattern parataxisCheckPattern2 = Pattern.compile("\\[X\\] \\[Y\\] \\[0-appos-1\\]");

    Pattern conjunctionAtTheEnd = Pattern.compile("\\[X\\]([\\w\\s]+) (\\w+) \\[Y\\] \\[(.*),\\d+-conj-\\d+\\]");

    /**
     * Extract the head of a named entity.
     *
     * @param namedEntity the named entity whose head to get
     * @param graph       the dependency graph
     * @return String head      the head of the named entity
     */
    public Token getNamedEntityHead(Annotation namedEntity, UndirectedGraph<Token, DependencyEdge> graph) {


        // get all tokens
        List<Token> tokens = JCasUtil.selectCovered(Token.class, namedEntity);
        if (tokens.size() == 0) {
            tokens = JCasUtil.selectCovering(Token.class, namedEntity);
        }

        Token candidate = null;

        for (Token token : tokens) {

            if (!graph.containsVertex(token)) {
                continue;
            }

            int inDegree = 0;
            int outDegree = 0;

            for (DependencyEdge dependencyEdge : graph.edgesOf(token)) {
                // check if source of edge is in the token list
                if (!tokens.contains(dependencyEdge.from)) {
                    // this links comes from "outside" so assume we are the head
                    return token;
                }

                Token edgeSource = graph.getEdgeSource(dependencyEdge);
                Token edgeTarget = graph.getEdgeTarget(dependencyEdge);

                if(edgeSource.equals(token)) {
                    outDegree++;
                } else if(edgeTarget.equals(token)) {
                    inDegree++;
                }
            }
            // if ..
            if(inDegree == 0 && outDegree > 0) {
                candidate = token;
            }

        }

        return candidate;

    }


    private static class DependencyEdge {
        protected Token from;
        private Token to;
        private String dependency;

        private DependencyEdge(Token from, Token to, String dependency) {
            this.from = from;
            this.to = to;
            this.dependency = dependency;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DependencyEdge that = (DependencyEdge) o;

            if (dependency != null ? !dependency.equals(that.dependency) : that.dependency != null) return false;
            if (from != null ? !from.equals(that.from) : that.from != null) return false;
            if (to != null ? !to.equals(that.to) : that.to != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = from != null ? from.hashCode() : 0;
            result = 31 * result + (to != null ? to.hashCode() : 0);
            result = 31 * result + (dependency != null ? dependency.hashCode() : 0);
            return result;
        }
    }

}
