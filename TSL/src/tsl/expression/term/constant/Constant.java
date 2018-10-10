package tsl.expression.term.constant;

import java.lang.reflect.Method;
import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.term.Term;
import tsl.expression.term.function.FunctionConstant;
import tsl.expression.term.property.PropertyConstant;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.type.SyntacticTypeConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.knowledgebase.SymbolTable;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLisp;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public abstract class Constant extends Term {

	private String userComment = null;
	protected String formalName = null;

	protected static char[] ConstantFormalNameChars = new char[] { '_', '-', ':' };

	public Constant() {
		super();
	}

	public Constant(String name) {
		super();
		this.setName(Constant.generateSimpleName(name));
		this.getKnowledgeBase().getNameSpace().addConstant(this);
	}

	public Constant(String name, TypeConstant type) {
		super();
		this.setName(Constant.generateSimpleName(name));
		this.getFormalName();
		this.setType(type);
		this.getKnowledgeBase().getNameSpace().addConstant(this);
	}

	public static Constant createConstant(Vector v) {
		Constant c = null;
		if ((c = StringConstant.createStringConstant(v)) != null || (c = TypeConstant.createTypeConstant(v)) != null
				|| (c = RelationConstant.createRelationConstant(v)) != null
				|| (c = FunctionConstant.createFunctionConstant(v)) != null
				|| (c = PropertyConstant.createPropertyConstant(v)) != null) {
			return c;
		}
		return null;
	}

	public String getUserComment() {
		return userComment;
	}

	public void setUserComment(String userComment) {
		this.userComment = userComment;
	}

	public void resolve() {

	}

	public Expression copy() {
		return this;
	}

	public static boolean isQualifiedMethodPathname(String name) {
		return name != null && name.contains(".") && name.contains(":");
	}

	// 11/23/2013: For use in JavaRelationConstant and JavaFunctionConstant

	// E.g. "workbench.tsl.AnnotationTerm:evAnnotationWithAttributeValue"
	// => "workbench.tsl.AnnotationTerm"
	public static String getMethodSourceClassName(String cname) {
		int index = cname.lastIndexOf(':');
		if (index > 0) {
			return cname.substring(0, index);
		}
		return cname;
	}

	// E.g. "workbench.tsl.AnnotationTerm:evAnnotationWithAttributeValue"
	// => "evAnnotationWithAttributeValue"
	public static String getMethodName(String cname) {
		int index = cname.lastIndexOf(':');
		if (index > 0) {
			return cname.substring(index + 1);
		}
		return cname;
	}

	public static Method getMethod(String name) {
		if (isQualifiedMethodPathname(name)) {
			return getMethodUsingQualifiedPathname(name);
		}
		KnowledgeEngine ke = KnowledgeEngine.getCurrentKnowledgeEngine();
		String[] paths = ke.getJavaPathnames();
		if (paths != null) {
			for (String path : paths) {
				String pathname = path + ":" + name;
				Method m = getMethodUsingQualifiedPathname(pathname);
				if (m != null) {
					return m;
				}
			}
		}
		return null;
	}

	// e.g. getMethod("a.b.c:methodname");
	// Finds a method without having to pre-specify parameter types.
	public static Method getMethodUsingQualifiedPathname(String fullname) {
		try {
			String cname = getMethodSourceClassName(fullname);
			String mname = getMethodName(fullname);
			if (cname != null && mname != null) {
				Class c = Class.forName(cname);
				Method[] methods = c.getMethods();
				if (methods != null) {
					for (Method m : methods) {
						if (mname.equals(m.getName())) {
							return m;
						}
					}
				}
			}
		} catch (Exception e) {
			// System.out.println("Java function/relation class not found: " +
			// fullname);
		}
		return null;
	}

	public String toString() {
		return this.getFormalName();
	}

	public String getFormalName() {
		if (this.formalName == null && this.name != null) {
			this.formalName = generateFormalName(this.name, "_", "_");
		}
		return this.formalName;
	}

	public String getSimpleName() {
		if (this.name == null && this.formalName != null) {
			this.name = generateSimpleName(this.formalName);
		}
		return this.name;
	}

	//////////////////
	// 9/27/2018
	private static char[] AllConstantSimpleNameChars = new char[] { '_', '-', ':' };
	private static char[] AllConstantFormalNameChars = new char[] { '<', '>', ':', '#' };

	public static String generateSimpleName(String str) {
		str = StrUtils.removeNonAlphaDigitArrayCharacters(str, AllConstantSimpleNameChars);
		str = (str.length() > 1 ? str.toLowerCase() : null);
		return str;
	}

	public static String generateFormalName(String str, String ldelim, String rdelim) {
		str = generateSimpleName(str);
		str = str.toUpperCase();
		if (!str.startsWith(ldelim)) {
			str = ldelim + str;
		}
		if (!str.endsWith(rdelim)) {
			str += rdelim;
		}
		return str;
	}

	//////////////////

	public static boolean isConstantFormalName(Object token) {
		if (token instanceof String && ((String) token).length() > 2) {
			String name = (String) token;
			char start = name.charAt(0);
			char end = name.charAt(name.length() - 1);
			if (!(VUtils.charInArray(start, AllConstantFormalNameChars)
					&& VUtils.charInArray(end, AllConstantFormalNameChars))) {
				return false;
			}
			for (int i = 1; i < name.length() - 1; i++) {
				char c = name.charAt(i);
				if (!(Character.isUpperCase(c) || VUtils.charInArray(c, AllConstantSimpleNameChars))) {
					return false;
				}
			}
			return true;
		}

		return false;
	}

	public static Object extractConstant(KnowledgeBase kb, Object token) {
		Object o = token;
		if (Constant.isConstantFormalName(token)) {
			String tname = (String) token;
			int x = 1;
			Constant c = kb.getConstant(tname);
			if (c == null) {
				if (TypeConstant.isTypeConstantFormalName(tname)) {
					c = TypeConstant.createTypeConstant(tname);
				} else if (StringConstant.isStringConstantFormalName(tname)) {
					TypeConstant attachtc = kb.getOntology().getUndefinedType();
					if (attachtc == null) {
						attachtc = kb.getOntology().getRootType();
					}
					c = new StringConstant(tname, attachtc, false);
					if (c instanceof StringConstant && !((StringConstant) c).isComplex()) {
						System.out.println("Missing constant for \"" + tname + "\"");
					}
				} else if (SyntacticTypeConstant.isSyntacticTypeConstantFormalName(tname)) {
					c = SyntacticTypeConstant.createTypeConstant(tname);
				}
			}
			if (c != null) {
				o = c;
			} else {
				System.out.println("Undefined constant: " + token);
			}
		}
		return o;
	}

	public String getTuffyString() {
		String cname = StrUtils.removeNonAlphaDigitUnderlineCharacters(this.getName());
		return cname;
	}

	public static String getTuffyString(String str) {
		String cname = StrUtils.removeNonAlphaDigitUnderlineCharacters(str);
		return cname;
	}

	public String toSexpString() {
		Sexp sexp = this.toSexp();
		String nlstr = null;
		if (sexp instanceof Sexp) {
			nlstr = sexp.toNewlinedString();
		}
		return nlstr;
	}

	public Sexp toSexp() {
		return null;
	}

	// 9/21/2018
	public void remove() {
		this.getKnowledgeBase().getNameSpace().removeConstant(this);
	}

}
