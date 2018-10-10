package tsl.expression.term.constant;

import tsl.expression.term.relation.RelationSentence;

public class SkolemConstant extends Constant {
	private RelationSentence source = null;
	
	public SkolemConstant(RelationSentence rs) {
		super();
		this.source = rs;
	}
	
	public RelationSentence getSource() {
		return source;
	}

	public void setSource(RelationSentence source) {
		this.source = source;
	}

	public SkolemConstant() {
		super();
	}
	
	public SkolemConstant(String name) {
		super(name);
	}

}
