package FFanalysis;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.AsSubgraph;

import graph.Edge;
import graph.EdgeTypes;
import graph.GraphAugmented;
import graph.Node;
import graph.NodeTypes;

public class ExtractRelations {
	Set<Triplet> extractedRelations;
	HashMap<String, Set<String>> inMWE;
	HashMap<String, Set<String>> hasLemma;
	
	public ExtractRelations(GraphAugmented graph) {
		Set<Edge> edges = graph.getAllEdges(EnumSet.of(EdgeTypes.R_AGENT, EdgeTypes.R_AGENT_1, EdgeTypes.R_PATIENT, EdgeTypes.R_PATIENT_1, EdgeTypes.R_CARAC, EdgeTypes.R_ISA));
		
		for(Edge e: edges) {
			
		}
	}

	public Set<Triplet> getExtractedRelations() {
		return extractedRelations;
	}

	public HashMap<String, Set<String>> getInMWE() {
		return inMWE;
	}

	public HashMap<String, Set<String>> getHasLemma() {
		return hasLemma;
	}
}
