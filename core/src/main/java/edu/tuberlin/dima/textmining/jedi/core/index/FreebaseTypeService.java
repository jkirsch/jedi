package edu.tuberlin.dima.textmining.jedi.core.index;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;
import edu.tuberlin.dima.textmining.jedi.core.freebase.FreebaseHelper;
import edu.tuberlin.dima.textmining.jedi.core.model.FreebaseRelation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 */
public class FreebaseTypeService {

	private static final Logger LOG = LoggerFactory.getLogger(FreebaseTypeService.class);

	private final Map<String, FreebaseRelation> relations;
	private final Map<String, List<String>> typeHierarchy;

	/**
	 * FROM -> TO.
	 */
	private final BiMap<String, String> inverseRelation;

	Map<String, String> normalizedTypes;

	public FreebaseRelation getTypes(String relation) {
		FreebaseRelation freebaseRelation = relations.get(relation);
		if (freebaseRelation == null && relation.contains("..")) {
			freebaseRelation = relations.get(relation.substring(0, relation.indexOf("..")));
		}
		return freebaseRelation;
	}

	public String getInverse(String relation) {
		// Get the inverse relation .. this is not
		if (inverseRelation.containsKey(relation)) {
			return inverseRelation.get(relation);
		} else {
			return inverseRelation.inverse().get(relation);
		}

	}

	/**
	 * Init with defaults
	 */
	public FreebaseTypeService() throws IOException {

		this(
			Resources.getResource("freepal/relation-types.txt"),
			Resources.getResource("freepal/typeHierarchy.txt"),
			Resources.getResource("freepal/inversetypes.txt"),
			Resources.getResource("freepal/normalizedtypes.map")
			);
	}

	public FreebaseTypeService(URL relationTypes, URL relationHierarchyFile, URL relationInverseFile, URL normalizedTypesFile) throws IOException {

		typeHierarchy = Resources.readLines(relationHierarchyFile, Charsets.UTF_8, new LineProcessor<Map<String, List<String>>>() {

			Map<String, List<String>> map = Maps.newHashMap();

			@Override
			public boolean processLine(String line) throws IOException {

				if (line.length() > 0 && !line.equals("null")) {
					final Iterable<String> split = Splitter.on("\t").trimResults().limit(2).split(line);
					final String[] strings = Iterables.toArray(split, String.class);
					if (strings.length != 2) {
						LOG.error("Error reading file, check line: \n" + line);
						return true;
					}

					// first the type
					String type = strings[0];

					if (strings[1].length() > 0) {

						final Iterable<String> otherTypes = Splitter.on(",").trimResults().split(strings[1]);

						List<String> types = Lists.newArrayList(Iterables.transform(otherTypes, new Function<String, String>() {
							@Nullable
							@Override
							public String apply(@Nullable String input) {
								return "ns:" + input.substring(1).replace("/", ".");
							}
						}));

						map.put(type, types);
					}
				}
				return true;
			}

			@Override
			public Map<String, List<String>> getResult() {
				return map;
			}
		});

		// now read the normalized types
		final List<String> typeNormalizerLines = Resources.readLines(normalizedTypesFile, Charsets.UTF_8);
		normalizedTypes = Maps.newHashMap();
		for (String typeNormalizerLine : typeNormalizerLines) {
			if (StringUtils.isEmpty(typeNormalizerLine) || StringUtils.startsWith(typeNormalizerLine, "#")) {
				continue;
			}
			// Parse
			// subtype -> type
			final Iterable<String> splits = Splitter.on("\t").limit(2).trimResults().split(typeNormalizerLine);
			final String subType = Iterables.getFirst(splits, null);
			final String mainType = Iterables.get(splits, 1);
			normalizedTypes.put(subType, mainType);
		}


		relations = Resources.readLines(relationTypes, Charsets.UTF_8, new LineProcessor<Map<String, FreebaseRelation>>() {

			Map<String, FreebaseRelation> map = Maps.newHashMap();

			@Override
			public boolean processLine(String line) throws IOException {

				if (line.length() > 0 && !line.equals("null")) {
					final Iterable<String> split = Splitter.on("\t").trimResults().limit(3).split(line);
					final String[] strings = Iterables.toArray(split, String.class);
					if (strings.length != 3) {
						System.out.println(line);
					}

					String domain = strings[1];
					String range = strings[2];

					List<String> domainTypes = typeHierarchy.get(domain);
					List<String> rangeTypes = typeHierarchy.get(range);

					// normalize the types
					String normalizedRange = normalizedTypes.get(FreebaseHelper.transformOldToNewId(range));
					range = Objects.firstNonNull(normalizedRange, FreebaseHelper.transformOldToNewId(range));
					String normalizedDomain = normalizedTypes.get(FreebaseHelper.transformOldToNewId(domain));
					domain = Objects.firstNonNull(normalizedDomain, FreebaseHelper.transformOldToNewId(domain));

					Function<String, String> transformIDFunction = new Function<String, String>() {
						@Nullable
						@Override
						public String apply(@Nullable String input) {
							String typeName = FreebaseHelper.transformOldToNewId(input);
							String normalizedTyp = normalizedTypes.get(typeName);
							return Objects.firstNonNull(normalizedTyp, typeName);
						}
					};
					if (rangeTypes != null) {
						rangeTypes = Lists.newArrayList(Sets.newHashSet(
							Iterables.filter(
								Iterables.transform(rangeTypes, transformIDFunction),
								Predicates.notNull()
							)
						));
						// remove the main type
						rangeTypes.remove(range);
					}
					if (domainTypes != null) {
						domainTypes = Lists.newArrayList(
							Sets.newHashSet(
								Iterables.filter(
									Iterables.transform(domainTypes, transformIDFunction),
									Predicates.notNull()
								)));
						domainTypes.remove(domain);
					}

					FreebaseRelation freebaseRelation = new FreebaseRelation(
						domain,
						range,
						rangeTypes,
						domainTypes);
					final String relation = strings[0];
					map.put(relation, freebaseRelation);
				}
				return true;
			}

			@Override
			public Map<String, FreebaseRelation> getResult() {
				return map;
			}
		});

		final List<String> lines = Resources.readLines(relationInverseFile, Charsets.UTF_8);
		inverseRelation = HashBiMap.create();
		for (String line : lines) {
			final Iterable<String> splits = Splitter.on("\t").limit(2).trimResults().split(line);
			final String relation = Iterables.getFirst(splits, null);
			String left = FreebaseHelper.transformOldToNewId(relation);
			inverseRelation.put(left, Iterables.getLast(splits));
		}

		LOG.info(String.format("Read the type information for %d relations from  %s", relations.size(), relationTypes.toString()));
	}
}
