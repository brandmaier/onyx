package charts;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class LongitudinalPlotSettingsDialog extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	public static class PlotSettings {
		public enum LineColorMode {
			USER_DEFINED, COLORED_PER_OBSERVATION
		}

		public LineColorMode lineColorMode = LineColorMode.COLORED_PER_OBSERVATION;
		public Color lineColor = new Color(51, 102, 204);
		public float lineThickness = 2.0f;
		public String xAxisLabel = "X";
		public String yAxisLabel = "Y";
	}

	public interface PlotSettingsCallback {
		void onSettingsConfirmed(PlotSettings settings);
	}

	private final PlotSettings settings;
	private final PlotSettingsCallback callback;

	private final JComboBox<PlotSettings.LineColorMode> lineColorModeCombo;
	private final JButton lineColorButton;
	private final JSpinner lineThicknessSpinner;
	private final JTextField xAxisLabelField;
	private final JTextField yAxisLabelField;
	private final JButton closeAndApplyButton;

	public LongitudinalPlotSettingsDialog(PlotSettings initialSettings, PlotSettingsCallback callback) {
		super("Plot Settings");
		this.settings = copy(initialSettings);
		this.callback = callback;

		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(6, 6, 6, 6);
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weightx = 1.0;

		gc.gridx = 0;
		gc.gridy = 0;
		add(new JLabel("Line color type"), gc);
		gc.gridx = 1;
		lineColorModeCombo = new JComboBox<>(PlotSettings.LineColorMode.values());
		lineColorModeCombo.setSelectedItem(settings.lineColorMode);
		lineColorModeCombo.addActionListener(this);
		add(lineColorModeCombo, gc);

		gc.gridx = 0;
		gc.gridy = 1;
		add(new JLabel("Line color"), gc);
		gc.gridx = 1;
		lineColorButton = new JButton("Select color");
		lineColorButton.setBackground(settings.lineColor);
		lineColorButton.setOpaque(true);
		lineColorButton.addActionListener(this);
		add(lineColorButton, gc);

		gc.gridx = 0;
		gc.gridy = 2;
		add(new JLabel("Line thickness"), gc);
		gc.gridx = 1;
		lineThicknessSpinner = new JSpinner(new SpinnerNumberModel((double) settings.lineThickness, 0.5, 10.0, 0.5));
		add(lineThicknessSpinner, gc);

		gc.gridx = 0;
		gc.gridy = 3;
		add(new JLabel("X axis label"), gc);
		gc.gridx = 1;
		xAxisLabelField = new JTextField(settings.xAxisLabel, 16);
		add(xAxisLabelField, gc);

		gc.gridx = 0;
		gc.gridy = 4;
		add(new JLabel("Y axis label"), gc);
		gc.gridx = 1;
		yAxisLabelField = new JTextField(settings.yAxisLabel, 16);
		add(yAxisLabelField, gc);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		closeAndApplyButton = new JButton("Close and apply");
		closeAndApplyButton.addActionListener(this);
		buttonPanel.add(closeAndApplyButton);

		gc.gridx = 0;
		gc.gridy = 5;
		gc.gridwidth = 2;
		gc.anchor = GridBagConstraints.EAST;
		add(buttonPanel, gc);

		updateLineColorEnabledState();
		pack();
		setLocationByPlatform(true);
		setVisible(true);
	}

	private static PlotSettings copy(PlotSettings source) {
		PlotSettings target = new PlotSettings();
		target.lineColorMode = source.lineColorMode;
		target.lineColor = source.lineColor;
		target.lineThickness = source.lineThickness;
		target.xAxisLabel = source.xAxisLabel;
		target.yAxisLabel = source.yAxisLabel;
		return target;
	}

	private void updateLineColorEnabledState() {
		PlotSettings.LineColorMode selectedMode = (PlotSettings.LineColorMode) lineColorModeCombo.getSelectedItem();
		lineColorButton.setEnabled(selectedMode == PlotSettings.LineColorMode.USER_DEFINED);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == lineColorModeCombo) {
			updateLineColorEnabledState();
			return;
		}

		if (e.getSource() == lineColorButton) {
			Color selected = JColorChooser.showDialog(this, "Choose line color", settings.lineColor);
			if (selected != null) {
				settings.lineColor = selected;
				lineColorButton.setBackground(selected);
			}
			return;
		}

		if (e.getSource() == closeAndApplyButton) {
			settings.lineColorMode = (PlotSettings.LineColorMode) lineColorModeCombo.getSelectedItem();
			settings.lineThickness = ((Number) lineThicknessSpinner.getValue()).floatValue();
			settings.xAxisLabel = xAxisLabelField.getText();
			settings.yAxisLabel = yAxisLabelField.getText();

			if (callback != null) {
				callback.onSettingsConfirmed(copy(settings));
			}
			dispose();
		}
	}
}
