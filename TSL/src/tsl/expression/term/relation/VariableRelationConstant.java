package tsl.expression.term.relation;

import tsl.expression.term.variable.Variable;

public class VariableRelationConstant extends RelationConstant {

	public VariableRelationConstant(String rname) {
		this.setName(rname);
	}

	public static VariableRelationConstant createVariableRelationConstant(
			String rname) {
		if (Variable.isVariable(rname)) {
			return new VariableRelationConstant(rname);
		}
		return null;
	}

}
