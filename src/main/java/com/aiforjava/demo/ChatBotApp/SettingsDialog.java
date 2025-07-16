package com.aiforjava.demo.ChatBotApp;

import com.aiforjava.llm.models.ModelParams;
import com.aiforjava.llm.models.ModelRegistry;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class SettingsDialog extends JDialog {

    private JSlider temperatureSlider;
    private JSlider topPSlider;
    private JSlider frequencyPenaltySlider;
    private JSlider presencePenaltySlider;
    private JTextField maxTokensField;
    private JComboBox<String> modelSelectionComboBox;
    private JButton applyButton;
    private JButton cancelButton;

    private ModelParams currentModelParams;
    private String currentModelName;
    private boolean settingsApplied = false;

    // UI Constants (matching SwingChatbot for consistency)
    private static final Color BACKGROUND_COLOR = new Color(36, 36, 36);
    private static final Color FOREGROUND_COLOR = new Color(220, 220, 220);
    private static final Color ACCENT_COLOR = new Color(66, 135, 245); // Vibrant Blue
    private static final Color BUTTON_COLOR = new Color(55, 55, 55);
    private static final Font INPUT_FONT = new Font("Consolas", Font.BOLD, 16);
    private static final Font BUTTON_FONT = new Font("Consolas", Font.BOLD, 14);

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(FOREGROUND_COLOR);
        label.setFont(BUTTON_FONT);
        return label;
    }

    public SettingsDialog(JFrame parent, ModelParams initialParams, String initialModelName) {
        super(parent, "AI Settings", true); // Modal dialog
        setSize(600, 500); // Increased height to accommodate new sliders
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Set the application icon
        try {
            //Image icon = Toolkit.getDefaultToolkit().getImage(SwingChatbot.class.getResource("/_icon.jpg"));
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/_icon.jpg")));
            this.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Error loading icon: " + e.getMessage());
        }

        this.currentModelParams = initialParams;
        this.currentModelName = initialModelName;

        JPanel settingsPanel = new JPanel(new GridLayout(7, 2, 10, 10)); // Changed to 7 rows
        settingsPanel.setBackground(BACKGROUND_COLOR);
        settingsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR), "AI Settings",
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
        // Custom labels for Temperature
        java.util.Hashtable<Integer, JLabel> tempLabelTable = new java.util.Hashtable<>();
        tempLabelTable.put(0, new JLabel("0.0"));
        tempLabelTable.put(25, new JLabel("0.25"));
        tempLabelTable.put(50, new JLabel("0.5"));
        tempLabelTable.put(75, new JLabel("0.75"));
        tempLabelTable.put(100, new JLabel("1.0"));
        temperatureSlider.setLabelTable(tempLabelTable);
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
        // Custom labels for Top P
        java.util.Hashtable<Integer, JLabel> topPLabelTable = new java.util.Hashtable<>();
        topPLabelTable.put(0, new JLabel("0.0"));
        topPLabelTable.put(25, new JLabel("0.25"));
        topPLabelTable.put(50, new JLabel("0.5"));
        topPLabelTable.put(75, new JLabel("0.75"));
        topPLabelTable.put(100, new JLabel("1.0"));
        topPSlider.setLabelTable(topPLabelTable);
        settingsPanel.add(topPSlider);

        // Frequency Penalty Slider
        settingsPanel.add(createLabel("Frequency Penalty:"));
        frequencyPenaltySlider = new JSlider(-200, 200, (int) (initialParams.getFrequencyPenalty() * 100));
        frequencyPenaltySlider.setBackground(BACKGROUND_COLOR);
        frequencyPenaltySlider.setForeground(FOREGROUND_COLOR);
        frequencyPenaltySlider.setMajorTickSpacing(100);
        frequencyPenaltySlider.setMinorTickSpacing(25);
        frequencyPenaltySlider.setPaintTicks(true);
        frequencyPenaltySlider.setPaintLabels(true);
        java.util.Hashtable<Integer, JLabel> freqLabelTable = new java.util.Hashtable<>();
        freqLabelTable.put(-200, new JLabel("-2.0"));
        freqLabelTable.put(-100, new JLabel("-1.0"));
        freqLabelTable.put(0, new JLabel("0.0"));
        freqLabelTable.put(100, new JLabel("1.0"));
        freqLabelTable.put(200, new JLabel("2.0"));
        frequencyPenaltySlider.setLabelTable(freqLabelTable);
        settingsPanel.add(frequencyPenaltySlider);

        // Presence Penalty Slider
        settingsPanel.add(createLabel("Presence Penalty:"));
        presencePenaltySlider = new JSlider(-200, 200, (int) (initialParams.getPresencePenalty() * 100));
        presencePenaltySlider.setBackground(BACKGROUND_COLOR);
        presencePenaltySlider.setForeground(FOREGROUND_COLOR);
        presencePenaltySlider.setMajorTickSpacing(100);
        presencePenaltySlider.setMinorTickSpacing(25);
        presencePenaltySlider.setPaintTicks(true);
        presencePenaltySlider.setPaintLabels(true);
        java.util.Hashtable<Integer, JLabel> presLabelTable = new java.util.Hashtable<>();
        presLabelTable.put(-200, new JLabel("-2.0"));
        presLabelTable.put(-100, new JLabel("-1.0"));
        presLabelTable.put(0, new JLabel("0.0"));
        presLabelTable.put(100, new JLabel("1.0"));
        presLabelTable.put(200, new JLabel("2.0"));
        presencePenaltySlider.setLabelTable(presLabelTable);
        settingsPanel.add(presencePenaltySlider);

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
        String[] models = ModelRegistry.getAllModelNames().toArray(new String[0]);
        modelSelectionComboBox = new JComboBox<>(models);
        modelSelectionComboBox.setSelectedItem(initialModelName);
        modelSelectionComboBox.setBackground(BUTTON_COLOR);
        modelSelectionComboBox.setForeground(FOREGROUND_COLOR);
        modelSelectionComboBox.setFont(INPUT_FONT);
        modelSelectionComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 3), // Accent border
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
            double frequencyPenalty = (double) frequencyPenaltySlider.getValue() / 100.0;
            double presencePenalty = (double) presencePenaltySlider.getValue() / 100.0;
            int maxTokens = Integer.parseInt(maxTokensField.getText());
            if (maxTokens <= 0) {
                JOptionPane.showMessageDialog(this, "Max Tokens must be a positive integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String modelName = (String) modelSelectionComboBox.getSelectedItem();

            currentModelParams = new ModelParams.Builder()
                    .setTemperature(temperature)
                    .setMaxTokens(maxTokens)
                    .setTopP(topP)
                    .setFrequencyPenalty(frequencyPenalty)
                    .setPresencePenalty(presencePenalty)
                    .build();
            currentModelName = modelName;
            settingsApplied = true;

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

    public boolean areSettingsApplied() {
        return settingsApplied;
    }
}
