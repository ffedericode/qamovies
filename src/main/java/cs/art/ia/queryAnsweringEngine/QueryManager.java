
package cs.art.ia.queryAnsweringEngine;

import cs.art.ia.kernel.Kernel;
import cs.art.ia.utils.Utility;
import cs.art.ia.model.QuerySPARQL;
import cs.art.ia.model.rdf.StringRDF;
import cs.art.ia.model.rdf.TripleRDFComponent;
import cs.art.ia.model.rdf.ElementRDF;
import it.uniroma2.art.owlart.exceptions.ModelAccessException;
import it.uniroma2.art.owlart.exceptions.ModelCreationException;
import it.uniroma2.art.owlart.exceptions.QueryEvaluationException;
import it.uniroma2.art.owlart.exceptions.UnsupportedQueryLanguageException;

import it.uniroma2.art.owlart.model.ARTNode;
import it.uniroma2.art.owlart.models.OWLArtModelFactory;
import it.uniroma2.art.owlart.models.TripleQueryModelHTTPConnection;
import it.uniroma2.art.owlart.query.MalformedQueryException;
import it.uniroma2.art.owlart.query.QueryLanguage;
import it.uniroma2.art.owlart.query.TupleBindings;
import it.uniroma2.art.owlart.query.TupleBindingsIterator;
import it.uniroma2.art.owlart.query.TupleQuery;
import it.uniroma2.art.owlart.sesame2impl.factory.ARTModelFactorySesame2Impl;
import it.uniroma2.art.owlart.sesame2impl.models.conf.Sesame2ModelConfiguration;

import java.util.*;

import com.google.common.base.CaseFormat;
import org.openrdf.query.algebra.Str;

/**
 *
 */
public class QueryManager {

	private static final String ENDPOINT_DBPEDIA = "http://dbpedia.org/sparql";

	private static final String PROPERTY_URI_DBPEDIA = "http://dbpedia.org/property/";

	private static final String RESOURCE_URI_DBPEDIA = "http://dbpedia.org/resource/";

	private static final String SPARQL_SELECT_LABEL = "SELECT";

	private static final String SPARQL_WHERE_LABEL = "WHERE";

	private List<String> preQuery=new ArrayList<String>();


	/**
	 * Creazione della query SPARQL
	 * @param queries
	 * @return
	 * @throws Exception
	 */
	public List<String> buildQuery(List<QuerySPARQL> queries,String input) throws Exception {

		TripleRDFComponent subject = null;
		TripleRDFComponent predicate = null;
		TripleRDFComponent object = null;

		List<String> querySparql=new ArrayList<String>();

		for (QuerySPARQL query : queries) {
			subject = query.getTripleRDF().getSubject();
			predicate = query.getTripleRDF().getPredicate();
			object = query.getTripleRDF().getObject();

			// Pre query con il predicate per estrapolare tutte le proprieta con
			// cui fare la query ed ottenere quindi piu significati possibili
//            if(!usingSyonimer)
			preQuery = Utility.eliminateDuplicatePropertyQuery(resultPrePredicateQuery(predicate));

			for (String queryPredicate : preQuery) {
				// Costrusici un nuovo string builder
				StringBuilder transformedQuery = new StringBuilder();
				System.out.println("Predicate Query: "+queryPredicate);

				// Costruzione intestazione query
				transformedQuery.append(SPARQL_SELECT_LABEL);
				if(input.contains("movies")||input.contains("films"))
					transformedQuery.append("?movie (count( ?1) as ?countReg )");
				else if (subject.getValue().equals("1"))
					transformedQuery.append("?1 (count( ?1) as ?countReg )");
				else
					transformedQuery.append("?movie (count( ?movie) as ?countReg )");
				transformedQuery.append(SPARQL_WHERE_LABEL);
				transformedQuery.append("{ \n");
				transformedQuery.append("?movie a <http://dbpedia.org/ontology/"+Kernel.getIstance().getKernelEngine().getOntologyReference()+"> .\n");
				transformedQuery.append("?movie <http://dbpedia.org/ontology/"+Kernel.getIstance().getKernelEngine().getFilterProperty()+"> ?abstract .\n");
				transformedQuery.append("?movie <"+queryPredicate+">");
				transformedQuery.append(convertTripleElementToSPARQLElement(subject, true));
				transformedQuery.append(". \n");
				transformedQuery.append("FILTER ( regex(str(?abstract), \"^.*" + object.getValue() + ".*\"))\n");
				transformedQuery.append("}");
				if(input.contains("movies")||input.contains("films"))
					transformedQuery.append("order by desc(?countReg)");
				else
					transformedQuery.append("order by desc(?countReg) LIMIT 1");

				querySparql.add(transformedQuery.toString());
			}
		}

		// return sparql query
		return querySparql;

	}


