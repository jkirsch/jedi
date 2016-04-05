package edu.tuberlin.dima.textmining.jedi.core.freebase;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import edu.tuberlin.dima.textmining.jedi.core.model.FreebaseRelation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Date: 08.09.2014
 * Time: 14:44
 *
 * @author Johannes Kirschnick
 */
public class FreebaseHelper {

    private static final String FREEBASE_TOPIC_ENDPOINT = "https://www.googleapis.com/freebase/v1/topic{id}?filter=/common/topic/description&filter=/type/object/guid&filter=/type/object/type&filter=/type/object/name&filter=/common/topic/alias&key=AIzaSyCyloa8DMxhKvBVdqdk_Drf900rAeeA7QA";

    private static final String FREEBASE_SEARCH_API = "https://www.googleapis.com/freebase/v1/search?limit=1&scoring=entity&query={id}&prefixed=true&output=(%2Fcommon%2Ftopic%2Fdescription+type+name+%2Fcommon%2Ftopic%2Falias)&lang=en&indent=true&key=AIzaSyCyloa8DMxhKvBVdqdk_Drf900rAeeA7QA";

    /**
     * MQL instead if topic
     [{
     "mid": "/m/0h6rm",
     "/type/object/name": [],
     "/common/topic/alias": [],
     "/common/topic/description": [],
     "/type/object/guid": null,
     "/type/object/type": []
     }]
     */


    // [{ "id": "AAA", "/dataworld/gardening_hint/replaced_by": [{"mid": null }] }]
    private static final String FREEBASE_MQL_ENDPOINT = "https://www.googleapis.com/freebase/v1/mqlread/?lang=%2Flang%2Fen&query=%5B%7B%20%22id%22%3A%20%22{id}%22%2C%20%22%2Fdataworld%2Fgardening_hint%2Freplaced_by%22%3A%20%5B%7B%22mid%22%3A%20null%20%7D%5D%20%7D%5D%0A%0A%0A&key=AIzaSyCyloa8DMxhKvBVdqdk_Drf900rAeeA7QA";

    private static final String FREEBASE_MQL_ENDPOINT_SUBRELATION_RELATION = "https://www.googleapis.com/freebase/v1/mqlread/?lang=%2Flang%2Fen&query=%5B%7B%20%22mid%22%3A%20%22{id1}%22%2C%20%22{REL1}%22%3A%20%5B%7B%22{REL2}%22%3A%20%7B%22mid%22%3A%20%22{id2}%22%7D%7D%5D%7D%5D%0A%0A&key=AIzaSyCyloa8DMxhKvBVdqdk_Drf900rAeeA7QA";
    private static final String FREEBASE_MQL_ENDPOINT_RELATION = "https://www.googleapis.com/freebase/v1/mqlread/?lang=%2Flang%2Fen&query=%5B%7B%20%22mid%22%3A%20%22{id1}%22%2C%20%22{REL1}%22%3A%20%5B%7B%22mid%22%3A%20%22{id2}%22%7D%5D%7D%5D%0A%0A&key=AIzaSyCyloa8DMxhKvBVdqdk_Drf900rAeeA7QA";

    private static final String FREEBASE_INVERSE_RELATION = "https://www.googleapis.com/freebase/v1/topic{id}?filter=/type/property/reverse_property&key=AIzaSyCyloa8DMxhKvBVdqdk_Drf900rAeeA7QA";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonFactory jfactory = new JsonFactory();

    private static final Logger LOG = LoggerFactory.getLogger(FreebaseHelper.class);


    private JsonNode getURL(String metaURL) throws IOException {

        String json = null;
        try {
            json = Resources.toString(new URL(metaURL), Charsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Error talking to server, retrying after 5s", e);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                LOG.error("Interrupted sleep");
            }
            json = Resources.toString(new URL(metaURL), Charsets.UTF_8);
        }
        JsonParser jParser = jfactory.createParser(json);
        JsonNode jsonNode = MAPPER.readTree(jParser);

        return jsonNode;
    }

