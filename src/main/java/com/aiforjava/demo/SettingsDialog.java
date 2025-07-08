package com.aiforjava.demo;

import com.aiforjava.llm.ModelParams;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsDialog extends JDialog {

    private JSlider temperatureSlider;
    private JSlider topPSlider;
    private JTextField maxTokensField;
    private JComboBox<String> modelSelectionComboBox;
    private JButton applyButton;
    private JButton cancelButton;

    private ModelParams currentModelParams;
    private String currentModelName;

    // UI Constants (matching SwingChatbot for consistency)
    private static final Color BACKGROUND_COLOR = new Color(48, 48, 48);
    private static final Color FOREGROUND_COLOR = new Color(172, 172, 172);
    private static final Color ACCENT_COLOR = new Color(0, 0, 0); // Cornflower Blue
    private static final Color BUTTON_COLOR = new Color(0, 0, 0, 171);
    private static final Font INPUT_FONT = new Font("Consolas", Font.BOLD, 16);
    private static final Font BUTTON_FONT = new Font("Consolas", Font.BOLD, 14);

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(FOREGROUND_COLOR);
        label.setFont(BUTTON_FONT);
        return label;
    }

    public SettingsDialog(JFrame parent, ModelParams initialParams, String initialModelName) {
        super(parent, "LLM Settings", true); // Modal dialog
        setSize(400, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        this.currentModelParams = initialParams;
        this.currentModelName = initialModelName;

        JPanel settingsPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        settingsPanel.setBackground(BACKGROUND_COLOR);
        settingsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR), "LLM Settings",
                TitledBorder.LEFT, TitledBorder.TOP, BUTTON_FONT, FOREGROUND_COLOR));

        // Temperature Slider
        settingsPanel.add(createLabel("Temperature:"));
        temperatureSlider = new JSlider(0, 100, (int) (initialParams.getTemperature() * 100));
        temperatureSlider.setBackground(BACKGROUND_COLOR);
        temperatureSlider.setForeground(FOREGROUND_COLOR);
        temperatureSlider.setMajorTickSpacing(25);
        temperatureSlider.setMinorTickSpacing(5);
        temperatureSlider.setPaintTicks(true);
        temperatureSlider.setPaintLabels(true);
        settingsPanel.add(temperatureSlider);

        // TopP Slider
        settingsPanel.add(createLabel("Top P:"));
        topPSlider = new JSlider(0, 100, (int) (initialParams.getTopP() * 100));
        topPSlider.setBackground(BACKGROUND_COLOR);
        topPSlider.setForeground(FOREGROUND_COLOR);
        topPSlider.setMajorTickSpacing(25);
        topPSlider.setMinorTickSpacing(5);
        topPSlider.setPaintTicks(true);
        topPSlider.setPaintLabels(true);
        settingsPanel.add(topPSlider);

        // Max Tokens Field
        settingsPanel.add(createLabel("Max Tokens:"));
        maxTokensField = new JTextField(String.valueOf(initialParams.getMaxTokens()));
        maxTokensField.setBackground(BUTTON_COLOR);
        maxTokensField.setForeground(FOREGROUND_COLOR);
        maxTokensField.setFont(INPUT_FONT);
        maxTokensField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1), // Accent border
                BorderFactory.createEmptyBorder(5, 10, 5, 10) // Inner padding
        ));
        settingsPanel.add(maxTokensField);

        // Model Name Dropdown
        settingsPanel.add(createLabel("Model Name:"));
        String[] models = {"google/gemma-3-1b", "qwen/qwen3-4b"}; // Hardcoded models
        modelSelectionComboBox = new JComboBox<>(models);
        modelSelectionComboBox.setSelectedItem(initialModelName);
        modelSelectionComboBox.setBackground(BUTTON_COLOR);
        modelSelectionComboBox.setForeground(FOREGROUND_COLOR);
        modelSelectionComboBox.setFont(INPUT_FONT);
        modelSelectionComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1), // Accent border
                BorderFactory.createEmptyBorder(5, 10, 5, 10) // Inner padding
        ));
        settingsPanel.add(modelSelectionComboBox);

        add(settingsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        applyButton = new JButton("Apply");
        applyButton.setBackground(ACCENT_COLOR);
        applyButton.setForeground(Color.WHITE);
        applyButton.setFont(BUTTON_FONT);
        applyButton.setFocusPainted(false);
        applyButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        cancelButton = new JButton("Cancel");
        cancelButton.setBackground(BUTTON_COLOR);
        cancelButton.setForeground(FOREGROUND_COLOR);
        cancelButton.setFont(BUTTON_FONT);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applySettings();
                setVisible(false);
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
    }

    private void applySettings() {
        try {
            double temperature = (double) temperatureSlider.getValue() / 100.0;
            double topP = (double) topPSlider.getValue() / 100.0;
            int maxTokens = Integer.parseInt(maxTokensField.getText());
            String modelName = (String) modelSelectionComboBox.getSelectedItem();

            currentModelParams = new ModelParams.Builder()
                    .setTemperature(temperature)
                    .setMaxTokens(maxTokens)
                    .setTopP(topP)
                    .build();
            currentModelName = modelName;

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format for Max Tokens.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public ModelParams getAppliedModelParams() {
        return currentModelParams;
    }

    public String getAppliedModelName() {
        return currentModelName;
    }
}
