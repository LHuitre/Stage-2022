package rulesGeneral;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import core.RuleGeneralApplication;

public class Interpretation {
	String userFilePath;
	String compiledFilePath;
	
	public Interpretation(String filePath) {
		userFilePath = filePath;
		
		TraductionFinal trad = new TraductionFinal(filePath);
		compiledFilePath = trad.trad();
	}
	
	public List<Rule> interpret() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(compiledFilePath, StandardCharsets.UTF_8));
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
		
		List<Rule> output = new ArrayList<>();
		for(List<String> rule: rulesStr) {
			output.add(new Rule(rule, userFilePath));
		}
		
		return output;
	}
	
	/*public static void main(String[] args) {
		Interpretation testInterpret = new Interpretation("rules/ExtractRelations.txt");
		List<Rule> rules = testInterpret.interpret();
		
		
		/*for(Rule r: rules) {
			System.out.println("-----------------------------------------------------");
			System.out.println(r.getUserFilePath());
			System.out.println(r.getNameRule());
			System.out.println();
			System.out.println(r.getTypeDeclaration());
			System.out.println(r.getNodeDeclaration());
			System.out.println();
			System.out.println(r.getTripletBody());
			System.out.println(r.getEqualityBody());
			System.out.println(r.getFunctionBody());
			System.out.println();
			System.out.println(r.getTripletHead());
			System.out.println(r.getFunctionHead());
			System.out.println();
		}
	}*/
}
