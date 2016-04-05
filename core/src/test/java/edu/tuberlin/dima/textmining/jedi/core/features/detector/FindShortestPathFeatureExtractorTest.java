package edu.tuberlin.dima.textmining.jedi.core.features.detector;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
import edu.tuberlin.dima.textmining.jedi.core.util.UIMAXMLConverterHelper;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.core.Is.is;

public class FindShortestPathFeatureExtractorTest {

	static DetectorPipeline detectorPipeline;
	private static FindShortestPathFeatureExtractor featureExtractor;
	private static UIMAXMLConverterHelper uimaxmlConverterHelper;

	static final Logger LOG = LoggerFactory.getLogger(FindShortestPathFeatureExtractorTest.class);

	@BeforeClass
	public static void setUp() throws Throwable {
		detectorPipeline = new DetectorPipeline("-lang en -testMode");
		featureExtractor = new FindShortestPathFeatureExtractor("-lemmatize -resolveCoreferences -pickupSimilar -selectionType " + N.class.getName() + " -additionalSelectionType " + PR.class.getName());

		uimaxmlConverterHelper = new UIMAXMLConverterHelper(true);
	}

	@Test
	public void testFind() throws Exception {

		final List<FoundFeature<Annotation>> exec = parse("Dmitry was born in Moscow. In 1993 he graduated from Russian Academy of Theatre Arts as stage director. ");

		Assert.assertThat(exec.size(), is(4));
		Assert.assertThat(exec.get(0).getEntity1().getCoveredText(), is("Dmitry"));
		Assert.assertThat(exec.get(0).getEntity2().getCoveredText(), is("Moscow"));

		Assert.assertThat(exec.get(3).getEntity1().getCoveredText(), is("Russian Academy of Theatre Arts"));
		Assert.assertThat(exec.get(3).getEntity2().getCoveredText(), is("stage director"));

	}

	@Test
	public void testFindOf() throws Exception {

		final List<FoundFeature<Annotation>> exec = parse(
			"Adam received a Bachelor of Science in commerce and business administration from the University of Illinois at Urbana-Champaign. He also holds a Master of Business Administration degree.");

		Assert.assertThat(exec.size(), is(16));
		Assert.assertThat(exec.get(0).getEntity1().getCoveredText(), is("Adam"));
		Assert.assertThat(exec.get(0).getEntity2().getCoveredText(), is("Bachelor of Science"));

		LOG.info(Joiner.on("\n").join(exec));

	}

	private List<FoundFeature<Annotation>> parse(String text) throws Exception {
		JCas cas = detectorPipeline.exec(Lists.newArrayList(text, "id"));
		return detect(cas);
	}

	private List<FoundFeature<Annotation>> parseFromXML(String xml) throws Exception {
		JCas deserialize = uimaxmlConverterHelper.deserialize(xml, JCasFactory.createJCas());
		LOG.info(deserialize.getDocumentText());

		return detect(deserialize);
	}

	private List<FoundFeature<Annotation>> detect(JCas cas) throws Exception {
		List<FoundFeature<Annotation>> exec = featureExtractor.exec(cas);

		LOG.info(Strings.repeat("-", 50));
		LOG.info("Found features \n{}", Joiner.on("\n").join(exec));

		return exec;
	}

	@Test
	public void testParse1() throws Throwable {

		final List<FoundFeature<Annotation>> exec = parse("Rob Chandra is an early investor in Kovio.");

		Assert.assertThat(exec.size(), is(2));
		Assert.assertThat(exec.get(0).getEntity1().getCoveredText(), is("Rob Chandra"));
		Assert.assertThat(exec.get(0).getEntity2().getCoveredText(), is("Kovio"));

		Assert.assertThat(exec.get(1).getEntity1().getCoveredText(), is("Rob Chandra"));
		Assert.assertThat(exec.get(1).getEntity2().getCoveredText(), is("Kovio"));

	}

	@Test
	public void testParse2() throws Throwable {

		final List<FoundFeature<Annotation>> exec = parse(
			"Martin Blank is an American glass artist who was born August 29, 1962. He received a BFA degree from the Rhode Island School of Design in 1984 with a major in glass.");
	}

