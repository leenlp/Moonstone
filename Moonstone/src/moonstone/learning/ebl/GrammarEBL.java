package moonstone.learning.ebl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import moonstone.annotation.Annotation;
import moonstone.grammar.Grammar;
import moonstone.rule.Rule;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.documentanalysis.document.Document;
import tsl.expression.Expression;
import tsl.expression.form.sentence.ImplicationSentence;
import tsl.expression.form.sentence.constraint.Constraint;
import tsl.expression.term.Term;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.relation.PatternRelationSentence;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypeConstant;
import tsl.inference.forwardchaining.ForwardChainingInferenceEngine;
import tsl.knowledge.engine.StartupParameters;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.tsllisp.Sexp;
import tsl.utilities.FUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.TimeUtils;
import tsl.utilities.VUtils;
import utility.UnixFormat;

// 2/4/2016 NOTE:  I just flipped the user-selected concept from a string to a StringConstant,
// and added a flag on StringConstant indicating whether it is complex.  I've made lots of 
// changes, not yet tested...

public class GrammarEBL {

	protected ForwardChainingInferenceEngine forwardChainingInferenceEngine = null;
	protected MoonstoneRuleInterface moonstoneRuleInterface = null;
	protected Vector<Annotation> sentenceAnnotations = null;
	protected Vector<File> documentFiles = null;
	protected String currentDocumentName = null;
	protected int annotationIndex = 0;
	protected int lastProcessedAnnotationIndex = 0;
	protected int documentIndex = 0;
	protected int newRuleID = 0;
	protected boolean quitting = false;
	protected Hashtable<String, String> acceptedStringHash = new Hashtable();
	protected boolean requiresUserValidation = true;
	protected String trainingCorpusDirectory = null;
	protected String sentenceGrammarRootDirectory = null;
	protected String corpusRuleStorageDirectory = null;
	protected Vector<Rule> newEBLGrammarRules = null;
	protected Hashtable<String, String> relationConceptHash = new Hashtable();
	protected String defaultRuleExtractionFile = null;
	protected String defaultEBLRuleDirectory = null;
	protected boolean analyzingCorpusFiles = false;
	protected int terminalPatternTokenType = GrammarEBL.UseConceptTerminalPatternTokens;
	protected Vector<String> targetConceptList = null;
	protected boolean gatherPatternToDepth = true;
	protected StringConstant lastSelectedEBLConstant = null;

	public static int UseTypeTerminalPatternTokens = 1;
	public static int UseConceptTerminalPatternTokens = 2;
	public static int UseWordTerminalPatternTokens = 3;

	public static String EBLGrammarAcceptedStringFile = "EBLGrammarAcceptedStringFile";
	public static String EBLRequireUserValidation = "EBLRequireUserValidation";

	public static String EBLSentenceGrammarRootDirectory = "EBLSentenceGrammarRootDirectory";
	public static String EBLCorpusRuleStorageDirectory = "EBLCorpusRuleStorageDirectory";
	protected static String EBLCorpusTrainingDirectory = "EBLCorpusTrainingDirectory";
	protected static String EBLDefaultRuleFile = "EBLDefaultRuleFile";
	protected static String EBLRuleDirectory = "EBLRuleDirectory";
	protected static String GrammarEBLConceptFileName = "GrammarEBLConceptFile";
	public static String InferredTargetRelationName = "inferred-target";

	public GrammarEBL(MoonstoneRuleInterface msri) {
		this.moonstoneRuleInterface = msri;
		StartupParameters sp = msri.getKnowledgeEngine().getStartupParameters();
		this.forwardChainingInferenceEngine = this.moonstoneRuleInterface.getMoonstoneQueryPanel()
				.getForwardChainingInferenceEngine();
		Vector<ImplicationSentence> isents = msri.getKnowledgeEngine().getCurrentKnowledgeBase()
				.getAllImplicationSentences();
		this.forwardChainingInferenceEngine.storeRules(isents);
		this.sentenceGrammarRootDirectory = sp.getPropertyValue(EBLSentenceGrammarRootDirectory);
		this.trainingCorpusDirectory = sp.getPropertyValue(EBLCorpusTrainingDirectory);
		this.corpusRuleStorageDirectory = sp.getPropertyValue(EBLCorpusRuleStorageDirectory);
		this.defaultRuleExtractionFile = sp.getPropertyValue(EBLDefaultRuleFile);
		this.defaultEBLRuleDirectory = sp.getPropertyValue(EBLRuleDirectory);
		this.readAcceptedStrings();
		this.requiresUserValidation = msri.getKnowledgeEngine().getStartupParameters()
				.isPropertyTrue(EBLRequireUserValidation);
		this.populateTargetConceptList();
	}

