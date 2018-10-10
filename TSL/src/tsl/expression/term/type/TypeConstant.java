package tsl.expression.term.type;

import java.util.Hashtable;
import java.util.Vector;

import tsl.expression.form.sentence.Sentence;
import tsl.expression.form.sentence.constraint.Constraint;
import tsl.expression.term.Term;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.property.PropertyConstant;
import tsl.expression.term.property.PropertySentence;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.variable.Variable;
import tsl.inference.backwardchaining.Query;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.knowledgebase.NameSpace;
import tsl.knowledge.ontology.TypeRelationSentence;
import tsl.knowledge.ontology.umls.UMLSTypeInfo;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLisp;
import tsl.utilities.VUtils;

/*
 (deftype person
 (isa human artificer)
 (relata person) ;; A list of types that may appear in descriptions of people.  Can use for 
 ;; feature vectors, etc.
 (parameters 
 (?a age (property age) (default 10) (type number))
 (?s sex (property sex) (default male) (type gender))
 (?p parents (type person) (cardinality <= 2))
 (description <some sentence>)
 (constraints (and (>= ?a 0) (<= ?a 100)))
 (integration <some sentence that integrates the TypeTemplate with the KB>)
 (comment "")
 )	
 */
public class TypeConstant extends Constant {
	private static final long serialVersionUID = 1L;
	protected Vector defv = null;
	protected Sexp sexp = null;
	protected Vector<String> parameters = null;
	protected Hashtable<Object, Variable> parameterVariableHash = new Hashtable();
	protected Hashtable<Object, String> variableParameterHash = new Hashtable();
	protected Hashtable<String, Constant> paramConstantHash = new Hashtable();
	protected Vector<PropertyConstant> propertyConstants = null;
	protected Constraint constraints = null;
	protected Sentence definition = null;
	protected Vector<RelationConstant> definitionRelations = null;
	protected Sentence integration = null;
	protected Vector<TypeRelationSentence> typeRelationSentences = null;
	protected Vector<TypeConstant> unifiableTypes = null;
	protected Vector<TypeConstant> connectedTypes = null;
	// protected Vector<TypeRelationSentence> relations = null;
	protected Hashtable<String, Object> defaultHash = new Hashtable();
	protected Vector<String> MoonstoneLabels = null;
	protected boolean isRoot = false;
	protected Vector<StringConstant> typedStringConstants = null;
	protected Hashtable<TypeConstant, Vector<TypeConstant>> basePathHash = new Hashtable();
	protected Vector<TypeConstant> baseTypes = null;

	// 5/6/2017
	protected Vector<TypeRelationSentence> unifiableTypeRelationSentences = null;
	protected Vector<PropertySentence> unifiablePropertySentences = null;

	// 7/2/2013: Taken from UMLSTypeConstant, since many ontologies/types can
	// exist in the CUIStructure file.
	protected UMLSTypeInfo typeInfo = null;

	private static String undefinedBaseTypeName = "undefined";

	public TypeConstant(String name) {
		super(name);
		this.getFormalName();
		this.setType(this);
	}

	public static TypeConstant createTypeConstant(Vector v) {
		if (v != null && "deftype".equals(v.firstElement())) {
			String name = (String) v.elementAt(1);
			TypeConstant type = new TypeConstant(name);
			type.defv = v;
			return type;
		}
		return null;
	}

	public static TypeConstant createTypeConstant(String name) {
		TypeConstant type = createTypeConstant(name, null);
		return type;
	}

	public static TypeConstant createTypeConstant(String name, String fullName) {
		if (name != null && name.contains("number")) {
			int x = 1;
		}
		TypeConstant tc = null;
		if (name != null) {
			KnowledgeBase kb = KnowledgeEngine.getCurrentKnowledgeEngine().getCurrentKnowledgeBase();
			if (fullName == null) {
				fullName = name;
			}
			tc = kb.getNameSpace().getTypeConstant(name);
			if (tc == null) {
				tc = new TypeConstant(name);
				tc.setFullName(fullName);
			}
		}
		return tc;
	}
	