	@Test
	public void testParse3() throws Throwable {

		final List<FoundFeature<Annotation>> exec = parse("The cognitive model was developed by Aaron Beck at the University of Pennsylvania");

		Assert.assertThat(exec.size(), is(3));
		Assert.assertThat(exec.get(0).getEntity1().getCoveredText(), is("model"));
		Assert.assertThat(exec.get(1).getEntity2().getCoveredText(), is("University of Pennsylvania"));
		Assert.assertThat(exec.get(2).getEntity1().getCoveredText(), is("Aaron Beck"));

	}

	@Test
	public void testParse4() throws Throwable {


		final List<FoundFeature<Annotation>> exec = parse("In June 2003 André Previn again performed the work together with the London Symphony Orchestra and original cast.");

		Assert.assertThat(exec.size(), is(6));
		Assert.assertThat(exec.get(0).getEntity1().getCoveredText(), is("André Previn"));
		Assert.assertThat(exec.get(0).getEntity2().getCoveredText(), is("work"));
		Assert.assertThat(exec.get(1).getEntity1().getCoveredText(), is("André Previn"));
		Assert.assertThat(exec.get(1).getEntity2().getCoveredText(), is("London Symphony Orchestra"));

	}

	@Test
	public void testSonOf() throws Exception {

		String xml = Resources.toString(Resources.getResource("sampleCAS/sonofcas.xml"), Charsets.UTF_8);

		List<FoundFeature<Annotation>> exec = parseFromXML(xml);


		Assert.assertThat(exec.size(), is(11));

		assertEntity(exec.get(3), "Lewis Yelland Andrews", "A.E. Andrews", "[X] be son of [Y] [1-attr-2,1-nsubj-0,2-prep-3,3-pobj-4]");

		assertEntity(exec.get(5), "Lewis Yelland Andrews", "A.E. Andrews", "[X] of [Y] [0-prep-1,1-pobj-2]");
		assertEntity(exec.get(9), "A.E. Andrews", "Imperial Forces", "[X] fight for [Y] [1-nsubj-0,1-prep-2,2-pobj-3]");
		assertEntity(exec.get(10), "World War I", "Imperial Forces", "fight in [X] for [Y] [0-prep-1,0-prep-3,1-pobj-2,3-pobj-4]");


	}

	@Test
	public void testParse5() throws Exception {
		final List<FoundFeature<Annotation>> exec = parse("Lonnen was born in Kingston upon Hull, Yorkshire into a theatrical family. ");

		Assert.assertThat(exec.size(), is(6));

		assertEntity(exec.get(0), "Lonnen", "Kingston", "[X] bear in [Y] [1-nsubjpass-0,1-prep-2,2-pobj-3]");
		assertEntity(exec.get(1), "Lonnen", "Hull, Yorkshire", "[X] bear upon [Y] [1-nsubjpass-0,1-prep-2,2-pobj-3]");
		assertEntity(exec.get(2), "Lonnen", "family", "[X] bear into [Y] [1-nsubjpass-0,1-prep-2,2-pobj-3]");
		assertEntity(exec.get(3), "Kingston", "Hull, Yorkshire", "bear in [X] upon [Y] [0-prep-1,0-prep-3,1-pobj-2,3-pobj-4]");
		assertEntity(exec.get(4), "Kingston", "family", "bear in [X] into [Y] [0-prep-1,0-prep-3,1-pobj-2,3-pobj-4]");
		assertEntity(exec.get(5), "Hull, Yorkshire", "family", "bear upon [X] into [Y] [0-prep-1,0-prep-3,1-pobj-2,3-pobj-4]");

	}

	@Test
	public void testConjunctionBeginning() throws Exception {
		List<FoundFeature<Annotation>> parse = parse("The ANSA news agency said the operation involved police in Umbria and Lazio provinces in central Italy and Campania province in the south , without specifying exact arrest locations .");

		assertEntity(parse.get(14), "Umbria", "Italy", "[X] province in [Y] [1-nn-0,1-prep-2,2-pobj-3]");
	}

