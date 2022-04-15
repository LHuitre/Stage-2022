package graph;

public class Node implements GraphElement{
	private NodeTypes type;
	private String label;
	private int weight;
	private Object data;
	
	public Node(NodeTypes type, String label, int weight, Object data) {
		this.type = type;
		this.label = label;
		this.weight = weight;
		this.data = data;
	}
	
	public Node(NodeTypes type, String label, int weight) {
		this.type = type;
		this.label = label;
		this.weight = weight;
		this.data = null;
	}

	public Node(NodeTypes type, String label) {
		this.type = type;
		this.label = label;
		this.weight = 1;
		this.data = null;
	}
	
	public Node(NodeTypes type) {
		this.type = type;
		this.label = "";
		this.weight = 1;
		this.data = null;
	}

	public Node(Node node) {
		this.type = node.getType();
		this.label = node.getLabel();
		this.weight = node.getWeight();
		this.data = node.getData();
	}
	
	public NodeTypes getType() {
		return type;
	}
	
	public String getLabel() {
		return label;
	}
	
	public int getWeight() {
		return weight;
	}

	public Object getData() {
		return data;
	}

	
	public void setWeight(int weight) {
		this.weight = weight;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String toString() {
		String output = "";
		if(label.isEmpty()) {
			output = type.toString();
		}
		else {
			output = label +" ["+ type +"]";
		}
		
		if(weight <= 0) {
			output += "(" + weight + ")";
		}
		return output;
	}
}
