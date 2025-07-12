package com.aiforjava.demo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LoginDialog extends JDialog {

    private JTextField usernameField;
    private JTextField apiKeyField;
    private JButton loginButton;
    private JButton createProfileButton;
    private boolean loggedIn = false;
    private DatabaseManager databaseManager;
    private MasterKeyManager masterKeyManager;
    private boolean useDatabaseAuth; // New field to store the authentication mode

    private static final java.io.File LAST_USERNAME_FILE = new java.io.File("last_username.txt");

    // Hardcoded API key for non-database mode
    private static final String HARDCODED_API_KEY = "demo";

    // UI Constants (matching SwingChatbot for consistency)
    private static final Color BACKGROUND_COLOR = new Color(48, 48, 48);
    private static final Color FOREGROUND_COLOR = new Color(172, 172, 172);
    private static final Color ACCENT_COLOR = new Color(100, 149, 237); // Cornflower Blue
    private static final Color BUTTON_COLOR = new Color(70, 70, 70);
    private static final Font LABEL_FONT = new Font("Consolas", Font.BOLD, 14);
    private static final Font INPUT_FONT = new Font("Consolas", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Consolas", Font.BOLD, 14);

    public LoginDialog(JFrame parent, boolean useDatabaseAuth) {
        super(parent, useDatabaseAuth ? "User Login / Create Profile" : "API Key Login", true);
        this.useDatabaseAuth = useDatabaseAuth;
        setSize(useDatabaseAuth ? 512 : 512, useDatabaseAuth ? 256 : 194); // Adjust size based on mode
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        if (useDatabaseAuth) {
            databaseManager = new DatabaseManager();
            masterKeyManager = new MasterKeyManager();
        }

        initUI();

        if (useDatabaseAuth) {
            String lastUsername = loadLastUsername();
            if (lastUsername != null) {
                usernameField.setText(lastUsername);
            }
        }
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(mainPanel);

        JPanel formPanel;
        if (useDatabaseAuth) {
            formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        } else {
            formPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        }
        formPanel.setBackground(BACKGROUND_COLOR);

        if (useDatabaseAuth) {
            JLabel usernameLabel = new JLabel("Username:");
            usernameLabel.setForeground(FOREGROUND_COLOR);
            usernameLabel.setFont(LABEL_FONT);
            formPanel.add(usernameLabel);
            usernameField = new JTextField();
            usernameField.setBackground(BUTTON_COLOR);
            usernameField.setForeground(FOREGROUND_COLOR);
            usernameField.setFont(INPUT_FONT);
            formPanel.add(usernameField);
        }

        JLabel apiKeyLabel = new JLabel("API Key:");
        apiKeyLabel.setForeground(FOREGROUND_COLOR);
        apiKeyLabel.setFont(LABEL_FONT);
        formPanel.add(apiKeyLabel);
        apiKeyField = new JTextField();
        apiKeyField.setBackground(BUTTON_COLOR);
        apiKeyField.setForeground(FOREGROUND_COLOR);
        apiKeyField.setFont(INPUT_FONT);
        formPanel.add(apiKeyField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        loginButton = new JButton("Login");
        loginButton.setBackground(ACCENT_COLOR);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(BUTTON_FONT);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        loginButton.addActionListener(e -> login());
        buttonPanel.add(loginButton);

        if (useDatabaseAuth) {
            createProfileButton = new JButton("Create Profile");
            createProfileButton.setBackground(BUTTON_COLOR);
            createProfileButton.setForeground(FOREGROUND_COLOR);
            createProfileButton.setFont(BUTTON_FONT);
            createProfileButton.setFocusPainted(false);
            createProfileButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            createProfileButton.addActionListener(e -> createProfile());
            buttonPanel.add(createProfileButton);
        }

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createProfile() {
        if (useDatabaseAuth) {
            String username = usernameField.getText().trim();
            String apiKey = apiKeyField.getText().trim();

            if (username.isEmpty() || apiKey.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and API Key cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validate API Key against master keys
            if (!masterKeyManager.isValidMasterKey(apiKey)) {
                JOptionPane.showMessageDialog(this, "Invalid API Key. Please use a valid key provided by the administrator.", "Creation Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (databaseManager.userExists(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists. Please choose a different one.", "Creation Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (databaseManager.addUser(username, apiKey)) {
                masterKeyManager.consumeMasterKey(apiKey); // Consume the master key after successful registration
                JOptionPane.showMessageDialog(this, "Profile created successfully! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create profile. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Profile creation is not available in hardcoded API Key mode.", "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void login() {
        String apiKey = apiKeyField.getText().trim();

        if (useDatabaseAuth) {
            String username = usernameField.getText().trim();

            if (username.isEmpty() || apiKey.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and API Key cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (databaseManager.authenticateUser(username, apiKey)) {
                loggedIn = true;
                saveLastUsername(username); // Save username on successful login
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or API Key.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Hardcoded API Key mode
            if (apiKey.equals(HARDCODED_API_KEY)) {
                loggedIn = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid API Key.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    private void saveLastUsername(String username) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LAST_USERNAME_FILE))) {
            writer.println(username);
        } catch (IOException e) {
            System.err.println("Error saving last username: " + e.getMessage());
        }
    }

    private String loadLastUsername() {
        if (LAST_USERNAME_FILE.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(LAST_USERNAME_FILE))) {
                return reader.readLine();
            } catch (IOException e) {
                System.err.println("Error loading last username: " + e.getMessage());
            }
        }
        return null;
    }
}