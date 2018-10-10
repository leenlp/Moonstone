package workbench.api.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import tsl.documentanalysis.document.Document;
import workbench.api.Analysis;
import workbench.api.Constants;
import workbench.api.annotation.Annotation;
import workbench.api.annotation.AnnotationCollection;
import workbench.api.annotation.Annotator;
import workbench.api.annotation.OverlappingAnnotationPair;
import workbench.api.annotation.Span;
import workbench.api.typesystem.Attribute;
import workbench.api.typesystem.Relation;
import workbench.api.typesystem.Type;
import workbench.arr.Colors;

public class DocumentPanel extends JPanel implements MouseMotionListener, MouseListener, ActionListener, KeyListener {
	Analysis analysis = null;
	Annotator annotator = null;
	AnnotationCollection currentAnnotationCollection = null;
	Annotation currentAnnotation = null;
	Annotation lastSelectedAnnotation = null;
	private JTextPane documentTextPane = null;
	private Document currentDocument = null;
	private static String tooltipText = "";
	private static boolean movingMouse = false;
	private static boolean allowUpdate = true;

	DocumentPanel(Analysis analysis, Annotator annotator) {
		super(new BorderLayout());
		this.analysis = analysis;
		this.annotator = annotator;
		documentTextPane = new JTextPane() {
			public String getToolTipText(MouseEvent e) {
				return DocumentPanel.tooltipText;
			}
		};

		// 11/13/2015
		documentTextPane.setPreferredSize(new Dimension(500, 10000));

		documentTextPane.addMouseListener(this);
		documentTextPane.addMouseMotionListener(this);
		documentTextPane.setEditable(false);
		documentTextPane.setCaretPosition(0);
		documentTextPane.setToolTipText("");
		documentTextPane.addKeyListener(this);
		if (this.analysis != null && this.getAnalysis().getSelectedDocument() != null) {
			String dtext = this.getAnalysis().getSelectedDocument().getText();
			documentTextPane.setText(dtext);
		}
		documentTextPane.setFocusTraversalKeysEnabled(false);
		FlowLayout fl = new FlowLayout();
		fl.setAlignment(FlowLayout.LEFT);
		this.add(documentTextPane);
		this.setOpaque(true);
	}

	public void actionPerformed(ActionEvent e) {
	}

	protected void setDocumentText() {
		if (this.getAnalysis().getSelectedDocument() != null
				&& this.currentDocument != this.getAnalysis().getSelectedDocument()) {
			this.currentDocument = this.getAnalysis().getSelectedDocument();
			String dtext = this.getAnalysis().getSelectedDocument().getText();
			this.documentTextPane.setText(dtext);
		}
	}

	protected void highlightSentences() {
		if (analysis.getSelectedDocument() == null) {
			return;
		}
		setDocumentText();
		int x = 1;

		// 10/15/2015
		if (lastSelectedAnnotation == this.currentAnnotation) {
			// return;
		}

		lastSelectedAnnotation = this.currentAnnotation;
		StyledDocument doc = (StyledDocument) documentTextPane.getDocument();
		Style style = doc.addStyle("Color", null);
		Color background = Color.white;
		StyleConstants.setBackground(style, background);
		StyleConstants.setForeground(style, Color.black);
		doc.setCharacterAttributes(0, analysis.getSelectedDocument().getText().length(), style, true);
		AnnotationCollection ac = this.setAnnotationCollection();
		Annotation currentAnnotation = this.currentAnnotation;
		if (ac != null && ac.getAnnotations() != null) {
			for (Annotation annotation : ac.getAnnotations()) {
				if (annotation.isDocumentLevel()) {
					continue;
				}
				Color color = this.getUnselectedColor(annotation);
				highlightAnnotation(annotation, doc, style, color);

				// Annotation other = annotation.getFirstMatchingAnnotation();
				// if (other == null) {
				// if (annotation.isPrimary()) {
				// highlightAnnotation(annotation, doc, style,
				// Color.yellow);
				// } else {
				// highlightAnnotation(annotation, doc, style, Color.RED);
				// }
				// } else {
				// highlightAnnotation(annotation, doc, style,
				// Color.LIGHT_GRAY);
				// }
			}
		}

		if (this.currentAnnotation != null) {
			Vector<Relation> rv = this.currentAnnotation.getRelations();
			if (rv != null) {
				for (Relation ro : rv) {
					Annotation relatum = ro.getModifier();
					highlightAnnotation(relatum, doc, style, Color.cyan);
				}
			}
			highlightAnnotation(this.currentAnnotation, doc, style, getSelectedColor(this.currentAnnotation));
		}
	}