	public void removeFromParents() {
		if (this.getParents() != null) {
			for (Term term : this.getParents()) {
				TypeConstant parent = (TypeConstant) term;
				parent.getChildren().remove(this);
			}
			this.setParents(null);
		}
	}

	// 9/18/2018
	public void remove() {
		super.remove();
		this.removeFromParents();
		if (this.getChildren() != null) {
			while (!this.getChildren().isEmpty()) {
				TypeConstant child = (TypeConstant) this.getChildren().firstElement();
				child.remove();
			}
			this.setChildren(null);
		}
		this.setChildren(null);
		if (this.getTypedStringConstants() != null) {
			while (!this.getTypedStringConstants().isEmpty()) {
				StringConstant sc = this.getTypedStringConstants().firstElement();
				sc.remove();
			}
			this.typedStringConstants = null;
		}
		if (this.getOntology() != null) {
			this.getOntology().removeFromAllTypeConstants(this);
		}
		this.ontology = null;
	}

	public void resolve() {
		KnowledgeEngine ke = KnowledgeEngine.getCurrentKnowledgeEngine();
		this.knowledgeBase = ke.getCurrentKnowledgeBase();
		NameSpace ns = this.knowledgeBase.getNameSpace();
		this.ontology = ke.getCurrentOntology();
		this.knowledgeBase = ke.getCurrentKnowledgeBase();
		this.knowledgeBase.clearFields();

		if (VUtils.assoc("isroot", defv) != null) {
			this.isRoot = true;
		}
		Vector<String> parentids = VUtils.rest(VUtils.assoc("isa", defv));
		if (parentids != null) {
			for (String pid : parentids) {
				TypeConstant ptc = ns.getTypeConstant(pid);
				this.addParent(ptc);
			}
		}
		Vector<Vector> pvs = VUtils.rest(VUtils.assoc("parameters", defv));
		if (pvs != null) {
			for (Vector pv : pvs) {
				String vname = (String) pv.elementAt(0);
				Variable var = (Variable) this.knowledgeBase.getTerm(this, vname);
				this.addVariable(var);
				String paramname = (String) pv.elementAt(1);
				String propname = (String) VUtils.assocValue("property", pv);
				String typename = (String) VUtils.assocValue("type", pv);
				Object deflt = VUtils.assocValue("default", pv);
				this.parameters = VUtils.add(this.parameters, paramname);
				this.parameterVariableHash.put(vname, var);
				this.parameterVariableHash.put(paramname, var);
				this.variableParameterHash.put(vname, paramname);
				if (propname != null) {
					PropertyConstant pc = ns.getPropertyConstant(propname);
					this.paramConstantHash.put(vname, pc);
					this.paramConstantHash.put(paramname, pc);
					this.propertyConstants = VUtils.add(this.propertyConstants, pc);
					this.parameterVariableHash.put(pc, var);
					if (deflt != null) {
						this.defaultHash.put(paramname, deflt);
					}
				} else if (typename != null) {
					RelationConstant rc = RelationConstant.createRelationConstant(paramname);
					this.paramConstantHash.put(vname, rc);
					this.paramConstantHash.put(paramname, rc);
					TypeConstant ptype = ns.getTypeConstant(typename);
					var.setType(ptype);
					this.parameterVariableHash.put(ptype, var);
					this.parameterVariableHash.put(rc, var);
				}
			}
		}
		Vector cv = (Vector) VUtils.assocValue("constraints", this.defv);
		if (cv != null) {
			this.constraints = Constraint.createConstraint(this.knowledgeBase, cv);
		}
		Vector dv = (Vector) VUtils.assocValue("definition", this.defv);
		if (dv != null) {
			this.definition = Sentence.createSentence(dv);
			this.definitionRelations = this.definition.gatherRelationConstants();
		}
		Vector iv = (Vector) VUtils.assocValue("integration", this.defv);
		if (iv != null) {
			this.integration = Sentence.createSentence(iv);
		}
		Vector<Vector<String>> trv = VUtils.rest(VUtils.assoc("relations", defv));
		if (trv != null) {
			for (Vector<String> rv : trv) {
				String rname = rv.firstElement();
				String mname = rv.elementAt(2);
				RelationConstant rc = ns.getRelationConstant(rname);
				TypeConstant mod = ns.getTypeConstant(mname);
				if (rc != null && mod != null) {
					TypeRelationSentence trs = new TypeRelationSentence(rc, this, mod);
					this.typeRelationSentences = VUtils.add(this.typeRelationSentences, trs);
					this.ontology.addSentence(trs);
				}
			}
		}
		this.setUserComment((String) VUtils.assocValue("comment", this.defv));
	}

