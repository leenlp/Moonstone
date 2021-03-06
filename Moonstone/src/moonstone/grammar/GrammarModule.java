package moonstone.grammar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import moonstone.annotation.Annotation;
import moonstone.annotation.AnnotationIntegrationMaps;
import moonstone.annotation.InferenceAnnotation;
import moonstone.annotation.MissingAnnotation;
import moonstone.annotation.NumberAnnotation;
import moonstone.annotation.RegExprAnnotation;
import moonstone.annotation.StructureAnnotation;
import moonstone.annotation.TagAnnotation;
import moonstone.annotation.TerminalAnnotation;
import moonstone.annotation.WordSequenceAnnotation;
import moonstone.context.ConText;
import moonstone.information.Information;
import moonstone.io.readmission.CombinedHeaderSentence;
import moonstone.io.readmission.Readmission;
import moonstone.rule.InferenceRule;
import moonstone.rule.Rule;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import moonstone.semantic.TypeInfo;
import tsl.documentanalysis.document.Document;
import tsl.documentanalysis.document.Header;
import tsl.documentanalysis.document.HeaderContent;
import tsl.documentanalysis.document.NarrativeContent;
import tsl.documentanalysis.document.Sentence;
import tsl.documentanalysis.tokenizer.Token;
import tsl.documentanalysis.tokenizer.regexpr.RegExprToken;
import tsl.expression.Expression;
import tsl.expression.form.sentence.ImplicationSentence;
import tsl.expression.form.sentence.constraint.Constraint;
import tsl.expression.term.Term;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.function.javafunction.JavaFunctionConstant;
import tsl.expression.term.relation.JavaRelationConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypeConstant;
import tsl.inference.backwardchaining.Query;
import tsl.inference.forwardchaining.ForwardChainingInferenceEngine;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.engine.StartupParameters;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.ontology.Ontology;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLUtils;
import tsl.tsllisp.TLisp;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class GrammarModule {
	private KnowledgeEngine knowledgeEngine = null;
	private MoonstoneRuleInterface moonstoneRuleInterface = null;
	private Hashtable<String, Vector<String>> headerHash = null;
	private ConText conTextLexicon = null;
	private TypeInfo typeInfo = null;
	private Hashtable<String, Grammar> grammarHash = new Hashtable();
	private Vector<Grammar> allGrammars = null;
	private Grammar currentGrammar = null;
	private Grammar narrativeGrammar = null;
	private boolean useStructureGrammar = false;

	public static String[] UniversalStopwords = new String[] { "and", "or" };
	public static GrammarModule CurrentGrammarModule = null;
	public static String SentenceConcept = ":SENTENCE:";
	public static String AVPairConcept = ":AVPAIR:";
	public static String SectionHeaderFilename = "SectionHeaders";

	public GrammarModule(MoonstoneRuleInterface msri) {
		CurrentGrammarModule = this;
		KnowledgeEngine ke = msri.getKnowledgeEngine();
		try {
			String str = null;
			this.moonstoneRuleInterface = msri;
			this.knowledgeEngine = ke;
			this.typeInfo = new TypeInfo(this);
			Constraint.initialize();
			moonstone.constraint.TonyxJavaFunctions.initialize();
			AnnotationIntegrationMaps.initialize(this);
			this.headerHash = loadHeaders(ke.getStartupParameters().getRootDirectory());
			str = ke.getStartupParameters().getPropertyValue("ConTextLexicon");
			String fpath = null;
			if (str != null) {
				fpath = ke.getStartupParameters().getResourceFileName(str);
			}
			if (fpath != null) {
				String dstr = ke.getStartupParameters().getPropertyValue("ConTextLexiconDelimiter");
				char delim = ',';
				if (dstr != null) {
					delim = dstr.charAt(0);
				}
				this.conTextLexicon = ConText.createConText(fpath, delim);
			}
			str = ke.getStartupParameters().getPropertyValue("TSLRelationClasses");
			if (str != null) {
				readTSLExpressions(str, "relation");
			}
			str = ke.getStartupParameters().getPropertyValue("TSLFunctionClasses");
			if (str != null) {
				readTSLExpressions(str, "function");
			}
			Information.initialize();
			// readGrammars();
			loadSectionHeaders();
			this.useStructureGrammar = ke.getStartupParameters()
					.isPropertyTrue(MoonstoneRuleInterface.UseStructureGrammarPropertyName);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readGrammars() {
		readGrammars(false);
	}

	public void readGrammars(boolean useExcel) {
		int x = 2;
		String ruledir = this.moonstoneRuleInterface.getSelectedGrammarRuleDirectoryPathName();
		Vector<String> gnames = FUtils.getSubdirectoryNames(ruledir);
		this.allGrammars = null;
		this.currentGrammar = null;
		this.grammarHash = new Hashtable();

		// 7/13/2016: To reload implication sentences
		KnowledgeBase rootkb = this.knowledgeEngine.getRootKnowledgeBase();
		rootkb.clearImplicationSentenceHash();

		if (gnames != null) {
			for (String gname : gnames) {
				Grammar grammar = null;
				String lcgname = gname.toLowerCase();
				String dirname = ruledir + File.separatorChar + gname;
				if (lcgname.contains("sentence")) {
					if (useExcel) {
						grammar = new ExcelNarrativeGrammar(this, gname, dirname);
					} else {
						grammar = new NarrativeGrammar(this, gname, dirname);
					}
				} else if (lcgname.contains("document")) {
					grammar = new DocumentGrammar(this, gname, dirname);
					((DocumentGrammar) grammar).storeRuleInputTokens();
				} else if (lcgname.contains("structure")) {
					grammar = new StructureGrammar(this, gname, dirname);
				} else {
					if (useExcel) {
						grammar = new ExcelNarrativeGrammar(this, gname, dirname);
					} else {
						// grammar = new NarrativeGrammar(this, gname, dirname);
					}
				}
				if (grammar != null) {
					allGrammars = VUtils.add(allGrammars, grammar);
					this.grammarHash.put(gname, grammar);
					if (grammar instanceof NarrativeGrammar) {
						this.currentGrammar = this.narrativeGrammar = grammar;
					}
					if (grammar.getAllRules() != null) {
						System.out.println("Read grammar \"" + grammar.getName() + "\".  Number of rules = "
								+ grammar.getAllRules().size());
					}
				}
			}

			// 9/20/2018:  Add synonyms from the ontology to the sentence grammar.
			Vector<StringConstant> scs = this.getMoonstoneRuleInterface().getKnowledgeEngine().getCurrentOntology()
					.getAllStringConstants();
			for (StringConstant sc : scs) {
				if (sc.getSynonyms() != null) {
					for (String syn : sc.getSynonyms()) {
						if (this.getSentenceGrammar().getSynonymStringConstants(syn) == null) {
							this.getSentenceGrammar().addSynonymStringConstant(sc, syn);
						}
					}
				}
			}
			
			// 9/20/2018:  Add synonyms from grammar if not obtained from the ontology
			for (Rule rule : this.getSentenceGrammar().getAllRules()) {
				if (rule.getPatternListCount() == 1 && rule.getResultConcept() instanceof StringConstant) {
					StringConstant sc = (StringConstant) rule.getResultConcept();
					for (Object o : rule.getPatternLists().firstElement()) {
						if (o instanceof String && Character.isLetter(((String) o).charAt(0))) {
							String syn = (String) o;
							if (sc.getSynonyms() == null || !sc.getSynonyms().contains(syn)) {
								sc.addSynonym(syn);
							}
							if (this.getSentenceGrammar().getSynonymStringConstants(syn) == null) {
								this.getSentenceGrammar().addSynonymStringConstant(sc, syn);
							}
						}
					}
				}
			}
			

		}

		// 7/13/2016
		ForwardChainingInferenceEngine fcie = this.moonstoneRuleInterface.getForwardChainingInferenceEngine();
		if (fcie != null) {
			fcie.storeRules(rootkb.getAllImplicationSentences());
		}
	}

	public Vector<ImplicationSentence> getAllImplicationSentences() {
		Vector<ImplicationSentence> isents = null;
		for (Grammar grammar : this.getAllGrammars()) {
			if (grammar.getAllInferenceRules() != null) {
				for (InferenceRule irule : grammar.getAllInferenceRules()) {
					isents = VUtils.add(isents, irule.getImplicationSentence());
				}
			}
		}
		return isents;
	}

	public void setCurrentGrammar(Grammar grammar) {
		Grammar.CurrentGrammar = this.currentGrammar = grammar;
	}

	public Grammar getGrammar(String name) {
		Grammar grammar = this.grammarHash.get(name);
		if (grammar != null) {
			setCurrentGrammar(grammar);
		}
		return grammar;
	}

	public Grammar getNarrativeGrammar() {
		return narrativeGrammar;
	}

	public void setNarrativeGrammar(Grammar narrativeGrammar) {
		this.narrativeGrammar = narrativeGrammar;
	}

	public Grammar getSentenceGrammar() {
		return getGrammar(Grammar.SentenceGrammarName);
	}

	public Grammar getStructureGrammar() {
		return getGrammar(Grammar.StructureGrammarName);
	}

	public Grammar getDocumentGrammar() {
		return getGrammar(Grammar.DocumentGrammarName);
	}

	public void deleteAllDisplayedAnnotations() {
		for (Grammar grammar : this.allGrammars) {
			grammar.setDisplayedAnnotations(null);
		}
	}

	private static Hashtable<String, Vector<String>> loadHeaders(String rootDir) {
		Hashtable<String, Vector<String>> headerHash = new Hashtable<String, Vector<String>>();
		try {
			String headerFile = FUtils.getFileName(rootDir, "gazeteers", "Section_Headers.lst");
			File f = new File(headerFile);
			if (f.exists()) {
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line = null;
				while ((line = in.readLine()) != null) {
					if (Character.isLetter(line.charAt(0))) {
						String header = line.trim();
						if (header.charAt(header.length() - 1) == ':') {
							header = header.substring(0, header.length() - 1);
						}
						Vector<String> v = StrUtils.stringList(header, ' ');
						VUtils.pushHashVector(headerHash, v.firstElement(), header);
					}
				}
				for (Enumeration e = headerHash.keys(); e.hasMoreElements();) {
					String key = (String) e.nextElement();
					Vector<String> headers = headerHash.get(key);
					Collections.sort(headers, new StrUtils.LengthSorterDescending());
				}
			} else {
				// System.out.println("ERROR: Unable to find Gazeteer files");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return headerHash;
	}

	private void readTSLExpressions(String cstr, String type) {
		if (cstr != null) {
			Vector<String> cnames = StrUtils.stringList(cstr, ',');
			if (cnames != null) {
				try {
					for (String cname : cnames) {
						Class.forName(cname);
						if ("relation".equals(type)) {
							JavaRelationConstant.createJavaRelationConstant(cname);
						} else if ("function".equals(type)) {
							JavaFunctionConstant.createJavaFunctionConstant(cname);
						}
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 6/6/2015: Doesn't store relsents for annotations covered by larger
	// relsents with
	// the same concept. (Want to avoid storing relsents missing modifiers that
	// appear higher
	// in the parse.)

	public KnowledgeBase populateQueryKBUsingAnnotations(Vector<Annotation> annotations) {
		KnowledgeBase kb = null;
		Vector<RelationSentence> rsents = getNonConceptDuplicatedRelationSentences(annotations);
		KnowledgeBase rootkb = this.knowledgeEngine.getRootKnowledgeBase();
		if (rootkb != null && rsents != null) {
			kb = new KnowledgeBase("QueryPanel", rootkb);
			for (RelationSentence rs : rsents) {
				kb.initializeAndAddForm(rs);
			}
		}
		return kb;
	}

	public Vector<RelationSentence> getNonConceptDuplicatedRelationSentences(Vector<Annotation> annotations) {
		Vector<RelationSentence> rsents = null;
		Hashtable<Object, Annotation> chash = new Hashtable();
		if (annotations != null) {
			KnowledgeBase rootkb = this.knowledgeEngine.getRootKnowledgeBase();
			for (Annotation annotation : annotations) {
				Object c = annotation.getConcept();
				if (c != null) {
					Annotation earlier = (c != null ? chash.get(c) : null);
					if (earlier != null && earlier.covers(annotation)) {
						continue;
					}
					chash.put(c, annotation);
					Vector<RelationSentence> relations = annotation.getSemanticInterpretation().getRelationSentences();
					rsents = VUtils.append(rsents, relations);
				}
			}
		}
		return rsents;
	}

	public KnowledgeBase populateQueryKBUsingSentences(
			Vector<tsl.expression.term.relation.RelationSentence> sentences) {
		KnowledgeBase rootkb = this.knowledgeEngine.getRootKnowledgeBase();
		KnowledgeBase kb = KnowledgeBase.createAndInitialize(rootkb, "QueryPanel", sentences);
		return kb;
	}

	// &&&&
	@SuppressWarnings("unused")
	public Vector<Annotation> applyNarrativeGrammarToText(Document doc, boolean removeNested, boolean removeCoinciding,
			boolean interpretedOnly) {
		try {
			MoonstoneRuleInterface msri = this.moonstoneRuleInterface;
			Readmission rm = msri.getReadmission();
			String text = doc.getText();

			this.getSentenceGrammar().clearRuleCountHashtables();

			if (text != null && text.length() > 1) {
				doc.extractPatientNameAndDatesFromFirstLine(Readmission.DocumentDateStringFormats);
				doc.analyzeContent(msri.isUseSectionHeaderHeuristics());
				msri.setSemanticAnnotationDisplay(true);
				this.deleteAllDisplayedAnnotations();
				Vector<StructureAnnotation> structureAnnotations = null;
				Vector<Annotation> narrativeAnnotations = null;
				Vector<Annotation> allNarrativeAnnotations = null;
				Vector<Annotation> nonNestedNarrativeAnnotations = null;
				Vector<Annotation> nonNestedNonCoincidentNarrativeAnnotations = null;

				Grammar structureGrammar = this.getStructureGrammar();
				Grammar narrativeGrammar = this.getSentenceGrammar();
				boolean doDocumentParse = removeNested;
				for (Header header : doc.getHeaders()) {
					Vector<WordSequenceAnnotation> sentenceAnnotations = null;
					if (isInvalidHeader(header.getText())) {
						continue;
					}
					if (msri.isDisplayRelevantSectionHeadersOnly() && !header.isFromSectionHeaderList()) {
						continue;
					}
					if (msri.getReadmission() != null) {
						if (!msri.getReadmission().MoonstoneHeaderTextisValid(header.getText())) {
							continue;
						}
					}
					if (header.getContent() == null) {
						continue;
					}
					if (!this.isUseStructureGrammar()) {
						Vector<Annotation> smas = this
								.applyNarrativeGrammarRules(((NarrativeContent) header.getContent()));

						// 11/13/2016
						// storeForGarbageCollection(smas);

						Vector<Annotation> nnsmas = Annotation.getNonNestedAnnotations(smas);

						// 7/6/2016
						// this.grammarEBL.assignInferredTargetConcepts(nnsmas);
						Vector<Annotation> nnncsmas = Annotation.getNonCoincidingAnnotations(nnsmas, interpretedOnly);

						// 6/13/2018: Reference resolution prep
						// Vector<Annotation> thingAnnotations = gatherThingAnnotations(nnncsmas);
						// this.resolveReferences(thingAnnotations);

						allNarrativeAnnotations = VUtils.append(allNarrativeAnnotations, smas);
						nonNestedNarrativeAnnotations = VUtils.append(nonNestedNarrativeAnnotations, nnsmas);
						nonNestedNonCoincidentNarrativeAnnotations = VUtils
								.append(nonNestedNonCoincidentNarrativeAnnotations, nnncsmas);

					} else {

					}
				}
				if (doDocumentParse) {

					// 12/15/2017: TEMPORARY: Just using all nonnested annotations. This will
					// produce a lot of
					// duplicates, but will allow all targets through...

					// Vector<Annotation> input = ((NarrativeGrammar) this.getSentenceGrammar())
					// .gatherDocumentGrammarInput(nonNestedNarrativeAnnotations,
					// nonNestedNarrativeAnnotations);

					int x = 2;
					Vector<Annotation> input = ((NarrativeGrammar) this.getSentenceGrammar())
							.gatherDocumentGrammarInput(nonNestedNonCoincidentNarrativeAnnotations,
									nonNestedNarrativeAnnotations);
					this.applyDocumentGrammarRules(doc, input);
					// storeForGarbageCollection(this.getDocumentGrammar()
					// .getAllAnnotations());
				}

				// 7/23/2018
				if (this.moonstoneRuleInterface.isPrintRuleStatistics()) {
					this.getSentenceGrammar().printRuleInstanceCounts();
				}

				if (removeNested && removeCoinciding) {
					narrativeAnnotations = nonNestedNonCoincidentNarrativeAnnotations;
				} else if (removeNested) {
					narrativeAnnotations = nonNestedNarrativeAnnotations;
				} else {
					narrativeAnnotations = allNarrativeAnnotations;
				}
				if (structureAnnotations != null && structureGrammar != null) {
					structureGrammar.setDisplayedAnnotations(VUtils.castTypes(structureAnnotations, Annotation.class));
				}
				if (narrativeAnnotations != null && narrativeGrammar != null) {

					// 12/16/2017
					// Collections.sort(narrativeAnnotations, new Annotation.NumericIDSorter());
					Collections.sort(narrativeAnnotations, new Annotation.ConceptCommaSorter());

					narrativeGrammar.setDisplayedAnnotations(narrativeAnnotations);
					this.setCurrentGrammar(narrativeGrammar);
				}

				return narrativeAnnotations;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// Moved code out of main parse routine, 3/6/2018
	private void doStructureGrammarParse() {
		// Extremely expensive! Need new parser...
		// 9/20/2015: Not updated with changes in previous
		// segment.
		// Vector<Annotation> stas =
		// this.control.processStructureAnnotations(header.getContent());
		// Vector<Annotation> astas =
		// StructureAnnotation.extractAnalyzableStructureAnnotations(stas);
		// if (removeNested) {
		// astas = Annotation.getNonNestedAnnotations(astas);
		// }
		// if (removeCoinciding) {
		// astas = Annotation.getNonCoincidingAnnotations(astas, false);
		// }
		// structureAnnotations = VUtils.append(structureAnnotations, astas);
		// if (astas != null) {
		// Vector<Annotation> nas = this.control.applyNarrativeGrammarRules(astas);
		// if (removeNested) {
		// nas = Annotation.getNonNestedAnnotations(nas);
		// }
		// if (removeCoinciding) {
		// nas = Annotation.getNonCoincidingAnnotations(nas, interpretedOnly);
		// }
		// narrativeAnnotations = VUtils.append(narrativeAnnotations, nas);
		// }
	}

	private boolean isInvalidHeader(String hname) {
		hname = hname.toLowerCase();
		return hname.contains("sample nameXX") || hname.contains("question");
	}

	// &&&&
	public Vector<Annotation> applyDocumentGrammarRules(Document document, Vector<Annotation> annotations) {
		Vector<Annotation> results = null;
		int x = 1;
		DocumentGrammar documentGrammar = (DocumentGrammar) this.getDocumentGrammar();
		if (documentGrammar != null && annotations != null) {
			Header header = new Header(document);
			WordSequenceAnnotation wsa = null;
			documentGrammar.initializeChart(document.getTokenCount());
			for (Annotation annotation : annotations) {

				if (annotation.getText().toLowerCase().contains("balance")
						&& annotation.getNestedTargetAnnotations() != null) {
					x = 1;
				}

				if (documentGrammar.annotationContainsRuleInput(annotation)) {
					documentGrammar.addAnnotation(annotation);
				}
			}
			documentGrammar.runRuleExpansions();
			results = documentGrammar.getAllAnnotations();

			// 12/16/2017
			results = Annotation.removeDuplicates(results);

			// 11/1/2016: Keep all document annotations containing targets.
			// results =
			// Annotation.getNonNestedTargetDocumentAnnotations(results);

			// BEFORE 11/1/2016: This is eliminating valid Narrative annotations
			// with different target concepts than coinciding Document
			// annotations.
			// results = Annotation.getNonNestedNonCoincidingAnnotations(results, true);

			documentGrammar.clearAnnotationStructures();
			documentGrammar.setDisplayedAnnotations(results);
		}
		return results;
	}

	public Vector<Annotation> applyNarrativeGrammarRules(NarrativeContent nh) {
		Vector<Annotation> allAnnotations = null;
		boolean flag = true;
		if (nh.getSentences() != null) {
			if (nh.getSentences().size() == 1 && nh.getHeader().isQuestion()
					&& this.moonstoneRuleInterface.isConjoinHeaderWithSentence()) {
				Sentence first = nh.getSentences().firstElement();
				Vector tokens = VUtils.appendNew(nh.getHeader().getTokens(), first.getTokens());
				Sentence s = new CombinedHeaderSentence(nh.getHeader(), tokens, false);
				Vector<Annotation> annotations = this.applyNarrativeGrammarRules(s, null);
				allAnnotations = VUtils.append(allAnnotations, annotations);
			} else {
				for (int i = 0; i < nh.getSentences().size(); i++) {
					if (nh.hasInterveningQuestionMarkOrColon(i, i + 1)) {
						Sentence s1 = nh.getSentences().elementAt(i);
						Sentence s2 = nh.getSentences().elementAt(i + 1);
						Vector<Token> tokens = VUtils.appendNew(s1.getTokens(), s2.getTokens());
						Sentence s = new CombinedHeaderSentence(nh.getHeader(), tokens, false);
						Vector<Annotation> annotations = this.applyNarrativeGrammarRules(s, null);
						allAnnotations = VUtils.append(allAnnotations, annotations);
						i++;
					} else {
						Sentence s = nh.getSentences().elementAt(i);
						if (!s.isQuestion()) {
							Vector<Annotation> annotations = this.applyNarrativeGrammarRules(s, null);
							allAnnotations = VUtils.append(allAnnotations, annotations);

							// this.getSentenceGrammar().printRuleInstanceCounts();
						}
					}
				}
			}
		}
		return allAnnotations;
	}

	// 4/15/2015
	public Vector<Annotation> applyNarrativeGrammarRules(Vector<Annotation> sas) {
		Vector<Annotation> allAnnotations = null;
		if (sas != null) {
			for (Annotation annotation : sas) {
				if (annotation instanceof StructureAnnotation) {
					StructureAnnotation sa = (StructureAnnotation) annotation;
					Header h = sa.getSentenceAnnotation().getSentence().getHeader();
					Sentence s = new Sentence(h, sa.getTokens());

					Vector<Annotation> annotations = this.applyNarrativeGrammarRules(s, sa);

					// 4/16/2015 ??? (Ugly...)
					annotations = VUtils.collectIfNotClass(annotations, StructureAnnotation.class);

					allAnnotations = VUtils.append(allAnnotations, annotations);
				}
			}
		}
		return allAnnotations;
	}

	public Vector<Annotation> processStructureAnnotations(HeaderContent hc) {
		StructureGrammar structureGrammar = (StructureGrammar) this.getStructureGrammar();
		Vector<Annotation> v = null;
		if (structureGrammar != null) {
			this.setCurrentGrammar(structureGrammar);
			Sentence cs = hc.getCoveringSentence();
			structureGrammar.initializeChart(cs.getTokenLength());
			WordSequenceAnnotation sa = new WordSequenceAnnotation(structureGrammar, cs);
			Vector<Token> tokens = hc.getHeader().getTextTokens();
			if (tokens != null) {
				for (Token token : tokens) {
					TerminalAnnotation ta = null;
					String substr = token.getString();
					Vector<String> indexes = token.generateMoonstoneIndex();
					int tokenStart = token.getIndex();
					int tokenEnd = token.getIndex();
					int textStart = token.getStart();
					int textEnd = token.getEnd();
					int wordTokenStart = tokenStart;
					int wordTokenEnd = tokenEnd;
					if (token.isNumber()) {
						ta = new NumberAnnotation(sa, substr, tokenStart, tokenEnd, textStart, textEnd, wordTokenStart,
								wordTokenEnd, token.getValue());
					} else if (token.isRegExp()) {
						RegExprToken rtoken = (RegExprToken) token;
						if (rtoken.isDate()) {
							ta = new RegExprAnnotation(sa, substr, rtoken);
						}
					} else {
						ta = new TagAnnotation(sa, null, null, null, token.getString(), tokenStart, tokenEnd, textStart,
								textEnd, wordTokenStart, wordTokenEnd, token, null);
					}
					ta.setIndexTokens(indexes);
				}
			}
			structureGrammar.run(sa);
			v = structureGrammar.getAllAnnotations();
			// v = StructureAnnotation.extractAnalyzableStructureAnnotations(v);
			// v = Annotation.getParentlessAnnotations(v);

			// 4/17/2015
			// structureGrammar.clearAnnotationStructures();
		}
		return v;
	}

	// &&&&&
	public Vector<Annotation> applyNarrativeGrammarRules(Sentence sentence, StructureAnnotation structureAnnotation) {
		// System.out.println("Parsing sentence: " + sentence.getText());
		Grammar grammar = this.getSentenceGrammar();
		Vector<Annotation> covering = null;
		boolean useIndexFinder = this.getMoonstoneRuleInterface().isUseIndexFinder();
		WordSequenceAnnotation sa = WordSequenceAnnotation.createWordSequenceAnnotation(grammar, sentence,
				structureAnnotation, useIndexFinder);
		grammar.run(sa);
		if (sa.isValid()) {
			covering = grammar.getAllAnnotations();
		}
		return covering;
	}

	// 6/1/2015: Creates transient grammar to parse text within
	// a structure annotation. (Note: I should handle sentences in the same
	// way as avpairs: Add the sentence to the chart then have a rule that
	// creates a transient grammar, parses the sentence and returns all the
	// annotations.)
	public Annotation getCoveringNarrativeAnnotation(StructureAnnotation sa) {
		Annotation covering = null;
		Grammar oldgrammar = this.getSentenceGrammar();
		NarrativeGrammar newgrammar = new NarrativeGrammar(oldgrammar, "transient");
		this.setCurrentGrammar(newgrammar);
		Sentence newsentence = new Sentence(sa.getSentenceAnnotation().getSentence().getHeader(), sa.getTokens());
		WordSequenceAnnotation wsa = WordSequenceAnnotation.createWordSequenceAnnotation(newgrammar, newsentence, null,
				this.getMoonstoneRuleInterface().isUseIndexFinder());
		newgrammar.run(wsa);
		this.setCurrentGrammar(oldgrammar);
		Vector<Annotation> v = newgrammar.getAllCoveringInterpretedAnnotations();
		if (v != null) {
			covering = v.firstElement();
		}
		return covering;
	}

	public Vector<Annotation> applyInferenceRules(String str) {
		Document doc = new Document(str, this.headerHash);
		doc.analyzeSentencesNoHeader();
		return this.applyInferenceRules(doc.getAllSentences().firstElement());
	}

	public Vector<Annotation> applyInferenceRules(Sentence sentence) {
		Grammar grammar = this.getCurrentGrammar();
		WordSequenceAnnotation sa = WordSequenceAnnotation.createWordSequenceAnnotation(grammar, sentence, null,
				this.getMoonstoneRuleInterface().isUseIndexFinder());
		grammar.run(sa);
		Vector<Annotation> annotations = grammar.getAllAnnotations();
		Vector<Annotation> pas = Annotation.getNonNestedNonCoincidingAnnotations(annotations, true);
		Vector<Annotation> inferenceAnnotations = gatherInferredAnnotations(pas);
		return inferenceAnnotations;
	}

	private Vector<Annotation> gatherInferredAnnotations(Vector<Annotation> paannotations) {
		Grammar grammar = this.getCurrentGrammar();
		Hashtable<String, Vector<Annotation>> conceptHash = new Hashtable();
		Hashtable<String, RelationSentence> cchash = grammar.getConceptConsequentHash();

		KnowledgeBase kb = new KnowledgeBase("InferredAnnotations");
		this.knowledgeEngine.pushKnowledgeBase(kb);

		if (paannotations != null) {
			for (Annotation annotation : paannotations) {
				if (annotation.getSemanticInterpretation() != null) {
					Vector<RelationSentence> relations = annotation.getSemanticInterpretation().getRelationSentences();
					if (relations != null) {
						for (RelationSentence rs : relations) {
							kb.initializeAndAddForm(rs);
						}
					}
				}
				if (annotation.getRule() != null && annotation.getRule().getInferenceRules() != null) {
					for (InferenceRule rule : annotation.getRule().getInferenceRules()) {
						kb.initializeAndAddForm(rule.getImplicationSentence());
					}
				}
				if (annotation.getConcept() != null) {
					VUtils.pushHashVector(conceptHash, annotation.getConcept(), annotation);
					TypeConstant type = TypeConstant.createTypeConstant(annotation.getConcept().toString());
					RelationSentence rs = new RelationSentence("type_of", annotation, type);
					kb.initializeAndAddForm(rs);
				}
			}
		}
		Vector<Annotation> inferredAnnotations = null;
		for (String concept : AnnotationIntegrationMaps.getConcepts()) {
			Vector<Annotation> v = conceptHash.get(concept);
			String cui = AnnotationIntegrationMaps.getCUI(concept);
			if (v == null) {
				RelationSentence rs = getQuerySentence(kb, concept, cchash);
				Vector<Vector<Annotation>> sv = findViaFOLQuery(kb, concept, rs);
				if (sv != null) {
					for (Vector<Annotation> sources : sv) {
						InferenceRule irule = null;
						Annotation first = sources.firstElement();
						Expression e = kb.getContainingExpression(first);
						String text = concept;
						if (e instanceof ImplicationSentence
								&& ((ImplicationSentence) e).getContainingObject() instanceof InferenceRule) {
							irule = (InferenceRule) ((ImplicationSentence) e).getContainingObject();
						}
						InferenceAnnotation ia = new InferenceAnnotation(sources, irule, text, cui, concept);
						v = VUtils.add(v, ia);
					}
				}
			}
			inferredAnnotations = VUtils.append(inferredAnnotations, v);
		}
		this.knowledgeEngine.popKnowledgeBase();
		if (inferredAnnotations != null) {
			for (Annotation ia : inferredAnnotations) {
				ia.setProperty("directionality", ia.getPropertyWithDefault("directionality"));
				ia.setProperty("experiencer", ia.getPropertyWithDefault("experiencer"));
				ia.setProperty("temporality", ia.getPropertyWithDefault("temporality"));
			}
		}
		return inferredAnnotations;
	}

	public RelationSentence getQuerySentence(KnowledgeBase kb, String concept,
			Hashtable<String, RelationSentence> cchash) {
		RelationSentence rs = cchash.get(concept);
		try {
			if (rs == null) {
				String cstr = "'(type_of ?cc \"" + concept + "\")";
				Sexp sexp = (Sexp) TLisp.getTLisp().evalString(cstr);
				Vector v = TLUtils.convertSexpToJVector(sexp);
				rs = RelationSentence.createRelationSentence(kb, v);
				rs.setSexp(sexp);
				kb.initializeForm(rs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}

	public static Vector findViaFOLQuery(KnowledgeBase kb, String concept, RelationSentence rs) {
		Vector results = Query.doQuery(kb, rs, null, null, true);
		return results;
	}

	public static void writeToStringBuffer(Vector<Annotation> annotations, StringBuffer sb) {
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (!(annotation instanceof MissingAnnotation)) {
					String rule = (annotation.getRule() != null ? annotation.getRule().getRuleID() : "*");
					sb.append("SNIPPET:Concept=" + annotation.getConcept() + ",");
					sb.append("Cui=" + annotation.getCui() + ",");
					sb.append("Directionality=" + annotation.getDirectionality() + ",");
					sb.append("Experiencer=" + annotation.getExperiencer() + ",");
					sb.append("Temporality=" + annotation.getTemporality() + ",");
					sb.append("Rule=" + rule + ",");
					sb.append("Start=" + annotation.getTextStart() + ",");
					sb.append("End=" + annotation.getTextEnd() + "\n");
				}
			}
		}
	}

	public KnowledgeEngine getKnowledgeEngine() {
		return knowledgeEngine;
	}

	public TypeInfo getTypeInfo() {
		return typeInfo;
	}

	public Grammar getCurrentGrammar() {
		return currentGrammar;
	}

	public Vector<Grammar> getAllGrammars() {
		return allGrammars;
	}

	public ConText getConTextLexicon() {
		return conTextLexicon;
	}

	public void loadSectionHeaders() {
		StartupParameters sp = this.knowledgeEngine.getStartupParameters();
		String hpath = sp.getResourceFileName(SectionHeaderFilename);
		if (hpath != null) {
			File file = new File(hpath);
			try {
				if (file.exists()) {
					BufferedReader in = new BufferedReader(new FileReader(file));
					String line = null;
					while ((line = in.readLine()) != null) {
						if (line.length() > 2 && Character.isLetter(line.charAt(0))) {
							String header = line.trim();
							if (header.charAt(header.length() - 1) == ':') {
								header = header.substring(0, header.length() - 1);
							}
							Vector<String> words = StrUtils.stringList(header, ' ');
							VUtils.pushIfNotHashVector(headerHash, words.firstElement(), header);
						}
					}
				}
				for (Enumeration<String> e = headerHash.keys(); e.hasMoreElements();) {
					String word = e.nextElement();
					Object o = headerHash.get(word);
					Vector<String> headers = headerHash.get(word);
					Collections.sort(headers);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Vector<String> getHeaders(String word) {
		return this.headerHash.get(word);
	}

	public Hashtable<String, Vector<String>> getHeaderHash() {
		return this.headerHash;
	}

	public boolean isUseStructureGrammar() {
		return useStructureGrammar;
	}

	public void setUseStructureGrammar(boolean useStructureGrammar) {
		this.useStructureGrammar = useStructureGrammar;
	}

	public void toggleUseStructureGrammar() {
		this.useStructureGrammar = !this.useStructureGrammar;
	}

	public MoonstoneRuleInterface getMoonstoneRuleInterface() {
		return moonstoneRuleInterface;
	}

	public void addGrammar(Grammar grammar) {
		if (grammar != null) {
			this.allGrammars = VUtils.add(this.allGrammars, grammar);
			this.grammarHash.put(grammar.getName(), grammar);
			this.currentGrammar = grammar;
			if (grammar instanceof Grammar) {
				this.currentGrammar = this.narrativeGrammar = grammar;
			}
		}
	}

	public Vector<Rule> getRulesContainingUserSpecifiedPatternElement(String elementName) {
		Vector<Rule> rules = null;
		if (elementName != null) {
			for (Grammar grammar : this.getAllGrammars()) {
				if (grammar.getAllRules() != null) {
					for (Rule rule : grammar.getAllRules()) {
						boolean found = false;
						if (rule != null && rule.getPatternLists() != null) {
							for (Vector plst : rule.getPatternLists()) {
								if (!found) {
									for (Object element : plst) {
										String ename = element.toString();
										if (element instanceof Constant) {
											Constant c = (Constant) element;
											ename = c.getFormalName();
										}
										if (elementName.equals(ename)) {
											rules = VUtils.add(rules, rule);
											found = true;
											break;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return rules;
	}

	// 4/5/2018: Coreference resolution functions
	public Vector<Annotation> gatherThingAnnotations(Vector<Annotation> annotations) {
		TypeConstant thingtype = TypeConstant.findByName("thing");
		Vector<Annotation> thingAnnotations = null;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				Vector<Annotation> tas = annotation.extractTypedAnnotations(thingtype);
				thingAnnotations = VUtils.append(thingAnnotations, tas);
			}
		}
		return thingAnnotations;
	}

	// &&&&&
	public void resolveReferences(Vector<Annotation> annotations) {
		if (annotations != null) {
			Hashtable<WordSequenceAnnotation, WordSequenceAnnotation> shash = new Hashtable();
			Vector<WordSequenceAnnotation> sas = null;
			Collections.sort(annotations, new Annotation.StartPositionSorter());
			for (Annotation a : annotations) {
				shash.put(a.getSentenceAnnotation(), a.getSentenceAnnotation());
			}
			sas = HUtils.getKeys(shash);
			Collections.sort(sas, new Annotation.StartPositionSorter());

		}

	}

}
