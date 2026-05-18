package com.nitrovoid.main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.nitrovoid.game.GamePanel;

import javafx.embed.swing.JFXPanel;

public class Main {
    public static void main(String[] args) {
        new JFXPanel();

        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("NitroVoid");

            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);
            GamePanel gamePanel = new GamePanel(window);
            // IMPORTANT (anti blank screen issue)
            window.setContentPane(gamePanel);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
            gamePanel.startGameThread();
        });
    }
}