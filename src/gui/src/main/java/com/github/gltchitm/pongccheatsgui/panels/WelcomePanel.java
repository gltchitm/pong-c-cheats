package com.github.gltchitm.pongccheatsgui.panels;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.github.gltchitm.pongccheatsgui.App;

public class WelcomePanel extends JPanel {
    public WelcomePanel() {
        super();

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JLabel welcomeLabel = new JLabel("Welcome to Pong C Cheats!", SwingConstants.CENTER);
        JLabel instructionsLabel = new JLabel("Start Pong C and press 'Attach'", SwingConstants.CENTER);
        JButton attachButton = new JButton("Attach");

        welcomeLabel.setFont(new Font(welcomeLabel.getFont().getName(), Font.BOLD, 25));

        instructionsLabel.setFont(new Font(instructionsLabel.getFont().getName(), Font.PLAIN, 18));
        instructionsLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        attachButton.setFocusable(false);
        attachButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                App.attach();
            }
        });

        add(welcomeLabel, constraints);
        add(instructionsLabel, constraints);
        add(attachButton, constraints);

        setVisible(false);
    }
}
