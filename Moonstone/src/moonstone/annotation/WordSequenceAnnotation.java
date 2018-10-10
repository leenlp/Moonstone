package moonstone.annotation;

import java.util.Collections;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Vector;

import moonstone.grammar.Grammar;
import moonstone.rule.Rule;
import moonstone.semantic.Interpretation;
import moonstone.syntactic.Syntax;
import tsl.documentanalysis.document.Document;
import tsl.documentanalysis.document.Sentence;
import tsl.documentanalysis.lexicon.Word;
import tsl.documentanalysis.tokenizer.Token;
import tsl.documentanalysis.tokenizer.regexpr.RegExprToken;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.type.SyntacticTypeConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.ontology.umls.CUIStructureShort;
import tsl.knowledge.ontology.umls.CUIStructureWrapperShort;
import tsl.knowledge.ontology.umls.UMLSStructuresShort;
import tsl.knowledge.ontology.umls.UMLSTypeConstant;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;

public class WordSequenceAnnotation extends Annotation {
	private Sentence sentence = null;
	private Vector<Annotation> annotations = null;
	private Vector<Annotation> expandedCoveringAnnotations = null;
	private int startTokenIndex = 0;
	private Vector<LabelAnnotation> stopWordLabels = null;
	private Vector<String> universalStopWords = null;
	private String string = null;
	private Vector<Token> wordTokens = null;
	private int[] commaPositions = new int[1024];
	private int[] punctuationPositions = new int[2048];
	private int numberOfCommas = 0;
	private int numberOfPunctuations = 0;

	// 6/13/2018: For reference resolution
	private Vector<Annotation> thingAnnotations = null;

	public WordSequenceAnnotation(Grammar grammar) {
		super();
		this.grammar = grammar;
		this.setPropertyType("sentence");
		this.getMoonstoneRuleInterface().storeForGarbageCollection(this);
	}

	public WordSequenceAnnotation(Grammar grammar, Sentence sentence) {
		this(grammar);
		this.sentence = sentence;
		this.string = sentence.text.toLowerCase();
		this.wordTokens = Token.gatherWordTokens(sentence.tokens);
		this.startTokenIndex = sentence.tokens.firstElement().getIndex();

		int cindex = 0;
		for (Token token : sentence.getTokens()) {
			if (token.isPunctuation()) {
				if (!token.isQuestionMark() && !token.isColon()) {
					this.punctuationPositions[this.numberOfPunctuations++] = token.getStart();
				}
			}
			if (token.isComma()) {
				this.commaPositions[this.numberOfCommas++] = token.getStart();
			}
		}
	}

