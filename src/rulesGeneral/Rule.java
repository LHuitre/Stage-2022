package rulesGeneral;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;

import graph.Node;
import graph.NodeTypes;

public class Rule {
	/** Chemin du fichier dans lequel la règle est présente **/
	private String userFilePath;
	
	/** Nom de la règle **/
	private String nameRule;
	
	/** Les variables présentes dans le corps de la règle **/
	private HashSet<String> variablesBody;
	
	/** Les variables présentes dans le corps de la règle **/
	private HashSet<String> variablesHead;
	
	/** Déclaration des types de variable, clé: nom variable, valeur: type variable **/
	private HashMap<String, String> typeDeclaration;
	/** Déclaration de nouveaux noeuds, clé: nom noeud, valeur: noeud **/
	private HashMap<String, Node> nodeDeclaration;
	
	/** Ensemble des triplet présent dans le corps de la règle **/
	private HashSet<Triplet> tripletBody;
	/** Ensemble des égalités présentes dans le corps de la règle **/
	private HashSet<Equality> equalityBody;
	/** Ensemble des fonctions présentes dans le corps de la règle **/
	private HashSet<Function> functionBody;
	/** Ensemble des triplet présent dans la tête de la règle **/
	private HashSet<Triplet> tripletHead;
	/** Ensemble des fonctions présentes dans la tête de la règle **/
	private HashSet<Function> functionHead;
	
	public Rule(List<String> linesRule, String filePath) {
		this.userFilePath = filePath;
		
		nameRule = "";
		variablesBody = new HashSet<>();
		variablesHead = new HashSet<>();
		typeDeclaration = new HashMap<>();
		nodeDeclaration = new HashMap<>();
		tripletBody = new HashSet<>();
		equalityBody = new HashSet<>();
		functionBody = new HashSet<>();
		tripletHead = new HashSet<>();
		functionHead = new HashSet<>();
		
		// Nom de la règle
		String nameLine = linesRule.get(0);
		nameRule = nameLine.split("/",3)[2];
		
		// Typage des variables et déclaration des noeuds
		for(int i=1; i<linesRule.size()-2; i++) {
			String line = linesRule.get(i);
			parseDeclaration(line);
		}
		
		// Traitement du corps de règle
		String lineBody = linesRule.get(linesRule.size()-2);
		parseBody(lineBody);
		
		// Traitement de la tête de règle
		String lineHead = linesRule.get(linesRule.size()-1);
		parseHead(lineHead);
		
		collectAllVariables();
	}
	
	
	public String getUserFilePath() {
		return userFilePath;
	}

	public String getNameRule() {
		return nameRule;
	}

	public HashSet<String> getVariablesBody(){
		return variablesBody;
	}
	
	public HashSet<String> getVariablesHead() {
		return variablesHead;
	}

	public HashMap<String, String> getTypeDeclaration() {
		return typeDeclaration;
	}

	public HashMap<String, Node> getNodeDeclaration() {
		return nodeDeclaration;
	}

	public HashSet<Triplet> getTripletBody() {
		return tripletBody;
	}

	public HashSet<Equality> getEqualityBody() {
		return equalityBody;
	}

	public HashSet<Function> getFunctionBody() {
		return functionBody;
	}

	public HashSet<Triplet> getTripletHead() {
		return tripletHead;
	}

	public HashSet<Function> getFunctionHead() {
		return functionHead;
	}


	private void parseDeclaration(String line) {
		int numLine = Integer.parseInt(line.split("/", 3)[1]);
		String restLine = line.split("/", 3)[2];
		
		String type = restLine.split(" ", 2)[0];
		String declarationLine = restLine.split(" ", 2)[1];
		if(!(NodeTypes.isType(type) || type.equals("ALLWORDS"))) {
			String message = String.format("Unknown type declaration in file %s at line %d. "
					+ "Found \"%s\".", userFilePath, numLine, type);
			throw new IllegalArgumentException(message);
		}
		
		if(declarationLine.startsWith("$")) {
			addVarType(type, declarationLine, numLine);
		}
		else if(declarationLine.startsWith("@")) {
			addNodeDeclaration(type, declarationLine, numLine);
		}
		else {
			String ukn = restLine.split(" ", 2)[1];
			String message = String.format("Expected variable or node in file %s at line %d. "
					+ "Found \"%s\" instead.", userFilePath, numLine, ukn);
			throw new IllegalArgumentException(message);
		}
	}
	
