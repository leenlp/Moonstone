package moonstone.information;

import java.util.Vector;

import moonstone.grammar.Grammar;
import moonstone.grammar.GrammarModule;
import tsl.expression.term.Term;
import tsl.jlisp.JLUtils;
import tsl.jlisp.JLisp;
import tsl.jlisp.Sexp;
import tsl.knowledge.engine.StartupParameters;

public abstract class Information extends Term {

	// public GrammarModule grammarModule = null;
	public Grammar grammar = null;
	public boolean expandImmediately = false;

	public Information() {
		super();
	}

	public Information(Grammar grammar, Vector pattern) {
		super(pattern);
		this.grammar = grammar;
	}

	public static void initialize() {
		try {
			GrammarModule control = GrammarModule.CurrentGrammarModule;
			StartupParameters sp = control.getKnowledgeEngine()
					.getStartupParameters();
			String defaultFile = sp.getResourceFileName("defaults");
			Sexp sexp = (Sexp) JLisp.jLisp.loadFile(defaultFile);
			Vector v = JLUtils.convertSexpToJVector(sexp);
			setDefaults(v);
			String relevantFeatureFile = sp
					.getResourceFileName("relevantFeatures");
			sexp = (Sexp) JLisp.jLisp.loadFile(relevantFeatureFile);
			if (sexp != null) {
				v = JLUtils.convertSexpToJVector(sexp);
				setRelevantFeatures(v);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Grammar getGrammar() {
		return this.grammar;
	}

	public void setGrammar(Grammar grammar) {
		this.grammar = grammar;
	}

}
