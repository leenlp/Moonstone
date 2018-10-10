package tsl.network;

import tsl.expression.term.relation.RelationConstant;

public class RelationEdge {
	private int sentenceInstanceID = 0;
	private RelationConstant relationConstant = null;
	private RelationNode subjectTerm = null;
	private RelationNode modifierTerm = null;

	// private Jung Edge

	public RelationEdge(int sid, RelationConstant rc, RelationNode subject,
			RelationNode modifier) {
		this.sentenceInstanceID = sid;
		this.relationConstant = rc;
		this.subjectTerm = subject;
		this.modifierTerm = modifier;
	}

	public int getSentenceInstanceID() {
		return sentenceInstanceID;
	}

	public RelationConstant getRelationConstant() {
		return relationConstant;
	}

	public RelationNode getSubjectTerm() {
		return subjectTerm;
	}

	public RelationNode getModifierTerm() {
		return modifierTerm;
	}

}
