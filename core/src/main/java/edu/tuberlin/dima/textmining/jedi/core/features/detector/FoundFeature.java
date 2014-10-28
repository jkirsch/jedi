package edu.tuberlin.dima.textmining.jedi.core.features.detector;

import org.apache.uima.jcas.tcas.Annotation;

/**
 * Date: 10.09.2014
 * Time: 18:36
 *
 * @author Johannes Kirschnick
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

    public T getEntity2() {
        return entity2;
    }

    public String getPattern() {
        return pattern;
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

    private static final <T> String transformToString(T input) {
        if(input instanceof Annotation) {
            return ((Annotation) input).getCoveredText();
        } else {
            return input.toString();
        }
    }
}
