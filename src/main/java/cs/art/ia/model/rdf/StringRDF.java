package cs.art.ia.model.rdf;



public class StringRDF extends TripleRDFComponent {

    private String stringRDF;

    public StringRDF(String stringRDF) {
        this.stringRDF = stringRDF;
    }

    public String getStringRDF() {
        return stringRDF;
    }

    public void setStringRDF(String stringRDF) {
        this.stringRDF = stringRDF;
    }

    @Override
    public String getValue() {
        return stringRDF;
    }
}
