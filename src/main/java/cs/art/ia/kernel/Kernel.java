package cs.art.ia.kernel;

import cs.art.ia.model.rdf.StringRDF;
import cs.art.ia.parserEngine.SyntaticParser;
import cs.art.ia.parserEngine.XmlParser;
import cs.art.ia.synonymerEngine.Synonymer;
import cs.art.ia.userInterfaceEngine.GuiController;
import cs.art.ia.utils.GuiResult;
import cs.art.ia.utils.Utility;
import cs.art.ia.model.QuerySPARQL;
import cs.art.ia.model.QuerySPARQLResult;
import cs.art.ia.model.rdf.ElementRDF;
import cs.art.ia.model.rdf.ResourceRDF;
import cs.art.ia.model.rdf.TripleRDFComponent;
import it.uniroma2.art.owlart.exceptions.ModelAccessException;
import it.uniroma2.art.owlart.exceptions.ModelCreationException;
import it.uniroma2.art.owlart.exceptions.QueryEvaluationException;
import it.uniroma2.art.owlart.exceptions.UnsupportedQueryLanguageException;
import it.uniroma2.art.owlart.model.ARTNode;
import it.uniroma2.art.owlart.model.ARTNodeFactory;
import it.uniroma2.art.owlart.model.impl.ARTNodeFactoryImpl;
import it.uniroma2.art.owlart.model.impl.ARTURIResourceEmptyImpl;
import it.uniroma2.art.owlart.query.MalformedQueryException;

import cs.art.ia.queryAnsweringEngine.AnswerManager;
import it.uniroma2.art.owlart.vocabulary.XmlSchema;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 */
public class Kernel {

    private GuiController controlleGui;
    private static Kernel kernelInstance;
    private AnswerManager answerManager;
    private static KernelEngine kernelEngine;
    private Synonymer synonimer;
    private String input;


    public static Kernel getIstance(){
        if(kernelInstance == null){
            kernelInstance = new Kernel();
        }
        return kernelInstance;
    }


    /**
     * Instanza principale del kernel del programma
     */
    public Kernel() {

		answerManager = new AnswerManager();

        kernelEngine=getConfigurationHandler();

        // Initialize SynonymerManager
        synonimer = new Synonymer();

        // Initialize wordnet.database property
        System.setProperty("wordnet.database.dir", kernelEngine.getWordnetPath());

	}


    /**
     * Thread di avvio della ricerca della query effettuata
     */
    public void run(){

        input=controlleGui.getAskField().getText();

                        if (input.equals("") || input.equals(" ")) {
                            System.out.println("Il testo è "+input);

                            Platform.runLater(new Runnable(){@Override public void run() {
                                Alert alert=new Alert(Alert.AlertType.ERROR);
                                alert.setContentText("Warning: your input is empty!");
                                alert.showAndWait();}});

                        } else {

                            controlleGui.getAskField().setDisable(true);
                            controlleGui.getAskButton().setDisable(true);
                            controlleGui.getSetting().setDisable(true);

                            Platform.runLater(new Runnable(){@Override public void run() {controlleGui.getLoadingBar().setProgress(ProgressBar.INDETERMINATE_PROGRESS);}});
                            controlleGui.setResult("Query in corso.....");

                            try {

                                List<QuerySPARQLResult> querySPARQLResultList = new ArrayList<QuerySPARQLResult>();
                                querySPARQLResultList = QARunner(input);

                                if (querySPARQLResultList==null||querySPARQLResultList.isEmpty()) {
                                    controlleGui.setResult("No results were found. Check the Sentence.");
                                    System.out.println("No results were found.");
                                    System.out.println("Possibile errore nel parsing dell'input e nella creazione della query (Assenza Query)");
                                } else {
                                    List<String> noDuplicateResults = Utility.getResultsWithoutDuplicates(querySPARQLResultList);

                                    if(controlleGui.getGuiViewResult()!=null){
                                        controlleGui.getGuiViewResult().setResult(noDuplicateResults.get(0));
                                        controlleGui.setGuiViewResult(null);
                                    }
                                    else
                                        controlleGui.getData().add(new GuiResult(input,noDuplicateResults.get(0)));


                                    String string = "Risultato Ottenuto:\n";
                                    for (String result : noDuplicateResults) {
                                        string += result + "\n";
                                    }
                                    String result=string.replace("_"," ");


                                    controlleGui.setResult(result);
                                }
                            }catch (Exception e1) {
                                Platform.runLater(new Runnable(){@Override public void run() {
                                    Alert alert=new Alert(Alert.AlertType.ERROR);
//                                alert.setTitle("test");
//                                alert.setHeaderText("test");
                                    alert.setContentText("DBPedia Error,Server Timeout");
                                    alert.showAndWait();}});
                                e1.printStackTrace();
                            }
                            controlleGui.getAskField().setDisable(false);
                            controlleGui.getAskButton().setDisable(false);
                            controlleGui.getSetting().setDisable(false);
                            Platform.runLater(new Runnable(){@Override public void run() {controlleGui.getLoadingBar().setProgress(0);}});
                            controlleGui.getTable().refresh();
    }}

