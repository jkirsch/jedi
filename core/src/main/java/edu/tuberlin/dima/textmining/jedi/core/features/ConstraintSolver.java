package edu.tuberlin.dima.textmining.jedi.core.features;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.common.primitives.Doubles;
import edu.tuberlin.dima.textmining.jedi.core.model.DetectedRelation;
import edu.tuberlin.dima.textmining.jedi.core.model.Edge;
import edu.tuberlin.dima.textmining.jedi.core.util.PrintCollector;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.uima.jcas.tcas.Annotation;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.Multigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.Arithmetic;
import solver.constraints.Constraint;
import solver.constraints.LogicalConstraintFactory;
import solver.constraints.Operator;
import solver.search.loop.monitors.IMonitorSolution;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.ESat;

import javax.annotation.Nullable;
import java.util.*;

/**
 * The main class.
 */
public class ConstraintSolver<T> {

    final Set<String> types;

    final Table<String, String, String> rel;

    Multigraph<T, Edge> graph;

    private static final Logger LOG = LoggerFactory.getLogger(ConstraintSolver.class);

    private final Predicate<T> orphanedVerticesPredicate = new Predicate<T>() {
        @Override
        public boolean apply(@Nullable T input) {
            return graph.degreeOf(input) == 0;
        }
    };

    private ConstraintSolver(Set<String> types, Table<String, String, String> rel, Multigraph<T, Edge> graph) {
        this.types = types;
        this.rel = rel;
        this.graph = graph;

        // Helper code that prints out code that could be important for testing
        /*
        for (Edge edge : graph.edgeSet()) {
            T edgeSource = graph.getEdgeSource(edge);
            T edgeTarget = graph.getEdgeTarget(edge);

            System.out.printf(Locale.ENGLISH, "{\"%s-%d\", \"%s-%d\", \"%s\", \"%s\", \"%s\", \"%s\", \"%f\"},\n",
                    transformToString(edgeSource),
                    edgeSource.hashCode(),
                    transformToString(edgeTarget),
                    edgeTarget.hashCode(),
                    edge.getRelation(),
                    edge.getPattern(),
                    edge.domain,
                    edge.range,
                    edge.getScore());
        } */

    }

    public static class ConstraintSolverBuilder<T> {

        final Set<String> types = new TreeSet<>();

        final Table<String, String, String> rel = HashBasedTable.create();

        final Multigraph<T, Edge> graph = new Multigraph<>(Edge.class);

        int edgeCounter = 0;

        public ConstraintSolverBuilder<T> add(T left, T right, String pattern, String relation, String domain, String range, double score, double entropy, int count) {
            return this.add(left, right, pattern, relation, domain, range, score, entropy, count, false);
        }

        public ConstraintSolverBuilder<T> add(T left, T right, String pattern, String relation, String domain, String range, double score, double entropy, int count, boolean fixedEdge) {
            types.add(domain);
            types.add(range);

            rel.put(relation, domain, range);

            // detect loops
            if (!left.equals(right)) {
                if (!graph.containsVertex(left)) {
                    graph.addVertex(left);
                }

                if (!graph.containsVertex(right)) {
                    graph.addVertex(right);
                }

                boolean added = graph.addEdge(left, right, new Edge(++edgeCounter, relation, pattern, domain, range, score, entropy, count, fixedEdge));
                if(!added) {
                    LOG.error("Did not add edge! " + Joiner.on(",").join(edgeCounter, relation, pattern, domain, range, score, entropy, count, fixedEdge));
                }
            } else {
                LOG.error(String.format("Adding %s -> %s (%s) would create a loop - skipping", transformToString(left), transformToString(right), relation));
            }

            return this;
        }

        public ConstraintSolver<T> build() {
            return new ConstraintSolver<>(types, rel, graph);
        }

    }

    /**
     * Predicate that filters a list of sets by their size.
     */
    private class SetSizePredicate<K> implements Predicate<Set<K>> {

