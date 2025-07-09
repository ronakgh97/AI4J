package com.aiforjava.demo;

import com.aiforjava.exception.ExceptionHandler;
import com.aiforjava.exception.LLMServiceException;
import com.aiforjava.exception.LLMParseException;
import com.aiforjava.llm.Chat.HighLevel.ChatServices;
import com.aiforjava.llm.Chat.LowLevel.ChatServices_LowLevel;
import com.aiforjava.llm.DefaultHttpClient;
import com.aiforjava.llm.DefaultStreamResponseParser;
import com.aiforjava.llm.LLM_Client;
import com.aiforjava.llm.ModelParams;
import com.aiforjava.llm.Prompt.PromptTemplate;
import com.aiforjava.memory.MemoryManager;
import com.aiforjava.memory.SlidingWindowMemory;
import com.aiforjava.memory.OptimizedSlidingWindowMemory;
import com.aiforjava.memory.ChatLogs.CachedFileMemory;
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

    private static final String LLM_BASE_URL = "http://localhost:1234";
    private static String MODEL_NAME = "google/gemma-3-1b"; // Or your preferred model
    private static final int MEMORY_WINDOW_SIZE = 50; // Keep last 50 messages in memory

    private ModelParams currentModelParams;
    private String currentModelName;
    private JButton settingsButton;
    private JLabel thinkingLabel;
    private Timer thinkingAnimationTimer;
    private int thinkingAnimationState = 0;

    private static final Color BACKGROUND_COLOR = new Color(48, 48, 48);
    private static final Color FOREGROUND_COLOR = new Color(172, 172, 172);
    private static final Color ACCENT_COLOR = new Color(44, 68, 108); // Cornflower Blue
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
        setSize(600, 512);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
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

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 0)); // Panel for buttons
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(sendButton);
        buttonPanel.add(clearButton);

        settingsButton = new JButton("Settings");
        settingsButton.setBackground(BUTTON_COLOR);
        settingsButton.setForeground(FOREGROUND_COLOR);
        settingsButton.setFont(BUTTON_FONT);
        settingsButton.setFocusPainted(false);
        settingsButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        buttonPanel.add(settingsButton);

        inputPanel.add(buttonPanel, BorderLayout.EAST);

        thinkingLabel = new JLabel("AI is thinking...");
        thinkingLabel.setForeground(FOREGROUND_COLOR);
        thinkingLabel.setFont(INPUT_FONT);
        thinkingLabel.setVisible(false); // Initially hidden
        inputPanel.add(thinkingLabel, BorderLayout.WEST);

        add(inputPanel, BorderLayout.SOUTH);

        // Initialize thinking animation timer
        thinkingAnimationTimer = new Timer(300, e -> {
            String text = "AI is thinking.";
            switch (thinkingAnimationState) {
                case 0:
                    thinkingLabel.setText(text + ".");
                    break;
                case 1:
                    thinkingLabel.setText(text + "..");
                    break;
                case 2:
                    thinkingLabel.setText(text + "...");
                    break;
            }
            thinkingAnimationState = (thinkingAnimationState + 1) % 3;
        });

        // Initialize ChatServices with default values
        currentModelParams = new ModelParams.Builder()
                .setTemperature(0.7)
                .setMaxTokens(1024)
                .setTopP(0.9).build();
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
                    appendStyledText("\nSettings updated!\n\n", FOREGROUND_COLOR, false, StyleConstants.ALIGN_CENTER);
                } else{
                    appendStyledText("\n\n\n", FOREGROUND_COLOR, false, StyleConstants.ALIGN_CENTER);
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
        appendStyledText("\n" + "─".repeat(50) + "\n\n", FOREGROUND_COLOR, false, StyleConstants.ALIGN_CENTER);

    }

    private void initializeChatService() {
        //LLM_Client client = new DefaultHttpClient(LLM_BASE_URL, Duration.ofSeconds(90), "local", false);
        LLM_Client client = new DefaultHttpClient(LLM_BASE_URL, Duration.ofSeconds(90), "local", false, new DefaultStreamResponseParser(), 50L);
        ChatServices_LowLevel lowLevelChatService = new ChatServices_LowLevel(client, currentModelName);

        // MemoryManager memory = new SlidingWindowMemory(MEMORY_WINDOW_SIZE);
        MemoryManager memory = new OptimizedSlidingWindowMemory(MEMORY_WINDOW_SIZE);
        //MemoryManager memory = new com.aiforjava.memory.ChatLogs.CachedFileMemory();

        PromptTemplate promptTemplate = new PromptTemplate("You are a helpful, friendly AI assistant.", "User: {user_message}\nAI:");

        chatService = new ChatServices(
                lowLevelChatService,
                memory,
                currentModelParams,
                promptTemplate
        );
    }

    private void sendMessage() {
        String userMessage = inputField.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }

        appendStyledText("You 👨‍💻: " + userMessage + "\n\n", FOREGROUND_COLOR, true, StyleConstants.ALIGN_RIGHT); // Add user message with two newlines for spacing
        inputField.setText(""); // Clear input field immediately after displaying user message

        if ("reset".equalsIgnoreCase(userMessage)) {
            chatService.reset();
            return;
        }

        // Disable input and button while AI is thinking
        inputField.setEnabled(false);
        sendButton.setEnabled(false);
        thinkingLabel.setVisible(true); // Show thinking indicator
        thinkingAnimationTimer.start(); // Start animation
        appendStyledText("AI 🤖: ", FOREGROUND_COLOR, true, StyleConstants.ALIGN_LEFT); // Prepare for AI response

        // Use SwingWorker for background LLM call
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception, LLMParseException {
                chatService.chatStream(userMessage, this::publish);
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    for (char c : chunk.toCharArray()) {
                        appendStyledText(String.valueOf(c), FOREGROUND_COLOR, false, StyleConstants.ALIGN_LEFT);
                        try {
                            Thread.sleep(10); // Adjust this value for desired typing speed
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
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
                    thinkingLabel.setVisible(false); // Hide thinking indicator
                    thinkingAnimationTimer.stop(); // Stop animation
                    inputField.requestFocusInWindow(); // Focus back to input field
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SwingChatbot().setVisible(true);
            }
        });
    }
}