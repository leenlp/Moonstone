package tsl.expression.form.definition;

import java.util.Vector;

import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class DefineGlobalVariable extends Definition {
	private String vname = null;
	private ObjectConstant term = null;
	
	public DefineGlobalVariable(Vector v) {
		super();
		this.vname = (String) v.elementAt(1);
		this.term = new ObjectConstant(v.elementAt(2));
	}
	
	public Object eval() {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		kb.addGlobalVariable(this.vname, this.term);
		return this.term;
	}
	
	public static DefineGlobalVariable createDefineGlobalVariable(Vector v) {
		if (isDefineGlobalVariable(v)) {
			return new DefineGlobalVariable(v);
		}
		return null;
	}
	
	// (defvar ?*high-level-words* '("high" "severe" ...))
	public static boolean isDefineGlobalVariable(Vector v) {
		if (v != null && v.size() == 3 && "defvar".equals(v.firstElement())
				&& v.elementAt(1) instanceof String) {
			String vname = (String) v.elementAt(1);
			if (Variable.isVariable(vname) && vname.charAt(1) == '*') {
				return true;
			}
		}
		return false;
	}

}
