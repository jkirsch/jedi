package edu.tuberlin.dima.textmining.jedi.core.model;

import edu.tuberlin.dima.textmining.jedi.core.freebase.FreebaseHelper;
import edu.tuberlin.dima.textmining.jedi.core.util.AnnovisTransformerWriter;
import edu.tuberlin.dima.textmining.jedi.core.util.TableBuilder;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The class hold the result of the releation detection.
 *
 * @param <T> the UIMA type annotation of the entities.
 */
public class RelationDetectionResults<T> {

    List<DetectedRelation<T>> detectedRelations;
    List<FoundFeature<T>> foundFeatures;

    Graph graph;

    String stdout;

    String detectorType;

    Map<String, AnnovisTransformerWriter.Annovis> annovis;

    public RelationDetectionResults(List<PatternSearchResult<T>> patternSearchResults, List<DetectedRelation<T>> detectedRelations, String detectorType, Graph graph, String stdout, Map<String, AnnovisTransformerWriter.Annovis> annovis) {

        this.foundFeatures = patternSearchResults.stream().map(PatternSearchResult::getTuple).collect(Collectors.toList());

        this.detectedRelations = detectedRelations;
        this.graph = graph;
        this.stdout = stdout;
        this.annovis = annovis;
        this.detectorType = detectorType;
    }

    public List<DetectedRelation<T>> getDetectedRelations() {
        return detectedRelations;
    }

    public Graph getGraph() {
        return graph;
    }

    public String getStdout() {
        return stdout;
    }

    public String getDetectorType() {
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
	 * @param <T> the type of the annotations
     * @return answer with string representations
     */
    public <T> RelationDetectionResults<String> generateStringVersion() {

		List<DetectedRelation<String>> transform = detectedRelations.stream().map(solution -> new DetectedRelation<>(
			transformToString(solution.getLeft()),
			transformToString(solution.getRight()),
			solution.getEdge())).collect(Collectors.toList());

		List<PatternSearchResult<String>> transformedFeatures = foundFeatures.stream().map(feature -> new PatternSearchResult<>(
			new FoundFeature<>(
				transformToString(feature.getEntity1()),
				transformToString(feature.getEntity2()),
				feature.getPattern()), null)).collect(Collectors.toList());;

        return new RelationDetectionResults<>(transformedFeatures, transform, detectorType, graph, stdout, annovis);

    }

	public String generateReadableTableString(){
		TableBuilder tb = new TableBuilder();

		RelationDetectionResults<String> stringVersion = generateStringVersion();

		tb.addRow("Object", "Relation", "Subject", "Pattern");
		tb.addRow("------", "--------", "-------", "-------");

		for (DetectedRelation<String> detectedRelation : stringVersion.getDetectedRelations()) {

			String relation = FreebaseHelper.transformOldToNewId(detectedRelation.getEdge().getRelation());
			tb.addRow(detectedRelation.getLeft(), relation, detectedRelation.getRight(), detectedRelation.getEdge().getPattern());
		}

		return tb.toString();
	}


    private static <T> String transformToString(T input) {
        if(input instanceof Annotation) {
            return ((Annotation) input).getCoveredText();
        } else {
            return input.toString();
        }
    }

}
