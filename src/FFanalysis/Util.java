package FFanalysis;

import java.util.HashSet;
import java.util.Set;

import core.Rezo;
import requeterrezo.Filtre;

public class Util {
	private Rezo rezo;
	
	public Util(Rezo rezo) {
		this.rezo = rezo;
	}
	
	private Set<String> testVariantsSubject(Triplet triplet, String variantRelSubject){
		
		Set<String> knownJdM = rezo.getRelationsWPos(triplet.getObjet(), triplet.getPredicat(), Filtre.RejeterRelationsSortantes);
		Set<String> variantToTest = rezo.getRelationsWPos(triplet.getSujet(), variantRelSubject, Filtre.RejeterRelationsEntrantes);
		
		knownJdM.retainAll(variantToTest);
		
		HashSet<String> output = new HashSet<>(knownJdM);
		return output;
	}
	
	private Set<String> testVariantsObject(Triplet triplet, String variantRelObject){
		
		Set<String> knownJdM = rezo.getRelationsWPos(triplet.getSujet(), triplet.getPredicat(), Filtre.RejeterRelationsEntrantes);
		Set<String> variantToTest = rezo.getRelationsWPos(triplet.getObjet(), variantRelObject, Filtre.RejeterRelationsEntrantes);
		
		knownJdM.retainAll(variantToTest);
		
		HashSet<String> output = new HashSet<>(knownJdM);
		return output;
	}
	
	private Set<String> getAllLemma(String word, String pos){
		HashSet<String> output = new HashSet<>();
		Set<String> lemma = rezo.getRelationsWPos(word, "r_lemma", Filtre.RejeterRelationsEntrantes);
		for(String lem: lemma) {
			Set<String> posLem = rezo.getRelationsWPos(lem, "r_pos", Filtre.RejeterRelationsEntrantes);
			if(posLem.contains(pos)) {
				output.add(lem);
			}
		}
		return output;
	}
	
	private Set<String> getFlexGenNum(String word, String gender, String number) {
		HashSet<String> output = new HashSet<>();
		Set<String> flexions = rezo.getRelationsWPos(word, "r_lemma", Filtre.RejeterRelationsSortantes);
		for(String flex: flexions) {
			Set<String> posFlex = rezo.getRelationsWPos(flex, "r_pos", Filtre.RejeterRelationsEntrantes);
			if(posFlex.contains(gender) && posFlex.contains(number)) {
				output.add(flex);
			}
		}
		return output;
	}
	
	private Set<String> testVariantsObjectLem(Triplet triplet, String variantRelObject){
		
		Set<String> knownJdM = rezo.getRelationsWPos(triplet.getSujet(), triplet.getPredicat(), Filtre.RejeterRelationsEntrantes);
		HashSet<String> variantToTest = new HashSet<>();
		for(String lem: getAllLemma(triplet.getObjet(), "Adj:")) {
			variantToTest.addAll(rezo.getRelationsWPos(lem, variantRelObject, Filtre.RejeterRelationsEntrantes));
		}
		
		
		knownJdM.retainAll(variantToTest);
		
		HashSet<String> output = new HashSet<>(knownJdM);
		return output;
	}
	
	public static void main(String[] args) {
		Util test = new Util(new Rezo());
		Set<String> res = test.testVariantsObject(new Triplet("chien", "r_carac", "gros"), "r_syn");
		System.out.println(res);
	}
}
