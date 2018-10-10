package moonstone.rulebuilder;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import moonstone.annotation.Annotation;
import moonstone.rulebuilder.MoonstoneRuleInterface.AnnotationRuleMutableTreeNode;
import tsl.expression.term.Term;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.knowledgebase.NameSpace;
import tsl.knowledge.knowledgebase.SymbolTable;
import tsl.knowledge.ontology.Ontology;

public class TSLOntologyPanel extends JPanel implements TreeSelectionListener, ItemListener, ActionListener,
		KeyListener, MouseMotionListener, MouseListener {

	protected TypeConstantMutableTreeNode ontologyRootNode = null;
	protected TypeConstantDefaultTreeModel ontologyModel = null;
	protected TypeConstantJTree typeConstantJTree = null;
	protected TypeConstantMutableTreeNode selectedNode = null;
	protected MoonstoneRuleInterface moonstone = null;
	protected TypeConstant selectedTypeConstant = null;
	protected StringConstant selectedStringConstant = null;

	protected static String[][] menuInfo = { { null, "operations", "Operations" } };

	protected static Object[][] menuItemInfo = { { "operations", "readOntology", "Read Ontology" },
			{ "operations", "storeOntology", "Store Ontology" }, { "operations", "printOntology", "Print Ontology" },
			{ "operations", "openRows", "Open Rows" }, { "operations", "closeRows", "Close Rows" },
			{ "operations", "addStringConstant", "Add New StringConstant" },
			{ "operations", "removeStringConstant", "Remove StringConstant" }, };

	public TSLOntologyPanel(MoonstoneRuleInterface msri) {
		this.moonstone = msri;
		Dimension d = new Dimension(1000, 1000);
		this.setMinimumSize(d);
		this.setPreferredSize(d);
		TypeConstant roottc = this.moonstone.getKnowledgeEngine().getCurrentOntology().getRootType();
		this.ontologyRootNode = new TypeConstantMutableTreeNode(this, roottc);
		this.ontologyModel = new TypeConstantDefaultTreeModel(this.ontologyRootNode);
		this.typeConstantJTree = new TypeConstantJTree(this.ontologyModel);
		this.typeConstantJTree.setCellRenderer(new MoonstoneRuleTreeCellRenderer());
		this.repopulateJTree(false);
		ToolTipManager.sharedInstance().registerComponent(this.typeConstantJTree);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

		this.typeConstantJTree.setEditable(false);
		this.typeConstantJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.typeConstantJTree.setShowsRootHandles(true);
		this.typeConstantJTree.addTreeSelectionListener(this);
		this.typeConstantJTree.addMouseMotionListener(this);
		this.typeConstantJTree.addMouseListener(this);

		JLabel label = new JLabel("Types:");
		JPanel panel = new JPanel();
		d = new Dimension(800, 800);

		JScrollPane jsp = new JScrollPane(this.typeConstantJTree, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setMinimumSize(d);
		jsp.setPreferredSize(d);
		jsp.setVisible(true);
		panel.add(label);
		panel.add(jsp);

		this.add(panel);
	}

	public static void displayTSLOntologyFrame(MoonstoneRuleInterface msri) {
		TSLOntologyPanel panel = new TSLOntologyPanel(msri);
		JFrame f = new JFrame();
		f.setJMenuBar(MoonstoneRuleInterface.createMenuBar(menuInfo, menuItemInfo, panel, panel));
		f.setContentPane(panel);
		f.show();
	}

	public void repopulateJTree(boolean expand) {
		TypeConstant root = this.moonstone.getKnowledgeEngine().getCurrentOntology().getRootType();
		this.ontologyRootNode = new TypeConstantMutableTreeNode(this, null);
		populateJTree(this.ontologyRootNode, root, true);
		if (this.ontologyModel != null) {
			this.ontologyModel.setRoot(this.ontologyRootNode);
			this.ontologyModel.reload();
		}
		this.collapseOrExpandRows(expand);
	}

	private void populateJTree(TypeConstantMutableTreeNode pnode, Term term, boolean atRootLevel) {
		boolean doitanyway = true;

		TypeConstantMutableTreeNode cnode = new TypeConstantMutableTreeNode(this, term);
		if (pnode != null) {
			this.ontologyModel.insertNodeInto(cnode, pnode, pnode.getChildCount());
		}
		if (term instanceof TypeConstant) {
			TypeConstant tc = (TypeConstant) term;
			if (!tc.isRoot() && tc.getTypedStringConstants() != null) {
				Vector<StringConstant> scv = tc.getTypedStringConstants();
				Collections.sort(scv, new Term.LabelSorter());
				for (Term cterm : scv) {
					populateJTree(cnode, cterm, false);
					if (cterm.getType().isRoot()) {
						System.out.println("\"" + cterm.getName() + "\"");
					}
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

	}

	private class TypeConstantMutableTreeNode extends DefaultMutableTreeNode {

		TSLOntologyPanel panel = null;
		Term term = null;

		TypeConstantMutableTreeNode(TSLOntologyPanel panel, Term t) {
			super(t);
			this.panel = panel;
			this.term = term;
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

	// private static JMenuBar createMenuBar(String[][] menuinfo, Object[][]
	// menuiteminfo, ActionListener listener,
	// JComponent component) {
	// Hashtable menuhash = new Hashtable();
	// JMenuBar menubar = new JMenuBar();
	//
	// for (int i = 0; i < menuinfo.length; i++) {
	// String[] array = (String[]) menuinfo[i];
	// String parentname = array[0];
	// String menuname = array[1];
	// String displayname = array[2];
	// JMenu menu = new JMenu(displayname);
	// menuhash.put(menuname, menu);
	// if (parentname != null) {
	// JMenu parent = (JMenu) menuhash.get(parentname);
	// parent.add(menu);
	// } else {
	// menubar.add(menu);
	// }
	// }
	//
	// for (int i = 0; i < menuiteminfo.length; i++) {
	// Object[] array = (Object[]) menuiteminfo[i];
	// String menuname = (String) array[0];
	// String actionname = (String) array[1];
	// String displayname = (String) array[2];
	// int key = -1;
	// int modifier = -1;
	// if (array.length > 3) {
	// Integer k = (Integer) array[3];
	// key = k.intValue();
	// Integer m = (Integer) array[4];
	// modifier = m.intValue();
	// }
	// JMenu menu = (JMenu) menuhash.get(menuname);
	// JMenuItem menuitem = new JMenuItem(displayname);
	// menuitem.setActionCommand(actionname);
	// menuitem.addActionListener(listener);
	// if (modifier > 0) {
	// KeyStroke ks = KeyStroke.getKeyStroke(key, modifier);
	// menuitem.setAccelerator(ks);
	// }
	// try {
	// menu.add(menuitem);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// return menubar;
	// }

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		String cmd = e.getActionCommand();
		if ("storeOntology".equals(cmd)) {
		} else if ("readOntology".equals(cmd)) {

		} else if ("printOntology".equals(cmd)) {
			String str = this.moonstone.getKnowledgeEngine().getCurrentOntology().toDefinitionString();
			System.out.println(str);
		} else if ("addStringConstant".equals(cmd)) {
			addStringConstant();
		} else if ("removeStringConstant".equals(cmd)) {
			removeStringConstant();
		} else if ("openRows".equals(cmd)) {
			this.collapseOrExpandRows(true);
		} else if ("closeRows".equals(cmd)) {
			this.collapseOrExpandRows(false);
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

	private void addStringConstant() {
		NameSpace ns = this.moonstone.getKnowledgeEngine().getCurrentKnowledgeBase().getNameSpace();
		Ontology o = this.moonstone.getKnowledgeEngine().getCurrentOntology();
		if (this.selectedTypeConstant == null) {
			JOptionPane.showMessageDialog(new JFrame(), "Must select type");
			return;
		}
		String cname = JOptionPane.showInputDialog(new JFrame(), "Constant Name:");
		if (!StringConstant.isStringConstantFormalName(cname)) {
			JOptionPane.showMessageDialog(new JFrame(), "Name not in correct format");
			return;
		}
		StringConstant sc = ns.getStringConstant(cname);
		if (sc != null) {
			JOptionPane.showMessageDialog(new JFrame(),
					"Constant already exists, under type " + sc.getType().getFormalName());
			return;
		}
		StringConstant.createStringConstant(cname, this.selectedTypeConstant, false);
		this.repopulateJTree(true);
	}

	private void removeStringConstant() {
		if (this.selectedStringConstant == null) {
			JOptionPane.showMessageDialog(new JFrame(), "Must select string constant to remove");
			return;
		}
		SymbolTable st = this.moonstone.getKnowledgeEngine().getCurrentKnowledgeBase().getNameSpace()
				.getCurrentSymbolTable();
		st.removeStringConstant(this.selectedStringConstant);
		this.repopulateJTree(true);
	}

	public void valueChanged(TreeSelectionEvent e) {
		TypeConstantMutableTreeNode node = (TypeConstantMutableTreeNode) typeConstantJTree
				.getLastSelectedPathComponent();
		if (node != null) {
			this.selectedNode = node;
			Object o = node.getUserObject();
			if (o instanceof TypeConstant) {
				this.selectedTypeConstant = (TypeConstant) o;
				Vector<StringConstant> scs = this.selectedTypeConstant.getTypedStringConstants();
				if (scs != null) {
					Vector<String> scnames = StringConstant.getFormalNames(scs);
					Collections.sort(scnames);
					for (String scname : scnames) {
						System.out.println("\t\"" + scname + "\"");
					}
				}
				this.selectedStringConstant = null;
			} else if (o instanceof StringConstant) {
				this.selectedStringConstant = (StringConstant) o;
				this.selectedTypeConstant = this.selectedStringConstant.getType();
			}
		}
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

}