	@Test
	public void testBornIn1() throws Exception {
		final List<FoundFeature<Annotation>> parse = parse("Morris Smith Miller (July 31, 1779 -- November 16, 1824) was a United States Representative from New York. Born in New York City, he graduated from Union College in Schenectady in 1798. He studied law and was admitted to the bar. Miller served as private secretary to Governor Jay, and subsequently, in 1806, commenced the practice of his profession in Utica. He was president of the village of Utica in 1808 and judge of the court of common pleas of Oneida County from 1810 until his death.");

		Assert.assertThat(parse.get(0).getEntity1().getCoveredText(), is("Morris Smith Miller"));
		Assert.assertThat(parse.get(0).getEntity2().getCoveredText(), is("New York"));

		Assert.assertThat(parse.get(1).getEntity1().getCoveredText(), is("Morris Smith Miller"));
		Assert.assertThat(parse.get(1).getEntity2().getCoveredText(), is("New York"));

		Assert.assertThat(parse.get(2).getEntity1().getCoveredText(), is("New York City"));
		Assert.assertThat(parse.get(2).getEntity2().getCoveredText(), is("Morris Smith Miller"));
	}

	@Test
	public void testBornOn() throws Exception {
		final List<FoundFeature<Annotation>> parse = parse("Adam was born on 7 March 1891 in Vitebsk, Russian Empire. He enlisted in the Russian Navy to become a military officer, finishing the Naval Corps School in Saint Petersburg in 1911.");

		Assert.assertThat(parse.get(0).getEntity1().getCoveredText(), is("Adam"));
		Assert.assertThat(parse.get(0).getEntity2().getCoveredText(), is("Vitebsk"));
	}

	@Test
	public void testBornPlaceParataxis() throws Exception {
		List<FoundFeature<Annotation>> parse = parse("Sinclair, was born in Russia in 1890 and educated in Philadelphia, before attending St Leonards School in St Andrews, where women's lacrosse had been introduced by Louisa Lumsden. Lumsden brought the game to Scotland after watching a men's lacrosse game between the Canghuwaya Indians and the Montreal Lacrosse Club.");

		assertEntity(parse.get(0), "Sinclair", "Russia", "[X] bear in [Y] [1-nsubjpass-0,1-prep-2,2-pobj-3]");

	}

	@Test
	public void testJoiner() throws Exception {

		String sentence = "STOCKTON-ON-TEES , England  AP  - Michael Minns received $160 , 000 from the father he never knew and thought was killed in World War II .";
		final List<FoundFeature<Annotation>> parse = parse(sentence);

		Assert.assertThat(parse.get(0).getEntity1().getCoveredText(), is("STOCKTON-ON-TEES"));
		Assert.assertThat(parse.get(0).getEntity2().getCoveredText(), is("England"));

		Assert.assertThat(parse.get(1).getEntity1().getCoveredText(), is("STOCKTON-ON-TEES"));
		Assert.assertThat(parse.get(1).getEntity2().getCoveredText(), is("AP"));

	}

	@Test
	public void testCoref() throws Exception {
		final List<FoundFeature<Annotation>> parse = parse("Dimitry was born in Moscow. In 1993 he graduated from Russian Academy of Theatre Arts as a stage director.");

		Assert.assertThat(parse.get(0).getEntity1().getCoveredText(), is("Dimitry"));
		Assert.assertThat(parse.get(0).getEntity2().getCoveredText(), is("Moscow"));

		Assert.assertThat(parse.get(3).getEntity1().getCoveredText(), is("Russian Academy of Theatre Arts"));
		Assert.assertThat(parse.get(3).getEntity2().getCoveredText(), is("stage director"));

	}

	@Test
	public void testParseConj() throws Exception {

		List<FoundFeature<Annotation>> parse = parse("His daughter was actress Jessie Lonnen, who performed with George Edwardes's company in England and the J. C. Williamson company in Australia.");

		Assert.assertThat(parse.get(0).getEntity1().getCoveredText(), is("daughter"));
		Assert.assertThat(parse.get(0).getEntity2().getCoveredText(), is("George Edwardes"));

	}

	@Test
	public void testCoref2() throws Exception {
		List<FoundFeature<Annotation>> parse = parse("Martin Blank is an American glass artist. He received a BFA degree from the Rhode Island School of Design in 1984.");

		Assert.assertThat(parse.get(0).getEntity1().getCoveredText(), is("Martin Blank"));
		Assert.assertThat(parse.get(0).getEntity2().getCoveredText(), is("BFA degree"));
		Assert.assertThat(parse.get(0).getPattern(), is("[X] receive [Y] [1-dobj-2,1-nsubj-0]"));


	}

