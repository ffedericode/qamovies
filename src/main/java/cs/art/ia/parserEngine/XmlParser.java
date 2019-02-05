package cs.art.ia.parserEngine;

import cs.art.ia.model.QuerySPARQL;
import cs.art.ia.model.rdf.ResourceRDF;
import cs.art.ia.model.rdf.TripleRDF;
import cs.art.ia.model.rdf.TripleRDFComponent;
import cs.art.ia.model.rdf.ElementRDF;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlParser {

    private Document document;


    /**
     * Inizializza il parser Xml caricando i pattern da valutare dal file di configurazione
     */
    public XmlParser() {
        loadXmlPattern();
    }

    /**
     * Parse la stringa in input utilizzando i pattern forniti nel file di configurazione al fine di trovare qualche match valido
     * @param input
     * @return
     * @throws Exception
     */
    public List<QuerySPARQL> parse(String input) throws Exception
    {
    	List<QuerySPARQL> listQuerySPARQL = new ArrayList<QuerySPARQL>();

        input=input.trim();

        if(input.length() == 0)
        	throw new Exception("User Input e' null");
        
        System.out.println("UserInput "+input);
        
    	TripleRDFComponent mSubject = new ElementRDF();
        TripleRDFComponent mPredicate = new ElementRDF();
        TripleRDFComponent mObject = new ElementRDF();

    	int mSubject_index;
    	int mPredicate_index;
    	int mObject_index;


    	XmlParser mXmlParser = new XmlParser();
    	List<String> mTemplates = mXmlParser.parseXmlPattern();
    	
    	Pattern mPattern;
    	Matcher mMatcher;


    	String wildcardStandard = "(.+?)";
    	
    	
    	boolean patternMatch = false;


        if (mTemplates != null) {
            for(String pattern: mTemplates)
            {

                String regex = pattern.replaceAll("#O|#S|#P", wildcardStandard);

                mPattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);

                mMatcher = mPattern.matcher(input.replaceAll("",""));

                System.out.println("Matcher con pattern cambiati: "+mMatcher.toString());

                if(mMatcher.matches())
                {
                    System.out.println("Template matched "+pattern);
                    patternMatch = true;

                    ArrayList<String> structure = new ArrayList<String>();
                    int gap = 0;
                    int[] groupsInBetween = new int[3];

                    for(int i=0;i< pattern.length()-1; i++)
                    {
                        if(pattern.substring(i, i+1).equals("("))
                        {
                            groupsInBetween[gap]++;
                        }
                        else
                            if(pattern.substring(i, i+2).matches("#[SOP]"))
                            {
                                structure.add(pattern.substring(i, i+2));
                                gap++;
                            }
                    }
                    mObject_index = structure.indexOf("#O")+1;
                    for(int i=0;i<=structure.indexOf("#O");i++)
                    {
                        mObject_index += groupsInBetween[i];
                    }
                    String obj = mMatcher.group(mObject_index);
                        mObject = new ResourceRDF(obj);

                    mPredicate_index = structure.indexOf("#P")+1;
                    for(int i=0;i<=structure.indexOf("#P");i++)
                    {
                        mPredicate_index += groupsInBetween[i];
                    }
                    mPredicate = new ResourceRDF(mMatcher.group(mPredicate_index));

                    if(pattern.contains("#S"))
                    {
                        mSubject_index = structure.indexOf("#S")+1;
                        for(int i=0;i<=structure.indexOf("#S");i++)
                        {
                            mSubject_index += groupsInBetween[i];
                        }
                        mSubject = new ResourceRDF(mMatcher.group(mSubject_index));
                    }
                    break;
                }
            }
        }
    	

    	if(!patternMatch)
    	{
    		throw new Exception("Pattern non corrispondente");
    	}
    	

    	TripleRDF tripleRDF =new TripleRDF(mSubject, mPredicate, mObject);
    	QuerySPARQL querySPARQL =new QuerySPARQL(tripleRDF);
    	listQuerySPARQL.add(querySPARQL);
        return listQuerySPARQL;
    }

    /**
     * Carica i pattern dal file di configurazione
     * Ci sono vari modi per caricare i template xml interni, nei testi in grigio  è presente una versione precedente o cmq un precedente test
     */
    private void loadXmlPattern() {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            String file=getClass().getResource("/template.xml").getPath();
//            file=getClass().getClassLoader().getResourceAsStream("template.xml");
            document = documentBuilder.parse(getClass().getClassLoader().getResourceAsStream("template.xml"));
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Analizza i pattern caricati in modo da poterli utilizzare
     * @return
     */
    private List<String> parseXmlPattern() {
        XPath xPath = XPathFactory.newInstance().newXPath();
        String expression = "/template/pattern/text()";

        try {
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
            if(nodeList != null) {

                List<String> pattern = new ArrayList<String>();
                for (int i = 0; i < nodeList.getLength(); i++) {

                    Node node = nodeList.item(i);
                    pattern.add(node.getTextContent());
                }
                return pattern;
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

}
