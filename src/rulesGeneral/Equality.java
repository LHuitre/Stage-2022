package rulesGeneral;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Equality {
	private String filePath;
	private int numLine;
	
	private String sujet;
	private boolean equal;
	private Set<String> objet;
	
	public Equality(String equality, String filePath, int numLine) {
		this.filePath = filePath;
		this.numLine = numLine;
		
		String[] split = equality.split(" ",3);
		
		sujet = split[0];
		equal = split[1].equals("==");
		objet = parseList(split[2]);
		
	}
	
	public String getSujet() {
		return sujet;
	}
	
	public boolean isEqual() {
		return equal;
	}

	public Set<String> getObjet() {
		return objet;
	}

	private Set<String> parseList(String list){
		Set<String> output = new HashSet<>();
		
		boolean isInQuotes = false;
		String acc = "";
		for(char c: list.toCharArray()) {
			if(c=='"') {
				isInQuotes = !isInQuotes;
				if(!isInQuotes) {
					output.add(acc);
					acc = "";
				}
			}
			else if(isInQuotes) {
				acc+=c;
			}
		}
		return output;
	}

	public String toString() {
		return String.format("(%s;%s;%s)", sujet, equal, objet);
	}
}