	@Test
	public void testBornInBrackets() throws Exception {
		final List<FoundFeature<Annotation>> parse = parse("Günter Theodor Netzer (born 14 September 1944 in Mönchengladbach) is a former German football player and team general manager currently working in the media business.");


		assertEntity(parse.get(0), "Günter Theodor Netzer", "Mönchengladbach", "[X] bear in [Y] [1-nsubjpass-0,1-prep-2,2-pobj-3]");
		assertEntity(parse.get(1), "Günter Theodor Netzer", "football player and team general manager", "[X] bear [Y] work [1-advcl-3,1-nsubjpass-0,3-attr-2]");
		assertEntity(parse.get(2), "Günter Theodor Netzer", "business", "[X] bear work in [Y] [1-advcl-2,1-nsubjpass-0,2-prep-3,3-pobj-4]");
		assertEntity(parse.get(3), "Mönchengladbach", "football player and team general manager", "bear in [X] [Y] work [0-advcl-4,0-prep-1,1-pobj-2,4-attr-3]");
		assertEntity(parse.get(4), "Mönchengladbach", "business", "bear in [X] work in [Y] [0-advcl-3,0-prep-1,1-pobj-2,3-prep-4,4-pobj-5]");
		assertEntity(parse.get(5), "football player and team general manager", "business", "[X] work in [Y] [1-attr-0,1-prep-2,2-pobj-3]");

	}

	@Test
	public void testBornInBrackets2() throws Exception {
		final List<FoundFeature<Annotation>> parse = parse("Vanessa Chinitor (born 13 October 1976, Dendermonde) is a Belgian singer");

		assertEntity(parse.get(0), "Vanessa Chinitor", "Dendermonde", "[X] bear in [Y] [1-npadvmod-0,1-prep-2,2-pobj-3]");
		assertEntity(parse.get(1), "Dendermonde", "singer", "Chinitor [X] be [Y] [0-appos-1,2-attr-3,2-nsubj-0]");

	}

	@Test
	public void testConjunctions() throws Exception {
		List<FoundFeature<Annotation>> parse = parse("John Milne Bramwell (1852 -- 1925) was a Scottish physician and author, born at Perth, and educated at the University of Edinburgh.");

		assertEntity(parse.get(1), "John Milne Bramwell", "Perth", "[X] be bear at [Y] [1-advcl-2,1-nsubj-0,2-prep-3,3-pobj-4]");


	}

	@Test
	public void testName() throws Exception {

		parse("Lonnen died of tuberculosis. His daughter was actress Jessie Lonnen.");

	}

	@Test
	public void testBornAt() throws Exception {
		List<FoundFeature<Annotation>> parse = parse("Morsztyn was born 24 July 1621 at Wiśnicz, near Kraków.");

		Assert.assertThat(parse.get(0).getEntity1().getCoveredText(), is("Morsztyn"));
		Assert.assertThat(parse.get(0).getEntity2().getCoveredText(), is("Wiśnicz"));
		Assert.assertThat(parse.get(0).getPattern(), is("[X] bear at [Y] [1-nsubjpass-0,1-prep-2,2-pobj-3]"));
		// [X] bear July at [Y] [1-npadvmod-2,1-nsubjpass-0,2-prep-3,3-pobj-4]

	}

	@Test
	public void testBornIn() throws Exception {
		List<FoundFeature<Annotation>> parse = parse("Morsztyn was born 24 July 1621 in Wiśnicz, near Kraków.");

		Assert.assertThat(parse.get(0).getEntity1().getCoveredText(), is("Morsztyn"));
		Assert.assertThat(parse.get(0).getEntity2().getCoveredText(), is("Wiśnicz"));
		Assert.assertThat(parse.get(0).getPattern(), is("[X] bear in [Y] [1-nsubjpass-0,1-prep-2,2-pobj-3]"));
		// [X] bear July at [Y] [1-npadvmod-2,1-nsubjpass-0,2-prep-3,3-pobj-4]

	}

	@Test
	public void testCollapseSimilar() throws Exception {
		List<FoundFeature<Annotation>> parse = parse("Lonnen died of tuberculosis at the age of 41 and was buried at the Norwood Cemetery. His daughter was actress Jessie Lonnen.");

		Assert.assertThat(parse.get(0).getEntity1().getCoveredText(), is("Lonnen"));
		Assert.assertThat(parse.get(0).getEntity2().getCoveredText(), is("tuberculosis"));
		Assert.assertThat(parse.get(0).getPattern(), is("[X] die of [Y] [1-nsubj-0,1-prep-2,2-pobj-3]"));

	}

