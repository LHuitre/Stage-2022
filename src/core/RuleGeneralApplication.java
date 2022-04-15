package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import graph.Edge;
import graph.EdgeTypes;
import graph.GraphAugmented;
import graph.Node;
import graph.NodeTypes;
import requeterrezo.Filtre;
import rulesGeneral.Equality;
import rulesGeneral.Function;
import rulesGeneral.RegexRule;
import rulesGeneral.Rule;
import rulesGeneral.Triplet;

public class RuleGeneralApplication {
	private GraphAugmented graph;
	private Rule rule;

	private HashMap<String, Set<Node>> candidates;
	private HashMap<List<String>, Set<List<Node>>> nupletRelation;
	private HashMap<String, Set<String>> externalLabels;
	
	public RuleGeneralApplication(GraphAugmented graph, Rule rule) {
		this.graph = graph;
		this.rule = rule;
		
		nupletRelation = new HashMap<>();
		
		externalLabels = new HashMap<>();
		for(Triplet triplet: rule.getTripletBody()) {
			if(triplet.getPredicat().matches("^!?e_.*") 
					&& (RegexRule.isSimpleVar(triplet.getObjet()) || RegexRule.isCompilVar(triplet.getObjet()))) {
				for(Equality equality: rule.getEqualityBody()) {
					if(equality.getSujet().equals(triplet.getObjet())){
						externalLabels.put(triplet.getObjet(), equality.getObjet());
						break;
					}
				}
			}
		}
		
		for(Triplet triplet: rule.getTripletBody()) {
			externalLabels.remove(triplet.getSujet());
			if(triplet.getPredicat().matches("^!?r_.*")) {
				externalLabels.remove(triplet.getObjet());
			}
		}
		
		for(Function function: rule.getFunctionBody()) {
			for(String args: function.getArgs()) {
				externalLabels.remove(args);
			}
		}
		
		candidates = new HashMap<>();
		for(String var: rule.getVariablesBody()) {
			if(!externalLabels.containsKey(var)) {
				candidates.put(var, graph.vertexSet());
			}
		}
		
	}

	public HashMap<String, Set<Node>> getCandidates() {
		return candidates;
	}

	public HashMap<String, Set<String>> getExternalLabels() {
		return externalLabels;
	}

	/**
	 * Cherche les noeuds candidats restreints par le typage
	 * @return false si une variable n'a plus aucun candidat
	 */
	private boolean typageBody() {
		HashMap<String, String> typeDeclaration = rule.getTypeDeclaration();
		for(String extVar: externalLabels.keySet()) {
			typeDeclaration.remove(extVar);
		}
		
		for(String var: typeDeclaration.keySet()) {
			// Comme on peut déclarer une variable mais ne pas s'en servir, on vérifie qu'elle est bien présente dans les candidats
			if(candidates.containsKey(var)) {
				String typeStr = rule.getTypeDeclaration().get(var);
				typeStr.replaceAll("^e_", "r_");
				
				if(typeStr.equals("ALLWORDS")) {
					Set<Node> tempCandidates = new HashSet<>(candidates.get(var));
					tempCandidates.retainAll(graph.getAllNodes(EnumSet.of(NodeTypes.WORD, NodeTypes.MWE, NodeTypes.LEMMA)));
					candidates.put(var, tempCandidates);
				}
				else {
					NodeTypes type = NodeTypes.valueOf(typeStr);
					Set<Node> tempCandidates = new HashSet<>(candidates.get(var));
					tempCandidates.retainAll(graph.getAllNodes(type));
					candidates.put(var, tempCandidates);
				}
				
				if(RegexRule.isSimpleVar(var) && candidates.get(var).isEmpty()) { return false; }
				//if(rule.getVariablesHead().contains(var) && candidates.get(var).isEmpty()) { return false; }
			}
		}
		
		return true;
	}
	
