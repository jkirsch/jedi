package edu.tuberlin.dima.textmining.jedi.core.features.detector;

import edu.tuberlin.dima.textmining.jedi.core.index.PatternIndexer;

public class SearchResult<T> {
        FoundFeature<T> tuple;
        PatternIndexer.PatternSearchResult patternSearchResult;

        public SearchResult(FoundFeature<T> tuple, PatternIndexer.PatternSearchResult patternSearchResult) {
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
