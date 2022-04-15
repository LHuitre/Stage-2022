package rulesGeneral;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TraductionFinal {
	String userFilePath;
	String precFileComp;
	int currentLine;
	
	
	public TraductionFinal(String filePath) {
		this.userFilePath = filePath;
		currentLine = 1;
		
		TraductionInter trad01 = new TraductionInter(filePath);
		precFileComp = trad01.tradFile();
	}

	 /* Ecriture du fichier de sortie correspondant au fichier pré-compilé.
	 * @param input
	 */
	private String writeOutputFile(String input) {
		String[] splitPath = userFilePath.split("/");
		String newDirPath = "rules_comp/";
		
		for(int i=1; i<splitPath.length-1; i++) {
			newDirPath += splitPath[i]+"/";
		}
		
		File directory = new File(newDirPath);
		if (!directory.exists()){
	        directory.mkdir();
	    }
		
		String newFilePath = newDirPath + splitPath[splitPath.length-1];
		newFilePath = newFilePath.replaceAll(".txt$", ".comp");
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(newFilePath));
			writer.write(input);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return newFilePath;
	}
	
	public String trad() {
		// Ouverture du fichier
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(precFileComp, StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<List<String>> rulesStr = new ArrayList<>();
		
		String line;
		try {
			List<String> rule = new ArrayList<>();
			while ((line = reader.readLine()) != null) {
				if(line.isEmpty()) {
					rulesStr.add(rule);
					rule = new ArrayList<String>();
				}
				else {
					rule.add(line);
				}
			}
			
			rulesStr.add(rule);
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String output = "";
		for(List<String> rule: rulesStr) {
			output += tradRule(rule);
		}
		
		output = output.trim();
		
		return writeOutputFile(output);
	}
	
	/**
	 * Condense les déclarations de variables et de nodes se trouvant sur des lignes différentes
	 * @param rule: rule représentée sous la forme de listes de lignes
	 * @return
	 */
	private List<String> condenseType(List<String> rule) {
		List<String> output = new ArrayList<>(); // Liste de ligne qui sera condensée
		output.add(rule.get(0)); // On ajoute la première ligne car c'est le nom de la règle
		
		// Structure pour la condensation des déclarations de variable
		HashMap<String, Integer> typeVarLine = new HashMap<>(); //clé: type de variable ; val: numéro de ligne
		HashMap<String, Integer> varLine = new HashMap<>(); //clé: nom de variable ; val: numéro de ligne
		HashMap<String, List<String>> typeVar = new HashMap<>(); //clé: type de variable ; val: nom de la variable
		
		// Structure pour la condensation des déclarations de nodes (idem que struct précédentes)
		HashMap<String, Integer> typeNodeLine = new HashMap<>();
		HashMap<String, Integer> nodeLine = new HashMap<>();
		HashMap<String, List<String>> typeNode = new HashMap<>();
		
		// De la 2e ligne à l'antépénultième
		for(int i=1; i<rule.size()-2; i++) {
			
			/* Condensation des déclarations de variable */
			int numLine = Integer.parseInt(rule.get(i).split("/")[1]); //le numéro de ligne courant
			String line = rule.get(i).split("/")[2]; //ce qui suit le numéro de ligne courant
			String[] splitLine = line.split(" ", 2); //split selon le 1er espace
			
			String type = splitLine[0]; //le type d'affectation de la ligne
			
			if(!RegexRule.isTypeVar(type)) {
				String message = String.format("Type declaration expected in file %s at line %d. Found \"%s\" instead.", userFilePath, numLine, type);
				throw new IllegalArgumentException(message);
			}
			
			// On commence bien avec une variable, tout le reste de la ligne devra aussi être des déclarations de var
			if(splitLine[1].startsWith("$")) {
				String[] splitVars = splitLine[1].split(" ?, ?"); //split selon le virgule suivie ou précédée par une espace
				
				typeVarLine.putIfAbsent(type, numLine);
				typeVar.putIfAbsent(type, new ArrayList<>());
				
				// Parcourt des différentes variables de la ligne
				for(String var: splitVars) {
					if(!RegexRule.isSimpleVar(var)) {
						String message = String.format("Variable declaration expected in file %s at line %d. Found \"%s\" instead.", userFilePath, numLine, var);
						throw new IllegalArgumentException(message);
					}
					typeVar.get(type).add(var);
					
					if(varLine.containsKey(var)) {
						String message = String.format("Multiple variable declaration of \"%s\" in file %s at line %d. Variable declaration must be unique.", var, userFilePath, numLine);
						throw new IllegalArgumentException(message);
					}
					varLine.put(var, numLine);
				}
			}
			
			// On commence avec une déclaration de node, tout le reste de la ligne doit être de même
			else if(splitLine[1].startsWith("@")){
				// On split selon la virgule, uniquement si celle-ci n'est pas entre quotes
				boolean isInQuotes = false;
				String acc = "";
				ArrayList<String> nodeDeclarations = new ArrayList<String>();
				for(char c: splitLine[1].toCharArray()) {
					if(!isInQuotes && c == ',') {
						if(!acc.isBlank()) {
							nodeDeclarations.add(acc.trim());
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
					nodeDeclarations.add(acc.trim());
				}
				acc = "";
				
				// Parcourt des différentes déclarations de nodes
				typeNodeLine.putIfAbsent(type, numLine);
				typeNode.putIfAbsent(type, new ArrayList<>());
				for(String nodeDecl: nodeDeclarations) {
					
					if(!RegexRule.isNodeName(nodeDecl.split(" ")[0])) {
						String message = String.format("Node declaration expected in file %s at line %d. Found \"%s\" instead.", userFilePath, numLine, nodeDecl);
						throw new IllegalArgumentException(message);
					}
					typeNode.get(type).add(nodeDecl);
					
					if(nodeLine.containsKey(nodeDecl)) {
						String message = String.format("Multiple node declaration in file %s at line %d. Node declaration must be unique.", userFilePath, numLine, nodeDecl);
						throw new IllegalArgumentException(message);
					}
					nodeLine.put(nodeDecl, numLine);
				}
			}
		}
		
		// Ecriture des variables condensées dans une String correspondant à la nouvelle ligne
		for(String type: typeVarLine.keySet()) {
			String tempStr = String.format("/%d/%s ", typeVarLine.get(type), type);
			for(String var: typeVar.get(type)) {
				if(!varLine.get(var).equals(typeVarLine.get(type))) {
					tempStr += String.format("/%d/%s, ", varLine.get(var), var);
				}
				else {
					tempStr += String.format("%s, ", var);
				}
			}
			
			tempStr = tempStr.substring(0, tempStr.length()-2);
			output.add(tempStr);
		}
		
		// Ecriture des déclarations de nodes condensées dans une String correspondant à la nouvelle ligne
		for(String type: typeNodeLine.keySet()) {
			String tempStr = String.format("/%d/%s ", typeNodeLine.get(type), type);
			for(String node: typeNode.get(type)) {
				if(!nodeLine.get(node).equals(typeNodeLine.get(type))) {
					tempStr += String.format("/%d/%s, ", nodeLine.get(node), node);
				}
				else {
					tempStr += String.format("%s, ", node);
				}
			}
			
			tempStr = tempStr.substring(0, tempStr.length()-2);
			output.add(tempStr);
		}
		
		// Ajout du corps et de la tête de règle
		output.add(rule.get(rule.size()-2));
		output.add(rule.get(rule.size()-1));
		
		return output;
	}
	
	/**
	 * Pour le corps de règles, indique pour chaque tuple à quelle ligne il est écrit dans le fichier original XXX.txt 
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
	
	/**
	 * Réécrit le corps de règle.
	 * Si une relation a comme objet une constante ou disjonction de constantes, 
	 * l'objet est réécrit comme une variable égale à la disjonction de constantes.<br>
	 * Une variable seule est réécrite comme disjonction d'une seule variable. 
	 * @param rule
	 * @return
	 */
	private List<String> rewriteTuples(List<String> rule) {
		int currentCompVar = 1;
		String body = rule.get(rule.size()-2); //le corps de règle
		HashMap<String, Integer> dicoTuples = dicoTupLine(body);
		
		HashMap<String, List<String>> newTypes = new HashMap<>();
		
		for(String triplet: dicoTuples.keySet()) {
			// Triplet ayant une relation en prédicat
			if(triplet.matches(RegexRule.SIMPLE_VAR + " "
					+RegexRule.RELATION+"(\\(.*\\))?" + " "
					+"(("+RegexRule.CONSTANT+")|("+RegexRule.LIST_CONSTANT+"))")) {
				String objet = triplet.split(RegexRule.RELATION+"(\\(.*\\))? ?")[1];
				String newObjet = objet;
				if(RegexRule.isConstant(objet)) { //on transforme la constante en disjonction d'une unique constante
					newObjet = "["+objet+"]";
				}
				
				String tripletEqual = "";
				String newVar = "";
				if(RegexRule.isListConst(newObjet)) {
					newVar = String.format("$$%02d", currentCompVar); //variable sous la forme $$dd avec d un chiffre
					tripletEqual = String.format("%s == %s", newVar, newObjet); //nouvelle relation d'égalité
					currentCompVar++;
				}
				
				String newTriplet = triplet;
				newTriplet = newTriplet.replace(objet, newVar);
				
				// On crée une paire de triplet : ancien triplet avec objet transformé en variable & nouvelle variable égale à l'ancien objet
				if(!tripletEqual.isEmpty()) {
					String newPair = newTriplet + " & " + tripletEqual;
					body = body.replace(triplet, newPair);
					
					// Si le prédicat est respectivement un pos ou lemma, alors l'objet est forcément de type POS ou LEMMA
					String predicat = newTriplet.split(" ")[1];
					if(predicat.matches("!?[er]_pos.*")) {
						newTypes.putIfAbsent("POS", new ArrayList<>());
						newTypes.get("POS").add(newVar);
					}
					else if(predicat.equals("!?[er]_lemma.*")) {
						newTypes.putIfAbsent("LEMMA", new ArrayList<>());
						newTypes.get("LEMMA").add(newVar);
					}
				}
			}
			
			// Si dans une égalité déjà existante, l'objet est une constante, on la transforme en disjoncition
			else if(triplet.matches("[^\"\t\r\n]+ ?[!=]=.*")) {
				String objet = triplet.split("[!=]=")[1].trim();
				
				String newObjet = objet;
				if(RegexRule.isConstant(objet)) {
					
					newObjet = "["+objet+"]";
				}
				
				String newTriplet = triplet.replace(objet, newObjet);
				body = body.replace(triplet, newTriplet);
			}
		}
		
		// On ajoute aux déclarations de variables déjà existantes les nouvelles déclarations de variables(ici POS et LEMMA)
		List<String> output = new ArrayList<>();
		output.add(rule.get(0));
		
		int numRuleLine = 1;
		String line = rule.get(numRuleLine);
		while(!line.split(" ", 2)[1].startsWith("@") && !newTypes.isEmpty() && numRuleLine < rule.size()-2) {
			String type = line.split("/", 3)[2].split(" ", 2)[0];
			
			if(newTypes.containsKey(type)) {
				List<String> compilVar = newTypes.get(type);
				for(String v: compilVar) {
					line += ", " + v;
				}
				newTypes.remove(type);
			}
			output.add(line);
			numRuleLine++;
			line = rule.get(numRuleLine);
		}
		
		if(!newTypes.isEmpty()) {
			for(String typesToAdd: newTypes.keySet()) {
				String newLine =  "/"+ numRuleLine +"/"+ typesToAdd + " ";
				for(String compilVar: newTypes.get(typesToAdd)){
					newLine += compilVar + ", ";
				}
				newLine = newLine.substring(0, newLine.length()-2);
				output.add(newLine);
			}
		}
		
		for(int i=numRuleLine; i<rule.size()-2; i++) {
			output.add(rule.get(i));
		}
		
		output.add(body);
		output.add(rule.get(rule.size()-1));
		
		return output;
	}
	
	private String tradRule(List<String> rule) {
		List<String> condensedRule = condenseType(rule);
		
		List<String> rewrotenRule = rewriteTuples(condensedRule);
		
		String output = "";
		for(String line: rewrotenRule) {
			output += line + "\n";
		}
		
		return output + "\n";
	}
	
	/*public static void main(String[] args) {
		TraductionFinal ExtractRelations = new TraductionFinal("rules/ExtractRelations.txt");
		ExtractRelations.trad();
	}*/
}
