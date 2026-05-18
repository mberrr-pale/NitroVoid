package com.nitrovoid.ui.screen;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.Locale;
import javax.imageio.ImageIO;

import com.nitrovoid.game.GameController;

public class GameOverScreen {

    private final GameController controller;
    private final int width, height;

    // Images
    private final BufferedImage backgroundImage;
    private final BufferedImage gameOverImage;
    private final BufferedImage restartButtonImage;
    private final BufferedImage backButtonImage;
    private final BufferedImage scoreImage;
    private final BufferedImage bestScoreImage;

    // Button bounds
    private Rectangle restartButtonBounds;
    private Rectangle backButtonBounds;

    // Hover
    private boolean isRestartHovered = false;
    private boolean isBackHovered = false;

    // Smooth scale
    private float restartScale = 1f;
    private float backScale = 1f;

    public GameOverScreen(GameController controller, int width, int height) {
        this.controller = controller;
        this.width = width;
        this.height = height;

        backgroundImage = load("/images/bg2.jpg");
        gameOverImage = load("/images/gameover.png");
        restartButtonImage = load("/images/restart.png");
        backButtonImage = load("/images/exit-to-menu.png");
        scoreImage = load("/images/score.png");
        bestScoreImage = load("/images/best-score.png");

        restartButtonBounds = new Rectangle();
        backButtonBounds = new Rectangle();
    }

    private BufferedImage load(String path) {
        try {
            return ImageIO.read(getClass().getResource(path));
        } catch (Exception e) {
            System.out.println("Missing: " + path);
            return null;
        }
    }

    // =========================
    // UPDATE ANIMATION
    // =========================
    public void update() {

        float targetRestart = isRestartHovered ? 1.15f : 1f;
        float targetBack = isBackHovered ? 1.15f : 1f;

        restartScale += (targetRestart - restartScale) * 0.15f;
        backScale += (targetBack - backScale) * 0.15f;
    }

    // =========================
    // HOVER
    // =========================
    public void handleMouseMove(Point p) {
        isRestartHovered = restartButtonBounds.contains(p);
        isBackHovered = backButtonBounds.contains(p);
    }

    // =========================
    // CLICK
    // =========================
    public int checkMouseClick(int mouseX, int mouseY) {

        Point mousePoint = new Point(mouseX, mouseY);

        if (restartButtonBounds.contains(mousePoint)) {
            return 0;
        }

        if (backButtonBounds.contains(mousePoint)) {
            return 1;
        }

        return -1;
    }

    public void updateHover(int mouseX, int mouseY) {

        Point mousePoint = new Point(mouseX, mouseY);

        isRestartHovered = restartButtonBounds.contains(mousePoint);

        isBackHovered = backButtonBounds.contains(mousePoint);
    }

    // =========================
    // DRAW BUTTON
    // =========================
    private void drawButton(
            Graphics2D g2,
            BufferedImage image,
            Rectangle bounds,
            float scale
    ) {

        if (image == null) {
            return;
        }

        int centerX = bounds.x + bounds.width / 2;
        int centerY = bounds.y + bounds.height / 2;

        int drawW = (int) (bounds.width * scale);
        int drawH = (int) (bounds.height * scale);

        int drawX = centerX - drawW / 2;
        int drawY = centerY - drawH / 2;

        g2.drawImage(image, drawX, drawY, drawW, drawH, null);
    }

    // =========================
    // DRAW
    // =========================
    public void draw(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;

        int centerX = width / 2 - 20;
        int currentY = 30;

        // Background
        if (backgroundImage != null) {

            int imgW = backgroundImage.getWidth();
            int imgH = backgroundImage.getHeight();

            float scaleX = (float) width / imgW;
            float scaleY = (float) height / imgH;
            float scale = Math.max(scaleX, scaleY);

            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);

            int x = (width - drawW) / 2;
            int y = (height - drawH) / 2;

            g2.drawImage(backgroundImage, x, y, drawW, drawH, null);
        }

        // GAME OVER
        if (gameOverImage != null) {

            int titleW = (int) (gameOverImage.getWidth() * 0.6);
            int titleH = (int) (gameOverImage.getHeight() * 0.6);

            g2.drawImage(
                    gameOverImage,
                    centerX - titleW / 2,
                    currentY,
                    titleW,
                    titleH,
                    null
            );

            currentY += titleH;
        }

        // SCORE IMAGE
        if (scoreImage != null) {

            int imgW = (int) (scoreImage.getWidth() * 0.3);
            int imgH = (int) (scoreImage.getHeight() * 0.3);

            g2.drawImage(
                    scoreImage,
                    centerX - imgW / 2,
                    currentY,
                    imgW,
                    imgH,
                    null
            );

            currentY += imgH + 60;
        }

        // SCORE TEXT
        g2.setFont(new Font("Arial", Font.BOLD, 80));
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        String scoreText = nf.format(controller.getScore());

        int textX = centerX - g2.getFontMetrics().stringWidth(scoreText) / 2;

        int textY = currentY;

        // OUTLINE MERAH
        g2.setColor(Color.RED);
        g2.drawString(scoreText, textX - 1, textY);
        g2.drawString(scoreText, textX + 1, textY);
        g2.drawString(scoreText, textX, textY - 1);
        g2.drawString(scoreText, textX, textY + 1);

        // TEXT PUTIH
        g2.setColor(Color.WHITE);
        g2.drawString(scoreText, textX, textY);
        currentY += 10;

        // BEST SCORE
        if (bestScoreImage != null) {

            int imgW = (int) (bestScoreImage.getWidth() * 0.3);
            int imgH = (int) (bestScoreImage.getHeight() * 0.3);

            g2.drawImage(
                    bestScoreImage,
                    centerX - imgW / 2,
                    currentY,
                    imgW,
                    imgH,
                    null
            );

            currentY += imgH + 40;
        }

        g2.setFont(new Font("Arial", Font.BOLD, 50));

        String bestScoreText = nf.format(controller.getBestScore());

        g2.drawString(
                bestScoreText,
                centerX - g2.getFontMetrics().stringWidth(bestScoreText) / 2,
                currentY
        );

        currentY += 40;

        // UPDATE BUTTON POSITION
        restartButtonBounds.setBounds(
                centerX - 100,
                currentY,
                210,
                70
        );

        currentY += 90;

        backButtonBounds.setBounds(
                centerX - 100,
                currentY,
                210,
                60
        );

        // DRAW BUTTONS
        drawButton(
                g2,
                restartButtonImage,
                restartButtonBounds,
                restartScale
        );

        drawButton(
                g2,
                backButtonImage,
                backButtonBounds,
                backScale
        );
    }
}
