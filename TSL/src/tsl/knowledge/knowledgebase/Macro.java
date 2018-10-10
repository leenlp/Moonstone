package tsl.knowledge.knowledgebase;

import java.util.Vector;

import tsl.expression.term.variable.Variable;
import tsl.utilities.VUtils;

public class Macro {

	private String name = null;
	private Vector<Variable> variables = null;
	private Vector definition = null;

	public Macro(Vector v) {
		this.name = (String) v.elementAt(1);
		Vector<String> vnames = (Vector<String>) v.elementAt(2);
		this.variables = Variable.createVariables(vnames);
		this.definition = (Vector) v.elementAt(3);
	}

	public static Macro createMacro(Vector v) {
		if (isMacroDefinition(v)) {
			return new Macro(v);
		}
		return null;
	}

	public Object expand(Object item) {
		if (Variable.isVariable(item)) {
			Variable var = Variable.find(this.variables, (String) item);
			if (var != null) {
				return var.getValue();
			} else {
				return item;
			}
		} else if (item instanceof Vector) {
			Vector v = new Vector(0);
			for (Object o : (Vector) item) {
				Object rv = expand(o);
				v = VUtils.add(v, rv);
			}
			return v;
		}
		return item;
	}

	public static boolean isMacroDefinition(Vector v) {
		return (v != null && v.size() == 4
				&& "defmacro".equals(v.firstElement())
				&& v.elementAt(1) instanceof String
				&& Variable.isVariable(v.elementAt(2)) && v.elementAt(3) instanceof Vector);
	}

	public String getName() {
		return name;
	}

	public Vector<Variable> getVariables() {
		return this.variables;
	}
	
	

	public void setVariables(Vector<Variable> variables) {
		this.variables = variables;
	}

	public Vector getDefinition() {
		return definition;
	}
	
	

}
