package moonstone.rulebuilder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import edu.utah.blulab.evaluationworkbenchmanager.EvaluationWorkbenchManager;
import edu.utah.blulab.nlp.AnnotationNLP;
import edu.utah.blulab.nlp.NLPToolInterface;
import moonstone.annotation.Annotation;
import moonstone.annotation.AnnotationIntegrationMaps;
import moonstone.annotation.MissingAnnotation;
import moonstone.annotation.StructureAnnotation;
import moonstone.api.MoonstoneAnnotation;
//import moonstone.db.DataBaseManager;
import moonstone.grammar.Grammar;
import moonstone.grammar.GrammarModule;
import moonstone.grammar.NarrativeGrammar;
import moonstone.io.db.MySQLAPI;
import moonstone.io.db.MySQLAPI_2S;
import moonstone.io.ehost.MoonstoneEHostXML;
import moonstone.io.modifiers.AMIR01Modifiers;
import moonstone.io.modifiers.Modifiers;
import moonstone.io.readmission.Readmission;
import moonstone.io.readmission.ReadmissionCorpusProcessor;
import moonstone.io.readmission.ReadmissionPatientResults;
import moonstone.io.readmission.ReadmissionTuffy;
import moonstone.learning.basilisk.Basilisk;
import moonstone.learning.basilisk.ExtractionPattern;
import moonstone.learning.basilisk.Lexicon;
import moonstone.learning.ebl.EBLGrammarCompiler;
import moonstone.learning.ebl.GrammarEBL;
import moonstone.learning.ebl.GrammarEBLCompilerSpecialization;
import moonstone.learning.feature.FeatureSet;
import moonstone.learning.workbench.EvaluationWorkbenchRuleExtractor;
import moonstone.ontology.framenet.FramenetOntology;
import moonstone.ontology.owl.ExtractOWLOntology;
import moonstone.ontology.tslpanel.TSLOntologyFrame;
import moonstone.probability.LPCFG;
import moonstone.rule.InferenceRule;
import moonstone.rule.Rule;
import moonstone.rule.SimplifiedRuleFormat;
import moonstone.utility.ThreadUtils;
// import moonstone.workfromhome.ontology.owl.ExtractOWLOntology;
import tsl.documentanalysis.document.Document;
import tsl.documentanalysis.document.Header;
import tsl.documentanalysis.document.NarrativeContent;
import tsl.documentanalysis.document.Sentence;
import tsl.documentanalysis.tokenizer.regexpr.RegExprManager;
import tsl.error.TSLException;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.inference.forwardchaining.ForwardChainingInferenceEngine;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.engine.StartupParameters;
import tsl.knowledge.knowledgebase.gui.TSLGUI;
import tsl.knowledge.ontology.Ontology;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLisp;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import workbench.api.gui.WBGUI;

