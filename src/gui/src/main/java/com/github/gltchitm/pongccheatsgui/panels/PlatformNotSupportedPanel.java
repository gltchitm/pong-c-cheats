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

public class PlatformNotSupportedPanel extends JPanel {
    public PlatformNotSupportedPanel() {
        super();

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JLabel platformNotSupportedLabel = new JLabel("Platform Not Supported", SwingConstants.CENTER);
        JLabel supportedPlatformLabel = new JLabel("Only Linux is currently supported", SwingConstants.CENTER);
        JButton quitButton = new JButton("Quit");

        platformNotSupportedLabel.setFont(new Font(platformNotSupportedLabel.getFont().getName(), Font.BOLD, 25));

        supportedPlatformLabel.setFont(new Font(supportedPlatformLabel.getFont().getName(), Font.PLAIN, 18));
        supportedPlatformLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        quitButton.setFocusable(false);
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });

        add(platformNotSupportedLabel, constraints);
        add(supportedPlatformLabel, constraints);
        add(quitButton, constraints);

        setVisible(false);
    }
}
