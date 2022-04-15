package graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.swing.JFrame;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.ListenableGraph;
import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedMultigraph;

import core.GraphInitialisation;
import core.GraphLemma;
import core.GraphMWE;
import core.GraphPOS;
import core.Rezo;
import core.RuleGeneralApplication;
import rulesGeneral.Interpretation;
import rulesGeneral.Rule;
import utilities.MultiWordTrie;
import utilities.TrieHash;

public class GraphAugmented implements Graph<Node, Edge>{
	private Graph<Node, Edge> graph; //graphe de travail
	private ListenableGraph<Node, Edge> listenableGraph; //"vue" sur le graphe de travail, utile pour les sous-graphes
	
	private NeighborCache<Node, Edge> neighborhood;
	
	private DicoGraph<NodeTypes, Node> dicoNodeTypes; //dictionnaire des noeuds indexés par type
	private DicoGraph<EdgeTypes, Edge> dicoEdgeTypes; //dictionnaire des arcs indexés par type
	
	private Rezo rezo;
	
	private TrieHash trieMWE;
	
	public GraphAugmented() {
		graph = new DirectedMultigraph<>(Edge.class);
		listenableGraph = new DefaultListenableGraph<Node, Edge>(graph);
		
		neighborhood = new NeighborCache<>(graph);
		
		dicoNodeTypes = new DicoGraph<>();
		dicoEdgeTypes = new DicoGraph<>();
		
		rezo = new Rezo();
		
		trieMWE = MultiWordTrie.loadTrieMWE();
	}

	
	
	public DicoGraph<NodeTypes, Node> getDicoNodeTypes() {
		return dicoNodeTypes;
	}

	public DicoGraph<EdgeTypes, Edge> getDicoEdgeTypes() {
		return dicoEdgeTypes;
	}
	
	public ListenableGraph<Node, Edge> getGraphListenable(){
		return listenableGraph;
	}

	public AsSubgraph<Node, Edge> getSubGraphNext(){
		return new AsSubgraph<>(listenableGraph, null, dicoEdgeTypes.get(EdgeTypes.R_SUCC));
	}
	
	public Rezo getRezo() {
		return rezo;
	}

	

	public void initialisation(List<String> sentence) {
		long start = System.currentTimeMillis();
		
		GraphInitialisation init = new GraphInitialisation(this);
		try {
			init.init(sentence);
		} catch (Exception e) {
			System.err.println("Problème rencontré à l'initialisation du graphe.");
			e.printStackTrace();
		}
		
		long end = System.currentTimeMillis();
		System.out.println("\nGraph initialisation done in " + (int)(end-start) + " ms.");
	}
	
	public void etiquetteMWE() {
		long start = System.currentTimeMillis();
		
		GraphMWE mwe = new GraphMWE(this, trieMWE);
		mwe.findMWE();
		
		long end = System.currentTimeMillis();
		System.out.println("\nMWE labeling of graph done in " + (int)(end-start) + " ms.");
	}
	
	public void etiquetteLemma() {
		long start = System.currentTimeMillis();
		
		GraphLemma lemma = new GraphLemma(this);
		lemma.loadLemma();
		
		long end = System.currentTimeMillis();
		System.out.println("\nLemma labeling of graph done in " + (int)(end-start) + " ms.");
	}
	
	public void etiquettePOS() {
		long start = System.currentTimeMillis();
		
		GraphPOS pos = new GraphPOS(this);
		pos.loadPOS();
		
		long end = System.currentTimeMillis();
		System.out.println("\nPOS labeling of graph done in " + (int)(end-start) + " ms.");
	}
	
	public void etiquetteRules(String rulePath) {
		long startInterp = System.currentTimeMillis();
		Interpretation testInterpret = new Interpretation(rulePath);
		List<Rule> rules = testInterpret.interpret();
		long endInterp = System.currentTimeMillis();
		System.out.println(String.format("\nRules interpretation done in %d ms.", (int)(endInterp - startInterp)));
		
		long startRule = System.currentTimeMillis();
		
		for(Rule rule: rules) {
			RuleGeneralApplication ruleApplication = new RuleGeneralApplication(this, rule);
			ruleApplication.applyRule();
		}
		
		long endRule = System.currentTimeMillis();
		System.out.println(String.format("\nRules application done in %d ms.", (int)(endRule - startRule)));
	}
	
