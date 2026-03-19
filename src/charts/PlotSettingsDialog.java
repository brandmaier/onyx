package charts;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.Styler.ChartTheme;

public class PlotSettingsDialog extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	public static class PlotSettings {
		public enum SymbolType {
			CIRCLE, RECTANGLE, DIAMOND
		}

		public Color symbolColor = new Color(51, 102, 204);
		public SymbolType symbolType = SymbolType.CIRCLE;
		public int symbolSize = 8;
		public ChartTheme theme = Styler.ChartTheme.XChart;
		public boolean gridLinesVisible = true;
	}

	public interface PlotSettingsCallback {
		void onSettingsConfirmed(PlotSettings settings);
	}

	private final PlotSettings settings;
	private final PlotSettingsCallback callback;

	private final JButton colorButton;
	private final JComboBox<PlotSettings.SymbolType> symbolTypeCombo;
	private final JSpinner symbolSizeSpinner;
	private final JComboBox<String> themeCombo;
	private final JCheckBox gridCheck;
	private final JButton closeAndApplyButton;

	public PlotSettingsDialog(PlotSettings initialSettings, PlotSettingsCallback callback) {
		super("Plot Settings");
		this.settings = copy(initialSettings);
		this.callback = callback;

		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(6, 6, 6, 6);
		gc.anchor = GridBagConstraints.WEST;

		gc.gridx = 0;
		gc.gridy = 0;
		add(new JLabel("Symbol color"), gc);
		gc.gridx = 1;
		colorButton = new JButton("Select color");
		colorButton.setBackground(settings.symbolColor);
		colorButton.setOpaque(true);
		colorButton.addActionListener(this);
		add(colorButton, gc);

		gc.gridx = 0;
		gc.gridy = 1;
		add(new JLabel("Symbol type"), gc);
		gc.gridx = 1;
		symbolTypeCombo = new JComboBox<>(PlotSettings.SymbolType.values());
		symbolTypeCombo.setSelectedItem(settings.symbolType);
		add(symbolTypeCombo, gc);

		gc.gridx = 0;
		gc.gridy = 2;
		add(new JLabel("Symbol size"), gc);
		gc.gridx = 1;
		symbolSizeSpinner = new JSpinner(new SpinnerNumberModel(settings.symbolSize, 2, 30, 1));
		add(symbolSizeSpinner, gc);

		gc.gridx = 0;
		gc.gridy = 3;
		add(new JLabel("Theme"), gc);
		gc.gridx = 1;
		themeCombo = new JComboBox<>(new String[] { "default", "ggplot2", "other" });
		themeCombo.setSelectedItem(toThemeLabel(settings.theme));
		add(themeCombo, gc);

		gc.gridx = 0;
		gc.gridy = 4;
		add(new JLabel("Grid lines"), gc);
		gc.gridx = 1;
		gridCheck = new JCheckBox("Show grid lines", settings.gridLinesVisible);
		add(gridCheck, gc);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		closeAndApplyButton = new JButton("Close and apply");
		closeAndApplyButton.addActionListener(this);
		buttonPanel.add(closeAndApplyButton);

		gc.gridx = 0;
		gc.gridy = 5;
		gc.gridwidth = 2;
		gc.anchor = GridBagConstraints.EAST;
		add(buttonPanel, gc);

		pack();
		setLocationByPlatform(true);
		setVisible(true);
	}

	private static PlotSettings copy(PlotSettings source) {
		PlotSettings target = new PlotSettings();
		target.symbolColor = source.symbolColor;
		target.symbolType = source.symbolType;
		target.symbolSize = source.symbolSize;
		target.theme = source.theme;
		target.gridLinesVisible = source.gridLinesVisible;
		return target;
	}

	private static String toThemeLabel(Styler.ChartTheme theme) {
		if (theme == Styler.ChartTheme.GGPlot2) {
			return "ggplot2";
		}
		if (theme == Styler.ChartTheme.Matlab) {
			return "other";
		}
		return "default";
	}

	private static Styler.ChartTheme fromThemeLabel(String label) {
		if ("ggplot2".equals(label)) {
			return Styler.ChartTheme.GGPlot2;
		}
		if ("other".equals(label)) {
			return Styler.ChartTheme.Matlab;
		}
		return Styler.ChartTheme.XChart;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == colorButton) {
			Color selected = JColorChooser.showDialog(this, "Choose symbol color", settings.symbolColor);
			if (selected != null) {
				settings.symbolColor = selected;
				colorButton.setBackground(selected);
			}
			return;
		}

		if (e.getSource() == closeAndApplyButton) {
			settings.symbolType = (PlotSettings.SymbolType) symbolTypeCombo.getSelectedItem();
			settings.symbolSize = ((Number) symbolSizeSpinner.getValue()).intValue();
			settings.theme = fromThemeLabel((String) themeCombo.getSelectedItem());
			settings.gridLinesVisible = gridCheck.isSelected();
			if (callback != null) {
				callback.onSettingsConfirmed(copy(settings));
			}
			dispose();
		}
	}
}
