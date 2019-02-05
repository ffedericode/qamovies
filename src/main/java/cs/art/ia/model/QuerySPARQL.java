package cs.art.ia.model;

import cs.art.ia.model.rdf.TripleRDF;

public class QuerySPARQL {

    private TripleRDF tripleRDF;

    public QuerySPARQL(TripleRDF tripleRDF) {
        this.tripleRDF = tripleRDF;
    }

    public TripleRDF getTripleRDF() {
        return tripleRDF;
    }

    public void setTripleRDF(TripleRDF tripleRDF) {
        this.tripleRDF = tripleRDF;
    }
    
    public String toString(){
    	return tripleRDF.toString();
    }
}