	// Altered 6/16/2016 to gather only the largest rule tags.
	public void gatherNarrativeAnnotations() {
		boolean allowCoveringRuleBreak = !this.getGrammar().getGrammarModule().getMoonstoneRuleInterface()
				.getStartupParameters().isPropertyTrue("InhibitNarrativeGrammarCoveringPhraseEarlyBreak");

		Hashtable<Rule, Integer> ruleCountHash = new Hashtable();
		Hashtable<Vector<Token>, Vector<Rule>> subTokenRuleHash = new Hashtable();
		int tsize = wordTokens.size();
		for (int i = 0; i < wordTokens.size(); i++) {
			Token token = wordTokens.elementAt(i);
			this.addTokenAnnotation(token, token.getString(), token, token);
		}
		for (int i = 0; i < wordTokens.size(); i++) {
			int max = (i + 3 < tsize ? i + 3 : tsize - 1);
			for (int j = max; j >= i; j--) {
				Vector<Token> subTokens = VUtils.subVector(wordTokens, i, j + 1);
				String substr = Token.stringListConcat(subTokens).toLowerCase();
				Token start = subTokens.firstElement();
				Token end = subTokens.lastElement();

				Vector<Rule> swrules = grammar.getStopWordRules(substr);
				if (swrules != null) {
					for (Rule swrule : swrules) {
						new LabelAnnotation(this, swrule, substr, start.getIndex(), end.getIndex(), start.getStart(),
								end.getEnd(), start.getWordIndex(), end.getWordIndex());
					}
				}
				Vector<Rule> rules = grammar.getRulesByIndexToken(substr);
				boolean foundRules = (rules != null);
				Word word = null;
				if (i == j) {
					Token token = subTokens.firstElement();
					if ((word = subTokens.firstElement().getWord()) != null) {
						for (int k = 0; k < Grammar.UniversalStopwords.length; k++) {
							String swstr = Grammar.UniversalStopwords[k];
							if (word.getString().equals(swstr)) {
								this.universalStopWords = VUtils.addIfNot(this.universalStopWords, swstr);
							}
						}
						if (!foundRules) {
							for (String ptypestr : word.getPartsOfSpeech()) {
								TypeConstant ptype = Syntax.convertPartOfSpeechToAnnotationFormat(ptypestr);
								Vector<Rule> prules = grammar.getRulesByIndexToken(ptype);
								if (prules != null) {
									int x = 1;
								}
								rules = VUtils.appendIfNot(rules, prules);
							}
						}
					}
					if (token.isPunctuation() && !(Annotation.isSpecialAnnotation(token.getString()))) {
						rules = null;
					}
				}
				if (rules != null) {
					for (Rule rule : rules) {
						HUtils.incrementCount(ruleCountHash, rule);
						VUtils.pushHashVector(subTokenRuleHash, subTokens, rule);
					}
				}

				// 9/12/2018:  Create annotations for words added directly to the ontology, not found in 
				// the grammar.
				if (!foundRules) {
					Vector<StringConstant> scs = this.getGrammar().getSynonymStringConstants(substr);
					if (scs != null) {
						for (StringConstant sc : scs) {
							TerminalAnnotation ta = new TerminalAnnotation(this, null, sc.getFullName(), null, substr,
									start.getIndex(), end.getIndex(), start.getStart(), end.getEnd(),
									start.getWordIndex(), end.getWordIndex(), null, sc.getType());
							Interpretation si = Interpretation.create(this, null, sc, null, null, sc.getType());
							ta.setSemanticInterpretation(si);
							ta.generateSuperIndexTokens();
						}
					}
				}

				// PROBLEM!!!: If a larger substring causes a smaller one to be omitted,
				// then if the rule containing the larger string is invalidated,
				// rules with the smaller substring are not activated instead. E.g.
				// the rule for "in home services" causes "home" not to be processed
				// in "lives in home".
				if (allowCoveringRuleBreak && foundRules) {
					i = j;
					break;
				}
			}
		}
		Vector<Rule> rules = HUtils.getKeys(ruleCountHash);
		if (rules != null) {
			for (Rule rule : rules) {
				int count = HUtils.getCount(ruleCountHash, rule);
				if (count < rule.getwordOnlyPatternCount()) {
					this.grammar.setRuleIsInvalid(rule);
				}
			}
		}
		Vector<Vector<Token>> subTokenLists = HUtils.getKeys(subTokenRuleHash);
		if (subTokenLists != null) {
			for (Vector<Token> subTokens : subTokenLists) {
				Vector<Rule> strules = subTokenRuleHash.get(subTokens);
				String substr = Token.stringListConcat(subTokens).toLowerCase();
				Token start = subTokens.firstElement();
				Token end = subTokens.lastElement();
				Word word = null;
				Vector<SyntacticTypeConstant> ptypes = null;
				if (subTokens.size() == 1 && ((word = subTokens.firstElement().getWord()) != null)
						&& word.getPartsOfSpeech() != null) {
					for (String pos : word.getPartsOfSpeech()) {
						SyntacticTypeConstant ptype = Syntax.convertPartOfSpeechToAnnotationFormat(pos);
						ptypes = VUtils.add(ptypes, ptype);
					}
				} else {
					SyntacticTypeConstant ptype = Syntax.getTerminalPhraseType(subTokens);
					if (ptype != null) {
						ptypes = VUtils.listify(ptype);
					}
				}
				if (ptypes != null) {
					for (SyntacticTypeConstant ptype : ptypes) {
						TagAnnotation ta = new TagAnnotation(this, null, null, ptype, substr, start.getIndex(),
								end.getIndex(), start.getStart(), end.getEnd(), start.getWordIndex(),
								end.getWordIndex(), null, null);
						int x = 1;
					}
				} else {
					TagAnnotation ta = new TagAnnotation(this, null, null, null, substr, start.getIndex(),
							end.getIndex(), start.getStart(), end.getEnd(), start.getWordIndex(), end.getWordIndex(),
							null, null);
					int x = 1;
				}
			}
		}
		if (this.stopWordLabels != null) {
			Collections.sort(this.stopWordLabels, new Annotation.TextStartSorter());
		}
	}

