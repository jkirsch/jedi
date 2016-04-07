package edu.tuberlin.dima.textmining.jedi.core.model;

import org.apache.uima.jcas.tcas.Annotation;

/**
 */
public class FoundFeature<T> {

    T entity1;
    T entity2;

    String pattern;
    String relation;

    public FoundFeature(T entity1, T entity2, String pattern) {
        this.entity1 = entity1;
        this.entity2 = entity2;
        this.pattern = pattern;
    }


    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public T getEntity1() {
        return entity1;
    }

	public void setEntity1(T entity1) {
		this.entity1 = entity1;
	}

	public T getEntity2() {
        return entity2;
    }

	public void setEntity2(T entity2) {
		this.entity2 = entity2;
	}

	public String getPattern() {
        return pattern;
    }

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Override
    public String toString() {
        return "FoundFeature{" +
                "entity1=" + transformToString(entity1) +
                ", entity2=" + transformToString(entity2) +
                ", pattern='" + pattern + '\'' +
                ", relation='" + relation + '\'' +
                '}';
    }

    private static <T> String transformToString(T input) {
        if(input instanceof Annotation) {
            return ((Annotation) input).getCoveredText();
        } else {
            return input.toString();
        }
    }
}
