package edu.tuberlin.dima.textmining.jedi.core.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

/**
 * Date: 15.07.2014
 * Time: 16:36
 *
 * @author Johannes Kirschnick
 */
public class AnnovisTransformerWriter {

    private static final String DASH = "_";

    public static Map<String, Annovis> generateFormat(JCas jCas) {

        StringBuilder stringBuilder = new StringBuilder();

        // all tokens
        final List<Token> tokens = Lists.newArrayList(JCasUtil.select(jCas, Token.class));

        Annovis spans = new Annovis(AnnovisType.span);
        Annovis links = new Annovis(AnnovisType.link);

        int counter = 0;
        for (Token token : tokens) {

            POS pos = token.getPos();

            spans.values.add(
                    new AnnovisValue(
                            counter,
                            pos != null ? pos.getPosValue() : DASH,
                            Lists.newArrayList(token.getBegin(), token.getEnd())));

            List<Dependency> deps = selectCovered(Dependency.class, token);

            String dependencyType = "ROOT";
            int governorID = 0;

            if(deps.size() == 1) {
                Dependency dep = deps.get(0);
                dependencyType = dep.getDependencyType();

                Token governor = dep.getGovernor();

                governorID = tokens.indexOf(governor);

                links.values.add(new AnnovisValue(null, dependencyType, Lists.newArrayList(governorID, counter)));
            }

            counter++;
        }
        Map<String, Annovis> annovisTypeAnnovisMap = Maps.newHashMap();
        annovisTypeAnnovisMap.put("layer+1", spans);
        annovisTypeAnnovisMap.put("layer+2", links);

        // now add core-links if there are any
        Annovis corefs = new Annovis(AnnovisType.span);
        Annovis corefsLinks = new Annovis(AnnovisType.link);
        for (CoreferenceChain coreferenceChain : JCasUtil.select(jCas, CoreferenceChain.class)) {
            final CoreferenceLink first = coreferenceChain.getFirst();

            CoreferenceLink last = first;
            while (last.getNext() != null) {
                final AnnovisValue marker = new AnnovisValue(
                        first.hashCode(),
                        "Mention",
                        Lists.newArrayList(first.getBegin(), first.getEnd()));


                // each one is a link
                last = last.getNext();
                if (first != last) {
                    // we have a link
                    // from last -> first


                    final AnnovisValue marker2 = new AnnovisValue(
                            last.hashCode(),
                            "Mention",
                            Lists.newArrayList(last.getBegin(), last.getEnd()));

                    corefs.getValues().add(marker);
                    corefs.getValues().add(marker2);

                    corefsLinks.values.add(new AnnovisValue(null, "Coref-" + last.getReferenceType(), Lists.newArrayList(last.hashCode(), first.hashCode())));
                }
            }
        }

        annovisTypeAnnovisMap.put("layer+3", corefs);
        annovisTypeAnnovisMap.put("layer+4", corefsLinks);



        return annovisTypeAnnovisMap;

    }

    public static class Annovis {
        /*
        //type: "annotation"
            layer+1":{
            //background: '#0f0',
            type: "annotation",
            values: [
                {id:1,  label:"EX", pos:[0, 5], color:'#fff', borderColor:'#f90'},
                {id:2,  label:"VBP",pos:[6, 9], fontColor: '#09f'},
                {id:3,  label:"CD", pos:[10, 14]},
        */

        AnnovisType type;
        Set<AnnovisValue> values;

        public Annovis(AnnovisType type) {
            this.type = type;
            values = Sets.newHashSet();
        }

        public AnnovisType getType() {
            return type;
        }

        public Set<AnnovisValue> getValues() {
            return values;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Annovis annovis = (Annovis) o;

            if (type != annovis.type) return false;
            if (values != null ? !values.equals(annovis.values) : annovis.values != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (values != null ? values.hashCode() : 0);
            return result;
        }
    }

    public enum AnnovisType {
        span,
        externLink,
        link
    }

    public static class AnnovisValue {
        Integer id;
        String label;
        List<Integer> pos;

        public AnnovisValue(Integer id, String label, List<Integer> pos) {
            this.id = id;
            this.label = label;
            this.pos = pos;
        }

        public Integer getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public List<Integer> getPos() {
            return pos;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AnnovisValue that = (AnnovisValue) o;

            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (!label.equals(that.label)) return false;
            if (!pos.equals(that.pos)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + label.hashCode();
            result = 31 * result + pos.hashCode();
            return result;
        }
    }


}
