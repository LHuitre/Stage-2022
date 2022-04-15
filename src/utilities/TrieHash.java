package utilities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("rawtypes")
public class TrieHash implements Serializable{
	private static final long serialVersionUID = 1L;
	
	// implementing a HashMap
	public HashMap<String, HashMap> origin;

	public TrieHash() {
		origin = new HashMap<String, HashMap>();
	}

	// attach function to add character to the trie
	@SuppressWarnings("unchecked")
	public void attach(String[] mwe) {
		HashMap<String, HashMap> node = origin;
		for(String word: mwe) {
			if(node.containsKey(word)) {
				node = node.get(word);
			}
			else {
				node.put(word, new HashMap<String, HashMap>());
				node = node.get(word);
			}
		}
		// putting "" to end the string
		node.put("", null);
	}

	// function to search for the specific String in the hash trie
	@SuppressWarnings("unchecked")
	/**
	 * 
	 * @param mwe
	 * @return paire <mwe connu de JdM?, potentiel mwe suivant?>
	 */
	public Entry<Boolean, Boolean> search(String mwe) {
		mwe = mwe.replace("'", "' ");
		String[] mweSplit = mwe.split(" ");
		
		HashMap<String, HashMap> presentNode = origin;

		for(String word: mweSplit) {
			if(presentNode.containsKey(word)) {
				presentNode = presentNode.get(word);
			}
			else {
				return Map.entry(false, false);
			}
		}
		
		return Map.entry(presentNode.containsKey(""), 
				(presentNode.containsKey("") && presentNode.size() > 1) //si mwe existe, est-ce qu'il y en a d'autres après ?
				|| (!presentNode.containsKey("") && presentNode.size() > 0)); //si mwe n'existe pas, est-ce qu'il y en a d'autres après ?
	}
	
	/*public static void main(String[] args) {
		TrieHash ExtractRelations = new TrieHash();
		String[] w1 = {"gros"};
		String[] w2 = {"gros", "chien", "noir"};
		String[] w3 = {"gros", "chien", "noir", "méchant"};
		String[] w4 = {"gros", "chien", "noir", "gentil"};
		String[] w5 = {"gros", "monsieur"};
		String[] w6 = {"petit", "chat"};
		String[] w7 = {"gros", "chien", "noir", "des", "Carpates"};
		
		ExtractRelations.attach(w1);
		ExtractRelations.attach(w2);
		ExtractRelations.attach(w3);
		ExtractRelations.attach(w4);
		ExtractRelations.attach(w5);
		ExtractRelations.attach(w6);
		ExtractRelations.attach(w7);
		
		
		System.out.println(ExtractRelations.search("gros chien"));
		System.out.println(ExtractRelations.search("gros chien noir"));
		System.out.println(ExtractRelations.search(""));
		
		//ExtractRelations.DFS(ExtractRelations.origin);
		
		for(String k1: ExtractRelations.origin.keySet()) {
			System.out.println(k1);
			HashMap<String, HashMap> val1 = ExtractRelations.origin.get(k1);
			for(String k2: val1.keySet()) {
				System.out.println("\t"+k2);
				HashMap<String, HashMap> val2 = val1.get(k2);
				if(val2!=null) {
					for(String k3: val2.keySet()) {
						System.out.println("\t\t"+k3);
						HashMap<String, HashMap> val3 = val2.get(k3);
						if(val3!=null) {
							for(String k4: val3.keySet()) {
								System.out.println("\t\t\t"+k4);
							}
						}
					}
				}
			}
		}
	}*/
}