	/**
	 * Creazione della query SPARQL che sfrutta il Synonimer
	 * @param subject
	 * @param newPredicate
	 * @param object
	 * @return
	 * @throws UnsupportedQueryLanguageException
	 * @throws QueryEvaluationException
	 * @throws MalformedQueryException
	 * @throws ModelCreationException
	 * @throws ModelAccessException
	 */
	public List<String> buildQuerySynonymer(TripleRDFComponent subject, String newPredicate, TripleRDFComponent object,String input) throws UnsupportedQueryLanguageException, QueryEvaluationException, MalformedQueryException, ModelCreationException, ModelAccessException {

			List<String> querySparql=new ArrayList<String>();

		preQuery = Utility.eliminateDuplicatePropertyQuery(resultPrePredicateQuerySynonymer(newPredicate));

			if(preQuery.size()>0) {
				for (String queryPredicate : preQuery) {
					// Costrusici un nuovo string builder
					StringBuilder transformedQuery = new StringBuilder();
					System.out.println("Predicate Query: " + queryPredicate);

					// Costruzione intestazione query
					transformedQuery.append(SPARQL_SELECT_LABEL);

					if(input.contains("movies")||input.contains("films"))
						transformedQuery.append("?movie (count( ?1) as ?countReg )");
					else if (subject.getValue().equals("1"))
						transformedQuery.append("?1 (count( ?1) as ?countReg )");
					else
						transformedQuery.append("?movie (count( ?movie) as ?countReg )");
					transformedQuery.append(SPARQL_WHERE_LABEL);
					transformedQuery.append("{ \n");
					transformedQuery.append("?movie a <http://dbpedia.org/ontology/"+Kernel.getIstance().getKernelEngine().getOntologyReference()+"> .\n");
					transformedQuery.append("?movie <http://dbpedia.org/ontology/"+Kernel.getIstance().getKernelEngine().getFilterProperty()+"> ?abstract .\n");
					transformedQuery.append("?movie " + "<" + queryPredicate + ">");
					transformedQuery.append(convertTripleElementToSPARQLElement(subject, true));
					transformedQuery.append(". \n");
					transformedQuery.append("FILTER ( regex(str(?abstract), \"^.*" + object.getValue() + ".*\"))\n");
					transformedQuery.append("}");
					if(input.contains("movies")||input.contains("films"))
						transformedQuery.append("order by desc(?countReg)");
					else
						transformedQuery.append("order by desc(?countReg) LIMIT 1");

					querySparql.add(transformedQuery.toString());
				}
			}

			return querySparql;
		}

	public String determinateContest(List<QuerySPARQL> queries) throws Exception {

		if(queries==null)
			throw new NullPointerException();

		String var = "";

		for (QuerySPARQL querySPARQL : queries) {
			TripleRDFComponent subject = querySPARQL.getTripleRDF().getSubject();
			TripleRDFComponent predicate = querySPARQL.getTripleRDF().getPredicate();
			TripleRDFComponent object = querySPARQL.getTripleRDF().getObject();

			if((subject==null)|(predicate==null)|(object==null))
				throw new Exception("Il soggetto,predicato oppure oggetto della tripla sono null");


			if (subject instanceof ElementRDF) {
				var = subject.getValue();
				break;
			}
			if (predicate instanceof ElementRDF) {
				var = predicate.getValue();
				break;
			}
			if (object instanceof ElementRDF) {
				var = object.getValue();
				break;
			}
		}

		return var;
	}


	public boolean yesOrNoQuery(List<QuerySPARQL> querySPARQL) throws Exception {
		String string = determinateContest(querySPARQL);
		if (string == "") {
			return true;
		}
		return false;
	}


