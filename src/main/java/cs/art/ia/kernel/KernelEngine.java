package cs.art.ia.kernel;




import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class KernelEngine {

        private static KernelEngine configurationHandler = null;
        private static String sPropFileName = null;  //nome del file di properties

        //Elenco di variabili relative ai moduli, contenute nel file .properties
        private static String WordnetPath;
        private static Boolean TemplateParser;
        private static Boolean SyntacticParser;
        private static Boolean Synonimer ;
        private static Boolean showAdvanceResult;
        private static Integer MaxHyponymDepthLevel;
        private static Integer MaxHypernymHeightLevel;
        private static String ontologyReference;
        private static String filterProperty;


        private KernelEngine(){}


    /**
     * Instanzia il KernelEngine caricando le impostazioni di avvio
     * @param propFileName
     * @return
     * @throws IOException
     */
        public static synchronized KernelEngine getConfigurationHandler(String propFileName) throws IOException {
            sPropFileName= propFileName;
            if (configurationHandler == null) {
                configurationHandler = new KernelEngine();
            }
            sPropFileName= propFileName;
            configurationHandler.updateValues();
            return configurationHandler;
        }


        public Integer getMaxHyponymDepthLevel() {
            return MaxHyponymDepthLevel;
        }

        public Integer getMaxHypernymHeightLevel() {
            return MaxHypernymHeightLevel;
        }


    /**
     * Carica le impostazioni del KernelEngine dal file di configurazione
     * @throws IOException
     */
        public void updateValues() throws IOException{
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(sPropFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            }
            else {
                throw new FileNotFoundException("Il file .property '" + sPropFileName + "' non è stato trovato nel classpath");
            }
//            getClass().getResource("/dict").getPath();
//            WordnetPath =  "./dict";

            WordnetPath = prop.getProperty("wordnet.dictonary.path");
            System.out.println("Dict Path: "+WordnetPath);
//            TemplateParser = prop.getProperty("TemplateParser");
//            Synonimer = prop.getProperty("Synonimer");
//            SyntacticParser = prop.getProperty("SyntacticParser");
//            VisualizeResultToUser = prop.getProperty("VisualizeResultToUser");

            MaxHyponymDepthLevel = Integer.decode(prop.getProperty("HyponymDepthLevel"));
            MaxHypernymHeightLevel = Integer.decode(prop.getProperty("HypernymHeightLevel"));
        }


    public String getWordnetPath() {
//        System.out.println("Path: " + WordnetPath);
        return WordnetPath;
    }

    public String getFilterProperty() {
        return filterProperty;
    }

    public void setFilterProperty(String filterProperty) {
        KernelEngine.filterProperty = filterProperty;
    }

    public void setShowAdvanceResult(Boolean showAdvanceResult) {
        KernelEngine.showAdvanceResult = showAdvanceResult;
    }

    public String getOntologyReference() {
        return ontologyReference;
    }

    public void setOntologyReference(String ontologyReference) {
        KernelEngine.ontologyReference = ontologyReference;
    }

    public Boolean getTemplateParser() {
        return TemplateParser;
    }

    public void setTemplateParser(Boolean templateParser) {
        TemplateParser = templateParser;
    }

    public Boolean getSyntacticParser() {
        return SyntacticParser;
    }

    public void setSyntacticParser(Boolean syntacticParser) {
        SyntacticParser = syntacticParser;
    }

    public Boolean getSynonimer() {
        return Synonimer;
    }

    public void setSynonimer(Boolean synonimer) {
        Synonimer = synonimer;
    }
}
