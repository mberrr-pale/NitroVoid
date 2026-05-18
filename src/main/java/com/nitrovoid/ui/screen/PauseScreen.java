package com.nitrovoid.ui.screen;

import com.nitrovoid.system.MusicSystem;
import com.nitrovoid.ui.components.BestScore;
import com.nitrovoid.ui.components.BtnVolume;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class PauseScreen {

    private BufferedImage resumeImg;
    private BufferedImage exitImg;
    private BufferedImage restartImg;
    private BestScore bestScoreUI;
    private BtnVolume btnVolume;
    private MusicSystem music;

    // Tombol hover & bounds
    private Rectangle resumeBounds;
    private Rectangle restartBounds;
    private Rectangle exitBounds;
    private int hoveredIndex = -1; // 0=resume, 1=restart, 2=exit

    public PauseScreen(MusicSystem music) {
        this.music = music;

        resumeImg = load("/images/resume.png");
        restartImg = load("/images/restart.png");
        exitImg = load("/images/back-to-menu.png");

        btnVolume = new BtnVolume(music);
        bestScoreUI = new BestScore();
    }

    private BufferedImage load(String path) {
        try {
            return ImageIO.read(getClass().getResource(path));
        } catch (Exception e) {
            System.out.println("Missing: " + path);
            return null;
        }
    }

    // Panggil ini setiap update mouse position
    public void updateHover(int mouseX, int mouseY) {
        hoveredIndex = -1;
        if (resumeBounds != null && resumeBounds.contains(mouseX, mouseY)) {
            hoveredIndex = 0;
        } else if (restartBounds != null && restartBounds.contains(mouseX, mouseY)) {
            hoveredIndex = 1;
        } else if (exitBounds != null && exitBounds.contains(mouseX, mouseY)) {
            hoveredIndex = 2;
        }
    }

    public int checkMouseClick(int mouseX, int mouseY) {
        if (btnVolume.handleClick(mouseX, mouseY)) {
            return -2;
        }
        if (resumeBounds != null && resumeBounds.contains(mouseX, mouseY)) {
            return 0;
        }
        if (restartBounds != null && restartBounds.contains(mouseX, mouseY)) {
            return 1;
        }
        if (exitBounds != null && exitBounds.contains(mouseX, mouseY)) {
            return 2;
        }
        return -1;
    }

    public void draw(Graphics g, int screenWidth, int screenHeight) {
        Graphics2D g2d = (Graphics2D) g.create();

        // Overlay semi-transparent
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, screenWidth, screenHeight);

        // Text PAUSED
        Font defaultFont = g2d.getFont();
        g2d.setColor(Color.WHITE);
        g2d.setFont(defaultFont.deriveFont(Font.BOLD, 40f));
        String pauseText = "PAUSED";
        int textWidth = g2d.getFontMetrics().stringWidth(pauseText);
        g2d.drawString(pauseText, screenWidth / 2 - textWidth / 2, screenHeight / 2 - 200);

        // Best score & volume
        bestScoreUI.draw(g2d, 20, 20);
        btnVolume.draw(g2d, screenWidth);

        // Tombol dengan efek zoom
        int buttonYStart = screenHeight / 2 - 100;
        double scale = 0.18;
        int spacing = 30;
        int y = buttonYStart;

        BufferedImage[] buttons = {resumeImg, restartImg, exitImg};
        Rectangle[] bounds = new Rectangle[buttons.length];

        for (int i = 0; i < buttons.length; i++) {
            BufferedImage img = buttons[i];
            if (img == null) {
                continue;
            }

            // Zoom saat hover
            boolean active = (hoveredIndex == i);
            double zoom = active ? 1.1 : 1.0;

            int w = (int) (img.getWidth() * scale * zoom);
            int h = (int) (img.getHeight() * scale * zoom);
            int x = screenWidth / 2 - w / 2;

            g2d.drawImage(img, x, y, w, h, null);

            // Simpan bounds untuk hover
            bounds[i] = new Rectangle(x, y, w, h);

            y += h + spacing;
        }

        // Update bounds untuk deteksi hover
        resumeBounds = bounds[0];
        restartBounds = bounds[1];
        exitBounds = bounds[2];

        g2d.dispose();
    }
}
