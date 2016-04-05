package edu.tuberlin.dima.textmining.jedi.core.features.detector.model;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.DetectorType;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.FoundFeature;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.SearchResult;
import edu.tuberlin.dima.textmining.jedi.core.model.Graph;
import edu.tuberlin.dima.textmining.jedi.core.model.Solution;
import edu.tuberlin.dima.textmining.jedi.core.util.AnnovisTransformerWriter;
import org.apache.uima.jcas.tcas.Annotation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Answer<T> {
    List<Solution<T>> solutions;
    List<FoundFeature<T>> foundFeatures;

    Graph graph;

    String stdout;

    DetectorType detectorType;

    Map<String, AnnovisTransformerWriter.Annovis> annovis;

    public Answer(List<SearchResult<T>> searchResults, List<Solution<T>> solutions, DetectorType detectorType, Graph graph, String stdout, Map<String, AnnovisTransformerWriter.Annovis> annovis) {

        this.foundFeatures = searchResults.stream().map(SearchResult::getTuple).collect(Collectors.toList());

        this.solutions = solutions;
        this.graph = graph;
        this.stdout = stdout;
        this.annovis = annovis;
        this.detectorType = detectorType;
    }

    public List<Solution<T>> getSolutions() {
        return solutions;
    }

    public Graph getGraph() {
        return graph;
    }

    public String getStdout() {
        return stdout;
    }

    public DetectorType getDetectorType() {
        return detectorType;
    }

    public Map<String, AnnovisTransformerWriter.Annovis> getAnnovis() {
        return annovis;
    }

    public List<FoundFeature<T>> getFoundFeatures() {
        return foundFeatures;
    }

    public void setAnnovis(Map<String, AnnovisTransformerWriter.Annovis> annovis) {
        this.annovis = annovis;
    }

    public static final <T> Answer<String> generateStringVersion(Answer<T> input) {

        List<Solution<String>> transform = Lists.newArrayList(Iterables.transform(input.getSolutions(), new Function<Solution<T>, Solution<String>>() {
            @Nullable
            @Override
            public Solution<String> apply(@Nullable Solution<T> input) {
                return new Solution<>(
                        transformToString(input.getLeft()),
                        transformToString(input.getRight()),
                        input.getEdge());
            }
        }));

        List<SearchResult<String>> transformedFeatures = Lists.newArrayList(Iterables.transform(input.foundFeatures, new Function<FoundFeature<T>, SearchResult<String>>() {
            @Nullable
            @Override
            public SearchResult<String> apply(@Nullable FoundFeature<T> input) {
                return new SearchResult<String>(
                        new FoundFeature<>(
                                transformToString(input.getEntity1()),
                                transformToString(input.getEntity2()),
                                input.getPattern()), null);
            }
        }));

        return new Answer<>(transformedFeatures, transform, input.getDetectorType(), input.getGraph(), input.getStdout(), input.getAnnovis());

    }

    private static final <T> String transformToString(T input) {
        if(input instanceof Annotation) {
            return ((Annotation) input).getCoveredText();
        } else {
            return input.toString();
        }
    }

}
