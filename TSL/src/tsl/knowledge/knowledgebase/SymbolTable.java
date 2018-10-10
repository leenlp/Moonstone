package tsl.knowledge.knowledgebase;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.function.FunctionConstant;
import tsl.expression.term.property.PropertyConstant;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.type.SyntacticTypeConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;

public class SymbolTable {

	private Hashtable<String, RelationConstant> relationConstantHash = new Hashtable();
	private Hashtable<String, FunctionConstant> functionConstantHash = new Hashtable();
	private Hashtable<String, TypeConstant> typeConstantHash = new Hashtable();
	private Hashtable<String, SyntacticTypeConstant> syntacticTypeConstantHash = new Hashtable();
	private Hashtable<String, ObjectConstant> objectConstantHash = new Hashtable();
	private Hashtable<String, PropertyConstant> propertyConstantHash = new Hashtable();
	private Hashtable<String, StringConstant> stringConstantHash = new Hashtable();
//	private Hashtable<TypeConstant, Vector<Constant>> typedConstantHash = new Hashtable();
	
	public Vector<StringConstant> getAllStringConstants() {
		Vector<StringConstant> constants = HUtils.getUniqueElements(stringConstantHash);
		return constants;
	}

	// Problem: There are two indexes for each type, so this returns doubles.
	public Vector<TypeConstant> getAllTypeConstants() {
		Vector<TypeConstant> constants = HUtils.getUniqueElements(typeConstantHash);
		return constants;
	}

//	public Vector<Constant> getAllTypedConstants(TypeConstant type) {
//		return typedConstantHash.get(type);
//	}

	public Vector<RelationConstant> getAllRelationConstants() {
		return HUtils.getElements(relationConstantHash);
	}

	public Vector<FunctionConstant> getAllFunctionConstants() {
		return HUtils.getElements(functionConstantHash);
	}

	public Vector<PropertyConstant> getAllPropertyConstants() {
		return HUtils.getElements(propertyConstantHash);
	}

	public Constant getConstant(String name) {
		Constant c = null;
		if (((c = getTypeConstant(name)) != null) || ((c = getTypeConstant(name)) != null)
				|| ((c = getStringConstant(name)) != null) || ((c = getRelationConstant(name)) != null)
				|| ((c = getFunctionConstant(name)) != null) || ((c = getPropertyConstant(name)) != null)
				|| ((c = getPropertyConstant(name)) != null)
				|| ((c = getSyntacticTypeConstant(name)) != null)) {
			return c;
		}
		return null;
	}

	public TypeConstant getTypeConstant(String name) {
		if (name != null) {
			TypeConstant tc = typeConstantHash.get(name);
			return tc;
		}
		return null;
	}
	
	public SyntacticTypeConstant getSyntacticTypeConstant(String name) {
		if (name != null) {
			SyntacticTypeConstant tc = syntacticTypeConstantHash.get(name);
			return tc;
		}
		return null;
	}

	public RelationConstant getRelationConstant(String name) {
		if (name != null) {
			return relationConstantHash.get(name);
		}
		return null;
	}

	public FunctionConstant getFunctionConstant(String name) {
		if (name != null) {
			return functionConstantHash.get(name);
		}
		return null;
	}

	public ObjectConstant getObjectConstant(String name) {
		if (name != null) {
			return objectConstantHash.get(name);
		}
		return null;
	}

	public PropertyConstant getPropertyConstant(String name) {
		if (name != null) {
			return propertyConstantHash.get(name);
		}
		return null;
	}

	public StringConstant getStringConstant(String name) {
		if (name != null) {
			return stringConstantHash.get(name);
		}
		return null;
	}

	public void addTypeConstant(TypeConstant type) {
		try {
			typeConstantHash.put(type.getName(), type);
			typeConstantHash.put(type.getFormalName(), type);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addSyntacticTypeConstant(SyntacticTypeConstant type) {
		syntacticTypeConstantHash.put(type.getName(), type);
		syntacticTypeConstantHash.put(type.getFormalName(), type);
	}

	public void addRelationConstant(RelationConstant rc) {
		relationConstantHash.put(rc.getName(), rc);
		relationConstantHash.put(rc.getFormalName(), rc);
	}

	public void addFunctionConstant(FunctionConstant fc) {
		functionConstantHash.put(fc.getName(), fc);
		functionConstantHash.put(fc.getFormalName(), fc);
	}

	public void addObjectConstant(ObjectConstant oc) {
		objectConstantHash.put(oc.getName(), oc);
		objectConstantHash.put(oc.getFormalName(), oc);
	}

	public void addPropertyConstant(PropertyConstant oc) {
		propertyConstantHash.put(oc.getName(), oc);
		propertyConstantHash.put(oc.getFormalName(), oc);
	}

	public void addStringConstant(StringConstant sc) {
		stringConstantHash.put(sc.getName(), sc);
		stringConstantHash.put(sc.getFormalName(), sc);
		if (sc.getType() != null) {
			sc.getType().addTypedStringConstant(sc);
		}
	}
	
	// 9/18/2018
	public void removeConstant(Hashtable hash, Constant c) {
		hash.remove(c.getName());
		hash.remove(c.getFormalName());
	}
	public void removeStringConstant(StringConstant sc) {
		stringConstantHash.remove(sc.getName());
		stringConstantHash.remove(sc.getFormalName());
	}
	
	public void removeTypeConstant(TypeConstant tc) {
		typeConstantHash.remove(tc.getName());
		typeConstantHash.remove(tc.getFormalName());
	}
	
	public void removePropertyConstant(PropertyConstant pc) {
		propertyConstantHash.remove(pc.getName());
		propertyConstantHash.remove(pc.getFormalName());
	}
	
	public void removeRelationConstant(RelationConstant rc) {
		relationConstantHash.remove(rc.getName());
		relationConstantHash.remove(rc.getFormalName());
	}
	
	public void removeSyntacticTypeConstant(SyntacticTypeConstant stc) {
		syntacticTypeConstantHash.remove(stc.getName());
		syntacticTypeConstantHash.remove(stc.getFormalName());
	}

	public void removeFunctionConstant(FunctionConstant fc) {
		functionConstantHash.remove(fc.getName());
		functionConstantHash.remove(fc.getFormalName());
	}

	public void removeObjectConstant(ObjectConstant oc) {
		objectConstantHash.remove(oc.getName());
		objectConstantHash.remove(oc.getFormalName());
	}


	public void resolveConstants() {
		for (Enumeration<String> e = typeConstantHash.keys(); e.hasMoreElements();) {
			typeConstantHash.get(e.nextElement()).resolve();
		}
		for (Enumeration<String> e = syntacticTypeConstantHash.keys(); e.hasMoreElements();) {
			syntacticTypeConstantHash.get(e.nextElement()).resolve();
		}
		for (Enumeration<String> e = relationConstantHash.keys(); e.hasMoreElements();) {
			relationConstantHash.get(e.nextElement()).resolve();
		}
		for (Enumeration<String> e = functionConstantHash.keys(); e.hasMoreElements();) {
			functionConstantHash.get(e.nextElement()).resolve();
		}
	}

}
