package cs.art.ia.model.rdf;

public class TripleRDF {

    private TripleRDFComponent subject;
    private TripleRDFComponent predicate;
    private TripleRDFComponent object;

    public TripleRDF(TripleRDFComponent subject, TripleRDFComponent predicate, TripleRDFComponent object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }
    
    public TripleRDFComponent getSubject() {
        return subject;
    }

    public void setSubject(TripleRDFComponent subject) {
        this.subject = subject;
    }

    public TripleRDFComponent getPredicate() {
        return predicate;
    }

    public void setPredicate(TripleRDFComponent predicate) {
        this.predicate = predicate;
    }

    public TripleRDFComponent getObject() {
        return object;
    }

    public void setObject(TripleRDFComponent object) {
        this.object = object;
    }

 
    
    public String toString(){
    	
    	String toReturn = new String();
    	
		if(subject instanceof ElementRDF){
			toReturn += ((ElementRDF) subject).getId() + " ";
		} else {
			toReturn += ((ResourceRDF) subject).getResourceRDF() + " ";
		}
		
		if(predicate instanceof ElementRDF){
			toReturn += ((ElementRDF) predicate).getId() + " ";
		} else {
			toReturn += ((ResourceRDF) predicate).getResourceRDF() + " ";
		}
		
		if(object instanceof ElementRDF){
			toReturn += ((ElementRDF) object).getId() + " ";
		} else {
			toReturn += ((ResourceRDF) object).getResourceRDF() + " ";
		}
    	
		return toReturn;
    }
}