public class MoonstoneRuleInterface extends JPanel implements TreeSelectionListener, ItemListener, ActionListener,
		KeyListener, MouseMotionListener, MouseListener, NLPToolInterface {
	private static final long serialVersionUID = 1L;
	private JFrame frame = null;
	protected KnowledgeEngine knowledgeEngine = null;
	protected WBGUI wbgui = null;
	protected GrammarModule control = null;
	protected TabbedPaneClass tabbedClassPane = null;
	protected String ruleType = null;
	protected String ruleFilePath = null;
	protected String corpusDirectoryName = null;
	protected String trainingDirectoryName = null;
	protected String resultsDirectoryName = null;
	protected String resourceDirectoryName = null;
	protected JButton ruleFileNameButton = null;
	protected JComboBox ruleTypeCB = null;
	protected JComboBox ruleConceptCB = null;
	protected JComboBox ruleIDCB = null;
	protected JTextPane ruleDefinitionTextPane = null;
	protected JTextField ruleTokenTextField = null;
	protected JTextField ruleNameTextField = null;
	public JTextField ruleFileNameTextField = null;
	protected JTextArea documentPanel = null;
	protected String ruleID = null;
	protected Rule rule = null;
	protected AnnotationRuleMutableTreeNode rootNode = null;
	protected AnnotationRuleDefaultTreeModel model = null;
	protected AnnotationRuleJTree annotationRuleJTree = null;
	protected AnnotationRuleMutableTreeNode selectedNode = null;
	protected Annotation lastSelectedAnnotation = null;
	protected Annotation selectedAnnotation = null;
	protected Hashtable<Rule, Vector<String>> ruleTokenHash = new Hashtable();
	protected Hashtable<String, Vector<Rule>> tokenRuleHash = new Hashtable();
	protected TreePath lastTreePath = null;
	public String topGrammarDirectoryPathName = null;
	public String selectedGrammarDirectoryName = null;
	public Vector<String> grammarDirectoryNames = null;
	protected Hashtable<Vector<Vector>, Rule> tentativeRuleHash = new Hashtable();
	protected TSLGUI tslGUI = null;
	protected Basilisk basilisk = null;
	protected MoonstoneQueryPanel moonstoneQueryPanel = null;
	protected Vector<Annotation> displayedAnnotations = null;

	// 11/13/2016
	protected Vector<Annotation> annotationsForGarbageCollection = null;

	protected Vector<tsl.expression.term.relation.RelationSentence> corpusTSLSentences = null;
	protected Hashtable<String, Vector<String>> corpusPatientFileHash = null;
	protected Readmission readmission = null;
	protected boolean displayTargetConceptAnnotationsOnly = false;
	protected DocumentListPanel documentListPanel = null;
	protected Document displayedDocument = null;
	protected String lastSelectedCorpusPatient = null;
	protected boolean isSemanticAnnotationDisplay = true;
	protected File corpusDirectory = null;
	protected boolean userInteractionThreadWait = false;
	private Object mouseEnteredSource = null;
	private boolean useSectionHeaderHeuristics = true;
	private boolean displayRelevantSectionHeadersOnly = false;
	private StartupParameters startupParameters = null;
	private Vector<Rule> automaticallyGeneratedRules = null;
	private boolean useForwardChainingInferenceInPatternAnalysis = false;
	private boolean conjoinHeaderWithSentence = false;
	private GrammarEBL grammarEBL = null;
	private GrammarEBLCompilerSpecialization grammarEBLCompilerSpecialization = null;
	private LPCFG lpcfg = null;
	private Vector<int[]> annotationAttachments = new Vector(0);
	private double annotationProbabilityCutoff = 0;
	private boolean useFCIEToInferTargetConcepts = false;
	private Vector<String> tuffyRelationNames = null;
	protected boolean isMouseDown = false;
	private boolean compareAnnotationsGoodnessOnly = true;
	private boolean compareAnnotationsTargetsAndGoodness = false;
	private moonstone.io.db.MySQLAPI MoonstoneMySQLAPI = null;
	private Modifiers annotationModifiers = new AMIR01Modifiers(this);
	private Rule selectedGrammarRule = null;
	private boolean printRuleStatistics = false;

	public static String OntologyPropertyName = "ontology";
	protected static String RuleDirectoryPropertyName = "rule_directory";
	protected static String WordRuleTemplateString = "'((ruleid xxx) (words  (\"x\" \"y\" \"z\")) (concept \"x\") )";
	protected static String MeasurementRuleTemplateString = "'((ruleid xxx) (words (\"x\" \"y\")) (cui \"Cxxx\") (concept \"x\") (window 4.0) (highrange (100.0 110.0)) "
			+ " (lowrange (90.0 100.0)) (convert_to_fahrenheit true) (ifhigh (directionality affirmed)) (iflow (directionality negated)))";
	protected static String TagRuleTemplateString = "'((ruleid xxx))";
	protected static String LabelRuleTemplateString = "'((ruleid xxx))";
	protected static String InferenceRuleTemplateString = "'((ruleid xxx) (-> () ()))";
	protected static Sexp WordRuleTemplate = null;
	protected static Sexp MeasurementRuleTemplate = null;
	protected static Sexp TagRuleTemplate = null;
	protected static Sexp LabelRuleTemplate = null;
	protected static Sexp InferenceRuleTemplate = null;
	public static String tentativeRuleFileName = "ExtractedWorkbenchRules";
	protected static String corpusDirectoryPropertyName = "input_directory";
	protected static String trainingDirectoryPropertyName = "training_directory";
	protected static String resultsDirectoryPropertyName = "output_directory";
	protected static String resourceDirectoryPropertyName = "resource_directory";
	public static String RuleDirectoryName = "rule_directory";
	protected static String[] RuleTypes = { "wordrule", "tagrule", "measurementrule", "labelrule", "inferencerule" };
	public static MoonstoneRuleInterface RuleEditor = null;
	protected static String JavaPathnameList = "JavaPackages";
	protected static String ParameterDelimiter = ",";
	public static String UseStructureGrammarPropertyName = "UseStructureGrammar";
	public static String UseSectionHeaderHeuristics = "UseSectionHeaderHeuristics";
	protected static String DefaultReportName = "NOPATIENT_00000000_xyz";
	protected static String DoConjoinHeaderWithSentence = "ConjoinHeaderWithSentence";
	protected static String UseForwardChainingInferenceEngineInPatternAnalysis = "UseForwardChainingInferenceEngineInPatternAnalysis";
	protected static String LoadKBFromMySQL = "LoadKBFromMySQL";
	// public static String RuleExtractionDirectoryPropertyName =
	// "RuleExtractionDirectoryName";
	// public static String RuleStorageDirectoryPropertyName =
	// "RuleStorageDirectoryPropertyName";
	public static String ExcelGrammarRuleFileName = "ExcelGrammarRuleFileName";
	public static String UseLCPFG = "UseLCPFG";
	public static String SelectedGrammarDirectoryName = "SelectedGrammar";
	public static String UseIndexFinder = "UseIndexFinder";
	public static String UseFCIEForTargetConcepts = "UseFCIEForTargetConcepts";
	public static String TuffyRelationNames = "TuffyRelationNames";

	protected static String[][] menuInfo = { { null, "operations", "Operations" }, { null, "rules", "Rules" },
			{ null, "interpretation", "Interpretation" }, { null, "basilisk", "Basilisk" },
			{ null, "workbench", "Evaluation Workbench" },
			// { null, "query", "Query" },
			{ null, "learning", "Learning" }, { null, "EBL", "EBL" }, { null, "projects", "Projects" },
			{ null, "LPCFG", "LPCFG" }, { null, "grammar", "Grammar" }, { null, "test", "Test" },
			{ null, "knowledge", "Knowledge" } };

	protected static Object[][] menuItemInfo = { { "rules", "reloadRulesFromFile", "Reload Rules From File" },
			{ "rules", "storerulestoselectedfile", "Store Rules To Selected File" },
			{ "rules", "deleterule", "Delete Current Rule" }, { "rules", "storeallrules", "Save All Rules" },
			{ "rules", "createrule", "Create New Rule" }, { "rules", "instantiaterule", "Instantiate New Rule" },
			{ "rules", "storeRulesAsExcelFile", "Store Grammar Rules as Excel File" },
			{ "rules", "loadRulesFromExcelFile", "Load Grammar Rules from Excel File" },
			{ "rules", "changeGrammarDirectory", "Change Selected Grammar" },
			{ "rules", "writeWordGrammarRuleFile", "Write Word Grammar Rule File" },
			{ "rules", "runRuleBuilder", "Run Rule Builder" },
			{ "rules", "viewSelectedRuleFile", "View Selected Rule in Text Editor" },
			{ "rules", "deleteSelectedRuleFile", "Delete Selected Rule File" },
			{ "rules", "identifyRulesContainingPatternElement", "Identify rules containing pattern element" },
			{ "rules", "printOrderedRulesWithNumericSuffixes", "Print ordered rules with numeric suffixes" },

			{ "interpretation", "getInterpretedPatternAnalysisAnnotations",
					"Get Interpreted Pattern Analysis Annotations" },
			{ "interpretation", "getAllPatternAnalysisAnnotations", "Get All Pattern Analysis Annotations" },
			{ "interpretation", "getGrammarRulePatternAnalysisAnnotations",
					"Get Rule-specific Pattern Analysis Annotations" },
			{ "interpretation", "analyzePatientFilesAndDisplayInQueryPanel",
					"Analyze Patient Files, Display in Query Panel" },
			{ "interpretation", "toggleDisplayTargetConceptAnnotationsOnly",
					"Toggle Display of Target Concept Annotations Only" },
			{ "interpretation", "toggleUseFCIEInPatternAnalysis",
					"Toggle Use of Forward Chaining Inference in Pattern Analysis" },

			{ "interpretation", "analyzeDBCorpusFilesReinitializeWB",
					"Analyze DB Corpus Files, Reinitialize Workbench" },

			{ "interpretation", "setAnnotationProbabilityCutoff", "Set Annotation Probability Cutoff" },
			{ "operations", "storeCorpusInterpretations", "Store Corpus Interpretations" },
			{ "operations", "endinteraction", "Continue to Next Document" },
			{ "operations", "toggleDisplayedAnnotations", "Toggle Grammar Annotation Display" },
			{ "operations", "toggleRelevantSectionHeadersOnly", "Toggle Relevant Section Headers Only" },
			{ "operations", "toggleUMLS", "Toggle UMLS" },
			{ "operations", "toggleAnnotationSelectionCriteria", "Toggle Annotation Selection Criteria" },
			{ "operations", "rewriteKBFiles", "Rewrite KB Files" },
			// { "operations", "addApacheLicense", "Add Apache2 License to Source" },
			{ "operations", "exit", "Exit Rule Builder" },
			{ "workbench", "importSelectedWorkbenchAnnotationText",
					"Import Selected Workbench Annotation for Display" },
			{ "workbench", "importWorkbenchDocumentText", "Import Workbench Document Text" },
			{ "workbench", "selectWorkbenchDocumentForDisplay", "Select Workbench Document for Display" },
			{ "workbench", "analyzeWorkbenchDocumentsAndDisplayInQueryPanel",
					"Analyze Workbench Documents, Display in Query Panel" },
			{ "workbench", "toggledisplayworkbench", "Toggle Display Workbench" },
			{ "workbench", "generateWorkbenchPreannotations", "Generate Workbench Annotations" },
			{ "workbench", "extractRuleFromWorkbenchAnnotation", "Extract Rule from Workbench Annotation" },
			{ "workbench", "extractTentativeRulesFromWorkbench", "Extract Tentative Rules from Workbench Annotations" },
			{ "basilisk", "extractBasiliskTypePatterns", "Extract Basilisk Type Patterns" },
			{ "basilisk", "extractBasiliskRelationPatterns", "Extract Basilisk Relation Patterns" },
			{ "basilisk", "generateBasiliskLexicon", "Extract New Lexicon Using Basilisk" },
			// { "query", "toggleQueryInterface", "Toggle Display of Query
			// Interface" },

			// { "learning", "extractNCBOConceptsFromDocuments", "Extract NCBO
			// Concepts from Documents" },
			// { "learning", "storeTempEVALRules", "Learn TempEVAL Rules from
			// Annotations" },
			// { "learning", "learnWorkbenchRules", "Learn Rules from Workbench
			// Annotations" },
			// { "learning", "calculateTempEVALWordRelationStatistics",
			// "Calculate TempEVAL Word Relation Statistics" },

			{ "learning", "extractModifierGrammarRulesFromDomainOntology",
					"Extract Modifier Grammar Rules from Domain Ontology" },
			{ "learning", "generateLearningFeatureDefinitionVector", "Generate Learning Feature Definition Vector" },
			{ "learning", "generatePatientTrainingVectors", "Generate Patient Training Feature Vectors" },
			{ "learning", "generatePatientTestVectors", "Generate Patient Test Feature Vectors" },
			{ "EBL", "extractDomainSpecificRulesFullParsetree", "Extract Domain-Specific Rules (at Depth)" },
			{ "EBL", "extractDomainSpecificRulesOneLevelUnifiableConcepts",
					"Extract Domain-Specific Rules (1 level, Show Unifiable Concepts)" },
			{ "EBL", "extractDomainSpecificRulesOneLevelAllConcepts",
					"Extract Domain-Specific Rules (1 level, Show All Concepts)" },
			{ "EBL", "createBagOfConceptsRuleTerminal", "Create BagOfConcepts Rule from Terminal Annotations" },
			{ "EBL", "createBagOfConceptsRuleTerminalUnordered",
					"Create BagOfConcepts Rule from Terminal Annotations (Unordered)" },
			{ "EBL", "createBagOfConceptsRuleTopOnly", "Create BagOfConcepts Rule from Top Annotation Only" },
			{ "EBL", "createBagOfConceptsRuleTopOnlyUseLast",
					"Create BagOfConcepts Rule from Top Annotation Only (Use Last)" },
			{ "EBL", "extractEBLGrammarRulesFromCorpus", "Extract EBL Grammar Rules from Corpus" },
			{ "EBL", "extractEBLCompiledGrammar", "Extract Compiled Grammar" },
			{ "EBL", "extractEBLCompiledRulesFromAnnotation", "Extract Compiled Rules from Selected Annotation" },
			{ "EBL", "addAnnotationAttachment", "ATTACH" }, { "EBL", "clearAnnotationAttachments", "DETATCH" },
			{ "EBL", "clearEBL", "Clear EBL Data Structures" },
			{ "EBL", "toggleUseFCIE", "Toggle FCIE for Target Concepts" },
			{ "LPCFG", "lpcfgProcessAnnotation", "LPCFG Process Selected Annotation" },
			{ "LPCFG", "reloadPCFG", "Reinitialize LPCFG" },
			{ "LPCFG", "toggleUseRuleConditionalsOnlyInLCPFGProbabilities",
					"Toggle Use of Rule Conditionals Only In LCPFG Probabilities" },
			{ "projects", "applyReadmissionQuestionsAllPatients", "Apply Readmission Questions (All Patientss)" },
			{ "projects", "applyReadmissionQuestionsToListOfPatients", "Apply Readmission Questions (Single Patient)" },
			{ "projects", "parseReadmissionAnnotationNarrativeAttributes",
					"Parse Readmission Annotation Narrative Attributes" },
			{ "projects", "generateSingleDocumentEHostXML-Readmission",
					"Generate Single Document EHost XML (VA Readmission)" },
			{ "projects", "generateSingleDocumentEHostXML-AMI/R01", "Generate Single Document EHost XML (AMI/R01)" },
			{ "projects", "generateCorpusEHostXMLReadmission",
					"Analyze Corpus - Output EHost XML (VA 30-day Readmission)" },
			{ "projects", "generateCorpusEHostXMLAMIR01", "Analyze Corpus - Output EHost XML (AMI R01)" },
			{ "projects", "generateMultipleReadmissionPatientResults",
					"Generate Multiple Readmission Patient Results" },

			{ "projects", "generateMultipleReadmissionPatientResultsTuffy",
					"Generate Multiple Readmission Patient Results (TUFFY)" },

			{ "projects", "generateMultipleReadmissionPatientResultsARFFTrain",
					"Generate Multiple Readmission Patient Results (ARFF - Train)" },

			{ "projects", "generateMultipleReadmissionPatientResultsARFFTest",
					"Generate Multiple Readmission Patient Results (ARFF - Test)" },

			{ "projects", "generateMultipleReadmissionPatientResultsOnePass",
					"Generate Multiple Readmission Patient Results One Pass" },
			{ "projects", "calculateTuffyEHostAgreementStatisticsWithPredicates",
					"Calculate Tuffy EHost Agreement Statistics With Predicates" },
			{ "projects", "calculateTuffyEHostAgreementStatisticsNoPredicates",
					"Calculate Tuffy EHost Agreement Statistics No Predicates" },
			{ "projects", "calculateSalomehStatisticsViaWEKA", "Calculate Salomeh Statistics using WEKA" },
			{ "grammar", "sentenceGrammarView", "Sentence Grammar View" },
			{ "grammar", "documentGrammarView", "Document Grammar View" },
			{ "grammar", "structureGrammarView", "Structure Grammar View" },
			{ "grammar", "toggleUseStructureGrammar", "Toggle Structure Grammar" },
			{ "grammar", "printGrammarRuleUsage", "Print Grammar Rule Usage" },
			{ "grammar", "printSentenceGrammar", "Print Sentence Grammar" },
			{ "knowledge", "analyzeOWLOntologyRestoreExisting",
					"Extract Types / Rules from OWL Ontology - Restore Existing Ontology" },
			{ "knowledge", "analyzeOWLOntologyRestoreFalseUseDomainVariables",
					"Extract Types / Rules from OWL Ontology - Use New Ontology, Use Domain Variables" },
			{ "knowledge", "analyzeOWLOntologyRestoreFalseNoDomainVariables",
					"Extract Types / Rules from OWL Ontology - Use New Ontology, No Domain Variables" },
			{ "knowledge", "viewOntology", "View Ontology" }, { "knowledge", "reloadOntology", "Reload Ontology" },
			{ "knowledge", "storeTSLOntologyToDB", "Store TSL Ontology to DB" },
			{ "knowledge", "storeDocumentsFromDirectoryToDB", "Store Corpus Documents to DB" },
			{ "knowledge", "analyzeDBCorpusFilesAndStoreToDB", "Analyze DB Documents" },
			{ "knowledge", "reinitializeWorkbenchFromDB", "Reinitialize Workbench from DB" },
			{ "knowledge", "emptyDBTables", "Empty All DB Tables" },
			{ "knowledge", "extractFramenetOntology", "Extract Framenet Ontology" },
			{ "test", "runInterpreterInJavaMonitorLoop", "Run Interpreter In JavaMonitor Loop" },
			{ "test", "stopInterpreterInJavaMonitorLoop", "Stop Interpreter In JavaMonitor Loop" },
			{ "test", "analyzeDirectoryFilesToBillScubaDB", "Analyze Directory Files, Store to DB" },
			{ "test", "loadMySQLTables", "Load MySQL Tables" },
			{ "test", "extractTuffyEvidence", "Extract TUFFY Evidence from Annotations" },
			{ "test", "extractOntologyFromGrammar", "Extract Ontology From Grammar" },
			{ "test", "sortGrammarRulesByConcept", "Sort Grammar Rules By Concept" },

	};

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				try {
					Map<String, String> emap = System.getenv();
					System.out.println("ENVIRONMENT VARIABLES: " + emap.toString());
					MoonstoneRuleInterface rb = createMoonstoneRuleBuilder(true, true);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(new JFrame(), StrUtils.getStackTrace(e));
					e.printStackTrace();
				}
			}
		});
	}

	public MoonstoneRuleInterface(String startupPropertyFile) throws TSLException {
		this(startupPropertyFile, false);
	}

	public MoonstoneRuleInterface(String startupPropertyFile, boolean blockWorkbench) throws TSLException {
		super(new GridBagLayout());
		if (startupPropertyFile != null) {
			this.knowledgeEngine = new KnowledgeEngine("new", true, startupPropertyFile);
		} else {
			RuleEditor = this;
			this.knowledgeEngine = KnowledgeEngine.getCurrentKnowledgeEngine();
		}
		initialize(blockWorkbench);
	}

	// 2/10/2017: From IEViz command line
	public MoonstoneRuleInterface(Properties properties) {
		RuleEditor = this;
		this.knowledgeEngine = KnowledgeEngine.getCurrentKnowledgeEngine(true, properties);
		initialize(true);
	}

	public MoonstoneRuleInterface() throws TSLException {
		this((String) null);
	}

	private void initialize(boolean blockWorkbench) {
		String msg = null;
		this.startupParameters = this.knowledgeEngine.getStartupParameters();
		this.topGrammarDirectoryPathName = this.startupParameters.getRuleDirectory();
		this.grammarDirectoryNames = FUtils.getSubdirectoryNames(this.topGrammarDirectoryPathName);
		this.selectedGrammarDirectoryName = this.knowledgeEngine.getStartupParameters()
				.getPropertyValue(SelectedGrammarDirectoryName);
		if (this.selectedGrammarDirectoryName == null) {
			this.selectedGrammarDirectoryName = this.grammarDirectoryNames.firstElement();
		}
		int x = 1;
		this.corpusDirectoryName = this.knowledgeEngine.getStartupParameters()
				.getRootFileName(corpusDirectoryPropertyName);
		this.resultsDirectoryName = this.knowledgeEngine.getStartupParameters()
				.getRootFileName(resultsDirectoryPropertyName);
		this.trainingDirectoryName = this.knowledgeEngine.getStartupParameters()
				.getRootFileName(trainingDirectoryPropertyName);
		this.resourceDirectoryName = this.knowledgeEngine.getStartupParameters().getResourceDirectory();
		this.useSectionHeaderHeuristics = this.knowledgeEngine.getStartupParameters()
				.isPropertyTrue(UseSectionHeaderHeuristics);
		this.conjoinHeaderWithSentence = this.startupParameters.isPropertyTrue(DoConjoinHeaderWithSentence);

		this.useForwardChainingInferenceInPatternAnalysis = this.startupParameters
				.isPropertyTrue(UseForwardChainingInferenceEngineInPatternAnalysis);
		boolean useMySQL = this.knowledgeEngine.getStartupParameters().isPropertyTrue(LoadKBFromMySQL);

		this.useFCIEToInferTargetConcepts = this.knowledgeEngine.getStartupParameters()
				.isPropertyTrue(UseFCIEForTargetConcepts);

		this.setIsUseIndexFinder(true);

		this.control = new GrammarModule(this);

		readOntology(false);

		if (useMySQL) {
			// For when grammars, etc are stored in DB
			// this.dataBaseManager = new DataBaseManager(this);
			// this.dataBaseManager.readMoonstoneKnowledgeFromMySQL();
		} else {
			this.knowledgeEngine.doLoadFiles();
			this.control.readGrammars();
		}

		// 5/15/2018: Not sure when I'll need this again...
		// this.MoonstoneMySQLAPI = new moonstone.io.db.MySQLAPI(this);

		boolean useWorkbench = this.startupParameters.isPropertyTrue("UseWorkbenchInMoonstone");
		if (useWorkbench && !blockWorkbench && !startupWorkbench()) {
			System.exit(-1);
		}

		this.readmission = new Readmission(this);
		this.basilisk = new Basilisk();
		RegExprManager.createRegExprManager();
		this.populateRuleTokenHash();
		RegExprManager.createRegExprManager();

		if (this.corpusDirectoryName != null) {
			this.extractCorpusPatientNames();
		}

		String trstr = this.knowledgeEngine.getStartupParameters().getPropertyValue(TuffyRelationNames);
		this.tuffyRelationNames = StrUtils.stringList(trstr, ",");
		this.printRuleStatistics = this.knowledgeEngine.getStartupParameters().isPropertyTrue("PrintRuleUseStatistics");
	}

	public void readOntology(boolean forceReload) {
		try {
			int x = 0;
			String fname = this.knowledgeEngine.getStartupParameters().getPropertyValue(OntologyPropertyName);
			if (fname != null) {
				String fpath = this.knowledgeEngine.getStartupParameters().getResourceFileName(fname);
				if (fpath != null) {
					String fstr = FUtils.readFile(fpath);
					Ontology ontology = Ontology.createFromLisp(fstr, forceReload);
					this.knowledgeEngine.setCurrentOntology(ontology);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static MoonstoneRuleInterface createMoonstoneRuleBuilder(boolean withGUI, boolean doDisplay)
			throws TSLException {
		if (MoonstoneRuleInterface.RuleEditor == null) {
			MoonstoneRuleInterface.RuleEditor = new MoonstoneRuleInterface();
			if (withGUI) {
				MoonstoneRuleInterface.RuleEditor.createRuleBuilderGUI(doDisplay);
			}
		}
		return MoonstoneRuleInterface.RuleEditor;
	}

	public void createRuleBuilderGUI(boolean doDisplay) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;

		Dimension d = new Dimension(1200, 800);
		this.setPreferredSize(d);
		this.setMinimumSize(d);

		JPanel panel = new JPanel();
		JLabel label = new JLabel("Document:   ");
		this.documentPanel = new JTextArea() {
			public String getToolTipText(MouseEvent e) {
				return getAnnotationToolTip();
			}
		};
		this.documentPanel.addKeyListener(this);
		this.documentPanel.addMouseListener(this);
		this.documentPanel.addMouseMotionListener(this);
		this.documentPanel.setToolTipText("");

		JScrollPane jsp = new JScrollPane(this.documentPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// d = new Dimension(800, 400);
		d = new Dimension(800, 150);
		jsp.setMinimumSize(d);
		jsp.setPreferredSize(d);

		panel.add(label);
		panel.add(jsp);
		this.add(panel, c);
		c.gridy++;

		this.rootNode = new AnnotationRuleMutableTreeNode(this, null);
		this.model = new AnnotationRuleDefaultTreeModel(this.rootNode);
		this.annotationRuleJTree = new AnnotationRuleJTree(this.model);
		this.annotationRuleJTree.setCellRenderer(new MoonstoneRuleTreeCellRenderer());
		ToolTipManager.sharedInstance().registerComponent(this.annotationRuleJTree);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

		this.annotationRuleJTree.setEditable(false);

		this.annotationRuleJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		// this.annotationRuleJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);

		this.annotationRuleJTree.setShowsRootHandles(true);
		this.annotationRuleJTree.addTreeSelectionListener(this);
		annotationRuleJTree.addMouseMotionListener(this);
		annotationRuleJTree.addMouseListener(this);

		panel = new JPanel();
		label = new JLabel("Annotations:");
		d = new Dimension(800, 200);
		// d = new Dimension(1000, 600);

		jsp = new JScrollPane(this.annotationRuleJTree, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setMinimumSize(d);
		jsp.setPreferredSize(d);
		jsp.setVisible(true);
		panel.add(label);
		panel.add(jsp);

		this.add(panel, c);
		c.gridy++;

		d = new Dimension(1000, 350);
		this.tabbedClassPane = new TabbedPaneClass(this);
		jsp = new JScrollPane(this.tabbedClassPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setPreferredSize(d);
		jsp.setMinimumSize(d);
		this.add(jsp, c);

		this.grammarEBL = new GrammarEBL(this);
		this.grammarEBLCompilerSpecialization = new GrammarEBLCompilerSpecialization(this);

		if (this.startupParameters.isPropertyTrue(UseLCPFG)) {
			this.lpcfg = new LPCFG(this);
		}

		this.frame = new JFrame();
		this.frame.setJMenuBar(createMenuBar(menuInfo, menuItemInfo, this, this));
		this.frame.setContentPane(this);
		this.frame.pack();
		this.frame.setVisible(doDisplay);
		if (!initializeRuleTemplates()) {
			this.displayMessageDialog("Unable to convert rule template strings to S-expressions");
			System.exit(-1);
		}
		this.resetTitle();
	}

	private String getAnnotationToolTip() {
		String str = "*";
		// if (this.selectedAnnotation != null) {
		// str = this.selectedAnnotation.toExpandedString();
		// }
		return str;
	}

	private boolean startupWorkbench() {
		try {
			moonstone.io.db.MySQLAPI m = this.getMoonstoneMySQLAPI();
			if (m != null && m.isUseMySQL() && m.getCorpusName() != null && m.getPrimaryAnnotatorName() != null
					&& m.getSecondaryAnnotatorName() != null) {
				// 5/15/2018 REMOVED
				// int primaryRunID =
				// m.getWorkbenchMySQL().getMaxAnnotationRun(m.getPrimaryAnnotatorName(),
				// m.getPrimaryAnnotationToolName(), m.getCorpusName(), m.getTypeSystemName());
				// int secondaryRunID =
				// m.getWorkbenchMySQL().getMaxAnnotationRun(m.getSecondaryAnnotatorName(),
				// m.getSecondaryAnnotationToolName(), m.getCorpusName(),
				// m.getTypeSystemName());
				// EvaluationWorkbenchManager.loadWBGUIFromDB(m.getTypeSystemName(),
				// m.getCorpusName(), primaryRunID,
				// secondaryRunID);
			} else {
				EvaluationWorkbenchManager.doTest();
			}
			this.wbgui = WBGUI.WorkbenchGUI;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean initializeRuleTemplates() {
		try {
			TLisp tl = TLisp.getTLisp();
			WordRuleTemplate = (Sexp) tl.evalString(WordRuleTemplateString);
			MeasurementRuleTemplate = (Sexp) tl.evalString(MeasurementRuleTemplateString);
			TagRuleTemplate = (Sexp) tl.evalString(TagRuleTemplateString);
			LabelRuleTemplate = (Sexp) tl.evalString(LabelRuleTemplateString);
			InferenceRuleTemplate = (Sexp) tl.evalString(InferenceRuleTemplateString);
		} catch (Exception e1) {
			return false;
		}
		return true;
	}

	private static boolean valueChanging = false;

	public void valueChanged(TreeSelectionEvent e) {
		if (valueChanging) {
			valueChanging = false;
			return;
		}
		if (valueChanging || !this.isMouseInAnnotationTreePanel()) {
			return;
		}
		valueChanging = true;
		AnnotationRuleMutableTreeNode node = (AnnotationRuleMutableTreeNode) annotationRuleJTree
				.getLastSelectedPathComponent();
		if (node != null) {
			this.selectedNode = node;
			Object o = node.getUserObject();
			if (o instanceof Annotation) {
				Annotation annotation = (Annotation) o;
				selectAnnotation(annotation, true);
			}
		}
		valueChanging = false;
	}

	private void selectAnnotation(Annotation annotation, boolean fromJTree) {
		if (annotation != null) {
			this.lastSelectedAnnotation = this.selectedAnnotation;
			this.selectedAnnotation = annotation;
			// this.selectedGrammarRule = annotation.getRule();
			highlightAnnotationText();
			displaySelectedRule(annotation.getRule());
			this.moonstoneQueryPanel.populateKBUsingAnnotations(VUtils.listify(annotation));
			if (!fromJTree) {
				selectAnnotationRuleNode(annotation);
			}
		}
	}

	public Annotation getSelectedAnnotation() {
		return this.selectedAnnotation;
	}

	private void selectAnnotationRuleNode(Annotation annotation) {
		if (annotation != null && annotation.getUserObject() != null
				&& this.selectedNode != annotation.getUserObject()) {
			this.selectedNode = (AnnotationRuleMutableTreeNode) annotation.getUserObject();
			TreeNode[] nodes = ((DefaultTreeModel) this.annotationRuleJTree.getModel())
					.getPathToRoot(this.selectedNode);
			TreePath tpath = new TreePath(nodes);
			this.annotationRuleJTree.scrollPathToVisible(tpath);
			this.annotationRuleJTree.setSelectionPath(tpath);
		}
	}

	private void highlightAnnotationText() {
		if (this.displayedAnnotations != null) {
			int start = 0;
			int end = 0;
			int length = 0;
			String text = this.documentPanel.getText();
			Highlighter hl = this.documentPanel.getHighlighter();
			HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.pink);
			for (Annotation annotation : this.displayedAnnotations) {
				start = annotation.getTextStart();
				end = annotation.getTextEnd();
				length = end - start + 1;
				try {
					if (annotation.equals(this.selectedAnnotation)) {
						hl.addHighlight(start, start + length, DefaultHighlighter.DefaultPainter);
					} else {
						hl.addHighlight(start, start + length, DefaultHighlighter.DefaultPainter);
						// hl.addHighlight(start, start + length,
						// painter);
					}

				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
			if (this.selectedAnnotation != null) {

				start = this.selectedAnnotation.getTextStart();
				end = this.selectedAnnotation.getTextEnd();
				length = end - start + 1;

				int cp = this.documentPanel.getCaretPosition();
				if (end < this.selectedAnnotation.getDocument().getTextLength()
						&& cp < this.selectedAnnotation.getTextStart() || cp > this.selectedAnnotation.getTextEnd()) {
					try {
						String dtext = this.documentPanel.getText();
						// System.out
						// .println("DPanelTextLength=" + dtext.length());
						this.documentPanel.setCaretPosition(end);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void itemStateChanged(ItemEvent e) {
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		String cmd = e.getActionCommand();
		if ("getInterpretedPatternAnalysisAnnotations".equals(cmd)) {
			applyNarrativeGrammarToText(true, true, true);
		} else if ("getAllPatternAnalysisAnnotations".equals(cmd)) {
			applyNarrativeGrammarToText(false, false, false);
		} else if ("getGrammarRulePatternAnalysisAnnotations".equals(cmd)) {
			this.applyNarrativeGrammarToTextWithSelectedGrammarRuleDisplay();
		} else if ("applyInferenceToText".equals(cmd)) {
			applyInferenceToText();
		} else if ("analyzePatientFilesAndDisplayInQueryPanel".equals(cmd)) {
			analyzePatientFilesAndDisplayInQueryPanel();
		} else if ("generateWorkbenchPreannotations".equals(cmd)) {
			// this.getMoonstoneControlInterface().preAnnotateSecondaryDocuments();
			// this.getMoonstoneControlInterface().preAnnotateSecondaryDocuments();
		} else if ("extractRuleFromWorkbenchAnnotation".equals(cmd)) {
			// extractRuleFromWorkbenchAnnotation();
		} else if ("extractTentativeRulesFromWorkbench".equals(cmd)) {
			// this.tentativeRuleHash.clear();
			// EvaluationWorkbenchRuleExtractor
			// .extractTentativeRules(this.moonstoneControlInterface);
		} else if ("convertRulesToSimpleFormat".equals(cmd)) {
			convertRulesToSimpleFormat();
		} else if ("extractBasiliskTypePatterns".equals(cmd)) {
			extractBasiliskTypePatterns();
		} else if ("extractBasiliskRelationPatterns".equals(cmd)) {
			extractBasiliskRelationPatterns();
		} else if ("generateBasiliskLexicon".equals(cmd)) {
			generateBasiliskLexicon();
		} else if ("reloadRulesFromFile".equals(cmd)) {
			reloadRules(false);
		} else if ("storeallrules".equals(cmd)) {
			int answer = JOptionPane.showConfirmDialog(new JFrame(), "Overwrite Existing Sentence Grammar?");
			if (answer == JOptionPane.YES_OPTION) {
				this.getSentenceGrammar().storeAllRules();
			}
		} else if ("toggleQueryInterface".equals(cmd)) {
			JFrame frame = this.moonstoneQueryPanel.getFrame();
			frame.setVisible(frame.isVisible() ? false : true);
		} else if ("extractNCBOConceptsFromDocuments".equals(cmd)) {
			extractNCBOConceptsFromDocuments();
		} else if ("learnWorkbenchRules".equals(cmd)) {
			learnWorkbenchRules();
		} else if (source.equals(this.ruleTypeCB)) {
			this.ruleType = (String) this.ruleTypeCB.getSelectedItem();
		} else if (source.equals(this.ruleIDCB)) {
			displaySelectedRule();
		} else if (source.equals(this.ruleTokenTextField)) {
			findRulesByToken();
		} else if (source.equals(this.ruleNameTextField)) {
			findRulesByName();
		} else if (source.equals(this.ruleFileNameTextField)) {
			this.assignRuleFilePath();
		} else if (source.equals(this.ruleFileNameButton)) {
			chooseRuleFile();
		} else if (source.equals(this.ruleConceptCB)) {
			findRulesContainingConcept();
		} else if ("createrule".equals(cmd)) {
			createRule();
		} else if ("instantiaterule".equals(cmd)) {
			instantiateNewRule();
		} else if ("storerulestoselectedfile".equals(cmd)) {
			this.displayMessageDialog("Currently unavailable");
			// storeRulesToSelectedFile();
		} else if ("deleterule".equals(cmd)) {
			deleteRule(true);
		} else if ("toggleAnnotationSelectionCriteria".equals(cmd)) {
			this.toggleCompareAnnotationsTargetsAndGoodness();
		} else if ("exit".equals(cmd)) {
			System.exit(0);
		} else if ("endinteraction".equals(cmd)) {
			this.setUserInteractionThreadWait(false);
		} else if ("toggledisplayworkbench".equals(cmd)) {
			// if (this.workbench != null) {
			// this.workbench.getFrame().setVisible(
			// !this.workbench.getFrame().isVisible());
			// }
		} else if ("parseReadmissionAnnotationNarrativeAttributes".equals(cmd)) {
			this.readmission.getNarrativeAnnotationMatchPercentage(this);
		} else if ("sentenceGrammarView".equals(cmd)) {
			viewGrammar(this.control.getSentenceGrammar());
		} else if ("documentGrammarView".equals(cmd)) {
			viewGrammar(this.control.getDocumentGrammar());
		} else if ("structureGrammarView".equals(cmd)) {
			viewGrammar(this.control.getStructureGrammar());
		} else if ("toggleUseStructureGrammar".equals(cmd)) {
			this.control.toggleUseStructureGrammar();
			this.resetTitle();
		} else if ("runInterpreterInJavaMonitorLoop".equals(cmd)) {
			// runInterpreterInJavaMonitorLoop();
		} else if ("stopInterpreterInJavaMonitorLoop".equals(cmd)) {
			stopInterpreterInJavaMonitorLoop();
		} else if ("toggleDisplayTargetConceptAnnotationsOnly".equals(cmd)) {
			this.toggleDisplayTargetConceptAnnotationsOnly();
			this.repopulateJTree();
		} else if ("selectWorkbenchDocumentForDisplay".equals(cmd)) {
			displayWorkbenchDocumentList();
		} else if ("toggleRelevantSectionHeadersOnly".equals(cmd)) {
			toggleRelevantSectionHeadersOnly();
		} else if ("loadMySQLTables".equals(cmd)) {
			// new DataBaseManager(this).storeAll();
		} else if ("extractSnapshotRulesFromSelectedAnnotation".equals(cmd)) {
			extractSnapshotRulesFromSelectedAnnotation();
		} else if ("toggleUseFCIEInPatternAnalysis".equals(cmd)) {
			this.useForwardChainingInferenceInPatternAnalysis = !this.useForwardChainingInferenceInPatternAnalysis;
		} else if ("generateCorpusEHostXMLReadmissionReadmission".equals(cmd)) {
			this.testEHostMultiReportAnalysisReadmission(true);
		} else if ("generateCorpusEHostXMLAMIR01".equals(cmd)) {
			this.getSentenceGrammar().clearRuleCountHashtables();
			this.testEHostMultiReportAnalysisAMIR01();
			this.getSentenceGrammar().clearRuleCountHashtables();
			this.getSentenceGrammar().printRuleInstanceCounts();
		} else if ("generateSingleDocumentEHostXML-Readmission".equals(cmd)) {
			String text = this.documentPanel.getText();
			String mexml = new MoonstoneEHostXML(this).generateEHostXMLSingleDocument(this, text, true, false);
			System.out.println(mexml);
		} else if ("generateSingleDocumentEHostXML-AMI/R01".equals(cmd)) {
			String text = this.documentPanel.getText();
			String mexml = new MoonstoneEHostXML(this).generateEHostXMLSingleDocument(this, text, true, true);
			System.out.println(mexml);
		} else if ("importSelectedWorkbenchAnnotationText".equals(cmd)) {
			if (this.getWorkbench() != null && this.getWorkbench().getAnalysis().getSelectedAnnotation() != null) {
				String atext = this.getWorkbench().getAnalysis().getSelectedAnnotation().getText() + "\n";
				this.documentPanel.setText(atext);
			}
		} else if ("importWorkbenchDocumentText".equals(cmd)) {
			if (this.getWorkbench() != null && this.getWorkbench().getAnalysis().getSelectedDocument() != null) {
				String dtext = this.getWorkbench().getAnalysis().getSelectedDocument().getText() + "\n";
				this.documentPanel.setText(dtext);
			}
		} else if ("extractEBLGrammarRulesFromCorpus".equals(cmd)) {
			this.runGrammarEBLCorpusThread();
		} else if ("extractEBLCompiledGrammar".equals(cmd)) {
			this.runGrammarEBLCompilerSpecializationThread();
		} else if ("extractDomainSpecificRulesOneLevelUnifiableConcepts".equals(cmd)) {
			this.grammarEBL.extractDomainSpecificRulesFromSelectedAnnotation(this.getSelectedAnnotation(), false, true);
		} else if ("extractDomainSpecificRulesOneLevelAllConcepts".equals(cmd)) {
			this.grammarEBL.extractDomainSpecificRulesFromSelectedAnnotation(this.getSelectedAnnotation(), false,
					false);
		} else if ("extractDomainSpecificRulesFullParsetree".equals(cmd)) {
			this.grammarEBL.extractDomainSpecificRulesFromSelectedAnnotation(this.getSelectedAnnotation(), true, false);
		} else if ("createBagOfConceptsRuleTerminal".equals(cmd)) {
			this.grammarEBL.createBagOfConceptsRuleFromMoonstoneAnnotation(false, true, false);
		} else if ("createBagOfConceptsRuleTerminalUnordered".equals(cmd)) {
			this.grammarEBL.createBagOfConceptsRuleFromMoonstoneAnnotation(false, false, false);
		} else if ("createBagOfConceptsRuleTopOnly".equals(cmd)) {
			this.grammarEBL.createBagOfConceptsRuleFromMoonstoneAnnotation(true, true, false);
		} else if ("createBagOfConceptsRuleTopOnlyUseLast".equals(cmd)) {
			this.grammarEBL.createBagOfConceptsRuleFromMoonstoneAnnotation(true, true, true);
		} else if ("clearEBL".equals(cmd)) {
			this.grammarEBL.clear();
		} else if ("lpcfgProcessAnnotation".equals(cmd)) {
			if (this.lpcfg != null) {
				this.lpcfg.processAnnotationMeaningCounts(this.getSelectedAnnotation());
			}
		} else if ("reloadPCFG".equals(cmd)) {
			if (this.lpcfg != null) {
				this.lpcfg.loadMeaningCounts();
			}
		} else if ("toggleUseRuleConditionalsOnlyInLCPFGProbabilities".equals(cmd)) {
			toggleUseRuleConditionalsOnlyInLCPFGProbabilities();
		} else if ("storeRulesAsExcelFile".equals(cmd)) {
			this.getControl().getSentenceGrammar().storeRulesAsExcelFile();
		} else if ("loadRulesFromExcelFile".equals(cmd)) {
			reloadRules(true);
		} else if ("addAnnotationAttachment".equals(cmd)) {
			this.addAnnotationAttachment();
		} else if ("clearAnnotationAttachments".equals(cmd)) {
			this.clearAnnotationAttachments();
		} else if ("printGrammarRuleUsage".equals(cmd)) {
			this.getSentenceGrammar().printRuleUsage();
		} else if ("generateMultipleReadmissionPatientResults".equals(cmd)) {
			ReadmissionCorpusProcessor rpp = new ReadmissionCorpusProcessor(this, false);
			rpp.analyzeMultipleCasesFromMultipleFolders(true, false, false, false);
		} else if ("generateMultipleReadmissionPatientResultsTuffy".equals(cmd)) {
			ReadmissionCorpusProcessor rpp = new ReadmissionCorpusProcessor(this, true);
			rpp.analyzeMultipleCasesFromMultipleFolders(true, true, false, false);
		} else if ("generateMultipleReadmissionPatientResultsARFFTrain".equals(cmd)) {
			ReadmissionCorpusProcessor rpp = new ReadmissionCorpusProcessor(this, false);
			// rpp.analyzeMultipleCasesFromMultipleFolders(true, false, true,
			// true);
			rpp.analyzeMultipleCasesFromSingleFolder(true, false, true, true,
					ReadmissionCorpusProcessor.CombinedFileOrganizationMode);
		} else if ("generateMultipleReadmissionPatientResultsARFFTest".equals(cmd)) {
			ReadmissionCorpusProcessor rpp = new ReadmissionCorpusProcessor(this, false);
			rpp.analyzeMultipleCasesFromMultipleFolders(true, false, true, false);
		} else if ("generateMultipleReadmissionPatientResultsARFFTestCombinedFolder".equals(cmd)) {
			ReadmissionCorpusProcessor rpp = new ReadmissionCorpusProcessor(this, false);
			rpp.analyzeMultipleCasesFromSingleFolder(true, false, true, false,
					ReadmissionCorpusProcessor.CombinedFileOrganizationMode);
		} else if ("generateMultipleReadmissionPatientResultsARFFTestFromSingleFolder".equals(cmd)) {
			ReadmissionCorpusProcessor rpp = new ReadmissionCorpusProcessor(this, false);
			rpp.analyzeMultipleCasesFromSingleFolder(true, false, true, false,
					ReadmissionCorpusProcessor.SingleFileOrganizationMode);
		} else if ("generateMultipleReadmissionPatientResultsOnePass".equals(cmd)) {
			ReadmissionCorpusProcessor rpp = new ReadmissionCorpusProcessor(this, false);
			rpp.analyzeMultipleCasesOnePass(true, false, ReadmissionCorpusProcessor.SingleFileOrganizationMode);
		} else if ("calculateSalomehStatisticsViaWEKA".equals(cmd)) {
			ReadmissionCorpusProcessor rpp = new ReadmissionCorpusProcessor(this, false);
			rpp.generateSalomehFormatResultsViaWEKA();
		} else if ("setAnnotationProbabilityCutoff".equals(cmd)) {
			String str = JOptionPane.showInputDialog("Cutoff:");
			double cutoff = Double.parseDouble(str);
			if (cutoff >= 0) {
				this.setAnnotationProbabilityCutoff(cutoff);
			}
		} else if ("analyzeOWLOntologyRestoreExisting".equals(cmd)) {
			this.analyzeOWLOntology(null, true, true);
		} else if ("analyzeOWLOntologyRestoreFalseNoDomainVariables".equals(cmd)) {
			this.analyzeOWLOntology(null, false, false);
		} else if ("analyzeOWLOntologyRestoreFalseUseDomainVariables".equals(cmd)) {
			this.analyzeOWLOntology(null, false, true);
		} else if ("changeGrammarDirectory".equals(cmd)) {
			this.changeGrammarDirectoryName();
		} else if ("toggleUMLS".equals(cmd)) {
			this.toggleUseIndexFinder();
		} else if ("writeWordGrammarRuleFile".equals(cmd)) {
			((NarrativeGrammar) this.getSentenceGrammar()).writeWordGrammarRuleFile();
		} else if ("extractTuffyEvidence".equals(cmd)) {
			this.outputTuffyEvidence();
		} else if ("calculateTuffyEHostAgreementStatisticsWithPredicates".equals(cmd)) {
			// new ReadmissionTuffy(this, true);
		} else if ("calculateTuffyEHostAgreementStatisticsNoPredicates".equals(cmd)) {
			// new ReadmissionTuffy(this, false);
		} else if ("generateLearningFeatureDefinitionVector".equals(cmd)) {
			FeatureSet.getCurrentFeatureSet(this).processMultipleTrainingSets(false, true);
		} else if ("generatePatientTrainingVectors".equals(cmd)) {
			FeatureSet.getCurrentFeatureSet(this).processMultipleTrainingSets(true, false);
		} else if ("generatePatientTestVectors".equals(cmd)) {
			FeatureSet.getCurrentFeatureSet(this).processMultipleTrainingSets(false, false);
		} else if ("viewOntology".equals(cmd)) {

			// TSLOntologyPanel.displayTSLOntologyFrame(this);

			TSLOntologyFrame f = new TSLOntologyFrame(this);
			f.setVisible(true);

		} else if ("printSentenceGrammar".equals(cmd)) {
			String str = this.getSentenceGrammar().toLispString();
			System.out.println(str);
		} else if ("runRuleBuilder".equals(cmd)) {
			MoonstoneRuleBuilderPanel mrb = new MoonstoneRuleBuilderPanel(this);
			mrb.show();
			MoonstoneRuleSexpPanel msp = new MoonstoneRuleSexpPanel();
			msp.show();
		} else if ("storeTSLOntologyToDB".equals(cmd)) {
			String tsname = JOptionPane.showInputDialog(new JFrame(), "Typesystem Name:");
			this.MoonstoneMySQLAPI.storeAnnotationTypeSystem(this.getKnowledgeEngine().getCurrentOntology(), tsname);
		} else if ("storeDocumentsFromDirectoryToDB".equals(cmd)) {
			this.MoonstoneMySQLAPI.getWorkbenchMySQL().getTSLMySQL()
					.storeDocumentsFromDirectory(this.MoonstoneMySQLAPI.getCorpusName(), this.getCorpusDirectoryName());
		} else if ("analyzeDBCorpusFilesAndStoreToDB".equals(cmd)) {
			this.MoonstoneMySQLAPI.processDocumentsFromCorpus();
		} else if ("reinitializeWorkbenchFromDB".equals(cmd)) {
			this.MoonstoneMySQLAPI.reinitializeWorkbenchFromDB();
		} else if ("reloadOntology".equals(cmd)) {
			this.reloadOntology();
		} else if ("emptyDBTables".equals(cmd)) {
			this.MoonstoneMySQLAPI.getWorkbenchMySQL().getTSLMySQL().emptyAllTables();
		} else if ("rewriteKBFiles".equals(cmd)) {
			this.getKnowledgeEngine().doWriteFiles();
		} else if ("viewSelectedRuleFile".equals(cmd)) {
			if (this.selectedAnnotation != null && this.selectedAnnotation.getRule() != null) {
				viewSelectedRuleFile(this.selectedAnnotation.getRule());
			}
		} else if ("deleteSelectedRuleFile".equals(cmd)) {
			deleteSelectedRuleFile();
		}

		// 5/16/2018 TEST
		else if ("extractOntologyFromGrammar".equals(cmd)) {
			this.getControl().getSentenceGrammar().extractOntology();
		} else if ("sortGrammarRulesByConcept".equals(cmd)) {
			this.getControl().getSentenceGrammar().printRulesByConcept();
		}

		// 6/7/2018
		else if ("extractFramenetOntology".equals(cmd)) {
			this.extractFramenetOntology();
		}

		// 6/15/2018
		else if ("identifyRulesContainingPatternElement".equals(cmd)) {
			this.printRulesContainingPatternElement();
		}

		// 7/12/2018
		else if ("analyzeDirectoryFilesToBillScubaDB".equals(cmd)) {
			this.analyzeDirectoryFilesToBillScubaDB();
		} else if ("extractEBLCompiledRulesFromAnnotation".equals(cmd)) {
			new GrammarEBLCompilerSpecialization(this).printNewRulesForSelectedAnnotation(this.selectedAnnotation);
		} else if ("printOrderedRulesWithNumericSuffixes".equals(cmd)) {
			printOrderedRulesWithNumericSuffixes();
		} else if ("addApacheLicense".equals(cmd)) {
			addApache2LicenseToSourceFiles();
		}
	}

	// 8/6/2018
	public static void addApache2LicenseToSourceFiles() {
		String ldir = "/Users/leechristensen/Desktop/MoonstoneDemoVersions/MoonstoneDemo/resources/Apache2License.txt";
		String sdir = "/Users/leechristensen/Desktop/READMISSION/VINCIMoonstoneBackup_8_7_2018";
		addApache2LicenseToSourceFiles(ldir, sdir);
	}

	public static void addApache2LicenseToSourceFiles(String licensefilestr, String sourcedirstr) {
		Hashtable<String, String> fhash = new Hashtable();
		if (sourcedirstr != null && licensefilestr != null) {
			String license = FUtils.readFile(licensefilestr);
			File sourcedir = new File(sourcedirstr);
			if (sourcedir.exists() && sourcedir.isDirectory()) {
				File[] files = sourcedir.listFiles();
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					String fpath = file.getAbsolutePath();
					if (file.isFile() && fpath.endsWith(".java")) {
						String text = FUtils.readFile(file);
						String alltext = "/*\n" + license + "*/\n" + text;
						fhash.put(fpath, alltext);
					} else if (file.isDirectory()) {
						addApache2LicenseToSourceFiles(licensefilestr, fpath);
					}
				}
			}
		}
		for (String fpath : fhash.keySet()) {
			String ftext = fhash.get(fpath);
			FUtils.writeFile(fpath, ftext);
		}
	}

	public void printOrderedRulesWithNumericSuffixes() {
		Hashtable<String, Vector<Rule>> idhash = new Hashtable();
		Hashtable<Vector<Vector>, Rule> newphash = new Hashtable();
		Vector<String> rids = new Vector(0);
		for (Rule rule : this.getSentenceGrammar().getAllRules()) {
			if (rule.getSourceFilePath().toLowerCase().contains("onyx")) {
				rids.add(rule.getRuleID());
				VUtils.pushHashVector(idhash, rule.getRuleID(), rule);
			}
		}
		Collections.sort(rids);
		StringBuffer sb = new StringBuffer();
		sb.append("'(\nwordrules\n");
		for (String rid : rids) {
			Vector<Rule> matching = idhash.get(rid);
			int rcount = 0;
			for (Rule rule : matching) {
				String newrid = rid + "_" + rcount++;
				Rule newrule = rule.clone();
				newrule.setRuleID(newrid);
				Sexp newsexp = newrule.toSexp();
				newrule.setSexp(newsexp);
				String newrulestr = newrule.getSexp().toNewlinedString();
				if (newphash.get(newrule.getPatternLists()) == null) {
					newphash.put(newrule.getPatternLists(), newrule);
					sb.append(newrulestr + "\n\n");
				}
			}
		}
		sb.append("\n)\n");
		String fname = this.getStartupParameters().getRuleDirectory() + File.separatorChar
				+ "AlphabetizedNumerizedSentenceRules";
		FUtils.writeFile(fname, sb.toString());
	}

	public void analyzeDirectoryFilesToBillScubaDB() {
		MySQLAPI_2S msbs = new MySQLAPI_2S(this);
		String dname = this.startupParameters.getRootDirectory() + File.separatorChar
				+ this.startupParameters.getPropertyValue("TextInputDirectory");
		msbs.analyzeDirectoryFiles("moonstone", dname);
	}

	public void runGrammarEBLCorpusThread() {
		this.grammarEBL.getDocumentFilesFromTrainingDirectory();
		this.grammarEBL.analyzeCorpusFiles();

		// Thread t = new Thread(new Runnable() {
		// public void run() {
		// GrammarEBL gebl = MoonstoneRuleInterface.RuleEditor.grammarEBL;
		// gebl.getDocumentFilesFromTrainingDirectory();
		// gebl.analyzeCorpusFiles();
		// }
		// });
		// t.start();
		// this.grammarEBL.runCorpusAnalysisThread();
	}

	public void runGrammarEBLCompilerSpecializationThread() {
		this.grammarEBLCompilerSpecialization.getDocumentFilesFromTrainingDirectory();
		this.grammarEBLCompilerSpecialization.analyzeCorpusFiles();

		// Thread t = new Thread(new Runnable() {
		// public void run() {
		// GrammarEBL gebl =
		// MoonstoneRuleInterface.RuleEditor.grammarEBLCompilerSpecialization;
		// gebl.getDocumentFilesFromTrainingDirectory();
		// gebl.analyzeCorpusFiles();
		// }
		// });
		// t.start();
		// this.grammarEBLCompilerSpecialization.runCorpusAnalysisThread();
	}

	public void printRulesContainingPatternElement() {
		String ename = JOptionPane.showInputDialog("Element Name;");
		if (ename != null) {
			Vector<Rule> rules = this.getControl().getRulesContainingUserSpecifiedPatternElement(ename);
			if (rules != null) {
				for (Rule rule : rules) {
					System.out.println(rule);
				}
			}
		}
	}

	public void extractFramenetOntology() {
		String fndir = "/Users/leechristensen/Desktop/MoonstoneGrant/KBs/fndata-1.7";
		FramenetOntology oc = new FramenetOntology(this, fndir);

		int x = 1;
	}

	public void deleteSelectedRuleFile() {
		if (this.selectedAnnotation != null && this.selectedAnnotation.getRule() != null
				&& this.selectedAnnotation.getRule().getSourceFilePath() != null) {
			Rule rule = this.selectedAnnotation.getRule();
			String fpath = rule.getSourceFilePath();
			File file = new File(fpath);
			if (file.exists()) {
				int answer = JOptionPane.showConfirmDialog(new JFrame(), "Delete rule file (" + file.getName() + ")?");
				if (answer == JOptionPane.YES_OPTION) {
					file.delete();
					this.reloadRules(false);
				}
			}
		}
	}

	public void viewSelectedRuleFile(Rule rule) {
		try {
			if (rule != null && rule.getSourceFilePath() != null) {
				String fpath = rule.getSourceFilePath();
				File file = new File(fpath);
				if (file.exists()) {
					StringSelection selection = new StringSelection(rule.getRuleID());
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(selection, selection);
					Desktop.getDesktop().open(file);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void delectSelectedRuleFile() {
		try {
			if (this.selectedAnnotation != null && this.selectedAnnotation.getRule() != null
					&& this.selectedAnnotation.getRule().getSourceFilePath() != null) {
				String fpath = this.selectedAnnotation.getRule().getSourceFilePath();
				File file = new File(fpath);
				if (file.exists()) {
					Desktop.getDesktop().open(file);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reloadOntology() {
		KnowledgeEngine.getCurrentKnowledgeEngine().createNewCurrentKnowledgeBase();
		readOntology(true);
		this.control = new GrammarModule(this);
		this.control.readGrammars();
	}

	private static long recordMostTime = 0;

	private void testEHostMultiReportAnalysisReadmission(boolean nested) {
		try {
			long starttime = System.currentTimeMillis();
			MoonstoneEHostXML mexml = new MoonstoneEHostXML(this);
			if (nested) {
				mexml.generateEHostAnnotationsNested(this, true, false, true);
			} else {
				mexml.readmissionGenerateEHostAnnotationsFlat(this, true);
			}
			long endtime = System.currentTimeMillis();
			long elapsed = (endtime - starttime) / 1000 / 60;
			System.out.println("Time=" + elapsed + " minutes");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void testEHostMultiReportAnalysisAMIR01() {
		try {
			long starttime = System.currentTimeMillis();
			MoonstoneEHostXML mexml = new MoonstoneEHostXML(this);
			mexml.generateEHostAnnotationsNested(this, true, true, false);
			long endtime = System.currentTimeMillis();
			long elapsedSeconds = (endtime - starttime) / 1000;
			long elapsedMinutes = elapsedSeconds / 60;
			System.out.println("Seconds=" + elapsedSeconds + ",Minutes=" + elapsedMinutes);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void toggleUseRuleConditionalsOnlyInLCPFGProbabilities() {
		boolean flag = this.getLpcfg().isUseRuleConditionalsOnlyInProbabilityCalculation();
		this.getLpcfg().setUseRuleConditionalsOnlyInProbabilityCalculation(!flag);
		this.resetTitle();
	}

	public void analyzeOWLOntology(String uri, boolean restoreOntology, boolean useDomainVariables) {
		if (uri == null) {
			uri = JOptionPane.showInputDialog(new JFrame(), "OWL URI",
					"http://blulab.chpc.utah.edu/ontologies/examples/smoking.owl");
			uri = uri.trim();
		}
		ExtractOWLOntology ont = new ExtractOWLOntology(this);
		ont.setDomain(uri);
		ont.analyze(restoreOntology, useDomainVariables);
	}

	// 10/1/2015: Not yet tested...
	private void extractSnapshotRulesFromSelectedAnnotation() {
		if (this.selectedAnnotation != null) {
			String rname = "EBL";
			Vector<Rule> newrules = null;
			// newrules = new GrammarEBL().generateSnapshotRules(
			// this.selectedAnnotation, rname, 0);
			StringBuffer sb = new StringBuffer();
			if (newrules != null) {
				for (Rule rule : newrules) {
					sb.append(rule.getSexp().toNewlinedString());
					sb.append("\n\n");
				}
			}
			System.out.println(sb.toString());
		}
	}

	private void toggleRelevantSectionHeadersOnly() {
		this.displayRelevantSectionHeadersOnly = !this.displayRelevantSectionHeadersOnly;
		this.resetTitle();
	}

	private void displayWorkbenchDocumentList() {
		if (this.wbgui != null) {
			if (true) {
				Vector<Document> docs = this.wbgui.getAnalysis().getAllDocuments();
				try {
					this.documentListPanel = new DocumentListPanel(this, docs);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			this.documentListPanel.getFrame().setVisible(true);
		}
	}

	private void stopInterpreterInJavaMonitorLoop() {
		this.setUserInteractionThreadWait(false);
	}

	private void viewGrammar(Grammar grammar) {
		this.control.setCurrentGrammar(grammar);
		this.repopulateJTree();
		this.resetTitle();
	}

	private void applyNarrativeGrammarToTextWithSelectedGrammarRuleDisplay() {
		if (this.selectedGrammarRule == null) {
			this.selectedGrammarRule = this.getCurrentGrammar().selectGrammarRule();
		}
		if (this.selectedGrammarRule != null) {
			this.releaseAnnotations();
			String text = this.documentPanel.getText();
			Document doc = new Document(DefaultReportName, text);
			this.control.applyNarrativeGrammarToText(doc, false, false, false);
			repopulateJTree();
		}
	}

	private void applyNarrativeGrammarToText(boolean removeNested, boolean removeOverlapping, boolean interpretedOnly) {
		this.selectedGrammarRule = null;
		this.releaseAnnotations();
		String text = this.documentPanel.getText();
		Document doc = new Document(DefaultReportName, text);
		this.control.applyNarrativeGrammarToText(doc, removeNested, removeOverlapping, interpretedOnly);
		repopulateJTree();
		// new MoonstoneEHostXML(this).displayParsedSentenceXML();
	}

	public void reloadRules(boolean useExcel) {
		this.control.deleteAllDisplayedAnnotations();
		// this.readOntology();
		this.control.readGrammars(useExcel);
		repopulateJTree();
		this.resetTitle();
	}

	private void learnWorkbenchRules() {
		System.out.print("Learning workbench rules...");
		EvaluationWorkbenchRuleExtractor wbre = new EvaluationWorkbenchRuleExtractor(this);
		wbre.extractRulesFromWorkbenchAnnotations();
		this.storeTentativeRules();
		this.clearAutomaticallyGeneratedRules();
		System.out.println("Done.");
	}

	private void extractNCBOConceptsFromDocuments() {
		// Vector<Document> docs = this.gatherTrainingDocuments();
		// MSNCBOAnnotator.getConcepts("Patient has opacity in the right lower lobe");
	}

	private void generateBasiliskLexicon() {
		if (this.basilisk.getWordPatternHash().isEmpty()) {
			displayMessageDialog("Must extract Basilisk patterns first");
			return;
		}
		String str = JOptionPane.showInputDialog("Lexicon:");
		if (str != null) {
			String[] strs = str.split(" ");

			// Vector<String> expanded = this.basilisk
			// .generatedExpandedLexicon(strs);
			Lexicon l = new Lexicon(strs);
			this.basilisk.setCurrentLexicon(l);
			Vector<String> expanded = this.basilisk.gatherNewWordCandidates(l, null, 20, 5);

			System.out.println("EXPANDED LEXICON:");
			if (expanded != null) {
				for (String word : expanded) {
					System.out.println("\t" + word);
				}
			}
		}
	}

	private void extractBasiliskTypePatterns() {
		this.basilisk.clear();
		Vector<Document> docs = gatherTrainingDocuments();
		if (docs != null) {
			for (Document doc : docs) {
				for (Sentence sentence : doc.getAllSentences()) {
					this.basilisk.gatherTypeExtractionPatterns(this, sentence);
				}
			}
		}
		this.basilisk.printPatterns();
	}

	private void extractBasiliskRelationPatterns() {
		this.basilisk.clear();
		if (this.corpusDirectoryName == null) {
			this.displayMessageDialog("Corpus directory not defined in properties file");
			return;
		}
		Vector<File> files = FUtils.readFilesFromDirectory(this.corpusDirectoryName);
		if (files != null) {
			for (File file : files) {
				String text = FUtils.readFile(file);
				Document document = new Document(text, this.control.getHeaderHash());
				document.analyzeSentencesNoHeader();
				for (Sentence sentence : document.getAllSentences()) {
					this.basilisk.gatherRelationExtractionPatterns(this, sentence);
				}
			}
		}
		for (Enumeration<String> e = this.basilisk.getWordPatternHash().keys(); e.hasMoreElements();) {
			String key = e.nextElement();
			Vector<ExtractionPattern> eps = this.basilisk.getWordPatternHash().get(key);
			for (ExtractionPattern ep : eps) {
				System.out.println("Key=" + key + ",Pattern=" + ep);
			}
		}
	}

	private void convertRulesToSimpleFormat() {
		for (Rule rule : this.control.getCurrentGrammar().getAllRules()) {
			if (!(rule instanceof InferenceRule)) {
				String rstr = SimplifiedRuleFormat.convertRuleToString(rule);
				System.out.println(rstr);
			}
		}
	}

	private void extractCorpusPatientNames() {
		this.corpusPatientFileHash = new Hashtable();
		if (this.corpusDirectoryName == null) {
			displayMessageDialog("Requires \"" + corpusDirectoryPropertyName + "\" parameter.");
			return;
		}
		String delim = this.getStartupParameters().getPropertyValue("FilePatientNameDelimiter");
		if (delim == null) {
			delim = "_";
		}
		Vector<File> files = FUtils.readFilesFromDirectory(this.corpusDirectoryName);
		if (files != null) {
			for (File file : files) {
				String pname = Document.extractPatientNameFromReportName(file.getName(), delim);
				if (pname != null) {
					VUtils.pushHashVector(this.corpusPatientFileHash, pname, file.getAbsolutePath());
				}
			}
		}
	}

	public boolean corpusPatientExists(String pname) {
		return pname != null && this.corpusPatientFileHash.get(pname) != null;
	}

	private void analyzePatientFilesAndDisplayInQueryPanel() {
		String pname = JOptionPane.showInputDialog("Patient Name:");
		analyzePatientFilesAndDisplayInQueryPanel(pname);
	}

	private void analyzeAllPatientFilesAndDisplayInQueryPanel() {
		if (this.corpusTSLSentences == null) {
			if (this.getCorpusPatientNames() != null) {
				this.lastSelectedCorpusPatient = null;
				Vector<RelationSentence> allRelationSentences = null;
				for (String pname : this.getCorpusPatientNames()) {
					Vector<RelationSentence> rsents = this.extractRelationSentencesFromPatientFiles(pname);
					allRelationSentences = VUtils.append(allRelationSentences, rsents);
				}
				this.corpusTSLSentences = allRelationSentences;
				this.moonstoneQueryPanel.populateKBUsingSentences(this.corpusTSLSentences);
				JOptionPane.showMessageDialog(new JFrame(), "Done");
			}
		}
	}

	public void analyzePatientFilesAndDisplayInQueryPanel(String pname) {
		if (this.lastSelectedCorpusPatient == pname && this.corpusTSLSentences != null) {
			JOptionPane.showMessageDialog(new JFrame(), "Patient files already analyzed");
			return;
		}
		this.lastSelectedCorpusPatient = pname;
		this.corpusTSLSentences = this.extractRelationSentencesFromPatientFiles(pname);
		this.moonstoneQueryPanel.populateKBUsingSentences(this.corpusTSLSentences);
		JOptionPane.showMessageDialog(new JFrame(), "Done");
	}

	// 10/26/2015: Pass in patient name, get back TSL sentences, store in KB,
	public Vector<RelationSentence> extractRelationSentencesFromPatientFiles(String pname) {
		Vector<RelationSentence> allRelationSentences = null;
		if (pname.equals(this.lastSelectedCorpusPatient) && this.corpusTSLSentences != null) {
			return this.corpusTSLSentences;
		}
		Vector<String> fnames = this.corpusPatientFileHash.get(pname);
		if (fnames != null) {
			for (String fname : fnames) {
				File file = new File(fname);
				if (file.exists()) {
					System.out.println("About to process: " + fname);
					String text = FUtils.readFile(file);
					Document doc = new Document(fname, text);
					this.control.applyNarrativeGrammarToText(doc, true, true, true);
					Vector<Annotation> annotations = VUtils.append(
							this.control.getSentenceGrammar().getDisplayedAnnotations(),
							this.control.getDocumentGrammar().getDisplayedAnnotations());
					annotations = Annotation.getTargetAnnotations(annotations);
					if (annotations != null) {
						ObjectConstant noc = new ObjectConstant(pname);
						RelationSentence prs = new RelationSentence("patient", noc);
						allRelationSentences = VUtils.add(allRelationSentences, prs);
						Vector<RelationSentence> rsents = this.control
								.getNonConceptDuplicatedRelationSentences(annotations);
						allRelationSentences = VUtils.append(allRelationSentences, rsents);
					}
				}
			}
		}
		return allRelationSentences;
	}

	private void chooseRuleFile() {
		File file = FUtils.chooseFile(new File(this.topGrammarDirectoryPathName), "Choose Rule File");
		if (file != null && file.exists() && file.isFile()) {
			this.ruleFilePath = file.getName();
			this.ruleFileNameTextField.setText(this.ruleFilePath);
			findRulesByFile();
		}
	}

	private void deleteRule(boolean doquery) {
		boolean doremove = true;
		if (this.rule == null) {
			this.displayMessageDialog("No rule currently selected.");
			return;
		}
		if (doquery) {
			int answer = JOptionPane.showConfirmDialog(new JFrame(), "Delete \"" + this.rule.getRuleID() + "\"?");
			doremove = (answer == JOptionPane.YES_OPTION);
		}
		if (doremove) {
			this.control.getCurrentGrammar().removeRule(this.rule);
			this.clearRuleInformation();
		}
	}

	private void assignRuleFilePath() {
		String str = this.ruleFileNameTextField.getText();
		if (str != null) {
			String fname = this.selectedGrammarDirectoryName + File.separator + str;
			File file = new File(fname);
			if (!file.exists()) {
				this.displayMessageDialog("File \"" + fname + "\" does not exist.");
			} else {
				this.ruleFilePath = str;
			}
		}
	}

	public Vector<Annotation> applyNarrativeGrammarToText(String fname, String text, boolean removeNested,
			boolean removeCoinciding, boolean interpretedOnly) {
		this.selectedGrammarRule = null;
		Document doc = new Document(fname, text);
		Vector<Annotation> annotations = this.control.applyNarrativeGrammarToText(doc, removeNested, removeCoinciding,
				interpretedOnly);
		doc.releaseText();
		return annotations;
	}

	private void applyInferenceToText() {
		String text = this.documentPanel.getText();
		Vector<Annotation> annotations = applyInferenceToText(text);
		this.control.getSentenceGrammar().setDisplayedAnnotations(annotations);
		repopulateJTree();
	}

	public Vector<Annotation> applyInferenceToText(String text) {
		Vector<Annotation> annotations = null;
		// this.setSemanticAnnotations(null);
		// this.setStructureAnnotations(null);
		if (text != null) {
			Document doc = new Document(text, this.control.getHeaderHash());
			doc.analyzeContent(this.isUseSectionHeaderHeuristics());
			if (doc.getAllSentences() != null) {
				for (Sentence s : doc.getAllSentences()) {
					Vector<Annotation> v = this.control.applyInferenceRules(s);
					annotations = VUtils.append(annotations, v);
				}
			}
		}
		return annotations;
	}

	private void displaySelectedRule() {
		String ruleid = (String) this.ruleIDCB.getSelectedItem();
		Rule rule = this.control.getCurrentGrammar().getRuleByID(ruleid);
		displaySelectedRule(rule);
	}

	private void displaySelectedRule(Rule rule) {
		this.ruleDefinitionTextPane.setText("");
		if (rule != null && rule.getSourceFilePath() != null) {
			File file = new File(rule.getSourceFilePath());
			if (file.exists()) {
				this.ruleID = (String) rule.getRuleID();
				this.rule = rule;
				this.ruleType = this.rule.getRuleType();
				this.ruleTypeCB.setSelectedItem(this.ruleType);
				this.ruleFilePath = this.rule.getSourceFilePath();
				this.ruleFileNameTextField.setText(file.getName());
				if (this.rule.getSexp() != null) {
					String sstr = this.rule.getSexp().toNewlinedString(0);
					this.ruleDefinitionTextPane.setText(sstr);
				}
			}
		}
	}

	private void findRulesByFile() {
		this.ruleIDCB.removeAllItems();
		this.ruleTokenTextField.setText("");
		if (this.ruleFilePath != null && this.control.getCurrentGrammar().getAllRules() != null) {
			Vector<Rule> rules = null;
			for (Rule rule : this.control.getCurrentGrammar().getAllRules()) {
				if (this.ruleFilePath.equals(rule.getSourceFilePath())) {
					rules = VUtils.add(rules, rule);
				}
			}
			if (rules != null) {
				Vector<String> rids = Grammar.getRuleIDs(rules);
				if (rids != null) {
					for (String rid : rids) {
						this.ruleIDCB.addItem(rid);
					}
				}
			}
		}
	}

	private void findRulesContainingConcept() {
		this.ruleIDCB.removeAllItems();
		this.ruleTokenTextField.setText("");
		String concept = (String) this.ruleConceptCB.getSelectedItem();
		Vector<Rule> rules = null;
		if (concept != null && this.control.getCurrentGrammar().getAllRules() != null) {
			String cui = AnnotationIntegrationMaps.getCUI(concept);
			for (Rule rule : this.control.getCurrentGrammar().getAllRules()) {
				if (concept.equals(rule.getResultConcept()) || (cui != null && cui.equals(rule.getResultCUI()))) {
					rules = VUtils.add(rules, rule);
				}
			}
		}
		if (rules != null) {
			Vector<String> rids = Grammar.getRuleIDs(rules);
			if (rids != null) {
				for (String rid : rids) {
					this.ruleIDCB.addItem(rid);
				}
			}
		}
	}

	private void findRulesByName() {
		this.ruleIDCB.removeAllItems();
		this.ruleTokenTextField.setText("");
		String str = this.ruleNameTextField.getText();
		if (str != null) {
			Vector<Rule> rules = this.getRulesMatchingName(str);
			Vector<String> rids = Grammar.getRuleIDs(rules);
			if (rids != null) {
				for (String rid : rids) {
					this.ruleIDCB.addItem(rid);
				}
			}
		}
	}

	private void findRulesByToken() {
		this.ruleIDCB.removeAllItems();
		this.ruleNameTextField.setText("");
		String str = this.ruleTokenTextField.getText();
		if (str != null) {
			Vector<Rule> rules = this.getRulesMatchingToken(str);
			Vector<String> rids = Grammar.getRuleIDs(rules);
			if (rids != null) {
				for (String rid : rids) {
					this.ruleIDCB.addItem(rid);
				}
			}
		}
	}

	private void createRule() {
		String template = "(\n\n)";
		clearRuleInformation();
		if ("wordrule".equals(this.ruleType)) {
			template = WordRuleTemplate.toNewlinedString(0);
		} else if ("measurementrule".equals(this.ruleType)) {
			template = MeasurementRuleTemplate.toNewlinedString(0);
		} else if ("tagrule".equals(this.ruleType)) {
			template = TagRuleTemplate.toNewlinedString(0);
		} else if ("labelrule".equals(this.ruleType)) {
			template = LabelRuleTemplate.toNewlinedString(0);
		} else if ("inferencerule".equals(this.ruleType)) {
			template = InferenceRuleTemplate.toNewlinedString(0);
		}
		this.ruleDefinitionTextPane.setText(template);
	}

	private void clearRuleInformation() {
		this.rule = null;
		this.ruleTokenTextField.setText("");
		this.ruleNameTextField.setText("");
		this.ruleDefinitionTextPane.setText("");
	}

	private void storeRulesToSelectedFile() {
		storeRules(this.ruleType, this.ruleFilePath);
	}

	private void storeRules(String type, String fpath) {
		if (type != null && fpath != null) {
			Vector<Rule> rules = this.control.getCurrentGrammar().getRulesFromFile(fpath, type);
			Grammar.storeRules(rules, fpath, type);
		}
	}

	// Before 5/31/2016
	// private void storeRules(String type, String fname) {
	// if (type != null && fname != null) {
	// Vector<Rule> rules =
	// this.control.getCurrentGrammar().getRulesFromFile(fname, type);
	// String fpath = this.getRuleFilePath();
	// if (rules != null) {
	// this.control.getCurrentGrammar().storeRules(rules);
	// } else if (fpath != null) {
	// DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	// Date date = new Date();
	//
	// String dateString = dateFormat.format(date);
	// FUtils.writeFile(fpath, "'(\n\n\t;; Automatically written: " + dateString
	// + "\n)\n");
	// }
	// }
	// }
	private void instantiateNewRule() {
		String rstr = this.ruleDefinitionTextPane.getText();
		if (rstr == null) {
			this.displayMessageDialog("Missing rule definition");
			return;
		}
		if (this.ruleFilePath == null) {
			this.displayMessageDialog("Need to specify file path");
			return;
		}
		if (rstr.charAt(0) != '\'') {
			rstr = "'" + rstr;
		}
		String vstr = this.control.getCurrentGrammar().validateRuleString(rstr, this.ruleType);
		if (vstr != null) {
			this.displayMessageDialog(vstr);
			return;
		}
		if (this.rule != null) {
			String rid = Rule.getRuleIdFromString(rstr);
			if (this.rule.getRuleID().equals(rid)) {
				String msg = "Rule \"" + this.rule.getRuleID() + "\" already exists.  Overwrite?";
				if (!getUserConfirmation(msg)) {
					return;
				}
				this.control.getCurrentGrammar().removeRule(this.rule);
				this.rule = null;
			}
		}
		this.rule = this.control.getCurrentGrammar().createRule(rstr, this.ruleType, this.ruleFilePath);
	}

	public Rule instantiateNewRule(String rstr, String rtype, String rfile) {
		Rule rule = null;
		if (rstr.charAt(0) != '\'') {
			rstr = "'" + rstr;
		}
		String vstr = this.control.getCurrentGrammar().validateRuleString(rstr, this.ruleType);
		if (vstr == null) {
			rule = this.control.getCurrentGrammar().createRule(rstr, rtype, rfile);
		} else {
			System.out.println("Invalid Rule String (" + vstr + ") + \"" + rstr + "\"");
		}
		return rule;
	}

	public void repopulateJTree() {
		repopulateJTree(false);
	}

	public void repopulateJTree(boolean expand) {
		Vector<Annotation> annotations = this.control.getCurrentGrammar().getDisplayedAnnotations();
		this.displayedAnnotations = null;
		this.rootNode = new AnnotationRuleMutableTreeNode(this, null);
		if (annotations != null) {

			// Collections.sort(annotations, new Annotation.NumericIDSorter());

			Collections.sort(annotations, new Annotation.TextStartSorter());
			Collections.sort(annotations, new Annotation.ConceptCommaSorter());
			// Collections.sort(annotations, new Annotation.GoodnessSorter());
			try {
				// Collections.sort(annotations, new Annotation.TextLengthSorter());
			} catch (Exception e) {
				// e.printStackTrace();
			}
			// Collections.sort(annotations, new Annotation.TextStartSorter());
			for (Annotation annotation : annotations) {
				if (this.selectedGrammarRule == null
						|| this.selectedGrammarRule.matchesSourceRule(annotation.getRule())) {
					populateJTree(this.rootNode, annotation, true);
				}
			}
		}
		if (this.model != null) {
			this.model.setRoot(this.rootNode);
			this.model.reload();
		}

		// For later...
		// ArrayList<MoonstoneAnnotation> msv = getAPIAnnotations();
		if (expand) {
			for (int i = 0; i < this.annotationRuleJTree.getRowCount(); i++) {
				this.annotationRuleJTree.expandRow(i);
			}
		}
	}

	public void repopulateJTree_BEFORE_12_16_2017(boolean expand) {
		Vector<Annotation> annotations = this.control.getCurrentGrammar().getDisplayedAnnotations();
		this.displayedAnnotations = null;
		this.rootNode = new AnnotationRuleMutableTreeNode(this, null);
		if (annotations != null) {
			// Collections.sort(annotations, new Annotation.GoodnessSorter());
			try {
				Collections.sort(annotations, new Annotation.TextLengthSorter());
			} catch (Exception e) {
				// e.printStackTrace();
			}
			Collections.sort(annotations, new Annotation.TextStartSorter());
			for (Annotation annotation : annotations) {
				populateJTree(this.rootNode, annotation, true);
			}
		}
		if (this.model != null) {
			this.model.setRoot(this.rootNode);
			this.model.reload();
		}

		// For later...
		// ArrayList<MoonstoneAnnotation> msv = getAPIAnnotations();
		if (expand) {
			for (int i = 0; i < this.annotationRuleJTree.getRowCount(); i++) {
				this.annotationRuleJTree.expandRow(i);
			}
		}
	}

	public ArrayList<MoonstoneAnnotation> getAPIAnnotations() {
		return MoonstoneAnnotation.wrapMoonstoneAnnotations(this.displayedAnnotations);
	}

	private void populateJTree(AnnotationRuleMutableTreeNode pnode, Annotation annotation, boolean atRootLevel) {
		boolean doitanyway = true;

		// 8/12/2015
		if (!annotation.isInterpreted() && !doitanyway) {
			return;
		}

		if (annotation instanceof MissingAnnotation) {
			return;
		}

		// 6/15/2015
		if (this.displayTargetConceptAnnotationsOnly && atRootLevel && annotation != null
				&& !annotation.containsTargetConcept()) {
			return;
		}

		if (annotation != null) {
			this.displayedAnnotations = VUtils.add(this.displayedAnnotations, annotation);
		}
		if (doitanyway || annotation.getRule() != null || annotation.isInterpreted()) {
			AnnotationRuleMutableTreeNode cnode = new AnnotationRuleMutableTreeNode(this, annotation);
			if (pnode != null) {
				if (annotation.getGoodness() > 0f) {
					model.insertNodeInto(cnode, pnode, pnode.getChildCount());
				}
			}
			if (annotation.getSourceAnnotations() != null) {
				for (Annotation child : annotation.getSourceAnnotations()) {
					populateJTree(cnode, child, false);
				}
			}
		}
	}

	public Vector<Rule> getRulesMatchingName(String str) {
		Vector<Rule> rules = null;
		if (str != null && rules == null && this.control.getCurrentGrammar().getAllRules() != null) {
			str = str.toLowerCase();
			for (Rule rule : this.control.getCurrentGrammar().getAllRules()) {
				if (rule.getRuleID() != null && rule.getRuleID().toLowerCase().contains(str)) {
					rules = VUtils.add(rules, rule);
				}
			}
		}
		return rules;
	}

	public Vector<Rule> getRulesMatchingToken(String str) {
		Vector<Rule> rules = null;
		if (str != null && rules == null && this.control.getCurrentGrammar().getAllRules() != null) {
			str = str.toLowerCase();
			for (Rule rule : this.control.getCurrentGrammar().getAllRules()) {
				if (rule.getPatternLists() != null) {
					boolean matched = false;
					for (Vector<String> wlst : rule.getPatternLists()) {
						for (String wstr : wlst) {
							if (str.contains(wstr.toLowerCase())) {
								rules = VUtils.add(rules, rule);
								matched = true;
								break;
							}
						}
						if (matched) {
							break;
						}
					}
				}
			}
		}
		return rules;
	}

	class AnnotationRuleMutableTreeNode extends DefaultMutableTreeNode {

		MoonstoneRuleInterface ruleEditor = null;
		Annotation annotation = null;

		AnnotationRuleMutableTreeNode(MoonstoneRuleInterface editor, Annotation annotation) {
			super(annotation);
			this.ruleEditor = editor;
			this.annotation = annotation;
			if (annotation != null) {
				annotation.setUserObject(this);
			}
		}

		public String toString() {
			String str = "";
			if (annotation != null) {
				str += "\"" + annotation.getText() + "\"";
				String typestr = this.annotation.getTypeConceptDisplayString();
				if (typestr != null) {
					str += typestr;
				}
				str += "[" + annotation.getAnnotationID() + "]";

				/// TEMPORARILY REMOVED 4/8/2017 FOR BETTER SNAPSHOTS FOR
				/// SALOMEH
				// if (annotation.getRule() != null) {
				// str += "(" + annotation.getRule().getRuleID() + ")";
				// }
				// String gstr =
				// Annotation.getShortenedPercentString(annotation.getGoodness(),
				// 4);
				// str += "[" + gstr + "]";
			} else {
				str = "                                                    ";
			}
			return str;
		}
	}

	class AnnotationRuleDefaultTreeModel extends DefaultTreeModel {

		AnnotationRuleDefaultTreeModel(AnnotationRuleMutableTreeNode node) {
			super(node);
		}

		public void valueForPathChanged(TreePath path, Object newValue) {
			AnnotationRuleMutableTreeNode pathnode = (AnnotationRuleMutableTreeNode) path.getLastPathComponent();
		}
	}

	class AnnotationRuleJTree extends JTree {

		public static final long serialVersionUID = 0;

		AnnotationRuleJTree(DefaultTreeModel model) {
			super(model);
			this.setCellEditor(cellEditor);
		}

	}

	private AnnotationRuleMutableTreeNode lastNode = null;

	private class MoonstoneRuleTreeCellRenderer extends DefaultTreeCellRenderer {

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			AnnotationRuleMutableTreeNode node = (AnnotationRuleMutableTreeNode) value;
			if (node.getUserObject() instanceof Annotation) {
				Annotation annotation = (Annotation) node.getUserObject();
				setToolTipText(annotation.toHTML());
			}
			return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		}
	}

	class AnnotationRuleJTreeModelListener implements TreeModelListener {

		public void treeNodesChanged(TreeModelEvent e) {
			AnnotationRuleMutableTreeNode node = (AnnotationRuleMutableTreeNode) (e.getTreePath()
					.getLastPathComponent());
			try {
				int index = e.getChildIndices()[0];
				node = (AnnotationRuleMutableTreeNode) (node.getChildAt(index));
			} catch (NullPointerException exc) {
			}
		}

		public void treeNodesInserted(TreeModelEvent e) {
		}

		public void treeNodesRemoved(TreeModelEvent e) {
		}

		public void treeStructureChanged(TreeModelEvent e) {
		}
	}

	protected static JMenuBar createMenuBar(String[][] menuinfo, Object[][] menuiteminfo, ActionListener listener,
			JComponent component) {
		Hashtable menuhash = new Hashtable();
		JMenuBar menubar = new JMenuBar();

		for (int i = 0; i < menuinfo.length; i++) {
			String[] array = (String[]) menuinfo[i];
			String parentname = array[0];
			String menuname = array[1];
			String displayname = array[2];
			JMenu menu = new JMenu(displayname);
			menuhash.put(menuname, menu);
			if (parentname != null) {
				JMenu parent = (JMenu) menuhash.get(parentname);
				parent.add(menu);
			} else {
				menubar.add(menu);
			}
		}

		for (int i = 0; i < menuiteminfo.length; i++) {
			Object[] array = (Object[]) menuiteminfo[i];
			String menuname = (String) array[0];
			String actionname = (String) array[1];
			String displayname = (String) array[2];
			int key = -1;
			int modifier = -1;
			if (array.length > 3) {
				Integer k = (Integer) array[3];
				key = k.intValue();
				Integer m = (Integer) array[4];
				modifier = m.intValue();
			}
			JMenu menu = (JMenu) menuhash.get(menuname);
			JMenuItem menuitem = new JMenuItem(displayname);
			menuitem.setActionCommand(actionname);
			menuitem.addActionListener(listener);
			if (modifier > 0) {
				KeyStroke ks = KeyStroke.getKeyStroke(key, modifier);
				menuitem.setAccelerator(ks);
			}
			try {
				menu.add(menuitem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return menubar;
	}

	public GrammarModule getControl() {
		return control;
	}

	public void keyReleased(KeyEvent e) {

	}

	public void keyPressed(KeyEvent e) {

	}

	public void keyTyped(KeyEvent e) {

	}

	private void populateRuleTokenHash() {
		if (this.control.getCurrentGrammar().getAllRules() != null) {
			for (Rule rule : this.control.getCurrentGrammar().getAllRules()) {
				if (rule.getPatternLists() != null) {
					for (Vector<String> wlist : rule.getPatternLists()) {
						for (Object token : wlist) {
							VUtils.pushHashVector(this.ruleTokenHash, token, rule);
							VUtils.pushHashVector(this.tokenRuleHash, rule, token);
						}
					}
				}
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (this.selectedNode != null && this.selectedNode.annotation != null
				&& this.selectedNode.annotation.getRule() != null) {
			Rule rule = this.selectedNode.annotation.getRule();
			this.displaySelectedRule(rule);

			// 6/23/2016
			if (this.selectedAnnotation.hasConcept()) {
				System.out.println(this.selectedAnnotation.getText() + " = \""
						+ this.selectedAnnotation.getConcept().toString() + "\"");
			}
		}
	}

	public void mouseExited(MouseEvent e) {
		this.mouseEnteredSource = null;
	}

	public void mouseEntered(MouseEvent e) {
		this.mouseEnteredSource = e.getSource();
	}

	public void mousePressed(MouseEvent e) {
		this.isMouseDown = true;
	}

	public void mouseReleased(MouseEvent e) {
		this.isMouseDown = false;
	}

	public void mouseMoved(MouseEvent e) {
		try {
			if (e.isControlDown()) {
				if (!processAnnotationRuleJTreeMouseDown(e)) {
					processDocumentPanelMouseDown(e);
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private boolean processDocumentPanelMouseDown(MouseEvent e) {
		int offset = this.documentPanel.viewToModel(e.getPoint());
		// System.out.println("Offset=" + offset);
		if (offset > 0 && this.displayedAnnotations != null) {
			for (Annotation annotation : this.displayedAnnotations) {
				if (annotation.getTextStart() <= offset && offset <= annotation.getTextEnd()) {
					selectAnnotation(annotation, false);
					return true;
				}
				if (annotation.getTextStart() > offset) {
					return false;
				}
			}
		}
		return false;
	}

	private boolean processAnnotationRuleJTreeMouseDown(MouseEvent e) {
		try {
			TreePath path = annotationRuleJTree.getPathForLocation(e.getX(), e.getY());
			if (path != null && path != this.lastTreePath) {
				this.lastTreePath = path;
				if (annotationRuleJTree.isPathSelected(path)) {
					annotationRuleJTree.removeSelectionPath(path);
				} else {
					Object lastElement = path.getLastPathComponent();
					this.selectedNode = (AnnotationRuleMutableTreeNode) lastElement;
					annotationRuleJTree.addSelectionPath(path);
					processValueSelection(this.selectedNode.getUserObject());
					return true;
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return false;
	}

	public void mouseDragged(MouseEvent e) {
	}

	private Object lastSelectedItem = null;
	private static boolean DoingUserSelection = false;

	void processValueSelection(Object o) throws Exception {
		DoingUserSelection = false;
		if (!DoingUserSelection && o != null && (lastSelectedItem == null || !lastSelectedItem.equals(o))) {
			lastSelectedItem = o;
			DoingUserSelection = true;
		}
	}

	public void displayMessageDialog(String msg) {
		JOptionPane.showMessageDialog(new JFrame(), msg);
	}

	public static boolean getUserConfirmation(String msg) {
		int answer = JOptionPane.showConfirmDialog(new JFrame(), msg);
		return answer == JOptionPane.YES_OPTION;
	}

	// private String getRuleFilePath() {
	// if (this.grammarRuleDirectoryName != null && this.ruleFileName != null) {
	// String fname = this.grammarRuleDirectoryName + File.separatorChar +
	// this.ruleFileName;
	// return fname;
	// }
	// return null;
	// }
	public WBGUI getWorkbench() {
		return this.wbgui;
	}

	public void setWorkbench(WBGUI wbgui) {
		this.wbgui = wbgui;
	}

	public Rule getTentativeRule(Vector<Vector> wlist) {
		Rule rule = null;
		if (wlist != null) {
			rule = this.tentativeRuleHash.get(wlist);
		}
		return rule;
	}

	public void addTentativeRule(Rule rule) {
		if (rule != null) {
			Rule existing = getTentativeRule(rule.getPatternLists());
			if (existing == null) {
				this.tentativeRuleHash.put(rule.getPatternLists(), rule);
			}
		}
	}

	public void storeTentativeRules() {
		if (!this.tentativeRuleHash.isEmpty()) {
			Vector<Rule> rules = HUtils.getElements(this.tentativeRuleHash);
			String fname = this.selectedGrammarDirectoryName + File.separatorChar + tentativeRuleFileName;
			Grammar.storeRules(rules, fname, "wordrule");
			this.tentativeRuleHash.clear();
		}
	}

	public TSLGUI getTSLGUI() {
		return this.tslGUI;
	}

	public KnowledgeEngine getKnowledgeEngine() {
		return knowledgeEngine;
	}

	public Vector<Annotation> getDisplayedAnnotations() {
		return displayedAnnotations;
	}

	public String getCorpusDirectoryName() {
		return this.corpusDirectoryName;
	}

	public String getResultsDirectoryName() {
		return resultsDirectoryName;
	}

	public String getResourceDirectoryName() {
		return resourceDirectoryName;
	}

	public String getTopGrammarDiretoryPathName() {
		return this.topGrammarDirectoryPathName;
	}

	public Hashtable<Vector<Vector>, Rule> getTentativeRuleHash() {
		return tentativeRuleHash;
	}

	public void setTextToAnalyze(String text) {
		this.documentPanel.setText(text);
	}

	public boolean isUserInteractionThreadWait() {
		return userInteractionThreadWait;
	}

	public void setUserInteractionThreadWait(boolean userInteractionThreadWait) {
		this.userInteractionThreadWait = userInteractionThreadWait;
	}

	public Vector<Document> gatherTrainingDocuments() {
		Vector<Document> documents = null;
		Vector<File> files = FUtils.readFilesFromDirectory(this.trainingDirectoryName);
		if (this.trainingDirectoryName == null || files == null) {
			displayMessageDialog("Training directory \"" + this.trainingDirectoryName + "\" nonexistent or empty.");
		}
		if (files != null) {
			for (File file : files) {
				String text = FUtils.readFile(file);
				text = text.toLowerCase();
				Document document = new Document(text, this.control.getHeaderHash());
				document.analyzeContent(this.isUseSectionHeaderHeuristics());
				documents = VUtils.add(documents, document);
			}
		}
		return documents;
	}

	public boolean isMouseInDocumentPanel() {
		return this.mouseEnteredSource == this.documentPanel;
	}

	public boolean isMouseInAnnotationTreePanel() {
		return this.mouseEnteredSource == this.annotationRuleJTree;
	}

	public void resetTitle() {
		if (this.frame == null) {
			return;
		}
		String title = "";
		if (this.control.getCurrentGrammar() != null) {
			title += "Grammar=" + this.control.getCurrentGrammar().getName();
		}
		title += ",UseIndexFinder=" + this.isUseIndexFinder();
		// title += ",RelevantHeadersOnly=" +
		// this.displayRelevantSectionHeadersOnly;
		// title += ",TargetOnly=" + this.displayTargetConceptAnnotationsOnly;
		String dname = "*";
		if (this.displayedDocument != null) {
			dname = this.displayedDocument.getName();
		}
		title += ",Document=" + dname;
		// title += ",UseStructureGrammar=" +
		// this.control.isUseStructureGrammar();
		if (this.annotationAttachments.size() > 0) {
			title += "[Attachments:";
			for (int i = 0; i < this.annotationAttachments.size(); i++) {
				int[] attachment = this.annotationAttachments.elementAt(i);
				title += attachment[0] + "-" + attachment[1];
				if (i < this.annotationAttachments.size() - 1) {
					title += ",";
				}
			}
			title += "]";
		}
		if (this.grammarEBL != null && this.grammarEBL.getEBLGrammarRules() != null) {
			title += ",*EBLRules*";
		}
		if (this.getLpcfg().isUseRuleConditionalsOnlyInProbabilityCalculation()) {
			title += ",LCPFG=RulesOnly";
		} else {
			title += ",LCPFG=Rules+Concepts";
		}
		if (this.isCompareAnnotationsTargetsAndGoodness()) {
			title += ",Decision=Targets+Goodness";
		} else {
			title += ",Decision=GoodnessOnly";
		}
		this.frame.setTitle(title);
	}

	public Vector<tsl.expression.term.relation.RelationSentence> getCorpusTSLSentences() {
		return corpusTSLSentences;
	}

	public boolean isUseSectionHeaderHeuristics() {
		return useSectionHeaderHeuristics;
	}

	public Vector<String> getCorpusPatientNames() {
		if (this.corpusPatientFileHash != null) {
			Vector<String> pnames = HUtils.getKeys(this.corpusPatientFileHash);
			Collections.sort(pnames);
			return pnames;
		}
		return null;
	}

	public Vector<String> getCorpusPatientFilePaths(String pname) {
		return this.corpusPatientFileHash.get(pname);
	}

	public void toggleDisplayTargetConceptAnnotationsOnly() {
		this.displayTargetConceptAnnotationsOnly = !this.displayTargetConceptAnnotationsOnly;
		this.resetTitle();
	}

	public JTextArea getDocumentPanel() {
		return documentPanel;
	}

	public Document getDisplayedDocument() {
		return displayedDocument;
	}

	public void setDisplayedDocument(Document displayedDocument) {
		this.displayedDocument = displayedDocument;
		if (displayedDocument != null) {
			this.documentPanel.setText(displayedDocument.getText());
			this.resetTitle();
		}
	}

	public boolean isDisplayRelevantSectionHeadersOnly() {
		return displayRelevantSectionHeadersOnly;
	}

	public MoonstoneQueryPanel getMoonstoneQueryPanel() {
		return moonstoneQueryPanel;
	}

	public String getLastSelectedCorpusPatient() {
		return lastSelectedCorpusPatient;
	}

	public StartupParameters getStartupParameters() {
		return startupParameters;
	}

	// 7/3/2015: Used to support indirect MoonstoneGUI access to
	// control model etc.
	public Grammar getCurrentGrammar() {
		return this.control.getCurrentGrammar();
	}

	public Grammar getGrammar(String name) {
		return this.control.getGrammar(name);
	}

	public void setCurrentGrammar(Grammar grammar) {
		this.control.setCurrentGrammar(grammar);
	}

	public Grammar getSentenceGrammar() {
		return getGrammar(Grammar.SentenceGrammarName);
	}

	public Grammar getStructureGrammar() {
		return getGrammar(Grammar.StructureGrammarName);
	}

	public Grammar getDocumentGrammar() {
		return getGrammar(Grammar.DocumentGrammarName);
	}

	public void toggleUseStructureGrammar() {
		this.control.toggleUseStructureGrammar();
	}

	public Vector<RelationSentence> getNonConceptDuplicatedRelationSentences(Vector<Annotation> annotations) {
		return this.control.getNonConceptDuplicatedRelationSentences(annotations);
	}

	public Vector<Rule> getAutomaticallyGeneratedRules() {
		return automaticallyGeneratedRules;
	}

	public void clearAutomaticallyGeneratedRules() {
		this.automaticallyGeneratedRules = null;
	}

	public boolean isUseForwardChainingInferenceInPatternAnalysis() {
		return useForwardChainingInferenceInPatternAnalysis;
	}

	public Readmission getReadmission() {
		return readmission;
	}

	public boolean isConjoinHeaderWithSentence() {
		return conjoinHeaderWithSentence;
	}

	public ForwardChainingInferenceEngine getForwardChainingInferenceEngine() {
		if (this.moonstoneQueryPanel != null) {
			return this.moonstoneQueryPanel.getForwardChainingInferenceEngine();
		}
		return null;
	}

	public String getDocumentPanelText() {
		return this.documentPanel.getText();
	}

	// FOR VINCI
	public LPCFG getLpcfg() {
		if (this.lpcfg == null) {
			this.lpcfg = new LPCFG(this);
		}
		return lpcfg;
	}

	public void addAnnotationAttachment() {
		if (this.selectedAnnotation != null && this.lastSelectedAnnotation != null) {
			int start1 = this.selectedAnnotation.getTextStart();
			int start2 = this.lastSelectedAnnotation.getTextStart();
			int end1 = this.selectedAnnotation.getTextEnd();
			int end2 = this.lastSelectedAnnotation.getTextEnd();
			int lowest = Math.min(start1, start2);
			int highest = Math.max(end1, end2);
			int[] pair = new int[] { lowest, highest };
			this.annotationAttachments.add(pair);
			this.resetTitle();
		}
	}

	public Vector<int[]> getAnnotationAttachments() {
		return this.annotationAttachments;
	}

	public void clearAnnotationAttachments() {
		this.annotationAttachments = new Vector(0);
		this.resetTitle();
	}

	public double getAnnotationProbabilityCutoff() {
		return annotationProbabilityCutoff;
	}

	public void setAnnotationProbabilityCutoff(double annotationProbabilityCutoff) {
		this.annotationProbabilityCutoff = annotationProbabilityCutoff;
	}

	public String getSelectedGrammarRuleDirectoryPathName() {
		String fstr = this.getTopGrammarDiretoryPathName() + File.separatorChar + this.selectedGrammarDirectoryName;
		File file = new File(fstr);
		if (file.exists()) {
			return file.getAbsolutePath();
		}
		return null;
	}

	public String getSelectedGrammarDirectoryName() {
		return this.selectedGrammarDirectoryName;
	}

	public Vector<String> getGrammarDirectoryNames() {
		return this.grammarDirectoryNames;
	}

	public void setSelectedGrammarDirectoryName(String selectedGrammarDirectoryName) {
		this.selectedGrammarDirectoryName = selectedGrammarDirectoryName;
	}

	public void changeGrammarDirectoryName() {
		String[] glist = VUtils.vectorToStringArray(this.grammarDirectoryNames);
		String gname = (String) JOptionPane.showInputDialog(new JFrame(), "Select Concept:", "Customized Dialog",
				JOptionPane.PLAIN_MESSAGE, null, glist, glist[0]);
		this.changeGrammarDirectoryName(gname);
	}

	public void changeGrammarDirectoryName(String gname) {
		if (gname != null) {
			this.selectedGrammarDirectoryName = gname;
			this.reloadRules(false);
		}
	}

	public boolean isUseIndexFinder() {
		return this.knowledgeEngine.getStartupParameters().isPropertyTrue(UseIndexFinder);
	}

	public void setIsUseIndexFinder(boolean value) {
		Boolean rv = new Boolean(value);
		String rstr = rv.toString().toLowerCase();
		this.knowledgeEngine.getStartupParameters().setPropertyValue(UseIndexFinder, rstr);
	}

	public void toggleUseIndexFinder() {
		boolean useIndexFinder = this.knowledgeEngine.getStartupParameters().isPropertyTrue(UseIndexFinder);
		setIsUseIndexFinder(new Boolean(!useIndexFinder));
		this.resetTitle();
	}

	public GrammarEBL getGrammarEBL() {
		return grammarEBL;
	}

	public GrammarEBLCompilerSpecialization getGrammarEBLCompilerSpecialization() {
		return this.grammarEBLCompilerSpecialization;
	}

	// 7/22/2016
	public void outputTuffyEvidence() {
		String estr = this.generateTuffyEvidence();
		if (estr != null) {
			System.out.println("TUFFY EVIDENCE:\n");
			System.out.println(estr);
		} else {
			this.displayMessageDialog("No annotations to extract Tuffy evidence from");
		}
	}

	public String generateTuffyEvidence() {
		StringBuffer sb = new StringBuffer();
		Annotation annotation = this.selectedAnnotation;
		if (annotation != null && annotation.isInterpreted()) {
			String estr = annotation.getSemanticInterpretation().getTuffyString();
			if (estr != null) {
				sb.append(estr);
			}
		}
		return sb.toString();
	}

	public boolean isUseFCIEToInferTargetConcept() {
		return true;
		// return this.useFCIEToInferTargetConcepts;
	}

	public boolean isValidTuffyRelationName(String rname) {
		return (this.tuffyRelationNames != null && this.tuffyRelationNames.contains(rname));
	}

	public void storeForGarbageCollection(Annotation annotation) {
		this.annotationsForGarbageCollection = VUtils.add(this.annotationsForGarbageCollection, annotation);
	}

	// public void storeForGarbageCollection(Vector<Annotation> annotations) {
	// this.annotationsForGarbageCollection = VUtils.append(
	// this.annotationsForGarbageCollection, annotations);
	// }

	public void releaseAnnotations() {
		if (this.annotationsForGarbageCollection != null) {
			Annotation.gcAll(this.annotationsForGarbageCollection);
		}
		this.lastSelectedAnnotation = this.selectedAnnotation = null;
		this.displayedAnnotations = this.annotationsForGarbageCollection = null;
	}

	public void toggleCompareAnnotationsTargetsAndGoodness() {
		this.compareAnnotationsTargetsAndGoodness = !this.compareAnnotationsTargetsAndGoodness;
		this.compareAnnotationsGoodnessOnly = !this.compareAnnotationsGoodnessOnly;
		this.resetTitle();
	}

	public boolean isCompareAnnotationsGoodnessOnly() {
		return compareAnnotationsGoodnessOnly;
	}

	public boolean isCompareAnnotationsTargetsAndGoodness() {
		return compareAnnotationsTargetsAndGoodness;
	}

	public moonstone.io.db.MySQLAPI getMoonstoneMySQLAPI() {
		return MoonstoneMySQLAPI;
	}

	//////////////////////////////////////////////////////////
	// 10/11/2017 -- Interfacing to IEViz prototype classes / interfaces

	public void initialize(String ontologyURL) {
		this.analyzeOWLOntology(ontologyURL, false, true);
		this.getMoonstoneMySQLAPI().storeAnnotationTypeSystem(this.getKnowledgeEngine().getCurrentOntology(),
				ontologyURL);
	}

	public List<AnnotationNLP> annotate(String docstr) {
		List<AnnotationNLP> annotations = new ArrayList<AnnotationNLP>();
		Document doc = new Document(docstr);
		this.control.applyNarrativeGrammarToText(doc, true, true, true);
		Vector<Annotation> v = this.getSentenceGrammar().getDisplayedAnnotations();
		if (v != null) {
			for (Annotation a : v) {
				AnnotationNLP annotation = new AnnotationNLP(a);
				annotations.add(annotation);
			}
		}
		return annotations;
	}

	public Modifiers getAnnotationModifiers() {
		return annotationModifiers;
	}

	public Vector<Annotation> getSelectedAnnotationsFromJTree() {
		int[] rows = annotationRuleJTree.getSelectionRows();
		Vector<Annotation> annotations = null;
		if (rows != null) {
			for (int i = 0; i < rows.length; i++) {
				int rowid = rows[i];
				TreePath path = this.annotationRuleJTree.getPathForRow(rowid);
				AnnotationRuleMutableTreeNode node = (AnnotationRuleMutableTreeNode) path.getLastPathComponent();
				Annotation annotation = (Annotation) node.getUserObject();
				annotations = VUtils.add(annotations, annotation);
			}
		}
		return annotations;
	}

	public boolean isSemanticAnnotationDisplay() {
		return isSemanticAnnotationDisplay;
	}

	public void setSemanticAnnotationDisplay(boolean isSemanticAnnotationDisplay) {
		this.isSemanticAnnotationDisplay = isSemanticAnnotationDisplay;
	}

	public boolean isPrintRuleStatistics() {
		return false;
		// return printRuleStatistics;
	}

}
