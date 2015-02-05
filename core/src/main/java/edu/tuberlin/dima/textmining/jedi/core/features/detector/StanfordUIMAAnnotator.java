/**
 * Copyright (c) 2011, Regents of the University of Colorado
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For a complete copy of the license please see the file LICENSE distributed
 * with the cleartk-stanford-corenlp project or visit
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package edu.tuberlin.dima.textmining.jedi.core.features.detector;

import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Adapted from
 * http://cleartk.googlecode.com/git/cleartk-stanford-corenlp/src/main/java/org/cleartk/stanford/corenlp/StanfordCoreNlpAnnotator.java
 * <p/>
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p/>
 * This uses the Stanford NER suite to parse an english document - copying over all annotations except the dependency parse
 *
 * @author Steven Bethard
 * @author Johannes Kirschnick
 */
public class StanfordUIMAAnnotator extends JCasAnnotator_ImplBase {

    private MappingProvider posMappingProvider;
    private MappingProvider nerMappingProvider;
    private CasConfigurableProviderBase<StanfordCoreNLP> modelProvider;

    public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
        return AnalysisEngineFactory.createEngineDescription(StanfordUIMAAnnotator.class);
    }

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

        modelProvider = new CasConfigurableProviderBase<StanfordCoreNLP>() {

            {
                setDefault(LOCATION, "classpath:/mappings/combined-ner.crf.map");
            }

            @Override
            protected StanfordCoreNLP produceResource(URL aUrl) throws IOException {
                Properties properties = new Properties();
                properties.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");

                return new StanfordCoreNLP(properties);
            }
        };


        posMappingProvider = new MappingProvider();
        posMappingProvider.setDefaultVariantsLocation(
                "de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/tagger-default-variants.map");
        posMappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/" +
                "core/api/lexmorph/tagset/${language}-${pos.tagset}-pos.map");
        posMappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
        posMappingProvider.setDefault("pos.tagset", "default");


        nerMappingProvider = new MappingProvider();
        nerMappingProvider
                .setDefaultVariantsLocation("de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/ner-default-variants.map");
        nerMappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/mappings/combined-ner.crf.map");
        nerMappingProvider.setDefault(MappingProvider.BASE_TYPE, NamedEntity.class.getName());
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

        CAS cas = jCas.getCas();
        posMappingProvider.configure(cas);
        nerMappingProvider.configure(cas);

        modelProvider.configure(cas);

        Annotation document = modelProvider.getResource().process(jCas.getDocumentText());

        String lastNETag = "O";
        int lastNEBegin = -1;
        int lastNEEnd = -1;
        for (CoreMap tokenAnn : document.get(TokensAnnotation.class)) {

            // create the token annotation
            int begin = tokenAnn.get(CharacterOffsetBeginAnnotation.class);
            int end = tokenAnn.get(CharacterOffsetEndAnnotation.class);
            String pos = tokenAnn.get(PartOfSpeechAnnotation.class);
            String lemma = tokenAnn.get(LemmaAnnotation.class);

            Token token = new Token(jCas, begin, end);

            Type posTag = posMappingProvider.getTagType(pos);
            POS posAnno = (POS) cas.createAnnotation(posTag, begin, end);
            posAnno.setStringValue(posTag.getFeatureByBaseName("PosValue"), pos.intern());
            posAnno.addToIndexes();
            token.setPos(posAnno);


            Lemma dkproLemma = new Lemma(jCas, begin, end);
            dkproLemma.setValue(lemma);
            dkproLemma.addToIndexes();


            token.setLemma(dkproLemma);
            token.addToIndexes();

            // hackery to convert token-level named entity tag into phrase-level tag
            String neTag = tokenAnn.get(NamedEntityTagAnnotation.class);
            if(neTag == null) continue;
            if (neTag.equals("O") && !lastNETag.equals("O")) {

                Type type = nerMappingProvider.getTagType(lastNETag);
                NamedEntity neAnno = (NamedEntity) cas.createAnnotation(type, lastNEBegin, lastNEEnd);
                neAnno.setValue(lastNETag);
                neAnno.addToIndexes();

            } else {
                if (lastNETag.equals("O")) {
                    lastNEBegin = begin;
                } else if (lastNETag.equals(neTag)) {
                    // do nothing - begin was already set
                } else {

                    Type type = nerMappingProvider.getTagType(lastNETag);
                    NamedEntity neAnno = (NamedEntity) cas.createAnnotation(type, lastNEBegin, lastNEEnd);
                    neAnno.setValue(lastNETag);
                    neAnno.addToIndexes();

                    lastNEBegin = begin;
                }
                lastNEEnd = end;
            }
            lastNETag = neTag;
        }
        if (!lastNETag.equals("O")) {

            Type type = nerMappingProvider.getTagType(lastNETag);
            NamedEntity neAnno = (NamedEntity) cas.createAnnotation(type, lastNEBegin, lastNEEnd);
            neAnno.setValue(lastNETag);
            neAnno.addToIndexes();

        }

        // add sentences and trees
        List<CoreMap> sentenceAnnotations = document.get(SentencesAnnotation.class);
        for (CoreMap sentenceAnn : sentenceAnnotations) {

            // add the sentence annotation
            int sentBegin = sentenceAnn.get(CharacterOffsetBeginAnnotation.class);
            int sentEnd = sentenceAnn.get(CharacterOffsetEndAnnotation.class);
            Sentence sentence = new Sentence(jCas, sentBegin, sentEnd);
            sentence.addToIndexes();

        }

        Map<Integer, CorefChain> corefChains = document.get(CorefChainAnnotation.class);
        if(corefChains != null) {
            for (CorefChain chain : corefChains.values()) {
                CoreferenceLink last = null;
                for (CorefMention mention : chain.getMentionsInTextualOrder()) {


                    CoreLabel beginLabel = sentenceAnnotations.get(mention.sentNum - 1)
                            .get(TokensAnnotation.class).get(mention.startIndex - 1);
                    CoreLabel endLabel = sentenceAnnotations.get(mention.sentNum - 1).get(TokensAnnotation.class)
                            .get(mention.endIndex - 2);
                    CoreferenceLink link = new CoreferenceLink(jCas, beginLabel.get(CharacterOffsetBeginAnnotation.class)
                            , endLabel.get(CharacterOffsetEndAnnotation.class));

                    if (mention.mentionType != null) {
                        link.setReferenceType(mention.mentionType.toString());
                    }

                    if (last == null) {
                        // This is the first mention. Here we'll initialize the chain
                        CoreferenceChain corefChain = new CoreferenceChain(jCas);
                        corefChain.setFirst(link);
                        corefChain.addToIndexes();
                    } else {
                        // For the other mentions, we'll add them to the chain.
                        last.setNext(link);
                    }
                    last = link;

                    link.addToIndexes();
                }
            }
        }




    }


}