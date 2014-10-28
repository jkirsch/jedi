package edu.tuberlin.dima.textmining.jedi.core.model;

public class Edge {

    public String relation;
    public String pattern;

    public String domain;
    public String range;
    public double score;
    public double entropy;

    public int count = 0;

    final boolean fixed;

    final int edgeNumber;

    public Edge(int edgeNumber, String relation, String pattern, String domain, String range, double score, double entropy, int count, boolean fixed) {
        this.edgeNumber = edgeNumber;
        this.relation = relation;
        this.pattern = pattern;
        this.domain = domain;
        this.range = range;
        this.score = score;
        this.entropy = entropy;
        this.count = count;
        this.fixed = fixed;
    }

    public String getRelation() {
        return relation;
    }

    public String getDomain() {
        return domain;
    }

    public String getRange() {
        return range;
    }

    public double getScore() {
        return score;
    }

    public String getPattern() {
        return pattern;
    }

    public boolean isFixed() {
        return fixed;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "relation='" + relation + '\'' +
                ", pattern='" + pattern + '\'' +
                ", domain='" + domain + '\'' +
                ", range='" + range + '\'' +
                ", score=" + score +
                ", fixed=" + fixed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge edge = (Edge) o;

        if (count != edge.count) return false;
        if (edgeNumber != edge.edgeNumber) return false;
        if (Double.compare(edge.entropy, entropy) != 0) return false;
        if (fixed != edge.fixed) return false;
        if (Double.compare(edge.score, score) != 0) return false;
        if (!domain.equals(edge.domain)) return false;
        if (!pattern.equals(edge.pattern)) return false;
        if (!range.equals(edge.range)) return false;
        if (!relation.equals(edge.relation)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = relation.hashCode();
        result = 31 * result + pattern.hashCode();
        result = 31 * result + domain.hashCode();
        result = 31 * result + range.hashCode();
        temp = Double.doubleToLongBits(score);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(entropy);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + count;
        result = 31 * result + (fixed ? 1 : 0);
        result = 31 * result + edgeNumber;
        return result;
    }
}