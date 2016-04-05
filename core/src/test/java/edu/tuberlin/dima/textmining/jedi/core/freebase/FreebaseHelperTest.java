package edu.tuberlin.dima.textmining.jedi.core.freebase;

import edu.tuberlin.dima.textmining.jedi.core.model.FreebaseRelation;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

public class FreebaseHelperTest {

	@Test
	public void testGetTopicForID() throws Exception {
		FreebaseHelper freebaseHelper = new FreebaseHelper();

/*
{"pred":"/people/person/education./education/education/degree",
"sub":"/m/0g6g2m",
"obj":"/m/02h4rq6",
"evidences":[{"url":"http://en.wikipedia.org/wiki/Adam_Hecktman","snippet":"Prior to Microsoft, Adam was a consultant with Andersen Consulting for three years. While at Andersen Consulting, Adam worked with clients including those in financial services, government, and utilities. Adam received a ((NAM: Bachelor of Science)) in commerce and business administration from the University of Illinois at Urbana-Champaign. He also holds a Master of Business Administration degree."}],"judgments":[{"rater":"1701217270337547159","judgment":"yes"},{"rater":"16812935633072558077","judgment":"yes"},{"rater":"5521403179797574771","judgment":"yes"},{"rater":"8046943553957200519","judgment":"yes"},{"rater":"9448866739620283545","judgment":"yes"}]}
         */

		FreebaseHelper.Entity subject = freebaseHelper.getNameForID("/m/0g6g2m");
		System.out.println(subject);

		subject = freebaseHelper.getNameForID("/m/047rcpg");
		System.out.println(subject);

		FreebaseHelper.Entity object = freebaseHelper.getNameForID("/m/02h4rq6");

		System.out.println(object);
	}

	@Test
	public void testReplacedTopic() throws Exception {
		FreebaseHelper  freebaseHelper = new FreebaseHelper();

		final FreebaseHelper.Entity nameForID = freebaseHelper.getNameForID("/m/07mt8q3");

		System.out.println(nameForID);

	}

	@Test
	public void testErrorID() throws Exception {

		FreebaseHelper freebaseHelper = new FreebaseHelper();

		final FreebaseHelper.Entity nameForID = freebaseHelper.getNameForID("/m/0h6rm");

		System.out.println(nameForID);

	}

	@Test
	public void testGetTypes() throws Exception {
		FreebaseHelper freebaseHelper = new FreebaseHelper();

		FreebaseRelation typesForRelationFromFreebase = freebaseHelper.getTypesForRelationFromFreebase("ns:people.person.education..education.education.degree");

		Assert.assertThat(typesForRelationFromFreebase.getDomain(), is("ns:people.person"));
		Assert.assertThat(typesForRelationFromFreebase.getRange(), is("ns:education.educational_degree"));

		typesForRelationFromFreebase = freebaseHelper.getTypesForRelationFromFreebase("ns:people.person.education..education.education.institution");

		Assert.assertThat(typesForRelationFromFreebase.getDomain(), is("ns:people.person"));
		Assert.assertThat(typesForRelationFromFreebase.getRange(), is("ns:education.educational_institution"));

		typesForRelationFromFreebase = freebaseHelper.getTypesForRelationFromFreebase("ns:base.rugby.rugby_club.coaches_of_this_team..base.rugby.rugby_coaching_tenure.coach");

		Assert.assertThat(typesForRelationFromFreebase.getDomain(), is("ns:base.rugby.rugby_club"));
		Assert.assertThat(typesForRelationFromFreebase.getRange(), is("ns:base.rugby.rugby_coach"));

		typesForRelationFromFreebase = freebaseHelper.getTypesForRelationFromFreebase("ns:education.educational_institution.students_graduates..education.education.specialization");

		Assert.assertThat(typesForRelationFromFreebase.getDomain(), is("ns:education.educational_institution"));
		Assert.assertThat(typesForRelationFromFreebase.getRange(), is("ns:education.field_of_study"));

	}

}
