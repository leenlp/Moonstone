package tsl.knowledge.constraint.temporal;

import java.util.Vector;

import tsl.expression.term.constant.Constant;
import tsl.expression.term.relation.RelationSentence;
import tsl.knowledge.constraint.ConstraintGraph;

public class TemporalConstraintGraph extends ConstraintGraph {

	private static String[] temporalRelations = {};
	private static String[][] consistentTemporalRelations = {};
	
	private void enforceConsistency(Constant startEvent) {

	}

	public Vector<RelationSentence> extractTemporalRelations() {
		Vector<RelationSentence> relations = null;
		return relations;
	}

}
