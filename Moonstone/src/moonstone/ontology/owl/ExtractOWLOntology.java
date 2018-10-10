package moonstone.ontology.owl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import edu.utah.blulab.domainontology.Anchor;
import edu.utah.blulab.domainontology.CompoundAnchor;
import edu.utah.blulab.domainontology.DomainOntology;
import edu.utah.blulab.domainontology.LexicalItem;
import edu.utah.blulab.domainontology.LogicExpression;
import edu.utah.blulab.domainontology.Modifier;
import edu.utah.blulab.domainontology.Variable;
import moonstone.grammar.Grammar;
import moonstone.grammar.NarrativeGrammar;
import moonstone.rule.Rule;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.documentanalysis.lexicon.Lexicon;
import tsl.documentanalysis.lexicon.Word;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.relation.PatternRelationSentence;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.ontology.Ontology;
import tsl.knowledge.ontology.TypeRelationSentence;
import tsl.tsllisp.Sexp;
import tsl.utilities.FUtils;
import tsl.utilities.ListUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.TimeUtils;
import tsl.utilities.VUtils;

public class ExtractOWLOntology {
	private KnowledgeEngine knowledgeEngine = null;
	private KnowledgeBase knowledgeBase = null;
	private Ontology ontology = null;
	private Grammar grammar = null;
	private DomainOntology domain = null;
	private MoonstoneRuleInterface moonstoneRuleInterface = null;
	private Vector<Rule> newrules = null;
	private Hashtable<Object, TypeConstant> typeHash = new Hashtable();
	private Hashtable<TypeConstant, TypeConstant> allTypeHash = new Hashtable();
	private Hashtable<Object, StringConstant> stringConstantHash = new Hashtable();
	private ArrayList<Modifier> modifierDictionary = null;
	private ArrayList<Anchor> anchorDictionary = null;
	private ArrayList<Variable> variableDictionary = null;
	private ArrayList<CompoundAnchor> compoundAnchorDictionary = null;
	private Vector<PatternRelationSentence> patternRelationSentences = null;

	public ExtractOWLOntology(MoonstoneRuleInterface msri) {
		this.moonstoneRuleInterface = msri;
	}

