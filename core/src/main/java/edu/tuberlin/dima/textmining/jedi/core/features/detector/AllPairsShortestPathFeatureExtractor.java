package edu.tuberlin.dima.textmining.jedi.core.features.detector;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.google.common.primitives.Ints;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.CARD;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.O;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
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

/**
 * Date: 14.11.2014
 * Time: 21:05
 *
 * @author Johannes Kirschnick
 */
public class AllPairsShortestPathFeatureExtractor extends AbstractShortestPathFeatureExtractor {

    public AllPairsShortestPathFeatureExtractor(String options) throws UIMAException {
        super(options);
    }

    @Override
    public List<FoundFeature<Annotation>> getShortestPaths(UndirectedGraph<Token, DependencyEdge> graph) {

        List<FoundFeature<Annotation>> dataBag = Lists.newArrayList();

        // pick up entities
        List<Annotation> namedEntities = Lists.newArrayList(JCasUtil.select(getjCas(), getSelectionType()));
        if(getAdditionalSelectionType() != null) {
            // add additional selection types
            namedEntities.addAll(JCasUtil.select(getjCas(), getAdditionalSelectionType()));
        }

        // break
        if (namedEntities == null || namedEntities.size() == 1) {
            return dataBag;
        }

        Set<CandidateEntity> candidateEntities = Sets.newHashSet();

        // now iterate over all entities and expand them
        for (Annotation namedEntity : namedEntities) {
            List<Token> subTree = getSubTree(namedEntity, graph);
            Token head = getNamedEntityHead(namedEntity, graph);
            if(graph.containsVertex(head)) {
                candidateEntities.add(new CandidateEntity(head, subTree));
            }

        }

        DirectedGraph<Annotation, DefaultEdge> coref = new SimpleDirectedGraph<>(DefaultEdge.class);
        if (isResolveCoreferences()) {

            for (CoreferenceChain coreferenceChain : JCasUtil.select(getjCas(), CoreferenceChain.class)) {

                final CoreferenceLink first = coreferenceChain.getFirst();

                if(coreferenceChain.links().size() < 2) {
                    continue;
                }

                // this is the token ...
                final Token entityHead = getNamedEntityHead(first, graph);

                // if we don't have the specific target annotation as a marker, just skip
                // this will prevent linking to pronouns that we are for example not interested in
                // check if we just have a number
                if (entityHead == null || entityHead.getPos().getClass().equals(CARD.class) || entityHead.getPos().getClass().equals(PUNC.class)) {
                    continue;
                }

                // get the actual named entity, if it is already in the set - we don't want duplicates
                final Optional<CandidateEntity> targetAnnotation = Iterables.tryFind(candidateEntities, new Predicate<CandidateEntity>() {
                    @Override
                    public boolean apply(@Nullable CandidateEntity input) {
                        return entityHead != null && (input.head.getBegin() <= entityHead.getBegin() && input.head.getEnd() >= entityHead.getEnd());
                    }
                });

                if(!targetAnnotation.isPresent()) {
                    continue;
                }

                CandidateEntity left = targetAnnotation.get();


                if(first.getCoveredText().length() < left.namedEntity.getCoveredText().length()) {
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
                        final Optional<CandidateEntity> targetRightAnnotation = Iterables.tryFind(candidateEntities, new Predicate<CandidateEntity>() {
                            @Override
                            public boolean apply(@Nullable CandidateEntity input) {
                                return rightToken != null && (input.namedEntity.getBegin() <= rightToken.getBegin() && input.namedEntity.getEnd() >= rightToken.getEnd());
                            }
                        });



                        final CandidateEntity right = targetRightAnnotation.or(new CandidateEntity(rightToken, getSubTree(rightToken, graph)));


                        // check that the annotation is really covering the underlying named entity
                        // but we also allow "HE SHE ..."

                        // we don't want
                        // Mention is way longer than the underlying annotation

                        if(last.getCoveredText().length() > right.namedEntity.getCoveredText().length() || right.namedEntity.getCoveredText().endsWith("degree")) {
                            // we don't want to widen the scope if the mention is way shorter
                            continue;
                        }


                        if (left != right && left != null && (StringUtils.startsWithIgnoreCase(left.namedEntity.getCoveredText(), "mount") || !StringUtils.startsWithIgnoreCase(right.namedEntity.getCoveredText(), "mount"))) {
                            coref.addVertex(left.namedEntity);
                            coref.addVertex(right.namedEntity);
                            coref.addEdge(left.namedEntity, right.namedEntity);

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
                            final boolean any = Iterables.any(candidateEntities, new Predicate<CandidateEntity>() {
                                @Override
                                public boolean apply(@Nullable CandidateEntity existing) {
                                    return existing.head.getBegin() <= right.head.getBegin() && existing.head.getEnd() >= right.head.getEnd();
                                }
                            });

                            if(!any && !(right.head.getCoveredText().equals("his") || right.head.getCoveredText().equals("her")) ) {
                                candidateEntities.add(right);
                            }
                        }
                    }
                }
            }

        }


