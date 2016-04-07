package edu.tuberlin.dima.textmining.jedi.core.model;

import edu.tuberlin.dima.textmining.jedi.core.index.PatternIndexer;

/**
 * Result withOptions querying the pattern index.
 *
 * @param <T>
 */
public class PatternSearchResult<T> {
        FoundFeature<T> tuple;
        PatternIndexer.PatternSearchResult patternSearchResult;

        public PatternSearchResult(FoundFeature<T> tuple, PatternIndexer.PatternSearchResult patternSearchResult) {
            this.tuple = tuple;
            this.patternSearchResult = patternSearchResult;
        }

        public FoundFeature<T> getTuple() {
            return tuple;
        }

        public PatternIndexer.PatternSearchResult getPatternSearchResult() {
            return patternSearchResult;
        }
    }