	/**
	 * Cherche les noeuds candidats restreints par une égalité (disjonction de constantes)
	 * @return false si une variable n'a plus aucun candidat
	 */
	private boolean equalityBody() {
		HashSet<Equality> equalities = rule.getEqualityBody();
		
		for(Equality eq: equalities) {
			HashSet<Node> retainedNodes = new HashSet<>();
			String var = eq.getSujet();
			
			Set<String> values = new HashSet<>();
			Set<String> valuesJoker = new HashSet<>();
			for(String v: eq.getObjet()) {
				if(v.endsWith("*")) {
					valuesJoker.add(v);
				}
				else {
					values.add(v);
				}
			}
			
			if(candidates.containsKey(var)) {
				for(Node node: candidates.get(var)) {
					boolean isJoker = false;
					for(String joker: valuesJoker) {
						joker = joker.substring(0, joker.length()-1); //suppression du * final
						if(node.getLabel().startsWith(joker)) {
							retainedNodes.add(node);
							isJoker = true;
						}
					}
					
					if(!isJoker && values.contains(node.getLabel())) {
						retainedNodes.add(node);
					}
				}
				
				
				
				Set<Node> tempCandidates = new HashSet<>(candidates.get(var));
				if(eq.isEqual()) {
					tempCandidates.retainAll(retainedNodes);
				}
				else {
					tempCandidates.removeAll(retainedNodes);
				}
				//tempCandidates.retainAll(retainedNodes);
				candidates.put(var, tempCandidates);
				
				if(RegexRule.isSimpleVar(var) && candidates.get(var).isEmpty()) { return false; }
				//if(rule.getVariablesHead().contains(var) && candidates.get(var).isEmpty()) { return false; }
			}
		}
		
		return true;
	}
	