        // check if one entity is an apposition of another
        // e.g. Bell, a telecommunication company,

        List<CandidateEntity> appositionCheckList = Lists.newArrayList(candidateEntities);
        for (int i = 0; i < appositionCheckList.size(); i++) {

            CandidateEntity left = appositionCheckList.get(i);

            for (int j = i + 1; j < appositionCheckList.size(); j++) {

                CandidateEntity right = appositionCheckList.get(j);

                DependencyEdge edge = graph.getEdge(left.head, right.head);

                if(edge != null && edge.getDependency().equals("appos")) {

                    // add coreference

                    if(coref.containsEdge(left.namedEntity, right.namedEntity) || coref.containsEdge(left.namedEntity, right.namedEntity)) {
                        continue;
                    }
                    coref.addVertex(left.namedEntity);
                    coref.addVertex(right.namedEntity);


                    // adhere to the ordering of the dependency edge
                    // the coref is basically flipped
                    // but the coreference graph is yet again flipped :)
                    if(edge.getFrom().equals(left.head)) {
                        coref.addEdge(left.namedEntity, right.namedEntity);
                    } else {
                        coref.addEdge(right.namedEntity, left.namedEntity);
                    }

                }

            }
        }


        // order candidate entities to make the search stable

        // get all pairs of entities omitting incestuous and duplicate pairs
        List<EntityPair> entityPairs = Lists.newArrayList();

        Iterator<CandidateEntity> iterator = candidateEntities.iterator();
        while (iterator.hasNext()) {
            CandidateEntity entity1 = iterator.next();
            iterator.remove();

            for (CandidateEntity entity2 : candidateEntities) {

                // check that the annotation1 is not contained in entity2
                if (!(entity2.list.contains(entity1.head) || entity1.list.contains(entity2.head))) {
                    if ((entity1.head.getBegin() < entity2.head.getBegin())) {
                        entityPairs.add(new EntityPair(entity1, entity2));
                    } else {
                        entityPairs.add(new EntityPair(entity2, entity1));
                    }
                }
            }
        }
        Collections.sort(entityPairs, Ordering.from(new Comparator<EntityPair>() {
            @Override
            public int compare(EntityPair o1, EntityPair o2) {
                return ComparisonChain.start()
                        .compare(o1.getEntity1().namedEntity.getBegin(), o2.getEntity1().namedEntity.getBegin())
                        .compare(o1.getEntity2().namedEntity.getBegin(), o2.getEntity2().namedEntity.getBegin())
                        .compare(o1.getEntity1().namedEntity.getEnd(), o2.getEntity1().namedEntity.getEnd())
                        .compare(o1.getEntity2().namedEntity.getEnd(), o2.getEntity2().namedEntity.getEnd())
                        .compare(o1.hashCode(), o2.hashCode())
                        .result();
            }
        }));


        //Table<Annotation, Annotation, Boolean> appositionCheck = HashBasedTable.create();

        // pairs

