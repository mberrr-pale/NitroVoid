package com.nitrovoid.system;

public class SlowMotionSystem {

    private int charge;
    private final int startCharge = 2;      // minimal
    private final int maxCharge = 5;        // maximum
    private boolean active = false;         // slow motion sedang aktif atau tidak
    private boolean onCooldown = false;     // sedang cooldown atau tidak
    private double cooldownTimer = 0;
    private final double cooldownDuration = 5.0; // cooldown 5 detik
    private double activeTimer = 0;
    private final double activeDuration = 3.0; // efek slow berlangsung 3 detik
    private double slowMultiplier = 0.4; // enemy jalan 40% dari kecepatan normal

    public void reset() {
        charge = startCharge;
        active = false;
        onCooldown = false;
        cooldownTimer = 0;
        activeTimer = 0;
    }

    public void update(double deltaTime) {
        // hitung mundur efek slow
        if (active) {
            activeTimer -= deltaTime;
            if (activeTimer <= 0) {
                active = false;
                activeTimer = 0;
                onCooldown = true;
                cooldownTimer = cooldownDuration;
                System.out.println("Slow motion habis, cooldown 5 detik...");
            }
        }
        // hitung mundur cooldown
        if (onCooldown) {
            cooldownTimer -= deltaTime;
            if (cooldownTimer <= 0) {
                cooldownTimer = 0;
                onCooldown = false;
                System.out.println("Slow motion cooldown selesai!");
            }
        }
    }

    public boolean activate() {
        // tidak bisa pakai kalau charge habis atau cooldown
        if (charge <= 0) {
            System.out.println("Charge habis! Ambil item slow motion.");
            return false;
        }
        if (onCooldown) {
            System.out.println("Slow motion masih cooldown!");
            return false;
        }
        if (active) {
            System.out.println("Slow motion sudah aktif!");
            return false;
        }
        charge--;
        active = true;
        activeTimer = activeDuration;
        System.out.println("Slow motion aktif! Charge tersisa: " + charge);
        return true;
    }

    public void addCharge() {
        if (charge < maxCharge) {
            charge++;
            System.out.println("Charge slow motion bertambah! Total: " + charge);
        }
    }

    public double getSpeedMultiplier() {
        return active ? slowMultiplier : 1.0;
    }

    public boolean isActive() { return active; }
    public boolean isOnCooldown() { return onCooldown; }
    public int getCharge() { return charge; }
    public double getCooldownTimer() { return cooldownTimer; }
}