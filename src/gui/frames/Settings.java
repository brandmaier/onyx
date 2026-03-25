/*
* Copyright 2023 by Timo von Oertzen and Andreas M. Brandmaier
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package gui.frames;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import engine.Preferences;
import gui.Desktop;
import gui.i18n.I18n;
public class Settings extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final String[] BACKGROUND_ITEMS = new String[] {
            "None", "Globe", "Crumpled Paper", "Board", "Desk", WelcomeFrame.OMEGA + "nyx" };

    private static final String LANGUAGE_ENGLISH_LABEL = "English";
    private static final String LANGUAGE_GERMAN_LABEL = "Deutsch";

    private final Desktop desktop;
    private final JButton ok;
    private final JComboBox<String> backgroundBox;
    private final JComboBox<String> languageBox;

    public Settings(Desktop desktop) {
        this.desktop = desktop;

        setTitle(I18n.tr("settings.title", "Settings"));

        backgroundBox = new JComboBox<String>(BACKGROUND_ITEMS);
        backgroundBox.addActionListener(this);

        languageBox = new JComboBox<String>(new String[] { LANGUAGE_ENGLISH_LABEL, LANGUAGE_GERMAN_LABEL });
        languageBox.addActionListener(this);

        ok = new JButton(I18n.tr("settings.done", "Done"));
        ok.addActionListener(this);

        JLabel backgroundLabel = new JLabel(I18n.tr("settings.backgroundImage", "Background Image"));
        JLabel languageLabel = new JLabel(I18n.tr("settings.language", "Language"));

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        add(backgroundLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        add(backgroundBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(languageLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        add(languageBox, gbc);

        gbc.gridy = 2;
        JPanel sep = new JPanel();
        sep.setSize(1, 40);
        add(sep, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        add(ok, gbc);

        backgroundBox.setSelectedIndex(resolveBackgroundIndex());
        languageBox.setSelectedItem(getLanguageLabel(Preferences.getAsString("Language")));

        setSize(340, 220);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();

        if (e.getSource() == ok) {
            this.dispose();
        }
    }

    public void update() {
        int idx = backgroundBox.getSelectedIndex();

        if (idx == 0) {
            Preferences.set("BackgroundImage", "");
        } else {
            Preferences.set("BackgroundImage", "#" + idx);
        }

        String selectedLanguageCode = getLanguageCode((String) languageBox.getSelectedItem());
        Preferences.set("Language", selectedLanguageCode);
        I18n.setApplicationLocale(selectedLanguageCode);

        desktop.updateBackgroundImage();
    }

    private int resolveBackgroundIndex() {
        String backgroundImage = Preferences.getAsString("BackgroundImage");
        if (backgroundImage == null || backgroundImage.length() == 0) {
            return 0;
        }
        if (backgroundImage.startsWith("#")) {
            try {
                int idx = Integer.parseInt(backgroundImage.substring(1));
                if (idx >= 0 && idx < BACKGROUND_ITEMS.length) {
                    return idx;
                }
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
        return 0;
    }

    private String getLanguageLabel(String languageCode) {
        if (I18n.LANGUAGE_GERMAN.equalsIgnoreCase(languageCode)) {
            return LANGUAGE_GERMAN_LABEL;
        }
        return LANGUAGE_ENGLISH_LABEL;
    }

    private String getLanguageCode(String languageLabel) {
        if (LANGUAGE_GERMAN_LABEL.equals(languageLabel)) {
            return I18n.LANGUAGE_GERMAN;
        }
        return I18n.LANGUAGE_ENGLISH;
    }
}