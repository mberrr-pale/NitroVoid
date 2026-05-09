package com.nitrovoid.entity;

import java.awt.Graphics;
import java.awt.Color;

public class Enemy extends Kendaraan {
    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
        speed = 3;
    }
    // tambah parameter speedMultiplier
    public void update(double worldSpeed, double speedMultiplier) {
        y += worldSpeed * speedMultiplier;
    }
    
    @Override
    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(x, y, width, height);
    }
}