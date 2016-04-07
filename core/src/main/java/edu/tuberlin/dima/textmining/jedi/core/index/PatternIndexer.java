package edu.tuberlin.dima.textmining.jedi.core.index;

import com.google.common.base.*;
import com.google.common.base.Objects;
import com.google.common.collect.*;
import com.google.common.collect.Multiset;
import com.google.common.io.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import edu.tuberlin.dima.textmining.jedi.core.freebase.FreebaseHelper;
import edu.tuberlin.dima.textmining.jedi.core.model.FreebaseRelation;
import edu.tuberlin.dima.textmining.jedi.core.util.CompressionHelper;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * The indexer used to query the pattern database.
 */
public class PatternIndexer {

	private static final Logger LOG = LoggerFactory.getLogger(PatternIndexer.class);

	// Field Definitions
	private static final String ENTROPY_FIELD = "entropy";
	private static final String GLOBALCOUNT_FIELD = "globalcount";
	private static final String RELATION_FIELD = "relation";
	private static final String COUNT_FIELD = "count";
	private static final String PATTERN_FIELD = "pattern";

	private final Directory index;
	private final StandardAnalyzer analyzer;

	private IndexSearcher searcher;

	Table<String, String, Integer> additionalPattern = HashBasedTable.create();

	private final JsonParser jsonParser = new JsonParser();
	final FreebaseTypeService freebaseTypeService;

	/**
	 * Creates a new instance of the pattern indexer, using default settings.
	 * @throws IOException in case of errors
     */
	public PatternIndexer() throws IOException {
		this(true, new File("freepal-index"),
			Resources.getResource("freepal/relation-types.txt"),
			Resources.getResource("freepal/typeHierarchy.txt"),
			Resources.getResource("freepal/inversetypes.txt"),
			Resources.getResource("freepal/normalizedtypes.map"));
	}

	public PatternIndexer(boolean initSearch, File indexDirectory, URL relationTypes, URL relationHierarchyFile, URL inverseTypesFile, URL normalizedTypesFile) throws IOException {
		this(initSearch, indexDirectory,
			new FreebaseTypeService(
				relationTypes,
				relationHierarchyFile,
				inverseTypesFile,
				normalizedTypesFile)
		);
	}

	public PatternIndexer(boolean initSearch, File indexDirectory, FreebaseTypeService freebaseTypeService) throws IOException {

		analyzer = new StandardAnalyzer((CharArraySet) null);
		//index = new RAMDirectory(new MMapDirectory(new File(indexDirectory)), IOContext.READ);
		index = MMapDirectory.open(indexDirectory.toPath());

		this.freebaseTypeService = freebaseTypeService;

		if (initSearch) {
			IndexReader reader = DirectoryReader.open(index);
			searcher = new IndexSearcher(reader);
		}

		reRanker.put("ns:people.person.nationality", 0.7f);

		readAdditionalTypes();
	}

    private void readAdditionalTypes() throws IOException {
        URL resource = Resources.getResource("freepal/additional-pattern.txt.gz");
        InputStream inputStream = CompressionHelper.getDecompressionStream(resource.openStream());

        Multiset<String> lines = CharStreams.readLines(new InputStreamReader(inputStream, Charsets.UTF_8), new LineProcessor<Multiset<String>>() {
            Multiset<String> lines = HashMultiset.create();

            @Override
            public boolean processLine(String line) throws IOException {
                if (!StringUtils.isEmpty(line)) {
                    lines.add(line);
                }
                return true;
            }

            @Override
            public Multiset<String> getResult() {
                return lines;
            }
        });


        LOG.info("Reading " + lines.size() +" additional Pattern from " + resource);

        for (String line : lines.elementSet()) {

            Iterable<String> strings = Splitter.on("\t").split(line);
            String[] array = Iterables.toArray(strings, String.class);
            //final PatternSearchResult search = search(array[0], 10.7f);
            //if(search == null) {
                Integer integer = additionalPattern.get(array[0], array[1]);
                // row , column -> integer
                // row     : pattern
                // column  : relation
                additionalPattern.put(array[0], array[1], Objects.firstNonNull(integer, 0) + lines.count(line));
            //}
        }

       LOG.info("Read " + additionalPattern.columnKeySet().size() + " additional pattern");
    }

