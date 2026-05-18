package com.nitrovoid.system;

public class NitroSystem {

    private int nitroCount;                    
    private final int maxNitro = 5;            
    private final int startNitro = 3;          
    private boolean onCooldown = false;        
    private double cooldownTimer = 0;          
    private final double cooldownDuration = 5.0; 
    private double lastUseTime = 0;            
    private final double berturutWindow = 3.0; 
    private int useStreak = 0;                 
    private double gameTime = 0;
    private double barPosition = 0.0;
    private double barDirection = 1.0;
    private final double barSpeed = 0.8;
    private final double PERFECT_MIN = 0.45;
    private final double PERFECT_MAX = 0.55;
    private final double GOOD_MIN = 0.33;
    private final double GOOD_MAX = 0.70;

    private NitroTiming lastTiming = NitroTiming.MISS;

    // hasil timing nitro
    public enum NitroTiming {
        PERFECT, GOOD, MISS
    }

    public void reset() {
        nitroCount = startNitro;
        onCooldown = false;
        cooldownTimer = 0;
        useStreak = 0;
        gameTime = 0;
        lastUseTime = 0;
        barPosition = 0.0;
        barDirection = 1.0;
    }

    public void update(double deltaTime) {
        gameTime += deltaTime;
        
        // Gerakan bar bolak balik
        barPosition += barDirection * barSpeed * deltaTime;
        if(barPosition >= 1.0){
            barPosition = 1.0;
            barDirection = -1.0;
        } else if (barPosition <= 0.0) {
            barPosition = 0.0;
            barDirection = 1.0;
        }

        // hitung mundur cooldown
        if (onCooldown) {
            cooldownTimer -= deltaTime;
            if (cooldownTimer <= 0) {
                cooldownTimer = 0;
                onCooldown = false;
                useStreak = 0; // reset streak setelah cooldown selesai
            }
        }
        // reset streak kalau sudah lebih dari 3 detik sejak pemakaian terakhir
        if (!onCooldown && useStreak > 0) {
            if (gameTime - lastUseTime > berturutWindow) {
                useStreak = 0;
            }
        }
    }

    public NitroTiming activate() {
        // tidak bisa pakai kalau cooldown atau stok habis
        if (onCooldown || nitroCount <= 0) {
            return NitroTiming.MISS;
        }
        nitroCount--;
        useStreak++;
        lastUseTime = gameTime;
        // cek apakah masuk cooldown
        if (useStreak >= 2) {
            onCooldown = true;
            cooldownTimer = cooldownDuration;
        }
        // tentukan timing
         if (barPosition >= PERFECT_MIN && barPosition <= PERFECT_MAX) {
            lastTiming = NitroTiming.PERFECT;
        } else if (barPosition >= GOOD_MIN && barPosition <= GOOD_MAX) {
            lastTiming = NitroTiming.GOOD;
        } else {
            lastTiming = NitroTiming.MISS;
        }
        return lastTiming;
    }

    public void addNitro() {
        if (nitroCount < maxNitro) nitroCount++;
    }

    public boolean isOnCooldown() { return onCooldown; }
    public int getNitroCount() { return nitroCount; }
    public double getCooldownTimer() { return cooldownTimer; }
    public NitroTiming getLastTiming() { return lastTiming; }
    public double getBarPosition() { return barPosition; }
}