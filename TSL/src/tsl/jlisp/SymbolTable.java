package tsl.jlisp;

import java.util.Hashtable;
import java.util.Vector;

public class SymbolTable {
	JLisp jlisp = null;
	Hashtable symhash = new Hashtable();

	public static SymbolTable globalSymbolTable = new SymbolTable();

	SymbolTable() {
		this.jlisp = JLisp.jLisp;
		this.jlisp.symbolTables.add(this);
		this.jlisp.currentSymbolTable = this;
	}

	public void addSymbol(Symbol sym) {
		this.symhash.put(sym.name, sym);
	}

	public void removeSymbol(Symbol sym) {
		this.symhash.remove(sym.name);
	}

	public Symbol getSymbol(String name, JLispObject value) {
		if (!JLisp.getJLisp().isPreserveSymbolCase()) {
			name = name.toUpperCase();
		}
		Symbol sym = (Symbol) this.symhash.get(name);
		if (sym == null) {
			sym = new Symbol(this, name, value);
		}
		return sym;
	}

	public static Symbol getGlobalSymbol(String name, JLispObject value) {
		return globalSymbolTable.getSymbol(name, value);
	}

	public static void pushSymbolTable() {
		new SymbolTable();
	}
	
	public void pushParameters(Vector<Symbol> params,
			Sexp args, int pindex) {
		if (!JLUtils.isNil(args)) {
			JLispObject arg = (JLispObject) args.getCar();
			Symbol param = (Symbol) params.elementAt(pindex);
			param.copy(this, arg);
			if (pindex < params.size()) {
				pushParameters(params, (Sexp) args.getCdr(), pindex++);
			}
		}
	}

	public static void popParameters() {
		if (JLisp.jLisp.symbolTables.size() > 1) {
			JLisp.jLisp.symbolTables
					.remove(JLisp.jLisp.symbolTables.size() - 1);
			JLisp.jLisp.currentSymbolTable = (SymbolTable) JLisp.jLisp.symbolTables
					.lastElement();
		}
	}

	public static Symbol findLocalSymbol(Symbol sym) {
		Symbol s = null;
		for (int i = JLisp.jLisp.symbolTables.size() - 1; i >= 0; i--) {
			SymbolTable st = (SymbolTable) JLisp.jLisp.symbolTables
					.elementAt(i);
			if ((s = (Symbol) st.symhash.get(sym.getName())) != null) {
				break;
			}
		}
		return s;
	}

}
