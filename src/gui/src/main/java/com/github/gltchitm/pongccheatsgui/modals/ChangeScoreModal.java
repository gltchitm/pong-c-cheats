package com.github.gltchitm.pongccheatsgui.modals;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ChangeScoreModal {
    public static Integer show(String playerName) {
        JPanel content = new JPanel();
        JLabel changeScoreLabel = new JLabel("Change Score");
        JLabel makeSurePongIsRunningLabel = new JLabel("Change the score for the " + playerName + " player");
        JSpinner scoreSpinner = new JSpinner();

        content.setLayout(new GridLayout(3, 1));

        changeScoreLabel.setFont(new Font(changeScoreLabel.getFont().getName(), Font.BOLD, 18));

        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(scoreSpinner, "#");
        scoreSpinner.setEditor(editor);
        ((JSpinner.NumberEditor) scoreSpinner.getEditor()).getTextField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent event) {
                JFormattedTextField source = ((JFormattedTextField) event.getSource());
                if (
                    !Character.isDigit(event.getKeyChar()) ||
                    (source.getText().length() >= 6 && source.getSelectedText() == null)
                ) {
                    event.consume();
                }
            }
        });
        scoreSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
                if ((int) scoreSpinner.getValue() >= 999_999) {
                    scoreSpinner.setValue(999_999);
                } else if ((int) scoreSpinner.getValue() < 0) {
                    scoreSpinner.setValue(0);
                }
            }
        });

        content.add(changeScoreLabel);
        content.add(makeSurePongIsRunningLabel);
        content.add(scoreSpinner);

        String[] options = {"Cancel", "Done"};
        int response = JOptionPane.showOptionDialog(
            null,
            content,
            "Change Score",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]
        );

        return response == 1 ? (int) scoreSpinner.getValue() : null;
    }
}
