package com.nitrovoid.entity;

import com.nitrovoid.input.InputHandler;
import java.awt.Graphics;
import java.awt.Color;
import com.nitrovoid.system.NitroSystem;

public class Player extends Kendaraan {     
    private double currentSpeed = 5.0;
    private double normalSpeed = 5.0;
    private double maxSpeed = 10.0;
    private double acceleration = 0.45;
    private double deceleration = 0.3;
    private double worldSpeed = 0;
    private boolean boostActive = false;
    private double  boostTimer  = 0;
    public  static final double BOOST_DURATION = 3.0;
    private boolean nitroActive = false;
    private double  nitroTimer  = 0;
    public  double  nitroBoost  = 0;  
    public static final int ROAD_LEFT  = 100;
    public static final int ROAD_RIGHT = 700;
    private int baseY = 520;
    private static final int FORWARD_OFFSET = 500;

    public Player() {
        x = 377;
        y = 520;
        width  = 45;
        height = 80;
    }

    public void applyNitro(NitroSystem.NitroTiming timing) {
        switch (timing) {
            case PERFECT:
                nitroBoost = 3.0;   // +3 px/frame ke worldSpeed
                nitroTimer = 3.0;
                break;
            case GOOD:
                nitroBoost = 1.5;   // +1.5 px/frame ke worldSpeed
                nitroTimer = 3.0;
                break;
            case MISS:
                nitroBoost = 0;
                nitroTimer = 0;
                break;
        }
        nitroActive = (timing != NitroSystem.NitroTiming.MISS);
    }

    public void applyBoost() {
        boostActive = true;
        boostTimer  = BOOST_DURATION;
    }
    
    public void update(InputHandler input, double deltaTime) {

        // Lateral — simpel, pakai currentSpeed
        if (input.left)  x -= (int) currentSpeed;
        if (input.right) x += (int) currentSpeed;
        if (x < ROAD_LEFT)          x = ROAD_LEFT;
        if (x > ROAD_RIGHT - width) x = ROAD_RIGHT - width;

        // Gas — smooth acceleration
        if (input.up) {
            currentSpeed += acceleration;
            if (currentSpeed > maxSpeed + nitroBoost) 
                currentSpeed = maxSpeed + nitroBoost;
        } else {
            currentSpeed -= deceleration;
            if (currentSpeed < normalSpeed) 
                currentSpeed = normalSpeed;
        }

        // offset proporsional — normalSpeed = y normal, maxSpeed = y paling atas
        double speedRatio = (currentSpeed - normalSpeed) / (maxSpeed - normalSpeed);
        if (speedRatio < 0) speedRatio = 0;
        if (speedRatio > 1) speedRatio = 1;

        int targetY = baseY - (int)(speedRatio * FORWARD_OFFSET);

        if (y > targetY) {
            y -= 3;
            if (y < targetY) y = targetY;
        } else if (y < targetY) {
            y += 3;
            if (y > targetY) y = targetY;
        }

        // Timer nitro
        if (nitroActive) {
            nitroTimer -= deltaTime;
            if (nitroTimer <= 0) {
                nitroActive = false;
                nitroBoost  = 0;
                System.out.println("Efek Nitro Habis");
            }
        }

        // Timer boost
        if (boostActive) {
            boostTimer -= deltaTime;
            if (boostTimer <= 0) {
                boostActive = false;
                System.out.println("Efek Boost Habis");
            }
        }
    }

//    getter
    public void setWorldSpeed(double ws) { this.worldSpeed = ws; }
    public int   getSpeedKmh()           { return (int)(worldSpeed * 20); } // 3→60, 9→180
    public boolean isBoostActive()       { return boostActive; }
    public boolean isNitroActive()       { return nitroActive; }
    public double getNitroBoost()        { return nitroBoost; }
    public boolean isBoostItemActive()   { return boostActive; }
    public double getCurrentSpeed()      { return currentSpeed; }

    @Override
    public void draw(Graphics g) {
        g.setColor(boostActive ? Color.YELLOW : Color.RED);
        g.fillRect(x, y, width, height);
    }
}