        private int size;

        private SetSizePredicate(int size) {
            this.size = size;
        }

        @Override
        public boolean apply(@Nullable Set<K> input) {
            return input.size() == size;
        }
    }

    /**
     * Computes a score for the found detectedRelation
     *
     * @param detectedRelation the current detectedRelation
     * @return the score
     */
    private float score(List<DetectedRelation<T>> detectedRelation) {
        float sums = 0;
        for (DetectedRelation<T> s : detectedRelation) {
            sums += s.edge.score;//+ graph.degreeOf(s.getLeft()) + graph.degreeOf(s.getRight());
        }
        // prevent divide by 0
        if (detectedRelation.size() > 0) {
            sums = sums / (float) (detectedRelation.size());
        }

        ConnectivityInspector<T, Edge> connectivityInspector = new ConnectivityInspector<>(graph);

        List<Set<T>> connectedComponents = connectivityInspector.connectedSets();

        return sums + detectedRelation.size()/ (float) connectedComponents.size();
    }

    private final Comparator<List<DetectedRelation<T>>> solutionComparator = (o1, o2) -> {

        // compare size of detectedRelation chain as well
        return ComparisonChain.start()/*.compare(o1.size(), o2.size())*/.compare(score(o1), score(o2)).result();
    };


    private class DistinctEdge {
        final Edge edge;

        final T left;
        final T right;

        DistinctEdge(Edge edge, T left, T right) {
            this.edge = edge;
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DistinctEdge that = (DistinctEdge) o;

            if (!left.equals(that.left)) return false;
            if (!edge.getPattern().equals(that.edge.getPattern())) return false;
            if (!right.equals(that.right)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = left.hashCode();
            result = 31 * result + right.hashCode();
            result = 31 * result + edge.getPattern().hashCode();
            return result;
        }

    }


    private void pruneGraph(PrintCollector printCollector) {


        Set<Edge> pruneEdges = Sets.newHashSet();

        // get the edges for each vertex
        for (T vertex : graph.vertexSet()) {

            Set<Edge> edges = graph.edgesOf(vertex);

            // now group into incoming and outgoing

            List<Edge> incoming = Lists.newArrayList();
            List<Edge> outgoing = Lists.newArrayList();

            for (Edge edge : edges) {
                T edgeSource = graph.getEdgeSource(edge);

                if (vertex.equals(edgeSource)) {
                    // source -> target
                    // outgoing
                    outgoing.add(edge);
                } else {
                    // target -> source
                    // incoming
                    incoming.add(edge);
                }
            }

            List<Edge> check = null;

            if (incoming.size() == 0) {
                check = outgoing;
            } else if (outgoing.size() == 0) {
                check = incoming;
            }

            if (check != null && check.size() > 0) {
                // now check if the source and target is 1

                Set<T> source = Sets.newHashSet();
                Set<T> target = Sets.newHashSet();

                for (Edge edge : check) {
                    source.add(graph.getEdgeSource(edge));
                    target.add(graph.getEdgeTarget(edge));
                }
                // not interested in the current vertex
                source.remove(vertex);
                target.remove(vertex);

                if (source.size() + target.size() == 1) {
                    // candidate
                    // we could fold  edges
                    Edge topEdge = Iterables.getFirst(edges, null);

                    String lastEdgeCompare = source.size() == 1 ? topEdge.getDomain() : topEdge.getRange();

                    // assume that all edges are sorted
                    // main types come first

                    for (Edge edge : Iterables.skip(edges, 1)) {
                        // respect ordering
                        String toCompare = source.size() == 1 ? edge.getDomain() : edge.getRange();
                        // we can remove the edge, if the domain type is similar
                        if(toCompare.equals(lastEdgeCompare)) {
                            // dos not add anything new
                            pruneEdges.add(edge);
                        } else {
                            // we have a new type
                            lastEdgeCompare = toCompare;
                        }

//                        }
                    }
                }

            }
        }

        printCollector.print("Pruning edges from graph : " + pruneEdges.size());
        graph.removeAllEdges(pruneEdges);
    }

