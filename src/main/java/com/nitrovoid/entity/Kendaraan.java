package com.nitrovoid.entity;

import java.awt.Graphics;
import java.awt.Color;

public class Kendaraan {

    protected int x, y;
    protected int speed = 5;
    protected int width = 50;
    protected int height = 80;

    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}