	public Constant getParameterConstant(String pname) {
		return this.paramConstantHash.get(pname);
	}

	public Variable getParameterVariable(String pname) {
		return this.parameterVariableHash.get(pname);
	}

	public String getVariableParameter(Variable var) {
		return this.variableParameterHash.get(var.getName());
	}

	public boolean isRelationProperty(String pname) {
		return this.paramConstantHash.get(pname) instanceof RelationConstant;
	}

	public boolean isDataProperty(String pname) {
		return this.paramConstantHash.get(pname) instanceof PropertyConstant;
	}

	public void bindVariables(Vector<Variable> boundVars) {
		Variable.unbind(this.getVariables());
		if (boundVars != null) {
			for (Variable bvar : boundVars) {
				Object bvalue = bvar.getValue();
				Variable tvar = this.parameterVariableHash.get(bvar.getName());
				if (tvar != null && bvalue != null) {
					tvar.bind(bvalue);
				}
			}
		}
	}

	public boolean validate(Vector<Variable> vars) {
		if (this.constraints != null) {
			if (!this.constraints.doTestConstraint(vars)) {
				return false;
			}
		}
		return true;
	}

	// 6/28/2013: This replaces Topaz1.OTClass.instantiate(). Take a relevant
	// type,
	// prove the definition sentence, and generate a new TypeTemplate for each
	// set of bindings.
	public Vector<TypeTemplate> generateTemplates(KnowledgeBase kb) {
		Vector<TypeTemplate> templates = null;
		if (this.definition != null) {
			Vector<Vector> vv = (Vector<Vector>) Query.doQuery(kb, this.definition, null, null, true);
			if (vv != null) {
				for (Vector values : vv) {
					Vector<Variable> bvars = Variable.createBoundVariables(this.getVariables(), values);
					TypeTemplate template = TypeTemplate.create(kb, this, bvars);
					templates = VUtils.add(templates, template);
				}
			}
		}
		return templates;
	}

	public void resolveReferences() {

	}

	public Object getHeritableDefaultProperty(String property) {
		Object value = this.defaultHash.get(property);
		if (value == null) {
			if (this.getParents() != null) {
				for (Term parent : this.getParents()) {
					value = ((TypeConstant) parent).defaultHash.get(property);
					if (value != null) {
						return value;
					}
				}
				for (Term parent : this.getParents()) {
					value = parent.getHeritableProperty(property);
					if (value != null) {
						return value;
					}
				}
			}
		}
		return value;
	}

	public Vector<TypeRelationSentence> getTypeRelationSentences() {
		return this.typeRelationSentences;
	}

	public void addTypeRelationSentence(TypeRelationSentence trs) {
		this.typeRelationSentences = VUtils.addIfNot(this.typeRelationSentences, trs);
	}

	// public Vector<TypeConstant> getParents() {
	// return parents;
	// }
	//
	// public void addParent(TypeConstant parent) {
	// this.parents = VUtils.addIfNot(this.parents, parent);
	// if (parent != null) {
	// parent.children = VUtils.addIfNot(parent.children, this);
	// }
	// }

	public Vector<TypeConstant> getConnectedTypes() {
		return connectedTypes;
	}

	public Sentence getDefinition() {
		return definition;
	}

	public Sentence getIntegration() {
		return integration;
	}

	// public Vector<TypeConstant> getChildren() {
	// return this.children;
	// }

	public boolean subsumedBy(TypeConstant tc) {
		return tc.subsumes(this);
	}

