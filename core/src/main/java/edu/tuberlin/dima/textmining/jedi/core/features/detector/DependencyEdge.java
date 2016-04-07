package edu.tuberlin.dima.textmining.jedi.core.features.detector;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Class used to link token in the dependency resolution graph.
 */
public class DependencyEdge {

        protected Token from;
        private Token to;
        private String dependency;

        public DependencyEdge(Token from, Token to, String dependency) {
            this.from = from;
            this.to = to;
            this.dependency = dependency;
        }

    public Token getFrom() {
        return from;
    }

    public Token getTo() {
        return to;
    }

    public String getDependency() {
        return dependency;
    }

    @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DependencyEdge that = (DependencyEdge) o;

            if (dependency != null ? !dependency.equals(that.dependency) : that.dependency != null) return false;
            if (from != null ? !from.equals(that.from) : that.from != null) return false;
            if (to != null ? !to.equals(that.to) : that.to != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = from != null ? from.hashCode() : 0;
            result = 31 * result + (to != null ? to.hashCode() : 0);
            result = 31 * result + (dependency != null ? dependency.hashCode() : 0);
            return result;
        }

    @Override
    public String toString() {
        return "DependencyEdge{" +
                "from=" + from.getCoveredText() +
                ", to=" + to.getCoveredText() +
                ", dependency='" + dependency + '\'' +
                '}';
    }
}