	public void setCaretPosition() {
		setCaretPosition(this.currentAnnotation);
	}

	public void setCaretPosition(Annotation annotation) {
		if (!withUserInteraction() && annotation != null && annotation.getSpans() != null
				&& this.getAnalysis().getSelectedDocument() != null) {
			int pos = annotation.getEnd();
			documentTextPane.setCaretPosition(pos);
		}
	}

	private DocumentPanel getOtherPanel() {
		DocumentPanel other = null;
		if (this.isPrimary()) {
			return this.getAnalysis().getWorkbenchGUI().getSecondaryDocumentPanel();
		}
		return this.getAnalysis().getWorkbenchGUI().getPrimaryDocumentPanel();
	}

	public boolean isPrimary() {
		return this.annotator.isPrimary();
	}

	void highlightAnnotation(Annotation annotation, Color color) {
		StyledDocument doc = (StyledDocument) documentTextPane.getDocument();
		Style style = doc.addStyle("Color", null);
		highlightAnnotation(annotation, doc, style, color);
	}

	void highlightAnnotation(Annotation annotation, StyledDocument doc, Style style, Color color) {
		if (annotation != null && color != null && annotation.getSpans() != null) {
			StyleConstants.setBackground(style, color);
			for (Span span : annotation.getSpans()) {
				String text = annotation.getDocument().getText().substring(span.getStart(), span.getEnd());
				doc.setCharacterAttributes(span.getStart(), span.getLength() + 1, style, true);
			}
		}
	}

	private AnnotationCollection setAnnotationCollection() {
		Annotation sa = analysis.getSelectedAnnotation();
		if (sa != null && analysis.getSelectedAnnotationEvent() != null) {
			if (this.annotator.isPrimary()) {
				this.currentAnnotationCollection = analysis.getSelectedAnnotationEvent()
						.getPrimaryAnnotationCollection();
				this.currentAnnotation = sa.getMatchingPrimaryAnnotation();
			} else {
				this.currentAnnotationCollection = analysis.getSelectedAnnotationEvent()
						.getSecondaryAnnotationCollection();
				this.currentAnnotation = sa.getMatchingSecondaryAnnotation();
			}
		}
		return this.currentAnnotationCollection;
	}