	private void addVarType(String type, String declarationLine, int numLine) {
		String[] vars = declarationLine.split(", ");
		
		for(String v: vars) {
			if(v.matches("/\\d+/.*")) {
				String[] split = v.split("/");
				numLine = Integer.parseInt(split[1]);
				v = split[2];
			}
			
			if(!(RegexRule.isSimpleVar(v) || RegexRule.isCompilVar(v))) {
				String message = String.format("Expected simple or compiled variable in type variable declaration in file %s at line %d. "
						+ "Found \"%s\" instead.", userFilePath, numLine, v);
				throw new IllegalArgumentException(message);
			}
			if(typeDeclaration.containsKey(v) && !typeDeclaration.get(v).equals(type)) {
				String message = String.format("Multiple variable declaration of \"%s\" in file %s at line %d. "
						+ "Variable declaration must be unique.", v, userFilePath, numLine);
				throw new IllegalArgumentException(message);
			}
			
			typeDeclaration.put(v, type);
		}
	}
	
	private void addNodeDeclaration(String type, String declarationLine, int numLine) {
		if(type.equals("ALLWORDS")) {
			String message = String.format("Nodes can't be typed with ALLWORDS in file %s at line %d.", userFilePath, numLine);
			throw new IllegalArgumentException(message);
		}
		
		// On split selon la virgule, uniquement si celle-ci n'est pas entre quotes
		boolean isInQuotes = false;
		String acc = "";
		ArrayList<String> nodesToTest = new ArrayList<String>();
		for(char c: declarationLine.toCharArray()) {
			if(!isInQuotes && c == ',') {
				if(!acc.isBlank()) {
					nodesToTest.add(acc.trim());
				}
				acc = "";
			}
			else if(c == '"') {
				isInQuotes = !isInQuotes;
				acc += c;
			}
			else {
				acc += c;
			}
		}
		if(!acc.isBlank()) {
			nodesToTest.add(acc.trim());
		}
		acc = "";

		for(String nd: nodesToTest) {
			String nodeName = nd.split(" = ", 2)[0];
			String valueNode = nd.split(" = ", 2)[1];
			
			if(nodeName.matches("/\\d+/.*")) {
				String[] split = nodeName.split("/");
				numLine = Integer.parseInt(split[1]);
				nodeName = split[2];
			}
			
			if(!RegexRule.isNodeName(nodeName)) {
				String message = String.format("Expected node name in node declaration in file %s at line %d. "
						+ "Found \"%s\".", userFilePath, numLine, nodeName);
				throw new IllegalArgumentException(message);
			}
			if(!RegexRule.isConstant(valueNode)) {
				String message = String.format("Expected constant in node declaration in file %s at line %d. "
						+ "Found \"%s\".", userFilePath, numLine, valueNode);
				throw new IllegalArgumentException(message);
			}
			if(nodesToTest.contains(nodeName)) {
				String message = String.format("Multiple node declaration of \"%s\" in file %s at line %d.", nodeName, userFilePath, numLine);
				throw new IllegalArgumentException(message);
			}
			
			valueNode = valueNode.substring(1, valueNode.length()-1);
			nodeDeclaration.put(nodeName, new Node(NodeTypes.valueOf(type), valueNode));
		}
	}

	/** Pour le corps de règles, indique pour chaque tuple à quelle ligne il est écrit dans le fichier original .txt 
	* @param body
	* @return
	*/
	private HashMap<String, Integer> dicoTupLine(String body) {
		boolean isInConst = false;
		boolean isInNumLine = false;
		String accTuple = "";
		String accNumLine = "";
		int currNumLine = 1;
		HashMap<String, Integer> dicoTupLine = new HashMap<>();
		for(char c: body.toCharArray()) {
			// Si on rencontre un quote on change d'état : dans une constante String ou non
			if(c == '"') {
				accTuple += c;
				isInConst = !isInConst;
			}
			// Si on est dans une constante String, on ajoute le char courant
			else if(isInConst){
				accTuple += c;
			}
			// Si on est pas dans une constante String
			else if(!isInConst) {
				// On entre ou on sort d'une déclaration de numéro de ligne
				if(c == '/') {
					isInNumLine = !isInNumLine;
					// Si on sort d'une déclaration de numéro de ligne ou le met à jour
					if(!isInNumLine) {
						currNumLine = Integer.parseInt(accNumLine);
						accNumLine = "";
					}
				}
				// On est en train de parcourir un numéro de ligne, on le met à jour
				else if(isInNumLine) {
					accNumLine += c;
				}
				// On n'est pas dans une déclaration de numéro de ligne
				else if(!isInNumLine) {
					// Si on rencontre un séparateur de tuples (&), on ajoute le tuple dans le dico
					if(c == '&' && !accTuple.isEmpty()) {
						accTuple = accTuple.trim();
						dicoTupLine.put(accTuple, currNumLine);
						accTuple = "";
					}
					// Sinon on accumule le char
					else {
						accTuple += c;
					}
				}
			}
		}
		
		if(!accTuple.isEmpty()) {
			accTuple = accTuple.trim();
			dicoTupLine.put(accTuple, currNumLine);
			accTuple = "";
		}
		
		return dicoTupLine;
	}

	
	private boolean isConnexRel() {
		Graph<String, DefaultEdge> connexGraph = new DefaultUndirectedGraph<>(DefaultEdge.class);
		for(Triplet triplet: tripletBody) {
			if(triplet.getPredicat().matches("^!?r_.*")
					&& RegexRule.isSimpleVar(triplet.getSujet())
					&& RegexRule.isSimpleVar(triplet.getObjet())) {
				
				String sujet = triplet.getSujet();
				String objet = triplet.getObjet();
				
				connexGraph.addVertex(sujet);
				connexGraph.addVertex(objet);
				connexGraph.addEdge(sujet, objet);
			}
		}
		
		if(connexGraph.vertexSet().size() <= 1) {
			return true;
		}
		
		ConnectivityInspector<String, DefaultEdge> inspector = new ConnectivityInspector<>(connexGraph);
		return inspector.isConnected();
	}
	
