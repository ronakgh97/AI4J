package com.aiforjava.demo;

import com.aiforjava.exception.ExceptionHandler;
import com.aiforjava.llm.Chat.HighLevel.ChatServices;
import com.aiforjava.llm.Chat.LowLevel.ChatServices_LowLevel;
import com.aiforjava.llm.DefaultHttpClient;
import com.aiforjava.llm.LLM_Client;
import com.aiforjava.llm.ModelParams;
import com.aiforjava.llm.Prompt.PromptTemplate;
import com.aiforjava.memory.MemoryManager;
import com.aiforjava.memory.SlidingWindowMemory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Duration;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.TitledBorder;

public class SwingChatbot extends JFrame {

    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JButton sendButton;
    private ChatServices chatService;

    private static final String LLM_BASE_URL = "http://localhost:1234";
    private static String MODEL_NAME = "google/gemma-3-1b"; // Or your preferred model
    private static int MEMORY_WINDOW_SIZE = 50; // Keep last 50 messages in memory

    private ModelParams currentModelParams;
    private String currentModelName;
    private JButton settingsButton;

    private static final Color BACKGROUND_COLOR = new Color(48, 48, 48);
    private static final Color FOREGROUND_COLOR = new Color(172, 172, 172);
    private static final Color ACCENT_COLOR = new Color(0, 0, 0); // Cornflower Blue
    private static final Color BUTTON_COLOR = new Color(0, 0, 0, 171);
    private static final Font CHAT_FONT = new Font("Consolas", Font.BOLD, 18);
    private static final Font INPUT_FONT = new Font("Consolas", Font.BOLD, 16);
    private static final Font BUTTON_FONT = new Font("Consolas", Font.BOLD, 14);

    private JLabel createLabel(String text, Color foreground, Font font) {
        JLabel label = new JLabel(text);
        label.setForeground(foreground);
        label.setFont(font);
        return label;
    }

    public SwingChatbot() {
        setTitle("AI4J Chatbot");
        setSize(512, 512);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Initialize UI components
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(BACKGROUND_COLOR);
        chatArea.setForeground(FOREGROUND_COLOR);
        chatArea.setFont(CHAT_FONT);
        chatArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
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

        inputField = new JTextField();
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
        sendButton.setFocusPainted(false); // Remove focus border
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        JButton clearButton = new JButton("Clear Chat");
        clearButton.setBackground(BUTTON_COLOR);
        clearButton.setForeground(FOREGROUND_COLOR);
        clearButton.setFont(BUTTON_FONT);
        clearButton.setFocusPainted(false);
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
        add(inputPanel, BorderLayout.SOUTH);

        // Initialize ChatServices with default values
        currentModelParams = new ModelParams.Builder().setTemperature(0.7).setMaxTokens(1024).setTopP(0.9).build();
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
                chatArea.setText("");
                chatService.reset();
                chatArea.append("Chat history cleared. New conversation started.\n\n");
            }
        });

        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SettingsDialog settingsDialog = new SettingsDialog(SwingChatbot.this, currentModelParams, currentModelName);
                settingsDialog.setVisible(true);
                if (settingsDialog.getAppliedModelParams() != null) {
                    currentModelParams = settingsDialog.getAppliedModelParams();
                    currentModelName = settingsDialog.getAppliedModelName();
                    initializeChatService(); // Re-initialize chat service with new settings
                    chatArea.append("\nLLM settings updated!\n\n");
                }
            }
        });

        // Display initial message
        chatArea.append("Welcome to AI4J Swing Chatbot!\n");
        chatArea.append("Type 'reset' to clear conversation.\n\n");
    }

    private void initializeChatService() {
        LLM_Client client = new DefaultHttpClient(LLM_BASE_URL, Duration.ofSeconds(90), "local");
        ChatServices_LowLevel lowLevelChatService = new ChatServices_LowLevel(client, currentModelName);

        MemoryManager memory = new SlidingWindowMemory(MEMORY_WINDOW_SIZE);

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

        chatArea.append("You: " + userMessage + "\n");
        inputField.setText(""); // Clear input field

        if ("reset".equalsIgnoreCase(userMessage)) {
            chatService.reset();
            chatArea.append("Chat history cleared. New conversation started.\n\n");
            return;
        }

        // Disable input and button while AI is thinking
        inputField.setEnabled(false);
        sendButton.setEnabled(false);
        chatArea.append("AI: "); // Prepare for AI response

        // Use SwingWorker for background LLM call
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                chatService.chatStream(userMessage, this::publish);
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    chatArea.append(chunk);
                }
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions from doInBackground
                    chatArea.append("\n\n"); // New line after AI's response
                } catch (Exception e) {
                    chatArea.append("\nError: " + e.getMessage() + "\n\n");
                    ExceptionHandler.handle(e);
                } finally {
                    // Re-enable input and button
                    inputField.setEnabled(true);
                    sendButton.setEnabled(true);
                    inputField.requestFocusInWindow(); // Focus back to input field
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SwingChatbot().setVisible(true);
            }
        });
    }
}