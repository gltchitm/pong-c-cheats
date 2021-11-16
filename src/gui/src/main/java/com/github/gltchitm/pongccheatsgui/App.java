package com.github.gltchitm.pongccheatsgui;

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JFrame;

import com.formdev.flatlaf.FlatDarkLaf;

import com.github.gltchitm.pongccheatsgui.communicator.Communicator;
import com.github.gltchitm.pongccheatsgui.communicator.Packet.ClientboundPacket;
import com.github.gltchitm.pongccheatsgui.modals.GenericErrorModal;
import com.github.gltchitm.pongccheatsgui.panels.AttachingPanel;
import com.github.gltchitm.pongccheatsgui.panels.ControlPanel;
import com.github.gltchitm.pongccheatsgui.panels.MainPanel;
import com.github.gltchitm.pongccheatsgui.panels.PlatformNotSupportedPanel;
import com.github.gltchitm.pongccheatsgui.panels.WelcomePanel;

public class App {
    private static JFrame window;
    private static MainPanel mainPanel;
    private static PlatformNotSupportedPanel platformNotSupportedPanel;
    private static WelcomePanel welcomePanel;
    private static AttachingPanel attachingPanel;
    private static ControlPanel controlPanel;

    private static Communicator communicator;

    public static void main(String[] args) {
        FlatDarkLaf.setup();

        window = new JFrame("Pong C Cheats");

        window.setSize(new Dimension(430, 195));
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);

        platformNotSupportedPanel = new PlatformNotSupportedPanel();
        mainPanel = new MainPanel();
        welcomePanel = new WelcomePanel();
        attachingPanel = new AttachingPanel();
        controlPanel = new ControlPanel();

        mainPanel.add(platformNotSupportedPanel);
        mainPanel.add(welcomePanel);
        mainPanel.add(attachingPanel);
        mainPanel.add(controlPanel);

        window.add(mainPanel);

        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            welcomePanel.setVisible(true);
        } else {
            platformNotSupportedPanel.setVisible(true);
        }

        window.setVisible(true);
    }
    public static Communicator getCommunicator() {
        return communicator;
    }
    public static void attach() {
        welcomePanel.setVisible(false);
        attachingPanel.setVisible(true);

        try {
            if (communicator == null) {
                communicator = new Communicator();
            }

            ClientboundPacket packet = communicator.attach();

            if (packet.error == ClientboundPacket.OK) {
                new Thread(new Runnable() {
                    public void run() {
                        controlPanel.start();
                        controlPanel.setVisible(true);
                        attachingPanel.setVisible(false);
                    }
                }).start();
            } else if (packet.error == ClientboundPacket.BUSY) {
                communicator = null;

                GenericErrorModal.show("Cannot Attach", "The daemon is busy");

                welcomePanel.setVisible(true);
                attachingPanel.setVisible(false);
            } else if (packet.error == ClientboundPacket.NOT_FOUND) {
                GenericErrorModal.show("Cannot Attach", "Make sure Pong C is running");

                welcomePanel.setVisible(true);
                attachingPanel.setVisible(false);
            }
        } catch (IOException e) {
            communicator = null;

            GenericErrorModal.show("Cannot Attach", "Make sure the Pong C Cheats daemon is running");

            attachingPanel.setVisible(false);
            welcomePanel.setVisible(true);
        }
    }
    public static void closeControlPanel() {
        controlPanel.setVisible(false);
        welcomePanel.setVisible(true);
        communicator = null;
    }
}
