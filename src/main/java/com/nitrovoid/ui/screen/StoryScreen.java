package com.nitrovoid.ui.screen;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class StoryScreen extends JPanel {

    private final JFXPanel fxPanel;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;

    private Runnable onFinish;
    private String currentVideoPath;

    public StoryScreen(Runnable onFinish) {
        this.onFinish = onFinish;

        setLayout(new BorderLayout());
        setBackground(java.awt.Color.BLACK);

        fxPanel = new JFXPanel();
        fxPanel.setPreferredSize(new Dimension(800, 600));

        add(fxPanel, BorderLayout.CENTER);

        Platform.setImplicitExit(false);

        System.out.println("[StoryScreen] initialized");
    }

    // =====================================================
    // PUBLIC API
    // =====================================================
    public void playVideo(String videoPath) {
        this.currentVideoPath = videoPath;

        Platform.runLater(() -> {
            cleanupMedia();
            buildAndPlay(videoPath);
        });
    }

    public void stopVideo() {
        Platform.runLater(this::cleanupMedia);
    }

    public String getCurrentVideoPath() {
        return currentVideoPath;
    }

    // =====================================================
    // CORE BUILD
    // =====================================================
    private void buildAndPlay(String videoPath) {

        URL url = getClass().getResource(videoPath);
        if (url == null) {
            System.out.println("[ERROR] Video not found: " + videoPath);
            return;
        }

        Media media = new Media(url.toExternalForm());
        mediaPlayer = new MediaPlayer(media);
        mediaView = new MediaView(mediaPlayer);

        mediaView.setPreserveRatio(true);

        StackPane root = new StackPane(mediaView, createSkipLabel());

        Scene scene = new Scene(root);
        fxPanel.setScene(scene);

        mediaView.fitWidthProperty().bind(scene.widthProperty());
        mediaView.fitHeightProperty().bind(scene.heightProperty());

        setupControls(scene);
        setupResizeHandling();
        setupMediaEvents();

        requestFocusSafe();

        mediaPlayer.setOnReady(mediaPlayer::play);
    }

    // =====================================================
    // UI COMPONENT
    // =====================================================
    private Label createSkipLabel() {
        Label label = new Label("Press ANY KEY to skip");

        label.setTextFill(Color.rgb(255, 255, 255, 0.7)); // 70% opaque
        label.setFont(Font.font("Arial", 20));
        label.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6); -fx-padding: 5px; -fx-background-radius: 5;");
        StackPane.setAlignment(label, javafx.geometry.Pos.BOTTOM_RIGHT);
        StackPane.setMargin(label, new Insets(0, 20, 20, 0)); // top, right, bottom, left

        return label;
    }

    // =====================================================
    // EVENTS
    // =====================================================
    private void setupControls(Scene scene) {
        scene.setOnKeyPressed(e -> skipVideo());
    }

    private void setupMediaEvents() {
        mediaPlayer.setOnError(()
                -> System.out.println("[Media Error] " + mediaPlayer.getError())
        );

        mediaPlayer.setOnEndOfMedia(this::finishVideo);
    }

    private void setupResizeHandling() {
        fxPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (mediaView == null) {
                    return;
                }

                Platform.runLater(() -> {
                    mediaView.setFitWidth(fxPanel.getWidth());
                    mediaView.setFitHeight(fxPanel.getHeight());
                });
            }
        });
    }

    // =====================================================
    // FLOW CONTROL
    // =====================================================
    private void skipVideo() {
        System.out.println("[StoryScreen] skipped");
        finishVideo();
    }

    private void finishVideo() {
        cleanupMedia();
        clearScene();

        if (onFinish != null) {
            SwingUtilities.invokeLater(onFinish);
        }
    }

    // =====================================================
    // CLEANUP
    // =====================================================
    private void cleanupMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        mediaView = null;
    }

    private void clearScene() {
        fxPanel.setScene(new Scene(new StackPane(), 800, 600));
    }

    // =====================================================
    // FOCUS FIX
    // =====================================================
    private void requestFocusSafe() {
        Platform.runLater(() -> {
            fxPanel.requestFocus();
        });
    }
}