	/**
	 * Cherche les noeuds candidats restreints par un triplet ayant un prédicat de la forme r_xxx
	 * @return false si une variable n'a plus aucun candidat
	 */
	private boolean internalRelationBody() {
		// On ne retient que les relations internes de la forme r_xxx
		HashSet<Triplet> internalTripletsBody = new HashSet<>();
		for(Triplet triplet: rule.getTripletBody()) {
			if(triplet.getPredicat().matches("^!?r_.*")) {
				internalTripletsBody.add(triplet);
			}
		}
		
		for(Triplet triplet: internalTripletsBody) {
			HashSet<Node> retainedNodesSujet = new HashSet<>();
			HashSet<Node> retainedNodesObjet = new HashSet<>();
			
			// Pour chaque sujet de chaque triplet, on va étudier le prédicat et l'objet
			for(Node nodeSubj: candidates.get(triplet.getSujet())) {
				// Restriction de poids spéciale MIN / MAX
				if(!triplet.getSpecialWeight().isEmpty()) {
					
					// On doit donc étudier toutes les relations pour après retenir ou pas le MIN / MAX
					Set<Node> tempNodesObjet = new HashSet<>();
					for(Node nodeObj: candidates.get(triplet.getObjet())) {
						EdgeTypes edgeType = EdgeTypes.valueOf(triplet.getPredicat().toUpperCase());
						Edge graphEdge = null;
						for(Edge e: graph.getAllEdges(nodeSubj, nodeObj)) {
							if(e.getType().equals(edgeType)) {
								graphEdge = e;
								break;
							}
						}
						if(graphEdge != null && graphEdge.getType().equals(edgeType)) {
							tempNodesObjet.add(nodeObj);
						}
					}
					
					// Recherche du MIN et du MAX
					int minWeight = Integer.MAX_VALUE;
					int maxWeight = Integer.MIN_VALUE;
					for(Node node: tempNodesObjet) {
						if(node.getWeight() < minWeight) {
							minWeight = node.getWeight();
						}
						if(node.getWeight() > maxWeight) {
							maxWeight = node.getWeight();
						}
					}
					
					// Restriction des noeuds en fonction de la contrainte MIN / MAX
					String specialWeight = triplet.getSpecialWeight();
					for(Node nodeObj: tempNodesObjet) {
						if((specialWeight.equals("<MAX") && nodeObj.getWeight() < maxWeight)
								|| (specialWeight.equals("==MAX") && nodeObj.getWeight() == maxWeight)
								|| (specialWeight.equals("<MIN") && nodeObj.getWeight() < minWeight)
								|| (specialWeight.equals("==MIN") && nodeObj.getWeight() == minWeight)) {
							retainedNodesSujet.add(nodeSubj);
							retainedNodesObjet.add(nodeObj);
							
							if(RegexRule.isSimpleVar(triplet.getSujet()) && RegexRule.isSimpleVar(triplet.getObjet())) {
								List<String> pairVar = new ArrayList<>(Arrays.asList(triplet.getSujet(), triplet.getObjet()));
								List<Node> pairNode = new ArrayList<>(Arrays.asList(nodeSubj, nodeObj));
								nupletRelation.putIfAbsent(pairVar, new HashSet<>());
								nupletRelation.get(pairVar).add(pairNode);
							}
						}
					}
				}
				
				// Restriction de poids avec un entier, utilisation get[Min/Max]WeightPredicat
				else {
					for(Node nodeObj: candidates.get(triplet.getObjet())) {
						
						EdgeTypes edgeType = EdgeTypes.valueOf(triplet.getPredicat().toUpperCase());
						Edge graphEdge = null;
						for(Edge e: graph.getAllEdges(nodeSubj, nodeObj)) {
							if(e.getType().equals(edgeType)) {
								graphEdge = e;
								break;
							}
						}
						
						if(graphEdge != null 
								&& graphEdge.getWeight() >= triplet.getMinWeightPredicat()
								&& graphEdge.getWeight() <= triplet.getMaxWeightPredicat()) {
							retainedNodesSujet.add(nodeSubj);
							retainedNodesObjet.add(nodeObj);
							
							if(RegexRule.isSimpleVar(triplet.getSujet()) && RegexRule.isSimpleVar(triplet.getObjet())) {
								List<String> pairVar = new ArrayList<>(Arrays.asList(triplet.getSujet(), triplet.getObjet()));
								List<Node> pairNode = new ArrayList<>(Arrays.asList(nodeSubj, nodeObj));
								nupletRelation.putIfAbsent(pairVar, new HashSet<>());
								nupletRelation.get(pairVar).add(pairNode);
							
							}
						}
					}
				}
			}
			
			Set<Node> tempCandidatesSujet = new HashSet<>(candidates.get(triplet.getSujet()));
			//System.out.println(triplet);
			if(triplet.getTruthValue()) {
				tempCandidatesSujet.retainAll(retainedNodesSujet);
			}
			else {
				tempCandidatesSujet.removeAll(retainedNodesSujet);
			}
			candidates.put(triplet.getSujet(), tempCandidatesSujet);
			
			if(RegexRule.isSimpleVar(triplet.getSujet()) && candidates.get(triplet.getSujet()).isEmpty()) { return false; }
			//if(rule.getVariablesHead().contains(triplet.getSujet()) && candidates.get(triplet.getSujet()).isEmpty()) { return false; }
			
			Set<Node> tempCandidatesObjet = new HashSet<>(candidates.get(triplet.getObjet()));
			tempCandidatesObjet.retainAll(retainedNodesObjet);
			candidates.put(triplet.getObjet(), tempCandidatesObjet);
			
			if(RegexRule.isSimpleVar(triplet.getObjet()) && candidates.get(triplet.getObjet()).isEmpty()) { return false; }
			//if(rule.getVariablesHead().contains(triplet.getObjet()) && candidates.get(triplet.getObjet()).isEmpty()) { return false; }
		}
			
		return true;
	}
	
