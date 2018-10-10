package tsl.inference;

import java.util.Vector;

import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.relation.RelationSentence;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public abstract class InferenceEngine {

	private KnowledgeBase knowledgeBase = null;
	private Sentence querySentence = null;
	public int proofCount = 0;
	
	public InferenceEngine() {
	}

	public InferenceEngine(KnowledgeBase kb) {
		this.knowledgeBase = kb;
	}

	public void initialize() {
		this.setProofCount(0);
	}

	public boolean prove(Sentence sentence,
			Vector<RelationSentence> localSentences) {
		return false;
	}

	public void initializeQuerySentence(KnowledgeBase kb, Sentence sentence,
			Vector binds) {
	}

	public Vector unpackValidatedBindings(boolean unpack) {
		return null;
	}

	public KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

	public void setKnowledgeBase(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	public int getProofCount() {
		return proofCount;
	}

	public void setProofCount(int pc) {
		this.proofCount = pc;
	}

	public void incrementProofCount() {
		proofCount++;
	}

	public Sentence getQuerySentence() {
		return querySentence;
	}

	public void setQuerySentence(Sentence querySentence) {
		this.querySentence = querySentence;
	}

}
