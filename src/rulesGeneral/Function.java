package rulesGeneral;

import java.util.ArrayList;
import java.util.List;

import core.FunctionsDeclaration;

public class Function {
	private String name;
	private List<String> args;
	
	private String filePath;
	private int numLine;
	
	public Function(String function, String filePath, int numLine) {
		this.filePath = filePath;
		this.numLine = numLine;
		
		name = function.split("\\(", 2)[0];
		if(!FunctionsDeclaration.functions.contains(name)) {
			String message = String.format("Unknown function \"%s\" in file %s at line %d.", name, filePath, numLine);
			throw new IllegalArgumentException(message);
		}
		
		String argsString = function.split("\\(", 2)[1];
		argsString = argsString.substring(0, argsString.length()-1);
		
		args = splitParam(argsString);
		verifyArgs(name, args);
	}
	
	public String getName() {
		return name;
	}

	public List<String> getArgs() {
		return args;
	}

	private List<String> splitParam(String argsString) {
		List<String> output = new ArrayList<>();
		
		boolean isInQuotes = false;
		String acc = "";
		for(char c: argsString.toCharArray()) {
			if(c==',' && !isInQuotes) {
				acc = acc.trim();
				output.add(acc);
				acc = "";
			}
			
			else if(c=='"') {
				isInQuotes = !isInQuotes;
				acc+=c;
			}
			else {
				acc+=c;
			}
		}
		
		acc = acc.trim();
		output.add(acc);
		acc = "";
		
		return output;
	}
	
	private void verifyArgs(String name, List<String> args) {
		switch(name) {
		case "#connect":
			if(args.size() < 2 || args.size() > 3) {
				String message = String.format("Unvalid argument arrity for function \"connect\" in file %s at line %s. "
						+ "Expected 2 or 3 arguments, found %d.", filePath, numLine, args.size());
				throw new IllegalArgumentException(message);
			}
			if(!RegexRule.isSimpleVar(args.get(0))) {
				String message = String.format("Unvalid first argument type for function \"connect\" in file %s at line %s. "
						+ "Variable expected, found %s.", filePath, numLine, args.get(0));
				throw new IllegalArgumentException(message);
			}
			if(!RegexRule.isNodeName(args.get(1))) {
				String message = String.format("Unvalid second argument type for function \"connect\" in file %s at line %s. "
						+ "Node expected, found %s.", filePath, numLine, args.get(1));
				throw new IllegalArgumentException(message);
			}
			if(args.size() == 3) {
				if(!args.get(2).matches("^r_"+RegexRule._CHAR_REL+"+$")) {
					String message = String.format("Unvalid third argument type for function \"connect\" in file %s at line %s. "
							+ "Expected valid relation name, found %s.", filePath, numLine, args.get(1));
					throw new IllegalArgumentException(message);
				}
			}
			break;
		
		case "#weight":
			if(args.size() != 2) {
				String message = String.format("Unvalid argument arrity for function \"weight\" in file %s at line %s. "
						+ "Expected 2 arguments, found %d.", filePath, numLine, args.size());
				throw new IllegalArgumentException(message);
			}
			if(!RegexRule.isSimpleVar(args.get(0))) {
				String message = String.format("Unvalid second argument type for function \"weight\" in file %s at line %s. "
						+ "Expected a variable, found %s.", filePath, numLine, args.get(0));
				throw new IllegalArgumentException(message);
			}
			try {
				Integer.parseInt(args.get(1));
			}
			catch(NumberFormatException e) {
				String message = String.format("Unvalid first argument type for function \"weight\" in file %s at line %s. "
						+ "Expected an integer, found %s.", filePath, numLine, args.get(1));
				throw new IllegalArgumentException(message);
			}
			break;
		}
	}
	
 	public String toString() {
		String argsToString = args.toString();
		return String.format("%s(%s)", name, argsToString.substring(1, argsToString.length()-1));
	}
}