	/**
	 * Cherche les noeuds candidats restreints par un triplet ayant un prédicat de la forme e_xxx<br>
	 * C'est un triplet appelant une relation dans JdM (interrogation de base de données plus lente)
	 * @return false si une variable n'a plus aucun candidat
	 */
	private boolean externalRelationBody() {
		HashSet<Triplet> externalTripletsBody = new HashSet<>();
		for(Triplet triplet: rule.getTripletBody()) {
			if(triplet.getPredicat().matches("^!?e_.*")) {
				externalTripletsBody.add(triplet);
			}
		}
		
		for(Triplet triplet: externalTripletsBody) {
			HashSet<Node> retainedNodesSujet = new HashSet<>();
			String correctedPredicat = triplet.getPredicat().replace("e_", "r_");
			
			// Pour chaque sujet de chaque triplet, on va étudier le prédicat et l'objet
			for(Node nodeSubj: candidates.get(triplet.getSujet())) {
				
				// Récupération des valeurs à trouver dans JdM
				Set<String> tempLabelsObjet = new HashSet<>();
				if(externalLabels.containsKey(triplet.getObjet())) {
					tempLabelsObjet.addAll(externalLabels.get(triplet.getObjet()));
				}
				else {
					for(Node nodeObj: candidates.get(triplet.getObjet())) {
						tempLabelsObjet.add(nodeObj.getLabel());
					}
				}
				
				// Restriction de poids spéciale MIN / MAX
				if(!triplet.getSpecialWeight().isEmpty()) {
					List<Entry<String, Integer>> tempResult = graph.getRezo().getSortedRelationsWithWeight(
							nodeSubj.getLabel(), correctedPredicat, Filtre.RejeterRelationsEntrantes, Integer.MIN_VALUE, Integer.MAX_VALUE);
					int currMax = tempResult.get(0).getValue();
					int currMin = tempResult.get(tempResult.size()).getValue();
					
					// Restriction des noeuds en fonction de la contrainte MIN / MAX
					String specialWeight = triplet.getSpecialWeight();
					for(Entry<String, Integer> entry: tempResult) {
						if(tempLabelsObjet.contains(entry.getKey()) 
								&& ((specialWeight.equals("<MAX") && entry.getValue() < currMax)
										|| (specialWeight.equals("==MAX") && entry.getValue() == currMax)
										|| (specialWeight.equals("<MIN") && entry.getValue() < currMin)
										|| (specialWeight.equals("==MIN") && entry.getValue() == currMin))) {
							retainedNodesSujet.add(nodeSubj);
							break;
						}
					}
				}
				
				// Restriction de poids avec un entier, utilisation get[Min/Max]WeightPredicat
				else {
					List<Entry<String, Integer>> tempResult = graph.getRezo().getSortedRelationsWithWeight(
							nodeSubj.getLabel(), correctedPredicat, Filtre.RejeterRelationsEntrantes, 
							triplet.getMinWeightPredicat(), triplet.getMaxWeightPredicat());
					for(Entry<String, Integer> entry: tempResult) {
						if(tempLabelsObjet.contains(entry.getKey())) {
							retainedNodesSujet.add(nodeSubj);
							break;
						}
					}
				}
			}
			
			Set<Node> tempCandidatesSujet = new HashSet<>(candidates.get(triplet.getSujet()));
			if(triplet.getTruthValue()) {
				tempCandidatesSujet.retainAll(retainedNodesSujet);
			}
			else {
				tempCandidatesSujet.removeAll(retainedNodesSujet);
			}
			candidates.put(triplet.getSujet(), tempCandidatesSujet);
			
			if(RegexRule.isSimpleVar(triplet.getSujet()) && candidates.get(triplet.getSujet()).isEmpty()) { return false; }
			//if(rule.getVariablesHead().contains(triplet.getSujet()) && candidates.get(triplet.getSujet()).isEmpty()) { return false; }
		}
		
		return true;
	}
	
	
	private HashMap<String, HashMap<List<String>, Set<List<Node>>>> cleanNUpletRel() {
		HashSet<List<String>> keySet = new HashSet<>(nupletRelation.keySet());
		for(List<String> pairVar: keySet) {
			HashSet<List<Node>> valSet = new HashSet<>(nupletRelation.get(pairVar));
			for(List<Node> pairNode: valSet) {
				if(!candidates.get(pairVar.get(0)).contains(pairNode.get(0))
						|| !candidates.get(pairVar.get(1)).contains(pairNode.get(1))) {
					nupletRelation.get(pairVar).remove(pairNode);
				}
			}
		}
		
		HashMap<String, HashMap<List<String>, Set<List<Node>>>> output = new HashMap<>();
		for(List<String> pairVar: nupletRelation.keySet()) {
			output.putIfAbsent(pairVar.get(0), new HashMap<>());
			output.get(pairVar.get(0)).put(pairVar, nupletRelation.get(pairVar));
			
			output.putIfAbsent(pairVar.get(1), new HashMap<>());
			output.get(pairVar.get(1)).put(pairVar, nupletRelation.get(pairVar));
		}

		return output;
	}
	