	// Altered 6/16/2016 to gather only the largest rule tags.
	public void gatherNarrativeAnnotations_BEFORE_9_20_2018() {
		boolean allowCoveringRuleBreak = !this.getGrammar().getGrammarModule().getMoonstoneRuleInterface()
				.getStartupParameters().isPropertyTrue("InhibitNarrativeGrammarCoveringPhraseEarlyBreak");

		Hashtable<Rule, Integer> ruleCountHash = new Hashtable();
		Hashtable<Vector<Token>, Vector<Rule>> subTokenRuleHash = new Hashtable();
		int tsize = wordTokens.size();
		for (int i = 0; i < wordTokens.size(); i++) {
			Token token = wordTokens.elementAt(i);
			this.addTokenAnnotation(token, token.getString(), token, token);
		}
		for (int i = 0; i < wordTokens.size(); i++) {
			int max = (i + 3 < tsize ? i + 3 : tsize - 1);
			for (int j = max; j >= i; j--) {
				Vector<Token> subTokens = VUtils.subVector(wordTokens, i, j + 1);
				String substr = Token.stringListConcat(subTokens).toLowerCase();
				Token start = subTokens.firstElement();
				Token end = subTokens.lastElement();

				// 9/12/2018: Find words that directly map to StringConstants. Skip the rules.
				Vector<StringConstant> scs = this.getGrammar().getSynonymStringConstants(substr);
				// if (scs != null) {
				// for (StringConstant sc : scs) {
				// TerminalAnnotation ta = new TerminalAnnotation(this, null, sc.getFullName(),
				// null, substr, start.getIndex(),
				// end.getIndex(), start.getStart(), end.getEnd(), start.getWordIndex(),
				// end.getWordIndex(), null, sc.getType());
				// Interpretation si = Interpretation.create(this, null, sc, null,
				// null, sc.getType());
				// ta.setSemanticInterpretation(si);
				// ta.generateSuperIndexTokens();
				// }
				// continue;
				// }

				Vector<Rule> swrules = grammar.getStopWordRules(substr);
				if (swrules != null) {
					for (Rule swrule : swrules) {
						new LabelAnnotation(this, swrule, substr, start.getIndex(), end.getIndex(), start.getStart(),
								end.getEnd(), start.getWordIndex(), end.getWordIndex());
					}
				}
				Vector<Rule> rules = grammar.getRulesByIndexToken(substr);
				boolean foundRules = (rules != null);
				Word word = null;
				if (i == j) {
					Token token = subTokens.firstElement();
					if ((word = subTokens.firstElement().getWord()) != null) {
						for (int k = 0; k < Grammar.UniversalStopwords.length; k++) {
							String swstr = Grammar.UniversalStopwords[k];
							if (word.getString().equals(swstr)) {
								this.universalStopWords = VUtils.addIfNot(this.universalStopWords, swstr);
							}
						}
						if (!foundRules) {
							for (String ptypestr : word.getPartsOfSpeech()) {
								TypeConstant ptype = Syntax.convertPartOfSpeechToAnnotationFormat(ptypestr);
								Vector<Rule> prules = grammar.getRulesByIndexToken(ptype);
								if (prules != null) {
									int x = 1;
								}
								rules = VUtils.appendIfNot(rules, prules);
							}
						}
					}
					if (token.isPunctuation() && !(Annotation.isSpecialAnnotation(token.getString()))) {
						rules = null;
					}
				}
				if (rules != null) {
					for (Rule rule : rules) {

						// 9/13/2018: If found singletons in StringConstant synonyms
						// if (rule.getPatternListCount() == 1 && scs != null) {
						// continue;
						// }

						HUtils.incrementCount(ruleCountHash, rule);
						VUtils.pushHashVector(subTokenRuleHash, subTokens, rule);
					}
				}
				// PROBLEM!!!: If a larger substring causes a smaller one to be omitted,
				// then if the rule containing the larger string is invalidated,
				// rules with the smaller substring are not activated instead. E.g.
				// the rule for "in home services" causes "home" not to be processed
				// in "lives in home".
				if (allowCoveringRuleBreak && foundRules) {
					i = j;
					break;
				}
			}
		}
		Vector<Rule> rules = HUtils.getKeys(ruleCountHash);
		if (rules != null) {
			for (Rule rule : rules) {
				int count = HUtils.getCount(ruleCountHash, rule);
				if (count < rule.getwordOnlyPatternCount()) {
					this.grammar.setRuleIsInvalid(rule);
				}
			}
		}
		Vector<Vector<Token>> subTokenLists = HUtils.getKeys(subTokenRuleHash);
		if (subTokenLists != null) {
			for (Vector<Token> subTokens : subTokenLists) {
				Vector<Rule> strules = subTokenRuleHash.get(subTokens);
				String substr = Token.stringListConcat(subTokens).toLowerCase();
				Token start = subTokens.firstElement();
				Token end = subTokens.lastElement();
				Word word = null;
				Vector<SyntacticTypeConstant> ptypes = null;
				if (subTokens.size() == 1 && ((word = subTokens.firstElement().getWord()) != null)
						&& word.getPartsOfSpeech() != null) {
					for (String pos : word.getPartsOfSpeech()) {
						SyntacticTypeConstant ptype = Syntax.convertPartOfSpeechToAnnotationFormat(pos);
						ptypes = VUtils.add(ptypes, ptype);
					}
				} else {
					SyntacticTypeConstant ptype = Syntax.getTerminalPhraseType(subTokens);
					if (ptype != null) {
						ptypes = VUtils.listify(ptype);
					}
				}
				if (ptypes != null) {
					for (SyntacticTypeConstant ptype : ptypes) {
						TagAnnotation ta = new TagAnnotation(this, null, null, ptype, substr, start.getIndex(),
								end.getIndex(), start.getStart(), end.getEnd(), start.getWordIndex(),
								end.getWordIndex(), null, null);
						int x = 1;
					}
				} else {
					TagAnnotation ta = new TagAnnotation(this, null, null, null, substr, start.getIndex(),
							end.getIndex(), start.getStart(), end.getEnd(), start.getWordIndex(), end.getWordIndex(),
							null, null);
					int x = 1;
				}
			}
		}
		if (this.stopWordLabels != null) {
			Collections.sort(this.stopWordLabels, new Annotation.TextStartSorter());
		}
	}

