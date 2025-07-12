package com.aiforjava.demo;

import com.aiforjava.exception.ExceptionHandler;
import com.aiforjava.exception.LLMServiceException;
import com.aiforjava.exception.LLMParseException;
import com.aiforjava.llm.Chat.HighLevel.ChatServices;
import com.aiforjava.llm.Chat.LowLevel.ChatServices_LowLevel;
import com.aiforjava.llm.client.DefaultHttpClient;
import com.aiforjava.llm.streams.DefaultStreamResponseParser;
import com.aiforjava.llm.client.LLM_Client;
import com.aiforjava.llm.models.ModelParams;
import com.aiforjava.llm.Prompt.PromptTemplate;
import com.aiforjava.memory.MemoryManager;
import com.aiforjava.memory.memory_algorithm.OptimizedSlidingWindowMemory;
import java.io.File;
import com.aiforjava.llm.models.ModelFeature;
import com.aiforjava.llm.models.ModelRegistry;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Duration;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class SwingChatbot extends JFrame {

    private final JTextPane chatArea;
    private final JTextField inputField;
    private final JButton sendButton;
    private ChatServices chatService;
    private File selectedImageFile; // To store the selected image file
    private JLabel selectedImageLabel; // To display the selected image filename
    private JCheckBox thinkCheckbox; // Checkbox for 'think' capability
    private JButton attachImageButton; // Button to attach image
    private long startTime; // To store the start time of AI thinking

    private static final String LLM_BASE_URL = "https://modest-literally-fish.ngrok-free.app";
    private static String MODEL_NAME = "google/gemma-3-1b"; // Or your preferred model
    private static final int MAX_MEMORY = 1000; // Max messages for memory (adjust as needed for your LLM's context window)

    private ModelParams currentModelParams;
    private String currentModelName;
    private JButton settingsButton;
    private JLabel generatingLabel;
    private Timer generatingAnimationTimer;
    private int thinkingAnimationState = 0;

    private static final Color BACKGROUND_COLOR = new Color(48, 48, 48);
    private static final Color FOREGROUND_COLOR = new Color(172, 172, 172);
    private static final Color ACCENT_COLOR = new Color(9, 29, 76); // Cornflower Blue
    private static final Color BUTTON_COLOR = new Color(70, 70, 70);
    private static final Font CHAT_FONT = new Font("Consolas", Font.BOLD, 16);
    private static final Font INPUT_FONT = new Font("Consolas", Font.BOLD, 14);
    private static final Font BUTTON_FONT = new Font("Consolas", Font.BOLD, 12);

    private void appendStyledText(String text, Color color, boolean bold, int alignment) {
        StyledDocument doc = chatArea.getStyledDocument();
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setForeground(attributes, color);
        StyleConstants.setBold(attributes, bold);
        StyleConstants.setAlignment(attributes, alignment);
        try {
            doc.insertString(doc.getLength(), text, attributes);
            doc.setParagraphAttributes(doc.getLength() - text.length(), text.length(), attributes, false);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public SwingChatbot() {
        setTitle("AI4J Chatbot 😊");
        setSize(1024, 1024);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setResizable(false);
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Initialize UI components
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(BACKGROUND_COLOR);
        chatArea.setForeground(FOREGROUND_COLOR);
        chatArea.setFont(CHAT_FONT);
        chatArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Set up styled document for JTextPane
        StyledDocument doc = chatArea.getStyledDocument();
        SimpleAttributeSet defaultAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(defaultAttributes, FOREGROUND_COLOR);
        StyleConstants.setFontFamily(defaultAttributes, CHAT_FONT.getFamily());
        StyleConstants.setFontSize(defaultAttributes, CHAT_FONT.getSize());
        doc.setParagraphAttributes(0, doc.getLength(), defaultAttributes, false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default scroll pane border
        add(scrollPane, BorderLayout.CENTER);

        // Automatic scrolling
        chatArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    JScrollBar vertical = scrollPane.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                });
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                // Not needed for this use case
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Not needed for this use case
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5)); // Add gap between components
        inputPanel.setBackground(BACKGROUND_COLOR);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding around the panel

        inputField = new JTextField("Type your message here...");
        inputField.setForeground(FOREGROUND_COLOR.darker()); // Placeholder color
        inputField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (inputField.getText().equals("Type your message here...")) {
                    inputField.setText("");
                    inputField.setForeground(FOREGROUND_COLOR);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (inputField.getText().isEmpty()) {
                    inputField.setText("Type your message here...");
                    inputField.setForeground(FOREGROUND_COLOR.darker());
                }
            }
        });
        inputField.setBackground(BUTTON_COLOR);
        inputField.setForeground(FOREGROUND_COLOR);
        inputField.setFont(INPUT_FONT);
        inputField.setCaretColor(FOREGROUND_COLOR); // Set caret color
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 5), // Accent border
                BorderFactory.createEmptyBorder(5, 10, 5, 10) // Inner padding
        ));

        sendButton = new JButton("Send");
        sendButton.setBackground(ACCENT_COLOR);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(BUTTON_FONT);
        sendButton.setFocusPainted(true); // Remove focus border
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        JButton clearButton = new JButton("Clear");
        clearButton.setBackground(BUTTON_COLOR);
        clearButton.setForeground(FOREGROUND_COLOR);
        clearButton.setFont(BUTTON_FONT);
        clearButton.setFocusPainted(true);
        clearButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        inputPanel.add(inputField, BorderLayout.CENTER);

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 0)); // Changed to 4 columns for new button
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(sendButton);
        buttonPanel.add(clearButton);

        // Attach Image Button
        attachImageButton = new JButton("Attach Image");
        attachImageButton.setBackground(BUTTON_COLOR);
        attachImageButton.setForeground(FOREGROUND_COLOR);
        attachImageButton.setFont(BUTTON_FONT);
        attachImageButton.setFocusPainted(false);
        attachImageButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        buttonPanel.add(attachImageButton);

        settingsButton = new JButton("Settings");
        settingsButton.setBackground(BUTTON_COLOR);
        settingsButton.setForeground(FOREGROUND_COLOR);
        settingsButton.setFont(BUTTON_FONT);
        settingsButton.setFocusPainted(false);
        settingsButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        buttonPanel.add(settingsButton);

        inputPanel.add(buttonPanel, BorderLayout.EAST);

        // Selected Image Label
        selectedImageLabel = new JLabel("No file selected");
        selectedImageLabel.setForeground(FOREGROUND_COLOR.darker());
        selectedImageLabel.setFont(INPUT_FONT);
        inputPanel.add(selectedImageLabel, BorderLayout.NORTH); // Placed above input field

        // Think Checkbox
        thinkCheckbox = new JCheckBox("Think");
        thinkCheckbox.setForeground(FOREGROUND_COLOR);
        thinkCheckbox.setBackground(BACKGROUND_COLOR);
        thinkCheckbox.setFont(INPUT_FONT);
        inputPanel.add(thinkCheckbox, BorderLayout.WEST); // Placed to the left of input field

        generatingLabel = new JLabel("AI is thinking...");
        generatingLabel.setForeground(FOREGROUND_COLOR);
        generatingLabel.setFont(INPUT_FONT);
        generatingLabel.setVisible(false); // Initially hidden
        inputPanel.add(generatingLabel, BorderLayout.SOUTH); // Moved to bottom of inputPanel

        add(inputPanel, BorderLayout.SOUTH);

        // Initialize thinking animation timer
        generatingAnimationTimer = new Timer(400, e -> {
            String text = "Generating.";
            switch (thinkingAnimationState) {
                case 0:
                    generatingLabel.setText(text + ".");
                    break;
                case 1:
                    generatingLabel.setText(text + "..");
                    break;
                case 2:
                    generatingLabel.setText(text + "...");
                    break;
            }
            thinkingAnimationState = (thinkingAnimationState + 1) % 3; //Can add more states just increasing this modulo
        });

        // Initialize ChatServices with default values
        currentModelParams = new ModelParams.Builder()
                .setTemperature(0.7)
                .setMaxTokens(1024)
                .setTopP(0.9)
                .setFrequencyPenalty(1.1)
                .setPresencePenalty(0)
                .build();
        currentModelName = "google/gemma-3-1b";
        initializeChatService();

        // Add action listener for the send button and input field
        ActionListener sendActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        };
        sendButton.addActionListener(sendActionListener);
        inputField.addActionListener(sendActionListener); // Allow sending with Enter key

        // Attach Image Button Action Listener
        attachImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".jpg") ||
                               f.getName().toLowerCase().endsWith(".jpeg") ||
                               f.getName().toLowerCase().endsWith(".png") ||
                               f.getName().toLowerCase().endsWith(".gif");
                    }
                    public String getDescription() {
                        return "Image Files (jpg, jpeg, png, gif)";
                    }
                });

                int returnValue = fileChooser.showOpenDialog(SwingChatbot.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    selectedImageFile = fileChooser.getSelectedFile();
                    selectedImageLabel.setText("Image: " + selectedImageFile.getName());
                    selectedImageLabel.setForeground(FOREGROUND_COLOR);
                } else {
                    selectedImageFile = null;
                    selectedImageLabel.setText("No image selected");
                    selectedImageLabel.setForeground(FOREGROUND_COLOR.darker());
                }
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    chatArea.getStyledDocument().remove(0, chatArea.getStyledDocument().getLength());
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
                chatService.reset();

            }
        });

        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SettingsDialog settingsDialog = new SettingsDialog(SwingChatbot.this, currentModelParams, currentModelName);
                settingsDialog.setVisible(true);
                if (settingsDialog.areSettingsApplied()) {
                    currentModelParams = settingsDialog.getAppliedModelParams();
                    currentModelName = settingsDialog.getAppliedModelName();
                    initializeChatService(); // Re-initialize chat service with new settings
                }
                inputField.requestFocusInWindow(); // Ensure focus returns to input field
            }
        });

        // Display initial message
        String welcomeMessage = "Welcome to AI4J Swing Chatbot!\n"; //Fallback message
        try{
            welcomeMessage=Welcome.generateWelcomeMessage();
        } catch (LLMServiceException e){
            // Log the exception, but use a fallback message for the UI
            ExceptionHandler.handle(e);
            welcomeMessage = "Welcome to AI4J Swing Chatbot!";
        }

        appendStyledText(welcomeMessage, FOREGROUND_COLOR, true, StyleConstants.ALIGN_CENTER);
        appendStyledText("\n" + "─".repeat(60) + "\n\n", FOREGROUND_COLOR, false, StyleConstants.ALIGN_CENTER);

    }

    private void initializeChatService() {
        //LLM_Client client = new DefaultHttpClient(LLM_BASE_URL, Duration.ofSeconds(90), "local", false);
        LLM_Client client = new DefaultHttpClient(LLM_BASE_URL, Duration.ofSeconds(90), "local", false, new DefaultStreamResponseParser(), 50L);
        ChatServices_LowLevel lowLevelChatService = new ChatServices_LowLevel(client, currentModelName);

        // MemoryManager memory = new SlidingWindowMemory(MAX_TOKENS);
         MemoryManager memory = new OptimizedSlidingWindowMemory(MAX_MEMORY);
        //MemoryManager memory = new com.aiforjava.memory.memory_algorithm.TokenCountingMemory(MAX_TOKENS);
        //MemoryManager memory = new com.aiforjava.memory.ChatLogs.CachedFileMemory();

        PromptTemplate promptTemplate = new PromptTemplate("You are a helpful, friendly AI assistant.", "User: {user_message}\nAI:");

        chatService = new ChatServices(
                lowLevelChatService,
                memory,
                currentModelParams,
                promptTemplate
        );
        updateUIBasedOnModelCapabilities(); // Call after chatService is initialized
    }

    private void sendMessage() {
        String userMessage = inputField.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }

        // If the 'Think' checkbox is unchecked, append /no_think to the message
        if (thinkCheckbox.isEnabled() && !thinkCheckbox.isSelected()) {
            userMessage += "/no_think";
        }

        appendStyledText("You 👨‍💻: " + userMessage.split("/no_think")[0] + "\n\n", FOREGROUND_COLOR, true, StyleConstants.ALIGN_RIGHT); // Add user message with two newlines for spacing
        inputField.setText(""); // Clear input field immediately after displaying user message

        if ("reset".equalsIgnoreCase(userMessage)) {
            chatService.reset();
            return;
        }

        final String finalUserMessage = userMessage;
        final ModelParams finalRequestParams = currentModelParams; // Always use currentModelParams

        // Disable input and button while AI is thinking
        inputField.setEnabled(false);
        sendButton.setEnabled(false);
        generatingLabel.setVisible(true); // Show thinking indicator
        generatingAnimationTimer.start(); // Start animation
        appendStyledText("AI 🤖: ", FOREGROUND_COLOR, true, StyleConstants.ALIGN_LEFT); // Prepare for AI response

        startTime = System.currentTimeMillis(); // Record start time

        // Use SwingWorker for background LLM call
        new SwingWorker<Void, String>() {
            boolean doneThinking = false; //helper var
            boolean inThinkBlock = false; // New flag to track if we are inside a <think> block

            @Override
            protected Void doInBackground() throws Exception, LLMParseException {
                if (selectedImageFile != null) {
                    chatService.chatStream(finalUserMessage, selectedImageFile, finalRequestParams, this::publish);
                } else {
                    chatService.chatStream(finalUserMessage, finalRequestParams, this::publish);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    String currentChunkContent = chunk; // The original chunk content

                    // Handle <think> tag: set inThinkBlock and append start marker if enabled
                    if (currentChunkContent.contains("<think>")) {
                        inThinkBlock = true;
                        if (thinkCheckbox.isSelected()) {
                            appendStyledText("[Thinking...\n", new Color(61, 103, 255, 171), false, StyleConstants.ALIGN_LEFT);
                        }
                        currentChunkContent = currentChunkContent.replace("<think>", "");
                    }

                    // Determine if content should be appended based on current inThinkBlock state
                    boolean shouldAppendContent = thinkCheckbox.isSelected() || !inThinkBlock;

                    // Handle </think> tag: set inThinkBlock to false and append end marker if enabled
                    int endIndex = currentChunkContent.indexOf("</think>");
                    String contentBeforeEndTag = currentChunkContent;
                    String contentAfterEndTag = "";

                    if (endIndex != -1) {
                        contentBeforeEndTag = currentChunkContent.substring(0, endIndex);
                        if (endIndex + "</think>".length() < currentChunkContent.length()) {
                            contentAfterEndTag = currentChunkContent.substring(endIndex + "</think>".length());
                        }
                        // Update inThinkBlock state for the content *after* the end tag
                        inThinkBlock = false;
                    }

                    // Process content before the end tag (if any)
                    if (shouldAppendContent) { // This should be based on inThinkBlock *before* this chunk's end tag
                        for (char c : contentBeforeEndTag.toCharArray()) {
                            if (thinkCheckbox.isSelected() && ModelRegistry.supportsFeature(currentModelName, ModelFeature.THINK) && inThinkBlock) {
                                appendStyledText(String.valueOf(c), new Color(0, 207, 255, 171), false, StyleConstants.ALIGN_LEFT);
                            } else {
                                appendStyledText(String.valueOf(c), FOREGROUND_COLOR, false, StyleConstants.ALIGN_LEFT);
                            }
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            } catch (NullPointerException e){
                                System.err.println("Stand by");
                            }
                        }
                    }

                    // If the chunk contained </think>, append the end marker and process content after it
                    if (endIndex != -1) {
                        doneThinking = true;
                        long endTime = System.currentTimeMillis();
                        long duration = (endTime - startTime) / 1000;
                        if (thinkCheckbox.isSelected()) {
                            appendStyledText("\n[End Thinking. (Thought for: " + duration + " seconds)]\n", new Color(40, 73, 201), false, StyleConstants.ALIGN_LEFT);
                        }

                        // Now process content after the end tag. This content is definitely not in a think block.
                        for (char c : contentAfterEndTag.toCharArray()) {
                            appendStyledText(String.valueOf(c), FOREGROUND_COLOR, false, StyleConstants.ALIGN_LEFT);
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            } catch (NullPointerException e){
                                System.err.println("Stand by");
                            }
                        }
                    }
                }
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions from doInBackground
                    appendStyledText("\n\n", FOREGROUND_COLOR, false, StyleConstants.ALIGN_LEFT); // New line after AI's response
                } catch (Exception e) {
                    appendStyledText("\nError: " + e.getMessage() + "\n\n", Color.RED, false, StyleConstants.ALIGN_LEFT);
                    ExceptionHandler.handle(e);
                } finally {
                    // Re-enable input and button
                    inputField.setEnabled(true);
                    sendButton.setEnabled(true);
                    generatingLabel.setVisible(false); // Hide thinking indicator
                    generatingAnimationTimer.stop(); // Stop animation
                    inputField.requestFocusInWindow(); // Focus back to input field

                    // Clear selected image after sending
                    selectedImageFile = null;
                    selectedImageLabel.setText("No image selected");
                    selectedImageLabel.setForeground(FOREGROUND_COLOR.darker());
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, fall back to original look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        SwingUtilities.invokeLater(() -> {
            // Create a dummy JFrame to act as parent for the dialog
            JFrame dummyFrame = new JFrame();
            dummyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            dummyFrame.setUndecorated(true); // Hide the dummy frame
            dummyFrame.setSize(0, 0);
            dummyFrame.setVisible(true);

            // Show Login Dialog first
            LoginDialog loginDialog = new LoginDialog(dummyFrame, AppConfig.USE_DATABASE_AUTH);
            loginDialog.setVisible(true);

            if (loginDialog.isLoggedIn()) {
                // Proceed to Model Check Dialog if login is successful
                ModelCheckDialog modelCheckDialog = new ModelCheckDialog(dummyFrame, LLM_BASE_URL);
                modelCheckDialog.setVisible(true); // This will block until the dialog is closed

                if (modelCheckDialog.canProceed()) {
                    new SwingChatbot().setVisible(true);
                } else if (modelCheckDialog.isCancelledByUser()) {
                    // User closed the dialog without proceeding, just exit silently
                    System.exit(0);
                } else {
                    // Model check failed for other reasons
                    JOptionPane.showMessageDialog(null, "Model check failed. Exiting application.", "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
            } else {
                // Login was cancelled or failed
                System.exit(0);
            }
            dummyFrame.dispose(); // Dispose the dummy frame after use
        });
    }

    /**
     * Updates the UI elements (buttons, checkboxes) based on the capabilities of the currently selected model.
     */
    private void updateUIBasedOnModelCapabilities() {
        boolean supportsVision = ModelRegistry.supportsFeature(currentModelName, ModelFeature.VISION);
        boolean supportsThink = ModelRegistry.supportsFeature(currentModelName, ModelFeature.THINK);

        attachImageButton.setEnabled(supportsVision);
        selectedImageLabel.setEnabled(supportsVision);
        if (!supportsVision) {
            selectedImageFile = null; // Clear selected image if model doesn't support vision
            selectedImageLabel.setText("No image selected");
            selectedImageLabel.setForeground(FOREGROUND_COLOR.darker());
        }

        thinkCheckbox.setEnabled(supportsThink);
        if (!supportsThink) {
            thinkCheckbox.setSelected(false); // Uncheck if model doesn't support thinking
        } else {
            thinkCheckbox.setSelected(true); // Thinking is enabled by default for models that support it
        }
    }
}