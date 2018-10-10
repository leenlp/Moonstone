package tsl.knowledge.knowledgebase;

import java.util.Hashtable;

import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.function.FunctionConstant;
import tsl.expression.term.property.PropertyConstant;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.type.SyntacticTypeConstant;
import tsl.expression.term.type.TypeConstant;

public class NameSpace {
	private Hashtable<String, Object> symbolTableHash = new Hashtable();
	private SymbolTable currentSymbolTable = null;

	public NameSpace() {
		this.currentSymbolTable = new SymbolTable();
	}

	public void addTypeConstant(TypeConstant tc) {
		currentSymbolTable.addTypeConstant(tc);
	}

	public void addConstant(Constant c) {
		if (c instanceof StringConstant) {
			currentSymbolTable.addStringConstant((StringConstant) c);
		} else if (c instanceof PropertyConstant) {
			currentSymbolTable.addPropertyConstant((PropertyConstant) c);
		} else if (c instanceof RelationConstant) {
			currentSymbolTable.addRelationConstant((RelationConstant) c);
		} else if (c instanceof SyntacticTypeConstant) {
			currentSymbolTable.addSyntacticTypeConstant((SyntacticTypeConstant) c);
		} else if (c instanceof TypeConstant) {
			currentSymbolTable.addTypeConstant((TypeConstant) c);
		} else if (c instanceof FunctionConstant) {
			currentSymbolTable.addFunctionConstant((FunctionConstant) c);
		} else if (c instanceof ObjectConstant) {
			currentSymbolTable.addObjectConstant((ObjectConstant) c);
		} 
	}
	
	// 9/18/2018
	public void removeConstant(Constant c) {
		if (c instanceof StringConstant) {
			currentSymbolTable.removeStringConstant((StringConstant) c);
		} else if (c instanceof PropertyConstant) {
			currentSymbolTable.removePropertyConstant((PropertyConstant) c);
		} else if (c instanceof RelationConstant) {
			currentSymbolTable.removeRelationConstant((RelationConstant) c);
		} else if (c instanceof SyntacticTypeConstant) {
			currentSymbolTable.removeSyntacticTypeConstant((SyntacticTypeConstant) c);
		} else if (c instanceof TypeConstant) {
			currentSymbolTable.removeTypeConstant((TypeConstant) c);
		} else if (c instanceof FunctionConstant) {
			currentSymbolTable.removeFunctionConstant((FunctionConstant) c);
		} else if (c instanceof ObjectConstant) {
			currentSymbolTable.removeObjectConstant((ObjectConstant) c);
		} 
	}
	
	
	
	public Constant getConstant(String name) {
		return currentSymbolTable.getConstant(name);
	}

	public TypeConstant getTypeConstant(String name) {
		return currentSymbolTable.getTypeConstant(name);
	}
	
	public TypeConstant getSyntacticTypeConstant(String name) {
		return currentSymbolTable.getSyntacticTypeConstant(name);
	}

	public RelationConstant getRelationConstant(String name) {
		return currentSymbolTable.getRelationConstant(name);
	}

	public FunctionConstant getFunctionConstant(String name) {
		return currentSymbolTable.getFunctionConstant(name);
	}

	public ObjectConstant getObjectConstant(String name) {
		return currentSymbolTable.getObjectConstant(name);
	}

	public PropertyConstant getPropertyConstant(String name) {
		return currentSymbolTable.getPropertyConstant(name);
	}
	
	public StringConstant getStringConstant(String name) {
		return currentSymbolTable.getStringConstant(name);
	}

	public void setCurrentSymbolTable(String path) {
		currentSymbolTable = getSymbolTable(path, true);
	}

	private SymbolTable getSymbolTable(String path, boolean create) {
		Hashtable<String, Object> hash = symbolTableHash;
		SymbolTable st = null;
		String[] names = path.split(".");
		String lastname = null;
		for (int i = 0; i < names.length - 1; i++) {
			Object value = hash.get(names[i]);
			if (i == names.length - 1) {
				lastname = names[i];
				st = (SymbolTable) value;
			} else if (value instanceof Hashtable) {
				hash = (Hashtable) value;
			} else if (value == null) {
				Hashtable<String, Object> h = new Hashtable();
				hash.put(names[i], h);
				hash = h;
			}
		}
		if (st == null) {
			st = new SymbolTable();
			hash.put(lastname, st);
		}
		return st;
	}

	public SymbolTable getCurrentSymbolTable() {
		return currentSymbolTable;
	}

}
