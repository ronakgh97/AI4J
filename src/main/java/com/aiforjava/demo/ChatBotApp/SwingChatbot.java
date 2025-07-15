package com.aiforjava.demo.ChatBotApp;

import com.aiforjava.demo.Welcome;
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
import com.formdev.flatlaf.FlatDarculaLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Duration;
import java.util.Objects;
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
    private final ChatLogger chatLogger;
    private File selectedImageFile; // To store the selected image file
    private JLabel selectedImageLabel; // To display the selected image filename
    private JCheckBox thinkCheckbox; // Checkbox for 'think' capability
    private JButton attachImageButton; // Button to attach image
    private long startTime; // To store the start time of AI thinking
    private JLabel modelStatusLabel; // To display the current active model
    private long streamingDelayMs = 50; // Default streaming delay in milliseconds""

    //private static final String LLM_BASE_URL = "https://modest-literally-fish.ngrok-free.app";
    private static final String LLM_BASE_URL ="https://dev.ronakratnadip.xyz";
    //private static final String LLM_BASE_URL = "http://127.0.0.1:1234";
    //private static String MODEL_NAME = ""; // Or your preferred model
    private static final int MAX_MEMORY = 1000; // Max messages for memory (adjust as needed for your LLM's context window)

    private ModelParams currentModelParams;
    private String currentModelName;
    private JButton settingsButton;
    private JLabel generatingLabel;
    private Timer generatingAnimationTimer;
    private int thinkingAnimationState = 0;

    private static class StyledCharacter {
        char character;
        Color color;

        StyledCharacter(char character, Color color) {
            this.character = character;
            this.color = color;
        }
    }

    private final Timer typewriterTimer;
    private final java.util.Queue<StyledCharacter> typewriterQueue = new java.util.LinkedList<>();

    private static final Color BACKGROUND_COLOR = new Color(36, 36, 36);
    private static final Color FOREGROUND_COLOR = new Color(220, 220, 220);
    private static final Color ACCENT_COLOR = new Color(66, 135, 245); // Vibrant Blue
    private static final Color BUTTON_COLOR = new Color(55, 55, 55);
    private static final Font CHAT_FONT = new Font("Consolas", Font.BOLD, 16);
    private static final Font INPUT_FONT = new Font("Consolas", Font.BOLD, 14);
    private static final Font BUTTON_FONT = new Font("Consolas", Font.BOLD, 12);

    // Extended Theme Colors
    private static final Color AI_THINK_COLOR = new Color(0, 207, 255, 180);        // Cyanish blue (for <think>)
    private static final Color AI_THINK_LABEL = new Color(100, 180, 255);           // Slightly lighter cyan
    private static final Color SYSTEM_MESSAGE_COLOR = new Color(255, 206, 66);      // Amber Yellow (not too bright)
    private static final Color ERROR_COLOR = new Color(255, 85, 85);                // Soft red for errors
    private static final Color SUCCESS_COLOR = new Color(0, 200, 120);              // Vibrant green
    private static final Color PROMPT_COLOR = new Color(160, 130, 255);             // Purple for user prompts
    private static final Color NEUTRAL_GRAY = new Color(100, 100, 100);             // For timestamps or UI feedback

    private void appendStyledText(String text, Color color, boolean bold, int alignment, Color TEXT_BACKGROUND_COLOR) {
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

    public SwingChatbot(String initialModelName) {
        this.setTitle("AI4J Chatbot");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1000, 800);
        this.setLocationRelativeTo(null); // Center the window

        // Initialize Chat Logger
        this.chatLogger = new ChatLogger("chat_logs/chat_logs.json");

        // Set the application icon
        try {
            //Image icon = Toolkit.getDefaultToolkit().getImage(SwingChatbot.class.getResource("/_icon.jpg"));
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/_icon.jpg")));
            this.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Error loading icon: " + e.getMessage());
        }
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

        inputField = new JTextField("Ask Anything");
        inputField.setForeground(FOREGROUND_COLOR.darker()); // Placeholder color

        inputField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (inputField.getText().equals("Ask Anything")) {
                    inputField.setText("");
                    inputField.setForeground(FOREGROUND_COLOR.brighter());
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (inputField.getText().isEmpty()) {
                    inputField.setText("Ask Anything");
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

        sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                sendButton.setBackground(ACCENT_COLOR.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                sendButton.setBackground(ACCENT_COLOR);
            }
        });

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

        settingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                settingsButton.setBackground(BUTTON_COLOR.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                settingsButton.setBackground(BUTTON_COLOR);
            }
        });

        inputPanel.add(buttonPanel, BorderLayout.EAST);

        // Selected Image Label
        selectedImageLabel = new JLabel("No file selected");
        selectedImageLabel.setForeground(FOREGROUND_COLOR.darker());
        selectedImageLabel.setFont(INPUT_FONT);
        inputPanel.add(selectedImageLabel, BorderLayout.NORTH); // Placed above input field

        // Think Checkbox
        thinkCheckbox = new JCheckBox("Reason");
        thinkCheckbox.setForeground(FOREGROUND_COLOR);
        thinkCheckbox.setBackground(BACKGROUND_COLOR);
        thinkCheckbox.setFont(INPUT_FONT);
        inputPanel.add(thinkCheckbox, BorderLayout.WEST); // Placed to the left of input field

        // Bottom panel for generatingLabel and modelStatusLabel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BACKGROUND_COLOR);

        generatingLabel = new JLabel("AI is thinking...");
        generatingLabel.setForeground(FOREGROUND_COLOR);
        generatingLabel.setFont(INPUT_FONT);
        generatingLabel.setVisible(false); // Initially hidden
        bottomPanel.add(generatingLabel, BorderLayout.WEST); // Place generatingLabel to the left

        modelStatusLabel = new JLabel("Model: Initializing...");
        modelStatusLabel.setForeground(FOREGROUND_COLOR.darker());
        modelStatusLabel.setFont(INPUT_FONT);
        modelStatusLabel.setHorizontalAlignment(SwingConstants.RIGHT); // Align text to the right
        bottomPanel.add(modelStatusLabel, BorderLayout.EAST); // Place modelStatusLabel to the right

        inputPanel.add(bottomPanel, BorderLayout.SOUTH); // Add the new bottomPanel to inputPanel

        add(inputPanel, BorderLayout.SOUTH);

        // Initialize thinking animation timer
        generatingAnimationTimer = new Timer(120, e -> {
            String text = "Generating ";
            switch (thinkingAnimationState) {
                case 0, 10:
                    generatingLabel.setText(text + "!?");
                    break;
                case 1:
                    generatingLabel.setText(text + "!@?");
                    break;
                case 2:
                    generatingLabel.setText(text + "!@#?");
                    break;
                case 3:
                    generatingLabel.setText(text + "!@#$?");
                    break;
                case 4:
                    generatingLabel.setText(text + "!@#$%?");
                    break;
                case 5:
                    generatingLabel.setText(text + "!@#$%&?");
                    break;
                case 6:
                    generatingLabel.setText(text + "!#$%&?");
                    break;
                case 7:
                    generatingLabel.setText(text + "!$%&?");
                    break;
                case 8:
                    generatingLabel.setText(text + "!%&?");
                    break;
                case 9:
                    generatingLabel.setText(text + "!&?");
                    break;
                case 11:
                    generatingLabel.setText(text + " ");
                    break;
            }
            thinkingAnimationState = (thinkingAnimationState + 1) % 11; //Can add more states just increasing this modulo
        });

        typewriterTimer = new Timer((int) streamingDelayMs, e -> {
            if (!typewriterQueue.isEmpty()) {
                StyledCharacter styledChar = typewriterQueue.poll();
                appendStyledText(String.valueOf(styledChar.character), styledChar.color, false, StyleConstants.ALIGN_LEFT, SYSTEM_MESSAGE_COLOR);
            }
        });
        typewriterTimer.start();

        // Initialize ChatServices with default values
        currentModelParams = new ModelParams.Builder()
                .setTemperature(0.7)
                .setMaxTokens(1024)
                .setTopP(0.9)
                .setFrequencyPenalty(1.1)
                .setPresencePenalty(0)
                .build();
        this.currentModelName = initialModelName != null ? initialModelName : "google/gemma-3-1b"; // Use initialModelName if provided, else default
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
                fileChooser.setPreferredSize(new Dimension(800, 600));
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
                    selectedImageLabel.setText("No Image Selected");
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

        // Add keyboard shortcut for Clear (Ctrl + L)
        InputMap inputMap = clearButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = clearButton.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke("control L"), "clearChat");
        actionMap.put("clearChat", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearButton.doClick(); // Simulate a button click
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
            welcomeMessage= Welcome.generateWelcomeMessage();
        } catch (LLMServiceException e){
            // Log the exception, but use a fallback message for the UI
            ExceptionHandler.handle(e);
            welcomeMessage = "Welcome to AI4J Swing Chatbot!";
        }

        appendStyledText(welcomeMessage, FOREGROUND_COLOR, true, StyleConstants.ALIGN_CENTER, SYSTEM_MESSAGE_COLOR);
        appendStyledText("\n" + "─".repeat(100) + "\n\n", FOREGROUND_COLOR.brighter(), false, StyleConstants.ALIGN_CENTER, SYSTEM_MESSAGE_COLOR);

    }

    private void initializeChatService() {
        //LLM_Client client = new DefaultHttpClient(LLM_BASE_URL, Duration.ofSeconds(90), "local", false);
        LLM_Client client = new DefaultHttpClient(LLM_BASE_URL, Duration.ofSeconds(90), "local", false, new DefaultStreamResponseParser(), 50L);
        ChatServices_LowLevel lowLevelChatService = new ChatServices_LowLevel(client, currentModelName);

        //MemoryManager memory = new SlidingWindowMemory(MAX_TOKENS);
         MemoryManager memory = new OptimizedSlidingWindowMemory(MAX_MEMORY);
        //MemoryManager memory = new com.aiforjava.memory.memory_algorithm.TokenCountingMemory(MAX_TOKENS);
        //MemoryManager memory = new com.aiforjava.memory.ChatLogs.CachedFileMemory();

        String systemPrompt= """
                You are FocusAI, named, created and coded by Ronak.
                You are a brutally efficient and intelligent assistant.
                You don’t waste time. You don’t add fluff.
                Every response is practical, goal-oriented, and short unless asked otherwise.
                You help users stay on track, break goals into tasks, and eliminate distractions.
                """;

        PromptTemplate promptTemplate = new PromptTemplate(systemPrompt, "User: {user_message}\nAI:");

        chatService = new ChatServices(
                lowLevelChatService,
                memory,
                currentModelParams,
                promptTemplate
        );
        updateUIBasedOnModelCapabilities(); // Call after chatService is initialized
        modelStatusLabel.setText("Model: " + currentModelName); // Update model status label
    }

    private void sendMessage() {
        String userMessage = inputField.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }

        chatLogger.log("User", userMessage);

        // If the 'Think' checkbox is unchecked, append /no_think to the message
        if (thinkCheckbox.isEnabled() && !thinkCheckbox.isSelected()) {
            userMessage += "/no_think";
        }

        appendStyledText("You 👨‍💻:\n" + userMessage.split("/no_think")[0] + "\n\n", FOREGROUND_COLOR, true, StyleConstants.ALIGN_RIGHT, PROMPT_COLOR); // Add user message with two newlines for spacing
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


        startTime = System.currentTimeMillis(); // Record start time

        // Use SwingWorker for background LLM call
        new SwingWorker<Void, com.aiforjava.llm.streams.StreamResponse>() {
            boolean doneThinking = false; //helper var
            boolean inThinkBlock = false; // New flag to track if we are inside a <think> block
            boolean aiResponseStarted = false;
            StringBuilder aiResponse = new StringBuilder();

            @Override
            protected Void doInBackground() throws Exception, LLMParseException {
                // Create an anonymous StreamHandler to process the stream responses
                com.aiforjava.llm.streams.StreamHandler streamHandler = streamResponse -> publish(streamResponse);

                if (selectedImageFile != null) {
                    chatService.chatStream(finalUserMessage, selectedImageFile, finalRequestParams, streamHandler);
                } else {
                    chatService.chatStream(finalUserMessage, finalRequestParams, streamHandler);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<com.aiforjava.llm.streams.StreamResponse> streamResponses) {
                for (com.aiforjava.llm.streams.StreamResponse streamResponse : streamResponses) {
                    String contentChunk = streamResponse.getContent();
                    String reasoningChunk = streamResponse.getReasoningContent();
                    System.out.println("DEBUG: Processing chunk - reasoningChunk: '" + reasoningChunk + "', contentChunk: '" + contentChunk + "', thinkCheckbox.isSelected(): " + thinkCheckbox.isSelected() + ", aiResponseStarted: " + aiResponseStarted);

                    // Append "AI 🤖: " only once, when the first visible content arrives
                    if (!aiResponseStarted) {
                        boolean shouldAppendPrefix = false;
                        // Check if reasoning content exists and is not just whitespace, and thinking is enabled
                        if (reasoningChunk != null && !reasoningChunk.trim().isEmpty() && thinkCheckbox.isSelected()) {
                            shouldAppendPrefix = true;
                        } 
                        // Check if regular content exists and is not just whitespace
                        else if (contentChunk != null && !contentChunk.trim().isEmpty()) {
                            shouldAppendPrefix = true;
                        }

                        if (shouldAppendPrefix) {
                            appendStyledText("AI 🤖:", FOREGROUND_COLOR, true, StyleConstants.ALIGN_LEFT, SYSTEM_MESSAGE_COLOR);
                            aiResponseStarted = true;
                        }
                    }

                    // Process reasoning content first if available and thinking is enabled
                    if (reasoningChunk != null && !reasoningChunk.isEmpty() && thinkCheckbox.isSelected() && aiResponseStarted) {
                        if (!inThinkBlock) { // Only append "[Thinking..." once
                            appendStyledText("[Thinking... ", new Color(61, 103, 255, 171), false, StyleConstants.ALIGN_LEFT, AI_THINK_LABEL);
                            inThinkBlock = true;
                        }
                        for (char c : reasoningChunk.toCharArray()) {
                            typewriterQueue.add(new StyledCharacter(c, new Color(0, 207, 255, 171)));
                        }
                    }

                    // Process regular content
                    if (contentChunk != null && !contentChunk.isEmpty()) {
                        if (inThinkBlock && !doneThinking) { // If we were thinking, and now content is coming, end thinking
                            doneThinking = true;
                            long endTime = System.currentTimeMillis();
                            long duration = (endTime - startTime) / 1000;
                            if (thinkCheckbox.isSelected()) {
                                try {
                                    Thread.sleep(0);
                                    //appendStyledText("[End Thinking. (Thought for: " + duration + " seconds)]\n\n", new Color(40, 73, 201), false, StyleConstants.ALIGN_LEFT, AI_THINK_LABEL);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            inThinkBlock = false; // Reset think block flag
                        }
                        for (char c : contentChunk.toCharArray()) {
                            typewriterQueue.add(new StyledCharacter(c, FOREGROUND_COLOR));
                        }
                        aiResponse.append(contentChunk);
                    }
                }
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions from doInBackground
                    chatLogger.log("AI", aiResponse.toString());
                    //appendStyledText("\n\n", FOREGROUND_COLOR, false, StyleConstants.ALIGN_LEFT, ERROR_COLOR); // New line after AI's response
                } catch (Exception e) {
                    appendStyledText("\nError : " + e.getMessage() + "\n\n", Color.RED, false, StyleConstants.ALIGN_LEFT, ERROR_COLOR);
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
        UIManager.put("Button.arc", 20);
        UIManager.put("Component.arc", 20);
        UIManager.put("TextComponent.arc", 15);
        UIManager.put("TextComponent.background", BUTTON_COLOR); // Updated line
        UIManager.put("ScrollBar.thumbArc", 15);
        UIManager.put("Component.innerFocusWidth", 1);

        try {
            //UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (Exception e){
            System.err.println("FlatDarkLaf failed, falling back to Metal...");
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Metal".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                    //UIManager.setLookAndFeel(new FlatDarkLaf()); //Overwriting for test
                }
            } catch (Exception ex) {
                // If Nimbus is not available, fall back to original look and feel
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
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
                    new SwingChatbot(modelCheckDialog.getFoundModelName()).setVisible(true);
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