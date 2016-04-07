package edu.tuberlin.dima.textmining.jedi.core.model;

/**
 * This holds information about the detected relation between two entities.
 *
 * @param <T> type of the entities
 */
public class DetectedRelation<T> {
    T left;
    T right;
    public Edge edge;

    public DetectedRelation(T left, T right, Edge edge) {
        this.left = left;
        this.right = right;
        this.edge = edge;
    }

    public T getLeft() {
        return left;
    }

    public T getRight() {
        return right;
    }

    public Edge getEdge() {
        return edge;
    }

    @Override
    public String toString() {
        return "DetectedRelation{" +
                "left=" + left +
                ", right=" + right +
                ", edge=" + edge +
                '}';
    }
}
