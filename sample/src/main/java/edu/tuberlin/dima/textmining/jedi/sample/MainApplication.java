package edu.tuberlin.dima.textmining.jedi.sample;

import edu.tuberlin.dima.textmining.jedi.core.config.DetectorService;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.DetectorType;
import edu.tuberlin.dima.textmining.jedi.core.features.detector.model.Answer;
import edu.tuberlin.dima.textmining.jedi.core.model.Solution;
import edu.tuberlin.dima.textmining.jedi.sample.util.TableBuilder;
import org.apache.uima.jcas.tcas.Annotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by Johannes on 05.04.2016.
 */
@ComponentScan(basePackages = {"edu.tuberlin.dima.textmining.jedi.core.config"})
@SpringBootApplication
public class MainApplication implements CommandLineRunner {

	@Autowired
	DetectorService detectorService;

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {

		String sentence = "Bill Gothard received his B.A. in Biblical Studies from Wheaton College in 1957.";

		final Answer<Annotation> annotationAnswer = detectorService.detectFeatures(sentence, DetectorType.ALLPairs, true);

		Answer<String> x = Answer.generateStringVersion(annotationAnswer);

		System.out.println("\n ------------ Input Sentence     ------------ \n");
		System.out.println(sentence);
		System.out.println("\n ------------ Detected Relations ------------ \n");

		TableBuilder tb = new TableBuilder();

		tb.addRow("Object", "Relation", "Subject", "Pattern");
		tb.addRow("-----", "----", "-----", "-----");

		for (Solution<String> solution : x.getSolutions()) {
			tb.addRow(solution.getLeft(), solution.getEdge().getRelation(), solution.getRight(), solution.getEdge().getPattern());
		}

		System.out.println(tb.toString());

	}
}