	public void setDomain(String uri) {
		try {
			System.out.println("OWL:  Extracting ontology from " + uri);
			
			// 10/10/2018:  Temporarily disabled.  I have removed Maven character to 
			// simplify GitHub upload, and am not in sync with DomainOntology project.
			// this.domain = new DomainOntology(uri);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void analyze(boolean restoreOntology, boolean useDomainVariables) {
		this.knowledgeEngine = this.moonstoneRuleInterface.getKnowledgeEngine();
		KnowledgeBase oldkb = this.knowledgeEngine.getCurrentKnowledgeBase();
		Ontology oldontology = this.knowledgeEngine.getCurrentOntology();
		this.knowledgeBase = new KnowledgeBase("OWLKB");
		this.ontology = new Ontology("OWLONT");
		this.ontology.setKnowledgeBase(this.knowledgeBase);
		this.knowledgeBase.setOntology(this.ontology);
		this.knowledgeEngine.setCurrentOntology(this.ontology);
		this.knowledgeEngine.setCurrentKnowledgeBase(this.knowledgeBase);
		this.grammar = new NarrativeGrammar(this.moonstoneRuleInterface.getControl(), "test", null);
		try {
			this.variableDictionary = domain.getAllVariables();
			this.modifierDictionary = domain.createModifierDictionary();
			this.anchorDictionary = domain.createAnchorDictionary();
			this.compoundAnchorDictionary = domain.createCompoundAnchorDictionary();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		ArrayList<String> swrl = this.domain.getSWRLRules();
		if (swrl != null) {
			System.out.println("FOUND SWRL RULES!!!");
		}

		// 6/6/2017
		this.extractAllTypeConstants();

		for (Variable var : this.variableDictionary) {
			this.extractVariableConstants(var);
		}

		for (Modifier mod : this.modifierDictionary) {
			this.extractModifierConstants(mod);
		}

		for (Anchor anchor : this.anchorDictionary) {
			this.extractAnchorConstants(anchor);
		}

		this.ontology.setupTypeConnectionsWithExpandedRelations();

		this.attachOWLTypes();

		this.extractTypeRelations();

		this.extractGrammarRules(useDomainVariables);

		String tstr = this.knowledgeBase.getOntology().getNewlineTypeHierarchyString();
		System.out.println("\n\n" + tstr + "\n\n");

		this.storeGrammarRules();
		this.storeOntologyDefinition();

		if (restoreOntology) {
			this.knowledgeEngine.setCurrentKnowledgeBase(oldkb);
			this.knowledgeEngine.setCurrentOntology(oldontology);
		} else {
			this.addRulesToGrammar();
		}

	}

	private void extractTypeRelations() {
		for (OWLOntology ont : this.domain.getOntology().getImports()) {
			Set<OWLObjectProperty> ops = ont.getObjectPropertiesInSignature();
			for (OWLObjectProperty op : ops) {
				String relname = this.extractShortURLName(op.getIRI().toString());
				
				// 10/10/2018:  Temporarily disabled.  I have removed Maven character to 
				// simplify GitHub upload, and am not in sync with DomainOntology project.
				Set<OWLClassExpression> domains = null;
				Set<OWLClassExpression> ranges = null;
//				Set<OWLClassExpression> domains = op.getDomains(ont);
//				Set<OWLClassExpression> ranges = op.getRanges(ont);
				
				if (!domains.isEmpty() || !ranges.isEmpty()) {
					for (OWLClassExpression domain : domains) {
						String dname = this.extractShortURLName(domain.toString());
						for (OWLClassExpression range : ranges) {
							String rname = this.extractShortURLName(range.toString());
							TypeConstant dtc = TypeConstant.getType(dname);
							TypeConstant rtc = TypeConstant.getType(rname);
							if (dtc != null && rtc != null) {
								RelationConstant rc = RelationConstant.createRelationConstant(relname, null, null);
								TypeRelationSentence trs = new TypeRelationSentence(rc, dtc, rtc);
								PatternRelationSentence prs = new PatternRelationSentence(trs, "?0", "?1");
								this.patternRelationSentences = VUtils.add(this.patternRelationSentences, prs);
							}
						}
					}
				}
			}
		}
	}

	// 6/6/2017 TEST
	private void extractAllTypeConstants() {
		Set<OWLClass> allclasses = this.domain.getOntology().getClassesInSignature();
		for (OWLClass oc : allclasses) {
			String uri = oc.toString();
			uri = StrUtils.trim(uri, new char[] { '<', '>' }, false);
			String tname = this.extractShortURLName(uri);
			extractTypeConstants(null, uri);
		}
	}

	private TypeConstant extractTypeConstants(Object item, String uri) {
		OWLClass oc = this.domain.getClass(uri);
		String tname = this.extractShortURLName(uri);
		TypeConstant tc = TypeConstant.createTypeConstant(tname);
		this.ontology.addTypeConstant(tc);
		this.typeHash.put(tname, tc);
		if (item != null) {
			this.typeHash.put(item, tc);
		}
		this.allTypeHash.put(tc, tc);
		ArrayList<String> pclasses = this.domain.getDirectSuperClasses(oc);
		for (String puri : pclasses) {
			TypeConstant ptc = extractTypeConstants(null, puri);
			if (ptc != null) {
				tc.addParent(ptc);
			}
		}
		return tc;
	}

	private void extractModifierConstants(Modifier mod) {
		String mname = mod.getModName();
		String mtype = mod.getModifierType();
		TypeConstant mtc = extractTypeConstants(mod, mod.getUri());
		this.typeHash.put(mod, mtc);
		this.typeHash.put(mname, mtc);
		String MoonstoneName = getMoonstoneFormatStringConstantName(mname);
		StringConstant msc = StringConstant.createStringConstant(MoonstoneName, mtc, false);
		msc.setType(mtc);
		mtc.addTypedStringConstant(msc);
		this.stringConstantHash.put(mname, msc);
		this.stringConstantHash.put(mod, msc);
	}

	private void extractVariableConstants(Variable var) {
		String vname = var.getVarName();
		String vtype = var.getVariableType();
		TypeConstant vtc = this.extractTypeConstants(var, var.getURI());
		String MoonstoneName = getMoonstoneFormatStringConstantName(vname);
		StringConstant vsc = StringConstant.createStringConstant(MoonstoneName, vtc, false);
		this.stringConstantHash.put(vname, vsc);
		this.stringConstantHash.put(var, vsc);
	}

	private void extractAnchorConstants(Anchor anchor) {
		String aname = anchor.getPrefTerm();
		TypeConstant atc = this.extractTypeConstants(anchor, anchor.getURI());
		String MoonstoneName = getMoonstoneFormatStringConstantName(aname);
		StringConstant asc = StringConstant.createStringConstant(MoonstoneName, atc, false);
		this.stringConstantHash.put(aname, asc);
		this.stringConstantHash.put(anchor, asc);
	}

	private void attachOWLTypes() {
		TypeConstant root = TypeConstant.createTypeConstant("root");
		this.ontology.addTypeConstant(root);
		root.setRoot();
		TypeConstant owltc = TypeConstant.createTypeConstant("owl");
		this.ontology.addTypeConstant(owltc);
		owltc.addParent(root);
		for (TypeConstant tc : this.allTypeHash.keySet()) {
			if (tc.getParents() == null || tc.getParents().isEmpty()) {
				tc.addParent(owltc);
			}
		}
	}

	private void extractGrammarRules(boolean useDomainVariables) {
		String timestr = TimeUtils.getDateTimeString();
		int x = 1;
		for (Modifier modifier : this.modifierDictionary) {
			String modname = modifier.getModName();
			if (modname.toLowerCase().contains("but")) {
				x = 1;
			}
			TypeConstant tc = this.typeHash.get(modifier);
			StringConstant msc = this.stringConstantHash.get(modifier);
			Vector patternList = this.extractModifierPatternList(modifier);
			if (patternList != null) {
				Vector<Vector> wlst = VUtils.listify(patternList);
				String rname = "OWL-MODIFIER-" + modname + "-" + timestr;
				Rule rule = this.extractGrammarRule(rname, modifier.getUri(), tc, msc.getFormalName(), wlst, null, null,
						"owl-modifier", false);
				// 6/12/2017: Why did I add this?
				// if (!this.grammar.rulePatternExists(rule)) {
				// this.grammar.addRule(rule);
				// }
			}
		}
		if (useDomainVariables) {
			for (Variable var : this.variableDictionary) {
				String vname = var.getVarName();
				TypeConstant tc = this.typeHash.get(var);
				StringConstant vsc = this.stringConstantHash.get(vname);
				HashMap<String, LogicExpression<Modifier>> mmap = var.getModifiers();
				for (String iri : mmap.keySet()) {
					Vector patternList = this.extractVariablePatternList(var, iri);
					if (patternList != null) {
						String iristr = this.extractShortURLName(iri);
						String rname = "OWL-VARIABLE-" + vname + "-" + iristr + "-" + timestr;
						Vector<Vector> ppreds = VUtils.listify(VUtils.listify(iristr, "?0"));

						// 6/19/2017: TEST. Just use dictionary entries and
						// abstract
						// relations for
						// Moonstone rules.
						Rule rule = this.extractGrammarRule(rname, var.getURI(), tc, vsc.getFormalName(), patternList,
								ppreds, null, "owl-variable", true);
					}
				}
			}
		}
		for (Anchor anchor : this.anchorDictionary) {
			TypeConstant tc = this.typeHash.get(anchor);
			StringConstant asc = this.stringConstantHash.get(anchor);
			Vector patternList = this.extractAnchorPatternList(anchor);
			if (patternList != null) {
				Vector<Vector> wlst = VUtils.listify(patternList);
				String rname = "OWL-ANCHOR-" + tc.getName() + "-" + timestr;
				Rule rule = this.extractGrammarRule(rname, anchor.getURI(), tc, asc.getFormalName(), wlst, null, null,
						"owl-anchor", false);
			}
		}

		// 6/7/2017
		if (this.patternRelationSentences != null) {
			for (PatternRelationSentence prs : this.patternRelationSentences) {
				TypeRelationSentence trs = prs.getTypeRelationSentence();
				Vector<Vector> wlst = new Vector(0);
				wlst.add(VUtils.listify(trs.getSubjectType().getFormalName()));
				wlst.add(VUtils.listify(trs.getModifierType().getFormalName()));
				String rname = "OWL-RELATION-" + trs.getRelation().getName() + "-" + trs.getSubject().getName() + "-"
						+ trs.getModifier().getName() + "-" + timestr;
				Rule rule = this.extractGrammarRule(rname, "NOURI", trs.getSubjectType(), "?0", wlst, null,
						VUtils.listify(prs), "owl-relation", false);
			}
		}
	}

	private Vector<Vector<String>> extractVariablePatternList(Variable var, String modiri) {
		Vector<Vector<String>> wlist = null;
		StringConstant vsc = this.stringConstantHash.get(var.getVarName());
		HashMap<String, LogicExpression<Modifier>> mmap = var.getModifiers();
		ArrayList<LogicExpression<Anchor>> alist = var.getAnchor();
		Vector<Vector<String>> modwlist = null;
		Vector<String> modwlst = null;
		Vector<String> anchorwlst = null;
		LogicExpression<Modifier> mods = mmap.get(modiri);
		for (Modifier mod : mods) {
			StringConstant msc = this.stringConstantHash.get(mod.getModName());
			if (msc != null) {
				modwlst = VUtils.add(modwlst, msc.getName());
			}
		}
		for (LogicExpression<Anchor> le : alist) {
			String tstr = le.getType();
			for (Anchor anchor : le) {
				StringConstant asc = this.stringConstantHash.get(anchor);
				if (asc == null) {
					asc = this.stringConstantHash.get(anchor.getPrefTerm());
				}
				if (asc != null) {
					anchorwlst = VUtils.add(anchorwlst, asc);
				}
			}
		}
		if (modwlst != null && anchorwlst != null) {
			wlist = VUtils.add(wlist, modwlst);
			wlist = VUtils.add(wlist, anchorwlst);
		}
		return wlist;
	}


	private Vector<Vector<String>> extractAnchorPatternList(Anchor anchor) {
		ArrayList<String> wlist = new ArrayList();
		wlist = (ArrayList) ListUtils.add(wlist, anchor.getPrefTerm());
		wlist = (ArrayList) ListUtils.appendIfNot(wlist, anchor.getSynonym());
		wlist = (ArrayList) ListUtils.appendIfNot(wlist, anchor.getMisspelling());
		Vector v = VUtils.listToVector(wlist);
		v = VUtils.flatten(v);
		Vector newv = new Vector(0);
		for (String str : (Vector<String>) v) {
			newv = VUtils.addIfNot(newv, str.toLowerCase());
		}
		return newv;
	}

	private Vector<Vector<String>> extractModifierPatternList(Modifier modifier) {
		String modname = modifier.getModName();
		if ("lung".equals(modname.toLowerCase())) {
			int x = 1;
		}
		ArrayList<String> wlist = new ArrayList();
		Vector newv = null;
		for (LexicalItem li : modifier.getItems()) {
			String pt = li.getPrefTerm();
			ArrayList sy = li.getSynonym();
			ArrayList ms = li.getMisspelling();
			if (pt != null && pt.length() > 0) {
				String cname = pt.getClass().getName();
				wlist = (ArrayList) ListUtils.add(wlist, li.getPrefTerm());
			}
			if (!sy.isEmpty()) {
				wlist = (ArrayList) ListUtils.appendIfNot(wlist, li.getSynonym());
			}
			if (!ms.isEmpty()) {
				wlist = (ArrayList) ListUtils.appendIfNot(wlist, li.getMisspelling());
			}
		}
		if (!wlist.isEmpty()) {
			Vector v = VUtils.listToVector(wlist);
			v = VUtils.flatten(v);
			newv = new Vector(0);
			for (String str : (Vector<String>) v) {
				newv.add(str.toLowerCase());
			}
		} else {
			String lcword = modname.toLowerCase();
			Word word = Lexicon.currentLexicon.getWord(lcword);
			if (word != null && word.isNoun()) {
				newv = VUtils.listify(lcword);
			}
		}
		return newv;
	}

	// 6/7/2017: Trying to use String for concept
	private moonstone.rule.Rule extractGrammarRule(String ruleID, String uri, TypeConstant type, String conceptString,
			Vector<Vector> wlsts, Vector<Vector> propertyPredicates, Vector<PatternRelationSentence> relationPredicates,
			String flag, boolean isOrdered) {
		moonstone.rule.Rule newrule = null;
		if (conceptString != null) {
			newrule = new moonstone.rule.Rule();
			newrule.setRuleID(ruleID);
			newrule.setUri(uri);
			newrule.setType(type);
			newrule.setResultConcept(conceptString);
			newrule.setPatternLists(wlsts);
			newrule.setPropertyPredicates(propertyPredicates);
			newrule.setSemanticRelations(relationPredicates);
			newrule.addFlag(flag);
			if (isOrdered) {
				newrule.setOrdered(true);
			}
			Sexp sexp = newrule.toSexp(false);
			newrule.setSexp(sexp);
			this.newrules = VUtils.add(this.newrules, newrule);
		}
		return newrule;
	}

	// private moonstone.rule.Rule extractGrammarRule(String ruleID, String uri,
	// StringConstant sc, Vector<Vector> wlsts,
	// Vector<Vector> propertyPredicates, Vector<PatternRelationSentence>
	// relationPredicates) {
	// moonstone.rule.Rule newrule = null;
	// if (sc != null) {
	// newrule = new moonstone.rule.Rule();
	// newrule.setRuleID(ruleID);
	// newrule.setUri(uri);
	// newrule.setType(sc.getType());
	// newrule.setResultConcept(sc);
	// newrule.setPatternLists(wlsts);
	// newrule.setPropertyPredicates(propertyPredicates);
	// newrule.setSemanticRelations(relationPredicates);
	// Sexp sexp = newrule.toSexp(false);
	// newrule.setSexp(sexp);
	// this.newrules = VUtils.add(this.newrules, newrule);
	// }
	// return newrule;
	// }

	private String getMoonstoneFormatStringConstantName(String cname) {
		String cstr = cname.toUpperCase().trim();
		String str = ":";
		for (int i = 0; i < cstr.length(); i++) {
			char c = cstr.charAt(i);
			if (!Character.isLetterOrDigit(c)) {
				str += '_';
			} else {
				str += c;
			}
		}
		str += ':';
		return str;
	}

	private String extractShortURLName(String uri) {
		int index = uri.lastIndexOf('#');
		if (index > 0) {
			String shortname = uri.substring(index + 1);
			shortname = StrUtils.trim(shortname, new char[] { '<', '>' }, false);
			return shortname.toLowerCase();
		}
		return uri;
	}

	public Vector<Rule> getNewrules() {
		return newrules;
	}

	// LEE TEST:
	public ArrayList<LexicalItem> getItems(String uri) {
		ArrayList<LexicalItem> items = new ArrayList<LexicalItem>();
		for (String item : domain.getAllIndividualURIs(domain.getClass(uri))) {
			items.add(new LexicalItem(item, domain));
		}
		return items;
	}

	public void storeGrammarRules() {
		String fname = "OWLGrammarFile";
		String fpath = this.moonstoneRuleInterface.getSelectedGrammarRuleDirectoryPathName() + File.separatorChar
				+ fname;
		StringBuffer sb = new StringBuffer();
		sb.append("'(\n\twordrule\n\n");
		for (Rule rule : this.newrules) {
			sb.append(rule.getSexp().toNewlinedString() + "\n");
		}
		sb.append("\n)\n");
		FUtils.writeFile(fpath, sb.toString());
		System.out.println("OWL Grammar rules: \n" + sb.toString());
	}

	public void storeOntologyDefinition() {
		String fname = this.moonstoneRuleInterface.getStartupParameters().getPropertyValue("OWLOntologyFile");
		if (fname != null) {
			String fpath = this.moonstoneRuleInterface.getResourceDirectoryName() + File.separatorChar + fname;
			String tdefstr = this.ontology.toDefinitionString();
			tdefstr = "'(\n\n" + tdefstr + "\n)\n";
			FUtils.writeFile(fpath, tdefstr);
		}
	}

	public void addRulesToGrammar() {
		checkForLungRule();
		if (this.getNewrules() != null) {
			this.moonstoneRuleInterface.getControl().readGrammars();
			Grammar grammar = this.moonstoneRuleInterface.getSentenceGrammar();
			for (Rule rule : this.getNewrules()) {
				if (grammar.getRuleByID(rule.getRuleID()) == null) {
					grammar.addRule(rule);
				} else {
					int x = 1;
				}
			}
		}
		checkForLungRule();
	}

	private void checkForLungRule() {
		for (Rule rule : this.getNewrules()) {
			if (grammar.getRuleByID(rule.getRuleID()) == null) {
				for (Vector plst : rule.getPatternLists()) {
					for (Object token : plst) {
						if ("lung".equals(token.toString().toLowerCase())) {
							int x = 1;
						}
					}
				}
			}
		}
	}

}