	public void addTokenAnnotation(Token token, String substr, Token start, Token end) {
		if (token.isNumber()) {
			new NumberAnnotation(this, substr, start.getIndex(), end.getIndex(), start.getStart(), end.getEnd(),
					start.getWordIndex(), end.getWordIndex(), token.getValue());
		} else if (token.isRegExp()) {
			RegExprToken rtoken = (RegExprToken) token;
			new RegExprAnnotation(this, substr, (RegExprToken) token);
		}
	}

	public static WordSequenceAnnotation createWordSequenceAnnotation(Grammar grammar, Sentence sentence,
			StructureAnnotation structureAnnotation, boolean useIndexFinder) {
		grammar.initializeChart(sentence.getTokenLength());
		WordSequenceAnnotation sa = new WordSequenceAnnotation(grammar, sentence);

		// 4/16/2015
		if (structureAnnotation != null) {
			grammar.addAnnotation(structureAnnotation);
		}

		sa.gatherNarrativeAnnotations();

		if (useIndexFinder) {
			sa.addTokenIndexFinderAnnotations(grammar, sentence);
		}
		return sa;
	}

	public void addTokenIndexFinderAnnotations(Grammar analysis, Sentence sentence) {
		Vector<Token> wtokens = Token.gatherWordTokens(sentence.tokens);
		Vector cws = UMLSStructuresShort.getUMLSStructures().getCUIStructureWrappers(wtokens, null, true);
		if (cws != null) {
			for (ListIterator li = cws.listIterator(); li.hasNext();) {
				CUIStructureWrapperShort cw = (CUIStructureWrapperShort) li.next();
				CUIStructureShort cs = cw.getCuiStructure();
				TypeConstant tc = cs.getType();
				if (!(tc instanceof UMLSTypeConstant)) {
					continue;
				}
				UMLSTypeConstant utc = (UMLSTypeConstant) tc;

				if (utc != null && !utc.isConnectedToOntology()) {
					continue;
				}
				String tindex = Annotation.getTypeIndex(utc);
				String tui = cw.getCuiStructure().getTUI();

				if (!(analysis.getRulesByIndexToken(tindex) != null
						|| this.grammar.getGrammarModule().getTypeInfo().isGenerallyRelevantTUI(tui))) {
					continue;
				}

				if (!cs.isValidPhrase()) {
					continue;
				}

				String cui = cs.getCui();
				String concept = cs.getWordString(false);
				concept = AnnotationIntegrationMaps.getName(cui, concept);
				Token start = cw.getTokens().firstElement();
				Token end = cw.getTokens().lastElement();
				String substr = getDocumentText().substring(start.getStart(), end.getEnd() + 1).toLowerCase();
				SyntacticTypeConstant ptype = Syntax.getTerminalPhraseType(cw.getTokens());
				TerminalAnnotation ta = new TerminalAnnotation(this, cui, concept, ptype, substr, start.getIndex(),
						end.getIndex(), cw.getTextStart(), cw.getTextEnd(), start.getWordIndex(), end.getWordIndex(),
						null, utc);
			}
		}
	}

