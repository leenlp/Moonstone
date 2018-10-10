package moonstone.rule;

import java.util.Vector;

import moonstone.annotation.AnnotationIntegrationMaps;
import tsl.expression.form.sentence.ImplicationSentence;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLUtils;

public class InferenceRule extends Rule {
	private ImplicationSentence implicationSentence = null;

	public InferenceRule(KnowledgeBase kb, Sexp sexp) {
		kb.clearFields();
		this.setPattern(TLUtils.convertSexpToJVector(sexp));
		this.setPropertiesFromPattern(this.getPattern());
		this.setRuleType("inferencerule");
		String idstr = this.getStringProperty("ruleid");
		this.setRuleID(idstr);
		Sexp s = (Sexp) sexp.getSecond();
		Vector sv = TLUtils.convertSexpToJVector(s);
		this.implicationSentence = (ImplicationSentence) ImplicationSentence
				.createImplicationSentence(sv);
		this.implicationSentence.setName(this.getRuleID());
		this.implicationSentence.setSexp(sexp);
		this.implicationSentence.setContainingObject(this);
		kb.initializeAndAddForm(this.implicationSentence);
	}
	
	public InferenceRule(KnowledgeBase kb, Sexp sexp, String idstr) {
		kb.clearFields();
		this.setRuleType("inferencerule");
		this.setRuleID(idstr);
		Vector sv = TLUtils.convertSexpToJVector(sexp);
		this.implicationSentence = (ImplicationSentence) ImplicationSentence
				.createImplicationSentence(sv);
		this.implicationSentence.setSexp(sexp);
		this.implicationSentence.setContainingObject(this);
		kb.initializeAndAddForm(this.implicationSentence);
	}

	public static InferenceRule createInferenceRule(KnowledgeBase kb, Sexp sexp) {
		if (isInferenceRule(sexp)) {
			return new InferenceRule(kb, sexp);
		}
		return null;
	}

	public static boolean isInferenceRule(Sexp sexp) {
		return (sexp.getLength() == 2 && TLUtils.isCons(sexp.getSecond()) && ImplicationSentence
				.isImpllicationSentence((Sexp) sexp.getSecond()));
	}

	public ImplicationSentence getImplicationSentence() {
		return implicationSentence;
	}

	public String getConsequentConcept() {
		if (this.getImplicationSentence() != null
				&& this.getImplicationSentence().getConsequent() != null) {
			RelationSentence consequent = this.getImplicationSentence()
					.getConsequentRelationSentence();
			if (consequent.getTermCount() > 0) {
				for (Object o : consequent.getTerms()) {
					if (o instanceof ObjectConstant) {
						ObjectConstant uo = (ObjectConstant) o;
						String str = uo.getObject().toString();
						if (AnnotationIntegrationMaps.getCUI(str) != null) {
							return str;
						}
					}
				}
			}
		}
		return null;
	}

}
