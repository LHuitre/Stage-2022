package core;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.AsSubgraph;

import graph.Edge;
import graph.EdgeTypes;
import graph.GraphAugmented;
import graph.Node;
import graph.NodeTypes;
import utilities.TrieHash;

public class GraphMWE {
	private GraphAugmented graph;
	private TrieHash trieMWE;

	public GraphMWE(GraphAugmented graph, TrieHash trieMWE) {
		this.graph = graph;
		this.trieMWE = trieMWE;
	}

	private String listNodeToString(List<Node> listNodes) {
		String output = "";
		Iterator<Node> iter = listNodes.iterator();
		
		if(iter.hasNext()) output += iter.next().getLabel();
		
		while(iter.hasNext()) output += " " + iter.next().getLabel();
		
		output = output.replace("' ", "'");
		
		return output;
	}
	
	private void insertMWENode(List<Node> nodeAcc) {
		AsSubgraph<Node, Edge> subNext = graph.getSubGraphNext();
		NeighborCache<Node, Edge> neigh = new NeighborCache<>(subNext);
		Set<Node> predecessors = neigh.predecessorsOf(nodeAcc.get(0));
		Set<Node> successors = neigh.successorsOf(nodeAcc.get(nodeAcc.size()-1));
		
		Node newNodeMWE = new Node(NodeTypes.MWE, listNodeToString(nodeAcc));
		
		graph.addVertex(newNodeMWE);

		// Si TOUS les noeuds de nodeAcc sont dans le MÊME MWE, alors on crée le nouvel arc
		// Pour cela on va comparer les noeuds en commun avec le premier noeud (nodeAcc.get(0))
		Set<Node> englobingMWE = new HashSet<>(); // les mwe englobant
		
		Set<Edge> outgoingEdges = new HashSet<>(graph.outgoingEdgesOf(nodeAcc.get(0)));
		outgoingEdges.retainAll(graph.getDicoEdgeTypes().get(EdgeTypes.R_ISIN));
		for(Edge e: outgoingEdges) {
			englobingMWE.add(graph.getEdgeTarget(e));
		}
		for(Node node: nodeAcc) {
			outgoingEdges = new HashSet<>(graph.outgoingEdgesOf(node));
			outgoingEdges.retainAll(graph.getDicoEdgeTypes().get(EdgeTypes.R_ISIN));
			Set<Node> nodeIsIn = new HashSet<>();
			for(Edge e: outgoingEdges) {
				nodeIsIn.add(graph.getEdgeTarget(e));
			}
			englobingMWE.retainAll(nodeIsIn);
			
			// On ajoute au passage la relation ISIN correspondant au constituant du MWE
			graph.addEdge(node, newNodeMWE, new Edge(EdgeTypes.R_ISIN));
		}
		
		for(Node node: englobingMWE) {
			graph.addEdge(newNodeMWE, node, new Edge(EdgeTypes.R_ISIN));
		}
		
		for(Node prec: predecessors) {
			graph.addEdge(prec, newNodeMWE, new Edge(EdgeTypes.R_SUCC));
		}
		for(Node next: successors) {
			graph.addEdge(newNodeMWE, next, new Edge(EdgeTypes.R_SUCC));
		}
	}
	
	
	/**
	 * Indique pour un ensemble de noeuds si un multi-mot du graphe englobe déjà tous ces noeuds.
	 * @param listNodes
	 * @return true si le multi-mot existe déjà dans le graphe
	 */
	private boolean MWEExists(List<Node> listNodes) {
		// l'ensemble des noeuds mwe communs aux noeuds de listNodes
		// comme aucun noeud n'est encore considéré, tous les noeuds techniquement sont en communs
		Set<Node> commonMWE = new HashSet<>(graph.getDicoNodeTypes().get(NodeTypes.MWE));
		if(commonMWE.isEmpty()) {
			return false; //aucun multi-mot n'est présent dans le graphe
		}
		
		Set<Edge> edgesISIN = graph.getDicoEdgeTypes().get(EdgeTypes.R_ISIN);

		
		for(Node node: listNodes) {
			Set<Edge> outgoingEdges = new HashSet<>(graph.outgoingEdgesOf(node));
			outgoingEdges.retainAll(edgesISIN);
			if(outgoingEdges.isEmpty()) { 
				//le mot contenu dans le mwe n'appartient à aucun mwe précédemment ajouté
				//ce mwe est donc nouveau
				return false;
			}
			
			// on recherche les mwe contenant le mot du node
			Set<Node> nodeInMWE = new HashSet<>();
			for(Edge e: outgoingEdges) {
				nodeInMWE.add(graph.getEdgeTarget(e));
			}
			
			// on ne garde que les noeuds qui sont déjà connus des noeuds précédents
			// knownMWE va se vider progressivement, s'il reste un élément à la fin
			// c'est que tous les noeuds de listNodes appartiennent à un même mwe qu'il ne faudra pas étiqueter
			commonMWE.retainAll(nodeInMWE);
			
			if(commonMWE.isEmpty()) {
				return false;
			}
		}
		
		//knownMWE n'est pas vide, il y a donc un mwe en commun
		for(Node node: commonMWE) {
			if(node.getLabel().equals(listNodeToString(listNodes))) {
				return true;
			}
		}
		
		return false;
	}
	
	private void findMWEfromNode(List<Node> accNode, Node currNode) {
		if(!currNode.getType().equals(NodeTypes.END)) {
			accNode.add(currNode);
			
			Entry<Boolean, Boolean> mweRes = trieMWE.search(listNodeToString(accNode));
			if(accNode.size() > 1 // un noeud mwe seul (accNode.size() == 1) sera dupliqué si on ne fait pas cette vérification
					&& mweRes.getKey() // accNode existe bien en tant que mwe
					&& !MWEExists(accNode))// on vérifie que la liste de node ne soit pas associé à un mwe déjà ajouté au graphe	
			{
				insertMWENode(accNode);
			}
			
			// il existe des mwe potentiellement plus longs
			if(mweRes.getValue()) {
				AsSubgraph<Node, Edge> subNext = graph.getSubGraphNext();
				NeighborCache<Node, Edge> neigh = new NeighborCache<>(subNext);
				for(Node succ: neigh.successorsOf(currNode)) {
					findMWEfromNode(accNode, succ);
					accNode.remove(succ);
				}
			}
		}
	}
	
	public void findMWE() {
		Set<Node> subNodes = graph.getAllNodes(EnumSet.of(NodeTypes.WORD, NodeTypes.MWE, NodeTypes.LEMMA, NodeTypes.POS));
		//Set<Node> sourceNodeISIN = graph.getAllSourceNodesEdge(EdgeTypes.R_ISIN);
		//subNodes.removeAll(sourceNodeISIN); //évite d'explorer les noeuds déjà exploré pour l'étiquetage de mwe
		
		Set<Edge> subEdges = graph.getAllEdges(EnumSet.of(EdgeTypes.R_SUCC));
		
		AsSubgraph<Node, Edge> subGraph = graph.getSubGraph(subNodes, subEdges);
		
		for(Node n: subGraph.vertexSet()) {
			findMWEfromNode(new ArrayList<Node>(), n);
		}
	}
}