	public boolean subsumes(TypeConstant tc) {
		if (tc != null) {
			if (this.equals(tc)) {
				return true;
			}
			if (tc.getParents() != null) {
				for (Term pterm : tc.getParents()) {
					if (this.subsumes((TypeConstant) pterm)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean equals(Object o) {
		try {
			if (this == o) {
				return true;
			}
			if (o instanceof TypeConstant) {
				TypeConstant other = (TypeConstant) o;
				return (this.getName().equals(other.getName()) || this.getFormalName().equals(other.getFormalName()));
			}
			if (o instanceof String) {
				return o.equals(this.getName());
			}
			if (o instanceof ObjectConstant) {
				ObjectConstant oc = (ObjectConstant) o;
				return this.getName().equals(oc.getObject().toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static TypeConstant findByName(String name) {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		TypeConstant tc = kb.getNameSpace().getTypeConstant(name);
		return tc;
	}

	public static Vector<TypeConstant> findByNames(Vector<String> names) {
		Vector<TypeConstant> types = null;
		if (names != null) {
			for (String name : names) {
				TypeConstant type = findByName(name);
				types = VUtils.add(types, type);
			}
		}
		return types;
	}

	public String getRightmostFullNamePath() {
		if (this.getParents() != null) {
			return this.getName() + "->" + ((TypeConstant) this.getParents().firstElement()).getRightmostFullNamePath();
		}
		return "*";
	}

	public Vector<PropertyConstant> getPropertyConstants() {
		return propertyConstants;
	}

	// //////////////////////////////////////////////////////////////////////////
	// FROM OLD TYPECONSTANT:
	public boolean isAbstract() {
		return this.getChildren() != null;
	}

	public void addUnifier(TypeConstant unifier) {
		try {
			this.unifiableTypes = VUtils.addIfNot(this.unifiableTypes, unifier);
			if (unifier.getParents() != null) {
				for (Term parent : unifier.getParents()) {
					this.unifiableTypes = VUtils.addIfNot(this.unifiableTypes, parent);
				}
				for (Term parent : unifier.getParents()) {
					this.addUnifier((TypeConstant) parent);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Vector expandUnifiableTypes(Vector types) {
		return VUtils.removeDuplicates(VUtils.flatten(VUtils.gatherFields(types, "unifiableTypes")));
	}

	public static void gatherUnifiables(Vector<TypeConstant> typeConstants) {
		if (typeConstants != null) {
			for (TypeConstant tc : typeConstants) {
				tc.addUnifier(tc);
			}
		}
	}

	// Removed 5/6/2017
	// public static void inheritRelations(Vector<RelationSentence> sentences) {
	// if (sentences != null) {
	// for (RelationSentence rs : sentences) {
	// TypeRelationSentence trs = (TypeRelationSentence) rs;
	// for (int i = 0; i < trs.getArity(); i++) {
	// TypeConstant tc = (TypeConstant) trs.getTerm(i);
	// tc.relations = VUtils.addIfNot(tc.relations, trs);
	// for (int j = 0; j < trs.getArity(); j++) {
	// TypeConstant otc = (TypeConstant) trs.getTerm(j);
	// if (!otc.equals(tc)) {
	// tc.connectedTypes = VUtils.addIfNot(tc.connectedTypes, otc);
	// }
	// }
	// }
	// }
	// }
	// }

	// public Vector<TypeRelationSentence> getRelations() {
	// return this.relations;
	// }

	public static boolean sameType(TypeConstant tc1, TypeConstant tc2) {
		return (tc1 != null && tc2 != null && tc1.equals(tc2));
	}

	public static Term mostSpecific(Term t1, Term t2) {
		if (t1.getType() != null && t1.getType().subsumedBy(t2.getType())) {
			return t1;
		} else if (t2.getType() != null && t2.getType().subsumedBy(t1.getType())) {
			return t2;
		}
		return null;
	}

	public static Vector removeSubsumed(Vector types) {
		Vector mostgeneral = null;
		if (types != null) {
			mostgeneral = new Vector(types);
			for (int i = 0; i < types.size(); i++) {
				TypeConstant tc1 = (TypeConstant) types.elementAt(i);
				for (int j = 0; j < types.size(); j++) {
					if (i != j) {
						TypeConstant tc2 = (TypeConstant) types.elementAt(j);
						if (tc1.subsumedBy(tc2)) {
							mostgeneral.remove(tc1);
						}
					}
				}
			}
		}
		return mostgeneral;
	}

	public boolean isLocation() {
		if ("location".equals(this.getName())) {
			return true;
		}
		if (this.getType().getParents() != null) {
			for (Term ptype : this.getType().getParents()) {
				if (((TypeConstant) ptype).isLocation()) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isCondition() {
		if ("condition".equals(this.getName())) {
			return true;
		}
		if (this.getType().getParents() != null) {
			for (Term ptype : this.getType().getParents()) {
				if (((TypeConstant) ptype).isCondition()) {
					return true;
				}
			}
		}
		return false;
	}

	public Vector<String> getParameters() {
		return parameters;
	}

	public Vector<RelationConstant> getDefinitionRelations() {
		return definitionRelations;
	}

	public UMLSTypeInfo getTypeInfo() {
		return typeInfo;
	}

	public void setTypeInfo(UMLSTypeInfo typeInfo) {
		this.typeInfo = typeInfo;
	}

	// 10/2/2013
	public static TypeConstant getType(Object o) {
		if (o instanceof Term) {
			Term t = (Term) o;
			return t.getType();
		}
		if (o instanceof String) {
			String tstr = (String) o;
			KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
			if (kb != null) {
				TypeConstant t = kb.getNameSpace().getTypeConstant(tstr);
				if (t != null) {
					return t;
				}
			}
		}
		return null;
	}

	public Vector<String> getMoonstoneLabels() {
		return MoonstoneLabels;
	}

	public void addMoonstoneLabel(String label) {
		this.MoonstoneLabels = VUtils.add(this.MoonstoneLabels, label);
	}

	public void toLispStringPlusChildrenAndConstants(StringBuffer sb) {
		Hashtable<Object, Object> stored = new Hashtable<Object, Object>();
		toLispStringPlusChildrenAndConstants(sb, stored);
	}

	public void toLispStringPlusChildrenAndConstants(StringBuffer sb, Hashtable<Object, Object> stored) {
		// 9/20/2018: If a type has multiple parents it will be multiply stored
		if (stored.get(this) != null) {
			return;
		}
		stored.put(this, this);
		String nlstr = this.toSexpString();
		sb.append(nlstr + "\n");
		if (this.getChildren() != null) {
			for (Term cterm : this.getChildren()) {
				TypeConstant child = (TypeConstant) cterm;
				child.toLispStringPlusChildrenAndConstants(sb, stored);
			}
		}
		if (!this.isRoot() && this.getTypedStringConstants() != null) {
			for (StringConstant sc : this.getTypedStringConstants()) {
				if (!sc.isComplex()) {
					sb.append(sc.toSexpString() + "\n");

					// 9/12/2018
					if (sc.getSynonyms() != null) {
						sb.append("(defsynonyms ");
						for (String syn : sc.getSynonyms()) {
							sb.append("\"" + syn + "\" ");
						}
						sb.append("\"" + sc.getFormalName() + "\")\n");
					}
				}
			}
		}
		if (this.getTypeRelationSentences() != null) {
			for (TypeRelationSentence trs : this.getTypeRelationSentences()) {
				sb.append(trs.toLisp() + "\n");
			}
		}

	}

	public void toLispStringPlusChildrenAndConstants_BEFORE_3_5_2018(StringBuffer sb) {
		String nlstr = this.toSexpString();
		sb.append(nlstr + "\n");
		if (!this.isRoot() && this.getTypedStringConstants() != null) {
			for (StringConstant sc : this.getTypedStringConstants()) {
				sb.append(sc.toSexpString() + "\n");
			}
		}
		if (this.getTypeRelationSentences() != null) {
			for (TypeRelationSentence trs : this.getTypeRelationSentences()) {
				sb.append(trs.toLisp() + "\n");
			}
		}
		if (this.getChildren() != null) {
			for (Term cterm : this.getChildren()) {
				TypeConstant child = (TypeConstant) cterm;
				child.toLispStringPlusChildrenAndConstants(sb);
			}
		}
	}

	public Sexp toSexp() {
		Sexp sexp = null;
		try {
			TLisp l = TLisp.getTLisp();
			String lstr = "'(deftype \"" + this.getName() + "\" ";
			if (this.getParents() != null) {
				lstr += "(isa ";
				for (Term parent : this.getParents()) {
					lstr += "\"" + parent.getName() + "\" ";
				}
				lstr += ") ";
			} else {
				lstr += "(isroot)";
			}
			if (this.MoonstoneLabels != null) {
				lstr += "(moonstone ";
				for (String label : this.MoonstoneLabels) {
					lstr += "\"" + label + "\" ";
				}
				lstr += ") ";
			}
			lstr += ")";
			sexp = (Sexp) l.evalString(lstr);
			this.setSexp(sexp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sexp;
	}

	public Sexp getSexp() {
		return sexp;
	}

	public Constraint getConstraints() {
		return constraints;
	}

	public Vector<TypeConstant> getUnifiableTypes() {
		return unifiableTypes;
	}

	public static char[] TypeConstantFormalNameChars = new char[] {'_', '-'};
	
	// e.g. "<DISEASE_OR_SYMPTOM>
	public static boolean isTypeConstantFormalName(String str) {
		if (str != null && str.length() > 4 && str.charAt(0) == '<' && str.charAt(str.length() - 1) == '>'
				&& Character.isUpperCase(str.charAt(1))) {
			for (int i = 2; i < str.length() - 1; i++) {
				char c = str.charAt(i);
				if (!(Character.isUpperCase(c) || VUtils.charInArray(c, TypeConstantFormalNameChars))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public String getFormalName() {
		if (this.formalName == null && this.name != null) {
			this.formalName = generateFormalName(this.name, "<", ">");
		}
		return this.formalName;
	}
	
	// Before 9/27/2018
//	public String getFormalName() {
//		if (this.formalName == null && this.name != null) {
//			String core = generateFormalNameCore(this.name);
//			if (!core.startsWith("<")) {
//				core = "<" + core + ">";
//			}
//			this.formalName = core;
//		}
//		return this.formalName;
//	}

	public boolean isRoot() {
		return this.getParents() == null && this.isRoot;
	}

	public boolean isUndefined() {
		return undefinedBaseTypeName.equals(this.getName());
	}

	public static boolean isNullOrUndefined(TypeConstant tc) {
		return (tc == null || tc.isUndefined());
	}

	public void setRoot() {
		if (this.getParents() == null) {
			this.isRoot = true;
		}
	}

	// BEWARE MULTIPLE PARENTS!
	// 4/16/2015: I assume <root> isn't a valid type for subsumption check. It's
	// a
	// place to park things until I decide what type to assign.
	public boolean isSubsumedBy(TypeConstant other) {
		if (other != null && !this.isRoot() && !other.isRoot() && !this.equals(other) && this.unifiableTypes != null) {
			for (TypeConstant unifier : this.unifiableTypes) {
				if (unifier.equals(other)) {
					return true;
				}
			}
		}
		return false;
	}

	// 8/12/2015: Determines whether two types are subsumed by a third type
	public static boolean areSubsumedBy(TypeConstant t1, TypeConstant t2, TypeConstant unifier) {
		return t1.isSubsumedBy(unifier) && t2.isSubsumedBy(unifier);
	}

	// 7/17/2017
	public static TypeConstant getLowestCommonAncestor(TypeConstant t1, TypeConstant t2) {
		if (t1 != null && t2 != null && t1.getUnifiableTypes() != null && t2.getUnifiableTypes() != null) {
			for (TypeConstant p1 : t1.getUnifiableTypes()) {
				for (TypeConstant p2 : t2.getUnifiableTypes()) {
					if (p1.equals(p2) && !p1.isRoot()) {
						return p1;
					}
				}
			}
		}
		return null;
	}

	// 5/18/2018 HACK!!
	public static boolean isUnifiableBelowBase(TypeConstant t1, TypeConstant t2) {
		if (t1 == t2) {
			return true;
		}
		TypeConstant lowest = getLowestCommonAncestor(t1, t2);
		if (lowest != null && !lowest.isBasetype() && !"<SEMANTIC-THING>".equals(lowest.getFormalName())
				&& !"<HEALTH-THING>".equals(lowest.getFormalName())) {
			return true;
		}
		return false;
	}

	public Vector<StringConstant> getTypedStringConstants() {
		return typedStringConstants;
	}

	public void addTypedStringConstant(StringConstant sc) {
		if (this.isUndefined()) {
			int x = 1;
		}
		sc.setType(this);
		this.typedStringConstants = VUtils.addIfNot(this.typedStringConstants, sc);
	}

	public void removeTypedStringConstant(StringConstant sc) {
		// 9/23/2018-- Just remove SC from TC. To remove SC, call SC.remove().
//		this.getKnowledgeBase().getNameSpace().getCurrentSymbolTable().removeStringConstant(sc);
		if (this.typedStringConstants != null) {
			this.typedStringConstants.remove(sc);
		}
		sc.setType(null);
	}

	// 5/4/2017
	public String toTSL() {
		String def = "(deftype " + this.getName().toLowerCase();
		if (this.getParents() == null) {
			def += " (isroot))";
		} else {
			def += " (isa ";
			for (Term pterm : this.getParents()) {
				TypeConstant parent = (TypeConstant) pterm;
				def += parent.getName() + " ";
			}
			def += "))";
		}
		return def;
	}

	//////////////////////////////////////////////////////////////////
	// 5/6/2017: NOT YET TESTED
	// Experimental- Inherit unifiable relations and properties into
	// type constant. Rationale: in large ontology, I don't want to expand
	// all relations for all tupes of subtypes. Instead, gather Relation and
	// Property sentences into a list, then create instances of these sentences
	// at parse time, or whenever needed. (This will supercede
	// Ontology.setupTypeConnectionsWithExpandedRelations())

	// Later, I can add ontology relations to annotations by finding all tuples
	// of
	// pattern elements that unify with unifiable sentences, and I can add
	// default properties from unifiablePropertySentences.

	public void gatherUnifiableSentences() {
		Hashtable<RelationConstant, Sentence> shash = new Hashtable();
		if (this.getParents() != null) {
			for (Term pterm : this.getParents()) {
				TypeConstant ptc = (TypeConstant) pterm;
				ptc.gatherUnifiableSentences(shash);
			}
		}
		for (RelationConstant rc : shash.keySet()) {
			Sentence s = shash.get(rc);
			if (s instanceof TypeRelationSentence) {
				this.unifiableTypeRelationSentences = VUtils.add(this.unifiableTypeRelationSentences,
						(TypeRelationSentence) s);
			} else if (s instanceof PropertySentence) {
				this.unifiablePropertySentences = VUtils.add(this.unifiablePropertySentences, (PropertySentence) s);
			}
		}
	}

	public void gatherUnifiableSentences(Hashtable<RelationConstant, Sentence> shash) {
		if (this.getTypeRelationSentences() != null) {
			for (TypeRelationSentence trs : this.getTypeRelationSentences()) {
				if (shash.get(trs.getRelation()) == null) {
					shash.put(trs.getRelation(), trs);
				}
			}
		}
		if (this.getPropertySentences() != null) {
			for (PropertySentence ps : this.getPropertySentences()) {
				if (shash.get(ps.getRelation()) == null) {
					shash.put(ps.getRelation(), ps);
				}
			}
		}
		if (this.getParents() != null) {
			for (Term pterm : this.getParents()) {
				TypeConstant ptc = (TypeConstant) pterm;
				ptc.gatherUnifiableSentences(shash);
			}
		}
	}

	public Vector<TypeRelationSentence> getUnifiableTypeRelationSentences() {
		return unifiableTypeRelationSentences;
	}

	public Vector<PropertySentence> getUnifiablePropertySentences() {
		return unifiablePropertySentences;
	}

	// 7/12/2017
	public String getPathname() {
		if (this.pathName == null) {
			String pname = "";
			Vector<TypeConstant> path = this.getPathToRoot();
			for (int i = 0; i < path.size(); i++) {
				TypeConstant type = path.elementAt(i);
				if (type.getName() != null) {
					pname += type.getName();
					if (i < path.size() - 1) {
						pname += ":";
					}
				}
			}
			this.pathName = pname;
		}
		return this.pathName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	private Vector<TypeConstant> getPathToRoot() {
		if (this.getParents() == null) {
			return VUtils.listify(this);
		}
		TypeConstant parent = (TypeConstant) this.getParents().firstElement();
		Vector<TypeConstant> path = parent.getPathToRoot();
		return VUtils.add(path, this);
	}

	// 9/13/2017
	public void populateBasePathHash() {
		if (this.basePathHash.isEmpty()) {
			Vector<Vector<TypeConstant>> paths = this.getBasePaths();
			if (paths != null) {
				for (Vector<TypeConstant> path : paths) {
					TypeConstant basetype = path.lastElement();
					this.basePathHash.put(basetype, path);
					this.baseTypes = VUtils.add(this.baseTypes, basetype);
				}
			}
		}
	}

	public Vector<TypeConstant> getPathToBaseType(TypeConstant base) {
		if (base != null) {
			return this.basePathHash.get(base);
		}
		return null;
	}

	public boolean containsBaseType(Object btoken) {
		if (btoken instanceof TypeConstant) {
			return this.baseTypes != null && this.baseTypes.contains(btoken);
		}
		if (btoken instanceof String) {
			return this.getBaseType((String) btoken) != null;
		}
		return false;
	}

	public Vector<TypeConstant> getBaseTypes() {
		return this.baseTypes;
	}

	public TypeConstant getFirstBaseType() {
		if (this.isBasetype()) {
			return this;
		}
		if (this.baseTypes != null) {
			return this.baseTypes.firstElement();
		}
		return null;
	}

	public boolean isBasetype() {
		return this.getParents() != null && ((TypeConstant) this.getParents().firstElement()).isRoot();
	}

	public TypeConstant getBaseType(Object token) {
		if (token instanceof String) {
			return getBaseType((String) token);
		}
		if (token instanceof TypeConstant) {
			return getBaseType((TypeConstant) token);
		}
		return null;
	}

	public TypeConstant getBaseType(String bname) {
		if (this.getBaseTypes() != null) {
			for (TypeConstant btype : this.getBaseTypes()) {
				if (bname.equals(btype.getName()) || bname.equals(btype.getFormalName())) {
					return btype;
				}
			}
		}
		return null;
	}

	public TypeConstant getBaseType(TypeConstant btype) {
		return btype.getFirstBaseType();
	}

	public static TypeConstant findBaseType(String bname) {
		TypeConstant btype = TypeConstant.findByName(bname);
		if (btype != null && btype.isBasetype()) {
			return btype;
		}
		return null;
	}

	public int getNumberOfBaseTypes() {
		if (this.baseTypes != null) {
			return this.baseTypes.size();
		}
		return 0;
	}

	public boolean hasMultipleBasePaths() {
		return this.basePathHash.size() > 1;
	}

	private Vector<Vector<TypeConstant>> getBasePaths() {
		if (this.isRoot()) {
			return null;
		}
		Vector<Vector<TypeConstant>> paths = VUtils.listify(VUtils.listify(this));
		if (this.getParents() != null) {
			Vector<Vector<TypeConstant>> newpaths = null;
			for (Term t : this.getParents()) {
				TypeConstant parent = (TypeConstant) t;
				Vector<Vector<TypeConstant>> ppaths = parent.getBasePaths();
				if (ppaths != null) {
					for (Vector<TypeConstant> ppath : ppaths) {
						Vector<TypeConstant> newpath = VUtils.appendNew(VUtils.listify(this), ppath);
						newpaths = VUtils.add(newpaths, newpath);
					}
					paths = newpaths;
				}
			}
		}
		return paths;
	}

	public Vector<StringConstant> gatherCoveredStringConstants() {
		Vector<StringConstant> constants = null;
		if (this.getTypedStringConstants() != null) {
			constants = new Vector(this.getTypedStringConstants());
		}
		if (this.getChildren() != null) {
			for (Term t : this.getChildren()) {
				TypeConstant child = (TypeConstant) t;
				if (child.getOntology() == KnowledgeEngine.getCurrentKnowledgeEngine().getCurrentOntology()) {
					constants = VUtils.appendNew(constants, child.gatherCoveredStringConstants());
				}
			}
		}
		return constants;
	}

	// 7/24/2018
	public boolean isGrammarType() {
		return this.getName().toLowerCase().contains("grammar");
	}

}