        for (EntityPair entityPair : entityPairs) {

            final Token e1 = entityPair.entity1.head;
            final Token e2 = entityPair.entity2.head;

            if (e1 == null || e2 == null) {
                continue;
            }


            DijkstraShortestPath<Token, DependencyEdge> shortestPaths = new DijkstraShortestPath<>(graph, e1, e2);

            GraphPath<Token, DependencyEdge> shortestPath = shortestPaths.getPath();
            if (shortestPath == null || shortestPath.getEdgeList().size() == 0) {
                continue;
            }

            // retrieve vertices on the shortest path
            // the first and last entry are the start and end vertex -> dynamically encode as X and Y
            final List<Token> vertices = Graphs.getPathVertexList(shortestPath);
            List<DependencyEdge> edges = shortestPath.getEdgeList();

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
            final String pattern = vertexLabels + " [" + edgeLabels + "]";

            Annotation ent1 = entityPair.entity1.namedEntity;
            Annotation ent2 = entityPair.entity2.namedEntity;

            if (isResolveCoreferences()) {
                // check if annotation1 is actually at the end of a co-reference chain

                // and also make sure we don't find a path between the same entities

                // so we ask the graph
                //noinspection ConstantConditions
                boolean replace = false; // don't replace two instances
                if (coref.containsVertex(entityPair.getEntity1().namedEntity) && coref.inDegreeOf(entityPair.getEntity1().namedEntity) > 0) {
                    final DefaultEdge edge = Iterables.getFirst(coref.incomingEdgesOf(entityPair.getEntity1().namedEntity), null);
                    final Annotation edgeSource = coref.getEdgeSource(edge);
                    if (!edgeSource.equals(entityPair.getEntity2().namedEntity) &&
                        !edgeSource.getCoveredText().startsWith(ent2.getCoveredText())) {
                        ent1 = edgeSource; //+ " -corf of " + e1text;
                        replace= true;

                    }
                }
                // skip the replacement if we have already replace the first instance
                if (!replace && coref.containsVertex(entityPair.getEntity2().namedEntity) && coref.inDegreeOf(entityPair.getEntity2().namedEntity) > 0) {
                    final DefaultEdge edge = Iterables.getFirst(coref.incomingEdgesOf(entityPair.getEntity2().namedEntity), null);
                    final Annotation edgeSource = coref.getEdgeSource(edge);
                    if (!edgeSource.equals(entityPair.getEntity1().namedEntity) &&
                        !edgeSource.getCoveredText().startsWith(ent1.getCoveredText())) {
                        ent2 = edgeSource; // + " -corf of " + e2text;
                    }
                    //ent1 = Iterables.getFirst(coref.incomingEdgesOf(pair.getEntity2()), null).getSource();
                }


            }

            dataBag.add(new FoundFeature<>(ent1, ent2, pattern));
            //appositionCheck.put(ent1, ent2, true);
        }

