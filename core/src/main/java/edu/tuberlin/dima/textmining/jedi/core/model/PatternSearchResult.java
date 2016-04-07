package edu.tuberlin.dima.textmining.jedi.core.model;

import edu.tuberlin.dima.textmining.jedi.core.index.PatternIndexer;

/**
 * Result for querying the pattern index.
 *
 * @param <T> the type paramter of the entities
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
