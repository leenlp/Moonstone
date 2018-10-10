package tsl.expression.term.template;

import java.util.Vector;

import tsl.expression.term.constant.Constant;
import tsl.expression.term.variable.Variable;

// I use the Term's variables to store parameters, but sometimes need to know that a Term is
// being used as a Template of a Type; hence the specialization.

public class Template extends Constant {
	
	public Template(String name) {
		super(name);
	}
	
	public Vector<Variable> getParameters() {
		return this.getVariables();
	}

}
