package moonstone.grammar;

public class RuleExpansionTimer implements Runnable {
	
	private Grammar grammar = null;
	public RuleExpansionTimer(Grammar g) {
		this.grammar = g;
	}
	
	public void run() {
		this.grammar.runRuleExpansions();
	}

}
