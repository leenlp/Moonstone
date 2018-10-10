package tsl.knowledge.constraint.temporal;

import java.util.Vector;

import tsl.expression.term.relation.RelationSentence;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class TemporalReasoner {
	private TemporalConstraintGraph temporalConstraintGraph = null;
	private KnowledgeBase knowledgeBase = null;

	public TemporalReasoner(KnowledgeBase kb) {
		this.temporalConstraintGraph = new TemporalConstraintGraph();
		this.knowledgeBase = kb;
	}

	public void processTemporalRelations() {

	}

	public Vector<RelationSentence> extractTemporalRelations() {
		return null;
	}

	private void extractEvents() {

	}

	private void addStatedTemporalRelations() {

	}

	private void addImpliedTemporalRelations() {

	}

	public TemporalConstraintGraph getTemporalConstraintGraph() {
		return temporalConstraintGraph;
	}

	public KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

}
