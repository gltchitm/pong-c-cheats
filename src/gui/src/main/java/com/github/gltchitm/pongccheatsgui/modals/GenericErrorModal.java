package com.github.gltchitm.pongccheatsgui.modals;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class GenericErrorModal {
    public static void show(String title, String text) {
        JPanel content = new JPanel();
        JLabel titleLabel = new JLabel(title);

        content.setLayout(new GridLayout(2, 1));

        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 18));

        content.add(titleLabel);
        content.add(new JLabel(text));

        JOptionPane.showMessageDialog(null, content, title, JOptionPane.ERROR_MESSAGE);
    }
}