	@Test
	public void testBrackets() throws Exception {
		List<FoundFeature<Annotation>> parse = parse("Henry Hall (born 1810 at Sheffield; died 1 December 1864 at Nottingham) ");

		assertEntity(parse.get(0), "Henry Hall", "Sheffield", "[X] bear at [Y] [1-nsubj-0,1-prep-2,2-pobj-3]");
		assertEntity(parse.get(1), "Henry Hall", "Nottingham", "[X] die at [Y] [1-nsubj-0,1-prep-2,2-pobj-3]");
	}

	@Test
	public void testReplaceHer() throws Exception {
		List<FoundFeature<Annotation>> parse = parse("Born in 1953 in Harrogate, Yorkshire, England, Carol was educated in Whitby by Anglican nuns within sight of Whitby Abbey. Her love of ancient and medieval history took her to London University where she read History at Royal Holloway College. Her first novel, set in 11th century England and published by Mills & Boon, won the RNA New Writers' Award in 1989. For the last 20 years she has lived in London with her husband and daughter.");


	}

	@Test
	public void testBrackets2() throws Exception {
		List<FoundFeature<Annotation>> parse = parse("Max Ferguson (born 1959, New York City) is an American artist best known for his realistic paintings of vanishing urban scenes in and around New York City.");

		assertEntity(parse.get(0), "Max Ferguson", "New York City", "[X] bear in [Y] [1-npadvmod-0,1-prep-2,2-pobj-3]");

	}


	@Test
	public void testBornNear() throws Exception {

		List<FoundFeature<Annotation>> parse = parse("Njavro was born in Cerovica, near Neum in the Kingdom of Yugoslavia (today part of Bosnia and Herzegovina). He attended elementary school here and gymnasium in Dubrovnik, Croatia.");

		assertEntity(parse.get(0), "Njavro", "Cerovica", "[X] bear in [Y] [1-nsubjpass-0,1-prep-2,2-pobj-3]");
		assertEntity(parse.get(1), "Njavro", "Neum", "[X] bear near [Y] [1-nsubjpass-0,1-prep-2,2-pobj-3]");
	}

	@Test
	public void testBornBracket() throws Exception {
		List<FoundFeature<Annotation>> parse = parse("Juan Laporte (born November 24, 1959) is a former boxer who was born in Guayama, Puerto Rico.");

		assertEntity(parse.get(0), "Juan Laporte", "Guayama, Puerto Rico", "[X] be boxer bear in [Y] [1-attr-2,1-nsubj-0,2-rcmod-3,3-prep-4,4-pobj-5]");
		assertEntity(parse.get(1), "Juan Laporte", "Guayama, Puerto Rico", "[X] bear in [Y] [0-rcmod-1,1-prep-2,2-pobj-3]");
	}

	@Test
	public void testCoreference() throws Exception {

		List<FoundFeature<Annotation>> parse = parse("Prior to Microsoft, Adam was a consultant with Andersen Consulting for three years. While at Andersen Consulting, Adam worked with clients including those in financial services, government, and utilities. Adam received a Bachelor of Science in commerce and business administration from the University of Illinois at Urbana-Champaign. He also holds a Master of Business Administration degree.");

		assertEntity(parse.get(0), "Microsoft", "Adam", "prior to [X] [Y] be [0-prep-1,1-pobj-2,4-advmod-0,4-nsubj-3]");
		assertEntity(parse.get(1), "Microsoft", "consultant", "prior to [X] be [Y] [0-prep-1,1-pobj-2,3-advmod-0,3-attr-4]");
		assertEntity(parse.get(2), "Microsoft", "Andersen Consulting", "prior to [X] be consultant with [Y] [0-prep-1,1-pobj-2,3-advmod-0,3-attr-4,4-prep-5,5-pobj-6]");
		assertEntity(parse.get(3), "Adam", "Andersen Consulting", "[X] be consultant with [Y] [1-attr-2,1-nsubj-0,2-prep-3,3-pobj-4]");
		assertEntity(parse.get(4), "Adam", "Andersen Consulting", "[X] with [Y] [0-prep-1,1-pobj-2]");
		assertEntity(parse.get(5), "Andersen Consulting", "Adam", "at [X] [Y] work [0-pobj-1,3-nsubj-2,3-prep-0]");
		assertEntity(parse.get(6), "Andersen Consulting", "government", "at [X] work with client include those in [Y] [0-pobj-1,2-prep-0,2-prep-3,3-pobj-4,4-prep-5,5-pobj-6,6-prep-7,7-pobj-8]");
		assertEntity(parse.get(7), "Adam", "government", "[X] work with client include those in [Y] [1-nsubj-0,1-prep-2,2-pobj-3,3-prep-4,4-pobj-5,5-prep-6,6-pobj-7]");
		assertEntity(parse.get(8), "Adam", "Bachelor of Science", "[X] receive [Y] [1-dobj-2,1-nsubj-0]");
		assertEntity(parse.get(9), "Adam", "commerce", "[X] receive bachelor in [Y] [1-dobj-2,1-nsubj-0,2-prep-3,3-pobj-4]");
		assertEntity(parse.get(10), "Adam", "business administration", "[X] receive bachelor in [Y] [1-dobj-2,1-nsubj-0,2-prep-3,3-pobj-4]");
		assertEntity(parse.get(11), "Adam", "University of Illinois", "[X] receive bachelor in commerce administration from [Y] [1-dobj-2,1-nsubj-0,2-prep-3,3-pobj-4,4-conj-5,5-prep-6,6-pobj-7]");
		assertEntity(parse.get(12), "Adam", "Urbana-Champaign", "[X] receive bachelor in commerce administration from University at [Y] [1-dobj-2,1-nsubj-0,2-prep-3,3-pobj-4,4-conj-5,5-prep-6,6-pobj-7,7-prep-8,8-pobj-9]");


	}