	/*public void applyRules(String pathGroupRules) {
		RuleApplication rules = new RuleApplication(this, pathGroupRules);
		rules.applyRules();
	}*/

	
	/****************************************
	 *										*
	 *         OVERRIDES Graph<V,E>			*
	 *     									*
	 ****************************************/
	
	@Override
	public Set<Edge> getAllEdges(Node sourceVertex, Node targetVertex) {
		return graph.getAllEdges(sourceVertex, targetVertex);
	}

	@Override
	public Edge getEdge(Node sourceVertex, Node targetVertex) {
		return graph.getEdge(sourceVertex, targetVertex);
	}

	@Override
	public Supplier<Node> getVertexSupplier() {
		return graph.getVertexSupplier();
	}

	@Override
	public Supplier<Edge> getEdgeSupplier() {
		return graph.getEdgeSupplier();
	}

	@Override
	public Edge addEdge(Node sourceVertex, Node targetVertex) {
		Edge output = graph.addEdge(sourceVertex, targetVertex);
		if(output != null) dicoEdgeTypes.addValue(output);
		return output;
	}

	@Override
	public boolean addEdge(Node sourceVertex, Node targetVertex, Edge e) {
		boolean output = graph.addEdge(sourceVertex, targetVertex, e);
		if(output) dicoEdgeTypes.addValue(e);
		return output;
	}

	@Override
	public Node addVertex() {
		Node output = graph.addVertex();
		dicoNodeTypes.addValue(output);
		return output;
	}

	@Override
	public boolean addVertex(Node v) {
		boolean output = graph.addVertex(v);
		if(output) dicoNodeTypes.addValue(v);
		return output;
	}

	@Override
	public boolean containsEdge(Node sourceVertex, Node targetVertex) {
		return graph.containsEdge(sourceVertex, targetVertex);
	}

	@Override
	public boolean containsEdge(Edge e) {
		return graph.containsEdge(e);
	}

	@Override
	public boolean containsVertex(Node v) {
		return graph.containsVertex(v);
	}

	@Override
	public Set<Edge> edgeSet() {
		return graph.edgeSet();
	}

	@Override
	public int degreeOf(Node vertex) {
		return graph.degreeOf(vertex);
	}

	@Override
	public Set<Edge> edgesOf(Node vertex) {
		return graph.edgesOf(vertex);
	}

	@Override
	public int inDegreeOf(Node vertex) {
		return graph.inDegreeOf(vertex);
	}

	@Override
	public Set<Edge> incomingEdgesOf(Node vertex) {
		return graph.incomingEdgesOf(vertex);
	}

	@Override
	public int outDegreeOf(Node vertex) {
		return graph.outDegreeOf(vertex);
	}

	@Override
	public Set<Edge> outgoingEdgesOf(Node vertex) {
		return graph.outgoingEdgesOf(vertex);
	}

	@Override
	public boolean removeAllEdges(Collection<? extends Edge> edges) {
		boolean output = graph.removeAllEdges(edges);
		if(output){
			for(Edge e: edges) dicoEdgeTypes.removeValue(e);
		}
		return output;
	}

	@Override
	public Set<Edge> removeAllEdges(Node sourceVertex, Node targetVertex) {
		Set<Edge> output = graph.removeAllEdges(sourceVertex, targetVertex);
		if(output != null) {
			for(Edge e: output) dicoEdgeTypes.removeValue(e);
		}
		return output;
	}

	@Override
	public boolean removeAllVertices(Collection<? extends Node> vertices) {
		boolean output = graph.removeAllVertices(vertices);
		for(Node n: vertices) dicoNodeTypes.removeValue(n);
		return output;
	}

	@Override
	public Edge removeEdge(Node sourceVertex, Node targetVertex) {
		Edge output = graph.removeEdge(sourceVertex, targetVertex);
		if(output != null) dicoEdgeTypes.removeValue(output);
		return output;
	}

