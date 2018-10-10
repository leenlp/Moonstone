
package moonstone.rulebuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import tsl.knowledge.ontology.Ontology;
import tsl.utilities.ListUtils;

public class MoonstoneRuleBuilderPanel extends javax.swing.JFrame {

	private MoonstoneRuleInterface moonstoneRuleInterface = null;
	private List<String> allConstants = new ArrayList<String>();

	public MoonstoneRuleBuilderPanel(MoonstoneRuleInterface msri) {
		this.moonstoneRuleInterface = msri;
		Vector<String> cnames = null;
		Ontology o = msri.getKnowledgeEngine().getCurrentOntology();
		this.allConstants.add("");
		this.allConstants = ListUtils.append(this.allConstants, o.getAllStringConstantNames());
		this.allConstants = ListUtils.append(this.allConstants, o.getAllTypeConstantNames());

		initComponents(this.allConstants);
	}

	  @SuppressWarnings("unchecked")
	    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
	    private void initComponents(List atlst) {

	        buttonGroup1 = new javax.swing.ButtonGroup();
	        topPanel = new javax.swing.JPanel();
	        ruleIDLabel = new javax.swing.JLabel();
	        ruleIDTextField = new javax.swing.JTextField();
	        conceptLabel = new javax.swing.JLabel();
	        stypeLabel = new javax.swing.JLabel();
	        constraintPanel = new javax.swing.JPanel();
	        orderedCheckbox = new javax.swing.JCheckBox();
	        juxtaposedCheckbox = new javax.swing.JCheckBox();
	        permitInterstitialCheckbox = new javax.swing.JCheckBox();
	        inhibitInterstitialCheckbox = new javax.swing.JCheckBox();
	        patternPanel = new javax.swing.JPanel();
	        subpattern1Panel = new javax.swing.JPanel();
	        subpatterntext_1_1 = new Java2sAutoTextField(atlst);
	        subpatterntext_1_2 = new Java2sAutoTextField(atlst);
	        subpatterntext_1_3 = new Java2sAutoTextField(atlst);
	        subpattern2Panel = new javax.swing.JPanel();
	        subpatterntext_2_1 = new Java2sAutoTextField(atlst);
	        subpatterntext_2_2 = new Java2sAutoTextField(atlst);
	        subpatterntext_2_3 = new Java2sAutoTextField(atlst);
	        subpattern3Panel = new javax.swing.JPanel();
	        subpatterntext_3_1 = new Java2sAutoTextField(atlst);
	        subpatterntext_3_2 = new Java2sAutoTextField(atlst);
	        subpatterntext_3_3 = new Java2sAutoTextField(atlst);
	        subpattern4Panel = new javax.swing.JPanel();
	        subpatterntext_4_1 = new Java2sAutoTextField(atlst);
	        subpatterntext_4_2 = new Java2sAutoTextField(atlst);
	        subpatterntext_4_3 = new Java2sAutoTextField(atlst);
	        subpattern5Panel = new javax.swing.JPanel();
	        subpatterntext_5_1 = new Java2sAutoTextField(atlst);
	        subpatterntext_5_2 = new Java2sAutoTextField(atlst);
	        subpatterntext_5_3 = new Java2sAutoTextField(atlst);
	        saveButton = new javax.swing.JButton();
	        quitButton = new javax.swing.JButton();
	        clearButton = new javax.swing.JButton();
	        conceptTextField = new Java2sAutoTextField(atlst);
	        stypeTextField = new Java2sAutoTextField(atlst);
	        testPanel = new javax.swing.JPanel();
	        test1Label = new javax.swing.JLabel();
	        test1VarLabel1 = new javax.swing.JLabel();
	        test1VarLabel2 = new javax.swing.JLabel();
	        test2Label = new javax.swing.JLabel();
	        test2VarLabel1 = new javax.swing.JLabel();
	        test2VarLabel2 = new javax.swing.JLabel();
	        test1Text = new javax.swing.JTextField();
	        test1Var1Text = new javax.swing.JTextField();
	        test2Text = new javax.swing.JTextField();
	        test1Var2Text = new javax.swing.JTextField();
	        test2Var1Text = new javax.swing.JTextField();
	        test2Var2Text = new javax.swing.JTextField();
	        relationsPanel = new javax.swing.JPanel();
	        relation1Label1 = new javax.swing.JLabel();
	        relation1VarLabel1 = new javax.swing.JLabel();
	        relation1VarLabel2 = new javax.swing.JLabel();
	        relation2Label = new javax.swing.JLabel();
	        relation2VarLabel1 = new javax.swing.JLabel();
	        relation2VarLabel2 = new javax.swing.JLabel();
	        relation1text = new Java2sAutoTextField(atlst);
	        relation1var1 = new javax.swing.JTextField();
	        relation1var2 = new javax.swing.JTextField();
	        relation2text = new Java2sAutoTextField(atlst);
	        relation2var1 = new javax.swing.JTextField();
	        relation2var2 = new javax.swing.JTextField();
	        propertyPanel = new javax.swing.JPanel();
	        property1Label = new javax.swing.JLabel();
	        property1ValueLabel = new javax.swing.JLabel();
	        property2Label = new javax.swing.JLabel();
	        property2ValueLabel = new javax.swing.JLabel();
	        property1text = new Java2sAutoTextField(atlst);
	        property1value = new javax.swing.JTextField();
	        property2text = new Java2sAutoTextField(atlst);
	        property2value = new javax.swing.JTextField();
	        directivePanel = new javax.swing.JPanel();
	        directiveLabel = new javax.swing.JLabel();
	        directiveValueLabel = new javax.swing.JLabel();
	        actionLabel = new javax.swing.JLabel();
	        actionValueLabel = new javax.swing.JLabel();
	        directiveText = new javax.swing.JTextField();
	        directiveValueText = new javax.swing.JTextField();
	        actionText = new javax.swing.JTextField();
	        actionValueText = new javax.swing.JTextField();

	        // setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

	        topPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Moonstone Rule Builder"));

	        ruleIDLabel.setText("RuleID");
	        ruleIDLabel.setName("RuleID"); // NOI18N

	        ruleIDTextField.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                ruleIDTextFieldActionPerformed(evt);
	            }
	        });

	        conceptLabel.setText("Concept");
	        conceptLabel.setName("Concept"); // NOI18N

	        stypeLabel.setText("SType");

	        constraintPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Constraints"));

	        orderedCheckbox.setText("Ordered");
	        orderedCheckbox.setSelected(true);

	        juxtaposedCheckbox.setText("Juxtaposed");

	        permitInterstitialCheckbox.setText("PermitInterstitial");

	        inhibitInterstitialCheckbox.setText("InhibitInterstitial");

	        javax.swing.GroupLayout constraintPanelLayout = new javax.swing.GroupLayout(constraintPanel);
	        constraintPanel.setLayout(constraintPanelLayout);
	        constraintPanelLayout.setHorizontalGroup(
	            constraintPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(constraintPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(orderedCheckbox)
	                .addGap(18, 18, 18)
	                .addComponent(juxtaposedCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(permitInterstitialCheckbox)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(inhibitInterstitialCheckbox)
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );
	        constraintPanelLayout.setVerticalGroup(
	            constraintPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(constraintPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(constraintPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(orderedCheckbox)
	                    .addComponent(juxtaposedCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                    .addComponent(permitInterstitialCheckbox)
	                    .addComponent(inhibitInterstitialCheckbox)))
	        );

	        patternPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Pattern"));

	        subpattern1Panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Position1"));

	        javax.swing.GroupLayout subpattern1PanelLayout = new javax.swing.GroupLayout(subpattern1Panel);
	        subpattern1Panel.setLayout(subpattern1PanelLayout);
	        subpattern1PanelLayout.setHorizontalGroup(
	            subpattern1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(subpattern1PanelLayout.createSequentialGroup()
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addGroup(subpattern1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
	                    .addComponent(subpatterntext_1_2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
	                    .addComponent(subpatterntext_1_3)
	                    .addComponent(subpatterntext_1_1, javax.swing.GroupLayout.Alignment.LEADING)))
	        );
	        subpattern1PanelLayout.setVerticalGroup(
	            subpattern1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(subpattern1PanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(subpatterntext_1_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(subpatterntext_1_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(subpatterntext_1_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );

	        subpattern2Panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Position2"));

	        javax.swing.GroupLayout subpattern2PanelLayout = new javax.swing.GroupLayout(subpattern2Panel);
	        subpattern2Panel.setLayout(subpattern2PanelLayout);
	        subpattern2PanelLayout.setHorizontalGroup(
	            subpattern2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(subpattern2PanelLayout.createSequentialGroup()
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addGroup(subpattern2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
	                    .addComponent(subpatterntext_2_2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
	                    .addComponent(subpatterntext_2_1, javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(subpatterntext_2_3)))
	        );
	        subpattern2PanelLayout.setVerticalGroup(
	            subpattern2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(subpattern2PanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(subpatterntext_2_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(subpatterntext_2_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(subpatterntext_2_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );

	        subpattern3Panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Position3"));

	        javax.swing.GroupLayout subpattern3PanelLayout = new javax.swing.GroupLayout(subpattern3Panel);
	        subpattern3Panel.setLayout(subpattern3PanelLayout);
	        subpattern3PanelLayout.setHorizontalGroup(
	            subpattern3PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(subpattern3PanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(subpattern3PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
	                    .addComponent(subpatterntext_3_2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
	                    .addComponent(subpatterntext_3_1, javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(subpatterntext_3_3))
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );
	        subpattern3PanelLayout.setVerticalGroup(
	            subpattern3PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(subpattern3PanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(subpatterntext_3_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(subpatterntext_3_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(subpatterntext_3_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );

	        subpattern4Panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Position4"));

	        javax.swing.GroupLayout subpattern4PanelLayout = new javax.swing.GroupLayout(subpattern4Panel);
	        subpattern4Panel.setLayout(subpattern4PanelLayout);
	        subpattern4PanelLayout.setHorizontalGroup(
	            subpattern4PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(subpattern4PanelLayout.createSequentialGroup()
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addGroup(subpattern4PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
	                    .addComponent(subpatterntext_4_2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
	                    .addComponent(subpatterntext_4_1, javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(subpatterntext_4_3)))
	        );
	        subpattern4PanelLayout.setVerticalGroup(
	            subpattern4PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(subpattern4PanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(subpatterntext_4_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(subpatterntext_4_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(subpatterntext_4_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );

	        subpattern5Panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Position5"));

	        javax.swing.GroupLayout subpattern5PanelLayout = new javax.swing.GroupLayout(subpattern5Panel);
	        subpattern5Panel.setLayout(subpattern5PanelLayout);
	        subpattern5PanelLayout.setHorizontalGroup(
	            subpattern5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addComponent(subpatterntext_5_3, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
	            .addComponent(subpatterntext_5_2)
	            .addComponent(subpatterntext_5_1)
	        );
	        subpattern5PanelLayout.setVerticalGroup(
	            subpattern5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(subpattern5PanelLayout.createSequentialGroup()
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addComponent(subpatterntext_5_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(subpatterntext_5_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(subpatterntext_5_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	        );

	        javax.swing.GroupLayout patternPanelLayout = new javax.swing.GroupLayout(patternPanel);
	        patternPanel.setLayout(patternPanelLayout);
	        patternPanelLayout.setHorizontalGroup(
	            patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(patternPanelLayout.createSequentialGroup()
	                .addGap(12, 12, 12)
	                .addComponent(subpattern1Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(subpattern2Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(subpattern3Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(subpattern4Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(subpattern5Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );
	        patternPanelLayout.setVerticalGroup(
	            patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(patternPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(subpattern5Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, patternPanelLayout.createSequentialGroup()
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addGroup(patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
	                    .addComponent(subpattern1Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                    .addGroup(patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
	                        .addComponent(subpattern2Panel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                        .addComponent(subpattern3Panel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                        .addComponent(subpattern4Panel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
	                .addGap(652, 652, 652))
	        );

	        subpattern4Panel.getAccessibleContext().setAccessibleName("Position4");

	        saveButton.setText("Save");

	        quitButton.setText("Quit");

	        clearButton.setText("Clear");
	        clearButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                clearButtonActionPerformed(evt);
	            }
	        });

	        testPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Tests"));

	        test1Label.setText("Test1");

	        test1VarLabel1.setText("Var1");

	        test1VarLabel2.setText("Var2");

	        test2Label.setText("Test2");

	        test2VarLabel1.setText("Var1");

	        test2VarLabel2.setText("Var2");

	        javax.swing.GroupLayout testPanelLayout = new javax.swing.GroupLayout(testPanel);
	        testPanel.setLayout(testPanelLayout);
	        testPanelLayout.setHorizontalGroup(
	            testPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(testPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(testPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(testPanelLayout.createSequentialGroup()
	                        .addComponent(test1Label)
	                        .addGap(195, 195, 195)
	                        .addComponent(test1VarLabel1))
	                    .addGroup(testPanelLayout.createSequentialGroup()
	                        .addComponent(test1Text, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                        .addComponent(test1Var1Text, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
	                .addGroup(testPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(testPanelLayout.createSequentialGroup()
	                        .addGap(16, 16, 16)
	                        .addComponent(test1VarLabel2))
	                    .addGroup(testPanelLayout.createSequentialGroup()
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                        .addComponent(test1Var2Text, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
	                .addGap(34, 34, 34)
	                .addGroup(testPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(test2Label, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(test2Text, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(testPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(test2VarLabel1)
	                    .addComponent(test2Var1Text, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addGroup(testPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(test2VarLabel2)
	                    .addComponent(test2Var2Text, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addGap(39, 39, 39))
	        );
	        testPanelLayout.setVerticalGroup(
	            testPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(testPanelLayout.createSequentialGroup()
	                .addGroup(testPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, testPanelLayout.createSequentialGroup()
	                        .addContainerGap()
	                        .addComponent(test1VarLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
	                    .addGroup(testPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                        .addComponent(test1Label)
	                        .addComponent(test1VarLabel1)
	                        .addComponent(test2Label)
	                        .addComponent(test2VarLabel1)
	                        .addComponent(test2VarLabel2)))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(testPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(test1Var1Text, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(test1Var2Text, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(test1Text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(test2Text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(test2Var1Text, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(test2Var2Text, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
	        );

	        relationsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Relations"));

	        relation1Label1.setText("Relation1");

	        relation1VarLabel1.setText("Var1");

	        relation1VarLabel2.setText("Var2");

	        relation2Label.setText("Relation2");

	        relation2VarLabel1.setText("Var1");

	        relation2VarLabel2.setText("Var2");

	        javax.swing.GroupLayout relationsPanelLayout = new javax.swing.GroupLayout(relationsPanel);
	        relationsPanel.setLayout(relationsPanelLayout);
	        relationsPanelLayout.setHorizontalGroup(
	            relationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(relationsPanelLayout.createSequentialGroup()
	                .addGroup(relationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(relationsPanelLayout.createSequentialGroup()
	                        .addContainerGap()
	                        .addComponent(relation1text, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
	                    .addGroup(relationsPanelLayout.createSequentialGroup()
	                        .addGap(14, 14, 14)
	                        .addComponent(relation1Label1)))
	                .addGap(12, 12, 12)
	                .addGroup(relationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(relation1var1, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(relation1VarLabel1))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(relationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(relation1VarLabel2)
	                    .addComponent(relation1var2, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addGroup(relationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(relationsPanelLayout.createSequentialGroup()
	                        .addGap(83, 83, 83)
	                        .addComponent(relation2text, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
	                    .addGroup(relationsPanelLayout.createSequentialGroup()
	                        .addGap(85, 85, 85)
	                        .addComponent(relation2Label)))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(relationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(relation2VarLabel1)
	                    .addComponent(relation2var1, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(relationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(relation2VarLabel2)
	                    .addComponent(relation2var2, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );
	        relationsPanelLayout.setVerticalGroup(
	            relationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(relationsPanelLayout.createSequentialGroup()
	                .addGroup(relationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(relation1Label1)
	                    .addComponent(relation1VarLabel1)
	                    .addComponent(relation1VarLabel2)
	                    .addComponent(relation2Label)
	                    .addComponent(relation2VarLabel1)
	                    .addComponent(relation2VarLabel2))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(relationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(relation1var1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(relation1var2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(relation1text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(relation2text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(relation2var1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(relation2var2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
	        );

	        propertyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Properties"));

	        property1Label.setText("Property1");

	        property1ValueLabel.setText("Value");

	        property2Label.setText("Property2");

	        property2ValueLabel.setText("Value");

	        javax.swing.GroupLayout propertyPanelLayout = new javax.swing.GroupLayout(propertyPanel);
	        propertyPanel.setLayout(propertyPanelLayout);
	        propertyPanelLayout.setHorizontalGroup(
	            propertyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(propertyPanelLayout.createSequentialGroup()
	                .addGroup(propertyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(propertyPanelLayout.createSequentialGroup()
	                        .addContainerGap()
	                        .addComponent(property1text, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
	                    .addGroup(propertyPanelLayout.createSequentialGroup()
	                        .addGap(14, 14, 14)
	                        .addComponent(property1Label)))
	                .addGap(12, 12, 12)
	                .addGroup(propertyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(property1ValueLabel)
	                    .addComponent(property1value, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addGroup(propertyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(propertyPanelLayout.createSequentialGroup()
	                        .addGap(68, 68, 68)
	                        .addComponent(property2text, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
	                    .addGroup(propertyPanelLayout.createSequentialGroup()
	                        .addGap(71, 71, 71)
	                        .addComponent(property2Label)))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(propertyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(property2ValueLabel)
	                    .addComponent(property2value, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );
	        propertyPanelLayout.setVerticalGroup(
	            propertyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(propertyPanelLayout.createSequentialGroup()
	                .addGroup(propertyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(property1Label)
	                    .addComponent(property1ValueLabel)
	                    .addComponent(property2Label)
	                    .addComponent(property2ValueLabel))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(propertyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(property1value, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(property1text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(property2text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(property2value, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
	        );

	        directivePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Directives"));

	        directiveLabel.setText("Directive");

	        directiveValueLabel.setText("Value");

	        actionLabel.setText("ActionOnAnnotation");

	        actionValueLabel.setText("Value");

	        javax.swing.GroupLayout directivePanelLayout = new javax.swing.GroupLayout(directivePanel);
	        directivePanel.setLayout(directivePanelLayout);
	        directivePanelLayout.setHorizontalGroup(
	            directivePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(directivePanelLayout.createSequentialGroup()
	                .addGroup(directivePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(directivePanelLayout.createSequentialGroup()
	                        .addContainerGap()
	                        .addComponent(directiveText, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
	                    .addGroup(directivePanelLayout.createSequentialGroup()
	                        .addGap(14, 14, 14)
	                        .addComponent(directiveLabel)))
	                .addGap(12, 12, 12)
	                .addGroup(directivePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(directiveValueLabel)
	                    .addComponent(directiveValueText, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addGap(74, 74, 74)
	                .addGroup(directivePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(actionLabel)
	                    .addComponent(actionText, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(directivePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(actionValueText, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(actionValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );
	        directivePanelLayout.setVerticalGroup(
	            directivePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(directivePanelLayout.createSequentialGroup()
	                .addGroup(directivePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(directiveLabel)
	                    .addComponent(directiveValueLabel)
	                    .addComponent(actionLabel)
	                    .addComponent(actionValueLabel))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(directivePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(directiveValueText, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(directiveText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(actionText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(actionValueText, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
	        );

	        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
	        topPanel.setLayout(topPanelLayout);
	        topPanelLayout.setHorizontalGroup(
	            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, topPanelLayout.createSequentialGroup()
	                .addGap(0, 0, Short.MAX_VALUE)
	                .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(quitButton)
	                .addGap(100, 100, 100))
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, topPanelLayout.createSequentialGroup()
	                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
	                    .addComponent(directivePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                    .addComponent(propertyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, topPanelLayout.createSequentialGroup()
	                        .addContainerGap()
	                        .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
	                            .addComponent(patternPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                            .addComponent(testPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                            .addComponent(relationsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
	                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, topPanelLayout.createSequentialGroup()
	                        .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
	                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, topPanelLayout.createSequentialGroup()
	                                .addContainerGap()
	                                .addComponent(constraintPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, topPanelLayout.createSequentialGroup()
	                                .addGap(18, 18, 18)
	                                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                                    .addComponent(ruleIDLabel)
	                                    .addComponent(conceptLabel))
	                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
	                                    .addGroup(topPanelLayout.createSequentialGroup()
	                                        .addComponent(conceptTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
	                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                                        .addComponent(stypeLabel)
	                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                                        .addComponent(stypeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE))
	                                    .addComponent(ruleIDTextField))))
	                        .addGap(0, 0, Short.MAX_VALUE)))
	                .addGap(77, 77, 77))
	        );
	        topPanelLayout.setVerticalGroup(
	            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(topPanelLayout.createSequentialGroup()
	                .addGap(15, 15, 15)
	                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(ruleIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(ruleIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(conceptLabel)
	                    .addComponent(stypeLabel)
	                    .addComponent(conceptTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(stypeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addGap(18, 18, 18)
	                .addComponent(constraintPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(patternPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(testPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(relationsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(propertyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(directivePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addGap(18, 18, 18)
	                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(saveButton)
	                    .addComponent(clearButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                    .addComponent(quitButton))
	                .addContainerGap())
	        );

	        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
	        getContentPane().setLayout(layout);
	        layout.setHorizontalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addComponent(topPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	        );
	        layout.setVerticalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );

	        pack();
	    }// </editor-fold>                        

	    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
	        // TODO add your handling code here:
	    }                                           

	    private void ruleIDTextFieldActionPerformed(java.awt.event.ActionEvent evt) {                                                
	        // TODO add your handling code here:
	    }                                               

	    /**
	     * @param args the command line arguments
	     */
	    public static void main(String args[]) {
	        /* Set the Nimbus look and feel */
	        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
	        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
	         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
	         */
	        try {
	            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
	                if ("Nimbus".equals(info.getName())) {
	                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
	                    break;
	                }
	            }
	        } catch (ClassNotFoundException ex) {
	            java.util.logging.Logger.getLogger(MoonstoneRuleBuilderPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	        } catch (InstantiationException ex) {
	            java.util.logging.Logger.getLogger(MoonstoneRuleBuilderPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	        } catch (IllegalAccessException ex) {
	            java.util.logging.Logger.getLogger(MoonstoneRuleBuilderPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
	            java.util.logging.Logger.getLogger(MoonstoneRuleBuilderPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	        }
	        //</editor-fold>

	        /* Create and display the form */
	        java.awt.EventQueue.invokeLater(new Runnable() {
	            public void run() {
	                new MoonstoneRuleBuilderPanel(null).setVisible(true);
	            }
	        });
	    }

	    // Variables declaration - do not modify                     
	    private javax.swing.JLabel actionLabel;
	    private javax.swing.JTextField actionText;
	    private javax.swing.JLabel actionValueLabel;
	    private javax.swing.JTextField actionValueText;
	    private javax.swing.ButtonGroup buttonGroup1;
	    private javax.swing.JButton clearButton;
	    private javax.swing.JLabel conceptLabel;
	    private javax.swing.JTextField conceptTextField;
	    private javax.swing.JPanel constraintPanel;
	    private javax.swing.JLabel directiveLabel;
	    private javax.swing.JPanel directivePanel;
	    private javax.swing.JTextField directiveText;
	    private javax.swing.JLabel directiveValueLabel;
	    private javax.swing.JTextField directiveValueText;
	    private javax.swing.JCheckBox inhibitInterstitialCheckbox;
	    private javax.swing.JCheckBox juxtaposedCheckbox;
	    private javax.swing.JCheckBox orderedCheckbox;
	    private javax.swing.JPanel patternPanel;
	    private javax.swing.JPanel patternPanel1;
	    private javax.swing.JCheckBox permitInterstitialCheckbox;
	    private javax.swing.JLabel property1Label;
	    private javax.swing.JLabel property1ValueLabel;
	    private javax.swing.JTextField property1text;
	    private javax.swing.JTextField property1value;
	    private javax.swing.JLabel property2Label;
	    private javax.swing.JLabel property2ValueLabel;
	    private javax.swing.JTextField property2text;
	    private javax.swing.JTextField property2value;
	    private javax.swing.JPanel propertyPanel;
	    private javax.swing.JButton quitButton;
	    private javax.swing.JLabel relation1Label1;
	    private javax.swing.JLabel relation1VarLabel1;
	    private javax.swing.JLabel relation1VarLabel2;
	    private javax.swing.JTextField relation1text;
	    private javax.swing.JTextField relation1var1;
	    private javax.swing.JTextField relation1var2;
	    private javax.swing.JLabel relation2Label;
	    private javax.swing.JLabel relation2VarLabel1;
	    private javax.swing.JLabel relation2VarLabel2;
	    private javax.swing.JTextField relation2text;
	    private javax.swing.JTextField relation2var1;
	    private javax.swing.JTextField relation2var2;
	    private javax.swing.JPanel relationsPanel;
	    private javax.swing.JLabel ruleIDLabel;
	    private javax.swing.JTextField ruleIDTextField;
	    private javax.swing.JButton saveButton;
	    private javax.swing.JLabel stypeLabel;
	    private javax.swing.JTextField stypeTextField;
	    private javax.swing.JPanel subpattern1Panel;
	    private javax.swing.JPanel subpattern1Panel1;
	    private javax.swing.JPanel subpattern2Panel;
	    private javax.swing.JPanel subpattern3Panel;
	    private javax.swing.JPanel subpattern4Panel;
	    private javax.swing.JPanel subpattern5Panel;
	    private javax.swing.JTextField subpatterntext_1_1;
	    private javax.swing.JTextField subpatterntext_1_2;
	    private javax.swing.JTextField subpatterntext_1_3;
	    private javax.swing.JTextField subpatterntext_1_4;
	    private javax.swing.JTextField subpatterntext_1_5;
	    private javax.swing.JTextField subpatterntext_1_6;
	    private javax.swing.JTextField subpatterntext_2_1;
	    private javax.swing.JTextField subpatterntext_2_2;
	    private javax.swing.JTextField subpatterntext_2_3;
	    private javax.swing.JTextField subpatterntext_3_1;
	    private javax.swing.JTextField subpatterntext_3_2;
	    private javax.swing.JTextField subpatterntext_3_3;
	    private javax.swing.JTextField subpatterntext_4_1;
	    private javax.swing.JTextField subpatterntext_4_2;
	    private javax.swing.JTextField subpatterntext_4_3;
	    private javax.swing.JTextField subpatterntext_5_1;
	    private javax.swing.JTextField subpatterntext_5_2;
	    private javax.swing.JTextField subpatterntext_5_3;
	    private javax.swing.JLabel test1Label;
	    private javax.swing.JTextField test1Text;
	    private javax.swing.JTextField test1Var1Text;
	    private javax.swing.JTextField test1Var2Text;
	    private javax.swing.JLabel test1VarLabel1;
	    private javax.swing.JLabel test1VarLabel2;
	    private javax.swing.JLabel test2Label;
	    private javax.swing.JTextField test2Text;
	    private javax.swing.JTextField test2Var1Text;
	    private javax.swing.JTextField test2Var2Text;
	    private javax.swing.JLabel test2VarLabel1;
	    private javax.swing.JLabel test2VarLabel2;
	    private javax.swing.JPanel testPanel;
	    private javax.swing.JPanel topPanel;
	    // End of variables declaration                   
	}
