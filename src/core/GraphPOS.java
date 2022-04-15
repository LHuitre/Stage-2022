package core;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.AsSubgraph;

import graph.Edge;
import graph.EdgeTypes;
import graph.GraphAugmented;
import graph.Node;
import graph.NodeTypes;
import requeterrezo.Filtre;

public class GraphPOS {
	private static List<String> listPOS = 
			Arrays.asList("Adj:", "Adv:", "Conj:", "Det:", "Nom:", "Pre:", //"Adj:Card",
					"Gender:Fem", "Gender:Mas", "Number:Sing", "Number:Plur",
					"Pro:Interro", "Pro:Pers:COD", "Pro:Pers:COI", "Pro:Pers:SUJ", "Pro:Rel",
					"Ver:Conjug", "Ver:Etat", "Ver:Inf", "Ver:PPas", "Ver:PPre",
					"VerbalTime:Future", "VerbalTime:Past", "VerbalTime:Present");
	
	/*private static List<String> listPOS = 
			Arrays.asList("Nom:", "Adj:", "Adv:", "Pre:", "Gender:Fem", "Gender:Mas", "Number:Sing", "Number:Plur");*/
	
	private static final Set<String> POS = new HashSet<String>(listPOS);
	
	private GraphAugmented graph;

	
	public GraphPOS(GraphAugmented graph) {
			this.graph = graph;
		}
	
	private void insertPOSNode(Node node, Set<String> pos) {
		AsSubgraph<Node, Edge> subNext = graph.getSubGraphNext();
		NeighborCache<Node, Edge> neigh = new NeighborCache<>(subNext);
		
		Set<Node> predecessors = neigh.predecessorsOf(node);
		Set<Node> successors = neigh.successorsOf(node);
		
		for(String p: pos) {
			Node newNodePOS = new Node(NodeTypes.POS, p);
			
			graph.addVertex(newNodePOS);
			graph.addEdge(node, newNodePOS, new Edge(EdgeTypes.R_POS));
			
			for(Node prec: predecessors) {
				graph.addEdge(prec, newNodePOS, new Edge(EdgeTypes.R_SUCC));
			}
			for(Node next: successors) {
				graph.addEdge(newNodePOS, next, new Edge(EdgeTypes.R_SUCC));
			}
		}
	}
	
	public void loadPOS() {
		Set<Node> posNodes = graph.getAllSourceNodesEdge(EdgeTypes.R_POS);
	
		Set<Node> retainedNodes = new HashSet<>(graph.getAllNodes(EnumSet.of(NodeTypes.WORD, NodeTypes.LEMMA, NodeTypes.MWE)));
		retainedNodes.removeAll(posNodes);
	
		for(Node node: retainedNodes) {
			String word = node.getLabel();
			Set<String> pos = graph.getRezo().getRelationsWPos(word, "r_pos", Filtre.RejeterRelationsEntrantes);
			
			pos.retainAll(POS);
			
			insertPOSNode(node, pos);
		}
	}
}