    /**
     * Metodo che valuta a quale parser scelto far cercare la query
     * @param input
     * @return
     * @throws Exception
     */
    private List<QuerySPARQL> parseQuestion(String input) throws Exception {

		List<QuerySPARQL> listQuerySPARQL = new ArrayList<QuerySPARQL>();

        if (kernelEngine.getTemplateParser()) {

            XmlParser xmlParser = new XmlParser();
            try {
                listQuerySPARQL = xmlParser.parse(input);
            } catch (NullPointerException e) {
                System.out.println(e.toString());
            }

        }

        else if (kernelEngine.getSyntacticParser()) {

            SyntaticParser syntaticParser=new SyntaticParser();


            try {
                listQuerySPARQL = syntaticParser.parseInput(input);
            } catch (NullPointerException e) {
                Platform.runLater(new Runnable(){@Override public void run() {
                    Alert alert=new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Parsing Sentence Exception");
                    alert.showAndWait();}});
                System.out.println(e.toString());
            }
        } else {
                Platform.runLater(new Runnable(){@Override public void run() {
                Alert alert=new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Error, undefined parser");
                alert.showAndWait();}});
        }

        if(listQuerySPARQL.size()>0){
            for (QuerySPARQL querySPARQL : listQuerySPARQL) {
                System.out.println("querySPARQL: "+querySPARQL.getTripleRDF().toString());
                System.out.println("Subject: "+ querySPARQL.getTripleRDF().getSubject().getValue());
                System.out.println("Predicate: "+ querySPARQL.getTripleRDF().getPredicate().getValue());
                System.out.println("Object: "+ querySPARQL.getTripleRDF().getObject().getValue());
            }
        }
		return listQuerySPARQL;
	}


    /**
     * Metodo che richiama il sistema per la gestione delle risposte alle query effettuate
     * @param listQuerySPARQL
     * @return
     * @throws Exception
     */
    private List<QuerySPARQLResult> executeQuery(List<QuerySPARQL> listQuerySPARQL) throws Exception {
		List<QuerySPARQLResult> querySPARQLResults = null;
		try {
			querySPARQLResults = answerManager.executeQuery(listQuerySPARQL,input);
		} catch (UnsupportedQueryLanguageException | QueryEvaluationException | MalformedQueryException | ModelCreationException | ModelAccessException e) {
			System.out.println(e.toString());
		}

		return querySPARQLResults;
	}


//    /**
//     * Elimina i risultati duplicati dalle risposte fornite da SPARQL
//     * @param listResults
//     * @return
//     */
//    private List<String> deleteDuplicateResult(List<QuerySPARQLResult> listResults){
//        List<String> noDuplicateResults = new ArrayList<String>();
//
//        for(QuerySPARQLResult querySPARQLResult : listResults){
//            ARTNode artNode = querySPARQLResult.getValue();
//            if(artNode.isURIResource()){
//                String localName = artNode.asURIResource().getLocalName();
//                System.out.println("Local Name: "+localName);
//                if(!noDuplicateResults.contains(localName)){
//                    noDuplicateResults.add(localName);
//                }
//            } else if(artNode.isLiteral()){
//                String label = artNode.asLiteral().getLabel();
//                String lang = artNode.asLiteral().toString();
//
//                String[] temp=lang.split("/");
//                String element=temp[temp.length-1];
//
//                String literalResult = label + " " + element.substring(0, element.length()-1);
//                System.out.println("literalResult: "+literalResult);
//                if(!noDuplicateResults.contains(literalResult)){
//                    noDuplicateResults.add(literalResult);
//                }
//            }
//        }
//
//        return noDuplicateResults;
//    }


    /**
     * Metodo che definire la execution line del sistema
     * @param inputQuery
     * @return
     * @throws Exception
     */
    private List<QuerySPARQLResult> QARunner(String inputQuery) throws Exception {

		ElementRDF.resetId();

		List<QuerySPARQL> listQuerySPARQL = parseQuestion(inputQuery);

		if (listQuerySPARQL.size()>0){
            List<QuerySPARQLResult> querySPARQLResult = executeQuery(listQuerySPARQL);
            if ( kernelEngine.getSynonimer() && (querySPARQLResult.isEmpty()||querySPARQLResult.get(0).getValue().getNominalValue().equals("false"))) {
                querySPARQLResult = findResultsUsingSynonymer(listQuerySPARQL);
            }

            if (true) {
                Utility.visualizeResults(querySPARQLResult);
            }
            return querySPARQLResult;
        }
        return null;
	}


