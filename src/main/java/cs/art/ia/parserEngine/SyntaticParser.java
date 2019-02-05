package cs.art.ia.parserEngine;

import cs.art.ia.kernel.Kernel;
import cs.art.ia.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cs.art.ia.model.rdf.ElementRDF;
import cs.art.ia.model.rdf.ResourceRDF;
import cs.art.ia.model.rdf.TripleRDF;
import cs.art.ia.model.rdf.TripleRDFComponent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class SyntaticParser {

    static Logger log = Logger.getLogger(SyntaticParser.class.getName());
    static boolean D = true;

    private BinaryTree mBinaryTree;
    private BinaryTree objectTree;
    private BinaryTree predicateTree;
    private Boolean flagPredicate=true;
    private BinaryTree subjectTree;
    private List<QuerySPARQL> mQueryList;
    private Boolean firstTime=true;

    private String subject=null;
    private String predicate=null;
    private String object=null;



    /**
     * Inizilizza i componenti principali per valutare tramite il parser del sistema la parola in input
     * @param userInput
     * @return
     */
    public List<QuerySPARQL> parseInput(String userInput){

        Kernel.getIstance().getControlleGui().setResult("Caricamento UIMAfit e impostazione Parser\n");
        JCas uimaContainer = SyntaticParserEngine.getInstance().getContainer();
        uimaContainer.reset();
        Kernel.getIstance().getControlleGui().appendResult("\nFine caricamento UIMAfit (creazione JCAS)");

        uimaContainer.setDocumentText(userInput);

        uimaContainer.setDocumentLanguage(SyntaticParserEngine.ENGLISH);
        // Launch an internal parser exception.
        try {
            if(SyntaticParserEngine.getInstance().getEngineAtWork().equals("Simple")){
                SimplePipeline.runPipeline(uimaContainer,SyntaticParserEngine.getInstance().getmSegmenter(),SyntaticParserEngine.getInstance().getmTagger(),SyntaticParserEngine.getInstance().getmParser());

            }else if(SyntaticParserEngine.getInstance().getEngineAtWork().equals("Stemmer")){
                SimplePipeline.runPipeline(uimaContainer, SyntaticParserEngine.getInstance().getmSegmenter(),SyntaticParserEngine.getInstance().getmStemmer(),SyntaticParserEngine.getInstance().getmTagger(),SyntaticParserEngine.getInstance().getmParser());

            }else if(SyntaticParserEngine.getInstance().getEngineAtWork().equals("Lemmatizer")){
                SimplePipeline.runPipeline(uimaContainer, SyntaticParserEngine.getInstance().getmSegmenter(),SyntaticParserEngine.getInstance().getmTagger(),SyntaticParserEngine.getInstance().getmLemmatizer(),SyntaticParserEngine.getInstance().getmParser());
            }
            Kernel.getIstance().getControlleGui().appendResult("\nFine caricamento pipeline Parser ");
        } catch (AnalysisEngineProcessException e) {

            Platform.runLater(new Runnable(){@Override public void run() {
                Alert alert=new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Parser Creation Exception");
                alert.showAndWait();}});

        }

        analyzeQuestionInput(uimaContainer);
        mQueryList = new ArrayList<QuerySPARQL>();
        buildQuery();
        return mQueryList;
    }


    /**
     * Analizza il Jcas ,completo di annotazioni ,fornito dalla valtuazione con il parser
     * @param jcas
     */
    public void analyzeQuestionInput(JCas jcas) {

        boolean isQuestionInput = false;
        FSIterator<Annotation> iterAnn = jcas.getAnnotationIndex().iterator();
        System.out.println("Analizzo la question");
        while (iterAnn.hasNext()) {
            Annotation annotation = iterAnn.next();
            if (annotation instanceof SBARQ || annotation instanceof SQ || annotation instanceof SBAR||annotation instanceof S) {
                isQuestionInput = true;

                if(D) log.info("Annotation è un simbolo terminale di tipo SBARQ");

                // Istanzia l'albero di parsing, passando S come radice dello stesso
                // con una lista vuota di figli ad esso collegata
                mBinaryTree = new BinaryTree(new Node(annotation.getClass().getSimpleName(), new ArrayList<Node>(), null));
                if(D) log.info("BinaryTree instanziato con nodo radice SBARQ");

                // Visita l'albero prodotto da UIMA creando parallelamente l'albero di parsing
                treeVisit(annotation, mBinaryTree.getRoot());

                createPredicateTree(annotation,new BinaryTree(new Node(SyntaticParserEngine.SBARQ, new ArrayList<Node>(), null)).getRoot());
                firstTime=true;
                if(predicateTree!=null){
                    if(Kernel.getIstance().getControlleGui().getShowTreeDebug().isSelected())
                        printTreeForGUI(predicateTree);
                    predicate=determinateTreeResource(predicateTree.getRoot());
                    List<String> splitting=new ArrayList<>();
                    splitting=(Arrays.asList(predicate.split(" ")));
                    if(splitting.size()==1)
                        predicate=splitting.get(0);
                    System.out.println("PREDICATO:"+predicate);
                }

                createObjectTree(annotation,new BinaryTree(new Node(SyntaticParserEngine.SBARQ, new ArrayList<Node>(), null)).getRoot());
                firstTime=true;
                //Fai un print dell'albero
                if(objectTree!=null){
                    if(Kernel.getIstance().getControlleGui().getShowTreeDebug().isSelected())
                        printTreeForGUI(objectTree);
                    Pattern MY_PATTERN = Pattern.compile("([A-Z].+)");
                    Matcher m = MY_PATTERN.matcher(determinateTreeResource(objectTree.getRoot()));
                    while (m.find()) {
                        object= m.group(1);
                    }
                    System.out.println("OGGETTO:"+object);
                }

                createSubjectTree(annotation, new BinaryTree(new Node(SyntaticParserEngine.SBARQ, new ArrayList<Node>(), null)).getRoot(),false);
                if(subjectTree!=null){
                    if(Kernel.getIstance().getControlleGui().getShowTreeDebug().isSelected())
                        printTreeForGUI(subjectTree);
                    subject=determinateTreeResource(subjectTree.getRoot());
                    System.out.println("SOGGETTO:"+subject);
                }

                break;
            }
        }

        if(Kernel.getIstance().getControlleGui().getShowTree().isSelected())
        printTreeForGUI(mBinaryTree);


        if(isQuestionInput != true) {
            Platform.runLater(new Runnable(){@Override public void run() {
                Alert alert=new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Text Not Well Formatted");
                alert.showAndWait();}});
        }
    }


    /**
     * Navighiamo all'interno del Jcas fornito dal parser al fine di creare un nuova struttura ad albero più comoda da gestire
     * @param nodeSpanningTree
     * @param nodeBinaryTree
     */
    private void treeVisit(Annotation nodeSpanningTree, Node nodeBinaryTree) {
        if(D) log.info("Visita albero ricorsiva");

        // Figli di nodeSpanningTree
        FSArray childrenNodeSpanningTree = ((Constituent) nodeSpanningTree).getChildren();
        // Se il nodo non è foglia
        if (childrenNodeSpanningTree.size() != 0) {
            // Per ogni figlio
            for (int i = 0; i < childrenNodeSpanningTree.size(); ++i) {
                // Nodo figlio di nodeSpanningTree in esame
                Annotation childNodeSpanningTree = ((Annotation) childrenNodeSpanningTree.get(i));
                if(D) log.info("Tipologia nodo: " + childNodeSpanningTree.getClass().getSimpleName());

                // Se non è un preterminale
                if (!(childNodeSpanningTree instanceof Token)) {

                    // Crea un nuovo nodo NonTerminal e lo aggiunge all'albero di parsing BinaryTree
                    NonTerminal newChildNodeBinaryTree = new NonTerminal(childNodeSpanningTree.getClass().getSimpleName(), new ArrayList<Node>(), nodeBinaryTree);
                    nodeBinaryTree.getChildren().add(newChildNodeBinaryTree);

                    String partOfSpeech =  childNodeSpanningTree.getClass().getSimpleName();
                    if(D) log.info("NonTerminal: nodo Non Terminale aggiunto a BinaryTree: " + newChildNodeBinaryTree.getData());
                    // Esegue ricorsivamente la visita
                    treeVisit(childNodeSpanningTree, newChildNodeBinaryTree);
                } else {
                    // Essendo un Token, instanzia una stringa con il part of speech
                    String partOfSpeech = ((Token) childNodeSpanningTree).getPos().getPosValue();
                    if(D) log.info("Nodo PoS Token, partOfSpeech: " + partOfSpeech + " coveredText: " + childNodeSpanningTree.getCoveredText());

//                         Crea un nuovo nodo Terminal e lo aggiunge all'albero di parsing BinaryTree
                    Terminal newChildNodeBinaryTree = new Terminal(partOfSpeech, new ArrayList<Node>(), nodeBinaryTree);
                    nodeBinaryTree.getChildren().add(newChildNodeBinaryTree);

                    // Setta il valore "data" del nodo con la stringa coperta dal preterminale
                    nodeBinaryTree.getChildren().get(nodeBinaryTree.getChildren().
                            lastIndexOf(newChildNodeBinaryTree)).
                            setData(childNodeSpanningTree.getCoveredText());
                    if(D) log.info("Terminal: nodo PoS Token aggiunto a BinaryTree: " + newChildNodeBinaryTree.getData());

                }
            }

        }
    }


    private void printTreeForGUI(BinaryTree tree){
        Kernel.getIstance().getControlleGui().viewTree(tree);
    }


    /**
     * Creiamo il sotto albero che ci permette di ottenere l'oggetto navigando nuovamente l'albero in modo da sfruttare varie annotazioni
     * @param nodeSpanningTree
     * @param nodeBinaryTree
     */
    private void createObjectTree(Annotation nodeSpanningTree, Node nodeBinaryTree){

        if(D) log.info("Visita albero ricorsiva");

        // Figli di nodeSpanningTree
        FSArray childrenNodeSpanningTree = ((Constituent) nodeSpanningTree).getChildren();
        // Se il nodo non è foglia
        if (childrenNodeSpanningTree.size() != 0) {
            // Per ogni figlio
            for (int i = 0; i < childrenNodeSpanningTree.size(); ++i) {
                // Nodo figlio di nodeSpanningTree in esame
                Annotation childNodeSpanningTree = ((Annotation) childrenNodeSpanningTree.get(i));
                if(D) log.info("Tipologia nodo: " + childNodeSpanningTree.getClass().getSimpleName());

                // Se non è un preterminale
                if (!(childNodeSpanningTree instanceof Token)) {
                    // Crea un nuovo nodo NonTerminal e lo aggiunge all'albero di parsing BinaryTree
                    NonTerminal newChildNodeBinaryTree = new NonTerminal(childNodeSpanningTree.getClass().getSimpleName(), new ArrayList<Node>(), nodeBinaryTree);
                    nodeBinaryTree.getChildren().add(newChildNodeBinaryTree);
                    String partOfSpeech =  childNodeSpanningTree.getClass().getSimpleName();
                    if(!partOfSpeech.equals("NP")&&!partOfSpeech.equals("PP")&&!partOfSpeech.equals("SBAR")){
                        if(D) log.info("NonTerminal: nodo Non Terminale aggiunto a BinaryTree: " + newChildNodeBinaryTree.getData());
                        // Esegue ricorsivamente la visita
                        createObjectTree(childNodeSpanningTree, newChildNodeBinaryTree);
                    } else{
                        if(firstTime){
                            objectTree = new BinaryTree(new Node("ROOT", new ArrayList<Node>(), null));
                            firstTime=false;
                            createObjectTree(childNodeSpanningTree,objectTree.getRoot());
                            System.out.println("Sotto albero");
                        }else{
                            createObjectTree(childNodeSpanningTree, newChildNodeBinaryTree);
                        }

                    }
                } else {
                    // Essendo un Token, instanzia una stringa con il part of speech
                    String partOfSpeech = ((Token) childNodeSpanningTree).getPos().getPosValue();
                    if(D) log.info("Nodo PoS Token, partOfSpeech: " + partOfSpeech + " coveredText: " + childNodeSpanningTree.getCoveredText());

                    if (!findInTree(predicateTree.getRoot(),childNodeSpanningTree.getCoveredText())){
                        //Crea un nuovo nodo Terminal e lo aggiunge all'albero di parsing BinaryTree
                        Terminal newChildNodeBinaryTree = new Terminal(partOfSpeech, new ArrayList<Node>(), nodeBinaryTree);
                        nodeBinaryTree.getChildren().add(newChildNodeBinaryTree);
                        // Setta il valore "data" del nodo con la stringa coperta dal preterminale
                        nodeBinaryTree.getChildren().get(nodeBinaryTree.getChildren().lastIndexOf(newChildNodeBinaryTree)).setData(childNodeSpanningTree.getCoveredText());
                        if(D) log.info("Terminal: nodo PoS Token aggiunto a BinaryTree: " + newChildNodeBinaryTree.getData());
                    }
                }
            }
        }
        //Mettendo utilizzato per evitare di fermarci in sotto alberi che non ci interessano
        if(nodeBinaryTree.getData().equals("ROOT"))
            firstTime=true;
//        arrayAnnotation.add(nodeBinaryTree);
    }

    /**
     * Creiamo il sotto albero che ci permette di ottenere il predicato navigando nuovamente l'albero in modo da sfruttare varie annotazioni
     * @param nodeSpanningTree
     * @param nodeBinaryTree
     */
    private void createPredicateTree(Annotation nodeSpanningTree, Node nodeBinaryTree){

        if(D) log.info("Visita albero ricorsiva");

        // Figli di nodeSpanningTree
        FSArray childrenNodeSpanningTree = ((Constituent) nodeSpanningTree).getChildren();
        // Se il nodo non è foglia
        if (childrenNodeSpanningTree.size() != 0) {
            // Per ogni figlio
            for (int i = 0; i < childrenNodeSpanningTree.size(); ++i) {
                if(flagPredicate){
                    // Nodo figlio di nodeSpanningTree in esame
                    Annotation childNodeSpanningTree = ((Annotation) childrenNodeSpanningTree.get(i));
                    if (D) log.info("Tipologia nodo: " + childNodeSpanningTree.getClass().getSimpleName());

                    // Se non è un preterminale
                    if (!(childNodeSpanningTree instanceof Token)) {

                        String partOfSpeech = childNodeSpanningTree.getClass().getSimpleName();
                        if (!partOfSpeech.equals(SyntaticParserEngine.ADJP)) {
                            if (partOfSpeech.equals("PP")) {
                                break;
                            }

                            // Crea un nuovo nodo NonTerminal e lo aggiunge all'albero di parsing BinaryTree
                            NonTerminal newChildNodeBinaryTree = new NonTerminal(childNodeSpanningTree.getClass().getSimpleName(), new ArrayList<Node>(), nodeBinaryTree);
                            nodeBinaryTree.getChildren().add(newChildNodeBinaryTree);

                            if (!partOfSpeech.equals("VP")&&!partOfSpeech.equals("NP")) {
                                if (D)
                                    log.info("NonTerminal: nodo Non Terminale aggiunto a BinaryTree: " + newChildNodeBinaryTree.getData());
                                // Esegue ricorsivamente la visita
                                createPredicateTree(childNodeSpanningTree, newChildNodeBinaryTree);
                            } else {
                                if (firstTime) {
                                    predicateTree = new BinaryTree(new Node("ROOT", new ArrayList<Node>(), null));
                                    firstTime = false;
                                    createPredicateTree(childNodeSpanningTree, predicateTree.getRoot());
                                    System.out.println("Sotto albero Creato");
                                }else{
                                    createPredicateTree(childNodeSpanningTree, newChildNodeBinaryTree);
                                }
                            }
                        }
                    } else {

                        String partOfSpeech = ((Token) childNodeSpanningTree).getPos().getPosValue();

                        if (!firstTime) {
                            String s = childNodeSpanningTree.getCoveredText();
                            if (!Character.isUpperCase(s.charAt(0))) {
//
                                findAll(partOfSpeech, childNodeSpanningTree, nodeBinaryTree);
                            }else {
                                break;
                            }
                        }
                    }
                }
            }
        }
        firstTime=true;
    }


  private Boolean findInTree(Node node,String target){
        Boolean result=false;
      ArrayList<Node> children=node.getChildren();
      if(children.size()>0){
          for (Node n:children){
            if(n.getData().equals(target))
                result=true;
            else
                result=findInTree(n,target);
          }
      }
      return result;
    }

    private void findAll(String pos,Annotation childNodeSpanningTree,Node nodeBinaryTree) {

        String partOfSpeech=pos;

        if (partOfSpeech.equals(SyntaticParserEngine.NN) || partOfSpeech.equals(SyntaticParserEngine.NNS) || partOfSpeech.equals(SyntaticParserEngine.NNP) || partOfSpeech.equals(SyntaticParserEngine.NNPS) || partOfSpeech.equals(SyntaticParserEngine.VB) || partOfSpeech.equals(SyntaticParserEngine.VBD) || partOfSpeech.equals(SyntaticParserEngine.VBG) || partOfSpeech.equals(SyntaticParserEngine.VBN) || partOfSpeech.equals(SyntaticParserEngine.VBP) || partOfSpeech.equals(SyntaticParserEngine.VBZ)|| partOfSpeech.equals(SyntaticParserEngine.JJ)|| partOfSpeech.equals(SyntaticParserEngine.JJR)|| partOfSpeech.equals(SyntaticParserEngine.JJS)) {

            partOfSpeech = childNodeSpanningTree.getCoveredText();
            //Stiamo sfanculando i verbi di questo tipo perchè non ci servono e stiamo considerando una serie di verbi che potremmo avere nelle domande
            if(!partOfSpeech.equals("are")&&!partOfSpeech.equals("is")&&!partOfSpeech.equals("did")&&!partOfSpeech.equals("was")){

//                System.out.println("Lemma: " + ((Token) childNodeSpanningTree).getLemma().getCoveredText());
//                System.out.println("Lemma: " + ((Token) childNodeSpanningTree).getLemma().getCoveredText());
//                System.out.println("Lemma: " + ((Token) childNodeSpanningTree).getLemma().toString());

                if (SyntaticParserEngine.getInstance().getmLemmatizer()!=null)
                    partOfSpeech = ((Token) childNodeSpanningTree).getLemma().getValue();
                else if (SyntaticParserEngine.getInstance().getmStemmer()!=null)
                    partOfSpeech = ((Token) childNodeSpanningTree).getStem().getValue();
                else
                    partOfSpeech = ((Token) childNodeSpanningTree).getPos().getPosValue();



                if (D)
                    log.info("Nodo PoS Token, partOfSpeech: " + partOfSpeech + " coveredText: " + childNodeSpanningTree.getCoveredText());

                //Crea un nuovo nodo Terminal e lo aggiunge all'albero di parsing BinaryTree
                Terminal newChildNodeBinaryTree = new Terminal(partOfSpeech, new ArrayList<Node>(), nodeBinaryTree);
                nodeBinaryTree.getChildren().add(newChildNodeBinaryTree);



                if(SyntaticParserEngine.getInstance().getmLemmatizer()==null&&SyntaticParserEngine.getInstance().getmStemmer()==null)
                    nodeBinaryTree.getChildren().get(nodeBinaryTree.getChildren().lastIndexOf(newChildNodeBinaryTree)).setData(childNodeSpanningTree.getCoveredText());
                else
                    // Setta il valore "data" del nodo con la stringa coperta dal preterminale //al posto del setdata c'era inveece di part of speech childNodeSpanningTree.getCoveredText()
                    nodeBinaryTree.getChildren().get(nodeBinaryTree.getChildren().lastIndexOf(newChildNodeBinaryTree)).setData(partOfSpeech);

                if (D)
                    log.info("Terminal: nodo PoS Token aggiunto a BinaryTree: " + newChildNodeBinaryTree.getData());
            }
        }
    }

    /**
     * Creiamo il sotto albero che ci permette di ottenere il predicato navigando nuovamente l'albero in modo da sfruttare varie annotazioni
     * Sono interessato al sotto albero del soggetto solo se è una query di tipo ask è quindi voglio sapere se tizio o caio hanno fatto qualcosa
     * @param nodeSpanningTree
     * @param nodeBinaryTree
     * @param askQuery
     */
    private void createSubjectTree(Annotation nodeSpanningTree, Node nodeBinaryTree,Boolean askQuery){

        if(D) log.info("Visita albero ricorsiva");

        // Figli di nodeSpanningTree
        FSArray childrenNodeSpanningTree = ((Constituent) nodeSpanningTree).getChildren();

        // Se il nodo non è foglia
        if (childrenNodeSpanningTree.size() != 0) {
            // Per ogni figlio
            for (int i = 0; i < childrenNodeSpanningTree.size(); ++i) {
                // Nodo figlio di nodeSpanningTree in esame
                Annotation childNodeSpanningTree = ((Annotation) childrenNodeSpanningTree.get(i));
                if(D) log.info("Tipologia nodo: " + childNodeSpanningTree.getClass().getSimpleName());

                // Se non è un preterminale
                if (!(childNodeSpanningTree instanceof Token)) {
                    if(askQuery){
                        // Crea un nuovo nodo NonTerminal e lo aggiunge all'albero di parsing BinaryTree
                        NonTerminal newChildNodeBinaryTree = new NonTerminal(childNodeSpanningTree.getClass().getSimpleName(), new ArrayList<Node>(), nodeBinaryTree);
                        nodeBinaryTree.getChildren().add(newChildNodeBinaryTree);

                        String partOfSpeech =  childNodeSpanningTree.getClass().getSimpleName();
                        if(!partOfSpeech.equals("NP")){
                            if(!firstTime){
                                break;
                            }
                            if(D) log.info("NonTerminal: nodo Non Terminale aggiunto a BinaryTree: " + newChildNodeBinaryTree.getData());
                            // Esegue ricorsivamente la visita
                            createSubjectTree(childNodeSpanningTree, newChildNodeBinaryTree,askQuery);
                        }else{
                            if(firstTime){
                                subjectTree = new BinaryTree(new Node("ROOT", new ArrayList<Node>(), null));
                                firstTime=false;
                                createSubjectTree(childNodeSpanningTree,subjectTree.getRoot(),askQuery);
                                System.out.println("Sotto albero");
                            }else{
                                break;
                            }
                        }
                    }
                } else {

                    // Essendo un Token, instanzia una stringa con il part of speech
                    String partOfSpeech = ((Token) childNodeSpanningTree).getPos().getPosValue();
                    if(D) log.info("Nodo PoS Token, partOfSpeech: " + partOfSpeech + " coveredText: " + childNodeSpanningTree.getCoveredText());

                    if(childNodeSpanningTree.getCoveredText().equals("Is")){
                        askQuery=true;
                    }

                    //Crea un nuovo nodo Terminal e lo aggiunge all'albero di parsing BinaryTree
                    Terminal newChildNodeBinaryTree = new Terminal(partOfSpeech, new ArrayList<Node>(), nodeBinaryTree);
                    nodeBinaryTree.getChildren().add(newChildNodeBinaryTree);

                    // Setta il valore "data" del nodo con la stringa coperta dal preterminale
                    nodeBinaryTree.getChildren().get(nodeBinaryTree.getChildren().lastIndexOf(newChildNodeBinaryTree)).setData(childNodeSpanningTree.getCoveredText());
                    if(D) log.info("Terminal: nodo PoS Token aggiunto a BinaryTree: " + newChildNodeBinaryTree.getData());
                }
            }

        }
    }


    /**
     * Estrae la risorsa da gli alberi da noi generati
     * @param node
     * @return
     */
    private String determinateTreeResource(Node node){
        String result="";
        ArrayList<Node> children=node.getChildren();
        if(children.size()>0){
            for (Node n:children){
                if(result.equals(""))
                    result=determinateTreeResource(n);
                else if(determinateTreeResource(n).equals(":"))
                    result=result+""+determinateTreeResource(n);//facciamo questo trick perchè cosi i titoli con i due punti vengono scritti bene
                    else{
                    String res=determinateTreeResource(n);
                    if(res.startsWith("'"))
                        result=result+res;
                    else
                                result=result+" "+res;
                }

            }
        }else{
            if(!node.getData().equals("NP")&&!node.getData().equals("PP"))
                return node.getData();

        }
        return result;
    }

    /**
     * Prepara le risorse per la fase di generazone successiva della query
     */
    private void buildQuery(){
        TripleRDFComponent subjectTriple = null;
        TripleRDFComponent predicateTriple = null;
        TripleRDFComponent objectTriple = null;

        if(subject!=null)
            subjectTriple=new ResourceRDF(subject);
        else
            subjectTriple=new ElementRDF();
        if(predicate!=null)
            predicateTriple=new ResourceRDF(predicate);
        if(object!=null)
            objectTriple=new ResourceRDF(object);

        if(predicate!=null&&object!=null) {
            mQueryList.add(new QuerySPARQL(new TripleRDF(subjectTriple,predicateTriple,objectTriple)));
        }
    }

}