    /**
     * ns:business.employer.employees..business.employment_tenure.person
     * ->
     * @param old
     * @return
     */
    public static String transformOldToNewId(String old) {
        final String replaceFirst = old.replaceFirst("ns:", "/");

        final Iterable<String> split = Splitter.on("..").limit(2).split(replaceFirst);

        return Joiner.on("./").join(Iterables.transform(split, new Function<String, Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable String input) {
                return input.replaceAll("\\.", "/");
            }
        }));

    }

    /**
     * ns:business.employer.employees..business.employment_tenure.person
     * ->
     * @param newId
     * @return
     */
    public static String transformNewToOldId(String newId) {

        Iterable<String> items = Splitter.on("/").omitEmptyStrings().split(newId);

        return "ns:" + Joiner.on(".").join(items);

    }

    public Entity getNameForID(String id) throws IOException {
        String trimmedID = id.replaceAll("\\.", "/");
        if(!trimmedID.startsWith("/")) {
            trimmedID = "/" + trimmedID;
        }
        String metaURL = FREEBASE_SEARCH_API.replace("{id}", trimmedID);

        JsonNode jsonNode = getURL(metaURL);

        LOG.trace(metaURL);

        if(jsonNode.get("result").size() < 1) {
            LOG.error("No results for mid {}", id);
            return null;
        }

        JsonNode result = jsonNode.get("result").get(0);

        String name = result.has("name")?result.get("name").asText():"";

        Entity person = new Entity(id, name);

        final JsonNode output = result.get("output");

        if(output.has("type")) {
            final JsonNode types = output.get("type").get("/type/object/type");
            person.types = Lists.newArrayList(Iterables.transform(types, new Function<JsonNode, String>() {
                @Nullable
                @Override
                public String apply(@Nullable JsonNode type) {
                    return type.get("id").asText();
                }
            }));
        }

        if(output.has("/common/topic/alias")) {
            final JsonNode alias = output.get("/common/topic/alias").get("/common/topic/alias");
            if(alias != null) {
                person.alias = Lists.newArrayList(Iterables.transform(alias, new Function<JsonNode, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable JsonNode alias) {
                        return alias.asText();
                    }
                }));
            }
        }


        return person;
    }



    public Entity getNameForIDs(String id) throws IOException {

        String trimmedID = id.replaceAll("\\.", "/");
        if(!trimmedID.startsWith("/")) {
            trimmedID = "/" + trimmedID;
        }
        String metaURL = FREEBASE_TOPIC_ENDPOINT.replace("{id}", trimmedID);

        JsonNode jsonNode = getURL(metaURL);

        LOG.trace(metaURL);
        JsonNode property = jsonNode.get("property");

        // check for replaced noded ... ->
       /*[{
            "id": "/guid/9202a8c04000641f80000000002c6185",
                    "/dataworld/gardening_hint/replaced_by": [{
                "mid": null
            }]
        }] */

        if (!property.has("/type/object/name")) {
            // we need to extract the GUID
            final String guid = property.get("/type/object/guid").get("values").get(0).get("value").asText();
            final String endpoint = FREEBASE_MQL_ENDPOINT.replace("{id}", "/guid/" + guid.replaceFirst("#", ""));

            jsonNode = getURL(endpoint);

            if(jsonNode.has("result") && jsonNode.get("result").size() > 0 && jsonNode.get("result").get(0).has("/dataworld/gardening_hint/replaced_by")) {
                final String replacedByMID = jsonNode.get("result").get(0).get("/dataworld/gardening_hint/replaced_by").get(0).get("mid").asText();
                jsonNode = getURL(FREEBASE_TOPIC_ENDPOINT.replace("{id}", replacedByMID));
                property = jsonNode.get("property");
            } else {
                LOG.error("Can't find a name for {} not even a renamed instance by first aksing {}, followed by asking {} -- first answer", id, metaURL, endpoint, property.toString());
                return null;
            }
        }


        // now pick up the name of the Person
        String name = property.get("/type/object/name").get("values").get(0).get("text").asText();

        Entity person = new Entity(id, name);
        if (property.has("/common/topic/alias")) {
            final JsonNode values = property.get("/common/topic/alias").get("values"); // array
            List<String> alias = Lists.newArrayList();
            for (JsonNode value : values) {
                alias.add(value.get("text").asText());
            }
            person.alias = alias;
        }

        if(property.has("/type/object/type")) {
            person.types = Lists.newArrayList(Iterables.transform(property.get("/type/object/type").get("values"), new Function<JsonNode, String>() {
                @Nullable
                @Override
                public String apply(@Nullable JsonNode type) {
                    return type.get("id").asText();
                }
            }));
        }


        return person;
    }

    public boolean checkIfCompoundRelationHolds(String id1, String id2, String[] relation) throws IOException {

        final String metaURL;
        if (relation.length == 2) {
            metaURL = FREEBASE_MQL_ENDPOINT_SUBRELATION_RELATION
                    .replace("{id1}", id1)
                    .replace("{id2}", id2)
                    .replace("{REL1}", relation[0])
                    .replace("{REL2}", relation[1]);
        } else {
            metaURL = FREEBASE_MQL_ENDPOINT_RELATION
                    .replace("{id1}", id1)
                    .replace("{id2}", id2)
                    .replace("{REL1}", relation[0]);
        }

        LOG.trace(metaURL);

        JsonNode jsonNode = getURL(metaURL);

        return jsonNode.get("result").size() > 0;
    }

    public static final class Entity {
        String id;
        String name;

        List<String> alias;
        List<String> types;

        public Entity(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getAlias() {
            return alias;
        }

        public void setAlias(List<String> alias) {
            this.alias = alias;
        }

        public List<String> getTypes() {
            return types;
        }

        @Override
        public String toString() {
            return "Entity{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", alias=" + alias +
                    ", types=" + types +
                    '}';
        }
    }

    /**
     * Tries to find the inverse relation
     * @return
     */
    private String getInverseRelation(String relation) throws IOException {
        String trimmedID = transformOldToNewId(relation);
        if(!trimmedID.startsWith("/")) {
            trimmedID = "/" + trimmedID;
        }

        final String metaURL = FREEBASE_INVERSE_RELATION.replace("{id}", trimmedID);
        LOG.trace(metaURL);

        JsonNode jsonNode = getURL(metaURL);


        if(jsonNode.has("property")) {
            final JsonNode property = jsonNode.get("property");
            if(property.has("/type/property/reverse_property")) {
                return property.get("/type/property/reverse_property").get("values").get(0).get("id").asText();
            }
        }

        return null;
    }

    private JsonNode getFreebaseTopicForID(String freebaseID) throws IOException {
        String ask = "https://www.googleapis.com/freebase/v1/topic/{freebaseID}?key=AIzaSyCyloa8DMxhKvBVdqdk_Drf900rAeeA7QA";

        String trimmedR = freebaseID.replaceAll("\\.", "/");
        String metaURL = ask.replace("{freebaseID}", trimmedR);

        LOG.info("Asking " + trimmedR);

        JsonNode parse = getURL(metaURL);

        return parse.get("property");


    }


    public FreebaseRelation getTypesForRelationFromFreebase(String r) throws IOException {

        final String relation = r.replaceFirst("ns:", "");

        String toAsk = relation;
        String left = null;

        boolean direct = true;

        // check if we have a compound type
        if (relation.contains("..")) {
            // examples
            // ns:location.location.adjoin_s..location.adjoining_relationship.adjoins	29587812
            // ns:organization.organization.headquarters..location.mailing_address.state_province_region	2506163
            // ns:organization.organization_member.member_of..organization.organization_membership.organization	2048109
            toAsk = relation.substring(0, relation.indexOf(".."));
            left = relation.substring(relation.indexOf("..") + 2);
            direct = false;
        }

        JsonNode property;

        try {
            property = getFreebaseTopicForID(toAsk);
        } catch (FileNotFoundException e) {
            LOG.warn("Error", e);
            return null;
        }

        String one;
        String two;

        if (direct) {

            one = property.has("/type/property/schema") ?
                    property.get("/type/property/schema").get("values").get(0).get("id").asText() :
                    property.get("/type/object/type").get("values").get(0).get("id").asText();

            two = property.has("/type/property/expected_type") ?
                    property.get("/type/property/expected_type").get("values").get(0).get("id").asText() :
                    property.get("/type/object/type").get("values").get(0).get("id").asText();


        } else if (!property.has("/type/property/expected_type")) {

            one = property.has("/type/property/schema") ?
                    property.get("/type/property/schema").get("values").get(0).get("id").asText() :
                    property.get("/type/object/type").get("values").get(0).get("id").asText() ;


            // Property 2
            final JsonNode second = getFreebaseTopicForID(left);

            two = second.has("/type/property/expected_type") ?
                    second.get("/type/property/expected_type").get("values").get(0).get("id").asText() :
                    second.get("/type/object/type").get("values").get(0).get("id").asText();


        } else {

            // Property 1
            one = property.get("/type/property/schema").get("values").get(0).get("id").asText();

            // Property 2
            JsonNode second = getFreebaseTopicForID(left);

            two = second.has("/type/property/expected_type") ?
                    second.get("/type/property/expected_type").get("values").get(0).get("id").asText() :
                    second.get("/type/object/type").get("values").get(0).get("id").asText();


            // Description


        }
        one = one.replaceFirst("/", "ns:").replaceAll("/", ".");
        two = two.replaceFirst("/", "ns:").replaceAll("/", ".");
        FreebaseRelation relDescription = new FreebaseRelation(one, two, null, null);
        LOG.info(relDescription.toString());


        return relDescription;

    }


    public void readAllInverseRelations() throws IOException {
        final List<String> lines = Resources.readLines(Resources.getResource("freepal/relation-types.txt"), Charsets.UTF_8);

        FileWriter writer = new FileWriter("inversetypes.txt", false);

        for (String relations : lines) {
            final Iterable<String> split = Splitter.on("\t").trimResults().split(relations);

            final String relation = Iterables.getFirst(split, null);
            if(StringUtils.contains(relation, "..")) continue;
            final String inverseRelation;
            try {
                inverseRelation = getInverseRelation(relation);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            if(inverseRelation != null) {
                final String join = Joiner.on("\t").join(relation, inverseRelation);
                System.out.println(join);
                writer.write(join);
                writer.write(System.getProperty("line.separator"));
                writer.flush();
            }
        }

        writer.close();
    }

    public static void main(String[] args) throws IOException {
        new FreebaseHelper().readAllInverseRelations();
    }

}
