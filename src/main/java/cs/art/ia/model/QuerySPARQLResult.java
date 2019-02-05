package cs.art.ia.model;

import it.uniroma2.art.owlart.model.ARTNode;


public class QuerySPARQLResult {

    private ARTNode artNodeValue;

    public QuerySPARQLResult(ARTNode mValue) {
        this.artNodeValue = mValue;
    }

    public ARTNode getValue() {
        return artNodeValue;
    }

    public void setValue(ARTNode value) {
        this.artNodeValue = value;
    }
}