    public void checkConvergence() throws IOException {

        LineIterator lineIterator = FileUtils.lineIterator(
                new File("additional-pattern.txt"), Charsets.UTF_8.name());

        while (lineIterator.hasNext()) {
            String line = lineIterator.nextLine();
            Iterable<String> strings = Splitter.on("\t").split(line);
            String[] array = Iterables.toArray(strings, String.class);
            final PatternSearchResult search = search(array[0], 10.7f);
            if(search == null) {
                System.out.println("Missing: " + line);
            }
        }
    }

    public static void main(String[] args) throws IOException, ArchiveException {
        new PatternIndexer().exampleSearch();
    }

	/**
	 * Tests if we can query.
	 *
	 * @throws IOException in case of error
	 * @throws ArchiveException in case of errors
     */
    public void exampleSearch() throws IOException, ArchiveException {

        final PatternIndexer patternIndexer = new PatternIndexer();

        //buildLuceneIndex.buildIndex();
        final PatternSearchResult search = patternIndexer.search("[X] base in [Y] [0-rcmod-1,1-prep-2,2-pobj-3]", 10.7f);

        System.out.println(Joiner.on("\n").skipNulls().join(search.counts,search.entropy));
        for (PatternSearchResult.SubRelation subRelation : search.relationCount) {
            final FreebaseRelation types = patternIndexer.freebaseTypeService.getTypes(subRelation.relation);
            System.out.println(Joiner.on("\n").useForNull("NULL").join(subRelation, types));
        }
    }

