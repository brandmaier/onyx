package gui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import gui.graph.Edge;
import gui.graph.Node;
import gui.views.ModelView;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class LatentMeanWizard extends Dialog {

	gui.Desktop desktop;
	
    public enum ApproachType { SMM, MIMIC }

    // Wizard state
    private int currentStep = 0;

    // Collected inputs
    private Integer indicatorCount = null;
    private final List<String> indicatorNames = new ArrayList<>();
    private String latentFactorName = "";
    private ApproachType approachType = ApproachType.SMM;
    private String groupingVariableName = "group";

    // Step panels (cards)
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    // Buttons
    private final JButton backBtn = new JButton("Back");
    private final JButton nextBtn = new JButton("Next");
    private final JButton finishBtn = new JButton("Create SEM");
    private final JButton cancelBtn = new JButton("Cancel");

    // Step 1 components
    private final JSpinner indicatorCountSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 200, 1));

    // Step 2 components (dynamic indicators UI)
    private final JPanel indicatorListPanel = new JPanel();
    private final JScrollPane indicatorScroll = new JScrollPane(indicatorListPanel);

    // Step 3 components
    private final JTextField latentFactorField = new JTextField("\\eta");

    // Step 4 components
    private final JRadioButton smmRadio = new JRadioButton("SMM");
    private final JRadioButton mimicRadio = new JRadioButton("MIMIC");

    // Step 5 components
    private final JTextField groupingVarField = new JTextField("group");

    public LatentMeanWizard(gui.Desktop desktop) {
    	
        super("Latent Mean Difference");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.desktop = desktop;
        
        buildUi();
        wireActions();

        setMinimumSize(new Dimension(560, 420));
       // setLocationRelativeTo(desktop);
        goToStep(0);
        
        setVisible(true);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        // Title area
        JLabel title = new JLabel("Latent Mean Modeling Wizard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        root.add(title, BorderLayout.NORTH);

        // Card area
        cardPanel.add(step1Panel(), "step1");
        cardPanel.add(step2Panel(), "step2");
        cardPanel.add(step3Panel(), "step3");
        cardPanel.add(step4Panel(), "step4");
        cardPanel.add(step5Panel(), "step5");
        root.add(cardPanel, BorderLayout.CENTER);

        // Buttons area
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.add(backBtn);
        btnBar.add(nextBtn);
        btnBar.add(finishBtn);
        btnBar.add(cancelBtn);
        root.add(btnBar, BorderLayout.SOUTH);

        // Initial button states
        finishBtn.setEnabled(false);
    }

    private JPanel step1Panel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Step 1 — Number of indicators"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        form.add(new JLabel("Number of indicators:"), gc);

        gc.gridx = 1;
        indicatorCountSpinner.setPreferredSize(new Dimension(100, indicatorCountSpinner.getPreferredSize().height));
        form.add(indicatorCountSpinner, gc);

        p.add(form, BorderLayout.NORTH);

        JTextArea hint = new JTextArea(
                "Choose how many indicators you want. In the next step you will enter their names.");
        hint.setLineWrap(true);
        hint.setWrapStyleWord(true);
        hint.setEditable(false);
        hint.setOpaque(false);
        p.add(hint, BorderLayout.CENTER);

        return p;
    }

    private JPanel step2Panel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Step 2 — Indicator names"));

        indicatorListPanel.setLayout(new BoxLayout(indicatorListPanel, BoxLayout.Y_AXIS));
        indicatorScroll.setBorder(BorderFactory.createEmptyBorder());
        p.add(indicatorScroll, BorderLayout.CENTER);

        JTextArea hint = new JTextArea(
                "Enter a name for each indicator (one per field).");
        hint.setLineWrap(true);
        hint.setWrapStyleWord(true);
        hint.setEditable(false);
        hint.setOpaque(false);
        p.add(hint, BorderLayout.NORTH);

        return p;
    }

    private JPanel step3Panel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Step 3 — Latent factor"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        form.add(new JLabel("Latent factor name:"), gc);

        gc.gridx = 1;
        latentFactorField.setColumns(24);
        form.add(latentFactorField, gc);

        p.add(form, BorderLayout.NORTH);
        return p;
    }

    private JPanel step4Panel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Step 4 — Approach"));

        ButtonGroup g = new ButtonGroup();
        g.add(smmRadio);
        g.add(mimicRadio);
        smmRadio.setSelected(true);

        JPanel choices = new JPanel(new GridLayout(0, 1, 4, 4));
        choices.add(smmRadio);
        choices.add(mimicRadio);

        JTextArea hint = new JTextArea(
                "Select the approach type to use for the SEM.");
        hint.setLineWrap(true);
        hint.setWrapStyleWord(true);
        hint.setEditable(false);
        hint.setOpaque(false);

        p.add(hint, BorderLayout.NORTH);
        p.add(choices, BorderLayout.CENTER);
        return p;
    }

    private JPanel step5Panel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Step 5 — Grouping variable"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        form.add(new JLabel("Grouping variable name:"), gc);

        gc.gridx = 1;
        groupingVarField.setColumns(24);
        form.add(groupingVarField, gc);

        p.add(form, BorderLayout.NORTH);

        JTextArea hint = new JTextArea(
                "Click “Create SEM” to finish.");
        hint.setLineWrap(true);
        hint.setWrapStyleWord(true);
        hint.setEditable(false);
        hint.setOpaque(false);
        p.add(hint, BorderLayout.CENTER);

        return p;
    }

    private void wireActions() {
        backBtn.addActionListener(e -> goToStep(currentStep - 1));
        nextBtn.addActionListener(e -> {
            if (validateAndStoreCurrentStep()) {
                goToStep(currentStep + 1);
            }
        });

        finishBtn.addActionListener(this);

        cancelBtn.addActionListener(e -> dispose());

        // update state when toggling approach
        smmRadio.addActionListener(e -> approachType = ApproachType.SMM);
        mimicRadio.addActionListener(e -> approachType = ApproachType.MIMIC);
    }

    private void goToStep(int stepIndex) {
        stepIndex = Math.max(0, Math.min(4, stepIndex));
        currentStep = stepIndex;

        // When entering step 2, rebuild indicator fields if count changed
        if (currentStep == 1) {
            indicatorCount = (Integer) indicatorCountSpinner.getValue();
            rebuildIndicatorFields(indicatorCount);
        }

        cardLayout.show(cardPanel, "step" + (currentStep + 1));

        backBtn.setEnabled(currentStep > 0);
        nextBtn.setEnabled(currentStep < 4);
        finishBtn.setEnabled(currentStep == 4);
        if (currentStep == 4) finishBtn.requestFocus();
    }

    private void rebuildIndicatorFields(int count) {
        indicatorListPanel.removeAll();

        // Create (or keep) a list of text fields. For simplicity, rebuild fresh each time.
        for (int i = 0; i < count; i++) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            JLabel label = new JLabel("Indicator " + (i + 1) + ":");
            JTextField tf = new JTextField();
            tf.setName("indicatorField" + i);
            tf.setText( "x"+(i+1));

            row.add(label, BorderLayout.WEST);
            row.add(tf, BorderLayout.CENTER);
            row.setBorder(new EmptyBorder(4, 4, 4, 4));
            indicatorListPanel.add(row);
        }

        indicatorListPanel.revalidate();
        indicatorListPanel.repaint();

        SwingUtilities.invokeLater(() -> indicatorScroll.getVerticalScrollBar().setValue(0));
    }

    private boolean validateAndStoreCurrentStep() {
        switch (currentStep) {
            case 0: {
                indicatorCount = (Integer) indicatorCountSpinner.getValue();
                if (indicatorCount == null || indicatorCount < 1) {
                    showError("Please choose at least 1 indicator.");
                    return false;
                }
                return true;
            }
            case 1: {
                indicatorNames.clear();
                for (Component c : indicatorListPanel.getComponents()) {
                    if (c instanceof JPanel row) {
                        Component center = ((BorderLayout) row.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                        if (center instanceof JTextField tf) {
                            String name = tf.getText().trim();
                            if (name.isEmpty()) {
                                showError("Please provide a name for every indicator.");
                                return false;
                            }
                            indicatorNames.add(name);
                        }
                    }
                }
                if (indicatorNames.isEmpty()) {
                    showError("Please enter indicator names.");
                    return false;
                }
                return true;
            }
            case 2: {
                latentFactorName = latentFactorField.getText().trim();
                if (latentFactorName.isEmpty()) {
                    showError("Please enter the latent factor name.");
                    return false;
                }
                return true;
            }
            case 3: {
                // approachType is updated by radio listeners, but validate anyway
                approachType = smmRadio.isSelected() ? ApproachType.SMM : ApproachType.MIMIC;
                return true;
            }
            case 4: {
                groupingVariableName = groupingVarField.getText().trim();
                if (groupingVariableName.isEmpty()) {
                    showError("Please enter the grouping variable name.");
                    return false;
                }
                return true;
            }
            default:
                return true;
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation error", JOptionPane.ERROR_MESSAGE);
    }

    public SemWizardResult getResult() {
        // Create a snapshot of values
        SemWizardResult r = new SemWizardResult();
        r.indicatorCount = indicatorCount != null ? indicatorCount : 0;
        r.indicatorNames = new ArrayList<>(indicatorNames);
        r.latentFactorName = latentFactorName;
        r.approachType = approachType;
        r.groupingVariableName = groupingVariableName;
        return r;
    }

    // Simple result holder (UI-only)
    public static class SemWizardResult {
        public int indicatorCount;
        public List<String> indicatorNames;
        public String latentFactorName;
        public ApproachType approachType;
        public String groupingVariableName;

        public String toMultilineString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Number of indicators: ").append(indicatorCount).append("\n");
            sb.append("Indicator names: ").append(indicatorNames).append("\n");
            sb.append("Latent factor: ").append(latentFactorName).append("\n");
            sb.append("Approach: ").append(approachType).append("\n");
            sb.append("Grouping variable: ").append(groupingVariableName).append("\n");
            return sb.toString();
        }
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == finishBtn) {
			ModelView mv = new ModelView(desktop);
			desktop.add(mv);
			
			ArrayList<String> errorNames = new ArrayList<String>();
			int num_obs = indicatorNames.size();
			for (int i=0; i < num_obs; i++)
				errorNames.add("e"+
						(i+1));
			
			if (smmRadio.isSelected()) {
			
				mv.setName("SMM");
				
				ModelFactory.createFactorModel(mv.getModelRequestInterface(),
						num_obs, latentFactorName,
						indicatorNames, errorNames, 0, 70, 100, false,false, "factor", groupingVariableName);
				
				int maxx = mv.getGraph().getNodeMaxX();				
				
				ModelFactory.createFactorModel(mv.getModelRequestInterface(),
						num_obs, latentFactorName,
						indicatorNames, errorNames, 1, maxx+70, 100, false, true, "factor2",groupingVariableName);
				
				Node node = mv.getGraph().getFirstNodeByTag("factor2");
				Node tri = new Node();
				tri.setTriangle(true);
				tri.setX(node.getX()+150);
				tri.setY(node.getY());
				mv.getModelRequestInterface().requestAddNode(tri);
				
				Edge edge = new Edge(tri, node);
				edge.setFixed(false);
				mv.getModelRequestInterface().requestAddEdge(edge);
				
				mv.setSize( 200+indicatorCount*80*2+70, 500);
				
			} else {
				
				mv.setName("MIMIC");
				
				ModelFactory.createFactorModel(mv.getModelRequestInterface(),
						num_obs, latentFactorName,
						indicatorNames, errorNames, -1, 70, 100, false, false, "factor", groupingVariableName);
				
				Node lf = mv.getGraph().getFirstNodeByTag("factor");
				
				if (lf != null) {
				System.out.println("Grouping variable name"+groupingVariableName);
				groupingVariableName = "group";
				Node pred = new Node(groupingVariableName, false);
				pred.setIsLatent(false);
				pred.setX(lf.getX()+100+indicatorCount*40);
				pred.setY(lf.getY());
				
				mv.getModelRequestInterface().requestAddNode(pred);
				
				Edge var = new Edge(pred,pred);
				var.setDoubleHeaded(true);
				var.setFixed(false);
				mv.getModelRequestInterface().requestAddEdge(var);
				
				
				Edge edge = new Edge(pred, lf);
				edge.setDoubleHeaded(false);
				edge.setFixed(false);
				
				mv.getModelRequestInterface().requestAddEdge(edge);
				
				
				Node mn = new Node();
				mn.setTriangle(true);
				mn.setPosition( pred.getX(), 430);
				
				mv.getModelRequestInterface().requestAddNode(mn);
				
				Edge mned = new Edge(mn, pred);
				mned.setFixed(false);
				
				mv.getModelRequestInterface().requestAddEdge(mned);
				
				mv.setSize( 200+indicatorCount*80+70, 500);
				} else {
					System.out.println("ERROR : Could not find node"+ lf);
				}
			}
		}
		
	}

    
}