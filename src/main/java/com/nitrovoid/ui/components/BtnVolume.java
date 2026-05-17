package com.nitrovoid.ui.components;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import com.nitrovoid.system.MusicSystem;

public class BtnVolume {

    private BufferedImage imgOn;
    private BufferedImage imgOff;

    private Rectangle bounds;

    private MusicSystem music;

    private boolean hovered = false;

    public BtnVolume(MusicSystem music,
                     BufferedImage imgOn,
                     BufferedImage imgOff) {

        this.music = music;
        this.imgOn = imgOn;
        this.imgOff = imgOff;
    }

    // ================= DRAW =================
    public void draw(Graphics g, int screenW) {

        BufferedImage img =
                music.isVolumeOn() ? imgOn : imgOff;

        if (img == null) return;

        int maxSize = 100;

        int imgW = img.getWidth();
        int imgH = img.getHeight();

        double scale = Math.min(
                (double) maxSize / imgW,
                (double) maxSize / imgH
        );

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