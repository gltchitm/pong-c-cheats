package com.github.gltchitm.pongccheatsgui.panels;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.github.gltchitm.pongccheatsgui.App;
import com.github.gltchitm.pongccheatsgui.communicator.Communicator;
import com.github.gltchitm.pongccheatsgui.communicator.Packet.ClientboundPacket;
import com.github.gltchitm.pongccheatsgui.modals.ChangeScoreModal;
import com.github.gltchitm.pongccheatsgui.modals.GenericErrorModal;

public class ControlPanel extends JPanel {
    private JLabel leftPlayerScore;
    private JLabel rightPlayerScore;
    private Timer timer;

    private boolean changingScore = false;
    private boolean detaching = false;

    public ControlPanel() {
        super();

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JPanel playersPanel = new JPanel();

        JPanel leftPlayerPanel = new JPanel();
        JLabel leftPlayerLabel = new JLabel("Left Player Score");
        leftPlayerScore = new JLabel("", SwingConstants.CENTER);
        JButton leftPlayerChangeScoreButton = new JButton("Change Score");

        JPanel rightPlayerPanel = new JPanel();
        JLabel rightPlayerLabel = new JLabel("Right Player Score");
        rightPlayerScore = new JLabel("", SwingConstants.CENTER);
        JButton rightPlayerChangeScoreButton = new JButton("Change Score");

        JPanel bottomControls = new JPanel();
        JButton detachButton = new JButton("Detach");
        JButton quitButton = new JButton("Quit");

        leftPlayerChangeScoreButton.setFocusable(false);
        rightPlayerChangeScoreButton.setFocusable(false);

        leftPlayerChangeScoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    changingScore = true;

                    Integer score = ChangeScoreModal.show("left");

                    if (score != null) {
                        ClientboundPacket packet = App.getCommunicator().changeLeftScore(score);
                        if (packet.error != ClientboundPacket.OK) {
                            timer.cancel();

                            if (packet.error == ClientboundPacket.NOT_ATTACHED) {
                                GenericErrorModal.show(
                                    "Cannot Change Score",
                                    "The daemon lost connection to Pong C. Pong C Cheats will now disconnect."
                                );
                            } else {
                                GenericErrorModal.show(
                                    "Cannot Change Score",
                                    "Received an unexpected response packet. Pong C Cheats will now disconnect."
                                );
                            }

                            App.closeControlPanel();
                        }
                    }

                    changingScore = false;
                } catch (IOException e) {
                    timer.cancel();
                    GenericErrorModal.show(
                        "Cannot Change Score",
                        "An error occurred while changing the score. Pong C Cheats will now disconnect."
                    );
                    App.closeControlPanel();
                }
            }
        });
        rightPlayerChangeScoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    changingScore = true;

                    Integer score = ChangeScoreModal.show("right");

                    if (score != null) {
                        ClientboundPacket packet = App.getCommunicator().changeRightScore(score);
                        if (packet.error != ClientboundPacket.OK) {
                            timer.cancel();

                            if (packet.error == ClientboundPacket.NOT_ATTACHED) {
                                GenericErrorModal.show(
                                    "Cannot Change Score",
                                    "The daemon lost connection to Pong C. Pong C Cheats will now disconnect."
                                );
                            } else {
                                GenericErrorModal.show(
                                    "Cannot Change Score",
                                    "Received an unexpected response packet. Pong C Cheats will now disconnect."
                                );
                            }

                            App.closeControlPanel();
                        }
                    }

                    changingScore = false;
                } catch (IOException e) {
                    timer.cancel();
                    GenericErrorModal.show(
                        "Cannot Change Score",
                        "An error occurred while changing the score. Pong C Cheats will now disconnect."
                    );
                    App.closeControlPanel();
                }
            }
        });
        detachButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    detaching = true;

                    timer.cancel();

                    ClientboundPacket packet = App.getCommunicator().detach();

                    if (packet.error != ClientboundPacket.OK) {
                        GenericErrorModal.show(
                            "Cannot Detach",
                            "Received an unexpected response packet. Pong C Cheats will not disconnect."
                        );
                    }
                } catch (IOException e) {
                    GenericErrorModal.show(
                        "Cannot Detach",
                        "An error occurred while detaching. Pong C Cheats will now disconnect."
                    );
                } finally {
                    App.closeControlPanel();
                }
            }
        });

        detachButton.setFocusable(false);
        quitButton.setFocusable(false);

        quitButton.setVisible(false);

        leftPlayerLabel.setFont(new Font(leftPlayerLabel.getFont().getName(), Font.BOLD, 17));
        rightPlayerLabel.setFont(new Font(rightPlayerLabel.getFont().getName(), Font.BOLD, 17));

        leftPlayerPanel.add(leftPlayerLabel);
        leftPlayerPanel.add(leftPlayerScore);
        leftPlayerPanel.add(leftPlayerChangeScoreButton);
        leftPlayerPanel.setLayout(new GridLayout(3, 1));
        leftPlayerPanel.setBorder(new EmptyBorder(0, 0, 0, 50));

        rightPlayerPanel.add(rightPlayerLabel);
        rightPlayerPanel.add(rightPlayerScore);
        rightPlayerPanel.add(rightPlayerChangeScoreButton);
        rightPlayerPanel.setLayout(new GridLayout(3, 1));

        bottomControls.add(detachButton);
        bottomControls.add(quitButton);
        bottomControls.setBorder(new EmptyBorder(15, 0, 0, 0));

        playersPanel.add(leftPlayerPanel);
        playersPanel.add(rightPlayerPanel);

        add(playersPanel, constraints);
        add(bottomControls, constraints);

        setVisible(false);
    }
    public void start() {
        updateScores();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!changingScore) {
                    updateScores();
                }
            }
        }, 0, 50);
    }
    private void updateScores() {
        Communicator communicator = App.getCommunicator();

        try {
            ClientboundPacket leftScorePacket = communicator.getLeftScore();
            ClientboundPacket rightScorePacket = communicator.getRightScore();

            if (
                leftScorePacket.error != ClientboundPacket.LEFT_SCORE ||
                rightScorePacket.error != ClientboundPacket.RIGHT_SCORE
            ) {
                timer.cancel();

                if (
                    leftScorePacket.error == ClientboundPacket.NOT_ATTACHED ||
                    rightScorePacket.error == ClientboundPacket.NOT_ATTACHED
                ) {
                    if (!detaching) {
                        GenericErrorModal.show(
                            "Cannot Refresh Scores",
                            "The daemon lost connection to Pong C. Pong C Cheats will now disconnect."
                        );
                    }
                } else {
                    GenericErrorModal.show(
                        "Cannot Refresh Scores",
                        "An error occurred when refreshing scores. Pong C Cheats will now disconnect."
                    );
                }

                App.closeControlPanel();
            } else {
                leftPlayerScore.setText(leftScorePacket.score.toString());
                rightPlayerScore.setText(rightScorePacket.score.toString());
            }
        } catch (IOException e) {
            timer.cancel();
            GenericErrorModal.show(
                "Cannot Refresh Scores",
                "An error occurred when refreshing scores. Pong C Cheats will now disconnect."
            );
            App.closeControlPanel();
        }
    }
}
