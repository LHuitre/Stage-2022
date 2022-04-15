package rulesGeneral;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

class TraductionInter {
	String filePath;
	Deque<Map.Entry<Character, Integer>> stackParenthesis;
	int currentLine;
	
	public TraductionInter(String filePath) {
		this.filePath = filePath;
		stackParenthesis = new ArrayDeque<>();
		currentLine = 1;
	}
	
	/**
	 * Ecriture du fichier de sortie correspondant au fichier pré-compilé.
	 * @param input
	 */
	private String writeOutputFile(String input) {
		String[] splitPath = filePath.split("/");
		String newDirPath = "rules_comptmp/";
		
		for(int i=1; i<splitPath.length-1; i++) {
			newDirPath += splitPath[i]+"/";
		}
		
		File directory = new File(newDirPath);
		if (!directory.exists()){
	        directory.mkdir();
	    }
		
		String newFilePath = newDirPath + splitPath[splitPath.length-1];
		newFilePath = newFilePath.replaceAll(".txt$", ".comptmp");
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(newFilePath));
			writer.write(input);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return newFilePath;
	}
	
	public String tradFile() {
		// Vérification du parenthésage
		verifyParenthesis();
		
		// Ouverture du fichier
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String acc = ""; // Accumulateur qui va sauvegarder l'ensemble du fichier
		
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				
				// Si la ligne n'est pas blanche, on la nettoie et on l'ajoute à acc
				// On ajoute à acc le numéro de ligne correspondant dans le fichier original
				if(!line.isBlank()) {
					String cleanLines = cleanLine(line);
					
					if(!cleanLines.isBlank()) {
						acc += cleanLines;
					}
				}
				currentLine++;
			}
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		acc = acc.replaceAll("/\\d+/[;{]\n", "\n");
		acc = acc.replaceAll("\n+", "\n");
		acc = acc.replaceAll("/\\d+/}\n", "\n");
		acc = acc.replaceAll("[;{]\n", "\n");
		
		
		acc = acc.trim() + "\n";
		
		return writeOutputFile(acc);
	}
	
	
	/**
	 * Méthode s'occupant de renvoyer un message d'erreur si on rencontre un char incohérent
	 * (mauvais parenthésage).
	 * <br>S'il n'y a pas d'incohérence, on enlève le dernier élément de la pile.
	 * @param currentChar: le char courant
	 * @param start: le dernier char ouvrant qui est attendu
	 */
	private void testClosureParenthesis(char currentChar, char start) {
		if(stackParenthesis.getLast().getKey() != start) {
			String message = String.format("Expecting closure of '%c' in file %s at line %d, found '%c' instead at line %d.", 
					stackParenthesis.getLast().getKey(), filePath, stackParenthesis.getLast().getValue(), currentChar, currentLine);
			throw new IllegalArgumentException(message);
		}
		else {
			stackParenthesis.removeLast();
		}
	}
	
	
	private void verifyLine(String line) {
		boolean isAConstant = false; // Le char courant est-il entre quote ou non ?
		
		for(char c: line.toCharArray()) {
			// On rencontre un quote, cela change on sort ou rentre dans une constante String
			if(c == '"') {
				isAConstant = !isAConstant;
			}
			
			// On n'est pas dans une constante, on doit donc vérifier le parenthésage
			else if(!isAConstant) {
				if(c == '(' || c == '{' || c == '[') {
					stackParenthesis.add(new AbstractMap.SimpleEntry<>(c, currentLine));
				}
				else {
					switch(c) {
						case ')':
							testClosureParenthesis(c, '(');
							break;
						case ']':
							testClosureParenthesis(c, '[');
							break;
						case '}':
							testClosureParenthesis(c, '{');
							break;
					}
				}
			}
		}
		
		if(isAConstant) { // il manque un quote pour terminer proprement la ligne
			String message = String.format("Bad use of '\"' in file %s at line %d.", filePath, currentLine);
			throw new IllegalArgumentException(message);
		}
	}
	
	/**
	 * Vérifie que le parenthésage des symboles (), [], {} est cohérent.
	 * Ne prend pas en compte ce qui se trouve dans un commentaire ou dans une constante déclarée entre quotes.
	 * <br>
	 * Vérifie aussi la bonne utilisation des quotes, 
	 * une String définie entre quotes ne peut pas s'étendre sur plusieurs lignes
	 * 
	 * @throw IllegalArgumentException() si le parenthésage est mauvais ou que les quotes sont invalides.
	 */
	public void verifyParenthesis() {
		// Ouverture du fichier courant
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Lecture du fichier ligne par ligne
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				// On ne retient que ce qui se trouve à gauche d'un commentaire
				String lineWithoutComm = line.split("//", 2)[0];
				lineWithoutComm = lineWithoutComm.trim();
				
				// La ligne sans commentaire n'est pas vide, on continue de vérifier le parenthésage
				if(!lineWithoutComm.isEmpty()) {
					verifyLine(lineWithoutComm);
				}
				
				currentLine++;
			}
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		currentLine--;
		
		// À la fin du fichier, toutes les parenthèses n'ont pas été fermées
		if(!stackParenthesis.isEmpty()) {
			String message = String.format("Missing closing parenthesis at end of file %s, "
					+ "this sequence must be closed: %s.", filePath, stackParenthesis.toString());
			throw new IllegalArgumentException(message);
		}
		
		// Réinitialisation des variables globales
		currentLine = 1;
		stackParenthesis = new ArrayDeque<>();
	}
	
	
	private String cleanLine(String line) {
		String inputClean = line.split("//", 2)[0];
		inputClean = inputClean.trim();
		
		// On cherche à condenser les char blancs successifs
		// On ne touche pas à ce qui est entre "", ce sont des constantes
		String[] split = inputClean.split("\"");
		String acc = "/"+currentLine+"/";
		for(int i=0; i<split.length; i++) {
			if(i%2 == 0) { // on n'est pas dans une String entre "
				String tmp = split[i].replaceAll("\\s+", " ");
				tmp = tmp.replace("}", "}\n"+"/"+currentLine+"/");
				tmp = tmp.replace("{", "{\n"+"/"+currentLine+"/");
				tmp = tmp.replace(";", ";\n"+"/"+currentLine+"/");
				
				acc += tmp;
			}
			else { // On est dans une String entre ", on ne touche à rien
				acc += "\""+ split[i] + "\"";
			}
		}
		
		acc = acc.replaceAll("/\\d+/$", ""); //On supprime le dernier numéro de ligne qui ne sert à rien
		return acc;
	}
	
	public static void main(String[] args) {
		TraductionInter test = new TraductionInter("rules/ExtractRelations.txt");
		test.tradFile();
	}
}
