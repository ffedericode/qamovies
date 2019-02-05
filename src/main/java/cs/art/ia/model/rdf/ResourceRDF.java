package cs.art.ia.model.rdf;


public class ResourceRDF extends TripleRDFComponent {

    private String resourceRDF;

    public ResourceRDF(String resourceRDF) {
        this.resourceRDF = resourceRDF;
    }

    public String getResourceRDF() {
        return resourceRDF;
    }

    public void setResourceRDF(String resourceRDF) {
        this.resourceRDF = resourceRDF;
    }

	@Override
	public String getValue() {
		return resourceRDF;
	}
}
