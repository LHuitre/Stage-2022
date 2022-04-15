package rulesGeneral;

import java.util.regex.Pattern;

public class RegexRule {
	public static final String _AFFECTATION = "=";
	public static final String _EQUAL = "==";
	public static final String _DIFFERENT = "!=";
	public static final String _END_OF_DECL = ";";
	public static final String _SEP_LIST = ",";
	public static final String _SEP_TRIPLET = "&";
	public static final String _QUOTE = "\"";
	
	public static final String _CHAR_VAR = "\\w";
	public static final String _CHAR_REL = "[a-z0-9_>/\\-]";
	public static final String _CHAR_CONST = "[^\"\t\r\n]";
	
	
	
	public static final String COMPIL_VAR = "\\$\\$" + _CHAR_VAR + "+";
	public static final String SIMPLE_VAR = "\\$" + _CHAR_VAR + "+";

	public static final String TYPE_VAR = "[A-Z_]+";
	
	public static final String NODE_NAME = "@"+_CHAR_VAR+"+";
	
	public static final String CONSTANT = "\""+ _CHAR_CONST +"*\"";
	public static final String LIST_CONSTANT = "\\[ *("+ CONSTANT +" *, *)*"+ CONSTANT +" *\\]";
	
	public static final String RELATION = "!?[er]_" + _CHAR_REL + "+";
	
	//public static final String DISJ_CONST_PERSO = "#[A-Z0-9_]+";
	public static final String FUNCTION = "#" + _CHAR_VAR + "+\\([^\"\r\n]*\\)";
	
	
	
	public static boolean isCompilVar(String str) {
		return Pattern.compile("^"+COMPIL_VAR+"$").matcher(str).find();
	}
	
	public static boolean isSimpleVar(String str) {
		return Pattern.compile("^"+SIMPLE_VAR+"$").matcher(str).find();
	}
	
	public static boolean isTypeVar(String str) {
		return Pattern.compile("^"+TYPE_VAR+"$").matcher(str).find();
	}
	
	public static boolean isNodeName(String str) {
		return Pattern.compile("^"+NODE_NAME+"$").matcher(str).find();
	}
	
	public static boolean isConstant(String str) {
		return Pattern.compile("^"+CONSTANT+"$").matcher(str).find();
	}
	
	public static boolean isListConst(String str) {
		return Pattern.compile("^"+LIST_CONSTANT+"$").matcher(str).find();
	}
	
	public static boolean isRelation(String str) {
		return Pattern.compile("^"+RELATION+"$").matcher(str).find();
	}
	
	public static boolean isFunction(String str) {
		return Pattern.compile("^"+FUNCTION+"$").matcher(str).find();
	}
}
