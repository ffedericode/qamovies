package cs.art.ia.utils;


        import cs.art.ia.model.QuerySPARQLResult;
        import cs.art.ia.model.rdf.ResourceRDF;
        import it.uniroma2.art.owlart.model.ARTNode;

        import java.util.*;


/**
 *
 */
public class Utility {

    /**
     * Converta la stringa in camelCase
     * @param lower
     * @param upper
     * @return
     */
    public static String convertToCamelCase(String lower,String upper){
        char s1 =  Character.toUpperCase(upper.charAt(0));
        char s2=    Character.toLowerCase(lower.charAt(0));
        String newUpper=s1+upper.substring(1);
        String newLower=s2+lower.substring(1);
        return newLower+newUpper;
    }


    /**
     * Elimina proprità duplicati all'interno di una lista
     * @param listDuplicate
     * @return
     */
    public static List<String> eliminateDuplicatePropertyQuery(List<String> listDuplicate){
        Set<String> foo = new HashSet<String>(listDuplicate);
        return new ArrayList<>(foo);
    }


    /**
     * Estrae tutte le propità da ricercare nel caso di inserimento di proprità multiple
     * @param resource
     * @return
     */
    public static List<String> determinateResource(ResourceRDF resource){
        // Usa l'API JAWS per interrogare Wordnet
//			String entry = resource.getmValue();
        System.out.println("Resource arrivato "+resource.getValue());
        List<String> synonymQuery= new ArrayList<>();
        synonymQuery.add(resource.getValue());
        List<String> split= Arrays.asList(resource.getValue().split(" "));
        if(split.size()>1){
            for(String s:split){
                synonymQuery.add(s);
            }
        }
        return  synonymQuery;
    }


    /**
     * Visualizza risultati sulla console
     * @param
     */
    public static void visualizeResults(List<QuerySPARQLResult> listQueryResults) {
        if(listQueryResults.isEmpty()){
            System.out.println("No results were found.");
        } else {

            // Get results as String distinguishing from URI and Literals.
            // Remove duplicates also.

            List<String> listResults = getResultsWithoutDuplicates(listQueryResults);

            System.err.println("Results:");
            for(String result : listResults){
                System.err.println(result);
            }

        }
    }


    /**
     * Elimina i risultati duplicati dalla risposta
     * @param listResults
     * @return
     */
    public static List<String> getResultsWithoutDuplicates(List<QuerySPARQLResult> listResults){
        List<String> noDuplicateResults = new ArrayList<String>();


        for(QuerySPARQLResult queryResult : listResults){
            ARTNode artNode = queryResult.getValue();
            if(artNode.isURIResource()){
                String localName = artNode.asURIResource().getLocalName();
                System.out.println("Local Name: "+localName);
                localName=localName.replaceAll("_"," ");
                if(!noDuplicateResults.contains(localName)){
                    noDuplicateResults.add(localName);
                }
            } else if(artNode.isLiteral()){
                String label = artNode.asLiteral().getLabel();
                String lang="";
                String literalResult = label;
                if(artNode.asLiteral().getDatatype()!=null){
                    lang = artNode.asLiteral().getDatatype().getLocalName();
                    literalResult+=" " + lang;
                }
                System.out.println("literalResult: "+literalResult);
                if(!noDuplicateResults.contains(literalResult)){
                    noDuplicateResults.add(literalResult);
                }
            }
        }

        return noDuplicateResults;
    }


}