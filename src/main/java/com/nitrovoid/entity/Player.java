package com.nitrovoid.entity;

import com.nitrovoid.input.InputHandler;
import java.awt.Graphics;
import java.awt.Color;
import com.nitrovoid.system.NitroSystem;
import com.nitrovoid.game.GameConfig;

public class Player extends Kendaraan {     
    private double currentSpeed = 5.0;
    private double normalSpeed = 5.0;
    private double maxSpeed = 18.0;
    private double acceleration = 6.0;
    private double deceleration = 3.0;
    private double lateralSpeed = 0;
    private final double lateralDecel = 1.8;
    private boolean boostActive = false;
    private double  boostTimer  = 0;
    public  static final double BOOST_DURATION = 3.0;
    private boolean nitroActive = false;
    private double  nitroTimer  = 0;
    public  double  nitroBoost  = 0;  
    public static final int ROAD_LEFT  = GameConfig.ROAD_LEFT;
    public static final int ROAD_RIGHT = GameConfig.ROAD_RIGHT;
    private int baseY = 460;
    private static final int FORWARD_OFFSET = 350;

    public Player() {
        x = GameConfig.PLAYER_START_X;
        y = GameConfig.PLAYER_START_Y;
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
        // === KECEPATAN MAJU ===
        double boostFloor = boostActive ? normalSpeed + 3.0 : normalSpeed;
        double topSpeed   = maxSpeed + nitroBoost;

        if (input.up) {
            currentSpeed += acceleration * deltaTime;
            if (currentSpeed > topSpeed) currentSpeed = topSpeed;
        } else {
            currentSpeed -= deceleration * deltaTime;
            if (currentSpeed < boostFloor) currentSpeed = boostFloor;
        }

        // === PERSPEKTIF Y ===
        // Makin cepat → player sedikit naik (ilusi maju)
        double speedRatio = (currentSpeed - normalSpeed) / (maxSpeed - normalSpeed);
        if (speedRatio < 0) speedRatio = 0;
        if (speedRatio > 1) speedRatio = 1;
        int targetY = baseY - (int)(speedRatio * FORWARD_OFFSET); // max naik 40px
        if (y > targetY) { y -= 2; if (y < targetY) y = targetY; }
        else if (y < targetY) { y += 2; if (y > targetY) y = targetY; }

        // === LATERAL — makin cepat makin susah dikontrol ===
        // lateralAccel naik proporsional dengan kecepatan
        double lateralAccel = 0.8 + (speedRatio * 2.0); // range 0.8 – 2.8
        double lateralMax   = 3.0 + (speedRatio * 4.0); // range 3.0 – 7.0

        if (input.left) {
            lateralSpeed -= lateralAccel;
            if (lateralSpeed < -lateralMax) lateralSpeed = -lateralMax;
        } else if (input.right) {
            lateralSpeed += lateralAccel;
            if (lateralSpeed > lateralMax) lateralSpeed = lateralMax;
        } else {
            // Decelerate — makin cepat makin lama berhenti (inersia)
            double decel = lateralDecel - (speedRatio * 0.8); // range 1.8 – 1.0
            if (lateralSpeed > 0) {
                lateralSpeed -= decel;
                if (lateralSpeed < 0) lateralSpeed = 0;
            } else if (lateralSpeed < 0) {
                lateralSpeed += decel;
                if (lateralSpeed > 0) lateralSpeed = 0;
            }
        }
        x += (int) lateralSpeed;
        if (x < ROAD_LEFT) x = ROAD_LEFT;
        if (x > ROAD_RIGHT - width) x = ROAD_RIGHT - width;

        // === TIMER NITRO ===
        if (nitroActive) {
            nitroTimer -= deltaTime;
            if (nitroTimer <= 0) {
                nitroActive = false;
                nitroBoost  = 0;
            }
        }

        // === TIMER BOOST ===
        if (boostActive) {
            boostTimer -= deltaTime;
            if (boostTimer <= 0) {
                boostActive = false;
            }
        }
    }

//    getter
    public int   getSpeedKmh()           { 
        double ratio = (currentSpeed - normalSpeed) / (maxSpeed - normalSpeed);
        return (int)(60 + ratio * 120); }
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