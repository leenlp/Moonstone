package tsl.planner;

import java.util.Vector;

import tsl.expression.term.relation.RelationSentence;
import tsl.inference.InferenceEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class HTNTaskSentence extends RelationSentence {
	private HTNMethod method = null;

	public HTNTaskSentence(Vector v) {
		super(v);
	}

	public boolean preProofValidate() {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		if (this.getPrecondition() != null) {
			InferenceEngine ie = kb.getInferenceEngine();
			return ie.prove(this.getPrecondition(), null);
		}
		return true;
	}

	public HTNMethod getMethod() {
		return method;
	}

	public void setMethod(HTNMethod method) {
		this.method = method;
	}

}
