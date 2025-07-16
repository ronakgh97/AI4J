package com.aiforjava.demo.ChatBotApp;

import javax.swing.*;
import java.awt.*;

public class MessageBubble extends RoundedPanel {
    private JTextArea textArea;

    public MessageBubble(String text, Color backgroundColor, Color foregroundColor, boolean isUser) {
        super(20); // 20 is the corner radius
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        setForeground(foregroundColor);

        textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(backgroundColor);
        textArea.setForeground(foregroundColor);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));

        // Add padding
        int padding = 10;
        setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));

        add(textArea, BorderLayout.CENTER);
    }
}