	/**
	 * Invia la query creata con i metodi sopra citati, all'Endpoint di Dbpedia al fine di ottenere una risposta (si spera)
	 * @param querySPARQL
	 * @param owlArtModelFactory
	 * @param isAsk
	 * @return
	 * @throws ModelCreationException
	 * @throws UnsupportedQueryLanguageException
	 * @throws MalformedQueryException
	 * @throws ModelAccessException
	 * @throws QueryEvaluationException
	 */
	public Map<String, List<ARTNode>> executeQuery(String querySPARQL, OWLArtModelFactory owlArtModelFactory, boolean isAsk) throws ModelCreationException, UnsupportedQueryLanguageException, MalformedQueryException, ModelAccessException, QueryEvaluationException {

		Map<String, List<ARTNode>> resultsQuery = new HashMap<>();
		TripleQueryModelHTTPConnection connection;
		connection = owlArtModelFactory.loadTripleQueryHTTPConnection(ENDPOINT_DBPEDIA);
		TupleQuery query = connection.createTupleQuery(QueryLanguage.SPARQL, querySPARQL, "");
		TupleBindingsIterator itRes = query.evaluate(false);

		while (itRes.hasNext()) {
			TupleBindings tuple = itRes.next();

			for (String bindName : tuple.getBindingNames()) {

				ARTNode artNode = tuple.getBoundValue(bindName);
				appendResponse(resultsQuery, bindName, artNode);
			}
		}
		connection.disconnect();

		return resultsQuery;
	}

	private void appendResponse(Map<String, List<ARTNode>> resultsOfSPARQLQuery, String value, ARTNode node) {
		if (resultsOfSPARQLQuery.get(value) != null) {
			resultsOfSPARQLQuery.get(value).add(node);
		} else {
			resultsOfSPARQLQuery.put(value, new ArrayList<ARTNode>());
			resultsOfSPARQLQuery.get(value).add(node);
		}
	}


	/**
	 * Permette di ottenere un lista di proprietà coerenti con l'ontologia inserita e il predicato fornito
	 * @param element
	 * @return
	 * @throws ModelCreationException
	 * @throws UnsupportedQueryLanguageException
	 * @throws MalformedQueryException
	 * @throws ModelAccessException
	 * @throws QueryEvaluationException
	 */
	private List<String> resultPrePredicateQuery(TripleRDFComponent element) throws ModelCreationException, UnsupportedQueryLanguageException, MalformedQueryException, ModelAccessException, QueryEvaluationException {

		String[] predicateList = formattingResultForPredicate(element.getValue()).split(" ");
		List<String> preQueryPredicate = new ArrayList<String>();
		if (predicateList.length > 1) {
			for (int i = 0; i < predicateList.length - 1; i++) {
				String query = predicateList[i];
				query = query.concat("_" + predicateList[i + 1]);
				String effectivequery = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, query);
				preQueryPredicate.add(effectivequery);
			}
		} else
			preQueryPredicate.add(element.getValue());

        List<String> querySparql = new ArrayList<String>();
        for (String string : preQueryPredicate) {
            StringBuilder transformedQuery = new StringBuilder();
            transformedQuery.append("select ?x where{\n");
            transformedQuery.append("?movie a <http://dbpedia.org/ontology/"+Kernel.getIstance().getKernelEngine().getOntologyReference()+">.\n");
            transformedQuery.append("?movie ?x ?y\n");
            transformedQuery.append("FILTER ( regex(str(?x), \"^.*" + string + ".*\"))\n");
            transformedQuery.append("}");
            System.out.println("Pre Query di ricerca: " + transformedQuery.toString());
            querySparql.add(transformedQuery.toString());
        }

        List<String> result = new ArrayList<String>();

        OWLArtModelFactory<Sesame2ModelConfiguration> factory = OWLArtModelFactory.createModelFactory(new ARTModelFactorySesame2Impl());

        for (String query : querySparql) {
            Map<String, List<ARTNode>> response = executeQuery(query, factory, false);
            if (response.get("x") != null) {
                for (ARTNode node : response.get("x")) {
                    System.out.println("Node: " + node.getNominalValue());
                    result.add(node.getNominalValue());
                }
            }

        }
        result=Utility.eliminateDuplicatePropertyQuery(result);

