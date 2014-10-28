package edu.tuberlin.dima.textmining.jedi.core.model;

import com.google.common.collect.Lists;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.List;

/**
 * D3 Graph serialization
 */
public class Graph {

    List<GraphNode> nodes;
    List<GraphEdge> edges;

    public Graph(List<GraphNode> nodes, List<GraphEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    private static class GraphEdge {
        int source;
        int target;
        String pattern;
        double score;

        private GraphEdge(int source, int target, Edge edge) {
            this.source = source;
            this.target = target;
            this.pattern = edge.getPattern();
            this.score = edge.getScore();
        }

        public int getSource() {
            return source;
        }

        public int getTarget() {
            return target;
        }

        public double getScore() {
            return score;
        }

        public String getPattern() {
            return pattern;
        }

        @Override
        public String toString() {
            return "GraphEdge{" +
                    "source=" + source +
                    ", target=" + target +
                    ", pattern='" + pattern + '\'' +
                    ", score=" + score +
                    '}';
        }
    }

    private static class GraphNode {
        String name;
        String type;

        private GraphNode(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GraphNode graphNode = (GraphNode) o;

            if (!name.equals(graphNode.name)) return false;
            if (!type.equals(graphNode.type)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "GraphNode{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    private static final <T> String transformToString(T node) {
        if(node instanceof Annotation) {
            return ((Annotation) node).getCoveredText();
        } else {
            return node.toString();
        }
    }

    public static <T> Graph transform(List<Solution<T>> solutions) {
        // build up the nodes array

        List<GraphNode> nodes = Lists.newArrayList();
        List<GraphEdge> edges = Lists.newArrayList();

        for (Solution<T> solution : solutions) {
            GraphNode left = new GraphNode(transformToString(solution.getLeft()), solution.getEdge().getDomain());
            GraphNode right = new GraphNode(transformToString(solution.getRight()), solution.getEdge().getRange());

            if (!nodes.contains(left)) {
                nodes.add(left);
            }

            if (!nodes.contains(right)) {
                nodes.add(right);
            }

            // add test edges
            edges.add(new GraphEdge(nodes.indexOf(left), nodes.indexOf(right), solution.getEdge()));
        }


        return new Graph(nodes, edges);
    }

    public List<GraphNode> getNodes() {
        return nodes;
    }

    public List<GraphEdge> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return "Graph{" +
                "nodes=" + nodes +
                ", edges=" + edges +
                '}';
    }
}