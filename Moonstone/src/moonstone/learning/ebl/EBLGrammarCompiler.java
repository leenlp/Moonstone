package moonstone.learning.ebl;

import moonstone.rulebuilder.MoonstoneRuleInterface;

public class EBLGrammarCompiler implements Runnable {
	
	public void run() {
		MoonstoneRuleInterface msri = MoonstoneRuleInterface.RuleEditor;
		GrammarEBLCompilerSpecialization cs = msri.getGrammarEBLCompilerSpecialization();
		cs.getDocumentFilesFromTrainingDirectory();
		cs.analyzeCorpusFiles();
	}
	
}
