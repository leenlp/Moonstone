package tsl.tsllisp;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class SymbolTable {
	private TLisp tlisp = null;
	private SymbolTable parent = null;
	private Hashtable symhash = new Hashtable();

	public SymbolTable() {
		this(TLisp.tLisp);
	}

	SymbolTable(TLisp tlisp) {
		this.tlisp = tlisp;
	}

	public void addSymbol(Symbol sym) {
		this.symhash.put(sym.getName(), sym);
	}

	public void removeSymbol(Symbol sym) {
		this.symhash.remove(sym.getName());
	}

	public Symbol findSymbol(String name) {
		if (!TLisp.tLisp.isPreserveSymbolCase()) {
//			name = name.toUpperCase();
			name = name.toLowerCase();
		}
		return (Symbol) this.symhash.get(name);
	}

	public Symbol getSymbol(String name, TLObject value) {
		if (!TLisp.tLisp.isPreserveSymbolCase()) {
//			name = name.toUpperCase();
			name = name.toLowerCase();
		}
		Symbol sym = (Symbol) this.symhash.get(name);
		if (sym == null && !TLisp.tLisp.isPreserveSymbolCase()) {
			sym = (Symbol) this.symhash.get(name.toLowerCase());
		}
		if (sym == null) {
			sym = new Symbol(this, name, value);
		}
		return sym;
	}

	public static Symbol getGlobalSymbol(String name, TLObject value) {
		return TLisp.tLisp.globalSymbolTable.getSymbol(name, value);
	}

	public static Symbol getFunctionSymbol(String name) {
		return TLisp.tLisp.functionSymbolTable.getSymbol(name, null);
	}

	public void pushParameters(Vector<Symbol> params, TLObject args)
			throws Exception {
		int i = 0;
		while (args.isCons()) {
			Sexp s = (Sexp) args;
			TLObject arg = (TLObject) s.getCar();
			Symbol param = (Symbol) params.elementAt(i);
			pushParameter(param, arg);
			args = s.getCdr();
			i++;
		}
	}

	public void pushParameter(Symbol param, TLObject arg) throws Exception {
		param.copy(this, arg);
	}

	public Symbol findLocalSymbol(Symbol sym) {
		Symbol s = null;
		if ((s = (Symbol) this.symhash.get(sym.getName())) != null) {
			return s;
		}
		if (this.parent != null) {
			return this.parent.findLocalSymbol(sym);
		}
		return null;
	}

	public SymbolTable getParent() {
		return parent;
	}

	public void setParent(SymbolTable parent) {
		this.parent = parent;
	}

	public void clear() {
		this.symhash.clear();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (Enumeration<String> e = this.symhash.keys(); e.hasMoreElements();) {
			sb.append(e.nextElement());
			if (e.hasMoreElements()) {
				sb.append(",");
			}
		}
		sb.append("]");
		return sb.toString();
	}

}
