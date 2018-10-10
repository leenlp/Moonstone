package tsl.expression.term.function.logicfunction;

import java.util.Vector;

import tsl.expression.term.function.FunctionConstant;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.VUtils;

public class LogicFunctionConstant extends FunctionConstant {

	public LogicFunctionConstant(String fname) {
		this(VUtils.listify(fname));
	}
	
	public LogicFunctionConstant(Vector v) {
		super((String) v.firstElement());
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		Vector<Variable> vars = null;
		for (int i = 1; i < v.size(); i++) {
			String vname = (String) v.elementAt(i);
			Variable var = (Variable) kb.getTerm(this, vname);
			vars = VUtils.add(vars, var);
		}
		this.setVariables(vars);
	}
	
	public static LogicFunctionConstant createLogicFunctionConstant(Vector v) {
		if ("deflogicfunctionconstant".equals(v.firstElement())) {
			return new LogicFunctionConstant(v);
		}
		return null;
	}

}
