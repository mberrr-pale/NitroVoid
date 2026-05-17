package com.nitrovoid.entity;

import java.util.Random;

public class Enemy extends Kendaraan {
    private int vehicleIndex;
    private static final Random random = new Random();
    
    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
        width = 45;
        height = 80;
        speed = 3;
        vehicleIndex = random.nextInt(5);
    }

    public void update(double worldSpeed, double speedMultiplier) {
        y += worldSpeed * speedMultiplier;
    }

    public int getVehicleIndex() {
        return vehicleIndex;
    }
}