        return result;
	}

	/**
	 * Permette di ottenere un lista di proprietà coerenti con l'ontologia inserita e il predicato fornito , nel caso di utilizzo del Synonimer
	 * @param element
	 * @return
	 * @throws ModelCreationException
	 * @throws UnsupportedQueryLanguageException
	 * @throws MalformedQueryException
	 * @throws ModelAccessException
	 * @throws QueryEvaluationException
	 */
	private List<String> resultPrePredicateQuerySynonymer(String element) throws UnsupportedQueryLanguageException, QueryEvaluationException, MalformedQueryException, ModelCreationException, ModelAccessException {

			String[] predicateList = formattingResultForPredicate(element).split(" ");
			List<String> preQueryPredicate = new ArrayList<String>();
			if (predicateList.length > 1) {
				for (int i = 0; i < predicateList.length - 1; i++) {
					String query = predicateList[i];
					query = query.concat("_" + predicateList[i + 1]);
					String effectivequery = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, query);
					preQueryPredicate.add(effectivequery);
				}
			} else
				preQueryPredicate.add(element);

			List<String> querySparql = new ArrayList<String>();
			for (String string : preQueryPredicate) {
				StringBuilder transformedQuery = new StringBuilder();
				transformedQuery.append("select ?x where{\n");
				transformedQuery.append("?movie a <http://dbpedia.org/ontology/"+Kernel.getIstance().getKernelEngine().getOntologyReference()+">.\n");
				transformedQuery.append("?movie ?x ?y\n");
				transformedQuery.append("FILTER ( regex(str(?x), \"^.*" + string + ".*\"))\n");
				transformedQuery.append("}");
				System.out.println("Pre Query di ricerca: " + transformedQuery.toString());
				querySparql.add(transformedQuery.toString());
			}

			List<String> result = new ArrayList<String>();

			OWLArtModelFactory<Sesame2ModelConfiguration> factory = OWLArtModelFactory.createModelFactory(new ARTModelFactorySesame2Impl());

			for (String query : querySparql) {
				Map<String, List<ARTNode>> response = executeQuery(query, factory, false);
				if (response.get("x") != null) {
					for (ARTNode node : response.get("x")) {
						System.out.println("Node: " + node.getNominalValue());
						result.add(node.getNominalValue());
					}
				}

			}

			result=Utility.eliminateDuplicatePropertyQuery(result);

			return result;
		}


	/**
	 * Converte la risorsa fornita in una risorsa in formato SPARQL
	 * @param element
	 * @param isResource
	 * @return
	 * @throws ModelCreationException
	 * @throws UnsupportedQueryLanguageException
	 * @throws MalformedQueryException
	 * @throws ModelAccessException
	 * @throws QueryEvaluationException
	 */
	private String convertTripleElementToSPARQLElement(TripleRDFComponent element, boolean isResource) throws ModelCreationException, UnsupportedQueryLanguageException, MalformedQueryException, ModelAccessException, QueryEvaluationException {

		if (element instanceof ElementRDF || element instanceof StringRDF) {
			return " ?" + formattingResult(element.getValue());
		} else {
			if (isResource) {
				return " <" + RESOURCE_URI_DBPEDIA + formattingResult(element.getValue()) + "> ";
			} else {
				return " <" + PROPERTY_URI_DBPEDIA + formattingResult(element.getValue()) + "> ";
			}

		}

	}

	private String formattingResultForPredicate(List<String> subquestion) {
		String result = new String();
		for (int i = 0; i < subquestion.size(); i++) {
			if (subquestion.get(i).equals("the"))
				continue;
			result += subquestion.get(i) + " ";
		}

		return result.substring(0, result.length() - 1);
	}

	private String formattingResultForPredicate(String subquestion) {
		return formattingResultForPredicate(Arrays.asList(subquestion.split(" |\\?|\\'")));
	}

	private String formattingResult(List<String> subquestion) {
		String result = new String();

		for (int i = 0; i < subquestion.size(); i++) {
			if (subquestion.get(i).equals("the"))
				continue;
			result += subquestion.get(i) + "_";
		}

		return result.substring(0, result.length() - 1);
	}

	private String formattingResult(String subquestion) {
		return formattingResult(Arrays.asList(subquestion.split(" |\\?|\\'")));
	}
}
