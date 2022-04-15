package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplitText {
	
	public static List<String> splitSentence(String sentence) {
		sentence = sentence.replace(".", " .");
		sentence = sentence.replace(",", " ,");
		sentence = sentence.replace("'", "' ");
		
		String[] sentenceSplit = sentence.split("\\s+");
		return new ArrayList<String>(Arrays.asList(sentenceSplit));
	}
}
