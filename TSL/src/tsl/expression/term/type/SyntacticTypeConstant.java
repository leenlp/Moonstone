package tsl.expression.term.type;

import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class SyntacticTypeConstant extends TypeConstant {

	public static SyntacticTypeConstant NOUN_SYNTACTIC_TYPE_CONSTANT = createSyntacticTypeConstant("#NOUN#");
	public static SyntacticTypeConstant NUMBER_SYNTACTIC_TYPE_CONSTANT = createSyntacticTypeConstant("#NUMBER#");

	public SyntacticTypeConstant(String name) {
		super(name);
	}

	public static SyntacticTypeConstant createSyntacticTypeConstant(String name) {
		SyntacticTypeConstant stype = createSyntacticTypeConstant(name, null);
		return stype;
	}

	public static SyntacticTypeConstant createSyntacticTypeConstant(String name, String fullName) {
		SyntacticTypeConstant tc = null;
		try {
			if (name != null) {
				KnowledgeBase kb = KnowledgeEngine.getCurrentKnowledgeEngine().getCurrentKnowledgeBase();
				if (fullName == null) {
					fullName = name;
				}
				tc = (SyntacticTypeConstant) kb.getNameSpace().getSyntacticTypeConstant(name);
				if (tc == null) {
					tc = new SyntacticTypeConstant(name);
					tc.setFullName(fullName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tc;
	}

	// e.g. "<DISEASE_OR_SYMPTOM>", "#COMPLEX_EVENT#
	public static boolean isSyntacticTypeConstantFormalName(String str) {
		if (str != null && str.length() > 4 && str.charAt(0) == '#' && str.charAt(str.length() - 1) == '#'
				&& Character.isUpperCase(str.charAt(1))) {
			for (int i = 2; i < str.length() - 1; i++) {
				char c = str.charAt(i);
				if (!(Character.isUpperCase(c) || c == '_' || c == '-' || c == ' ' || c == ':' || c == '.')) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public String getFormalName() {
		if (this.formalName == null && this.name != null) {
			this.formalName =  generateFormalName(this.name, "#", "#");
		}
		return this.formalName;
	}
}