	private void parseBody(String line) {
		HashMap<String, Integer> dico = dicoTupLine(line);
		
		for(String entry: dico.keySet()) {
			if(entry.matches(RegexRule.SIMPLE_VAR + " "
					+RegexRule.RELATION+"(\\(.*\\))?" + " "
					+"(("+RegexRule.SIMPLE_VAR+")|("+RegexRule.COMPIL_VAR+"))")) {
				tripletBody.add(new Triplet(entry, userFilePath, dico.get(entry)));
			}
			else if(entry.matches("(("+RegexRule.SIMPLE_VAR+")|("+RegexRule.COMPIL_VAR+")) "
					+"(("+RegexRule._EQUAL+")|("+RegexRule._DIFFERENT+")) "
					+RegexRule.LIST_CONSTANT)) {
				equalityBody.add(new Equality(entry, userFilePath, dico.get(entry)));
			}
			else if(RegexRule.isFunction(entry)) {
				functionBody.add(new Function(entry, userFilePath, dico.get(entry)));
			}
			else {
				String message = String.format("Unknown kind of condition in body rule \"%s\" in file %s at line %d. "
						+ "Found \"%s\".", nameRule, userFilePath, dico.get(entry), entry);
				throw new IllegalArgumentException(message);
			}
		}
		
		if(!isConnexRel()) {
			String message = String.format("Unconnected relations in body rule \"%s\" in file %s.", nameRule, userFilePath);
			throw new IllegalArgumentException(message);
		}
	}
	
	private void parseHead(String line) {
		HashMap<String, Integer> dico = dicoTupLine(line);
		
		for(String entry: dico.keySet()) {
			if(entry.matches("(("+RegexRule.SIMPLE_VAR+")|("+RegexRule.COMPIL_VAR+"))" + " "
					+RegexRule.RELATION+"(\\(.*\\))?" + " "
					+"(("+RegexRule.SIMPLE_VAR+")|("+RegexRule.COMPIL_VAR+")|("+RegexRule.NODE_NAME+"))")) {
				Triplet newTriplet = new Triplet(entry, userFilePath, dico.get(entry));
				tripletHead.add(newTriplet);
				if(RegexRule.isSimpleVar(newTriplet.getSujet())) {
					variablesHead.add(newTriplet.getSujet());
				}
				if(RegexRule.isSimpleVar(newTriplet.getObjet())) {
					variablesHead.add(newTriplet.getObjet());
				}
			}
			else if(RegexRule.isFunction(entry)) {
				Function newFunction = new Function(entry, userFilePath, dico.get(entry));
				functionHead.add(newFunction);
				for(String var: newFunction.getArgs()) {
					if(RegexRule.isSimpleVar(var)) {
						variablesHead.add(var);
					}
				}
			}
			else {
				String message = String.format("Unknown kind of condition in head rule \"%s\" in file %s at line %d. "
						+ "Found \"%s\".", nameRule, userFilePath, dico.get(entry), entry);
				throw new IllegalArgumentException(message);
			}
		}
	}
	
	private void collectAllVariables() {
		for(Triplet triplet: tripletBody) {
			String sujet = triplet.getSujet();
			if(RegexRule.isSimpleVar(sujet) || RegexRule.isCompilVar(sujet)) {
				variablesBody.add(sujet);
			}
			String objet = triplet.getObjet();
			if(RegexRule.isSimpleVar(objet) || RegexRule.isCompilVar(objet)) {
				variablesBody.add(objet);
			}
		}
		
		for(Equality equality: equalityBody) {
			String sujet = equality.getSujet();
			if(RegexRule.isSimpleVar(sujet) || RegexRule.isCompilVar(sujet)) {
				variablesBody.add(sujet);
			}
			
			List<String> objet = new ArrayList<>(equality.getObjet());
			for(String o: objet) {
				if(RegexRule.isSimpleVar(o) || RegexRule.isCompilVar(o)) {
					variablesBody.add(o);
				}
			}
		}
		
		for(Function function: functionBody) {
			List<String> args = function.getArgs();
			for(String a: args) {
				if(RegexRule.isSimpleVar(a) || RegexRule.isCompilVar(a)) {
					variablesBody.add(a);
				}
			}
		}
	}
}
