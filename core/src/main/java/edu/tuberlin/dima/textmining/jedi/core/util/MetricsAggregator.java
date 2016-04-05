package edu.tuberlin.dima.textmining.jedi.core.util;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.uima.util.ProcessTrace;
import org.apache.uima.util.ProcessTraceEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Helper to record UIMA performance.
 */
public class MetricsAggregator {

	/**
	 * Global Metrics Registry.
	 */
	private static final MetricRegistry registry = new MetricRegistry();

	public static final ConsoleReporter reporter = ConsoleReporter.forRegistry(registry)
		.convertRatesTo(TimeUnit.SECONDS)
		.convertDurationsTo(TimeUnit.MILLISECONDS)
		.build();

	private static LoadingCache<String, Timer> timers = CacheBuilder.newBuilder().build(new CacheLoader<String, Timer>() {
		@Override
		public Timer load(String key) throws Exception {
			return registry.timer(key);
		}
	});

	private static LoadingCache<String, Histogram> histograms = CacheBuilder.newBuilder().build(new CacheLoader<String, Histogram>() {
		@Override
		public Histogram load(String key) throws Exception {
			return registry.histogram(key);
		}
	});


	/**
	 * Static helper method to relay UIMA timing traces.
	 *
	 * @param process   the process
	 * @param sentences the sentences processed
	 * @param tokens    how many tokens processed
	 **/
	public static void accummulateMetrics(ProcessTrace process, int sentences, int tokens) {

		List<ProcessTraceEvent> events = process.getEvents();
		// This is the global analysis engine
		//the individual components are sub-children
		if (events.size() == 1) {
			List<ProcessTraceEvent> subEvents = events.get(0).getSubEvents();

			// store the timings for the subprocesses
			for (ProcessTraceEvent subEvent : subEvents) {
				updateMetrics(sentences, tokens, subEvent.getDuration(), subEvent.getComponentName());
			}

			updateMetrics(sentences, tokens, events.get(0).getDuration(), "UIMAPipeline");

		}
	}


	public static void updateMetrics(
		int sentences, int tokens, long miliseconds, String name) {
		double seconds = miliseconds / (double) 1000;
		updateMetrics(miliseconds, name);
		histograms.getUnchecked(name + " TokensPerSecond").update((int) (tokens / seconds));
		histograms.getUnchecked(name + " SentencesPerSecond").update((int) (sentences / seconds));
		histograms.getUnchecked(name + " DocumentsPerSecond").update((int) (1 / seconds));
	}

	public static void updateMetrics(long miliseconds, String name) {
		timers.getUnchecked(name + " Timer").update(miliseconds, TimeUnit.MILLISECONDS);
	}


}
