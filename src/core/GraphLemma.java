package core;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.AsSubgraph;

import graph.Edge;
import graph.EdgeTypes;
import graph.GraphAugmented;
import graph.Node;
import graph.NodeTypes;
import requeterrezo.Filtre;

public class GraphLemma {
	private GraphAugmented graph;

	public GraphLemma(GraphAugmented graph) {
		this.graph = graph;
	}
	
	private void insertLemmaNode(Node node, String lemma) {
		AsSubgraph<Node, Edge> subNext = graph.getSubGraphNext();
		NeighborCache<Node, Edge> neigh = new NeighborCache<>(subNext);
		
		Set<Node> predecessors = neigh.predecessorsOf(node);
		Set<Node> successors = neigh.successorsOf(node);
		
		Node newNodeLemma = new Node(NodeTypes.LEMMA, lemma);
		
		graph.addVertex(newNodeLemma);
		graph.addEdge(node, newNodeLemma, new Edge(EdgeTypes.R_LEMMA));
		
		for(Node prec: predecessors) {
			graph.addEdge(prec, newNodeLemma, new Edge(EdgeTypes.R_SUCC));
		}
		for(Node next: successors) {
			graph.addEdge(newNodeLemma, next, new Edge(EdgeTypes.R_SUCC));
		}
	}
	
	public void loadLemma() {
		//noeud déjà lemmatisé avant l'appel de la fonction
		Set<Node> lemmatisedNodes = graph.getAllSourceNodesEdge(EdgeTypes.R_LEMMA);
		
		Set<Node> retainedNodes = new HashSet<>(graph.getAllNodes(EnumSet.of(NodeTypes.WORD, NodeTypes.MWE)));
		retainedNodes.removeAll(lemmatisedNodes);
		
		for(Node node: retainedNodes) {
			String word = node.getLabel();
			Set<String> lemma = graph.getRezo().getRelationsWPos(word, "r_lemma", Filtre.RejeterRelationsEntrantes);
			
			//on étudie aussi le mot sans majuscule
			String wordLow = word.toLowerCase();
			if(!word.equals(wordLow)) {
				lemma.addAll(graph.getRezo().getRelationsWPos(wordLow, "r_lemma", Filtre.RejeterRelationsEntrantes));
			}
			
			//un mot peut être lemmatisé en lui-même
			lemma.remove(word);
			lemma.remove(wordLow);
			
			if(!word.equals(wordLow)) {
				insertLemmaNode(node, wordLow);
			}
			
			for(String lem: lemma) {
				insertLemmaNode(node, lem);
			}
		}
	}
}
