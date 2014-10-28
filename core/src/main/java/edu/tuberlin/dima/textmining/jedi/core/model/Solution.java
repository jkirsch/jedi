package edu.tuberlin.dima.textmining.jedi.core.model;

public class Solution<T> {
    T left;
    T right;
    public Edge edge;

    public Solution(T left, T right, Edge edge) {
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
        return "Solution{" +
                "left=" + left +
                ", right=" + right +
                ", edge=" + edge +
                '}';
    }
}