    public List<DetectedRelation<T>> solve(PrintCollector printCollector) {

        List<DetectedRelation<T>> detectedRelations = Lists.newArrayList();

        // prune graph
        pruneGraph(printCollector);

        ConnectivityInspector<T, Edge> connectivityInspector = new ConnectivityInspector<>(graph);

        List<Set<T>> connectedComponents = connectivityInspector.connectedSets();

/*        for (Edge edge : graph.edgeSet()) {
            T edgeSource = graph.getEdgeSource(edge);
            T edgeTarget = graph.getEdgeTarget(edge);
            System.out.println(
                    Joiner.on(" ").join(transformToString(edgeSource), ((Annotation)edgeSource).getBegin(), ((Annotation)edgeSource).getEnd(), edgeSource.hashCode()) + "   ->  " +
                            Joiner.on(" ").join(((Annotation)edgeTarget).getCoveredText(),((Annotation)edgeTarget).getBegin(), ((Annotation)edgeTarget).getEnd(), edgeTarget.hashCode()));
        } */

        // solve each connected component separately

        // save the current state of the graph to revert
        final UndirectedGraph<T, Edge> restored = new Multigraph<>(Edge.class);
        Graphs.addGraph(restored, graph);

        printCollector.print(String.format("Graph has %2d components", connectedComponents.size()));
        for (Set<T> connectedComponent : connectedComponents) {

            // remove all vertices that are not part of the connected component
            printCollector.print(String.format("Solving connected component with %d vertices", connectedComponent.size()));
            //printCollector.print(Iterables.toString(Iterables.transform(connectedComponent, vertexToString)));

            // determine which nodes need to go
            HashSet<T> difference = Sets.newHashSet(Sets.difference(graph.vertexSet(), connectedComponent));
            graph.removeAllVertices(difference);

            detectedRelations.addAll(solveConnectedComponent(printCollector, connectedComponent));

            // restore graph
            Graphs.addGraph(graph, restored);
        }

        return detectedRelations;
    }


