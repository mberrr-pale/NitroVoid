package com.nitrovoid.ui.components;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import com.nitrovoid.system.MusicSystem;

public class BtnVolume {

    private BufferedImage imgOn;
    private BufferedImage imgOff;

    private Rectangle bounds;

    private MusicSystem music;

    private boolean hovered = false;

    private int maxSize = 100;


    public BtnVolume(MusicSystem music) {
        this.music = music;

        imgOn = load("/images/volume-on.png");
        imgOff = load("/images/volume-off.png");
    }

    // ================= LOAD IMAGE =================
    private BufferedImage load(String path) {
        try {
            return ImageIO.read(getClass().getResource(path));
        } catch (IOException e) {
            System.out.println("Missing volume image: " + path);
            return null;
        }
    }

    // ================= DRAW =================
    public void draw(Graphics g, int screenW) {
        BufferedImage img = music.isVolumeOn() ? imgOn : imgOff;

        if (img == null) {
            return;
        }

        int imgW = img.getWidth();
        int imgH = img.getHeight();

        // Scale proportionally
        double scale = Math.min((double) maxSize / imgW, (double) maxSize / imgH);
        int newW = (int) (imgW * scale);
        int newH = (int) (imgH * scale);

        int x = screenW - newW - 20;
        int y = 20;

        bounds = new Rectangle(x, y, newW, newH);

        g.drawImage(img, x, y, newW, newH, null);
    }

    // ================= CLICK =================
    public boolean handleClick(int mx, int my) {
        if (bounds != null && bounds.contains(mx, my)) {
            music.toggleVolume();
            return true;
        }
        return false;
    }

    // ================= HOVER (optional future) =================
    public boolean isHovered(int mx, int my) {
        return bounds != null && bounds.contains(mx, my);
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
