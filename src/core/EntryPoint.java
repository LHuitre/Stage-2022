package core;

import java.util.List;

import FFanalysis.ExtractRelations;
import graph.GraphAugmented;
import rulesGeneral.Rule;
import rulesGeneral.Interpretation;

public class EntryPoint {
	public static void main(String[] args) throws Exception {
		//String sentence = "Les petits chats boivent du lait de chèvre";
		//String sentence = "Le petit chat boit du lait";
		String sentence = "le chercheur a une idée brillante";
		//String sentence = "le gros chien noir";
		List<String> sentenceList = SplitText.splitSentence(sentence);
		
		GraphAugmented testGraph = new GraphAugmented();
		
		testGraph.initialisation(sentenceList);
		//ExtractRelations.afficheGraphe();
		
		testGraph.etiquetteLemma();
		//ExtractRelations.afficheGraphe();
		
		testGraph.etiquetteMWE();
		//ExtractRelations.afficheGraphe();
		
		//testGraph.etiquettePOS();
		//testGraph.afficheGraphe();
		
		testGraph.etiquetteRules("rules/relationsToEnforce.txt");
		
		//testGraph.afficheGraphe(true);
		testGraph.afficheGraphe(false);
		
		ExtractRelations test = new ExtractRelations(testGraph);
		System.out.println(test.getExtractedRelations());
		System.out.println(test.getHasLemma());
		System.out.println(test.getInMWE());
	}
}
