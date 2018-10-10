package moonstone.learning.ebl;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import moonstone.annotation.Annotation;
import moonstone.grammar.Grammar;
import moonstone.rule.Rule;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.documentanalysis.document.Document;
import tsl.expression.form.sentence.constraint.Constraint;
import tsl.expression.term.Term;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.relation.PatternRelationSentence;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.engine.StartupParameters;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.tsllisp.Sexp;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.TimeUtils;
import tsl.utilities.VUtils;
import utility.UnixFormat;

public class GrammarEBLCompilerSpecialization extends GrammarEBL {

	private Hashtable<Object, Vector<Rule>> redundantPatternTestHash = new Hashtable(1000000);
	private boolean suppressNewRulesIfMatchExists = true;

	public GrammarEBLCompilerSpecialization(MoonstoneRuleInterface msri) {
		super(msri);
	}
	
	public void analyzeCorpusFiles() {
		this.analyzingCorpusFiles = true;
		this.gatherPatternToDepth = false; // TEST
		this.suppressNewRulesIfMatchExists = true; // TEST -- allow multiples
		this.documentIndex = 0;
		if (this.documentFiles != null) {
			for (File file : this.documentFiles) {
				this.processNextCorpusFile(file);
			}
		}
		System.out.println("FINISHED: Number of new rules=" + this.newEBLGrammarRules.size());
		this.storeEBLGrammarRules();
		this.clear();
		this.documentFiles = null;
	}

