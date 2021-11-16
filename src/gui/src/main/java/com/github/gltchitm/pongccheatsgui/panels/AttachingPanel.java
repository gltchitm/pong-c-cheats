package com.github.gltchitm.pongccheatsgui.panels;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class AttachingPanel extends JPanel {
    public AttachingPanel() {
        super();

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JLabel attachingLabel = new JLabel("Attaching...", SwingConstants.CENTER);
        JLabel startGameLabel = new JLabel("Make sure Pong C is not waiting at the main menu.", SwingConstants.CENTER);

        attachingLabel.setFont(new Font(attachingLabel.getFont().getName(), Font.PLAIN, 30));

        add(attachingLabel, constraints);
        add(startGameLabel, constraints);

        setVisible(false);
    }
}
