package edu.tuberlin.dima.textmining.jedi.core.util;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Resources;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Collection;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

public class UIMAXMLConverterHelperTest {

    private JCas testCas;
    private UIMAXMLConverterHelper converterHelper;

    @Before
    public void setUp() throws Exception {
        try {
            testCas = JCasFactory.createJCas();
            converterHelper = new UIMAXMLConverterHelper(true);
        } catch (ResourceInitializationException e) {
            throw new IllegalArgumentException(e);
        }

    }


    @Test
    public void testConvert() throws Exception {

        testCas.reset();

		URL input = Resources.getResource("sampleCAS/websitesamplecas.xml");

		converterHelper.deserialize(Resources.toString(input, Charsets.UTF_8), testCas);

        Collection<NamedEntity> namedEntities = JCasUtil.select(testCas, NamedEntity.class);

        for (NamedEntity namedEntity : namedEntities) {
            System.out.println(namedEntity.getType() + " " + namedEntity.getCoveredText());
        }
        System.out.println(Joiner.on("").join(namedEntities));

		Assert.assertThat(namedEntities, is(not(empty())));
	}

}
