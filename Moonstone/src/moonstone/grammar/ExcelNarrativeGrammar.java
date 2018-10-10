package moonstone.grammar;

import java.io.File;
import java.util.Vector;
import moonstone.rule.Rule;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.utilities.FUtils;
import tsl.utilities.StrUtils;

public class ExcelNarrativeGrammar extends NarrativeGrammar {
	
	public ExcelNarrativeGrammar(GrammarModule gmod, String name, String ruledir) {
		super(gmod, name, ruledir);
	}

	public ExcelNarrativeGrammar(Grammar parent, String name) {
		super(parent, name);
	}

	public void readRules() {
		int x = 1;
		String filename = this.knowledgeEngine.getStartupParameters()
				.getPropertyValue(MoonstoneRuleInterface.ExcelGrammarRuleFileName);
		if (filename == null) {
			filename = MoonstoneRuleInterface.ExcelGrammarRuleFileName;
		}
		String fullname = this.getGrammarModule().getMoonstoneRuleInterface().getResourceDirectoryName()
				+ File.separator + filename;
		String rtext = FUtils.readFile(fullname);
		if (rtext != null) {
			Vector<String> estrs = StrUtils.stringList(rtext, "\n");
			if (estrs != null) {
				String sourcefile = "*";
				for (String estr : estrs) {
					Vector<String> strs = StrUtils.stringList(estr, Rule.ExcelPatternDelimiter);
					if (strs != null) {
						String firststr = strs.firstElement();
						if (strs.size() > 6 && !"RULEID".equals(firststr)) {
							Rule.readFromExcelString(this, estr);
						}
					}
				}
				this.resolveRules();
			}
		}
	}
	
	public void loadRulesFromExcelFile_BEFORE_5_22_2018() {
		int x = 1;
		String filename = this.knowledgeEngine.getStartupParameters()
				.getPropertyValue(MoonstoneRuleInterface.ExcelGrammarRuleFileName);
		if (filename == null) {
			filename = MoonstoneRuleInterface.ExcelGrammarRuleFileName;
		}
		String fullname = this.getGrammarModule().getMoonstoneRuleInterface().getResourceDirectoryName()
				+ File.separator + filename;
		String rtext = FUtils.readFile(fullname);
		if (rtext != null) {
			MoonstoneRuleInterface msri = this.getGrammarModule().getMoonstoneRuleInterface();
			Grammar newSentenceGrammar = new NarrativeGrammar(this, "excel-sentence-grammar");
			newSentenceGrammar.knowledgeEngine = this.getKnowledgeEngine();
			Vector<String> estrs = StrUtils.stringList(rtext, "\n");
			if (estrs != null) {
				String sourcefile = "*";
				for (String estr : estrs) {
					Vector<String> strs = StrUtils.stringList(estr, Rule.ExcelPatternDelimiter);
					if (strs != null) {
						String firststr = strs.firstElement();
						if (strs.size() > 6 && !"RULEID".equals(firststr)) {
							Rule.readFromExcelString(newSentenceGrammar, estr);
						}
					}
				}
			}
			this.getGrammarModule().setNarrativeGrammar(newSentenceGrammar);
			this.getGrammarModule().setCurrentGrammar(newSentenceGrammar);
			msri.setCurrentGrammar(newSentenceGrammar);
		}
	}

	



}
