package moonstone.ontology.framenet;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import moonstone.grammar.NarrativeGrammar;
import moonstone.rule.Rule;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.ontology.Ontology;
import tsl.utilities.FUtils;
import tsl.utilities.JDomUtils;
import tsl.utilities.VUtils;

public class FramenetOntology {

	private Hashtable<String, LexicalUnit> lexicalUnitIDHash = new Hashtable();
	private Hashtable<String, Frame> frameIDHash = new Hashtable();
	protected Hashtable<String, SemType> semTypeIDHash = new Hashtable();
	private Vector<Frame> allFrames = null;
	private Vector<SemType> allSemanticTypes = null;
	private Vector<SemType> rootSemanticTypes = null;
	private Vector<LexicalUnit> allLexicalUnits = null;
	private KnowledgeBase TSLKnowledgeBase = null;
	private Ontology TSLOntology = null;
	private NarrativeGrammar grammar = null;
	private MoonstoneRuleInterface msri = null;

	public FramenetOntology(MoonstoneRuleInterface msri, String dir) {
		this.msri = msri;
		this.readFramenet(dir);
		this.extractTSLOntology();
		this.extractGrammar();
		msri.getControl().setNarrativeGrammar(this.grammar);
		this.storeGrammarRules();
		int x = 1;
	}

	public void extractGrammar() {
		this.grammar = new NarrativeGrammar(msri.getSentenceGrammar(), "FramenetGrammar");
		if (this.allLexicalUnits != null) {
			for (LexicalUnit lu : this.allLexicalUnits) {
				Rule rule = lu.extractGrammarRule();
				if (rule != null) {
					int x = 1;
				}
				this.grammar.addRule(rule);
			}
		}
		if (this.allFrames != null) {
			for (Frame frame : this.allFrames) {
				Rule rule = frame.extractGrammarRule();
				if (rule != null) {
					int x = 1;
				}
				this.grammar.addRule(rule);
			}
		}
	}
	
	public void storeGrammarRules() {
		String fname = "FramenetGrammarFile";
		String fpath = this.msri.getSelectedGrammarRuleDirectoryPathName() + File.separatorChar
				+ fname;
		StringBuffer sb = new StringBuffer();
		sb.append("'(\n\twordrule\n\n");
		for (Rule rule : this.grammar.getAllRules()) {
			sb.append(rule.getSexp().toNewlinedString() + "\n");
		}
		sb.append("\n)\n");
		FUtils.writeFile(fpath, sb.toString());
		System.out.println("Framenet Grammar rules: \n" + sb.toString());
	}

	public void extractTSLOntology() {
		TSLKnowledgeBase = new KnowledgeBase("FrameNetKnowledgeBase");
		this.TSLOntology = new Ontology("FrameNetOntology");
		this.TSLOntology.setKnowledgeBase(TSLKnowledgeBase);
		TSLKnowledgeBase.setOntology(this.TSLOntology);
		KnowledgeEngine.currentKnowledgeEngine.setCurrentOntology(this.TSLOntology);
		KnowledgeEngine.currentKnowledgeEngine.setCurrentKnowledgeBase(TSLKnowledgeBase);

		Vector<TypeConstant> roots = null;
		for (SemType st : this.allSemanticTypes) {
			st.typeConstant = TypeConstant.createTypeConstant(st.name);
			this.TSLOntology.addTypeConstant(st.typeConstant);
			if (st.getParent() == null) {
				roots = VUtils.add(roots, st.typeConstant);
			}
		}
		for (SemType st : this.allSemanticTypes) {
			if (st.parent != null) {
				st.typeConstant.addParent(st.parent.typeConstant);
			}
		}
		TypeConstant roottc = new TypeConstant("root");
		roottc.setRoot();
		this.TSLOntology.addTypeConstant(roottc);
		for (TypeConstant root : roots) {
			root.addParent(roottc);
		}
	}

	public void readFramenet(String inputDir) {
		String stfile = inputDir + File.separatorChar + "semTypes.xml";
		String fdir = inputDir + File.separatorChar + "frame";
		String ludir = inputDir + File.separatorChar + "lu";

		try {
			org.jdom.Document jdoc = new SAXBuilder().build(stfile);
			Element root = jdoc.getRootElement();
			Element stroot = JDomUtils.getElementByName(root, "semTypes");
			Vector<Element> stnodes = JDomUtils.getElementsByName(stroot, "semType");
			for (Element stnode : stnodes) {
				SemType st = new SemType(this, stnode);
				this.allSemanticTypes = VUtils.add(this.allSemanticTypes, st);
			}
			this.resolveSemanticTypes();
			File ffile = new File(fdir);
			Vector<File> lufiles = FUtils.readFilesFromDirectory(ludir);
			for (File f : lufiles) {
				LexicalUnit lu = new LexicalUnit(this, f.getAbsolutePath());
				if (lu.getId() != null) {
					System.out.println("LexicalUnit: " + lu.getName());
					this.lexicalUnitIDHash.put(lu.getId(), lu);
					this.allLexicalUnits = VUtils.add(this.allLexicalUnits, lu);
				}
			}
			Vector<File> ffiles = FUtils.readFilesFromDirectory(fdir);
			for (File f : ffiles) {
				Frame frame = new Frame(this, f.getAbsolutePath());
				if (frame.getId() != null) {
					this.frameIDHash.put(frame.getId(), frame);
					this.allFrames = VUtils.add(this.allFrames, frame);
				}
			}
			// this.resolveLexicalUnits();
			for (Frame frame : this.allFrames) {
				frame.resolveRelatedFrames();
			}
			// Collections.sort(this.allLexicalUnits, new LexicalUnit.LexicalUnitSorter());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void resolveSemanticTypes() {
		for (String id : this.semTypeIDHash.keySet()) {
			SemType st = this.semTypeIDHash.get(id);
			if (st.pid != null) {
				SemType pst = this.semTypeIDHash.get(st.pid);
				st.parent = pst;
				pst.children = VUtils.add(pst.children, st);
			}
		}
		for (SemType st : this.allSemanticTypes) {
			if (st.parent == null && st.children != null) {
				this.rootSemanticTypes = VUtils.add(this.rootSemanticTypes, st);
			}
		}
	}

	public void resolveLexicalUnits() {
		for (String id : this.lexicalUnitIDHash.keySet()) {
			LexicalUnit lu = this.lexicalUnitIDHash.get(id);
			lu.frame = this.frameIDHash.get(lu.frameID);
			if (lu.getSemTypes() != null) {
				for (SemType st : lu.getSemTypes()) {
					st.lexicalUnits = VUtils.add(st.lexicalUnits, lu);
				}
			}
		}
	}

	public SemType getSemType(String stid) {
		return this.semTypeIDHash.get(stid);
	}

	public LexicalUnit getLexicalUnit(String luid) {
		return this.lexicalUnitIDHash.get(luid);
	}

	public Frame getFrame(String fid) {
		return this.frameIDHash.get(fid);
	}

}
