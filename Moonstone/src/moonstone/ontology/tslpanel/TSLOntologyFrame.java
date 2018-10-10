package moonstone.ontology.tslpanel;

import java.awt.Component;
import java.awt.Dimension;
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
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import moonstone.rule.Rule;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.expression.term.Term;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.knowledgebase.NameSpace;
import tsl.knowledge.ontology.Ontology;
import tsl.utilities.FUtils;
import tsl.utilities.SetUtils;
import tsl.utilities.TimeUtils;
import tsl.utilities.VUtils;

public class TSLOntologyFrame extends JFrame implements TreeSelectionListener, ItemListener, ActionListener,
		KeyListener, MouseMotionListener, MouseListener {
	private static final long serialVersionUID = 1L;
	protected TypeConstantMutableTreeNode ontologyRootNode = null;
	protected TypeConstantDefaultTreeModel ontologyModel = null;
	protected TypeConstantJTree typeConstantJTree = null;
	protected TypeConstantMutableTreeNode selectedNode = null;
	protected MoonstoneRuleInterface moonstone = null;
	protected TypeConstant selectedTypeConstant = null;
	protected StringConstant selectedStringConstant = null;
	protected Rule selectedGrammarRule = null;
	protected String selectedSynonym = null;
	protected Hashtable<Term, TreePath> nodePathHash = new Hashtable();
	protected Hashtable<String, Vector<StringConstant>> synonymConstantHash = new Hashtable();
	private boolean PerformingSelectionPanelOperation = false;
	private boolean UpdatingSelections = false;
	private boolean ClickingTree = false;

	protected static TSLOntologyFrame CurrentTypeOntologyFrame = null;

	protected static String[][] menuInfo = { { null, "operations", "Operations" } };

	protected static Object[][] menuItemInfo = { { "operations", "storeOntology", "Store Ontology" },
			// { "operations", "reloadOntology", "Reload Ontology" },
			{ "operations", "addTypeConstant", "Add Type" }, { "operations", "removeTypeConstant", "Remove Type" },
			{ "operations", "addStringConstant", "Add Constant" },
			{ "operations", "removeStringConstant", "Remove Constant" }, { "operations", "addSynonym", "Add Synonym" },
			{ "operations", "removeSynonym", "Remove Synonym" }, { "operations", "collapseRows", "Collapse All Rows" },
			{ "operations", "expandRows", "Expand All Rows" }, { "operations", "doQuit", "Quit" }, };

	public TSLOntologyFrame(MoonstoneRuleInterface msri) {
		this.moonstone = msri;
		CurrentTypeOntologyFrame = this;
		this.initializeTypeTree();
		this.initComponents();
		this.setJMenuBar(createMenuBar(menuInfo, menuItemInfo, this));
	}

	public void initializeTypeTree() {
		this.nodePathHash.clear();
		this.synonymConstantHash.clear();
		TypeConstant roottc = this.moonstone.getKnowledgeEngine().getCurrentOntology().getRootType();
		this.ontologyRootNode = new TypeConstantMutableTreeNode(this, roottc);
		this.ontologyModel = new TypeConstantDefaultTreeModel(this.ontologyRootNode);
		this.typeConstantJTree = new TypeConstantJTree(this.ontologyModel);
		this.typeConstantJTree.setCellRenderer(new MoonstoneRuleTreeCellRenderer());
		this.repopulateJTree(false);
		ToolTipManager.sharedInstance().registerComponent(this.typeConstantJTree);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

		this.typeConstantJTree.setEditable(false);

		this.typeConstantJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		this.typeConstantJTree.setShowsRootHandles(true);
		this.typeConstantJTree.addTreeSelectionListener(this);
		this.typeConstantJTree.addMouseMotionListener(this);
		this.typeConstantJTree.addMouseListener(this);
	}

	public void repopulateJTree(boolean expand) {
		clearHashTables();
		TypeConstant root = this.moonstone.getKnowledgeEngine().getCurrentOntology().getRootType();
		this.ontologyRootNode = new TypeConstantMutableTreeNode(this, null);

		populateJTree(this.ontologyRootNode, root, true);
		if (this.ontologyModel != null) {
			this.ontologyModel.setRoot(this.ontologyRootNode);
			this.ontologyModel.reload();
		}
		// this.collapseOrExpandRows(expand);
	}

	private void populateJTree(TypeConstantMutableTreeNode pnode, Term term, boolean atRootLevel) {
		if (term instanceof StringConstant && ((StringConstant) term).isComplex()) {
			return;
		}
		TypeConstantMutableTreeNode cnode = new TypeConstantMutableTreeNode(this, term);

		if (pnode != null) {
			this.ontologyModel.insertNodeInto(cnode, pnode, pnode.getChildCount());
		}
		if (term instanceof TypeConstant) {
			TypeConstant tc = (TypeConstant) term;
			if (!tc.isRoot() && tc.getTypedStringConstants() != null) {
				Vector<StringConstant> scv = tc.getTypedStringConstants();
				Collections.sort(scv, new Term.LabelSorter());
				for (StringConstant sc : scv) {
					if (sc.getSynonyms() != null) {
						for (String syn : sc.getSynonyms()) {
							VUtils.pushIfNotHashVector(this.synonymConstantHash, syn, sc);
						}
					}
				}
				for (Term cterm : scv) {
					populateJTree(cnode, cterm, false);
				}
			}
			if (tc.getChildren() != null) {
				Vector<Term> children = tc.getChildren();
				Collections.sort(children, new Term.LabelSorter());
				for (Term cterm : children) {
					populateJTree(cnode, cterm, false);
				}
			}
		}
		TreeNode[] nodes = (TreeNode[]) this.ontologyModel.getPathToRoot(cnode);
		TreePath tpath = new TreePath(nodes);
		if (this.nodePathHash.get(term) == null) {
			this.nodePathHash.put(term, tpath);
		}
	}
	
	private Vector getSelectedTreeNodeValues() {
		TreePath[] paths = this.typeConstantJTree.getSelectionPaths();
		Vector rv = null;
		if (paths != null && paths.length > 0) {
			for (TreePath path : paths) {
				Object o = ((TypeConstantMutableTreeNode) path.getLastPathComponent()).getUserObject();
				rv = VUtils.add(rv, o);
			}
		}
		return rv;
	}
	
	private class TypeConstantMutableTreeNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = 1L;
		TSLOntologyFrame panel = null;
		Term term = null;

		TypeConstantMutableTreeNode(TSLOntologyFrame panel, Term t) {
			super(t);
			this.panel = panel;
			this.term = t;
			if (term != null) {
				term.setUserObject(this);
			}
		}

		public String toString() {
			Term term = (Term) this.getUserObject();
			if (term != null) {
				String str = term.toString();
				if (term.getParents() != null) {
					str += " (";
					for (int i = 0; i < term.getParents().size(); i++) {
						Term parent = term.getParents().elementAt(i);
						str += parent.toString();
						if (i < term.getParents().size() - 1) {
							str += ",";
						}
					}
					str += ")";
				}
				return str;
			}
			return "*";
		}
	}

	private class TypeConstantDefaultTreeModel extends DefaultTreeModel {

		TypeConstantDefaultTreeModel(TypeConstantMutableTreeNode node) {
			super(node);
		}

		public void valueForPathChanged(TreePath path, Object newValue) {
			TypeConstantMutableTreeNode pathnode = (TypeConstantMutableTreeNode) path.getLastPathComponent();
		}
	}

	class TypeConstantJTree extends JTree {

		public static final long serialVersionUID = 0;

		TypeConstantJTree(DefaultTreeModel model) {
			super(model);
			this.setCellEditor(cellEditor);
		}

	}

	private TypeConstantMutableTreeNode lastNode = null;

	private class MoonstoneRuleTreeCellRenderer extends DefaultTreeCellRenderer {

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			TypeConstantMutableTreeNode node = (TypeConstantMutableTreeNode) value;
			if (node.getUserObject() instanceof TypeConstant) {
				TypeConstant tc = (TypeConstant) node.getUserObject();
				setToolTipText(tc.toString());
			}
			return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		}
	}

	class TypeConstantJTreeModelListener implements TreeModelListener {

		public void treeNodesChanged(TreeModelEvent e) {
			TypeConstantMutableTreeNode node = (TypeConstantMutableTreeNode) (e.getTreePath().getLastPathComponent());
			try {
				int index = e.getChildIndices()[0];
				node = (TypeConstantMutableTreeNode) (node.getChildAt(index));
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

	private static JMenuBar createMenuBar(String[][] menuinfo, Object[][] menuiteminfo, ActionListener listener) {
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

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		String cmd = e.getActionCommand();
		if ("storeOntology".equals(cmd)) {
			storeOntology();
		} else if ("reloadOntology".equals(cmd)) {
			this.moonstone.reloadOntology();
			this.initializeTypeTree();
			this.repopulateJTree(false);
			this.initComponents();
		} else if ("addTypeConstant".equals(cmd)) {
			addTypeConstant();
		} else if ("removeTypeConstant".equals(cmd)) {
			removeTypeConstant();
		} else if ("addStringConstant".equals(cmd)) {
			addStringConstant();
		} else if ("removeStringConstant".equals(cmd)) {
			removeStringConstant();
		} else if ("addSynonym".equals(cmd)) {
			addSynonym();
		} else if ("removeSynonym".equals(cmd)) {
			removeSynonym();
		} else if ("collapseRows".equals(cmd)) {
			collapseOrExpandRows(false);
		} else if ("expandRows".equals(cmd)) {
			collapseOrExpandRows(true);
		} else if ("doQuit".equals(cmd)) {
			this.dispose();
		}
	}

	private void collapseOrExpandRows(boolean expand) {
		for (int i = 0; i < this.typeConstantJTree.getRowCount(); i++) {
			if (expand) {
				this.typeConstantJTree.expandRow(i);
			} else {
				this.typeConstantJTree.collapseRow(i);
			}
		}
	}

	private void removeSelections() {
		this.selectedGrammarRule = null;
		this.selectedNode = null;
		this.selectedStringConstant = null;
		this.selectedSynonym = null;
		this.selectedTypeConstant = null;
		updateListModel(searchResultsListModel, null);
		updateListModel(constantWordsListModel, null);
		updateListModel(grammarRuleListModel, null);
	}

	private void storeOntology() {
		try {
			String fname = this.moonstone.getKnowledgeEngine().getStartupParameters()
					.getPropertyValue(MoonstoneRuleInterface.OntologyPropertyName);
			String fpath = this.moonstone.getResourceDirectoryName() + File.separatorChar + fname;
			String bfilepath = fpath + "_" + TimeUtils.getDateTimeString("yyyy_MM_dd_HH_mm_ss");
			FUtils.copyFile(fpath, bfilepath);
			File file = new File(fpath);
			StringBuffer sb = new StringBuffer();
			String oname = this.moonstone.getKnowledgeEngine().getCurrentOntology().getName();
			sb.append("'(" + oname + "\n\n");
			String str = this.moonstone.getKnowledgeEngine().getCurrentOntology().toDefinitionString();
			sb.append(str);
			sb.append("\n)\n");
			FUtils.writeFile(file.getAbsolutePath(), sb.toString(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addTypeConstant() {
		NameSpace ns = this.moonstone.getKnowledgeEngine().getCurrentKnowledgeBase().getNameSpace();
		Ontology o = this.moonstone.getKnowledgeEngine().getCurrentOntology();
		if (this.selectedTypeConstant == null) {
			JOptionPane.showMessageDialog(new JFrame(), "Must select parent type");
			return;
		}
		String tname = JOptionPane.showInputDialog(new JFrame(), "New Type Name:");
		if (!TypeConstant.isTypeConstantFormalName(tname)) {
			JOptionPane.showMessageDialog(new JFrame(), "Name not in correct format");
			return;
		}
		TypeConstant tc = ns.getTypeConstant(tname);
		if (tc != null) {
			JOptionPane.showMessageDialog(new JFrame(), "Type already exists");
			return;
		}
		tc = TypeConstant.createTypeConstant(tname);
		tc.addParent(this.selectedTypeConstant);
		this.selectedTypeConstant.addChild(tc);
		this.selectedTypeConstant = tc;
		this.repopulateJTree(false);
		this.updateAllPanels();
	}

	private void removeTypeConstant() {
		if (this.selectedTypeConstant == null) {
			JOptionPane.showMessageDialog(new JFrame(), "Must select type");
			return;
		}
		Vector rv = this.getSelectedTreeNodeValues();
		if (rv != null) {
			int answer = JOptionPane.showConfirmDialog(new JFrame(),
					"WARNING:  Remove selected types?");
			if (answer != JOptionPane.YES_OPTION) {
				return;
			}
		}
		for (Object o : rv) {
			if (o instanceof TypeConstant) {
				TypeConstant tc = (TypeConstant) o;
				tc.remove();
			}
		}
		removeSelections();
		this.repopulateJTree(false);
		this.updateAllPanels();
	}

	private void addStringConstant() {
		NameSpace ns = this.moonstone.getKnowledgeEngine().getCurrentKnowledgeBase().getNameSpace();
		if (this.selectedTypeConstant == null) {
			JOptionPane.showMessageDialog(new JFrame(), "Must select parent type");
			return;
		}
		String cname = JOptionPane.showInputDialog(new JFrame(), "New Constant Name:");
		if (!StringConstant.isStringConstantFormalName(cname)) {
			JOptionPane.showMessageDialog(new JFrame(), "Name not in correct format");
			return;
		}
		StringConstant sc = ns.getStringConstant(cname);
		if (sc != null) {
			JOptionPane.showMessageDialog(new JFrame(), "Constant already exists");
			return;
		}
		sc = StringConstant.createStringConstant(cname, this.selectedTypeConstant, false);
		this.selectedStringConstant = sc;
		this.repopulateJTree(false);
		this.updateAllPanels();
	}

	private void removeStringConstant() {
		if (this.selectedStringConstant == null) {
			JOptionPane.showMessageDialog(new JFrame(), "Must select constant");
			return;
		}
		Vector rv = this.getSelectedTreeNodeValues();
		for (Object o : rv) {
			if (o instanceof StringConstant) {
				StringConstant sc = (StringConstant) o;
				sc.remove();
			}
		}
		removeSelections();
		this.repopulateJTree(false);
		this.updateAllPanels();
	}

	private void addSynonym() {
		if (this.selectedStringConstant == null) {
			JOptionPane.showMessageDialog(new JFrame(), "Must select constant to add synonym to");
			return;
		}
		String syn = JOptionPane.showInputDialog(new JFrame(), "New Synonym:");
		this.selectedStringConstant.addSynonym(syn);
		this.moonstone.reloadRules(false);
		this.updateAllPanels();
	}

	private void removeSynonym() {
		if (this.selectedStringConstant == null || this.selectedSynonym == null) {
			JOptionPane.showMessageDialog(new JFrame(), "Must select constant & synonym");
			return;
		}
		this.selectedStringConstant.removeSynonym(this.selectedSynonym);
		this.selectedSynonym = null;
		this.moonstone.reloadRules(false);
		this.updateAllPanels();
	}

	public void valueChanged(TreeSelectionEvent e) {
		ClickingTree = true;
		TypeConstantMutableTreeNode node = (TypeConstantMutableTreeNode) typeConstantJTree
				.getLastSelectedPathComponent();
		if (node != null) {
			this.selectedNode = node;
			Object o = node.getUserObject();
			if (o instanceof TypeConstant) {
				this.selectedTypeConstant = (TypeConstant) o;
				this.selectedStringConstant = null;
			} else if (o instanceof StringConstant) {
				this.selectedStringConstant = (StringConstant) o;
				this.selectedTypeConstant = this.selectedStringConstant.getType();
				updateListModel(constantWordsListModel, this.selectedStringConstant.getSynonyms());
			}
			this.updateAllPanels();
		}
		ClickingTree = false;
	}

	public void keyReleased(KeyEvent e) {

	}

	public void keyPressed(KeyEvent e) {

	}

	public void keyTyped(KeyEvent e) {

	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void itemStateChanged(ItemEvent e) {
		int x = 1;
	}

	public void performSearch(String text) {
		Vector rv = this.moonstone.getKnowledgeEngine().getCurrentOntology().doGeneralTextSearch(text);
		updateListModel(searchResultsListModel, rv);
	}

	private boolean updatingModel = false;

	public void updateListModel(DefaultListModel model, Vector rv) {
		if (updatingModel) {
			return;
		}
		updatingModel = true;
		if (model == null) {
			int x = 1;
		}
		try {
			model.removeAllElements();
			if (rv != null) {
				for (Object o : rv) {
					if (!(o instanceof StringConstant && ((StringConstant) o).isComplex())) {
						model.addElement(o);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updatingModel = false;
	}

	public void updateAllPanels() {
		if (updatingModel) {
			return;
		}
		TreePath path = null;
		if (this.selectedStringConstant != null) {
			updateListModel(constantWordsListModel, this.selectedStringConstant.getSynonyms());
			Vector<Rule> conceptRules = this.moonstone.getSentenceGrammar()
					.getStringConstantRules(this.selectedStringConstant);
			Vector<Rule> patternRules = this.moonstone.getSentenceGrammar()
					.getRulesByIndexToken(this.selectedStringConstant);
			Vector<String> ids = null;
			if (conceptRules != null) {
				ids = VUtils.add(ids, "CONCEPT:");
				ids = VUtils.append(ids, Rule.getRuleIDs(conceptRules));
			}
			if (patternRules != null) {
				ids = VUtils.add(ids, "PATTERN:");
				ids = VUtils.append(ids, Rule.getRuleIDs(patternRules));
			}
			updateListModel(grammarRuleListModel, ids);
			path = this.nodePathHash.get(this.selectedStringConstant);
		} else if (this.selectedTypeConstant != null) {
			Vector<Rule> conceptRules = null;
			Vector<Rule> patternRules = this.moonstone.getSentenceGrammar()
					.getRulesByIndexToken(this.selectedTypeConstant);
			Vector<String> ids = null;
			if (conceptRules != null) {
				ids = VUtils.add(ids, "CONCEPT:");
				ids = VUtils.append(ids, Rule.getRuleIDs(conceptRules));
			}
			if (patternRules != null) {
				ids = VUtils.add(ids, "PATTERN:");
				ids = VUtils.append(ids, Rule.getRuleIDs(patternRules));
			}
			updateListModel(grammarRuleListModel, ids);
			path = this.nodePathHash.get(this.selectedTypeConstant);
		}
		if (path != null && !ClickingTree) {
			this.typeConstantJTree.scrollPathToVisible(path);
			this.typeConstantJTree.setSelectionPath(path);
		}
	}

	public void updateOntologyObjectSelection(Object selected) {
		if (updatingModel || UpdatingSelections) {
			return;
		}
		UpdatingSelections = true;
		this.selectedTypeConstant = null;
		this.selectedStringConstant = null;
		this.selectedSynonym = null;
		if (selected instanceof TypeConstant) {
			TypeConstant tc = (TypeConstant) selected;
			this.selectedTypeConstant = (TypeConstant) selected;
			this.selectedStringConstant = null;
			updateAllPanels();
		} else if (selected instanceof StringConstant) {
			StringConstant sc = (StringConstant) selected;
			this.selectedStringConstant = sc;
			this.selectedTypeConstant = sc.getType();
			updateAllPanels();
		} else if (selected instanceof String) {
			String str = (String) selected;
			Vector<StringConstant> syns = this.synonymConstantHash.get(str);
			updateListModel(searchResultsListModel, syns);
		}
		UpdatingSelections = false;
	}

	public void displayGrammarRule() {
		if (this.selectedGrammarRule != null) {
			StringBuffer sb = new StringBuffer();
			sb.append("FILE: " + this.selectedGrammarRule.getSourceFileName() + "\n");
			sb.append(this.selectedGrammarRule.getSexp().toNewlinedString());
			sb.append("\n\nEdit Rule?");
			int answer = JOptionPane.showConfirmDialog(this, sb.toString());
			if (answer == JOptionPane.YES_OPTION) {
				StringSelection selection = new StringSelection(this.selectedGrammarRule.getRuleID());
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selection, selection);
				CurrentTypeOntologyFrame.moonstone.viewSelectedRuleFile(this.selectedGrammarRule);
			}
		}
	}

	private void clearHashTables() {
		nodePathHash.clear();
		synonymConstantHash.clear();
	}

	///////////// NETBEANS ////////////
	private void initComponents() {

		typeTreeSP = new javax.swing.JScrollPane(this.typeConstantJTree,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		typeTreeSP.setVisible(true);
		searchPanel = new javax.swing.JPanel();
		searchFieldLabel = new javax.swing.JLabel();
		searchFieldText = new javax.swing.JTextField();
		searchFieldText.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (updatingModel || UpdatingSelections) {
					return;
				}
				PerformingSelectionPanelOperation = true;
				performSearch(searchFieldText.getText());
				PerformingSelectionPanelOperation = false;
			}
		});
		searchResultsSP = new javax.swing.JScrollPane();
		searchResultsListModel = new DefaultListModel();
		searchResultsList = new javax.swing.JList(searchResultsListModel);
		searchResultsLabel = new javax.swing.JLabel();
		searchResultsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				updateOntologyObjectSelection(searchResultsList.getSelectedValue());
			}
		});
		constantWordsSP = new javax.swing.JScrollPane();
		constantWordsListModel = new DefaultListModel();
		constantWordsList = new javax.swing.JList(constantWordsListModel);

		constantWordsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				CurrentTypeOntologyFrame.selectedSynonym = constantWordsList.getSelectedValue();
			}
		});

		grammarRuleSP = new javax.swing.JScrollPane();
		grammarRuleListModel = new DefaultListModel();
		grammarRuleList = new javax.swing.JList(grammarRuleListModel);
		grammarRuleLabel = new javax.swing.JLabel();
		grammarRuleList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				String ruleid = grammarRuleList.getSelectedValue();
				CurrentTypeOntologyFrame.selectedGrammarRule = CurrentTypeOntologyFrame.moonstone.getSentenceGrammar()
						.getRuleByID(ruleid);
				displayGrammarRule();
			}
		});

		constantWordsLabel = new javax.swing.JLabel();
		ontologyToolLabel = new javax.swing.JLabel();
		actionButtonPanel = new javax.swing.JPanel();
		addTypeButton = new javax.swing.JButton();
		deleteTypeButton = new javax.swing.JButton();
		addConstantButton = new javax.swing.JButton();
		deleteConstantButton = new javax.swing.JButton();
		addWordButton = new javax.swing.JButton();
		deleteWordButton = new javax.swing.JButton();
		deleteRuleButton = new javax.swing.JButton();
		moveTypeButton = new javax.swing.JButton();
		moveConstantButton = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		searchPanel.setBorder(new javax.swing.border.MatteBorder(null));

		searchFieldLabel.setText("Search:");

		searchResultsSP.setViewportView(searchResultsList);

		searchResultsLabel.setText("Results:");

		constantWordsSP.setViewportView(constantWordsList);

		constantWordsLabel.setText("Words:");

		grammarRuleSP.setViewportView(grammarRuleList);

		grammarRuleLabel.setText("Rules:");

		javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
		searchPanel.setLayout(searchPanelLayout);
		searchPanelLayout
				.setHorizontalGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(searchPanelLayout.createSequentialGroup().addGap(9, 9, 9).addGroup(searchPanelLayout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
								.addComponent(constantWordsLabel, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(searchResultsLabel, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(searchFieldLabel).addComponent(grammarRuleLabel,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(searchPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										.addComponent(grammarRuleSP, javax.swing.GroupLayout.DEFAULT_SIZE, 259,
												Short.MAX_VALUE)
										.addComponent(searchResultsSP).addComponent(searchFieldText,
												javax.swing.GroupLayout.PREFERRED_SIZE, 254,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(searchPanelLayout.createSequentialGroup().addGap(65, 65, 65)
										.addComponent(constantWordsSP, javax.swing.GroupLayout.PREFERRED_SIZE, 266,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addContainerGap(144, Short.MAX_VALUE))));
		searchPanelLayout.setVerticalGroup(searchPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(searchPanelLayout.createSequentialGroup().addGap(15, 15, 15)
						.addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(searchFieldLabel).addComponent(searchFieldText,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(searchPanelLayout.createSequentialGroup().addGap(33, 33, 33).addComponent(
										searchResultsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26,
										javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGroup(searchPanelLayout.createSequentialGroup().addGap(16, 16, 16).addComponent(
										searchResultsSP, javax.swing.GroupLayout.PREFERRED_SIZE, 109,
										javax.swing.GroupLayout.PREFERRED_SIZE)))
						.addGap(18, 18, 18)
						.addComponent(constantWordsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 33,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 83, Short.MAX_VALUE)
						.addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(grammarRuleSP, javax.swing.GroupLayout.Alignment.TRAILING,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
										searchPanelLayout.createSequentialGroup()
												.addComponent(grammarRuleLabel, javax.swing.GroupLayout.PREFERRED_SIZE,
														27, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addGap(103, 103, 103)))
						.addGap(53, 53, 53))
				.addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(searchPanelLayout.createSequentialGroup().addGap(179, 179, 179)
								.addComponent(constantWordsSP, javax.swing.GroupLayout.PREFERRED_SIZE, 106,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addContainerGap(208, Short.MAX_VALUE))));

		ontologyToolLabel.setBackground(new java.awt.Color(153, 204, 255));
		ontologyToolLabel.setFont(new java.awt.Font("Lucida Grande", 0, 24));
		ontologyToolLabel.setText("TSL Ontology Tool");

		actionButtonPanel.setBorder(new javax.swing.border.MatteBorder(null));

		addTypeButton.setText("AddType");
		addTypeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addTypeButtonActionPerformed(evt);
			}
		});

		deleteTypeButton.setText("DeleteType");
		deleteTypeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteTypeButtonActionPerformed(evt);
			}
		});

		addConstantButton.setText("AddConst");
		addConstantButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addConstantButtonActionPerformed(evt);
			}
		});

		deleteConstantButton.setText("DeleteConst");
		deleteConstantButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteConstantButtonActionPerformed(evt);
			}
		});

		addWordButton.setText("AddWord");
		addWordButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addWordButtonActionPerformed(evt);
			}
		});

		deleteWordButton.setText("DeleteWord");
		deleteWordButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteWordButtonActionPerformed(evt);
			}
		});

		deleteRuleButton.setText("DeleteRule");
		deleteRuleButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteRuleButtonActionPerformed(evt);
			}
		});

		moveTypeButton.setText("MoveType");
		moveTypeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				moveTypeButtonActionPerformed(evt);
			}
		});

		moveConstantButton.setText("MoveConstant");
		moveConstantButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				moveConstantButtonActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout actionButtonPanelLayout = new javax.swing.GroupLayout(actionButtonPanel);
		actionButtonPanel.setLayout(actionButtonPanelLayout);
		actionButtonPanelLayout.setHorizontalGroup(actionButtonPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(actionButtonPanelLayout.createSequentialGroup().addGap(15, 15, 15)
						.addGroup(actionButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(addTypeButton).addComponent(addWordButton).addComponent(moveTypeButton))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(actionButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(moveConstantButton, javax.swing.GroupLayout.PREFERRED_SIZE, 146,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGroup(actionButtonPanelLayout.createSequentialGroup().addComponent(deleteTypeButton)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(addConstantButton)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(deleteConstantButton))
								.addGroup(actionButtonPanelLayout.createSequentialGroup().addComponent(deleteWordButton)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(deleteRuleButton)))
						.addGap(0, 20, Short.MAX_VALUE)));
		actionButtonPanelLayout
				.setVerticalGroup(actionButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(actionButtonPanelLayout.createSequentialGroup().addContainerGap()
								.addGroup(actionButtonPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(addTypeButton).addComponent(deleteTypeButton)
										.addComponent(addConstantButton).addComponent(deleteConstantButton))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(actionButtonPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(addWordButton).addComponent(deleteWordButton)
										.addComponent(deleteRuleButton))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(actionButtonPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(moveTypeButton).addComponent(moveConstantButton))
								.addContainerGap(7, Short.MAX_VALUE)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap()
						.addComponent(typeTreeSP, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(actionButtonPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(searchPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addContainerGap())
				.addGroup(layout.createSequentialGroup().addGap(136, 136, 136)
						.addComponent(ontologyToolLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 304,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup().addGap(4, 4, 4).addComponent(ontologyToolLabel)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addComponent(typeTreeSP, javax.swing.GroupLayout.PREFERRED_SIZE, 683,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGroup(layout.createSequentialGroup()
								.addComponent(searchPanel, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(actionButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		pack();
	}// </editor-fold>

	private void addTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		this.addTypeConstant();
	}

	private void deleteTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		this.removeTypeConstant();
	}

	private void addConstantButtonActionPerformed(java.awt.event.ActionEvent evt) {
		this.addStringConstant();
	}

	private void deleteConstantButtonActionPerformed(java.awt.event.ActionEvent evt) {
		this.removeStringConstant();
	}

	private void addWordButtonActionPerformed(java.awt.event.ActionEvent evt) {
		this.addSynonym();
	}

	private void deleteWordButtonActionPerformed(java.awt.event.ActionEvent evt) {
		this.removeSynonym();
	}

	private void deleteRuleButtonActionPerformed(java.awt.event.ActionEvent evt) {
		if (this.selectedGrammarRule == null) {
			JOptionPane.showMessageDialog(this, "Must select grammar rule to delete");
			return;
		}
		String fpath = this.selectedGrammarRule.getSourceFilePath();
		File file = new File(fpath);
		if (file.exists()) {
			int answer = JOptionPane.showConfirmDialog(new JFrame(),
					"Delete rule file (" + file.getName() + ")? (Not reversible)");
			if (answer == JOptionPane.YES_OPTION) {
				this.selectedGrammarRule = null;
				 file.delete();
				 this.moonstone.reloadRules(false);
				this.updateAllPanels();
			}
		}
	}

	private void moveTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		TreePath[] paths = this.typeConstantJTree.getSelectionPaths();
		if (!(paths != null && paths.length == 2)) {
			JOptionPane.showMessageDialog(this, "Must select source and target types");
			return;
		}
		Object o1 = ((TypeConstantMutableTreeNode) paths[0].getLastPathComponent()).getUserObject();
		Object o2 = ((TypeConstantMutableTreeNode) paths[1].getLastPathComponent()).getUserObject();
		if (!(o1 instanceof TypeConstant && o2 instanceof TypeConstant)) {
			JOptionPane.showMessageDialog(this, "Must select source and target types");
			return;
		}
		TypeConstant sourcetype = (TypeConstant) o1;
		TypeConstant targettype = (TypeConstant) o2;
		int answer = JOptionPane.showConfirmDialog(this,
				"Move " + sourcetype.getFormalName() + " to " + targettype.getFormalName() + "?");
		if (answer == JOptionPane.YES_OPTION) {
			sourcetype.removeFromParents();
			sourcetype.addParent(targettype);
			this.repopulateJTree(false);
			this.updateAllPanels();
		}
	}

	private void moveConstantButtonActionPerformed(java.awt.event.ActionEvent evt) {
		TreePath[] paths = this.typeConstantJTree.getSelectionPaths();
		if (paths == null || paths.length < 2) {
			JOptionPane.showMessageDialog(this, "Must select constants and target");
			return;
		}
		Object o = ((TypeConstantMutableTreeNode) paths[paths.length - 1].getLastPathComponent()).getUserObject();
		if (!(o instanceof TypeConstant)) {
			JOptionPane.showMessageDialog(this, "Last selected node must be Type");
			return;
		}
		TypeConstant tc = (TypeConstant) o;
		Vector<StringConstant> scs = new Vector(0);
		for (int i = 0; i < paths.length - 1; i++) {
			o = ((TypeConstantMutableTreeNode) paths[i].getLastPathComponent()).getUserObject();
			if (!(o instanceof StringConstant)) {
				JOptionPane.showMessageDialog(this, "All elements to move must be Constants");
				return;
			}
			scs.add((StringConstant) o);
		}
		for (StringConstant sc : scs) {
			sc.getType().removeTypedStringConstant(sc);
			sc.setType(null);
			tc.addTypedStringConstant(sc);
		}
		this.selectedStringConstant = null;
		this.repopulateJTree(false);
		this.updateAllPanels();
	}

	private javax.swing.JButton addConstantButton;
	private javax.swing.JButton addTypeButton;
	private javax.swing.JButton addWordButton;
	private javax.swing.JLabel constantWordsLabel;
	private javax.swing.JList<String> constantWordsList;
	private javax.swing.JScrollPane constantWordsSP;
	private javax.swing.JButton deleteConstantButton;
	private javax.swing.JButton deleteTypeButton;
	private javax.swing.JButton deleteWordButton;
	private javax.swing.JButton deleteRuleButton;
	private javax.swing.JPanel actionButtonPanel;
	private javax.swing.JLabel ontologyToolLabel;
	private javax.swing.JLabel searchFieldLabel;
	private javax.swing.JTextField searchFieldText;
	private javax.swing.JPanel searchPanel;
	private javax.swing.JLabel searchResultsLabel;
	private javax.swing.JList<String> searchResultsList;
	private javax.swing.JScrollPane searchResultsSP;
	private javax.swing.JScrollPane typeTreeSP;
	private javax.swing.JLabel grammarRuleLabel;
	private javax.swing.JList<String> grammarRuleList;
	private javax.swing.JScrollPane grammarRuleSP;
	private javax.swing.JButton moveTypeButton;
	private javax.swing.JButton moveConstantButton;
	// End of variables declaration

	private DefaultListModel searchResultsListModel = null;
	private DefaultListModel stringConstantsListModel = null;
	private DefaultListModel constantWordsListModel = null;
	private DefaultListModel grammarRuleListModel = null;

	// End of variables declaration
}
