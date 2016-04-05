package edu.tuberlin.dima.textmining.jedi.core.features.detector.model;

import edu.tuberlin.dima.textmining.jedi.core.features.detector.DetectorType;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.FoundFeature;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.SearchResult;
import edu.tuberlin.dima.textmining.jedi.core.model.Graph;
import edu.tuberlin.dima.textmining.jedi.core.model.Solution;
import edu.tuberlin.dima.textmining.jedi.core.util.AnnovisTransformerWriter;
import org.apache.uima.jcas.tcas.Annotation;

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

	/**
	 * Transforms a solution using UIMA objects into their string representations, by using the covered text implementation.
	 *
	 * @param input object with cas annotations
	 * @param <T> the type of the annotations
     * @return answer with string representations
     */
    public static <T> Answer<String> generateStringVersion(Answer<T> input) {

		List<Solution<String>> transform = input.getSolutions().stream().map(solution -> new Solution<>(
			transformToString(solution.getLeft()),
			transformToString(solution.getRight()),
			solution.getEdge())).collect(Collectors.toList());

		List<SearchResult<String>> transformedFeatures = input.foundFeatures.stream().map(feature -> new SearchResult<>(
			new FoundFeature<>(
				transformToString(feature.getEntity1()),
				transformToString(feature.getEntity2()),
				feature.getPattern()), null)).collect(Collectors.toList());;

        return new Answer<>(transformedFeatures, transform, input.getDetectorType(), input.getGraph(), input.getStdout(), input.getAnnovis());

    }

    private static <T> String transformToString(T input) {
        if(input instanceof Annotation) {
            return ((Annotation) input).getCoveredText();
        } else {
            return input.toString();
        }
    }

}
