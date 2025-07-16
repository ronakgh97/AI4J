package com.aiforjava.demo.ChatBotApp;

import com.aiforjava.llm.models.ModelRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ModelCheckDialog extends JDialog {
    private JLabel statusLabel;
    private JLabel animationLabel; // New: For custom animation
    private Timer animationTimer; // New: For custom animation
    private int animationState = 0; // New: To track animation frame
    private JButton proceedButton;
    private JButton retryButton;
    private String llmBaseUrl;
    private boolean canProceed = false;
    private boolean cancelledByUser = true; // New: Flag to track if dialog was cancelled by user
    private String foundModelName; // To store the name of the first found model

    // UI Constants (matching SwingChatbot for consistency)
    private static final Color BACKGROUND_COLOR = new Color(48, 48, 48);
    private static final Color FOREGROUND_COLOR = new Color(172, 172, 172);
    private static final Color ACCENT_COLOR = new Color(100, 149, 237); // Cornflower Blue
    private static final Color BUTTON_COLOR = new Color(70, 70, 70);
    private static final Font TITLE_FONT = new Font("Consolas", Font.BOLD, 20);
    private static final Font LABEL_FONT = new Font("Consolas", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Consolas", Font.BOLD, 14);

    public ModelCheckDialog(JFrame parent, String llmBaseUrl) {
        super(parent, "Model Availability Check", true);
        this.llmBaseUrl = llmBaseUrl;
        setSize(512, 256);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Prevent closing until check is done
        setLocationRelativeTo(null);
        setResizable(false);

        // Set the application icon
        try {
            //Image icon = Toolkit.getDefaultToolkit().getImage(SwingChatbot.class.getResource("/_icon.jpg"));
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/_icon.jpg")));
            this.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Error loading icon: " + e.getMessage());
        }

        initUI();
        
        // Initialize animation timer
        animationTimer = new Timer(10, e -> {
            String[] frames = {"Loading ", "Loading. ", "Loading.. ", "Loading... ", "Loading.... "};
            animationLabel.setText(frames[animationState]);
            animationState = (animationState + 1) % frames.length;
        });

        startModelCheck();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Server & Model Check", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(ACCENT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        // Status Label
        statusLabel = new JLabel("Connecting to LLM server...", SwingConstants.CENTER);
        statusLabel.setFont(LABEL_FONT);
        statusLabel.setForeground(FOREGROUND_COLOR);
        gbc.gridy = 1;
        add(statusLabel, gbc);

        // Custom Animation Label
        animationLabel = new JLabel("Loading... ", SwingConstants.CENTER);
        animationLabel.setFont(LABEL_FONT);
        animationLabel.setForeground(FOREGROUND_COLOR);
        gbc.gridy = 2;
        add(animationLabel, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        retryButton = new JButton("Retry");
        retryButton.setBackground(BUTTON_COLOR);
        retryButton.setForeground(FOREGROUND_COLOR);
        retryButton.setFont(BUTTON_FONT);
        retryButton.setFocusPainted(false);
        retryButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        retryButton.setEnabled(false); // Disabled initially
        retryButton.addActionListener(e -> {
            resetUIForCheck();
            startModelCheck();
        });
        buttonPanel.add(retryButton);

        proceedButton = new JButton("Proceed to Chat");
        proceedButton.setBackground(ACCENT_COLOR);
        proceedButton.setForeground(Color.WHITE);
        proceedButton.setFont(BUTTON_FONT);
        proceedButton.setFocusPainted(false);
        proceedButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        proceedButton.setEnabled(false); // Disabled until check is complete
        proceedButton.addActionListener(e -> {
            canProceed = true;
            cancelledByUser = false; // User explicitly chose to proceed
            dispose(); // Close the dialog
        });
        buttonPanel.add(proceedButton);

        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);
    }

    private void resetUIForCheck() {
        statusLabel.setText("Connecting to LLM server...");
        animationLabel.setText("Loading "); // Reset animation text
        animationLabel.setVisible(true); // Show animation
        animationTimer.start(); // Start animation
        proceedButton.setEnabled(false);
        retryButton.setEnabled(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    private void startModelCheck() {
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Connecting to LLM server...");
                try {
                    HttpClient httpClient = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(10))
                            .version(HttpClient.Version.HTTP_2)
                            .build();

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(llmBaseUrl + "/v1/models"))
                            .GET()
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        publish("LLM server connected. Checking models...");
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(response.body());
                        JsonNode data = root.path("data");

                        Set<String> availableModels = new HashSet<>();
                        if (data.isArray()) {
                            System.out.println("LM Studio /v1/models response (extracted IDs):");
                            for (JsonNode modelNode : data) {
                                String id = modelNode.path("id").asText();
                                System.out.println("- " + id);
                                availableModels.add(id);
                            }
                        }

                        Set<String> hardcodedModels = ModelRegistry.getAllModelNames();
                        System.out.println(" Models from ModelRegistry:");
                        for (String hardcodedModel : hardcodedModels) {
                            System.out.println("- " + hardcodedModel);
                        }

                        StringBuilder missingModels = new StringBuilder();
                        boolean allFound = true;

                        System.out.println(" Checking available models:");
                        for (String hardcodedModel : hardcodedModels) {
                            boolean found = availableModels.contains(hardcodedModel);
                            System.out.println("- " + hardcodedModel + ": " + (found ? "Found" : "NOT Found"));
                            if (!found) {
                                missingModels.append("- ").append(hardcodedModel).append(" ");
                                allFound = false;
                            }
                        }

                        if (allFound) {
                            publish("All models found on the server.");
                            canProceed = true;
                            // Set foundModelName to the first hardcoded model if all are found
                            if (!hardcodedModels.isEmpty()) {
                                foundModelName = hardcodedModels.iterator().next();
                            }
                        } else {
                            publish("<html><font color='red'>Warning: Some models are not available on the server:</font><br>" + missingModels.toString().replace(" ", "<br>") + "</html>");
                            canProceed = false; // Cannot proceed if models are missing
                        }

                    } else {
                        throw new IOException("Failed to connect to LLM server. Status: " + response.statusCode());
                    }
                } catch (IOException | InterruptedException e) {
                    publish("<html><font color='red'>Error: Could not connect to LLM server " + "</font><br>" + e.getMessage() + "</html>");
                    canProceed = false; // Cannot proceed if server is down
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    statusLabel.setText(chunk);
                }
            }

            @Override
            protected void done() {
                animationTimer.stop(); // Stop animation
                animationLabel.setVisible(false); // Hide animation
                proceedButton.setEnabled(canProceed);
                retryButton.setEnabled(true); // Always allow retry after a check
                setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            }
        }.execute();
    }

    public boolean canProceed() {
        return canProceed;
    }

    public boolean isCancelledByUser() {
        return cancelledByUser;
    }

    public String getFoundModelName() {
        return foundModelName;
    }

    @Override
    protected void processWindowEvent(java.awt.event.WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == java.awt.event.WindowEvent.WINDOW_CLOSING) {
            // If the window is closed by the user, and they haven't proceeded, it's a cancellation.
            if (!canProceed) {
                cancelledByUser = true;
            }
        }
    }
}