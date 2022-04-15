package core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import requeterrezo.Filtre;
import requeterrezo.Mot;
import requeterrezo.Relation;
import requeterrezo.RequeterRezo;
import requeterrezo.RequeterRezoDump;
import requeterrezo.RequeterRezoSQL;
import requeterrezo.Resultat;


/**
 * Sert à initialiser correctement Rezo.
 * Peut fonctionner avec une BDD interne ou interroger JdM à l'aide de RezoDump si la BDD n'existe pas.
 */
public class Rezo {
	private final static int weightMinJdM = -5000; //poids minimal d'une relation dans JdM
	private final static int weightMaxJdM = 5000; //poids maximal d'une relation dans JdM
	
	private RequeterRezo rezo;
	
	/**
	 * GraphInitialisation de la BDD JdM.
	 * @param fromRezoDump : <li>true => permet de faire les requêtes à partir de RezoDump</li>
	 * <li>false => permet de faire les requêtes à partir d'une BDD MySql en local (penser à modifier le fichier db.properties)</li>
	 */
	public Rezo() {
		//Read properties
		Properties prop = new Properties();
	    InputStream input = null;

	    try {
	    	input = getClass().getClassLoader().getResourceAsStream("db.properties");
	        // load a properties file
	        prop.load(input);

	    } catch (IOException ex) {
	        ex.printStackTrace();
	    } finally {
	        if (input != null) {
	            try {
	                input.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    
	    boolean localDB = Boolean.parseBoolean(prop.getProperty("use_local_db"));
	    if(!localDB) {
			rezo = new RequeterRezoDump();
		}
		
		else {
			rezo = new RequeterRezoSQL(prop.getProperty("server"),
										prop.getProperty("database"),
										prop.getProperty("user"),
										prop.getProperty("password"));
		}
	}
	
	public RequeterRezo getRezo() {
		return rezo;
	}
	
	
	/**
	 * Est-ce qu'un mot est connu de JdM ?
	 * @param word : le mot à tester
	 * @return
	 */
	public boolean existWord(String word) {
		boolean output = rezo.requete(word, Filtre.RejeterRelationsEntrantesEtSortantes).getMot() != null;
		return output;
	}
	
	/**
	 * Est-ce qu'une relation est un nom de relation utilisé par JdM ?
	 * @param relation : la relation à tester (doit contenir le préfixe "r_")
	 * @return
	 */
	public boolean existNameRelation(String relation) {
		boolean output = relation.startsWith("r_") && RequeterRezo.correspondancesRelations.get(relation) != null;
		return output;
	}
	
	/**
	 * Convertit un mot raffiné en son/ses id(s) correspondant.
	 * <br> ex : plante>botanique => plante>3674
	 * @param input : le mot raffiné à convertir
	 * @return Le mot raffiné avec ses id.
	 */
	public String raffToId(String input) {
		if(input.contains(">")) {
			String[] split = input.split(">");
			int n = split.length;
			
			String acc = split[0];
			for(int i = 1; i < n ; i++) {
				long idWord = rezo.requete(split[i], Filtre.RejeterRelationsEntrantesEtSortantes).getMot().getIdRezo();
				acc += ">" + ((int) idWord);
			}
			return acc;
		}
		else {
			return input;
		}
	}
	
	
	
	
	/**
	 * Vérifie si un mot ou une relation existent dans JdM.
	 * Affiche dans err si un des deux n'existe pas.
	 * @param word : le mot à tester
	 * @param relation : la relation à tester (munie de son préfixe "r_")
	 * @return true si la relation et le mot existent, false sinon
	 */
	private boolean verifWordRel(String word, String relation) {
		boolean existRel = existNameRelation(relation);
		boolean existWord = existWord(word);
		if(!(existRel && existWord)) {
			System.err.println("Echec de la requête : word: \"" + word + "\", relation: \"" + relation + "\"");
			if(!existRel) {
				System.err.println("\nNom de relation \"" + relation + "\" inconnu.");
			}
			if (!existWord) {
				System.err.println("\nMot \"" + word + "\" inconnu.");
			}
			return false;
		}
		return true;
	}
	
	
	
	/**
	 * 
	 * @param word : le mot sur lequel porte la requête
	 * @param relation : la relation choisie
	 * @param filtre : restriction sur les relations
	 * @param weightMin : poids minimal de la relation
	 * @param weightMax : poids maximal de la relation
	 * @return
	 */
	public Set<String> getRelations(String word, String relation, Filtre filtre, int weightMin, int weightMax){
		Set<String> output = new HashSet<>();
		
		//vérifie si le mot et la relation existent dans JdM
		if(!verifWordRel(word, relation)) {
			return output;
		}
		
		Resultat resultatRequete;
		Mot mot;
		List<Relation> voisins;
	
		try {
			resultatRequete = rezo.requete(word, relation, filtre);
			mot = resultatRequete.getMot();
		
			voisins = mot.getRelationsSortantesTypees(relation);
			for(Relation voisin : voisins) {
				if(voisin.getPoids()!=0 && voisin.getPoids() >= weightMin && voisin.getPoids() <= weightMax) {
					output.add(voisin.getNomDestination());
				}
			}
			voisins = mot.getRelationsEntrantesTypees(relation);
			for(Relation voisin : voisins) {
				if(voisin.getPoids()!=0 && voisin.getPoids() >= weightMin && voisin.getPoids() <= weightMax) {
					output.add(voisin.getNomSource());
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	/**
	 * Renvoie les relations ayant un poids supérieur à une valeur donnée.
	 * @param word : le mot sur lequel porte la requête
	 * @param relation : la relation choisie
	 * @param filtre : restriction sur les relations
	 * @param weightMin : poids minimal de la relation (weightMin inclus)
	 * @return
	 */
	public Set<String> getRelationsWMin(String word, String relation, Filtre filtre, int weightMin){
		return getRelations(word, relation, filtre, weightMin, weightMaxJdM);
	}
	
	/**
	 * Renvoie les relations ayant un poids positif.
	 * @param word : le mot sur lequel porte la requête
	 * @param relation : la relation choisie
	 * @param filtre : restriction sur les relations
	 * @return
	 */
	public Set<String> getRelationsWPos(String word, String relation, Filtre filtre){
		return getRelations(word, relation, filtre, 1, weightMaxJdM);
	}
	
	/**
	 * Renvoie les relations ayant un poids négatif.
	 * @param word : le mot sur lequel porte la requête
	 * @param relation : la relation choisie
	 * @param filtre : restriction sur les relations
	 * @return
	 */
	public Set<String> getRelationsWNeg(String word, String relation, Filtre filtre){
		return getRelations(word, relation, filtre, weightMinJdM, -1);
	}
	
	/**
	 * Renvoie toutes les relations.
	 * @param word : le mot sur lequel porte la requête
	 * @param relation : la relation choisie
	 * @param filtre : restriction sur les relations
	 * @return
	 */
	public Set<String> getAllRelations(String word, String relation, Filtre filtre){
		return getRelations(word, relation, filtre, weightMinJdM, weightMaxJdM);
	}
	
	
	/**
	 * Requête de base sur la BD par RezoSQL.
	 * @param word : le mot sur lequel porte la requête
	 * @param relation : la relation choisie
	 * @param filtre : restriction sur les relations
	 * @return Une HashMap<String, Integer> ou String est la cible de la relation et Integer son poids.
	 */
	private HashMap<String, Integer> getWeightedRelation(String word, String relation, Filtre filtre, int weightMin, int weightMax) {
		HashMap<String, Integer> output = new HashMap<String, Integer>();
		
		//vérifie si le mot et la relation existent dans JdM
		if(!verifWordRel(word, relation)) {
			return output;
		}
		
		Resultat resultatRequete;
		Mot mot;
		List<Relation> voisins;
	
		try {
			resultatRequete = rezo.requete(word, relation, filtre);
			mot = resultatRequete.getMot();
			
			if(mot != null) {
				voisins = mot.getRelationsSortantesTypees(relation);
				for(Relation voisin : voisins) {
					if(voisin.getPoids()!=0 && voisin.getPoids() >= weightMin && voisin.getPoids() <= weightMax) {
						output.put(voisin.getNomDestination(), voisin.getPoids());
					}
				}
				voisins = mot.getRelationsEntrantesTypees(relation);
				for(Relation voisin : voisins) {
					if(voisin.getPoids()!=0 && voisin.getPoids() >= weightMin && voisin.getPoids() <= weightMax) {
						output.put(voisin.getNomSource(), voisin.getPoids());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	public List<Entry<String, Integer>> getSortedRelationsWithWeight(String word, String relation, Filtre filtre, int weightMin, int weightMax){
		HashMap<String, Integer> map = getWeightedRelation(word, relation, filtre, weightMin, weightMax);
        List<Entry<String, Integer>> list = new ArrayList<>(map.entrySet());

        Collections.sort(list, new Comparator<Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
                return (-(e1.getValue()).compareTo(e2.getValue()));
            }});
		
		return list;
	}
	
	private List<String> convertEntryToString(List<Entry<String, Integer>> input){
		List<String> output = new ArrayList<>();
		
		for(Entry<String, Integer> e: input) {
			output.add(e.getKey());
		}
		
		return output;
	}
	
	
	/**
	 * Renvoie les relations ayant un poids supérieur à une valeur donnée.
	 * @param word : le mot sur lequel porte la requête
	 * @param relation : la relation choisie
	 * @param filtre : restriction sur les relations
	 * @param weightMin : poids minimal de la relation (weightMin inclus)
	 * @return
	 */
	public List<String> getSortedRelationsWMin(String word, String relation, Filtre filtre, int weightMin){
		return convertEntryToString(
				getSortedRelationsWithWeight(word, relation, filtre, weightMin, weightMaxJdM));
	}
	
	/**
	 * Renvoie les relations ayant un poids positif.
	 * @param word : le mot sur lequel porte la requête
	 * @param relation : la relation choisie
	 * @param filtre : restriction sur les relations
	 * @return
	 */
	public List<String> getSortedRelationsWPos(String word, String relation, Filtre filtre, int weight){
		return convertEntryToString(
				getSortedRelationsWithWeight(word, relation, filtre, 1, weightMaxJdM));
	}
	
	/**
	 * Renvoie les relations ayant un poids négatif trié par ordre décroissant.
	 * @param word : le mot sur lequel porte la requête
	 * @param relation : la relation choisie
	 * @param filtre : restriction sur les relations
	 * @return
	 */
	public List<String> getSortedRelationsWNeg(String word, String relation, Filtre filtre){
		return convertEntryToString(
				getSortedRelationsWithWeight(word, relation, filtre, weightMinJdM, -1));
	}
	
	/**
	 * Renvoie toutes les relations triées par poids décroissant.
	 * @param word : le mot sur lequel porte la requête
	 * @param relation : la relation choisie
	 * @param filtre : restriction sur les relations
	 * @return
	 */
	public List<String> getAllSortedRelations(String word, String relation, Filtre filtre){
		return convertEntryToString(
				getSortedRelationsWithWeight(word, relation, filtre, weightMinJdM, weightMaxJdM));
	}

	public static void main(String[] args) {
		Rezo rezo = new Rezo();
		Set<String> output = rezo.getRelationsWPos("chat", "r_pos", Filtre.RejeterRelationsEntrantes);
		System.out.println(output);
	}
}
