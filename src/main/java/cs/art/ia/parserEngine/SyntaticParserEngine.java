package cs.art.ia.parserEngine;


import de.tudarmstadt.ukp.dkpro.core.gate.GateLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.snowball.SnowballStemmer;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

public class SyntaticParserEngine {

    /***********************************************************************************************************************\
     PARSER - SIMBOLI NON TERMINALI E POS
     \***********************************************************************************************************************/

    public static final String S = "S";
    public static final String SBARQ = "SBARQ";
    public static final String NP = "NP";
    public static final String NN = "NN";
    public static final String NNS = "NNS";
    public static final String NNP = "NNP";
    public static final String NNPS = "NNPS";
    public static final String ADJP = "ADJP";

    public static final String VB = "VB";
    public static final String VBD = "VBD";
    public static final String VBG = "VBG";
    public static final String VBN = "VBN";
    public static final String VBP = "VBP";
    public static final String VBZ = "VBZ";

    public static final String JJ = "JJ";
    public static final String JJS = "JJS";
    public static final String JJR = "JJR";


    /***********************************************************************************************************************\
     LINGUA
     \***********************************************************************************************************************/

    public static final String ENGLISH = "en";


    // Singleton object of the class
    private static SyntaticParserEngine sSyntaticParserEngine = null;
    private AnalysisEngine mSegmenter = null;
    private AnalysisEngine mParser = null;
    private JCas mUimaContainer;
    private AnalysisEngine mStemmer=null;
    private AnalysisEngine mTagger=null;
    private AnalysisEngine mLemmatizer=null;

    private String engineAtWork=null;

    private SyntaticParserEngine() {
        sSyntaticParserEngine = this;
    }


    /**
     * Instanzia l'engine del SyntaticParser
     * @return
     */
    public static SyntaticParserEngine getInstance() {
        if(sSyntaticParserEngine==null) {
            sSyntaticParserEngine = new SyntaticParserEngine();
        }
        return sSyntaticParserEngine;
    }


    /**
     * Cambia l'engine da utilizzare in base al sistema selzionato dalla gui
     * @param engine
     */
    public void switchEngine(String engine){
        mParser=null;
        mSegmenter=null;
        if(engine.equals("Simple")){
            engineAtWork="Simple";
            engineSimple();}
        if (engine.equals("Stemmer")){
            engineAtWork="Stemmer";
            engineStemmer();}
        if (engine.equals("Lemmatizer")){
             engineAtWork="Lemmatizer";
             engineLemmatizer();}
    }


    /**
     * Inizilizza la configurazione di base del nostro sistema che non sfrutta componenti aggiuntive
     */
    private void engineSimple() {
        System.out.println("preparo parser");
        //TODO Cambiare il tipo di return in querylist. Rivedere il configuration manager per la tipologia di parser
        if(mParser == null || mSegmenter == null) {
            try {
                mStemmer=null;

                mLemmatizer=null;

                // Istanzia il segmenter per tokenizzare la frase in input
                mSegmenter = AnalysisEngineFactory.createEngine(OpenNlpSegmenter.class);
                mTagger = AnalysisEngineFactory.createEngine(OpenNlpPosTagger.class);
                // Istanzia il parser
                mParser = AnalysisEngineFactory.createEngine(OpenNlpParser.class, OpenNlpParser.PARAM_WRITE_PENN_TREE, true);
                // Crea il JCas
                mUimaContainer = JCasFactory.createJCas();
            } catch (UIMAException e) {
                System.out.println("Errore creazione parser");
                e.printStackTrace();
            }
        }
    }

    /**
     * Inizilizza la configurazione del nostro sistema che sfrutta la componente aggiuntiva dello Stemmer
     */
    private void engineStemmer() {
        System.out.println("preparo parser");
        //TODO Cambiare il tipo di return in querylist. Rivedere il configuration manager per la tipologia di parser
        if(mParser == null || mSegmenter == null) {
            try {
                // Istanzia il segmenter per tokenizzare la frase in input
                mSegmenter = AnalysisEngineFactory.createEngine(OpenNlpSegmenter.class);

                mStemmer= AnalysisEngineFactory.createEngine(SnowballStemmer.class);

                mLemmatizer=null;

                mTagger = AnalysisEngineFactory.createEngine(OpenNlpPosTagger.class);

                // Istanzia il parser
                mParser = AnalysisEngineFactory.createEngine(OpenNlpParser.class, OpenNlpParser.PARAM_WRITE_PENN_TREE, true);
                // Crea il JCas
                mUimaContainer = JCasFactory.createJCas();
            } catch (UIMAException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Inizilizza la configurazione del nostro sistema che sfrutta la componente aggiuntiva del Lemmatizer
     */
    private void engineLemmatizer() {
        System.out.println("preparo parser");
        //TODO Cambiare il tipo di return in querylist. Rivedere il configuration manager per la tipologia di parser
        if(mParser == null || mSegmenter == null) {
            try {
                // Istanzia il segmenter per tokenizzare la frase in input
                mSegmenter = AnalysisEngineFactory.createEngine(OpenNlpSegmenter.class);

                mStemmer=null;

                mTagger = AnalysisEngineFactory.createEngine(OpenNlpPosTagger.class);

                mLemmatizer = AnalysisEngineFactory.createEngine(GateLemmatizer.class,GateLemmatizer.PARAM_LANGUAGE,"en");

                // Istanzia il parser
                mParser = AnalysisEngineFactory.createEngine(OpenNlpParser.class, OpenNlpParser.PARAM_WRITE_PENN_TREE, true);
                // Crea il JCas
                mUimaContainer = JCasFactory.createJCas();
            } catch (UIMAException e) {
                e.printStackTrace();
            }
        }
    }

    public JCas getContainer() {
        return mUimaContainer;
    }

    public AnalysisEngine getmSegmenter() {
        return mSegmenter;
    }

    public AnalysisEngine getmParser() {
        return mParser;
    }

    public AnalysisEngine getmStemmer() {
        return mStemmer;
    }

    public AnalysisEngine getmTagger() {
        return mTagger;
    }

    public AnalysisEngine getmLemmatizer() {
        return mLemmatizer;
    }

    public String getEngineAtWork() {
        return engineAtWork;
    }
}