	protected void processNextCorpusFile(File file) {
		Vector<Annotation> sas = null;
		System.out.println("\nGrammarEBL: Processing \"" + file.getName() + "\"...");
		String text = FUtils.readFile(file);
		try {
			text = UnixFormat.convertToUnixFormat(text);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Document doc = new Document(this.currentDocumentName, text);
		sas = this.moonstoneRuleInterface.getControl().applyNarrativeGrammarToText(doc, true, true, true);
		if (sas != null) {
			for (Annotation annotation : sas) {
				Vector<Annotation> complexAnnotations = getAllComplexConceptAnnotations(annotation);
				if (complexAnnotations != null) {
					if (complexAnnotations.size() > 1) {
						int x = 2;
					}
					for (Annotation child : complexAnnotations) {
						Vector<Rule> newrules = this.generateSnapshotRules(child, null, true);
						this.newEBLGrammarRules = VUtils.append(this.newEBLGrammarRules, newrules);
					}
				}
			}
		}
		this.moonstoneRuleInterface.releaseAnnotations();
	}

	public void printNewRulesForSelectedAnnotation(Annotation annotation) {
		this.gatherPatternToDepth = false;
		this.suppressNewRulesIfMatchExists = false;
		Vector<Annotation> annotations = getAllComplexConceptAnnotations(annotation);
		if (annotations != null) {
			System.out.println("'(\nwordrules\n\n");
			for (Annotation ca : annotations) {
				Vector<Rule> newrules = this.generateSnapshotRules(ca, null, true);
				if (this.newEBLGrammarRules != null) {
					for (Rule rule : this.newEBLGrammarRules) {
						System.out.println(rule.getSexp().toNewlinedString() + "\n");
					}
				}
			}
			System.out.println(")\n");
		}
	}

	protected Vector<Rule> generateSnapshotRules(Annotation annotation, String dateString, boolean attop) {
		Rule rule = annotation.getRule();
		KnowledgeBase kb = rule.getKnowledgeBase();
		Grammar sgrammar = this.moonstoneRuleInterface.getSentenceGrammar();
		if (dateString == null) {
			dateString = TimeUtils.getDateTimeString();
		}
		Document doc = annotation.getDocument();
		Rule newrule = annotation.getRule().clone();
		Vector<Rule> newrules = null;
		Vector<Vector> patternLists = null;
		newrule.setSpecialized(true);
		if (!attop) {
			newrule.setIntermediate(true);
		}

		newrule.setSourceID(rule.getRuleID());
		newrule.setExampleSnippet(StrUtils.removeNonAlphaDigitSpaceCharacters(annotation.getText()));
		StringConstant concept = (StringConstant) annotation.getConcept();
		newrule.setResultConcept(concept);
		
		TypeConstant newtype = null;
		
		if (concept.getType() != null && !concept.getType().isUndefined() && !concept.getType().isRoot()) {
			newtype = concept.getType();
		}
		if (newtype == null && rule.getType() != null) {
			TypeConstant rtype = rule.getType();
			if (!rtype.isGrammarType()) {
				newtype = rtype;
			} else {
				TypeConstant rbtype = rtype.getFirstBaseType();
				newtype = rbtype;
			}
		}
		if (newtype == null 
				&& rule.getSourceRule() != null && rule.getSourceRule().getType() != null) {
			TypeConstant rstype = rule.getSourceRule().getType();
			if (!rstype.isGrammarType()) {
				newtype = rstype;
			} else {
				TypeConstant rbtype = rstype.getFirstBaseType();
				newtype = rbtype;
			}
		}

		newrule.setType(newtype);
		
		Vector<Vector<String>> tvs = new Vector(0);
		for (int i = 0; i < rule.getPatternListCount(); i++) {
			String ctoken = null;
			Annotation child = annotation.getLexicalChild(i);
			String vname = "?" + i;
			String tname = (child.isNegated() ? "neg" : "notneg");
			Vector<String> tv = new Vector(0);
			tv.add(tname);
			tv.add(vname);
			tvs.add(tv);
			Vector<String> plst = rule.getPatternLists().elementAt(i);
			if (child.getRule() != null) {
				if (!this.gatherPatternToDepth && child.hasConcept()) {
					ctoken = this.getAnnotationPatternToken(child, plst);
				} else if (isIncludableSingletonAnnotation(child) || child.getRule().isBagOfConceptsRule()) {
					ctoken = child.getRule().getRulenamePatternToken();
				} else {
					Vector<Rule> ncr = generateSnapshotRules(child, dateString, false);
					if (ncr != null) {
						Rule firstcrule = ncr.firstElement();
						Vector<Rule> oldcrules = sgrammar.getRulesFromPatternHash(firstcrule);
						if (oldcrules != null) {
							firstcrule = oldcrules.firstElement();
						}
						ctoken = firstcrule.getRulenamePatternToken();
						newrules = VUtils.append(newrules, ncr);
					} else {
						ctoken = child.getRule().getRulenamePatternToken();
					}
				}
			} else {
				ctoken = this.getAnnotationPatternToken(child, plst);
			}
			patternLists = VUtils.add(patternLists, VUtils.listify(ctoken));
		}
		newrule.setPatternLists(patternLists);

		Vector<Constraint> tps = null;
		
		// 7/18/2018
		if (rule.getTestPredicates() != null) {
//			tps = new Vector(rule.getTestPredicates());
		}
		
		for (Vector<String> v : tvs) {
			Constraint pt = Constraint.createConstraint(kb, v);
			tps = VUtils.add(tps, pt);
		}
		newrule.setTestPredicates(tps);
		String newruleid = getNewRuleID(rule, concept, dateString);
		newrule.setRuleID(newruleid);
		Sexp sexp = newrule.toSexp();
		newrule.setSexp(sexp);

		boolean foundmatch = false;
		if (this.suppressNewRulesIfMatchExists) {
			Vector<Rule> m1 = this.redundantPatternTestHash.get(patternLists);
			Vector<Rule> m2 = sgrammar.getRulesFromPatternHash(newrule);
			Vector<Rule> matching = VUtils.append(m1, m2);
			if (matching != null) {
				for (Rule match : matching) {
					String s1 = match.getSexp().toNewlinedString();
					String s2 = newrule.getSexp().toNewlinedString();
					if (newrule.contentEquals(match)) {
						foundmatch = true;
						int x = 2;
						break;
					}
				}
			}
		}
		if (!foundmatch) {
			VUtils.pushHashVector(this.redundantPatternTestHash, patternLists, newrule);
			this.newEBLGrammarRules = VUtils.add(this.newEBLGrammarRules, newrule);
			System.out.print(".");
		} else {
			int x = 2;
		}
		return newrules;
	}

	public void storeEBLGrammarRules() {
		MoonstoneRuleInterface msri = this.moonstoneRuleInterface;
		String dateString = TimeUtils.getDateTimeString();
		dateString = StrUtils.replaceNonAlphaNumericCharactersWithDelim(dateString, '_');
		String fpath = msri.getSentenceGrammar().getRuleDirectory() + File.separatorChar + "CompiledGrammar_"
				+ dateString;
		if (this.newEBLGrammarRules != null) {
			StringBuffer sb = new StringBuffer();
			sb.append("'(\nwordrules\n\n");
			for (Rule rule : this.newEBLGrammarRules) {
				sb.append(rule.getSexp().toNewlinedString() + "\n");
			}
			sb.append(")\n");
			FUtils.writeFile(fpath, sb.toString());
		}
	}

	protected String getNewRuleID(Rule rule, Object concept, String dateString) {
		int numrules = (this.newEBLGrammarRules == null ? 0 : this.newEBLGrammarRules.size());
		String newruleid = "CG:" + concept + "_" + numrules;
		return newruleid;
	}

	private Vector<Annotation> getAllComplexConceptAnnotations(Annotation annotation) {
		Vector<Annotation> v = new Vector(0);
		getAllComplexConceptAnnotations(annotation, v);
		return v;
	}

	// 7/18/2018:  Some complex rules produce non-complex concepts, e.g. "have observed".  I also want
	// those in my compiled grammar
	private void getAllComplexConceptAnnotations(Annotation annotation, Vector<Annotation> v) {
		if (annotation.getConcept() != null && annotation.getConcept() instanceof StringConstant) {
			Rule rule = annotation.getRule();
			StringConstant sc = (StringConstant) annotation.getConcept();
			if (rule.isComplexConcept() && sc.getNumberOfColons() <= 10) {
				v = VUtils.add(v, annotation);
			}
		}
		if (annotation.hasChildren()) {
			for (Annotation child : annotation.getChildAnnotations()) {
				getAllComplexConceptAnnotations(child, v);
			}
		}
	}

}