    private List<DetectedRelation<T>> solveConnectedComponent(PrintCollector printCollector, Set<T> connectedComponents) {


        // save the current state of the graph to revert
        final UndirectedGraph<T, Edge> restored = new Multigraph<>(Edge.class);
        Graphs.addGraph(restored, graph);

        if(graph.vertexSet().size() == 2) {
            // this means there is only one connection ..
            // remove all edges that are not fixed
            ArrayList<Edge> edges = Lists.newArrayList(graph.edgeSet());
            Iterable<Edge> filter = Iterables.filter(edges, input -> !input.isFixed());
            for (Edge edge : Iterables.skip(filter, 1)) {
                graph.removeEdge(edge);
            }
        }


        // build a new set of edges .. that have a different equals

        // if there is an edge which is fixed between two Entities and a Pattern ..
        // that means all edges with the similar pattern are fixed ....

        Set<DistinctEdge> distinctEdgesSet = new HashSet<>();

        for (Edge edge : graph.edgeSet()) {
            T edgeSource = graph.getEdgeSource(edge);
            T edgeTarget = graph.getEdgeTarget(edge);
            DistinctEdge distinctEdge = new DistinctEdge(edge, edgeSource, edgeTarget);
            if (true) {
                distinctEdgesSet.add(distinctEdge);
            }
        }

       /* for (DistinctEdge distinctEdge : distinctEdgesSet) {
            System.out.printf("%s -> %s | %s\n", transformToString(distinctEdge.left), transformToString(distinctEdge.right), distinctEdge.edge.getPattern());
        }  */

        Set<Edge> distinctEdges = Sets.newHashSet();
        for (DistinctEdge distinctEdge : distinctEdgesSet) {
            distinctEdges.add(distinctEdge.edge);
        }


        final Set<Set<Edge>> powerSet;

        if (distinctEdges.size() >= 30) {
            LOG.info("Graph contains {} elements", distinctEdges.size());
            // this set is too large to enumerate every possible instance
            // build a set which
            powerSet = Sets.newLinkedHashSet();
            powerSet.add(Collections.<Edge>emptySet());
            // generate set with 1 object missing
            for (Edge edge : distinctEdges) {
                powerSet.add(Sets.newHashSet(edge));
            }
            // generate set with any two combinations
            List<Edge> initialEdgeList = Lists.newArrayList(distinctEdges);
            for (int i = 0; i < initialEdgeList.size(); i++) {
                for (int j = i + 1; j < initialEdgeList.size(); j++) {
                      powerSet.add(Sets.newHashSet(initialEdgeList.get(i), initialEdgeList.get(j)));
                }
            }
        } else {
            powerSet = Sets.powerSet(distinctEdges);
        }


        final int size = graph.vertexSet().size();


        List<DetectedRelation<T>> bestDetectedRelation = Lists.newArrayList();
        float bestScore = 0;
        // make sure we check at least 1,2 and 3 layers down
        int layersSuccess = 0;
        boolean lastLayerSuccess = false;
        int solverReachedLimit = 0;
        for (int i = 0; i < size; i++) {
            boolean thisLayerSuccess = false;

            printCollector.print("Testing constraints with  " + i + " edge(s) removed");
            for (Set<Edge> removeCandidates : Sets.filter(powerSet, new SetSizePredicate<Edge>(i))) {

                //printCollector.print("Removing: " + Iterables.toString(Iterables.transform(removeCandidates, vertexToString)));

                // remove all edges between two node pairs
                Set<Edge> edges = Sets.newHashSet();

                for (Edge removeCandidate : removeCandidates) {

                    // get all edges between which are connected by a similar edge
                    T edgeSource = graph.getEdgeSource(removeCandidate);
                    T edgeTarget = graph.getEdgeTarget(removeCandidate);

                    // Removing
                    //printCollector.print("Removing : " + String.format("%s -> %s | %s", transformToString(edgeSource), transformToString(edgeTarget), removeCandidate.getPattern()));

                    for (Edge edge : graph.getAllEdges(edgeSource, edgeTarget)) {
                        // get all edges which are part of the same pattern
                        if (edgeSource.equals(graph.getEdgeSource(edge)) && edgeTarget.equals(graph.getEdgeTarget(edge)) && edge.getPattern().equals(removeCandidate.getPattern())) {
                            edges.add(edge);
                        }
                    }

                }

                //printCollector.print(Strings.repeat("-",50));

                // also if no complete detectedRelation has been found in the complete layer remove unspecific edges  - start with a small number
/*                if (i > 1) {
                    ImmutableSet<Edge> edgeCopy = ImmutableSet.copyOf(graph.edgeSet());
                    for (Edge edge : edgeCopy) {
                        if (edge.score < (0.02 * i)) {
                            graph.removeEdge(edge);
                        }
                    }
                }      */

                // no need to check if we are not removing anything and not inspecting the full graph
                if (edges.size() == 0 && i > 0) continue;

              if(i > 3 && !lastLayerSuccess) {
                  // remove edges if the complete graph did not yield anything yet
                  ArrayList<Edge> allEdges = Lists.newArrayList(graph.edgeSet());


                  // TODO .. remove more intelligently
                  // IDEA: count distinct entropy and filter based on this list
                  TreeSet<Double> entropy = new TreeSet<>();
                  for (Edge edge : allEdges) {
                      entropy.add(edge.entropy);
                  }

                  TreeSet<Integer> counts = new TreeSet<>();
                  for (Edge edge : allEdges) {
                      counts.add(edge.count);
                  }

                  int iterationCutoff = 40;

                  int cutOffPositionEntropy = (int) Math.floor(entropy.size() / (float) 100 * Math.max( 100 - (i-1) * iterationCutoff,2));
                  int cutOffPositionCounts = (int) Math.floor(counts.size() / (float) 100 * Math.max( 100 - (i-1) * 10,2));
                  // remove 10% 20% 30%
                  // i = 2 ... 80%
                  // i = 3 ... 60 %

                  double cutOffEntropy = Iterables.get(entropy, cutOffPositionEntropy, 1d);
                  double cutOffCount = Iterables.get(counts, cutOffPositionCounts, 20);

                  for (Edge edge : allEdges) {
                      if(edge.entropy >= cutOffEntropy && edge.count < cutOffCount && edge.getScore() < 0.9) {
                          graph.removeEdge(edge);
                      } //else if(edge.getScore() < (0.5 + ((i-2)*0.1f)) && edge.count > 4000) {
                          // hard cutoff
//                          graph.removeEdge(edge);
//                     }
                  }

                  pruneGraph(new PrintCollector(false));

                  //graph.removeAllEdges(Lists.newArrayList(Iterables.skip(allEdges, Math.max((10- (i- 1)*4)*tenPercent,5))));

                }

                if(i <= 3) { graph.removeAllEdges(edges); }
                //graph.removeAllVertices(removeCandidates);

                // now check if we have orphaned any other vertices
                final Iterable<T> filter = Iterables.filter(graph.vertexSet(), orphanedVerticesPredicate);

                final ArrayList<T> orphanedVertices = Lists.newArrayList(filter);
                if (orphanedVertices != null && orphanedVertices.size() > 0) {
                    //printCollector.print("Removing orphaned: " + Iterables.toString(Iterables.transform(orphanedVertices, vertexToString)));
                    graph.removeAllVertices(orphanedVertices);
                }
                final InternalSolveResult<T> internalSolveResult = internalSolve(printCollector, false);
                List<DetectedRelation<T>> candidateDetectedRelation = internalSolveResult.detectedRelation;
                if (internalSolveResult.solverReachedLimit) {
                    solverReachedLimit++;
                }

                if (candidateDetectedRelation != null && candidateDetectedRelation.size() > 0) {
                    // is candidate better than bestDetectedRelation ?

                    float score = score(candidateDetectedRelation);
                    if (score > bestScore) {
                        bestScore = score;
                        bestDetectedRelation = candidateDetectedRelation;
                        thisLayerSuccess = true;
                    }

/*                    final int compare = Ordering.withOptions(solutionComparator).reverse().compare(bestDetectedRelation, candidateDetectedRelation);
                    bestDetectedRelation = compare < 0 ? bestDetectedRelation : candidateDetectedRelation;
                    thisLayerSuccess = true;*/
                    // shortcut if limit reached .. don't look further
                    // if the solver hit the search limit more than x times ..
                    if (solverReachedLimit > 3) {
                        lastLayerSuccess = true;
                        thisLayerSuccess = true;
                        break;
                    }

                }
                // restore graph
                Graphs.addGraph(graph, restored);

                // only check
                if(i > 3) { break; }
            }
            // TODO now iterate over all solutions that are on the same size level and one down (?)
            if (lastLayerSuccess || (thisLayerSuccess && i >= 4) || ( i >= 5)) break;
            lastLayerSuccess = thisLayerSuccess;//layersSuccess==3;
            //layersSuccess = thisLayerSuccess;//?1:0;
        }

        if (bestDetectedRelation.size() == 0) {
            printCollector.print("No detectedRelation found");
        }
        // the best detectedRelation
        return bestDetectedRelation;
    }