	private HashSet<HashMap<String, Node>> internalRelationHead() {
		HashMap<String, HashMap<List<String>, Set<List<Node>>>> mapNUplet = cleanNUpletRel();
		
		HashSet<HashMap<String, Node>> tableau = new HashSet<>();
		
		// On fait le produit cartésien de toutes les candidats
		if(mapNUplet.isEmpty()) {
			HashSet<String> toExplore = new HashSet<>(rule.getVariablesHead());
			String firstVar = toExplore.iterator().next();
			toExplore.remove(firstVar);
			for(Node val: candidates.get(firstVar)) {
				HashMap<String, Node> newLine = new HashMap<>();
				newLine.put(firstVar, val);
				tableau.add(newLine);
			}
			
			for(String var: toExplore) {
				Set<HashMap<String, Node>> toAddTableau = new HashSet<>();
				Set<HashMap<String, Node>> toRemove = new HashSet<>();
				for(HashMap<String, Node> line: tableau) {
					toRemove.add(line);
					for(Node val: candidates.get(var)) {
						HashMap<String, Node> newLine = new HashMap<>(line);
						newLine.put(var, val);
						toAddTableau.add(newLine);
					}
				}
				tableau.removeAll(toRemove);
				tableau.addAll(toAddTableau);
			}
		}
		
		else {
			String currentVar = nupletRelation.keySet().iterator().next().get(0);
			Set<String> exploredVar = new HashSet<>();
			Queue<String> varToExplore = new LinkedList<>();
			for(List<String> pairVar: mapNUplet.get(currentVar).keySet()) {
				int idxCurrVar;
				int idxOtherVar;
				if(pairVar.get(0).equals(currentVar)) {
					idxCurrVar = 0;
					idxOtherVar = 1;
				}
				else {
					idxCurrVar = 1;
					idxOtherVar = 0;
				}
				
				varToExplore.add(pairVar.get(idxOtherVar));
				
				for(List<Node> pairNode: mapNUplet.get(currentVar).get(pairVar)) {
					Node node = pairNode.get(idxCurrVar);
					
					HashMap<String, Node> newLine = new HashMap<>();
					newLine.put(currentVar, node);
					
					tableau.add(newLine);
				}
			}
			
			while(!varToExplore.isEmpty()) {
				currentVar = varToExplore.remove();
				exploredVar.add(currentVar);
				
				for(List<String> pairVar: mapNUplet.get(currentVar).keySet()) {
					int idxCurrVar;
					int idxOtherVar;
					if(pairVar.get(0).equals(currentVar)) {
						idxCurrVar = 0;
						idxOtherVar = 1;
					}
					else {
						idxCurrVar = 1;
						idxOtherVar = 0;
					}
					
					if(!exploredVar.contains(pairVar.get(idxOtherVar))) {
						varToExplore.add(pairVar.get(idxOtherVar));
					}
					
					Set<HashMap<String, Node>> toAddTableau = new HashSet<>();
					Set<HashMap<String, Node>> toRemove = new HashSet<>();
					for(List<Node> pairNode: mapNUplet.get(currentVar).get(pairVar)) { //paires de Nodes correspondant à (X, $b)
						for(HashMap<String, Node> line: tableau) {
							if(line.containsKey(pairVar.get(idxOtherVar)) 
									&& !line.containsKey(pairVar.get(idxCurrVar))
									&& line.get(pairVar.get(idxOtherVar)).equals(pairNode.get(idxOtherVar))) {
								toRemove.add(line);
								HashMap<String, Node> newLine = new HashMap<>(line);
								newLine.put(currentVar, pairNode.get(idxCurrVar));
								toAddTableau.add(newLine);
							}
						}
					}
					tableau.removeAll(toRemove);
					tableau.addAll(toAddTableau);
				}
			}
		}
		
		//System.out.println(tableau);
		
		for(Triplet triplet: rule.getTripletHead()) {
			if(RegexRule.isSimpleVar(triplet.getSujet()) && RegexRule.isSimpleVar(triplet.getObjet())) {
				for(HashMap<String, Node> lineTab: tableau) {
					Node nodeSujet = lineTab.get(triplet.getSujet());
					Node nodeObjet = lineTab.get(triplet.getObjet());
					Edge edgeSujObj = graph.getEdge(nodeSujet, nodeObjet);
					
					
					if(edgeSujObj == null) {
						Edge newEdge;
						if(triplet.getMinWeightPredicat() == triplet.getMaxWeightPredicat()) { //poids spécifié
							newEdge = new Edge(EdgeTypes.valueOf(triplet.getPredicat().toUpperCase()), triplet.getMinWeightPredicat());
						}
						else if(!triplet.getTruthValue()) {
							newEdge = new Edge(EdgeTypes.valueOf(triplet.getPredicat().toUpperCase()), -10);
						}
						else {
							newEdge = new Edge(EdgeTypes.valueOf(triplet.getPredicat().toUpperCase()));
						}
						graph.addEdge(nodeSujet, nodeObjet, newEdge);
					}
					else if(edgeSujObj.getType().equals(EdgeTypes.valueOf(triplet.getPredicat().toUpperCase()))) { //l'arrête existe déjà dans le graphe
						if(triplet.getMinWeightPredicat() == triplet.getMaxWeightPredicat()) { //poids spécifié
							edgeSujObj.setWeight(triplet.getMinWeightPredicat());
						}
						else if(!triplet.getTruthValue()) {
							edgeSujObj.setWeight(-10);
						}
						//else: on ne fait rien, l'arc existe déjà
					}
					else {
						Edge newEdge;
						if(triplet.getMinWeightPredicat() == triplet.getMaxWeightPredicat()) { //poids spécifié
							newEdge = new Edge(EdgeTypes.valueOf(triplet.getPredicat().toUpperCase()), triplet.getMinWeightPredicat());
						}
						else if(!triplet.getTruthValue()) {
							newEdge = new Edge(EdgeTypes.valueOf(triplet.getPredicat().toUpperCase()), -10);
						}
						else {
							newEdge = new Edge(EdgeTypes.valueOf(triplet.getPredicat().toUpperCase()));
						}
						graph.addEdge(nodeSujet, nodeObjet, newEdge);
					}
				}
			}
		}
		
		return tableau;
	}
	