        // here check if any of the Entities are an apposition of each other .. that is not adding new value
/*        for (Map.Entry<Annotation, Map<Annotation, Boolean>> mapEntry : appositionCheck.rowMap().entrySet()) {
            if(mapEntry.getValue().size() > 1) {
                // here check if any of the KEYs are an apposition of each other
                List<Annotation> toCheck = Lists.newArrayList(mapEntry.getValue().keySet());
                for (int i = 0; i < toCheck.size(); i++) {
                    for (int j = i + 1; j < toCheck.size(); j++) {
                        // compare i - j
                        Annotation annotation1 = toCheck.get(i);
                        Annotation annotation2 = toCheck.get(j);

                        Token namedEntityHead1 = getNamedEntityHead(annotation1, graph);
                        Token namedEntityHead2 = getNamedEntityHead(annotation2, graph);

                        DependencyEdge edge = graph.getEdge(namedEntityHead1, namedEntityHead2);
                        Token nextToken = Iterables.getFirst(JCasUtil.selectFollowing(Token.class, namedEntityHead1, 1), null);

                        if(edge != null && edge.getDependency().equals("appos")) {

                            if(nextToken != null && nextToken.getPos().getClass().equals(O.class)) {
                                continue;
                            }

                            // check the order now (from -> to)
                            Annotation toCompare = edge.getFrom().equals(namedEntityHead1) ? annotation1 : annotation2;

                            // remove from DATABAG
                            // mapEntry.getKEY -> toCompare
                            Iterator<FoundFeature<Annotation>> featureIterator = dataBag.iterator();
                            while (featureIterator.hasNext()) {
                                FoundFeature<Annotation> feature = featureIterator.next();
                                if(feature.getEntity1().equals(mapEntry.getKey()) && feature.getEntity2().equals(toCompare)) {
                                   // featureIterator.remove();
                                }
                            }

                        }

                    }
                }
            }
        }
*/
        return dataBag;
    }

    private static final Set<String> skips = ImmutableSet.of("partmod", "det", "rcmod", "appos", "nsubj", "conj", "cc");
    private static final Set<String> allowed = ImmutableSet.of("of", "at");
    private static final Set<String> pronouns = ImmutableSet.of("he", "her", "his", "their");

    /**
     * Find the subtree for an annotation.
     *
     * @param annotation the candidate
     */
    public List<Token> getSubTree(Annotation annotation, UndirectedGraph<Token, DependencyEdge> graph) {
        // get all tokens
        Set<Token> tokens = Sets.newHashSet(JCasUtil.selectCovered(Token.class, annotation));
        if (tokens.size() == 0) {
            tokens.addAll(JCasUtil.selectCovering(Token.class, annotation));
        }

        Queue<Token> v = Queues.newArrayDeque(tokens);

        // breadth first
        while (!v.isEmpty()) {
            Token token = v.poll();

            if(!graph.containsVertex(token)) {
                continue;
            }

            Set<DependencyEdge> edges = graph.edgesOf(token);
            // filter the list of outwards edges
            for (DependencyEdge edge : edges) {

                if(edge.getFrom().equals(token) && !skips.contains(edge.getDependency()))  {
                    if(edge.getDependency().equals("prep") && !allowed.contains(edge.getTo().getLemma().getValue()) && !edge.getFrom().equals("degree")) {
                        continue;
                    }
                    if(edge.getDependency().equals("poss") && pronouns.contains(edge.getTo().getCoveredText())) {
                        continue;
                    }

                    // must be x left or x right of edge source - no jumps
                    List<Token> before = JCasUtil.selectPreceding(Token.class, edge.getFrom(), 4);
                    List<Token> after = JCasUtil.selectFollowing(Token.class, edge.getFrom(), 4);

                    if(before.contains(edge.getTo()) || after.contains(edge.getTo())) {
                        // we are going down that path
                        tokens.add(edge.getTo());
                        v.add(edge.getTo());
                    }

                }
            }
        }

        // order tokens by

        List<Token> subtree = Lists.newArrayList(tokens);
        Collections.sort(subtree, new Comparator<Token>() {
            @Override
            public int compare(Token o1, Token o2) {
                return Ints.compare(o1.getBegin(), o2.getBegin());
            }
        });

        // remove all PUNCs at the end and also the Possessive
        if(subtree.size() > 1) {
            Token last = Iterables.getLast(subtree, null);
            while (subtree.size() > 1 && last != null && (O.class.equals(last.getPos().getClass()) || PUNC.class.equals(last.getPos().getClass()) || allowed.contains(last.getCoveredText()))) {
                subtree.remove(last);
                last = Iterables.getLast(subtree, null);
            }
        }
        return subtree;

    }

    private class CandidateEntity {
        Token head;
        List<Token> list;
        Annotation namedEntity;

        public CandidateEntity(Token head, List<Token> list) {
            this.head = head;
            this.list = list;

            Token first = Iterables.getFirst(list, null);
            Token last = Iterables.getLast(list);

            namedEntity = new Annotation(getjCas() , first.getBegin(), last.getEnd());


        }
    }

    private static class EntityPair {

        private CandidateEntity entity1;
        private CandidateEntity entity2;

        public CandidateEntity getEntity1() {
            return entity1;
        }

        public CandidateEntity getEntity2() {
            return entity2;
        }

        private EntityPair(CandidateEntity entity1, CandidateEntity entity2) {
            this.entity1 = entity1;
            this.entity2 = entity2;
        }
    }

}
