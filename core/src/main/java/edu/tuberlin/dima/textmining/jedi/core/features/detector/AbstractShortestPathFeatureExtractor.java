package edu.tuberlin.dima.textmining.jedi.core.features.detector;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.O;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import edu.tuberlin.dima.textmining.jedi.core.util.JCommanderClassConverter;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public abstract class AbstractShortestPathFeatureExtractor {

    @Parameter(names = {"-lemmatize"}, description = "Lemmatize the pattern to remove times", required = false)
    private boolean lemmatize = false;

    @Parameter(names = {"-pickupSimilar"}, description = "If set, the token is expanded left and right with tokens of the same selectionType", required = false)
    private boolean pickupSimilar = false;

    @Parameter(names = {"-collapseMentions"}, description = "If set, similar mentions are collapsed", required = false)
    private boolean collapseMentions = false;

    @Parameter(names = {"-selectionType"}, description = "Class of selection Type (e.g. Named Entity)", required = true, converter = JCommanderClassConverter.class)
    private Class<? extends Annotation> selectionType;

    @Parameter(names = {"-additionalSelectionType"}, description = "AdditionalClass of selection Type (e.g. Named Entity)", required = false, converter = JCommanderClassConverter.class)
    private Class<? extends Annotation> additionalSelectionType;

    @Parameter(names = {"-resolveCoreferences"}, description = "Resolve Co-references for a given token", required = false)
    private boolean resolveCoreferences = false;

    private JCas jCas;

	@Parameter(names = {"-name"}, description = "The name of the extractor", required = true)
	private String name;

    public AbstractShortestPathFeatureExtractor(String options) throws UIMAException {

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
            throw new RuntimeException("Error initializing " + this.getClass().getSimpleName(), e);
        }

    }

    public final List<FoundFeature<Annotation>> exec(JCas xmlDocument) throws IOException {
        if (xmlDocument == null) {
            return null;
        }

        setjCas(xmlDocument);

        // choose a feature extraction strategy
        // 1. insert dependencies into a graph and extract shortest path
        UndirectedGraph<Token, DependencyEdge> graph = makeDependencyGraph();
        if (graph == null || graph.vertexSet().size() == 0) {
            return Lists.newArrayList();
        }

        List<FoundFeature<Annotation>> shortestPaths = getShortestPaths(graph);

        return postProcess(shortestPaths, graph);
    }

    /**
     * Implementation of the pattern finder.
     *
     * @param graph input graph
     * @return list of features
     */
    abstract public List<FoundFeature<Annotation>> getShortestPaths(final UndirectedGraph<Token, DependencyEdge> graph);

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

	public String getName() {
		return name;
	}

	public JCas getjCas() {
        return jCas;
    }

    public void setjCas(JCas jCas) {
        this.jCas = jCas;
    }

    public Class<? extends Annotation> getSelectionType() {
        return selectionType;
    }

    public Class<? extends Annotation> getAdditionalSelectionType() {
        return additionalSelectionType;
    }

    public boolean isResolveCoreferences() {
        return resolveCoreferences;
    }

    public boolean isLemmatize() {
        return lemmatize;
    }

    public boolean isPickupSimilar() {
        return pickupSimilar;
    }

    public boolean isCollapseMentions() {
        return collapseMentions;
    }

    /**
     * Extract the head of a named entity
     * - simplified head finding rule
     * - the token that is the
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
                if (!tokens.contains(dependencyEdge.getFrom())) {
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

        if(candidate == null) {
            return Iterables.getFirst(tokens, null);
        }

        return candidate;

    }

    protected final List<FoundFeature<Annotation>> postProcess(List<FoundFeature<Annotation>> dataBag, UndirectedGraph<Token, DependencyEdge> graph) {

        List<FoundFeature<Annotation>> additional = Lists.newArrayList();
        final Iterator<FoundFeature<Annotation>> postProcessor = dataBag.iterator();
        while (postProcessor.hasNext()) {
            final FoundFeature<Annotation> annotationFoundFeature = postProcessor.next();
            String pattern = annotationFoundFeature.getPattern();

            Matcher ageOfMatcher1 = ageOf.matcher(annotationFoundFeature.getEntity1().getCoveredText());
            Matcher ageOfMatcher2 = ageOf.matcher(annotationFoundFeature.getEntity2().getCoveredText());

            if(ageOfMatcher1.find() || ageOfMatcher2.find()) {
                postProcessor.remove();
                continue;
            }

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
                        return input.getFrom().equals(namedEntityHead) && ("parataxis".equals(input.getDependency()) || ("punct".equals(input.getDependency()) && "(".equals(input.getTo().getCoveredText())));
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
                        String to = isLemmatize() ? dependencyEdge.get().getTo().getLemma().getValue():dependencyEdge.get().getTo().getCoveredText();
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
                        String to = isLemmatize() ? afterBracket.getLemma().getValue():afterBracket.getCoveredText();
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
    Pattern dateMatcher = Pattern.compile("(?i)^(-|\"|“|”|'|„|“|‘|’|«|»|‹|›|¡|¿||who|[\\d]+|year| years|post|life|age|January|February|March|April|May|June|July|August|September|October|November|December)$");
    Pattern deathMatcherPattern = Pattern.compile("\\[X] die(,|\\s|on )+(January|February|March|April|May|June|July|August|September|October|November|December){0,1}[\\s\\d]* (in|at) \\[Y\\] \\[.*\\d+-prep-\\d+,\\d+-pobj-\\d+\\]");
    static Pattern birthMatcher = Pattern.compile("\\[X\\] bear(,|\\s|on |in )+(January|February|March|April|May|June|July|August|September|October|November|December){0,1}[,\\s\\d]* (to|in|at) \\[Y\\] \\[(\\d)*-(\\w*)-(\\d)*.*\\d+-(\\w+)-\\d+\\]");

    // Günter Theodor Netzer (born 14 September 1944 in Mönchengladbach)
    // Parataxis check
    static Pattern parataxisCheckPattern = Pattern.compile("\\[X\\] (January|February|March|April|May|June|July|August|September|October|November|December){0,1}[\\s\\d]* (in|at) \\[Y\\] \\[0-appos-1,1-prep-2,2-pobj-3\\]");
    static Pattern parataxisCheckPattern2 = Pattern.compile("\\[X\\] \\[Y\\] \\[0-appos-1\\]");

    Pattern conjunctionAtTheEnd = Pattern.compile("\\[X\\]([\\w\\s]+) (\\w+) \\[Y\\] \\[(.*),\\d+-conj-\\d+\\]");

    Pattern ageOf = Pattern.compile("age(d)*( of)* \\d+");

}
