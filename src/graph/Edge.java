package graph;

import org.jgrapht.graph.DefaultEdge;

public class Edge extends DefaultEdge implements GraphElement{
	//TODO : créer champs et méthodes dans GraphElement pour Edge ET Node
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_WEIGHT = 1;
	
	private EdgeTypes type;
	private int weight;
	private String label;
	
	public Edge(EdgeTypes type, int weight, String data) {
		super();
		this.type = type;
		this.weight = weight;
		this.label = data;
	}

	public Edge(EdgeTypes type) {
		super();
		this.type = type;
		this.weight = DEFAULT_WEIGHT;
		this.label = "";
	}

	public Edge(EdgeTypes type, int weight) {
		super();
		this.type = type;
		this.weight = weight;
		this.label = "";
	}

	public Edge(EdgeTypes type, String data) {
		super();
		this.type = type;
		this.weight = DEFAULT_WEIGHT;
		this.label = data;
	}
	
	

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	public void negativeWeight() {
		this.weight = -DEFAULT_WEIGHT;
	}

	public EdgeTypes getType() {
		return type;
	}

	public String getLabel() {
		return label;
	}
	
	public String toString() {
		if(label.isEmpty()) {
			return "("+ type +";"+ weight +")";
		}
		else {
			return "("+ type +";"+ label +";"+ weight +")";
		}
	}
}