    /**
     * Metodo per la gestione del Synonimer al fine di trovare risultati validi alla query posta
     * @param listQuery
     * @return
     * @throws Exception
     */
    private List<QuerySPARQLResult> findResultsUsingSynonymer(List<QuerySPARQL> listQuery) throws Exception {

        // Initialize queryResults.
        List<QuerySPARQLResult> queryResults = new ArrayList<>();


        // Check if there is an ASK query.
        boolean askQuery = answerManager.getmQueryBuilder().yesOrNoQuery(listQuery);

        System.out.println("ASK query: " + askQuery);

        // Get resource predicate from first query.
        TripleRDFComponent predicate = listQuery.get(0).getTripleRDF().getPredicate();
        ResourceRDF resourcePredicate = new ResourceRDF("");
        if (predicate instanceof ResourceRDF) {
            resourcePredicate = (ResourceRDF) predicate;}


        List<String> allResourcePredicate= Utility.determinateResource(resourcePredicate);


        for(String resource:allResourcePredicate){



            controlleGui.setResult("Predicato in analisi: "+resource+" utilizzo il Synonymer");

            System.out.println("Risorsa in Analisi: "+resource);

            controlleGui.appendResult("\nCerco tra i sinonimi");
            queryResults= findUsingSynonyms(listQuery,resource);
            if(!queryResults.isEmpty()){
                return queryResults;
            }

            System.out.println("Niente return passo a Hyponimi");

            controlleGui.appendResult("\nCerco tra gli iponimi");
            queryResults=findUsingHyponyms(listQuery,resource);
            if(!queryResults.isEmpty()){
                return queryResults;
            }

            System.out.println("Niente return passo a Hyper");

            controlleGui.appendResult("\nCerco tra gli iperonimi");
            queryResults=findUsingHypernyms(listQuery,resource);
            if(!queryResults.isEmpty()){
                return queryResults;
            }

            controlleGui.setResult("");
        }
        // Manage for ASK query: return false if no results has been found.
        if (askQuery) {
//            ARTNode nodeASK = new ARTURIResourceEmptyImpl("false");
            ARTNodeFactory nodeFactory=new ARTNodeFactoryImpl();
            ARTNode nodeASK=nodeFactory.createLiteral("false", XmlSchema.BOOLEAN);
            QuerySPARQLResult queryResult = new QuerySPARQLResult(nodeASK);
            queryResults.add(queryResult);
            return queryResults;
        }
        return queryResults;
    }


    /**
     * Metodo per la ricerca tra i sinonimi di ulteriori parole per ampliare il campo di ricerca
     * @param listQuery
     * @param resource
     * @return
     * @throws Exception
     */
    private List<QuerySPARQLResult> findUsingSynonyms(List<QuerySPARQL> listQuery, String resource) throws Exception {
        List<QuerySPARQLResult> queryResults = new ArrayList<>();
        //Lista sinonimi

        List<String> allSynonyms=new ArrayList<>();
        System.out.println("Lista Sinonimi Ottenuti: ");
        allSynonyms = synonimer.fetchSynonyms(resource);


        //Lista contenente i sinomi diretti con tutto il predicato inserito
        List<String> synonymToMatch=new ArrayList<>();
        //Lista contenente i sinomi con le permutazioni fra di essi dato il preidicato scomposto
//		Modellizzazione dei sinonimi direttamente collegati alla proprieta per renderli camlecase
        if(allSynonyms.size()>0){
            for(String s:allSynonyms){
                controlleGui.appendResult("\n"+s);
                List<String> splitting=new ArrayList<>();
                splitting=(Arrays.asList(s.split(" ")));
                if(splitting.size()>1)
                    synonymToMatch.add(Utility.convertToCamelCase(splitting.get(0),splitting.get(1)));
                else
                    synonymToMatch.add(s);
            }
        }

        // If results are found, return them.
        if (synonymToMatch.size()>0) {
            queryResults=answerManager.executeSynonymerQuery(listQuery,synonymToMatch,input);
            if (!queryResults.isEmpty()) {
                return queryResults;
            }
        }
        return  queryResults;
    }