	public void mouseClicked(MouseEvent e) {
		try {
			int offset = documentTextPane.viewToModel(e.getPoint());
			if (e.isControlDown()) {
				AnnotationCollection ac = this.setAnnotationCollection();
				if (ac != null) {
					Annotation annotation = ac.getClosestMatchingAnnotation(offset);
					analysis.setSelectedAnnotation(annotation);
				}
				this.getAnalysis().getWorkbenchGUI().fireAllDataUpdates();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
		this.getAnalysis().setSelectedAnnotator(this.annotator);
	}

	public void mousePressed(MouseEvent e) {
		int x = 1;
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		DocumentPanel.tooltipText = "";
		try {
			boolean ctrldown = e.isControlDown();
			boolean isAltDown = e.isAltDown();
			if (ctrldown && isAllowUpdate()) {
				movingMouse = true;
				int offset = documentTextPane.viewToModel(e.getPoint());
				AnnotationCollection ac = this.setAnnotationCollection();
				if (ac != null) {
					Annotation annotation = ac.getClosestMatchingAnnotation(offset);
					analysis.setSelectedAnnotation(annotation);
					if (annotation != null) {
						// String cstr = annotation.getOverlappingClassifications();
						// cstr += "[" + annotation.getAnnotatorName() + "]";
						// DocumentPanel.tooltipText = cstr;
						this.updateTooltipText();
						this.getAnalysis().getWorkbenchGUI().fireAllDataUpdates();
					}
				}
				movingMouse = false;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void updateTooltipText() {
		DocumentPanel.tooltipText = null;
		Annotation annotation = analysis.getSelectedAnnotation();
		if (annotation != null) {
			DocumentPanel.tooltipText = annotation.toHTML();
		}
	}

	boolean canSelectAnnotation(Annotation annotation, typesystem.Annotation type) {
		return true;
	}

	boolean canOffSelectAnnotation(Annotation annotation, Type type) {
		return true;
	}

	public void mouseDragged(MouseEvent e) {
	}

	static boolean withUserInteraction() {
		return movingMouse;
	}
	
	Color getSelectedColor(Annotation annotation) {
		// 11/24/2015: DANGER: This can cause undercounting of some errors!!!
		boolean hasClassMatch = annotation.overlappingSetContainsMatchedClassification();

		// Before 11/24/2015
		// boolean hasClassMatch = annotation.isContainsClassificationMatch();

		boolean hasMatch = annotation.hasMatchingAnnotations();
		Color color = null;
		if (hasClassMatch) {
			color = Color.GRAY;
		} else {
			color = Color.red;
		}
		return color;
	}

	Color getSelectedColor_BEFORE_6_28_2018(Annotation annotation) {
		// 11/24/2015: DANGER: This can cause undercounting of some errors!!!
		boolean hasClassMatch = annotation.overlappingSetContainsMatchedClassification();

		// Before 11/24/2015
		// boolean hasClassMatch = annotation.isContainsClassificationMatch();

		boolean hasMatch = annotation.hasMatchingAnnotations();
		Color color = null;
		if (annotation.isPrimary()) {
			color = Color.GRAY;
		} else {
			if (hasClassMatch) {
				color = Color.GRAY;
			} else {
				color = Color.red;
			}
		}
		return color;
	}

	Color getUnselectedColor(Annotation annotation) {
		Color color = null;

		// Before 11/24/2015
		// boolean hasClassMatch = annotation.isContainsClassificationMatch();

		// 11/24/2015: DANGER: This will undercount some errors!!
		boolean hasClassMatch = annotation.overlappingSetContainsMatchedClassification();

		boolean hasMatch = annotation.hasMatchingAnnotations();

		if (annotation.getType() != null && annotation.getType().getColor() != null) {
			color = annotation.getType().getColor();
		} else {
			if (annotation.isPrimary()) {
				if (hasClassMatch) {
					color = Color.green;
				} else if (hasMatch) {
					color = Color.yellow;
				} else {
					color = Color.gray;
				}
			} else {
				if (hasClassMatch) {
					color = Colors.darkGreen;
				} else if (hasMatch) {
					color = Colors.darkYellow;
				} else {
					color = Color.pink;
				}
			}
		}
		return color;
	}

	public static boolean isAllowUpdate() {
		return allowUpdate;
	}

	public static void setAllowUpdate(boolean allowUpdate) {
		DocumentPanel.allowUpdate = allowUpdate;
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		char c = e.getKeyChar();
		if (c == '\t') {
			processTabPress();
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	private void processTabPress() {
	}

	public String toString() {
		String type = (this.isPrimary() ? "Primary" : "Secondary");
		String fname = "*";
		if (this.currentDocument != null) {
			fname = this.currentDocument.getName();
		}
		String str = "<DocumentPanel: Type=" + type + ",File=" + fname + ">";
		return str;
	}

	public Annotation getCurrentAnnotation() {
		return currentAnnotation;
	}

	public Analysis getAnalysis() {
		return analysis;
	}

	public void setAnalysis(Analysis analysis) {
		this.analysis = analysis;
	}

}
