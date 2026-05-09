package com.nitrovoid.entity;

import java.awt.Graphics;
import java.awt.Color;

public class Item extends Kendaraan {
    public enum TipeItem {
        BOOST,      // menambah kecepatan sementara
        NITRO,      // menambah stok nitro
        SLOWMOTION  // menambah charge slow motion
    }

    private TipeItem tipe;

    public Item(int x, int y, TipeItem tipe) {
        this.x = x;
        this.y = y;
        this.tipe = tipe;
        this.width = 30;
        this.height = 30;
    }
    // item scrolling 
    public void update(double worldSpeed, double speedMultiplier) {
        y += worldSpeed * speedMultiplier;
    }
    public TipeItem getTipe() {
        return tipe;
    }

    @Override
    public void draw(Graphics g) {
        // warna beda tiap tipe — sementara pakai kotak warna dulu
        switch (tipe) {
            case BOOST:
                g.setColor(Color.BLUE);
                break;
            case NITRO:
                g.setColor(Color.ORANGE);
                break;
            case SLOWMOTION:
                g.setColor(Color.CYAN);
                break;
        }
        g.fillRect(x, y, width, height);
        // label tipe di atas kotak
        g.setColor(Color.BLACK);
        switch (tipe) {
            case BOOST:
                g.drawString("B", x + 10, y + 20);
                break;
            case NITRO:
                g.drawString("N", x + 10, y + 20);
                break;
            case SLOWMOTION:
                g.drawString("S", x + 10, y + 20);
                break;
        }
    }
}