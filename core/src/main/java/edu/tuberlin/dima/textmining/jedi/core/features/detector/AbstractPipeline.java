package edu.tuberlin.dima.textmining.jedi.core.features.detector;

import com.beust.jcommander.Parameter;
import com.codahale.metrics.CsvReporter;
import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.tuberlin.dima.textmining.jedi.core.util.MetricsAggregator;
import edu.tuberlin.dima.textmining.jedi.core.util.UIMAXMLConverterHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.ProcessTrace;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AbstractPipeline {
    private static final Log LOG = LogFactory
            .getLog(AbstractPipeline.class);

    private AnalysisEngine analysisEngine;

    private JCas jCas;

    private UIMAXMLConverterHelper converterHelper;

    private CsvReporter csvreporter;

    private Boolean renderNewCas = false;

    private String defaultLanguage;

    private boolean enableReporting = false;

    @Parameter(names = {"-testMode"},
            description = "If enabled, frontend checking is disabled", required = false)
    private boolean testMode = false;

    @Parameter(names = {"-warmup"},
            description = "Parses a small sentence, to load the resources", required = false)
    private boolean warmup = false;

    enum Counters {
        TOKENS,
        SENTENCES,
        DOCUMENTS,
        WRONG_LANGUAGE,
        ERRORS
    }

    Collection<Token> tokens;
    Collection<Sentence> sentences;

    public void init(
            AnalysisEngine analysisEngine,
            boolean renderNewCas,
            String defaultLanguage,
            boolean enableReporting,
            String reportCSV,
            boolean prettyPrintXML)
            throws Throwable {
        // load properties early
        this.analysisEngine = analysisEngine;
        this.renderNewCas = renderNewCas;
        this.defaultLanguage = defaultLanguage;
        this.enableReporting = enableReporting;

        // init only in backend
        if (testMode) {

            this.converterHelper = new UIMAXMLConverterHelper(prettyPrintXML);
            this.jCas = JCasFactory.createJCas();
            if (enableReporting) {

                MetricsAggregator.reporter.start(10, TimeUnit.MINUTES);
                if (reportCSV != null) {

                    csvreporter.start(
                            10, TimeUnit.MINUTES);
                }
            }

            if (warmup) {
                warmup();
            }
        }
    }

    public JCas exec(
            List<String> input) throws IOException {
        tokens = null;
        sentences = null;

        if (input == null) {
            return null;
        }
        if (renderNewCas && input.size() != 2) {
            throw new IllegalArgumentException(
                    "Tuple needs to contain two arguments");
        }

        String document = input.get(0);
        String documentID = renderNewCas ? input.get(1) : "";

        if (document == null) {
            return null;
        }


        try {
            Stopwatch complete = new Stopwatch();
            Stopwatch serialize = new Stopwatch();

            complete.start();
            runPipeline(StringUtils.trim(document), documentID);
            serialize.start();

            //String result = converterHelper.serialize( jCas );

            serialize.stop();
            complete.stop();

            if (enableReporting) {
                MetricsAggregator.updateMetrics(
                        sentences.size(), tokens.size(),
                        serialize.elapsedTime(TimeUnit.MILLISECONDS),
                        "Serialization");

                MetricsAggregator.updateMetrics(
                        sentences.size(), tokens.size(),
                        complete.elapsedTime(TimeUnit.MILLISECONDS),
                        "UDF");
            }

            return jCas;

        } catch (Throwable e) {
            LOG.error(
                    e.getMessage() + ">" + document + "<", e);
            return null;
        }
    }

    /**
     * Triggers the pipeline, without reporting measurements - loads all classes = warmup
     */
    private void warmup() throws AnalysisEngineProcessException {

        LOG.info("Triggering a warmup, with language set to " + defaultLanguage);
        jCas.reset();

        String sampleText;
        switch (defaultLanguage) {
            case "en":
                sampleText = "This is a not so super important sample warmup text to trigger the loading";
                break;
            case "de":
                sampleText = "Dies ist in Beispiel text, der zum Laden von Modulen führen sollte ....";
                break;
            default:
                sampleText = "This is a not so super important sample warmup text to trigger the loading";

        }

        jCas.setDocumentText(sampleText);
        jCas.setDocumentLanguage(defaultLanguage);

        // clear jcas again
        jCas.reset();
        LOG.info("Done with warmup test ...");
    }

    /**
     * Performs the named entity recognition with the specified model.
     *
     * @param document   the document to be ne-tagged
     * @param documentID the document id - or null
     * @throws Exception
     */
    private void
    runPipeline(
            String document, String documentID) throws Exception {

        Stopwatch languageTimer = new Stopwatch();
        if (renderNewCas) {
            jCas.reset();
            jCas.setDocumentText(UIMAXMLConverterHelper
                    .sanitizeString(document));

            languageTimer.start();

            setLanguage();

            if (documentID != null) {
                DocumentMetaData documentMetaData = DocumentMetaData.create(jCas);
                documentMetaData.setDocumentUri(documentID);
            }
            languageTimer.stop();

        } else {

            jCas = converterHelper.deserialize(
                    IOUtils.toInputStream(
                            document, Charsets.UTF_8.name()), jCas);
        }

        ProcessTrace process = analysisEngine.process(jCas);

        if (enableReporting) {

            sentences = JCasUtil.select(
                    jCas, Sentence.class);
            tokens = JCasUtil.select(
                    jCas, Token.class);

            MetricsAggregator.accummulateMetrics(
                    process, sentences.size(), tokens.size());

            MetricsAggregator.updateMetrics(
                    sentences.size(), tokens.size(),
                    languageTimer.elapsedTime(TimeUnit.MILLISECONDS),
                    "LanguageDetect");
        }

    }

    public void setLanguage()
            throws IllegalArgumentException {
        if (defaultLanguage != null) {
            jCas.setDocumentLanguage(defaultLanguage);
        } else {
            throw new IllegalArgumentException(
                    "No default language set.");
        }
    }

    public void finish() {
        if (enableReporting) {
            MetricsAggregator.reporter.report();
        }
    }
}
