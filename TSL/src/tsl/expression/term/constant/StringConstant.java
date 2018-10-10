package tsl.expression.term.constant;

import java.util.Collections;
import java.util.Vector;

import tsl.expression.term.Term;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLisp;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class StringConstant extends Constant {
	private StringConstant parent = null;
	private boolean isComplex = false;
	private Vector<StringConstant> supportedInGrammar = null;
	private int numberOfColons = 2;
	private static StringConstant EmptySC = new StringConstant();

	// 9/11/2018
	private Vector<String> synonyms = null;

	public StringConstant() {
		int x = 1;
	}

	public StringConstant(String label, TypeConstant type, boolean iscomplex) {
		super(label, type);
		this.isComplex = iscomplex;
		// 12/7/2017- Missing some complex concepts...
		if (this.name.indexOf("::") > 0) {
			this.isComplex = true;
			int numColons = 0;
			for (int i = 0; i < this.name.length(); i++) {
				char c = this.name.charAt(i);
				if (c == ':') {
					numColons++;
				}
			}
			this.numberOfColons = numColons;
		}
	}

	// e.g. (defstringconstant x y z type), (defstringconstant x), ...
	// Problem: Returns just the first constant.
	public static StringConstant createStringConstant(Vector v) {
		StringConstant sc = null;
		int x = 1;
		try {
			if (v != null && "defstringconstant".equals(v.firstElement()) && v.size() > 2) {
				String tname = (String) v.lastElement();
				TypeConstant type = KnowledgeBase.getCurrentKnowledgeBase().getTypeConstant(tname);
				int end = (type == null ? v.size() - 1 : v.size() - 2);
				for (int i = 1; i <= end; i++) {
					String label = (String) v.elementAt(i);
					sc = new StringConstant(label, type, false);
				}
			}

			// 9/12/2018
			else if (v != null && "defsynonyms".equals(v.firstElement()) && v.size() > 2) {
				String scname = (String) v.lastElement();
				StringConstant tsc = KnowledgeBase.getCurrentKnowledgeBase().getStringConstant(scname);
				if (tsc != null) {
					for (int i = 1; i < v.size() - 1; i++) {
						String syn = (String) v.elementAt(i);
						
						if (syn.equals("lower tract")) {
							x = 1;
						}
						
						tsc.addSynonym(syn);
					}
				}
				sc = EmptySC;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sc;
	}

	public static StringConstant createStringConstant(String name, TypeConstant type, boolean iscomplex) {
		if ("#NUMBER#".equals(name)) {
			int x = 1;
		}
		KnowledgeBase kb = KnowledgeEngine.getCurrentKnowledgeEngine().getCurrentKnowledgeBase();
		StringConstant sc = kb.getNameSpace().getStringConstant(name);
		if (sc == null) {
			sc = new StringConstant(name, type, iscomplex);
		}
		return sc;
	}

	public boolean equals(Object o) {
		boolean rv = (o != null && this.toString().equals(o.toString()));
		return rv;
	}

	public static char[] StringConstantFormalNameChars = new char[] {'_', '-', ':'};
	
	// e.g. ":COMPULSIVE_GAMBLING:"
	public static boolean isStringConstantFormalName(String str) {
		if (str != null && str.length() > 2 && str.charAt(0) == ':' && str.charAt(str.length() - 1) == ':'
				&& Character.isUpperCase(str.charAt(1))) {
			for (int i = 2; i < str.length() - 1; i++) {
				char c = str.charAt(i);
				if (!(Character.isUpperCase(c) || VUtils.charInArray(c, StringConstantFormalNameChars))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public StringConstant getParent() {
		return parent;
	}

	public void setParent(StringConstant parent) {
		this.parent = parent;
	}

	public String getTuffyString() {
		String cname = this.getFormalName();
		char fc = cname.charAt(0);
		if (!Character.isLetter(fc)) {
			cname = cname.substring(1, cname.length() - 1);
		}
		cname = StrUtils.replaceNonAlphaNumericCharactersWithDelim(cname, '_');
		return cname;
	}

	public boolean isComplex() {
		return isComplex;
	}

	public Sexp toSexp() {
		Sexp sexp = null;
		try {
			TLisp l = TLisp.getTLisp();
			String lstr = "'(defstringconstant \"" + this.getName() + "\" \"";
			lstr += this.getType().getFormalName() + "\")";
			sexp = (Sexp) l.evalString(lstr);
			this.setSexp(sexp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sexp;
	}

	public static Vector<String> getFormalNames(Vector<StringConstant> constants) {
		Vector<String> names = null;
		if (constants != null) {
			for (StringConstant sc : constants) {
				names = VUtils.add(names, sc.getFormalName());
			}
		}
		return names;
	}
	
	public String getFormalName() {
		if (this.formalName == null && this.name != null) {
			this.formalName = generateFormalName(this.name, ":", ":");
		}
		return this.formalName;
	}
	
//	public String getFormalName() {
//		if (this.formalName == null && this.name != null) {
//			String core = generateFormalNameCore(this.name);
//			if (!core.startsWith(":")) {
//				core = ":" + core + ":";
//			}
//			this.formalName = core;
//		}
//		return this.formalName;
//	}

	public void addSupportedGrammarConcept(StringConstant concept) {
		this.supportedInGrammar = VUtils.addIfNot(this.supportedInGrammar, concept);
	}

	public boolean directlySupportsInGrammar(StringConstant concept) {
		return this.supportedInGrammar != null && this.supportedInGrammar.contains(concept);
	}

	public boolean supportsInGrammar(StringConstant concept, int depth) {
		if (depth < 1) {
			return false;
		}
		if (this.directlySupportsInGrammar(concept)) {
			return true;
		}
		if (this.supportedInGrammar != null) {
			for (StringConstant sc : this.supportedInGrammar) {
				if (sc.supportsInGrammar(concept, depth - 1)) {
					return true;
				}
			}
		}
		return false;
	}

	public int getNumberOfColons() {
		return numberOfColons;
	}

	// 9/11/2018: Store words in grammar rules with single inputs that map to
	// a StringConstant with that StringConstant, e.g. "parent" -> ":FAMILY:"
	public void addSynonym(String synonym) {
		this.synonyms = VUtils.addIfNot(this.synonyms, synonym);
		Collections.sort(this.synonyms);
	}

	public void addSynonyms(Vector<String> synonyms) {
		this.synonyms = VUtils.appendIfNot(this.synonyms, synonyms);
		Collections.sort(this.synonyms);
	}

	public Vector<String> getSynonyms() {
		return synonyms;
	}
	
	// 9/18/2018
	public void remove() {
		super.remove();
		if (this.getType() != null) {
			this.getType().removeTypedStringConstant(this);
		}
		if (this.getOntology() != null) {
			this.getOntology().removeFromAllStringConstants(this);
		}
		this.ontology = null;
		this.synonyms = null;
	}
	
	public void removeSynonym(String synonym) {
		if (this.synonyms != null) {
			this.synonyms.remove(synonym);
			if (this.synonyms.isEmpty()) {
				this.synonyms = null;
			}
		}
	}

}