    private static final class InternalSolveResult<T> {
        List<DetectedRelation<T>> detectedRelation;
        boolean solverReachedLimit;

        private InternalSolveResult(List<DetectedRelation<T>> detectedRelation, boolean solverReachedLimit) {
            this.detectedRelation = detectedRelation;
            this.solverReachedLimit = solverReachedLimit;
        }
    }

    /**
     * Empty DetectedRelation list
     */
    private final List<DetectedRelation<T>> emptyList = Lists.<DetectedRelation<T>>newArrayList();

    private InternalSolveResult<T> internalSolve(final PrintCollector printCollector, final boolean initial) {

        final BiMap<Integer, String> typeMapping = HashBiMap.create();

        int i = 0;
        for (String type : types) {
            typeMapping.put(i, type);
            i++;
        }

        final Solver solver = new Solver("Type inference");
        // limit the time for solving - so we are not reaching deadlocks
        SearchMonitorFactory.limitThreadTime(solver, "5s");

        final Map<T, IntVar> encoding = Maps.newHashMap();

        final Set<T> vertexSet = graph.vertexSet();

        for (final T vertex : vertexSet) {
            final Set<Edge> edgeSet = graph.edgesOf(vertex);

            final Iterable<String> domains = Iterables.transform(edgeSet, new Function<Edge, String>() {
                @Nullable
                @Override
                public String apply(@Nullable Edge input) {
                    return graph.getEdgeSource(input).equals(vertex) ? input.domain : input.range;
                }
            });

            final Function<String, Integer> getDomainTypeID = new Function<String, Integer>() {
                @Nullable
                @Override
                public Integer apply(@Nullable String input) {
                    return typeMapping.inverse().get(input);
                }
            };

            final Iterable<Integer> domainTypeIDs = Iterables.transform(ImmutableSet.copyOf(domains), getDomainTypeID);

            final Integer[] integers = Iterables.toArray(domainTypeIDs, Integer.class);

            // vertex = IntVariable
            if (integers.length > 0) {
                final int[] values = ArrayUtils.toPrimitive(integers);

                Arrays.sort(values);

                IntVar var = VariableFactory.enumerated(transformToString(vertex),
                        values, solver);
                encoding.put(vertex, var);
            }

        }

        // for all edges between two
        // all vertexes
        final Set<T> ts = graph.vertexSet();
        final Set<T> other = Sets.newHashSet(graph.vertexSet());
        for (final T source : ts) {
            other.remove(source);
            for (final T target : other) {
                final Set<Edge> rawEdges = graph.getAllEdges(source, target);

                final List<Edge> edges = Lists.newArrayList(rawEdges);
                Collections.sort(edges, Ordering.from(new EdgeComparator()).reverse());


                // constraint that any of these edges must be true
                if (edges.size() > 0) {
                    if (initial) {
                        printCollector.print(Strings.padStart("OR'ING", 20, '-'));
                    }
                    final String[] lastRelation = {""};
                    // if we are very confident about an edge tie the value down
                    solver.post(
                            LogicalConstraintFactory.or(
                                    Iterables.toArray(Iterables.transform(edges, new Function<Edge, Constraint>() {
                                        @Nullable
                                        @Override
                                        public Constraint apply(@Nullable Edge edge) {
                                            // ensure we look at the correct types
                                            final boolean isSourceOrdering = graph.getEdgeSource(edge).equals(source);

                                            if (!lastRelation[0].equals(edge.relation)) {
                                                if (initial)
                                                    printCollector.print(String.format("Relation %s \t %f", edge.relation, edge.score));
                                                lastRelation[0] = edge.relation;
                                            }
                                            if (initial) printCollector.print(String.format("%20s \t %20s",
                                                    Joiner.on("=").join(transformToString(source), isSourceOrdering ? edge.domain : edge.range),
                                                    Joiner.on("=").join(transformToString(target), isSourceOrdering ? edge.range : edge.domain)));

                                            return LogicalConstraintFactory.and(
                                                    new Arithmetic(
                                                            encoding.get(source),
                                                            Operator.EQ,
                                                            typeMapping.inverse().get(isSourceOrdering ? edge.domain : edge.range)
                                                    ),
                                                    new Arithmetic(
                                                            encoding.get(target),
                                                            Operator.EQ,
                                                            typeMapping.inverse().get(isSourceOrdering ? edge.range : edge.domain)
                                                    )
                                            );
                                        }
                                    }), Constraint.class)
                            ));
                }
            }

        }


        final List<List<DetectedRelation<T>>> solutions = Lists.newArrayList();

        solver.plugMonitor(new IMonitorSolution() {
            @Override
            public void onSolution() {
                if (!solver.hasReachedLimit()) {
                    List<DetectedRelation<T>> generateDetectedRelation = generateSolution(typeMapping, encoding);
                    solutions.add(generateDetectedRelation);
                }
            }
        });


        // find the first 50 detectedRelation
        solver.findSolution();
        int counter = 0;

        while (counter < 100 && !solver.hasReachedLimit() && solver.nextSolution()) {
            counter++;
        }

        if (solver.isFeasible().equals(ESat.FALSE)) {
            // no detectedRelation
            new InternalSolveResult<T>(emptyList, solver.hasReachedLimit());
        }

        // score all solutions

        Collections.sort(solutions, Ordering.from(solutionComparator).reverse());

        final List<DetectedRelation<T>> detectedRelation = Iterables.getFirst(solutions, null);

        return new InternalSolveResult<>(detectedRelation, solver.hasReachedLimit());
    }

