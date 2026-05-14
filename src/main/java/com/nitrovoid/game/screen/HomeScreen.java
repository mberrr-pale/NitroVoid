package com.nitrovoid.game.screen;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class HomeScreen {

    private BufferedImage playImg;
    private BufferedImage settingsImg;
    private BufferedImage exitImg;
    private BufferedImage backgroundImg;

    private int selectedIndex = 0;

public HomeScreen() {

    backgroundImg = load("/images/background.png");

    playImg = load("/images/play.png");
    settingsImg = load("/images/setting.png");
    exitImg = load("/images/exit.png");
}
private BufferedImage load(String path) {
    try {
        return ImageIO.read(getClass().getResource(path));
    } catch (Exception e) {
        System.out.println("Gagal load: " + path);
        return null;
    }
}

    public void moveUp() {
        selectedIndex--;
        if (selectedIndex < 0) {
            selectedIndex = 2;
        }
    }

    public void moveDown() {
        selectedIndex++;
        if (selectedIndex > 2) {
            selectedIndex = 0;
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void draw(Graphics g, int width, int height) {

        // background
        if (backgroundImg != null) {
            g.drawImage(backgroundImg, 0, 0, width, height, null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
        }

        // title
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.drawString("NITROVOID", width / 2 - 150, 120);

        // posisi menu
        int centerX = width / 2 - 70;

        int startY = 180;
        int gap = 70;

        drawMenuImage(g, playImg, centerX, startY + (0 * gap), 0);
        drawMenuImage(g, settingsImg, centerX, startY + (1 * gap), 1);
        drawMenuImage(g, exitImg, centerX, startY + (2 * gap), 2);

        // help text
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(Color.GRAY);
        g.drawString(
                "UP/DOWN = Navigate | ENTER = Select",
                width / 2 - 150,
                height - 40
        );
    }

    private void drawMenuImage(Graphics g, BufferedImage img, int x, int y, int index) {

        if (img == null) {
            return;
        }

        int maxSize = 140;

        int imgW = img.getWidth();
        int imgH = img.getHeight();

        double scale = Math.min(
                (double) maxSize / imgW,
                (double) maxSize / imgH
        );

        // 🔥 ZOOM EFFECT
        double zoom = (selectedIndex == index) ? 1.2 : 1.0;

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
    }
}
