package moonstone.learning.ebl;

import moonstone.rulebuilder.MoonstoneRuleInterface;

public class GrammarEBLCorpusAnalysis implements Runnable {
	
	public void run() {
		MoonstoneRuleInterface msri = MoonstoneRuleInterface.RuleEditor;
		GrammarEBL gebl = MoonstoneRuleInterface.RuleEditor.getGrammarEBL();
		gebl.getDocumentFilesFromTrainingDirectory();
		gebl.analyzeCorpusFiles();
		int x = 1;
	}
}