    public void buildIndex(String sourceFile) throws IOException {

        // building the index
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		final IndexWriter w = new IndexWriter(index, config);

        final Gson gson = new GsonBuilder().create();

        FileInputStream fin = new FileInputStream(sourceFile);

        InputStream decompressionStream = CompressionHelper.getDecompressionStream(fin);

        CharStreams.readLines(new InputStreamReader(decompressionStream, Charsets.UTF_8),

                new LineProcessor<Object>() {

                    long counter = 0;

                    @Override
                    public boolean processLine(String line) throws IOException {
                        final PatternSearchResult element = gson.fromJson(line, PatternSearchResult.class);
                        if(element.feature.startsWith("[X] [Y] [")
                                || element.feature.startsWith("[X] [Y] ( [")
                                || element.counts < 3) {
                            // skip meaningless pattern
                            return true;
                        }
                        addDocumentToIndex(w, element);
                        counter++;
                        if (counter % 10000 == 0) {
                            LOG.info(String.format("indexed %6d", counter));
                        }
                        return true;
                    }

                    @Override
                    public Object getResult() {
                        try {
                            w.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                });

        LOG.info("Done indexing ...");
        IndexReader reader = DirectoryReader.open(index);
        searcher = new IndexSearcher(reader);

    }

    public void getListOfRelations(String sourceFile) throws IOException {
        FileInputStream fin = new FileInputStream(sourceFile);
        final Gson gson = new GsonBuilder().create();
        //GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(fin);

        final Set<String> relations = CharStreams.readLines(new InputStreamReader(fin, Charsets.UTF_8),

                new LineProcessor<Set<String>>() {

                    long counter = 0;
                    final Set<String> relations = new HashSet<>();

                    @Override
                    public boolean processLine(String line) throws IOException {
                        final PatternSearchResult element = gson.fromJson(line, PatternSearchResult.class);

                        for (PatternSearchResult.SubRelation subRelation : element.relationCount) {
                            relations.add(subRelation.relation);
                        }
                        counter++;
                        if (counter % 10000 == 0) {
                            LOG.info(String.format("processed %6d", counter));
                        }
                        return true;
                    }

                    @Override
                    public Set<String> getResult() {
                        return relations;
                    }
                });

        LOG.info("Relations : " + relations.size());
        final CharSink charSink = Files.asCharSink(new File("relation-names2.txt"), Charsets.UTF_8);
        charSink.writeLines(relations);
    }




    public void writeRelationTypes(String relationNamesLocation) throws IOException {

        final List<String> lines = Files.readLines(new File(relationNamesLocation), Charsets.UTF_8);

        final FreebaseHelper freebaseHelper = new FreebaseHelper();

        LOG.info("Relations : " + lines.size());
        final CharSink charSink = Files.asCharSink(new File("relation-types.txt"), Charsets.UTF_8);
        charSink.writeLines(Iterables.transform(lines, new Function<String, CharSequence>() {

            int counter = 0;

            @Override
            public CharSequence apply(String relation) {
                FreebaseRelation freebaseRelation;

                try {
                    freebaseRelation = freebaseHelper.getTypesForRelationFromFreebase(relation);
                    counter++;
                    if (counter % 100 == 0) {
                        LOG.info(String.format("processed %6d", counter));
                    }
                    if (freebaseRelation != null) {
                        return Joiner.on("\t").join(relation, freebaseRelation.getDomain(), freebaseRelation.getRange());
                    }
                    return null;
                } catch (IOException e) {
                    LOG.error("Error writing relationNamesLocation from "+  relationNamesLocation, e);
                }
                return null;
            }
        }));
    }

    String[] skipPatternAtStart = {"[X] [Y] ( [", "[X] [Y] ) [", "[X] [Y] ["};
    PatternSearchResult EMPTY = new PatternSearchResult();

    @Cacheable("lucene")
    public PatternSearchResult search(String query, float maxEntropy) {

		try {

			if (StringUtils.startsWithAny(query, skipPatternAtStart)) {
				return EMPTY;
			}

			if (query.contains("[0-appos-1,0-appos-2]") || query.contains("[0-appos-2,2-nn-1]")) {
				return EMPTY;
			}

			if (query.equals("[X] name after [Y] [1-npadvmod-0,1-prep-2,2-pobj-3]")) {
				query = "[X] name after [Y] [1-nsubjpass-0,1-prep-2,2-pobj-3]";
			}

			PatternSearchResult patternSearchResult = new PatternSearchResult();
			List<PatternSearchResult.SubRelation> results = Lists.newArrayList();


       /* if(additionalPattern.containsRow(query)) {

            Map<String, Integer> row = additionalPattern.row(query);

            patternSearchResult.counts = row.keySet().size();
            patternSearchResult.entropy = 1;

            patternSearchResult.feature = query;
            patternSearchResult.relationCount = results;
            for (Map.Entry<String, Integer> additionalRelation : row.entrySet()) {
                String rel = FreebaseHelper.transformNewToOldId(additionalRelation.getKey());

                results.add(new PatternSearchResult.SubRelation(rel, additionalRelation.getValue()));
            }


            return patternSearchResult;

        }        */

			TermQuery patternQuery = new TermQuery(new Term(PATTERN_FIELD, query));

			BooleanQuery booleanClauses = new BooleanQuery();
			booleanClauses.add(patternQuery, BooleanClause.Occur.MUST);
			booleanClauses.add(NumericRangeQuery.newFloatRange(ENTROPY_FIELD, 0f, maxEntropy, true, true), BooleanClause.Occur.MUST);
			int hitsPerPage = 200;

			// search
			ScoreDoc[] hits = searcher.search(booleanClauses, hitsPerPage).scoreDocs;

			//System.out.println("Found " + hits.length + " hits.");
			if (hits.length == 0) return null;

			for (ScoreDoc hit : hits) {
				int docId = hit.doc;
				Document d = searcher.doc(docId);

				int count = d.getField(COUNT_FIELD).numericValue().intValue();
				final int globalcount = d.getField(GLOBALCOUNT_FIELD).numericValue().intValue();
				float entropy = d.getField(ENTROPY_FIELD).numericValue().floatValue();

				// global count needs to be at least X
				if (globalcount < 9) break;

				// individual count needs to be at least X
				if (count <= 3) continue;
				// demand that feature accounts for at least .1% for that relation
				//if(count / (float) globalcount < 0.02f) continue;


				patternSearchResult.counts = globalcount;
				String subrelation = d.get(RELATION_FIELD);

				patternSearchResult.entropy = entropy;

				count = (int) (Objects.firstNonNull(reRanker.get(subrelation), 1.0f) * count);

				results.add(new PatternSearchResult.SubRelation(subrelation, count));
				//return d.get("relation");
			}

			// sort
			Collections.sort(results, new Ordering<PatternSearchResult.SubRelation>() {
				@Override
				public int compare(@Nullable PatternSearchResult.SubRelation left, @Nullable PatternSearchResult.SubRelation right) {
					return ComparisonChain.start().compare(left.count, right.count).compare(left.hashCode(), right.hashCode()).result();
				}
			}.reverse());
			patternSearchResult.relationCount = results;

			// This is a shortcut ... if we have one dominating pattern, trim the list down
			if (results.size() > 0 && results.get(0).count / (float) patternSearchResult.getCounts() >= 0.4) {
				// dominating pattern
				// float confidence = results.get(0).count / (float) patternSearchResult.getCounts();
				// we just just the top one (sorted)
				patternSearchResult.relationCount = Lists.newArrayList(Iterables.limit(results, 3));

			}
			// additional pattern to account for
			// FoundFeature{entity1=Ph.D., entity2=Psychology, pattern='[X] in [Y] [0-prep-1,1-pobj-2]'
			if (query.equals("[X] in [Y] [0-prep-1,1-pobj-2]")) {
				patternSearchResult.relationCount.add(new PatternSearchResult.SubRelation("ns:education.educational_degree.people_with_this_degree..education.education.major_field_of_study", 20));
			}


			return patternSearchResult;
		} catch (IOException e) {
			LOG.error("Error searching pattern DB with " + query, e);
		}

		return EMPTY;

	}

	private Map<String, Float> reRanker = Maps.newHashMap();

	private static void addDocumentToIndex(IndexWriter w, PatternSearchResult element) throws IOException {

		for (PatternSearchResult.SubRelation subRelation : element.relationCount) {
			Document doc = new Document();

			String relation = subRelation.relation;

			doc.add(new StringField(PATTERN_FIELD, element.feature, org.apache.lucene.document.Field.Store.YES));
			doc.add(new StringField(RELATION_FIELD, relation, org.apache.lucene.document.Field.Store.YES));
			doc.add(new IntField(COUNT_FIELD, subRelation.count, org.apache.lucene.document.Field.Store.YES));
			doc.add(new IntField(GLOBALCOUNT_FIELD, element.counts, org.apache.lucene.document.Field.Store.YES));
			doc.add(new FloatField(ENTROPY_FIELD, element.entropy, org.apache.lucene.document.Field.Store.YES));
			w.addDocument(doc);
		}

	}

	public static class PatternSearchResult {

		String feature;
		String toprelation;
		int counts;
		float entropy;

		List<SubRelation> relationCount = Lists.newArrayList();

		public String getFeature() {
			return feature;
		}

		public String getToprelation() {
			return toprelation;
		}

		public int getCounts() {
			return counts;
		}

		public float getEntropy() {
			return entropy;
		}

		public List<SubRelation> getRelationCount() {
			return relationCount;
		}

		@Override
		public String toString() {
			return "PatternSearchresult{" +
				"feature='" + feature + '\'' +
				", toprelation='" + toprelation + '\'' +
				", counts=" + counts +
				", entropy=" + entropy +
				", relationCount=" + relationCount +
				'}';
		}

		public static class SubRelation {
			String relation;
			int count;

			public SubRelation(String relation, int count) {
				this.relation = relation;
				this.count = count;
			}

			public String getRelation() {
				return relation;
			}

			public int getCount() {
				return count;
			}

			@Override
			public String toString() {
				return "SubRelation{" +
					"relation='" + relation + '\'' +
					", count=" + count +
					'}';
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;

				SubRelation that = (SubRelation) o;

				if (count != that.count) return false;
				if (relation != null ? !relation.equals(that.relation) : that.relation != null) return false;

				return true;
			}

			@Override
			public int hashCode() {
				int result = relation != null ? relation.hashCode() : 0;
				result = 31 * result + count;
				return result;
			}
		}
	}

}
