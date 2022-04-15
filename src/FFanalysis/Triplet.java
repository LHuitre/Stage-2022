package FFanalysis;

import graph.Edge;
import graph.GraphAugmented;

public class Triplet {
	String sujet;
	String predicat;
	String objet;
	
	public Triplet(String sujet, String predicat, String objet) {
		this.sujet = sujet;
		this.predicat = predicat;
		this.objet = objet;
	}
	
	/*public Triplet(GraphAugmented graph, Edge edge) {
		this.sujet = graph.getEdgeSource(edge).getLabel();
		this.predicat = edge.getType().toString().toLowerCase();
		this.objet = graph.getEdgeTarget(edge).getLabel();
	}*/
	

	public String getSujet() {
		return sujet;
	}

	public String getPredicat() {
		return predicat;
	}

	public String getObjet() {
		return objet;
	}
	
	public String toString() {
		return String.format("(%s;%s;%s)", sujet, predicat, objet);
	}
}