	private void newNode() {
		for(Triplet triplet: rule.getTripletHead()) {
			if(RegexRule.isNodeName(triplet.getSujet()) && RegexRule.isNodeName(triplet.getObjet())) {
				Node sujet = new Node(rule.getNodeDeclaration().get(triplet.getSujet()));
				graph.addVertex(sujet);
				Node objet = new Node(rule.getNodeDeclaration().get(triplet.getObjet()));
				graph.addVertex(objet);
				graph.addEdge(sujet, objet, new Edge(EdgeTypes.valueOf(triplet.getPredicat().toUpperCase())));
				
			}
			else if(RegexRule.isNodeName(triplet.getSujet()) && RegexRule.isSimpleVar(triplet.getObjet())) {
				for(Node nodeVar: candidates.get(triplet.getObjet())) {
					Node sujet = new Node(rule.getNodeDeclaration().get(triplet.getSujet()));
					graph.addVertex(sujet);
					graph.addEdge(sujet, nodeVar, new Edge(EdgeTypes.valueOf(triplet.getPredicat().toUpperCase())));
				}
			}
			else if(RegexRule.isSimpleVar(triplet.getSujet()) && RegexRule.isNodeName(triplet.getObjet())) {
				for(Node nodeVar: candidates.get(triplet.getSujet())) {
					Node objet = new Node(rule.getNodeDeclaration().get(triplet.getObjet()));
					graph.addVertex(objet);
					graph.addEdge(nodeVar, objet, new Edge(EdgeTypes.valueOf(triplet.getPredicat().toUpperCase())));
				}
			}
		}
	}
	
