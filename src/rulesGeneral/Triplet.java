package rulesGeneral;

public class Triplet {
	private String filePath;
	private int numLine;
	
	private String sujet;
	
	private String predicat;
	private int minWeightPredicat;
	private int maxWeightPredicat;
	private String specialWeight;
	private boolean truthValue;
	
	private String objet;
	
	public Triplet(String triplet, String filePath, int numLine) {
		this.filePath = filePath;
		this.numLine = numLine;
		minWeightPredicat = 0;
		maxWeightPredicat = Integer.MAX_VALUE;
		specialWeight = "";
		
		String[] split = triplet.split(" ");
		sujet = split[0];
		parsePredicat(split[1]);
		objet = split[2];
	}
	
	public String getSujet() {
		return sujet;
	}

	public String getPredicat() {
		return predicat;
	}

	public int getMinWeightPredicat() {
		return minWeightPredicat;
	}

	public int getMaxWeightPredicat() {
		return maxWeightPredicat;
	}
	
	public String getSpecialWeight() {
		return specialWeight;
	}
	
	public String getObjet() {
		return objet;
	}

	public boolean getTruthValue() {
		return truthValue;
	}
	
	/**
	 * Méthode parsant le prédicat.
	 * Affecte les champs : predicat, minWeightPredicat, maxWeightPredicat, specialWeight et truthValue
	 * @param pred
	 */
	private void parsePredicat(String pred) {
		truthValue = !pred.startsWith("!"); //si le predicat commence par un ! alors sa valeur de vérité est à false, true sinon
		
		// Predicat sans restriction de poids
		if(pred.matches("!?[er]_" + RegexRule._CHAR_REL + "+")) {
			predicat = pred;
		}
		
		// Predicat avec restriction de poids
		else if(pred.matches("!?[er]_" + RegexRule._CHAR_REL + "+\\(.+\\)")) {
			predicat = pred.split("[\\(\\)]")[0];
			
			String param = pred.split("[\\(\\)]")[1];
			param.replace(" ", ""); //suppression des espaces
			
			int value;
			String message = String.format("Integer expected in weight restriction of relation in file %s at line %d.", filePath, numLine);
			
			// Vérification de la validité d'une restriction sur le poids
			if(param.matches("(<|<=|>|>=|==)?(max|MAX|min|MIN)")) {
				String newParam = param;
				if(newParam.contains("max")) { newParam.replace("max", "MAX"); }
				else if(newParam.contains("min")) { newParam.replace("min", "MIN"); }
				
				if(newParam.equals("<MIN") || newParam.equals(">MAX")) {
					String warningMessage = String.format("\nWARNING: weight restriction can't be <MIN or >MAX in file %s at line %d. Found \"%s\"."
							+ "\nThis restriction is ignored.", filePath, numLine, param);
					System.err.println(warningMessage);
					newParam = "";
				}
				else if(newParam.equals("<=MIN") || newParam.equals(">=MAX")) {
					String warningMessage = String.format("\nWARNING: weight restriction can't be <=MIN or >=MAX in file %s at line %d. Found \"%s\"."
							+ "\nThis restriction is kindly transformed in \"==\".", filePath, numLine, param);
					System.err.println(warningMessage);
					
					newParam = newParam.replaceAll("[<>]", "=");
					specialWeight = newParam;
				}
				else if(newParam.equals(">=MIN") || newParam.equals("<=MAX")) {
					String warningMessage = String.format("\nWARNING: weight restriction >=MIN or <=MAX in file %s at line %d. Found \"%s\"."
							+ "\nThis restriction is trivial and is ignored.", filePath, numLine, param);
					System.err.println(warningMessage);
					newParam = "";
				}
				else if(newParam.equals("!=MIN")) {
					newParam = ">MIN";
					specialWeight = newParam;
				}
				else if(newParam.equals("!=MAX")) {
					newParam = "<MAX";
					specialWeight = newParam;
				}
				else if(newParam.equals("MIN") || newParam.equals("MAX")) {
					newParam = "==" + newParam;
					specialWeight = newParam;
				}
				else {
					specialWeight = newParam;
				}
			}
			
			// Vérification et modification des bornes si un poids spécifique est donné
			
			else if(param.startsWith("<")) {
				String valStr = param.substring(1);
				try { value = Integer.parseInt(valStr); }
				catch(NumberFormatException e) { throw new IllegalArgumentException(message += String.format(" Found \"%s\" instead.", valStr)); }
				maxWeightPredicat = value - 1;
				if(value <= 0) {
					minWeightPredicat = Integer.MIN_VALUE;
				}
			}
			else if(param.startsWith("<=")) {
				String valStr = param.substring(2);
				try { value = Integer.parseInt(valStr); }
				catch(NumberFormatException e) { throw new IllegalArgumentException(message += String.format(" Found \"%s\" instead.", valStr)); }
				maxWeightPredicat = value;
				if(value < 0) {
					minWeightPredicat = Integer.MIN_VALUE;
				}
			}
			else if(param.startsWith(">")) {
				String valStr = param.substring(1);
				try { value = Integer.parseInt(valStr); }
				catch(NumberFormatException e) { throw new IllegalArgumentException(message += String.format(" Found \"%s\" instead.", valStr)); }
				minWeightPredicat = value + 1;
			}
			else if(param.startsWith(">=")) {
				String valStr = param.substring(2);
				try { value = Integer.parseInt(valStr); }
				catch(NumberFormatException e) { throw new IllegalArgumentException(message += String.format(" Found \"%s\" instead.", valStr)); }
				minWeightPredicat = value;
			}
			else if(param.startsWith("==")) {
				String valStr = param.substring(2);
				try { value = Integer.parseInt(valStr); }
				catch(NumberFormatException e) { throw new IllegalArgumentException(message += String.format(" Found \"%s\" instead.", valStr)); }
				minWeightPredicat = value;
				maxWeightPredicat = value;
			}
			else {
				try { value = Integer.parseInt(param); }
				catch(NumberFormatException e) { throw new IllegalArgumentException(message += String.format(" Found \"%s\" instead.", param)); }
				minWeightPredicat = value;
				maxWeightPredicat = value;
			}
		}
		
		// Suppression du ! au début du prédicat
		if(!truthValue) {
			predicat = predicat.substring(1);
		}
	}
	
	public String toString() {
		if(truthValue) {
			return "("+sujet+";"+predicat+";"+objet+")";
		}
		else {
			return "("+sujet+";"+predicat+";"+objet+";"+truthValue+")";
		}
	}

	public static void main(String[] args) {
		Triplet test = new Triplet("$x !r_pos $y", "ExtractRelations.txt", 1);
		System.out.println(test.getPredicat());
		System.out.println(test.getTruthValue());
	}
}
