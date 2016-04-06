package edu.tuberlin.dima.textmining.jedi.sample;

import edu.tuberlin.dima.textmining.jedi.core.config.DetectorService;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.DetectorType;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.model.Answer;
import org.apache.uima.jcas.tcas.Annotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Sample application.
 */
@ComponentScan(basePackages = {"edu.tuberlin.dima.textmining.jedi.core.config"})
@SpringBootApplication
public class JediSpringSampleApplication implements CommandLineRunner {

	@Autowired
	DetectorService detectorService;

	public static void main(String[] args) {
		SpringApplication.run(JediSpringSampleApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {

		String sentence = "Bill Gothard received his B.A. in Biblical Studies from Wheaton College in 1957.";

		final Answer<Annotation> annotationAnswer = detectorService.detectFeatures(sentence, DetectorType.ALLPairs, true);

		System.out.println("\n ------------ Input Sentence     ------------ \n");
		System.out.println(sentence);
		System.out.println("\n ------------ Detected Relations ------------ \n");

		String tableString = annotationAnswer.generateReadableTableString();

		System.out.println(tableString);

	}
}
