package core;

import java.util.List;

import graph.DicoGraph;
import graph.Edge;
import graph.EdgeTypes;
import graph.GraphAugmented;
import graph.Node;
import graph.NodeTypes;

public class GraphInitialisation {	
	private GraphAugmented graph;

	public GraphInitialisation(GraphAugmented graph) {
		this.graph = graph;
	}

	/** 
	 * Crée un noeud dont le type ne peut pas apparaître plusieurs fois dans le graphe.
	 * @param type : normalement :START: ou :END:.
	 * @throws Exception si on essaie de créer un noeud dont le type est déjà présent dans le graphe.
	 */
	private void createUniqueNode(NodeTypes type) throws Exception {
		DicoGraph<NodeTypes, Node> dicoNodeTypes = graph.getDicoNodeTypes();
		if(dicoNodeTypes.containsKey(type) 
				&& dicoNodeTypes.get(type) != null 
				&& dicoNodeTypes.get(type).size() > 0) {
			throw(new Exception("Impossible d'ajouter un noeud de type "+ type +" car il en existe déjà un."));
		}
		
		Node newNode = new Node(type);
		graph.addVertex(newNode);
	}
	
	/**
	 * Crée le noeud :START: du graphe.
	 * @throws Exception si le noeud existe déjà
	 */
	private void createStartNode() throws Exception {
		createUniqueNode(NodeTypes.START);
	}
	
	/**
	 * Crée le noeud :END: du graphe.
	 * @throws Exception si le noeud existe déjà
	 */
	private void createEndNode() throws Exception {
		createUniqueNode(NodeTypes.END);
	}
	
	/**
	 * GraphInitialisation du graphe
	 * @throws Exception
	 */
	public void init(List<String> words) throws Exception {
		createStartNode();
		createEndNode();
		graph.addEdge(graph.getStart(), graph.getEnd(), new Edge(EdgeTypes.R_SUCC));
		
		Node precedentNode = graph.getStart();
		for(String word: words) {
			Node newNode = new Node(NodeTypes.WORD, word);
			graph.insertNode(newNode, precedentNode, graph.getEnd(), EdgeTypes.R_SUCC, true);
			precedentNode = newNode;
		}
	}
}
