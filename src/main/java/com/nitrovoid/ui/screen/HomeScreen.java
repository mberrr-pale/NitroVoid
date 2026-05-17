package com.nitrovoid.ui.screen;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.nitrovoid.system.MusicSystem;
import com.nitrovoid.ui.components.BestScore;
import com.nitrovoid.ui.components.BtnVolume;

public class HomeScreen extends JPanel {

    private BufferedImage playImg;
    private BufferedImage exitImg;
    private BufferedImage backgroundImg;
    private BufferedImage titleImg;
    private BufferedImage volumeOnImg;
    private BufferedImage volumeOffImg;
    private BtnVolume btnVolume;
    private MusicSystem music;

    private int selectedIndex = 0;
    private int hoveredIndex = -1;

    private BestScore bestScoreUI;
    private Rectangle playBounds;
    private Rectangle exitBounds;

    private boolean mouseActive = false;

    public HomeScreen(MusicSystem music) {

        this.music = music;
        setOpaque(false);
        backgroundImg = load("/images/bg3.png");
        titleImg = load("/images/title.png");

        playImg = load("/images/play.png");
        exitImg = load("/images/exit.png");

        volumeOnImg = load("/images/volume-on.png");
        volumeOffImg = load("/images/volume-off.png");
       

        btnVolume = new BtnVolume(
                music,
                volumeOnImg,
                volumeOffImg
        );

        bestScoreUI = new BestScore(0);
    }

    private BufferedImage load(String path) {
        try {
            return ImageIO.read(getClass().getResource(path));
        } catch (Exception e) {
            System.out.println("Missing: " + path);
            return null;
        }
    }

    // ================= INPUT =================
    public void moveUp() {
        selectedIndex = (selectedIndex + 1) % 2;
    }

    public void moveDown() {
        selectedIndex = (selectedIndex + 1) % 2;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    // ================= HOVER =================
    public void updateHover(int mx, int my) {

        mouseActive = true;

        hoveredIndex = -1;

        if (playBounds != null && playBounds.contains(mx, my)) {
            hoveredIndex = 0;
        } else if (exitBounds != null && exitBounds.contains(mx, my)) {
            hoveredIndex = 1;
        }
    }

    public void resetHover() {
        hoveredIndex = -1;
    }

    // ================= CLICK =================
    public int checkMouseClick(int mx, int my) {

        if (btnVolume.handleClick(mx, my)) {
            return -2;
        }

        if (playBounds != null && playBounds.contains(mx, my)) {
            return 0;
        }
        if (exitBounds != null && exitBounds.contains(mx, my)) {
            return 1;
        }

        return -1;
    }

    // ================= DRAW =================
    public void draw(Graphics g, int w, int h) {

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        // BACKGROUND
        if (backgroundImg != null) {
            g.drawImage(backgroundImg, 0, 0, w, h, null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, w, h);
        }

        bestScoreUI.draw(g, 20, 20);
        btnVolume.draw(g, w);
        drawTitle(g, w);

        int centerX = w / 2 - 70;
        int startY = 250;
        int gap = 70;

        playBounds = drawButton(g, playImg, centerX, startY, 0);
        exitBounds = drawButton(g, exitImg, centerX, startY + gap, 1);
    }

    private void drawTitle(Graphics g, int w) {

        if (titleImg == null) {
            return;
        }

        int maxW = 400;

        int imgW = titleImg.getWidth();
        int imgH = titleImg.getHeight();

        double scale = Math.min((double) maxW / imgW, 1.0);

        int newW = (int) (imgW * scale);
        int newH = (int) (imgH * scale);

        int x = (w - newW) / 2;
        int y = 20;

        g.drawImage(titleImg, x, y, newW, newH, null);
    }

    // ================= BUTTON =================
    private Rectangle drawButton(Graphics g, BufferedImage img, int x, int y, int index) {

        if (img == null) {
            return null;
        }

        int maxSize = 140;

        int imgW = img.getWidth();
        int imgH = img.getHeight();

        double scale = Math.min(
                (double) maxSize / imgW,
                (double) maxSize / imgH
        );

        // 🔥 ZOOM EFFECT
        boolean active;

        if (hoveredIndex != -1) {
            active = (hoveredIndex == index);
        } else {
            active = (selectedIndex == index);
        }

        double zoom = active ? 1.2 : 1.0;

        int newW = (int) (imgW * scale * zoom);
        int newH = (int) (imgH * scale * zoom);

        // center offset (biar tetap rapi)
        int offsetX = (maxSize - newW) / 2;
        int offsetY = (maxSize - newH) / 2;

        g.drawImage(
                img,
                x + offsetX,
                y + offsetY,
                newW,
                newH,
                null
        );
        return new Rectangle(x + offsetX, y + offsetY, newW, newH);
    }

}
