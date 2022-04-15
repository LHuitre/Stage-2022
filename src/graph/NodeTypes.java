package graph;

public enum NodeTypes implements Types {
	// noeuds outils
	START,
	END,
	
	// noeuds de travail
	WORD,
	MWE,
	POS,
	LEMMA,
	GROUP,
	;
	
	public static boolean isType(String inputType) {
		for(NodeTypes type: NodeTypes.values()) {
			if(inputType.equals(type.name())){
				return true;
			}
		}
		return false;
	}
	
	public String toString() {
		if(this==START || this==END) {
			return ":"+ this.name() +":";
		}
		return this.name();
	}
}