	void addAnnotation(Annotation annotation) {
		this.annotations = VUtils.add(this.annotations, annotation);
	}

	public Vector<LabelAnnotation> getStopWordLabels() {
		return stopWordLabels;
	}

	public void addStopWordLabel(LabelAnnotation annotation) {
		this.stopWordLabels = VUtils.add(this.stopWordLabels, annotation);
	}

	public String toString() {
		String str = this.string;
		if (str.length() > 100) {
			str = str.substring(0, 100) + "...";
		}
		str = "\"" + str + "\"";
		return str;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}

	public Vector<Annotation> getAnnotations() {
		return annotations;
	}

	public Document getDocument() {
		if (this.sentence != null) {
			return this.sentence.getDocument();
		}
		return null;
	}

	public String getDocumentText() {
		if (this.sentence.getDocument() != null) {
			return this.sentence.getDocument().getText();
		}
		return this.sentence.getText();
	}

	public Vector<Annotation> getExpandedCoveringAnnotations() {
		return expandedCoveringAnnotations;
	}

	public void setExpandedCoveringAnnotations(Vector<Annotation> expandedCoveringAnnotations) {
		this.expandedCoveringAnnotations = expandedCoveringAnnotations;
	}

	public int getStartTokenIndex() {
		return startTokenIndex;
	}

	public String getString() {
		return string;
	}

	public Vector<String> getUniversalStopWords() {
		return universalStopWords;
	}

	public Vector<Token> getWordTokens() {
		return this.wordTokens;
	}

	// 6/24/2016
	public boolean containsComma(Annotation annotation) {
		for (int i = 0; i < this.numberOfCommas; i++) {
			int cstart = this.commaPositions[i];
			if (annotation.getTextStart() < cstart && cstart < annotation.getTextEnd()) {
				return true;
			}
		}
		return false;
	}

	public boolean containsPunctuation(Annotation annotation) {
		for (int i = 0; i < this.numberOfPunctuations; i++) {
			int pstart = this.punctuationPositions[i];
			if (annotation.getTextStart() < pstart && pstart < annotation.getTextEnd()) {
				return true;
			}
		}
		return false;
	}

	public Vector<Annotation> getThingAnnotations() {
		return thingAnnotations;
	}

	public static void gatherThingAnnotations(Vector<Annotation> annotations) {
		WordSequenceAnnotation wsa = null;
		Vector<Annotation> things = new Vector(0);
		if (annotations != null) {
			wsa = annotations.firstElement().getSentenceAnnotation();
			for (Annotation annotation : annotations) {
				wsa.gatherThingAnnotations(annotation, things);
			}
		}
		if (!things.isEmpty()) {
			Collections.sort(things, new Annotation.TextStartSorter());
			wsa.thingAnnotations = things;
		}
	}

	private void gatherThingAnnotations(Annotation annotation, Vector<Annotation> things) {
		TypeConstant thingType = TypeConstant.findByName("<THING>");
		if (annotation.isInterpreted()) {
			if (annotation.getConcept() instanceof StringConstant) {
				StringConstant sc = (StringConstant) annotation.getConcept();
				if (sc.getType().subsumedBy(thingType)) {
					things = VUtils.addIfNot(things, annotation);
				} else if (annotation.hasChildren()) {
					for (Annotation child : annotation.getChildAnnotations()) {
						gatherThingAnnotations(child, things);
					}
				}
			}
		}
	}

}