	@Test
	public void testCoref3() throws Exception {

		List<FoundFeature<Annotation>> parse = parse("Eric was born in Bronx, New York. He received a B.A. in French.");
		assertEntity(parse.get(0), "Eric", "Bronx, New York", "[X] bear in [Y] [1-nsubjpass-0,1-prep-2,2-pobj-3]");
		assertEntity(parse.get(1), "Eric", "B.A.", "[X] receive [Y] [1-dobj-2,1-nsubj-0]");
		assertEntity(parse.get(2), "Eric", "French", "[X] receive in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]");
		assertEntity(parse.get(3), "B.A.", "French", "receive [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]");
	}

	@Test
	public void testSampleSentence() throws Exception {

		List<FoundFeature<Annotation>> parse = parse("Bell , a telecommunication company , which is based in Los Angeles , makes and distributes electronic , computer and building products");

		assertEntity(parse.get(1), "Bell", "Los Angeles",  "[X] company base in [Y] [0-appos-1,1-rcmod-2,2-prep-3,3-pobj-4]");
		assertEntity(parse.get(2), "Bell", "computer",  "[X] make distribute electronic [Y] product [1-conj-2,1-nsubj-0,2-dobj-5,3-conj-4,5-amod-3]");
		assertEntity(parse.get(3), "Bell", "building",  "[X] make distribute electronic computer [Y] product [1-conj-2,1-nsubj-0,2-dobj-6,3-conj-4,4-conj-5,6-amod-3]");

	}

	@Test
	public void testParsePhrase() throws Exception {

		List<FoundFeature<Annotation>> parse = parse("Warga received a Bachelor of Science in anthropology from UC Davis in 1995 and a Master of Arts in visual anthropology from Goldsmiths.");


		assertEntity(parse.get(0), "Warga", "Bachelor of Science", "[X] receive [Y] [1-dobj-2,1-nsubj-0]");
		assertEntity(parse.get(3), "Warga", "Master of Arts", "[X] receive in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]");
	}

	@Test
	public void testPatternDetect() throws Exception {
		List<FoundFeature<Annotation>> parse = parseFromXML(Resources.toString(Resources.getResource("sampleCAS/sampleSentenceCAS.xml"), Charsets.UTF_8));

		Assert.assertThat(parse.size(), is(49));
		assertEntity(parse.get(37), "Bachelor of Music", "Villanova", "receive [X] in 1984 master from [Y] [0-dobj-1,0-prep-2,2-pobj-3,3-conj-4,4-prep-5,5-pobj-6]");

	}

	private void assertEntity(FoundFeature<Annotation> toCheck, String entity1, String entity2, String pattern) {
		Assert.assertThat(toCheck.getEntity1().getCoveredText(), is(entity1));
		Assert.assertThat(toCheck.getEntity2().getCoveredText(), is(entity2));
		Assert.assertThat(toCheck.getPattern(), is(pattern));
	}

}
