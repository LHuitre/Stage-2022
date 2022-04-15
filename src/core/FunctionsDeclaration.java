package core;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.AsSubgraph;

import graph.Edge;
import graph.EdgeTypes;
import graph.GraphAugmented;
import graph.Node;

public class FunctionsDeclaration {
	public final static Set<String> functions = new HashSet<>(Arrays.asList("#connect", "#weight"));
	
	private GraphAugmented graph;
	
	public FunctionsDeclaration(GraphAugmented graph) {
		this.graph = graph;
	}

	public void connect(Collection<Node> nodesToConnect, Node newNode) {
		for(Node node: nodesToConnect) {
			AsSubgraph<Node, Edge> subNext = graph.getSubGraphNext();
			NeighborCache<Node, Edge> neigh = new NeighborCache<>(subNext);
			
			Set<Node> predecessors = neigh.predecessorsOf(node);
			Set<Node> successors = neigh.successorsOf(node);
			
			Node nodeCopy = new Node(newNode);
			graph.addVertex(nodeCopy);
			
			for(Node prec: predecessors) {
				graph.addEdge(prec, nodeCopy, new Edge(EdgeTypes.R_SUCC));
			}
			for(Node next: successors) {
				graph.addEdge(nodeCopy, next, new Edge(EdgeTypes.R_SUCC));
			}
		}
	}
	
	public void connect(Collection<Node> nodesToConnect, Node newNode, EdgeTypes newEdge) {
		for(Node node: nodesToConnect) {
			AsSubgraph<Node, Edge> subNext = graph.getSubGraphNext();
			NeighborCache<Node, Edge> neigh = new NeighborCache<>(subNext);
			
			Set<Node> predecessors = neigh.predecessorsOf(node);
			Set<Node> successors = neigh.successorsOf(node);
			
			Node nodeCopy = new Node(newNode);
			graph.addVertex(nodeCopy);
			
			for(Node prec: predecessors) {
				graph.addEdge(prec, nodeCopy, new Edge(EdgeTypes.R_SUCC));
			}
			for(Node next: successors) {
				graph.addEdge(nodeCopy, next, new Edge(EdgeTypes.R_SUCC));
			}
			
			graph.addEdge(node, nodeCopy, new Edge(newEdge));
		}
	}
	
	public void weight(Collection<Node> nodes, int weight) {
		for(Node node: nodes) {
			node.setWeight(weight);
			for(Edge edge: graph.edgesOf(node)) {
				edge.setWeight(weight);
			}
		}
	}
	
	public void cover(String varLeft, String varRight, Node newNode, HashSet<HashMap<String, Node>> tabAssociatedVarNodes) {
		
	}
}