	@Override
	public boolean removeEdge(Edge e) {
		boolean output = graph.removeEdge(e);
		if(output) dicoEdgeTypes.removeValue(e);
		return output;
	}

	@Override
	public boolean removeVertex(Node v) {
		boolean output = graph.removeVertex(v);
		if(output) dicoNodeTypes.removeValue(v);
		return output;
	}

	@Override
	public Set<Node> vertexSet() {
		return graph.vertexSet();
	}

	@Override
	public Node getEdgeSource(Edge e) {
		return graph.getEdgeSource(e);
	}

	@Override
	public Node getEdgeTarget(Edge e) {
		return graph.getEdgeTarget(e);
	}

	@Override
	public GraphType getType() {
		return graph.getType();
	}

	@Override
	public double getEdgeWeight(Edge e) {
		return graph.getEdgeWeight(e);
	}

	@Override
	public void setEdgeWeight(Edge e, double weight) {
		graph.setEdgeWeight(e, weight);
		e.setWeight((int) Math.round(weight));
	}
	
	
	
	/****************************************
	 *										*
	 *     Méthodes d'accès spécifiques		*
	 *     									*
	 ****************************************/
	
	public AsSubgraph<Node, Edge> getSubGraph(Set<Node> nodes, Set<Edge> edges) {
		return new AsSubgraph<>(listenableGraph, nodes, edges);
	}
	
	private Node getUniqueExpectedNode(NodeTypes type) {
		if(!dicoNodeTypes.containsKey(type) 
				|| dicoNodeTypes.get(type) == null 
				|| dicoNodeTypes.get(type).size() == 0
				) {
			throw(new NullPointerException("Le graphe ne contient pas de noeud "+ type +"."));
		}
		
		/*if(dicoNodeTypes.get(type).size() > 1) {
			throw(new Exception("Le graphe contient plusieurs noeuds "+ type +"."));
		}*/
		
		return dicoNodeTypes.get(type).iterator().next();
	}
	
	/**
	 * Obtention du noeud :START:
	 * @return
	 * @throws Exception
	 */
	public Node getStart() {
		return getUniqueExpectedNode(NodeTypes.START);
	}
	
	/**
	 * Obtention du noeud :END:
	 * @return
	 * @throws Exception
	 */
	public Node getEnd() {
		return getUniqueExpectedNode(NodeTypes.END);
	}

	/**
	 * Renvoie tous les noeuds d'un type donné.
	 * @param type
	 * @return
	 */
	public Set<Node> getAllNodes(NodeTypes type) {
		if(!dicoNodeTypes.containsKey(type) || dicoNodeTypes.get(type) == null) {
			//System.err.println("Type \""+ type +"\" inconnu du dictionnaire.");
			return new HashSet<Node>();
		}
		return dicoNodeTypes.get(type);
	}
	
	/**
	 * Renvoie tous les noeuds de plusieurs types donnés.
	 * Penser à utiliser EnumSet.of()
	 * @param types
	 * @return
	 */
	public Set<Node> getAllNodes(Set<NodeTypes> types){
		Set<Node> output = new HashSet<Node>();
		for(NodeTypes t: types) {
			output.addAll(getAllNodes(t));
		}
		
		return output;
	}
	
	/**
	 * Renvoie tous les noeuds sources d'un type d'arc donné
	 * @param edgeType
	 * @return
	 */
	public Set<Node> getAllSourceNodesEdge(EdgeTypes edgeType){
		//TODO: Créer un dico dédié si souvent utilisé
		Set<Node> output = new HashSet<>();
		//if(dicoEdgeTypes.containsKey(edgeType) && dicoEdgeTypes.get(edgeType) != null) {
			for(Edge e: dicoEdgeTypes.get(edgeType)) {
				output.add(graph.getEdgeSource(e));
			}
		//}
		return output;
	}
	
