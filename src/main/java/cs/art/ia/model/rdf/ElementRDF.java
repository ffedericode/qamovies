package cs.art.ia.model.rdf;

public class ElementRDF extends TripleRDFComponent {

	private static Integer baseId = 1;

    private Integer id;
	
	public ElementRDF(){
        id = baseId;
        incrementId();
    }
	
	public static void incrementId(){
		baseId++;
	}
	
	public static void resetId(){
		baseId = 1;
	}
	
	public Integer getId(){
		return id;
	}

	@Override
	public String getValue() {
		return id.toString();
	}
	
}