	public void getDocumentFilesFromTrainingDirectory() {
		String fpath = this.moonstoneRuleInterface.getKnowledgeEngine().getStartupParameters().getRootDirectory()
				+ File.separatorChar + this.trainingCorpusDirectory;
		Vector<File> files = FUtils.readFilesFromDirectory(fpath);
		this.documentFiles = null;
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".txt")) {
					this.documentFiles = VUtils.add(this.documentFiles, file);
				}
			}
		}
	}

	public void analyzeCorpusFiles() {
		this.analyzingCorpusFiles = true;
		while (!this.quitting) {
			this.processNextCorpusAnnotation();
		}
		this.storeEBLGrammarRules();
		this.clear();
		this.documentFiles = null;
		System.out.println("FINISHED");
	}

	protected void readAcceptedStrings() {
		String fname = this.moonstoneRuleInterface.getKnowledgeEngine().getStartupParameters()
				.getPropertyValue(EBLGrammarAcceptedStringFile);
		if (fname != null) {
			String fpath = this.moonstoneRuleInterface.getResourceDirectoryName() + File.separatorChar + fname;
			String fstr = FUtils.readFile(fpath);
			if (fstr != null) {
				String[] strs = fstr.split("::");
				for (int i = 0; i < strs.length; i++) {
					String str = strs[i];
					this.acceptedStringHash.put(str, str);
				}
			}
		}
	}

	protected void processNextCorpusAnnotation() {
		if (this.documentFiles != null && this.documentIndex < this.documentFiles.size()
				&& (this.sentenceAnnotations == null || this.annotationIndex >= this.sentenceAnnotations.size() - 1)) {
			this.newEBLGrammarRules = null;
			this.annotationIndex = this.lastProcessedAnnotationIndex = 0;
			File file = this.documentFiles.elementAt(this.documentIndex++);
			this.currentDocumentName = file.getName();
			System.out.println("\nGrammarEBL: Processing \"" + file.getName() + "\"...");
			String text = FUtils.readFile(file);
			try {
				text = UnixFormat.convertToUnixFormat(text);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			Document doc = new Document(this.currentDocumentName, text);
			this.sentenceAnnotations = this.moonstoneRuleInterface.getControl().applyNarrativeGrammarToText(doc, true,
					false, true);
		}
		if (this.sentenceAnnotations != null && this.annotationIndex < this.sentenceAnnotations.size()) {

			Annotation annotation = this.sentenceAnnotations.elementAt(this.annotationIndex++);
			if (annotation.isInterpreted() && this.acceptedStringHash.get(annotation.getText()) == null) {
				System.out.print(".");
				if (annotation.isInterpreted() && !annotation.containsTargetConcept()
						&& this.acceptedStringHash.get(annotation.getText()) == null) {
					Vector<RelationSentence> tisents = this.gatherInferredTargetSentences(annotation);
					if (tisents != null) {
						RelationSentence firstrs = tisents.firstElement();
						for (int i = 0; i < firstrs.getArity(); i++) {
							Object t = firstrs.getTerm(i);
							String cname = t.getClass().getName();
							int x = 1;
						}
						this.processAnnotationWithInferredStatements(annotation, tisents);
					}
				}
			}
		} else if (this.documentIndex >= this.documentFiles.size()) {
			this.quitting = true;
		}
	}

	protected Vector<RelationSentence> gatherInferredTargetSentences(Annotation annotation) {
		Vector<RelationSentence> tisents = null;
		Vector<RelationSentence> rsents = annotation.getRelationSentences(false);
		if (rsents != null) {
			Vector<RelationSentence> isents = (Vector<RelationSentence>) this.forwardChainingInferenceEngine
					.getAllInferredRelationSentences(rsents);
			tisents = this.gatherInferredTargetSentences(isents);
		}
		return tisents;
	}

	public void createBagOfConceptsRuleFromMoonstoneAnnotation(boolean topLevelOnly, boolean isOrdered,
			boolean useLastConstant) {
		int x = 0;
		Grammar grammar = this.moonstoneRuleInterface.getSentenceGrammar();
		Document doc = null;
		Vector<Annotation> annotations = this.moonstoneRuleInterface.getSelectedAnnotationsFromJTree();

		// 9/27/2018
		boolean hasNegatedAnnotations = false;

		if (annotations != null) {
			Annotation selectedAnnotation = annotations.firstElement();
			KnowledgeBase kb = selectedAnnotation.getKnowledgeBase();
			Rule selectedRule = selectedAnnotation.getRule();
			Vector<String> tokens = new Vector(0);
			Vector<Vector<String>> tvs = new Vector(0);
			for (int i = 0; i < annotations.size(); i++) {
				String vname = "?" + i;
				Annotation annotation = annotations.elementAt(i);
				String tname = null;
				if (annotation.isNegated()) {
					tname = "neg";
					hasNegatedAnnotations = true;
				} else {
					tname = "notneg";
				}
				Vector<String> tv = new Vector(0);
				tv.add(tname);
				tv.add(vname);
				tvs.add(tv);
				if (annotation.getRule() != null && annotation.hasConcept()) {
					this.gatherPatternToDepth = !topLevelOnly;
					Rule rule = annotation.getRule();
					doc = annotation.getDocument();
					if (topLevelOnly) {
						tokens.add(annotation.getConcept().toString());
					} else {
						this.gatherAllAnnotationConcepts(annotation, tokens);
					}
				}
			}
			if (!tokens.isEmpty()) {
				Vector<Vector<String>> embedded = VUtils.listifyVector(tokens);
				Rule newrule = new Rule();
				Vector<Vector> patternLists = newrule.extractEmbeddedPatternConstants(embedded);
				newrule.setPatternLists(patternLists);
				StringConstant concept = null;
				if (useLastConstant && this.lastSelectedEBLConstant != null) {
					concept = this.lastSelectedEBLConstant;
				} else {
					concept = this.getConceptStringFromTargetList(selectedAnnotation, false);
					this.lastSelectedEBLConstant = concept;
				}
				if (concept == null) {
					return;
				}
				String dateString = TimeUtils.getDateTimeString();

				String newruleid = "BOC-" + concept.getFormalName() + "-" + this.newRuleID++ + "-" + dateString;
				newrule.setRuleID(newruleid);
				if (annotations.size() == 1) {
					newrule.setExampleSnippet(
							StrUtils.removeNonAlphaDigitSpaceCharacters(selectedAnnotation.getText()));
				}
				newrule.setResultConcept(concept);
				newrule.setType(concept.getType());
				newrule.setOrdered(isOrdered);
				newrule.setSpecialized(true);

				// newrule.setWindow(rule.getWindow());
				newrule.setBagOfConceptsRule(true);

				Vector<Constraint> tps = null;
				for (Vector<String> v : tvs) {
					Constraint pt = Constraint.createConstraint(kb, v);
					tps = VUtils.add(tps, pt);
				}
				newrule.setTestPredicates(tps);

				// 12/16/2017: This makes it impossible to pass negation up the tree
				// 11/29/2017: Assume the result is positive
				// newrule.addPropertyToRemove("directionality");

				if (hasNegatedAnnotations) {
					int answer = JOptionPane.showConfirmDialog(new JFrame(),
							"Annotation contains negated sub-annotation(s).  Add negation removal directive?");
					if (answer == JOptionPane.YES_OPTION) {
						newrule.addPropertyToRemove("directionality");
					}
				}

				Sexp sexp = newrule.toSexp(false);
				newrule.setSexp(sexp);

				boolean allowanyway = true;

				if (!allowanyway && grammar.rulePatternExists(newrule)) {
					this.moonstoneRuleInterface
							.displayMessageDialog("Rule pattern already exists: " + newrule.getPattern());
				} else {
					grammar.pushRulePatternHash(newrule);
					this.newEBLGrammarRules = VUtils.add(this.newEBLGrammarRules, newrule);
				}
			}
		}
		this.storeEBLGrammarRules();
	}

	// 11/29/2017
	public boolean gatherAllAnnotationConcepts(Vector<Annotation> annotations, Vector<String> tokens) {
		boolean foundTokens = false;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (this.isIncludableSingletonAnnotation(annotation)) {
					if (annotation.hasConcept()) {
						foundTokens = true;
						tokens = VUtils.addIfNot(tokens, annotation.getConcept().toString());
					}
				} else if (annotation.hasChildren()) {
					for (Annotation child : annotation.getChildAnnotations()) {
						if (gatherAllAnnotationConcepts(child, tokens)) {
							foundTokens = true;
						}
					}
					if (!foundTokens && annotation.hasConcept()) {
						tokens = VUtils.addIfNot(tokens, annotation.getConcept().toString());
						foundTokens = true;
					}
				}
			}
		}
		return foundTokens;
	}

	public boolean gatherAllAnnotationConcepts(Annotation annotation, Vector<String> tokens) {
		boolean foundTokens = false;
		if (annotation != null) {
			if (this.isIncludableSingletonAnnotation(annotation)) {
				if (annotation.hasConcept()) {
					foundTokens = true;
					tokens = VUtils.addIfNot(tokens, annotation.getConcept().toString());
				}
			} else if (annotation.hasChildren()) {
				for (Annotation child : annotation.getChildAnnotations()) {
					if (gatherAllAnnotationConcepts(child, tokens)) {
						foundTokens = true;
					}
				}
				if (!foundTokens && annotation.hasConcept()) {
					tokens = VUtils.addIfNot(tokens, annotation.getConcept().toString());
					foundTokens = true;
				}
			}
		}
		return foundTokens;
	}

	public void extractDomainSpecificRulesFromSelectedAnnotation(Annotation annotation, boolean atDepth,
			boolean unifiableConcepts) {
		this.gatherPatternToDepth = atDepth;
		StringConstant concept = null;
		if (annotation != null) {
			concept = this.getConceptStringFromTargetList(annotation, unifiableConcepts);
			if (concept == null) {
				return;
			}
			this.analyzingCorpusFiles = false;
			Vector<RelationSentence> isents = this.gatherInferredTargetSentences(annotation);
			this.extractRulesFromAnnotation(annotation, isents, concept);
			this.storeEBLGrammarRules();
		} else {
			this.moonstoneRuleInterface.displayMessageDialog("No annotation selected...");
		}
	}

	public void processAnnotationWithInferredStatements(Annotation annotation, Vector<RelationSentence> tisents) {
		Vector<Annotation> displayed = VUtils.listify(annotation);
		this.moonstoneRuleInterface.getControl().getSentenceGrammar().setDisplayedAnnotations(displayed);
		if (this.requiresUserValidation) {
			this.lastProcessedAnnotationIndex = this.annotationIndex - 1;
			this.moonstoneRuleInterface.repopulateJTree(true);
			this.moonstoneRuleInterface.setTextToAnalyze(annotation.getText());
			String tistr = "Snippet=\"" + annotation.getText() + "\"\n";

			tistr += "Sentence=\"" + annotation.getSentenceAnnotation().getSentence().getText() + "\"\n";
			Object[] options = { "Accept (Concepts)", "Reject", "Negative Example", "Bad String", "Backup", "Cancel" };
			int answer = JOptionPane.showOptionDialog(new JFrame(), tistr, null, JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

			switch (answer) {
			case 0:
				this.extractRulesFromAnnotation(annotation, tisents);
				break;
			case 1:
				break;
			case 2:
				break;
			case 3:
				this.acceptedStringHash.put(annotation.getText(), annotation.getText());
				this.storeAcceptedStrings();
				break;
			case 4:
				this.backupAnnotation();
				break;
			case 5:
				this.quitting = true;
				break;
			default:
				break;
			}
		} else {
			this.extractRulesFromAnnotation(annotation, tisents);
		}
	}

	public void extractRulesFromAnnotation(Annotation annotation, Vector<RelationSentence> isents) {
		extractRulesFromAnnotation(annotation, isents, null);
	}

	public void extractRulesFromAnnotation(Annotation annotation, Vector<RelationSentence> isents,
			StringConstant concept) {
		if (annotation.getRule() != null) {
			// if (annotation.getRule().isSpecialized()) {
			// return;
			// }
			if (this.analyzingCorpusFiles) {
				this.acceptedStringHash.put(annotation.getText(), annotation.getText());
			}
			Vector<Rule> newrules = generateSnapshotRules(annotation, isents, null, true, concept);
			this.newEBLGrammarRules = VUtils.append(this.newEBLGrammarRules, newrules);
		}
	}

	protected Vector<Rule> generateSnapshotRules(Annotation annotation, Vector<RelationSentence> isents,
			String dateString, boolean attop, StringConstant concept) {
		if (annotation.getRule() == null || annotation.getRule().isTerminal()) {
			return null;
		}
		Rule rule = annotation.getRule();
		KnowledgeBase kb = rule.getKnowledgeBase();
		Grammar sgrammar = this.moonstoneRuleInterface.getSentenceGrammar();
		if (dateString == null) {
			dateString = TimeUtils.getDateTimeString();
		}
		Document doc = annotation.getDocument();
		Rule newrule = annotation.getRule().clone();
		newrule.setSpecialized(true);
		TypeConstant newtype = null;
		if (!attop) {
			newrule.setIntermediate(true);
		} else {
			// newrule.addPropertyToRemove("directionality");
			newtype = this.getTypeForSpecializedRule(annotation, concept);
			if (newtype == null) {
				int answer = JOptionPane.showConfirmDialog(new JFrame(),
						"Grammar rule type & selected constant type not unifiable.  Proceed?");
				if (answer != JOptionPane.YES_OPTION) {
					return null;
				}
				newtype = concept.getType();
			}
		}

		newrule.setSourceID(rule.getRuleID());
		newrule.setExampleSnippet(StrUtils.removeNonAlphaDigitSpaceCharacters(annotation.getText()));

		if (concept == null && isents != null) {
			for (RelationSentence isent : isents) {
				RelationConstant rc = isent.getRelation();
				if (InferredTargetRelationName.equals(rc.getName()) && isent.getArity() == 2
						&& isent.getModifier() instanceof StringConstant) {
					concept = (StringConstant) isent.getModifier();
					break;
				}
			}
		}
		if (concept != null) {
			newrule.setResultConcept(concept);
		}
		Vector<Vector> patternLists = null;
		Vector<Rule> newrules = null;
		if (newtype != null) {
			newrule.setType(newtype);
		} else if (annotation.getType() != null) {
			newrule.setType(annotation.getType());
		}
		int x = 2;
		Vector<Vector<String>> tvs = new Vector(0);
		boolean hasNegatedAnnotations = false;
		for (int i = 0; i < rule.getPatternListCount(); i++) {
			String ctoken = null;
			Annotation child = annotation.getLexicalChild(i);
			String vname = "?" + i;
			String tname = null;
			if (child.isNegated()) {
				tname = "neg";
				hasNegatedAnnotations = true;
			} else {
				tname = "notneg";
			}
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
					Vector<Rule> ncr = generateSnapshotRules(child, null, dateString, false, null);
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
		for (Vector<String> v : tvs) {
			Constraint pt = Constraint.createConstraint(kb, v);
			tps = VUtils.add(tps, pt);
		}
		newrule.setTestPredicates(tps);

		// 9/27/2018
		if (hasNegatedAnnotations) {
			int answer = JOptionPane.showConfirmDialog(new JFrame(),
					"Annotation contains negated sub-annotation(s).  Add negation removal directive?");
			if (answer == JOptionPane.YES_OPTION) {
				newrule.addPropertyToRemove("directionality");
			}
		}

		// 1/12/2018: Do I need this?
		// if (annotation.getNumberOfChildren() == 1 &&
		// annotation.getLexicalChild(0).isNegated()) {
		// newrule.addPropertyToRemove("directionality");
		// }

		if (isents != null) {
			Hashtable rhash = new Hashtable();
			for (RelationSentence is : isents) {
				if (this.isTargetRelation(is.getRelation())) {
					newrule.setContainsTargetConcept(true);
					Vector terms = new Vector(0);
					terms.add(is.getRelation());
					for (int i = 0; i < is.getTermCount(); i++) {
						Object o = is.getTerm(i);
						if (o instanceof Annotation) {
							o = annotation.getParsetreePathName((Annotation) o);
						}
						o = Term.wrapTerm(o);
						terms.add(o);
					}
					PatternRelationSentence prs = new PatternRelationSentence(terms);
					if (rhash.get(prs.getRelation()) == null) {
						rhash.put(prs.getRelation(), prs);
						newrule.addSemanticRelation(prs);
					}
				}
			}
		}

		String newruleid = getNewRuleID(rule, concept, dateString);
		newrule.setRuleID(newruleid);

		Sexp sexp = newrule.toSexp();
		newrule.setSexp(sexp);
		newrules = VUtils.append(VUtils.listify(newrule), newrules);
		return newrules;
	}

	protected String getAnnotationPatternToken(Annotation annotation, Vector plst) {
		String token = annotation.getText();
		Rule rule = annotation.getRule();
		if (this.terminalPatternTokenType == GrammarEBL.UseTypeTerminalPatternTokens) {
			if (annotation.getType() != null) {
				token = annotation.getType().getFormalName();
			}
		} else if (this.terminalPatternTokenType == GrammarEBL.UseConceptTerminalPatternTokens) {
			if (annotation.getCui() != null) {
				token = annotation.getCui().toUpperCase();
				// }
				//
				// else if (annotation.getRule().isComplexConcept()) {
				// TypeConstant type = (TypeConstant) annotation.getRule().getResultType();
				// TypeConstant btype = type.getFirstBaseType();
				// token = type.getFormalName();

			} else if (annotation.getConcept() != null) {
				token = annotation.getConcept().toString();
				if (annotation.getConcept() instanceof StringConstant) {
					StringConstant sc = (StringConstant) annotation.getConcept();
					TypeConstant basetype = TypeConstant.getLowestCommonAncestor(rule.getType(), sc.getType());
					if (sc.getType() != null) {
						if (basetype == null && plst != null) {
							for (Object ptoken : plst) {
								TypeConstant btype = sc.getType().getBaseType(ptoken);
								if (btype instanceof TypeConstant) {
									basetype = btype;
									break;
								}
							}
						}
						if (sc.getType().getNumberOfBaseTypes() > 1 && basetype != null) {
							token = Annotation.getConceptPlusBaseTypeIndex(sc, basetype);

							// 7/18/2018: Removing type suffix temporarily...
							token = sc.toString();
						}

					}
				}
			}
		}
		return token;
	}

	protected Vector<RelationSentence> gatherInferredTargetSentences(Vector<RelationSentence> isents) {
		Vector<RelationSentence> targets = null;
		if (isents != null) {
			for (RelationSentence isent : isents) {
				if (isTargetRelation(isent.getRelation())) {
					targets = VUtils.add(targets, isent);
				}
			}
		}
		return targets;
	}

	protected boolean isTargetRelation(RelationConstant rc) {
		return this.relationConceptHash.get(rc.getName()) != null;
	}

	protected boolean isIncludableSingletonAnnotation(Annotation annotation) {
		Rule rule = annotation.getRule();
		if (rule != null && rule.isSingleton()) {
			if (rule.isSingletonWordInput()) {
				return true;
			}
			if (annotation.isNumber()) {
				return true;
			}
			if (annotation.getSingleChild() != null) {
				return isIncludableSingletonAnnotation(annotation.getSingleChild());
			}
		}
		return false;
	}

	protected void backupAnnotation() {
		int last = this.lastProcessedAnnotationIndex;
		if (last > 0) {
			last--;
		}
		this.annotationIndex = last;
	}

	protected void storeAcceptedStrings() {
		if (this.analyzingCorpusFiles) {
			String fname = this.moonstoneRuleInterface.getKnowledgeEngine().getStartupParameters()
					.getPropertyValue(EBLGrammarAcceptedStringFile);
			if (fname != null) {
				String fpath = this.moonstoneRuleInterface.getResourceDirectoryName() + File.separatorChar + fname;
				StringBuffer sb = new StringBuffer();
				for (Enumeration<String> e = this.acceptedStringHash.keys(); e.hasMoreElements();) {
					String str = e.nextElement();
					sb.append(str + "::");
				}
				FUtils.writeFile(fpath, sb.toString());
			}
		}
	}

	public void clear() {
		this.documentIndex = 0;
		this.annotationIndex = 0;
		this.currentDocumentName = null;
		this.quitting = false;
		this.newEBLGrammarRules = null;
		this.moonstoneRuleInterface.resetTitle();
	}

	public void storeEBLGrammarRules() {
		this.moonstoneRuleInterface.resetTitle();
		MoonstoneRuleInterface msri = this.moonstoneRuleInterface;
		StartupParameters sp = msri.getStartupParameters();
		String dateString = TimeUtils.getDateTimeString();
		dateString = StrUtils.replaceNonAlphaNumericCharactersWithDelim(dateString, '_');
		int x = 1;
		if (this.newEBLGrammarRules != null) {
			Rule firstrule = this.newEBLGrammarRules.firstElement();
			String fullname = sp.getRuleDirectory() + File.separatorChar + msri.getSelectedGrammarDirectoryName();
			if (this.currentDocumentName == null) {
				// fullname += File.separatorChar + "sentence-grammar" + File.separatorChar +
				// "EBL" + File.separatorChar
				// + "EBLRules" + File.separatorChar + "EBLRule_" + dateString;

				String fname = this.getNewRuleFileName(firstrule);
				fullname += File.separatorChar + "sentence-grammar" + File.separatorChar + "EBL" + File.separatorChar
						+ "EBLRules" + File.separatorChar + fname;
			} else {
				fullname += File.separatorChar + this.corpusRuleStorageDirectory + File.separatorChar
						+ this.currentDocumentName;
			}
			FUtils.findOrCreateDirectory(fullname, true);
			Grammar.storeRules(this.newEBLGrammarRules, fullname, "wordrule");

			// if (this.moonstoneRuleInterface.getLpcfg() != null) {
			// this.moonstoneRuleInterface.getLpcfg().storeMeaningCounts();
			// }
			// 10/31/2016
			this.newEBLGrammarRules = null;
			this.moonstoneRuleInterface.reloadRules(false);
			// msri.viewSelectedRuleFile(firstrule);
		} else {
			this.moonstoneRuleInterface.displayMessageDialog("No rules to store");
		}
	}

	protected StringConstant getConceptStringFromTargetList(Annotation annotation, boolean coveredTypesOnly) {
		StringConstant selected = null;
		Vector<StringConstant> displayable = null;
		Vector<StringConstant> allConcepts = this.moonstoneRuleInterface.getKnowledgeEngine().getCurrentOntology()
				.getAllStringConstants();
		if (annotation != null) {
			TypeConstant type = annotation.getType();
			if ((type == null || type.isUndefined() || type.isRoot()) && annotation.getRule() != null
					&& annotation.getRule().getResultType() != null) {
				type = (TypeConstant) annotation.getRule().getResultType();
			}

			// type = null; // Have to fix problem, where "at nursing home" only shows
			// ":PP:"

			if (coveredTypesOnly && type != null && !type.isUndefined() && !type.isRoot()) {
				type = type.getFirstBaseType();
				if (type != null) {
					allConcepts = type.gatherCoveredStringConstants();
				}
			}
		}
		if (allConcepts != null) {
			for (StringConstant cs : allConcepts) {
				try {
					if (!cs.isComplex() && cs.getType() != null
					// && !cs.getType().isUndefined()
							&& !cs.getType().isRoot() && cs.getFormalName().charAt(0) == ':') {
						displayable = VUtils.add(displayable, cs);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (displayable != null) {
			Collections.sort(displayable, new Expression.NameSorter());
			Object[] clist = VUtils.vectorToArray(displayable);
			selected = (StringConstant) JOptionPane.showInputDialog(new JFrame(), "Select Concept:",
					"Customized Dialog", JOptionPane.PLAIN_MESSAGE, null, clist, clist[0]);
		}
		return selected;
	}

	protected void populateTargetConceptList() {
		this.targetConceptList = this.moonstoneRuleInterface.getReadmission().getAllMoonstoneTargetConcepts();
		String fname = this.moonstoneRuleInterface.getStartupParameters().getPropertyValue(GrammarEBLConceptFileName);
		if (fname != null) {
			String fullname = this.moonstoneRuleInterface.getResourceDirectoryName() + File.separatorChar + fname;
			File f = new File(fullname);
			try {
				if (f.exists()) {
					BufferedReader in = new BufferedReader(new FileReader(f));
					String line = null;
					Vector<String> strs = null;
					while ((line = in.readLine()) != null) {
						if (line.length() > 0) {
							strs = VUtils.add(strs, line.trim());
						}
					}
					this.targetConceptList = VUtils.appendNew(this.targetConceptList, strs);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void populateTargetConceptList(TypeConstant baseType) {
		Vector<StringConstant> scs = baseType.gatherCoveredStringConstants();
		Vector<String> names = StringConstant.getFormalNames(scs);
		this.targetConceptList = names;
	}

	public Vector<Rule> getEBLGrammarRules() {
		return this.newEBLGrammarRules;
	}

	public void assignInferredTargetConcepts(Vector<Annotation> annotations) {
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				assignInferredTargetConcept(annotation);
			}
		}
	}

	// 7/6/2016
	public void assignInferredTargetConcept(Annotation annotation) {
		if (!annotation.containsTargetConcept() && annotation.getType() != null && annotation.getRule() != null
				&& annotation.getRule().isComplexConcept()) {
			Object oldconcept = annotation.getConcept();
			Vector<RelationSentence> isents = this.gatherInferredTargetSentences(annotation);
			if (isents != null) {
				for (RelationSentence isent : isents) {
					String cname = this.relationConceptHash.get(isent.getRelation().getName());
					if (cname != null) {
						StringConstant sc = StringConstant.createStringConstant(cname, annotation.getType(), false);
						annotation.setConcept(sc);
						System.out.println("AssignInferredTargetConcept: " + annotation.getText() + ",OLD=" + oldconcept
								+ ",NEW=" + sc);
						return;
					}
				}
			}
		}
	}

	// 7/5/2016
	public void runCorpusAnalysisThread() {
		try {
			Thread t = new Thread(new GrammarEBLCorpusAnalysis());
			t.start();
			while (t.isAlive()) {
				t.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected TypeConstant getTypeForSpecializedRule(Annotation annotation, StringConstant concept) {
		Rule rule = annotation.getRule();
		TypeConstant newtype = null;
		TypeConstant ruletype = (rule.getResultType() instanceof TypeConstant ? (TypeConstant) rule.getResultType()
				: annotation.getType());
		if (ruletype != null && concept.getType() != null) {
			TypeConstant lca = TypeConstant.getLowestCommonAncestor(ruletype, concept.getType());
			if (lca != null) {
				// return lca;
				return concept.getType();
			}
		}
		return null;
	}

	protected String getNewRuleID(Rule rule, Object concept, String dateString) {
		String newruleid = "EBL:" + concept + ":" + dateString;
		return newruleid;
	}

	protected String getNewRuleFileName(Rule rule) {
		String fname = StrUtils.replaceNonAlphaNumericCharactersWithDelim(rule.getRuleID(), '_');
		fname += ".txt";
		return fname;
	}

}