    /**
     * Metodo per la ricerca tra gli Iponimi di ulteriori parole per ampliare il campo di ricerca
     * @param listQuery
     * @param resource
     * @return
     * @throws Exception
     */
    private List<QuerySPARQLResult> findUsingHyponyms(List<QuerySPARQL> listQuery, String resource) throws Exception {
        List<QuerySPARQLResult> queryResults = new ArrayList<>();
        //Hyponimi associati ai pezzi della frase

        List<String> allHyponyms=new ArrayList<>();


        Integer maxHyponymDepthLevel = kernelEngine.getMaxHyponymDepthLevel();

        for (Integer hyponymDepthLevel = 1; hyponymDepthLevel <= maxHyponymDepthLevel; hyponymDepthLevel++) {
            System.out.println("Lista Iponimi Ottenuti: ");
            allHyponyms = synonimer.fetchHyponyms(resource, hyponymDepthLevel);

            // No other results possible. No hyponyms.
            if (allHyponyms.isEmpty()) {
//                getUiManager().getGui().addLog("No other hyponyms");
                System.out.println("No other hyponyms");
                break;
            }

            //Lista contenente i sinomi diretti con tutto il predicato inserito
            List<String> hyponymsToMatch=new ArrayList<>();
            //Lista contenente i sinomi con le permutazioni fra di essi dato il preidicato scomposto
//			List<String> permutationHyponymsToMatch=new ArrayList<>();

            if(allHyponyms.size()>0){

                for(String s:allHyponyms){
                    controlleGui.appendResult("\n"+s);
                    List<String> splitting=new ArrayList<>();
                    splitting=(Arrays.asList(s.split(" ")));
                    if(splitting.size()>1)
                        hyponymsToMatch.add(Utility.convertToCamelCase(splitting.get(0),splitting.get(1)));
                    else
                        hyponymsToMatch.add(s);
                }
            }

            // If results are found, return them.
            if (hyponymsToMatch.size()>0) {
//                getUiManager().getGui().addLog("Results founded at depth level: " + 0 + ".\n");
                queryResults=answerManager.executeSynonymerQuery(listQuery,hyponymsToMatch,input);
                if (!queryResults.isEmpty()) {
                    return queryResults;
                }
            }

        }
        return  queryResults;
    }


    /**
     * Metodo per la ricerca tra gli Iperonimi di ulteriori parole per ampliare il campo di ricerca
     * @param listQuery
     * @param resource
     * @return
     * @throws Exception
     */
    public List<QuerySPARQLResult> findUsingHypernyms(List<QuerySPARQL> listQuery,String resource) throws Exception {
        List<QuerySPARQLResult> queryResults = new ArrayList<>();
        List<String> allHypernyms=new ArrayList<>();

        Integer maxhypernymHeightLevel = kernelEngine.getMaxHypernymHeightLevel();

        for (Integer hypernymHeightLevel = 1; hypernymHeightLevel <= maxhypernymHeightLevel; hypernymHeightLevel++) {

            System.out.println("Lista Iperonimi Ottenuti: ");
            allHypernyms = synonimer.fetchHypernyms(resource, hypernymHeightLevel);


            // No other results possible. No hypernyms.
            if (allHypernyms.isEmpty()) {
//                getUiManager().getGui().addLog("No other hypernyms");
                System.out.println("No other hypernyms");
                break;
            }


            List<String> hypernymsToMatch=new ArrayList<>();

            if(allHypernyms.size()>0){
                for(String s:allHypernyms){
                    controlleGui.appendResult("\n"+s);
                    List<String> splitting=new ArrayList<>();
                    splitting=(Arrays.asList(s.split(" ")));
                    if(splitting.size()>1)
                        hypernymsToMatch.add(Utility.convertToCamelCase(splitting.get(0),splitting.get(1)));
                    else
                        hypernymsToMatch.add(s);
                }
            }

            if (hypernymsToMatch.size()>0) {
//                getUiManager().getGui().addLog("Results founded at depth level: " + 0 + ".\n");
                queryResults=answerManager.executeSynonymerQuery(listQuery,hypernymsToMatch,input);
                if (!queryResults.isEmpty()) {
                    return queryResults;
                }
            }
        }
        return  queryResults;
    }


    /**
     * Creazione Instanza del KernelEngine al fine di caricare le impostazioni di avvio
     * @return
     */
    private KernelEngine getConfigurationHandler() {
        // Initialize configuration handler.
        KernelEngine configurationHandler = null;

        try {
            configurationHandler = KernelEngine.getConfigurationHandler("configuration.properties");
        } catch (IOException e) {
//            uiManager.showErrorOnGUI("Error, try inserting another question");
        }
        return configurationHandler;
    }

    public KernelEngine getKernelEngine() {
        return kernelEngine;
    }

    public void setController(GuiController controller) {
        this.controlleGui = controller ;
    }

    public GuiController getControlleGui() {
        return controlleGui;
    }

}
