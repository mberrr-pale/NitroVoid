package com.nitrovoid.entity;

import com.nitrovoid.input.InputHandler;
import java.awt.Graphics;
import java.awt.Color;
import com.nitrovoid.system.NitroSystem;

public class Player extends Kendaraan {
    private double currentSpeed;
    private final double normalSpeed = 7;
    private final double maxSpeed = 12;
    private final double acceleration = 0.2;
    private final double deceleration = 0.1;
    private boolean nitroActive = false;
    private double nitroTimer = 0;
    private double nitroBoost = 0;

    public Player() {
        x = 375;
        y = 500;
        currentSpeed = normalSpeed;
    }

    public void applyNitro(NitroSystem.NitroTiming timing) {
        switch (timing) {
            case PERFECT:
                nitroBoost = maxSpeed * 0.5; // +50% dari maxSpeed
                nitroTimer = 2.0;            // efek 2 detik
                break;
            case GOOD:
                nitroBoost = maxSpeed * 0.25; // +25% dari maxSpeed
                nitroTimer = 2.0;
                break;
            case MISS:
                nitroBoost = 0;
                nitroTimer = 0;
                break;
        }
        nitroActive = timing != NitroSystem.NitroTiming.MISS;
    }
    
    public void update(InputHandler input, double deltaTime) {
        // Movement
         if (input.left && x > 0) {
            x -= (int) currentSpeed;
        }
        if (input.right && x < 800 - width) {
            x += (int) currentSpeed;
        }

        // Gas
        if (input.up) {
            currentSpeed += acceleration;
            if (currentSpeed > maxSpeed + nitroBoost) {
                currentSpeed = maxSpeed + nitroBoost;
            }
        } else {
            currentSpeed -= deceleration;
            if (currentSpeed < normalSpeed) {
                currentSpeed = normalSpeed;
            }
        }
        if (nitroActive){
            nitroTimer -= deltaTime;
            if (nitroTimer <= 0){
                nitroActive = false;
                nitroBoost = 0;
                System.out.println("Efek Nitro Habis");
            }
        }
    }

    public double getcurrentSpeed() {
        return currentSpeed;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);
    }
}