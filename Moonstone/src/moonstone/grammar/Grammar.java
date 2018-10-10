package moonstone.grammar;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import moonstone.annotation.Annotation;
import moonstone.annotation.StructureAnnotation;
import moonstone.annotation.WordSequenceAnnotation;
import moonstone.annotation.TerminalAnnotation;
import moonstone.information.Information;
import moonstone.rule.InferenceRule;
import moonstone.rule.LabelRule;
import moonstone.rule.Rule;
import moonstone.rule.RuleExpansion;
import moonstone.rule.RuleUsage;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import moonstone.semantic.TypeInfo;
import tsl.documentanalysis.document.Header;
import tsl.documentanalysis.document.Sentence;
import tsl.documentanalysis.tokenizer.Token;
import tsl.documentanalysis.tokenizer.regexpr.RegExprManager;
import tsl.expression.form.Form;
import tsl.expression.form.sentence.ImplicationSentence;
import tsl.expression.form.sentence.constraint.Constraint;
import tsl.expression.term.Term;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypeConstant;
import tsl.information.TSLInformation;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.ontology.Ontology;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLUtils;
import tsl.tsllisp.TLisp;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public abstract class Grammar {

	protected GrammarModule grammarModule = null;
	protected KnowledgeEngine knowledgeEngine = null;
	protected String ruleDirectory = null;
	protected boolean useOnlyCoreConceptAnnotations = false;
	protected TypeInfo typeInfo = null;
	protected String name = null;

	protected Hashtable<String, Vector<Rule>> fileRuleHash = new Hashtable();
	protected Hashtable<String, Vector<Rule>> ruleTokenHash = null;

	protected Hashtable<Object, Vector<Rule>> rulePatternHash = null;
	protected Hashtable<String, Vector<Rule>> actionRuleTokenHash = null;
	protected Hashtable<String, Vector<RuleExpansion>> ruleExpansionHash = null;
	protected Hashtable<Rule, Rule> invalidatedRuleHash = null;
	protected Hashtable<String, Annotation> annotationSignatureHash = null;
	protected Hashtable<String, Vector<Rule>> stopWordRuleHash = null;
	protected Vector<Rule> allRules = null;
	protected Vector<InferenceRule> allInferenceRules = null;
	protected Vector<Annotation> allAnnotations = null;
	protected Vector<Annotation> displayedAnnotations = null;
	protected Vector<Vector<Annotation>> interpretedAnnotationChart = null;
	protected int currentAgendaIndex = 0;
	protected WordSequenceAnnotation sentenceAnnotation = null;
	protected Vector<Vector<RuleExpansion>> newAgendas = null;
	protected boolean doDebug = false;
	protected Hashtable<String, Annotation> largestCoverageAnnotationHash = null;
	protected Hashtable targetConceptHash = new Hashtable();
	protected Hashtable<StringConstant, Vector<StringConstant>> gateRuleEquivalentConceptMap = new Hashtable();
	protected Hashtable<StringConstant, Vector<Rule>> stringConstantRuleHash = new Hashtable<StringConstant, Vector<Rule>>();

	// 3/15/2016 TEST
	protected Hashtable<String, RuleExpansion> largestCoverageRuleExpansionHash = null;

	// 4/23/2018 TEST: For keeping track of which rules are most commonly instanced
	public Hashtable<Rule, Integer> ruleInstanceCountHash = new Hashtable();
	public Hashtable<Rule, Integer> ruleExpansionCountHash = new Hashtable();
	public Hashtable<Rule, Float> ruleExpansionInstanceRatioHash = new Hashtable();

	// 9/12/2018: Map words to string constants
	public Hashtable<String, Vector<StringConstant>> synonymStringConstantHash = new Hashtable();

	public static String[] UniversalStopwords = new String[] { "and", "but" };
	public static Grammar CurrentGrammar = null;
	public static String SentenceGrammarName = "sentence-grammar";
	public static String StructureGrammarName = "structure-grammar";
	public static String DocumentGrammarName = "document-grammar";
	public static String WordGrammarRuleFileName = "WordGrammarRules";

	protected static int MaximumAgendaLength = 50;

	public Grammar(Grammar parent, String name) {
		this.name = name;
		this.grammarModule = parent.grammarModule;
		this.ruleTokenHash = parent.ruleTokenHash;
		this.rulePatternHash = parent.rulePatternHash;
		this.actionRuleTokenHash = parent.actionRuleTokenHash;
		this.stopWordRuleHash = parent.stopWordRuleHash;
		this.ruleExpansionHash = new Hashtable<String, Vector<RuleExpansion>>();
		this.invalidatedRuleHash = new Hashtable();
		this.annotationSignatureHash = new Hashtable();
		this.largestCoverageAnnotationHash = new Hashtable();
		this.largestCoverageRuleExpansionHash = new Hashtable();
		this.targetConceptHash = new Hashtable();
		this.allAnnotations = null;
	}

	public Grammar(GrammarModule gmod, String name, String ruledir) {
		CurrentGrammar = this;
		String extendedName = gmod.getMoonstoneRuleInterface().getSelectedGrammarDirectoryName() + ":" + name;
		this.name = extendedName;
		this.grammarModule = gmod;
		this.ruleTokenHash = new Hashtable();
		this.rulePatternHash = new Hashtable();
		this.actionRuleTokenHash = new Hashtable();
		this.ruleExpansionHash = new Hashtable();
		this.invalidatedRuleHash = new Hashtable();
		this.annotationSignatureHash = new Hashtable();
		this.stopWordRuleHash = new Hashtable();
		this.largestCoverageAnnotationHash = new Hashtable();
		this.largestCoverageRuleExpansionHash = new Hashtable();
		this.knowledgeEngine = gmod.getKnowledgeEngine();
		// this.knowledgeEngine.findOrCreateKnowledgeBase("Rules");
		this.doDebug = this.knowledgeEngine.getStartupParameters().isPropertyTrue("debug");

		if (ruledir != null) {
			this.ruleDirectory = ruledir;
			readRules();
		}
	}

	public void runTHREAD(WordSequenceAnnotation sentenceAnnotation) {
		this.initializeRuleExpansions(sentenceAnnotation);
		Thread t = new Thread(new RuleExpansionTimer(this));
		t.start();
		try {
			t.join(5000);
		} catch (InterruptedException e) {
			String stext = this.sentenceAnnotation.getSentence().getText();
			String msg = "\n     ***Grammar.run(): Exceeded 2000 millisecond(s) for sentence \"" + stext + "\"";
			System.out.println(msg);
		}
	}

	public void run(WordSequenceAnnotation sentenceAnnotation) {
		this.initializeRuleExpansions(sentenceAnnotation);
		this.runRuleExpansions();

		// 2/13/2018
		this.getAllValidAnnotations();
	}

	public void initializeRuleExpansions(WordSequenceAnnotation sentenceAnnotation) {
		this.sentenceAnnotation = sentenceAnnotation;
		if (this.allAnnotations != null) {
			for (Annotation annotation : this.allAnnotations) {
				this.expandAnnotation(annotation);
			}
		}
	}

	public void runRuleExpansions() {
		int x = 1;
		boolean finished = false;
		long starttime = System.currentTimeMillis();

		while (!finished) {
			int index = getCurrentAgendaIndex();
			if (index >= 0) {
				Vector<Vector<RuleExpansion>> rev = this.newAgendas;
				this.currentAgendaIndex = index;
				Vector<RuleExpansion> expansions = this.newAgendas.elementAt(index);
				Vector<RuleExpansion> currentAgenda = new Vector(expansions);
				expansions.clear();
				Collections.sort(currentAgenda, new RuleExpansion.CoverageSorter());
				for (RuleExpansion expansion : currentAgenda) {

					// 2/13/2018
					if (!expansion.allMatchedAnnotationsAreValid()) {
						continue;
					}

					Rule rule = expansion.getRule();
					if (rule.isDispositionExecuteActions()) {
						if (expansion.isValidPerRuleConstraints()) {
							if (rule.isDoDebug()) {
								x = 1;
							}
							for (Constraint c : expansion.getRule().getActions()) {
								Annotation annotation = (Annotation) c.evalConstraint(expansion.getVariables());

								// BEFORE 3-22-2018: Need to re-add annotation regardless of whether it has
								// parents. it may be picked
								// up by rules that didn't apply before.
								if (annotation != null
								// && annotation.getParents() != null
								) {
									annotation.invalidateDerivativeAnnotations("InvalidatedViaModifierProperty");
									if (annotation.isValid() && annotation.getSentenceAnnotation().isValid()) {
										annotation.getGrammar().expandAnnotation(annotation);
										this.currentAgendaIndex = 0;
									}
								}
							}
						}
					} else {
						Annotation annotation = expansion.createAnnotation();
						x = 1;
					}
				}
			} else {
				finished = true;
			}
		}
	}

	protected int getCurrentAgendaIndex() {
		for (int i = this.currentAgendaIndex; i < this.newAgendas.size(); i++) {
			if (!this.newAgendas.elementAt(i).isEmpty()) {
				return i;
			}
		}
		return -1;
	}

	public void initializeChart(int length) {
		this.currentAgendaIndex = 0;
		this.interpretedAnnotationChart = null;
		this.newAgendas = null;

		this.clearAnnotationStructures();

		for (int i = 0; i <= length; i++) {
			this.interpretedAnnotationChart = VUtils.add(this.interpretedAnnotationChart, new Vector(0));
			this.newAgendas = VUtils.add(this.newAgendas, new Vector(0));
		}
	}

	protected void executeAnnotationActions(Annotation annotation) {
		if (annotation.getRule().getActions() != null) {
			for (Constraint c : annotation.getRule().getActions()) {
				c.evalConstraint(annotation.getVariables());
			}
		}
	}

	public void expandAnnotation(Annotation annotation) {
		try {
			if (!annotation.isValid()) {
				return;
			}
			WordSequenceAnnotation sa = annotation.getSentenceAnnotation();
			Vector<String> tokens = annotation.getIndexTokens();
			if (annotation.getRule() != null && annotation.getRule().isDoDebug()) {
				int x = 2;
			}
			boolean processed = false;
			if (!processed && tokens != null) {
				for (Object token : tokens) {
					Vector<Rule> rules = this.getExpandableRulesByIndexToken(token);
					if (rules != null) {
						for (Rule rule : rules) {
							if (rule.isDoDebug()) {
								int x = 1;
							}
							String key = rule.getRuleID() + ":" + token;
							Vector<Integer> positions = rule.getRuleTokenPositionHash().get(token);
							if (positions != null) {
								for (Integer pos : positions) {
									RuleExpansion newExpansion = null;
									key = rule.getRuleID() + ":" + pos;
									Vector<RuleExpansion> expansions = ruleExpansionHash.get(key);
									if (expansions != null) {
										for (RuleExpansion expansion : expansions) {
											newExpansion = new RuleExpansion(expansion, annotation, pos.intValue());
											if (rule != null && rule.isDoDebug()) {
												int x = 2;
											}
											processExpansion(newExpansion);
										}
									}
									newExpansion = new RuleExpansion(rule, annotation, pos.intValue());
									processExpansion(newExpansion);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void processExpansion(RuleExpansion newExpansion) {
		if (newExpansion.isValid()) {
			Rule rule = newExpansion.getRule();
			if (newExpansion.isFullyMatched()) {
				if (rule.isDoDebug()) {
					int x = 1;
				}
				newExpansion.calculateWordTokenValues();
				String ckey = newExpansion.getWordTokenStart() + "-" + newExpansion.getWordTokenEnd();
				RuleExpansion highest = this.largestCoverageRuleExpansionHash.get(ckey);

				// 4/25/2018: WHY IS MS PRODUCING DUPLICATE EXPANSIONS???
				if (newExpansion.fullyMatchedEquals(highest)) {
					int x = 1;
					return;
				}

				if (highest == null || newExpansion.getCoveredWordTokenCount() >= highest.getCoveredWordTokenCount()) {
					this.largestCoverageRuleExpansionHash.put(ckey, newExpansion);
					int index = newExpansion.getCoveredWordTokenCount() - 1;
					if (index < this.newAgendas.size()) {
						Vector<RuleExpansion> rev = this.newAgendas.elementAt(index);
						rev.add(newExpansion);
					} else {
						int x = 1;
					}
				} else {
					int x = 1;
				}
			} else {
				for (int i = 0; i < rule.getPatternListCount(); i++) {
					Annotation matchedAnnotation = newExpansion.getMatchedAnnotation(i);
					if (matchedAnnotation == null) {
						if (rule.isDoDebug()) {
							int x = 1;
						}
						String key = rule.getRuleID() + ":" + i;
						// FROM PROFILER: THIS CONSUMES 20% OF THE OVERALL PROCESSING TIME!!!
						VUtils.pushHashVector(this.ruleExpansionHash, key, newExpansion);
					}
				}
			}
		}
	}

	public void addAnnotation(Annotation annotation) {
		// 6/20/2016
		Rule rule = annotation.getRule();
		if (rule != null && rule.isDoDebug()) {
			int x = 1;
		}

		if (!annotation.isValid() || annotation.getGoodness() < this.getGrammarModule().getMoonstoneRuleInterface()
				.getAnnotationProbabilityCutoff()) {
			return;
		}

		// 8/8/2015: If the annotation covers less text than any other
		// annotation in the same space, reject it. Later, if it is valid
		// store its coverage size if it is larger than anything seen so far.
		String ckey = annotation.getStartEndString();

		// 5/19/2016 TEST
		if (annotation.getRule() != null) {
			ckey = annotation.getRule().getRuleID() + ":" + ckey;
		}

		Annotation other = this.largestCoverageAnnotationHash.get(ckey);
		int otc = (other != null ? other.getCoveredTextLength() : 0);
		int atc = annotation.getCoveredTextLength();

		// 4/26/2018
		if (annotation.isInterpreted() && annotation.equalsAnnotation(other)) {
			return;
		}

		// 4/30/2018 NOTE: THIS is the instruction that creates a 7+ fold difference in
		// speed.
		// It simply ensures that the annotation that covers a particular space have the
		// highest
		// word coverage, but we have to add all annotations with the same word
		// coverage.
		if (otc > atc && other.isValid()) {
			annotation.setValid(false);
			return;
		}

		// 5/1/2018 TEST!! CHECK FOR ACCURACY...
		if (other != null && other.isValid() && other.getConcept() instanceof StringConstant
				&& annotation.getConcept() instanceof StringConstant) {
			StringConstant sco = (StringConstant) other.getConcept();
			StringConstant sca = (StringConstant) annotation.getConcept();
			if (sco.getNumberOfColons() < sca.getNumberOfColons()) {
				return;
			}
		}

		if ((annotation.getRule() == null || !annotation.getRule().isTerminal())
				&& !duplicateAnnotationExists(annotation)) {
			if (atc > otc || !other.isValid()) {
				this.largestCoverageAnnotationHash.put(ckey, annotation);
			}

			if (annotation.getSignature() != null) {
				this.annotationSignatureHash.put(annotation.getSignature(), annotation);
			}

			if (annotation.isInterpreted() && !(annotation.hasRule() && annotation.getRule().isInhibitInterstitial())
					&& annotation.getRelativeTokenStart() < this.interpretedAnnotationChart.size()) {
				int rts = annotation.getRelativeTokenStart();
				Vector<Annotation> sv = this.interpretedAnnotationChart.elementAt(rts);
				VUtils.add(sv, annotation);
			}

			// 5/19/2016: Leave intermediate annotations out of final list.
			if (!(annotation.getRule() != null && annotation.getRule().isIntermediate())) {
				this.allAnnotations = VUtils.add(this.allAnnotations, annotation);
			} else {
				int x = 2;
			}

			this.expandAnnotation(annotation);

			// 7/23/2018
			if (rule != null && MoonstoneRuleInterface.RuleEditor.isPrintRuleStatistics()) {
				HUtils.incrementCount(rule.grammar.ruleInstanceCountHash, rule);
			}

		}
	}

	// 4/16/2015: For adding StructureAnnotation to SentenceAnnotation grammar.
	public void addAnnotation(StructureAnnotation annotation) {
		if (annotation != null) {
			this.allAnnotations = VUtils.add(this.allAnnotations, annotation);
		}
	}

	// 8/5/2015: Given two annotations with the same position and rule, keep the
	// one with the most complete word coverage. If the two have the same
	// coverage, but the two are "essentially the same", don't store the new
	// annotation.
	public boolean duplicateAnnotationExists(Annotation annotation) {
		if (annotation.getSignature() != null) {
			Annotation other = this.annotationSignatureHash.get(annotation.getSignature());

			// 4/30/2018
			if (annotation.equalsAnnotation(other)) {
				return true;
			}

			if (other != null && !annotation.isTerminal() && other.isValid() && annotation.getRule() != null) {
				double acp = annotation.getCoveredTextPercent();
				double ocp = other.getCoveredTextPercent();
				if (ocp >= acp) {
					if (annotation.getRule().isDoDebug()) {
						String s1 = annotation.getSignature();
						String s2 = other.getSignature();
						Rule r1 = annotation.getRule();
						Rule r2 = other.getRule();
						Object c1 = annotation.getConcept();
						Object c2 = other.getConcept();
						Vector v1 = annotation.getChildren();
						Vector v2 = other.getChildren();
						int x = 1;
					}
					return true;
				}
			}
		}
		return false;
	}

	public void addRule(Rule rule) {
		if (rule != null) {
			this.allRules = VUtils.add(this.allRules, rule);
			VUtils.pushHashVector(this.fileRuleHash, rule.getSourceFilePath(), rule);
			this.pushRuleHash(rule.getRuleType(), rule);
			this.pushRuleHash(rule.getRuleID(), rule);
			if (!(rule instanceof InferenceRule) && rule.getPatternLists() != null) {
				for (int i = 0; i < rule.getPatternListCount(); i++) {
					Vector tokens = rule.getPatternLists().elementAt(i);
					for (Object token : tokens) {
						this.pushRuleHash(token, rule);
						Integer index = new Integer(i);
						VUtils.pushHashVector(rule.getRuleTokenPositionHash(), token, index);
					}
				}
			}
		}
	}

	public Vector<Integer> getRuleTokenPositions(Rule rule, TerminalAnnotation ta) {
		Vector<Integer> positions = null;
		if (rule != null && ta != null) {
			for (String index : ta.getIndexTokens()) {

				Vector<Integer> v = rule.getRuleTokenPositionHash().get(index);

				// Before 2/15/2014
				// String key = rule.getRuleID() + ":" + index;
				// Vector<Integer> v = this.ruleTokenPositionHash.get(key);
				positions = VUtils.append(positions, v);
			}
		}
		return positions;
	}

	public void removeRule(Rule rule) {
		if (this.allRules != null) {
			for (Enumeration<String> e = this.getRuleTokenHash().keys(); e.hasMoreElements();) {
				String key = e.nextElement();
				Vector<Rule> rules = this.getRuleTokenHash().get(key);
				if (rules != null) {
					// 7/10/2018: Need to do this also for pattern hash...
					rules.remove(rule);
					if (rules.isEmpty()) {
						this.getRuleTokenHash().remove(key);
					}
				}

				rules = this.getRulesFromPatternHash(rule);
				if (rules != null) {
					rules.remove(rule);
					if (rules.isEmpty()) {
						this.rulePatternHash.remove(key);
					}
				}
			}
			this.allRules.remove(rule);
			if (this.allRules.isEmpty()) {
				this.allRules = null;
			}
		}
	}

	public void pushRuleHash(Object key, Rule rule) {
		if (rule.isDoDebug()) {
			int x = 1;
		}
		if (key != null) {
			VUtils.pushIfNotHashVector(getRuleTokenHash(), key, rule);
			this.pushRulePatternHash(rule);
		}
	}

	public boolean rulePatternExists(Rule rule) {
		Object key = rule.getGrammarPatternHashKey();
		boolean rv = this.rulePatternHash.get(key) != null;
		if (rv) {
			int x = 1;
		}
		return rv;
	}

	public void pushRulePatternHash(Rule rule) {
		Object key = rule.getGrammarPatternHashKey();
		VUtils.pushIfNotHashVector(this.rulePatternHash, key, rule);
	}

	public Vector<Rule> getRulesFromPatternHash(Rule rule) {
		Object key = rule.getGrammarPatternHashKey();
		if (key != null) {
			return this.rulePatternHash.get(key);
		}
		return null;
	}

	public Rule getContentMatchingRuleFromPatternHash(Rule rule) {
		Object key = rule.getGrammarPatternHashKey();
		Vector<Rule> samePatternRules = this.rulePatternHash.get(key);
		if (samePatternRules != null) {
			for (Rule match : samePatternRules) {
				if (match.contentEquals(rule)) {
					return match;
				}
			}
		}
		return null;
	}

	// Before 7/10/2019
	// public Vector<Rule> getRulePatternHash(Object key) {
	// return this.rulePatternHash.get(key);
	// }

	public void pushRuleHash(Vector keys, Rule rule) {
		if (keys != null && rule != null) {
			for (Object key : keys) {
				VUtils.pushIfNotHashVector(getRuleTokenHash(), key, rule);
			}
			this.allRules = VUtils.addIfNot(allRules, rule);
		}
	}

	public boolean hasRulesByIndexToken(Object key) {
		return this.getRulesByIndexToken(key) != null;
	}

	public Vector<Rule> getRulesByIndexToken(Object key) {
		Vector<Rule> rules = null;
		if (key != null) {
			rules = getRuleTokenHash().get(key);
			if (rules != null) {
				rules = new Vector<Rule>(rules);
			}
		}
		return rules;
	}

	// PROFILER: 30% OF CPU
	public Vector<Rule> getExpandableRulesByIndexToken(Object key) {
		Vector<Rule> rules = null;
		if (key != null) {
			Vector<Rule> v = getRuleTokenHash().get(key);
			if (v != null) {
				for (Rule rule : v) {
					if (!this.ruleIsInvalid(rule) && !(rule instanceof moonstone.rule.LabelRule) && !rule.isVisited()) {
						rules = VUtils.add(rules, rule);
						rule.setVisited(true);
					}
				}
			}
		}
		if (rules != null) {
			for (Rule rule : rules) {
				rule.setVisited(false);
			}
		}
		return rules;
	}

	public Rule getRuleByID(String id) {
		if (id != null) {
			Vector<Rule> rules = this.getRuleTokenHash().get(id);
			if (rules != null) {
				return rules.firstElement();
			}
		}
		return null;
	}

	public Vector<Rule> getRulesByType(String type) {
		if (type != null) {
			return this.getRuleTokenHash().get(type);
		}
		return null;
	}

	public Vector<Rule> getAllRules() {
		return this.allRules;
	}

	public Vector<Rule> getRulesFromFile(String fname, String rtype) {
		Vector<Rule> rules = null;
		if (fname != null && rtype != null) {
			if (this.getAllRules() != null) {
				for (Rule rule : this.getAllRules()) {
					if (fname.equals(rule.getSourceFilePath()) && rtype.equals(rule.getRuleType())) {
						rules = VUtils.add(rules, rule);
					}
				}
			}
		}
		return rules;
	}

	public static Vector<String> getRuleIDs(Vector<Rule> rules) {
		Vector<String> rids = null;
		if (rules != null) {
			for (Rule rule : rules) {
				if (rule.getRuleID() != null) {
					rids = VUtils.add(rids, rule.getRuleID());
				}
			}
		}
		return rids;
	}

	public void resolveRules() {
		if (this.getAllRules() != null) {
			for (Rule rule : this.getAllRules()) {
				if (rule.getSourceID() != null) {
					Rule source = this.getRuleByID(rule.getSourceID());
					if (source != null) {
						rule.setSourceRule(source);
					}
				}
			}
		}
	}

	public void readRules() {
		if (this instanceof DocumentGrammar) {
			((DocumentGrammar) this).gateRuleEquivalentConceptMap = new Hashtable();
		}
		this.clearAnnotationStructures();
		this.clearRuleStructures();
		RegExprManager.createRegExprManager();

		Vector<File> files = FUtils.readFilesFromDirectory(this.ruleDirectory);
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().charAt(0) != '.') {
					createRules(file.getAbsolutePath());
				}
			}
			this.resolveRules();
		} else {
			System.out.println("Unable to find rules directory: " + this.ruleDirectory);
		}
	}

	public void storeRulesAsExcelFile() {
		String filename = this.knowledgeEngine.getStartupParameters()
				.getPropertyValue(MoonstoneRuleInterface.ExcelGrammarRuleFileName);
		if (filename == null) {
			filename = MoonstoneRuleInterface.ExcelGrammarRuleFileName;
		}
		String fullname = this.getGrammarModule().getMoonstoneRuleInterface().getResourceDirectoryName()
				+ File.separator + filename;
		StringBuffer sb = new StringBuffer();
		Vector<String> fnames = HUtils.getKeys(this.fileRuleHash);
		Collections.sort(fnames);
		for (int i = 0; i <= TSLInformation.getExcelAttributeMaxIndex(); i++) {
			Vector<String> attributes = TSLInformation.getExcelIndexAttributes(i);
			String astr = StrUtils.stringListConcat(attributes, "/");
			astr = astr.toUpperCase();
			sb.append(astr);
			sb.append(Rule.ExcelDelimiter);
		}
		sb.append("File\n");
		for (String fname : fnames) {
			Vector<Rule> rules = this.fileRuleHash.get(fname);
			Collections.sort(rules, new Rule.RuleNameSorter());
			if (rules != null) {
				for (Rule rule : rules) {
					if (!(rule instanceof InferenceRule)) {
						String xstr = rule.toExcelString();
						sb.append(xstr + "\n");
					} else {
						int x = 1;
					}
				}
			}
		}
		FUtils.writeFile(fullname, sb.toString());
	}

	public void createRules(String filename) {
		String type = null;
		int x = 0;
		try {
			TLisp tlisp = TLisp.getTLisp();
			Sexp sexp = (Sexp) tlisp.loadFile(filename);
			try {
				type = (String) TLUtils.convertToJObject(sexp.getFirst());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			sexp = (Sexp) sexp.getCdr();
			Sexp ruleFileProperties = Sexp.doAssoc(sexp, "all-rule-properties");
			String shortname = new File(filename).getName();
			for (Enumeration e = sexp.elements(); e.hasMoreElements();) {
				Object o = e.nextElement();
				if (o instanceof Sexp) {
					Sexp sub = (Sexp) o;
					createRule(sub, type, filename, ruleFileProperties);
				}
			}
		} catch (Exception e) {
			System.out.println("Error with rule file: " + filename);
			e.printStackTrace();
		}
	}

	public Rule createRule(String rstr, String type, String filename) {
		Rule rule = null;
		try {
			Sexp sexp = (Sexp) TLisp.getTLisp().evalString(rstr);
			Sexp ruleFileProperties = Sexp.doAssoc(sexp, "all-rule-properties");
			rule = createRule(sexp, type, filename, ruleFileProperties);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rule;
	}

	public Rule createRule(Sexp sexp, String type, String filename, Sexp ruleFileProperties) {
		Rule rule = null;
		KnowledgeBase rootkb = this.knowledgeEngine.getRootKnowledgeBase();
		rootkb.clearFields();
		try {
			Vector pattern = TLUtils.convertSexpToJVector(sexp);
			String ruleid = (String) VUtils.assocValue("ruleid", pattern);
			if (ruleid != null) {
				if ("inferencerule".equals(type)) {

					rule = InferenceRule.createInferenceRule(rootkb, sexp);

					// 3/24/2014
					if (rule == null) {
						Form form = Form.createForm(sexp);
						if (form != null) {
							rootkb.initializeForm(form);
						}
					}
				} else {
					Vector<Vector<String>> wordLists = null;
					Vector wv = VUtils.assoc("words", pattern);
					if (wv == null) {
						wv = VUtils.assoc("pattern", pattern);
					}
					if (wv != null && wv.size() > 1) {
						wordLists = VUtils.rest(wv);
						for (Vector vs : wordLists) {
							Collections.sort(vs, new StrUtils.LengthSorterDescending());
						}
					}
					if ("labelrule".equals(type)) {
						rule = new LabelRule(this, sexp, pattern, wordLists);
					} else {
						rule = new Rule(this, sexp, pattern, wordLists, ruleFileProperties);
					}
				}
				if (rule != null) {
					Rule existing = this.getRuleByID(rule.getRuleID());
					if (existing != null) {
						// System.out.println("DUPLICATE RULE: " +
						// rule.getRuleID() + " in " + filename +
						// ", Existing in "
						// + existing.getSourceFileName());
					}
					rule.setSexp(sexp);
					rule.setRuleType(type);

					String sourcefile = (String) VUtils.assocValue("sourcefile", pattern);
					if (filename == null && sourcefile != null) {
						filename = sourcefile;
					}
					rule.setSourceFilePath(filename);
					this.addRule(rule);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		rootkb.clearFields();
		return rule;
	}

	public String validateRuleString(String rstr, String ruletype) {
		try {
			Object o = TLisp.getTLisp().evalString(rstr);
			if (!(o instanceof Sexp)) {
				return "Invalid Lisp S-expression string";
			}
			Sexp s = (Sexp) o;
			Vector v = TLUtils.convertSexpToJVector(s);
			Vector<Vector> avpairs = Information.getAVPairs(v);
			o = VUtils.assocValue("ruleid", avpairs);
			if (!(o instanceof String)) {
				return "Must have String rule id";
			}
			if ("inferencerule".equals(ruletype)) {
				return validateInferenceRuleString(s);
			} else {
				return validatePatternAnalysisRuleString(s);
			}
		} catch (Exception e) {
			return "Malformed Lisp expression string";
		}
	}

	public String validatePatternAnalysisRuleString(Sexp s) {
		try {
			Vector v = TLUtils.convertSexpToJVector(s);
			Vector<Vector> avpairs = Information.getAVPairs(v);
			for (Vector avpair : avpairs) {
				Object fo = avpair.elementAt(0);
				if (!(fo instanceof String)) {
					return "First element of " + fo + " must be a property name";
				}
				String property = (String) fo;
				Object value = avpair.elementAt(1);
				if (!Information.validateProperty(property, value)) {
					return avpair + " has invalid value type";
				}
			}
		} catch (Exception e) {
			return "Malformed Lisp expression";
		}
		return null;
	}

	public String validateInferenceRuleString(Sexp s) {
		try {
			Vector v = TLUtils.convertSexpToJVector(s);
			if (!(v.size() == 2 && s.getSecond() instanceof Sexp)) {
				return "Must have 2 items:  rule id and ImplicationSentence definition";
			}
			Sexp def = (Sexp) s.getSecond();
			Vector defv = TLUtils.convertSexpToJVector(def);
			ImplicationSentence.createImplicationSentence(defv);
		} catch (Exception e) {
			return "Malformed Lisp expression";
		}
		return null;
	}

	// 5/24/2018 (NOT YET TESTED)
	public void storeAllRules() {
		Hashtable<String, Vector<Rule>> fileRuleHash = new Hashtable();
		MoonstoneRuleInterface msri = this.getGrammarModule().getMoonstoneRuleInterface();
		String rpath = msri.topGrammarDirectoryPathName + File.separatorChar + msri.selectedGrammarDirectoryName
				+ File.separatorChar + "sentence-grammar";
		String defaultSourceFilePath = rpath + File.separatorChar + "UnnamedRules" + File.separatorChar
				+ "UnnamedRules.txt";
		if (this.allRules != null) {
			for (Rule rule : this.allRules) {
				if (!(rule instanceof InferenceRule)) {
					if (rule.getSourceFilePath() == null) {
						rule.setSourceFilePath(defaultSourceFilePath);
					}
					VUtils.pushHashVector(fileRuleHash, rule.getSourceFilePath(), rule);
				}
			}
			for (String fpath : fileRuleHash.keySet()) {
				Vector<Rule> rules = fileRuleHash.get(fpath);
				if (rules.size() > 1) {
					int x = 1;
				}
				FUtils.writeFile(fpath, "'(wordrules\n\n");
				for (Rule rule : rules) {
					String text = rule.getSexp().toNewlinedString();
					FUtils.writeFile(fpath, text + "\n\n", false);
				}
				FUtils.writeFile(fpath, ")\n", false);
			}
		}
	}

	public void storeRules(Vector<Rule> rules) {
		if (rules != null) {
			Rule first = rules.firstElement();
			String fpath = first.getSourceFilePath();
			String type = first.getRuleType();
			File file = new File(fpath);
			if (file.exists()) {
				StringBuffer sb = new StringBuffer();
				sb.append("\'(\n" + type + "\n");
				for (Rule rule : rules) {
					if (rule.getSexp() != null) {
						String lstr = rule.getSexp().toNewlinedString(0);
						sb.append(lstr + "\n\n");
					}
				}
				sb.append(")\n");
				FUtils.writeFile(fpath, sb.toString());
			}
		}
	}

	public static void storeRules(Vector<Rule> rules, String fname, String type) {
		try {
			if (rules != null) {
				File file = new File(fname);
				if (!file.exists()) {
					file.createNewFile();
				}
				StringBuffer sb = new StringBuffer();
				sb.append("\'(\n" + type + "\n");
				for (Rule rule : rules) {
					if (rule.getSexp() != null) {
						String lstr = rule.getSexp().toNewlinedString(0);
						sb.append(lstr + "\n\n");
					}
				}
				sb.append(")\n");
				FUtils.writeFile(fname, sb.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setRuleIsInvalid(Rule rule) {
		this.invalidatedRuleHash.put(rule, rule);
	}

	public boolean ruleIsInvalid(Rule rule) {
		return this.invalidatedRuleHash.get(rule) != null;
	}

	// 5/6/2014
	public void clearRuleStructures() {
		this.ruleTokenHash = new Hashtable();
		this.actionRuleTokenHash = new Hashtable();
		allRules = null;
		allInferenceRules = null;
	}

	public void clearAnnotationStructures() {
		this.allAnnotations = null;
		this.displayedAnnotations = null;
		this.ruleExpansionHash = new Hashtable();
		this.invalidatedRuleHash = new Hashtable();
		this.annotationSignatureHash = new Hashtable();
		this.largestCoverageAnnotationHash = new Hashtable();
		this.largestCoverageRuleExpansionHash = new Hashtable();

		this.interpretedAnnotationChart = null;
		this.newAgendas = null;
		this.currentAgendaIndex = 0;
		Annotation.LastSignatureID = 0;
	}

	public Vector<Annotation> getAllAnnotations() {
		return allAnnotations;
	}

	public Vector<Annotation> getAllValidAnnotations() {
		Vector<Annotation> valid = null;
		if (this.allAnnotations != null) {
			for (Annotation annotation : this.allAnnotations) {
				if (annotation.isValid()) {
					valid = VUtils.add(valid, annotation);
				} else {
					int x = 1;
				}
			}
			this.allAnnotations = valid;
		}
		return this.allAnnotations;
	}

	public Vector<Annotation> getAllCoveringInterpretedAnnotations() {
		return Annotation.getNonNestedNonCoincidingAnnotations(this.allAnnotations, true);
	}

	public String getRuleDirectory() {
		return ruleDirectory;
	}

	public boolean isDoDebug() {
		return this.doDebug;
	}

	public void setDoDebug(boolean debug) {
		this.doDebug = debug;
	}

	public KnowledgeEngine getKnowledgeEngine() {
		return knowledgeEngine;
	}

	public boolean isUseOnlyCoreConceptAnnotations() {
		return useOnlyCoreConceptAnnotations;
	}

	public void setUseOnlyCoreConceptAnnotations(boolean useOnlyCoreConceptAnnotations) {
		this.useOnlyCoreConceptAnnotations = useOnlyCoreConceptAnnotations;
	}

	public Hashtable<String, RelationSentence> getConceptConsequentHash() {
		Hashtable<String, RelationSentence> cchash = new Hashtable();
		Vector<Rule> irules = this.getRulesByType("inferencerule");
		if (irules != null) {
			for (Rule rule : irules) {
				InferenceRule irule = (InferenceRule) rule;
				String concept = irule.getConsequentConcept();
				RelationSentence consequent = (RelationSentence) irule.getImplicationSentence().getConsequent();
				if (concept != null) {
					cchash.put(concept, consequent);
				}
			}
		}
		return cchash;
	}

	public Vector<InferenceRule> getAllInferenceRules() {
		return allInferenceRules;
	}

	public void addStopWord(Rule rule, String sword) {
		VUtils.pushHashVector(this.stopWordRuleHash, sword, rule);
	}

	public Vector<Rule> getStopWordRules(String sword) {
		return this.stopWordRuleHash.get(sword);
	}

	// 3/27/2015: Excludes interpreted annotations that extend beyond the
	// tokenEnd.
	public boolean interpretedAnnotationsExist(int tokenStart, int tokenEnd) {
		if (tokenStart <= tokenEnd && this.interpretedAnnotationChart != null
				&& tokenEnd < this.interpretedAnnotationChart.size()) {
			for (int i = tokenStart; i <= tokenEnd; i++) {
				Vector<Annotation> sv;
				try {
					sv = this.interpretedAnnotationChart.elementAt(i);
					if (sv.size() > 0) {
						for (Annotation interpreted : sv) {
							if (interpreted.isValid() && interpreted.getRelativeTokenEnd() <= tokenEnd // 7/19/2018 TEST
							) {
								return true;
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					return true;
				}
			}
		}
		return false;
	}

	public WordSequenceAnnotation getSentenceAnnotation() {
		return this.sentenceAnnotation;
	}

	public boolean containsUniversalStopword(int tokenStart, int tokenEnd) {
		try {
			if (this.sentenceAnnotation == null || this.sentenceAnnotation.getSentence() == null
					|| this.sentenceAnnotation.getTokens() == null) {
				return false;
			}
			Vector<Token> tokens = this.sentenceAnnotation.getSentence().getTokens();
			if (tokenEnd >= tokens.size()) {
				tokenEnd = tokens.size() - 1;
			}
			if (tokenStart <= tokenEnd && this.sentenceAnnotation.getUniversalStopWords() != null
					&& this.sentenceAnnotation.getSentence().getTokens() != null) {
				for (int i = tokenStart; i <= tokenEnd; i++) {
					Token token = tokens.elementAt(i);
					String tstr = token.getString().toLowerCase();
					for (int j = 0; j < this.sentenceAnnotation.getUniversalStopWords().size(); j++) {
						String swstr = this.sentenceAnnotation.getUniversalStopWords().elementAt(j);
						if (swstr.equals(tstr)) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public GrammarModule getGrammarModule() {
		return grammarModule;
	}

	public String toString() {
		return "<Grammar: " + this.name + ">";
	}

	public String toLispString() {
		if (this.getAllRules() != null) {
			StringBuffer sb = new StringBuffer();
			for (Rule rule : this.getAllRules()) {
				sb.append(rule.toSexpString());
				sb.append("\n");
			}
			return sb.toString();
		}
		return null;
	}

	public Vector<Annotation> getDisplayedAnnotations() {
		return displayedAnnotations;
	}

	public void setDisplayedAnnotations(Vector<Annotation> displayedAnnotations) {
		this.displayedAnnotations = displayedAnnotations;
	}

	public String getName() {
		return name;
	}

	public Vector<Sentence> gatherSentencesForInterpretation(Header header, Vector<String> concepts,
			Vector<Annotation> annotations) {
		Vector<Sentence> sentences = null;
		if (concepts != null && annotations != null) {
			for (Annotation annotation : annotations) {
				for (String concept : concepts) {
					if (concept.equals(annotation.getConcept())) {
						Sentence s = new Sentence(header, annotation.getTokens(), false);
						sentences = VUtils.add(sentences, s);
					}
				}
			}
		}
		return sentences;
	}

	// 4/7/2015: Given a type, return a vector of grammar rules indexed on
	// subtypes.
	public Vector<Rule> extractOntologyTypeGrammarRules(TypeConstant type) {
		Vector<Rule> rules = null;
		if (type.getChildren() != null) {
			for (Term ctype : type.getChildren()) {
				Vector<Rule> crules = extractOntologyTypeGrammarRules((TypeConstant) ctype);
				rules = VUtils.append(rules, crules);
			}
			if (!type.isRoot()) {
				String rid = "TypeGrammarRule_" + type.getName();
				Rule rule = new Rule();
				rule.setName(rid);
				rule.setRuleID(rid);
				rule.setType(type);
				rule.setConcept("?0");
				Vector<String> tlist = null;
				for (Term ctype : type.getChildren()) {
					tlist = VUtils.add(tlist, ((TypeConstant) ctype).getFormalName());
				}
				rule.setPatternLists(VUtils.listify(tlist));
				rules = VUtils.add(rules, rule);
			}
		}
		return rules;
	}

	public Vector<Rule> extractOntologyTypedStringConstantGrammarRules() {
		Vector<Rule> rules = null;
		Ontology ontology = this.knowledgeEngine.getCurrentOntology();
		if (ontology.getAllTypeConstants() != null) {
			for (TypeConstant type : ontology.getAllTypeConstants()) {
				Vector<StringConstant> scs = type.getTypedStringConstants();
				if (scs != null) {
					String rid = "StringConstantGrammarRule_" + type.getName();
					Rule rule = new Rule();
					rule.setName(rid);
					rule.setRuleID(rid);
					rule.setType(type);
					rule.setConcept("?0");
					Vector<String> sclist = null;
					for (StringConstant sc : scs) {
						sclist = VUtils.add(sclist, sc.getName());
					}
					rule.setPatternLists(VUtils.listify(sclist));
					rules = VUtils.add(rules, rule);
				}
			}
		}
		return rules;
	}

	public Hashtable<String, Vector<Rule>> getRuleTokenHash() {
		return this.ruleTokenHash;
	}

	public Hashtable<String, Vector<Rule>> getActionRuleTokenHash() {
		return this.actionRuleTokenHash;
	}

	public void printRuleUsage() {
		Hashtable rehash = new Hashtable();
		Hashtable fmrehash = new Hashtable();
		Vector<RuleUsage> rus = null;
		for (Enumeration<String> e = this.ruleExpansionHash.keys(); e.hasMoreElements();) {
			String key = e.nextElement();
			Vector<RuleExpansion> v = this.ruleExpansionHash.get(key);
			for (RuleExpansion re : v) {
				HUtils.incrementCount(rehash, re.getRule());
			}
		}
		for (Annotation a : this.allAnnotations) {
			if (a.getRule() != null) {
				HUtils.incrementCount(fmrehash, a.getRule());
			}
		}
		for (Enumeration<Rule> e = rehash.keys(); e.hasMoreElements();) {
			Rule rule = e.nextElement();
			int matched = HUtils.getCount(rehash, rule);
			int fullymatched = HUtils.getCount(fmrehash, rule);
			RuleUsage ru = new RuleUsage(rule, matched, fullymatched);
			rus = VUtils.add(rus, ru);
		}
		Collections.sort(rus, new RuleUsage.RuleUsageMatchSorter());
		for (RuleUsage ru : rus) {
			System.out.println(ru);
		}
	}

	public void storeTargetConcepts(Vector targets) {
		for (Object t : targets) {
			this.targetConceptHash.put(t, t);
		}
	}

	public boolean isTargetConcept(Object target) {
		return (target != null && this.targetConceptHash.get(target) != null);
	}

	public void showHashTableSizes() {
		int rehsize = this.ruleExpansionHash.size();
		int lcreh = this.largestCoverageRuleExpansionHash.size();
		// System.out.println("Sizes: " + rehsize + " " + lcreh);
	}

	// public boolean conceptSupports(Constant c1, Constant c2) {
	//
	// }

	public Rule selectGrammarRule() {
		Vector<String> rnames = this.getGrammarRuleNames();
		Object[] rlist = VUtils.vectorToArray(rnames);
		String selected = (String) JOptionPane.showInputDialog(new JFrame(), "Select Concept:", "Customized Dialog",
				JOptionPane.PLAIN_MESSAGE, null, rlist, rlist[0]);
		Rule rule = this.getRuleByID(selected);
		return rule;
	}

	public Vector<String> getGrammarRuleNames() {
		Hashtable<String, Rule> rhash = new Hashtable();
		for (Rule rule : this.allRules) {
			if (!rule.getRuleID().startsWith("EBL") && !rule.getRuleID().startsWith("BOC")) {
				rhash.put(rule.getRuleID(), rule);
			}
		}
		Vector<String> rnames = HUtils.getKeys(rhash);
		Collections.sort(rnames);
		return rnames;
	}

	// 5/16/2018
	public void extractOntology() {
		Hashtable<String, StringConstant> schash = new Hashtable();
		Hashtable<String, Vector<String>> thash = new Hashtable();
		Ontology ontology = new Ontology();
		for (Rule rule : this.getAllRules()) {
			Object tc = rule.getResultType();
			if (tc instanceof TypeConstant) {
				int x = 1;
			}
			try {
				if (rule.getResultConcept() instanceof StringConstant) {
					StringConstant sc = (StringConstant) rule.getResultConcept();
					schash.put(sc.getFormalName(), sc);
					if ((sc.getType() == null || sc.getType().isUndefined()) && tc instanceof TypeConstant) {
						sc.setType((TypeConstant) tc);
					}
					if (!sc.getType().isUndefined()) {
						VUtils.pushIfNotHashVector(thash, sc.getType().getFormalName(), sc.getFormalName());
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Vector<String> tnames = HUtils.getKeys(thash);
		Collections.sort(tnames);
		Vector<String> scnames = HUtils.getKeys(schash);
		Collections.sort(scnames);
		StringBuffer sb = new StringBuffer();
		for (String tname : tnames) {
			Vector<String> scns = thash.get(tname);
			Collections.sort(scns);
			sb.append("(defstringconstant ");
			for (String scname : scns) {
				sb.append("\"" + scname + "\" ");
			}
			sb.append("\"" + tname + "\")\n");
		}
		System.out.println(sb.toString());
		System.out.println("*******************");
		for (String scname : scnames) {
			StringConstant sc = schash.get(scname);
			if (sc.getType().isUndefined()) {
				System.out.println("\"" + sc.getFormalName() + "\"");
			}
		}
	}

	public void printRulesByConcept() {
		Vector<Rule> sortedRules = new Vector(0);
		for (Rule rule : this.allRules) {
			if (rule.getSourceFileName().toLowerCase().contains("onyxpneumoniamoonstonegrammarrules")) {
				sortedRules.add(rule);
			}
		}
		Collections.sort(sortedRules, new RuleResultConceptSorter());
		for (Rule rule : sortedRules) {
			System.out.println(rule.getSexp().toNewlinedString() + "\n\n");
		}
	}

	public static class RuleResultConceptSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Rule r1 = (Rule) o1;
			Rule r2 = (Rule) o2;
			Object c1 = r1.getResultConcept();
			Object c2 = r2.getResultConcept();
			String n1 = "zzz";
			String n2 = "zzz";
			if (c1 instanceof StringConstant) {
				StringConstant sc1 = (StringConstant) c1;
				n1 = sc1.getName();
			}
			if (c2 instanceof StringConstant) {
				StringConstant sc2 = (StringConstant) c2;
				n2 = sc2.getName();
			}
			return n1.compareTo(n2);
		}
	}

	// 4/23/2018 TEST
	public void printRuleInstanceCounts() {
		Vector<Rule> rules = HUtils.getKeys(this.ruleExpansionCountHash);
		if (rules != null) {
			System.out.println("RULE EXPANSION COUNTS");
			Collections.sort(rules, new RuleExpansionCountSorter());
			for (Rule rule : rules) {
				int expcount = this.ruleExpansionCountHash.get(rule);
				int instcount = (this.ruleInstanceCountHash.get(rule) != null ? this.ruleInstanceCountHash.get(rule)
						: 0);
				float ratio = (float) instcount / (float) expcount;
				this.ruleExpansionInstanceRatioHash.put(rule, ratio);
				System.out.println("\t" + rule.getRuleID() + "Expansions=" + expcount + ",Instances=" + instcount
						+ ",Ratio=" + ratio);
			}
		}
	}

	public static class RuleInstanceCountSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Rule r1 = (Rule) o1;
			Rule r2 = (Rule) o2;
			int c1 = r1.getGrammar().ruleInstanceCountHash.get(r1);
			int c2 = r2.getGrammar().ruleInstanceCountHash.get(r2);
			if (c1 > c2) {
				return -1;
			}
			if (c2 > c1) {
				return 1;
			}
			return 0;
		}
	}

	public static class RuleExpansionCountSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Rule r1 = (Rule) o1;
			Rule r2 = (Rule) o2;
			int c1 = r1.getGrammar().ruleExpansionCountHash.get(r1);
			int c2 = r2.getGrammar().ruleExpansionCountHash.get(r2);
			if (c1 > c2) {
				return -1;
			}
			if (c2 > c1) {
				return 1;
			}
			return 0;
		}
	}

	public static class RuleExpansionInstanceRatioSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Rule r1 = (Rule) o1;
			Rule r2 = (Rule) o2;
			float c1 = r1.getGrammar().ruleExpansionInstanceRatioHash.get(r1);
			float c2 = r1.getGrammar().ruleExpansionInstanceRatioHash.get(r2);
			if (c1 > c2) {
				return -1;
			}
			if (c2 > c1) {
				return 1;
			}
			return 0;
		}
	}

	public void clearRuleCountHashtables() {
		this.ruleInstanceCountHash.clear();
		this.ruleExpansionCountHash.clear();
		this.ruleExpansionInstanceRatioHash.clear();
	}

	// 9/12/2018: Synonym / String Constant hash. A word may point to > 1 SC.
	public void addSynonymStringConstant(StringConstant sc, String syn) {
		VUtils.pushIfNotHashVector(this.synonymStringConstantHash, syn, sc);
	}

	public Vector<StringConstant> getSynonymStringConstants(String syn) {
		return this.synonymStringConstantHash.get(syn);
	}

	// 9/19/2018:  For TSLOntology tool
	public Vector<Rule> getStringConstantRules(StringConstant sc) {
		return this.stringConstantRuleHash.get(sc);
	}

	public void addStringConstantRule(Rule rule) {
		if (rule.getResultConcept() instanceof StringConstant) {
			VUtils.pushIfNotHashVector(this.stringConstantRuleHash, (StringConstant) rule.getResultConcept(), rule);
		}
	}

}