    private List<DetectedRelation<T>> generateSolution(final BiMap<Integer, String> typeMapping, Map<T, IntVar> encoding) {

        List<DetectedRelation<T>> detectedRelations = Lists.newArrayList();

        Set<T> sources = graph.vertexSet();
        HashSet<T> targets = Sets.newHashSet(graph.vertexSet());

        Ordering<Edge> ordering = Ordering.natural().onResultOf(new Function<Edge, Comparable>() {
            @Nullable
            @Override
            public Comparable apply(@Nullable Edge input) {
                return input.score;
            }
        }).reverse();

        for (final T source : sources) {
            final IntVar sourceBinding = encoding.get(source);
            targets.remove(source);
            for (final T target : targets) {
                final IntVar targetBinding = encoding.get(target);
                final Set<Edge> edges = graph.getAllEdges(source, target);
                // find the first satisfying edge
                final int sourceBindingValue = sourceBinding.getValue();
                final int targetBindingValue = targetBinding.getValue();

                final Edge qualifying = Iterables.find(ordering.sortedCopy(edges), new Predicate<Edge>() {
                    @Override
                    public boolean apply(@Nullable Edge input) {

                        // ensure we look at the correct types
                        final boolean isSourceOrdering = graph.getEdgeSource(input).equals(source);
                        // check if the edge is one that we like
                        return sourceBindingValue == typeMapping.inverse().get(isSourceOrdering ? input.domain : input.range) &&
                                targetBindingValue == typeMapping.inverse().get(isSourceOrdering ? input.range : input.domain);
                    }
                }, null);
                if (qualifying != null) {
                    // check if we need to flip the edge
                    final boolean isSourceOrdering = graph.getEdgeSource(qualifying).equals(source);
                    detectedRelations.add(new DetectedRelation<>(isSourceOrdering ? source : target, isSourceOrdering ? target : source, qualifying));
                }
            }
        }

        return detectedRelations;
    }

    private final static class EdgeComparator implements Comparator<Edge> {

        @Override
        public int compare(Edge o1, Edge o2) {
            return Doubles.compare(o1.score, o2.score);
        }
    }

    private static <T> String transformToString(T node) {
        if (node instanceof Annotation) {
            return ((Annotation) node).getCoveredText();
        } else {
            return node.toString();
        }
    }

    private final Function<T, String> vertexToString = new Function<T, String>() {
        @Nullable
        @Override
        public String apply(@Nullable T input) {
            return transformToString(input) + " " + input.hashCode();
        }
    };
}