	/**
	 * Renvoie tous les noeuds cibles d'un type d'arc donné
	 * @param edgeType
	 * @return
	 */
	public Set<Node> getAllTargetNodesEdge(EdgeTypes edgeType){
		//TODO: Créer un dico dédié si souvent utilisé
		Set<Node> output = new HashSet<>();
		//if(dicoEdgeTypes.containsKey(edgeType) && dicoEdgeTypes.get(edgeType) != null) {
			for(Edge e: dicoEdgeTypes.get(edgeType)) {
				output.add(graph.getEdgeTarget(e));
			}
		//}
		return output;
	}
	
	/**
	 * Renvoie tous les arcs d'un type donné.
	 * @param type
	 * @return
	 */
	public Set<Edge> getAllEdges(EdgeTypes type) {
		if(!dicoEdgeTypes.containsKey(type) || dicoEdgeTypes.get(type) == null) {
			//System.err.println("Type \""+ type +"\" inconnu du dictionnaire.");
			return new HashSet<Edge>();
		}
		return dicoEdgeTypes.get(type);
	}
	
	/**
	 * Renvoie tous les arcs de plusieurs types donnés.
	 * Penser à utiliser EnumSet.of()
	 * @param types
	 * @return
	 */
	public Set<Edge> getAllEdges(Set<EdgeTypes> types){
		Set<Edge> output = new HashSet<Edge>();
		for(EdgeTypes t: types) {
			output.addAll(getAllEdges(t));
		}
		return output;
	}

	/**
	 * Insert un noeud entre deux autres noeuds en utilisant un type d'arc spécifié.
	 * @param newNode : le noeud à insérer
	 * @param source : le noeud précédent le noeud à insérer
	 * @param target : le noeud suivant le noeud à insérer
	 * @param typeEdge : le type de relation liant les trois noeuds
	 * @param deleteEdge : true supprime tous les arcs typeEdge qui reliaient source et target
	 */
	public void insertNode(Node newNode, Node source, Node target, EdgeTypes typeEdge, boolean deleteEdge) {
		addVertex(newNode);
		if(deleteEdge) {
			AsSubgraph<Node, Edge> subGraph = getSubGraph(vertexSet(), getAllEdges(typeEdge));
			removeAllEdges(subGraph.getAllEdges(source, target));
		}
		addEdge(source, newNode, new Edge(typeEdge));
		addEdge(newNode, target, new Edge(typeEdge));
	}
	
	
	public Set<Node> predecessorsOf(Node n){
		return neighborhood.predecessorsOf(n);
	}
	
	public Set<Node> successorsOf(Node n){
		return neighborhood.successorsOf(n);
	}
	
	
	
	/****************************************
	 *										*
	 * 		  	      AUTRES				*
	 *     									*
	 ****************************************/
	
	/**
	 * Méthode permettant d'afficher le graphe de travail dans une nouvelle fenêtre.
	 * @throws Exception
	 */

	/**
	 * Méthode d'affichage du graphe
	 * @param printAll: true si on veut afficher tout le graphe, false si on ne veut afficher que les éléments de poids positif
	 */
	public void afficheGraphe(boolean printAll) {
		AffichageGraphe applet = null;
		
		Set<Node> nodesToNOTPrint = new HashSet<>();
		Set<Edge> edgesToNOTPrint = new HashSet<>();
		
		if(!printAll) {
			for(Node n: this.vertexSet()) {
				if(n.getWeight() <= 0) {
					nodesToNOTPrint.add(n);
				}
			}
			for(Edge e: this.edgeSet()) {
				if(e.getWeight() <= 0) {
					edgesToNOTPrint.add(e);
				}
			}
		}
		
		Graph<Node, Edge> graphToPrint = listenableGraph;
		graphToPrint.removeAllVertices(nodesToNOTPrint);
		graphToPrint.removeAllEdges(edgesToNOTPrint);
		
		try {
			applet = new AffichageGraphe(graphToPrint);
		} catch (Exception e) {
			System.err.println("Impossible d'initialiser l'affichage du graphe.");
			e.printStackTrace();
		}
		applet.init();
		
		JFrame frame = new JFrame();
		frame.getContentPane().add(applet);
		frame.setTitle("Graph from GraphAugmented");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		
		System.out.println("\nGraph printed.");
	}
}
