package edu.tuberlin.dima.textmining.jedi.core.model;

import java.util.List;

/**
 * A freebase relation.
 */
public class FreebaseRelation {

        String domain;
        String range;

        List<String> domainTypes;
        List<String> rangeTypes;

        public FreebaseRelation(String domain, String range, List<String> rangeTypes, List<String> domainTypes) {
            this.domain = domain;
            this.range = range;
            this.rangeTypes = rangeTypes;
            this.domainTypes = domainTypes;
        }

        public String getRange() {
            return range;
        }

        public String getDomain() {
            return domain;
        }

        public List<String> getRangeTypes() {
            return rangeTypes;
        }

        public List<String> getDomainTypes() {
            return domainTypes;
        }

    @Override
    public String toString() {
        return "FreebaseRelation{" +
                "domain='" + domain + '\'' +
                ", range='" + range + '\'' +
                ", domainTypes=" + domainTypes +
                ", rangeTypes=" + rangeTypes +
                '}';
    }
}