	/**
	 * Cherche les noeuds candidats restreints par une fonction
	 * @return
	 */
	private void functionsHead() {
		for(Function function: rule.getFunctionHead()) {
			FunctionsDeclaration declaredFunctions = new FunctionsDeclaration(graph);
			switch(function.getName()) {
			case "#connect":
				Collection<Node> nodesToConnect = candidates.get(function.getArgs().get(0));
				Node newNode = rule.getNodeDeclaration().get(function.getArgs().get(1));
				if(function.getArgs().size() == 2) {
					declaredFunctions.connect(nodesToConnect, newNode);
				}
				else if(function.getArgs().size() == 3) {
					EdgeTypes edgeType = EdgeTypes.valueOf(function.getArgs().get(2).toUpperCase());
					declaredFunctions.connect(nodesToConnect, newNode, edgeType);
				}
				break;
				
			case "#weight":
				Collection<Node> nodes = candidates.get(function.getArgs().get(0));
				int weight = Integer.parseInt(function.getArgs().get(1));
				declaredFunctions.weight(nodes, weight);
				break;
				
			default:
				String message = String.format("Unlucky, you forgot to declare function \"%s\" in the method functionsBody() of classe RuleGeneral :(", function.getName());
				throw new IllegalArgumentException(message);
			}
		}
	}
	
	private boolean searchCandidates() {
		/*System.out.println();
		System.out.println();
		System.out.println(rule.getNameRule());*/
		if(!typageBody()) return false;
		//System.out.println("TYPAGE");
		//printCandidates();
		if(!equalityBody()) return false;
		//System.out.println("EQUALITY");
		//printCandidates();
		if(!internalRelationBody()) return false;
		//printCandidates();
		if(!internalRelationBody()) return false; //revérification de la cohérence, l'ordre d'application des conditions peut faire varier les résultats
		//System.out.println("INTERNAL");
		//printCandidates();
		if(!externalRelationBody()) return false;
		//System.out.println("EXTERNAL");
		//printCandidates();
		return true;
	}
	
	private void printCandidates() {
		System.out.println();
		for(String var: externalLabels.keySet()) {
			System.out.println(String.format("%s: %s", var, externalLabels.get(var)));
		}
		for(String var: candidates.keySet()) {
			System.out.println(String.format("%s: %s", var, candidates.get(var)));
		}
	}
	
	public void applyRule() {
		if(searchCandidates()) {
			HashSet<HashMap<String, Node>> tabAssociatedVarNodes = internalRelationHead();
			functionsHead();
			newNode();
		}
	}
}
