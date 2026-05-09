package com.nitrovoid.system;

public class NitroSystem {

    private int nitroCount;                    // stok nitro
    private final int maxNitro = 5;            // maksimal stok nitro
    private final int startNitro = 3;          // stok awal
    private boolean onCooldown = false;        // sedang cooldown atau tidak
    private double cooldownTimer = 0;          // hitung mundur cooldown
    private final double cooldownDuration = 5.0; // cooldown 5 detik
    private double lastUseTime = 0;            // waktu terakhir nitro dipakai
    private final double berturutWindow = 3.0; // window berturut-turut 3 detik
    private int useStreak = 0;                 // hitung pemakaian berturut-turut
    private double gameTime = 0;               // waktu total game berjalan

    // hasil timing nitro
    public enum NitroTiming {
        PERFECT, GOOD, MISS
    }

    private NitroTiming lastTiming = NitroTiming.MISS;

    public void reset() {
        nitroCount = startNitro;
        onCooldown = false;
        cooldownTimer = 0;
        useStreak = 0;
        gameTime = 0;
        lastUseTime = 0;
    }

    public void update(double deltaTime) {
        gameTime += deltaTime;

        // hitung mundur cooldown
        if (onCooldown) {
            cooldownTimer -= deltaTime;
            if (cooldownTimer <= 0) {
                cooldownTimer = 0;
                onCooldown = false;
                useStreak = 0; // reset streak setelah cooldown selesai
                System.out.println("Nitro cooldown selesai!");
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
            System.out.println("Nitro tidak bisa dipakai!");
            return NitroTiming.MISS;
        }
        nitroCount--;
        useStreak++;
        lastUseTime = gameTime;
        // cek apakah masuk cooldown
        if (useStreak >= 2) {
            onCooldown = true;
            cooldownTimer = cooldownDuration;
            System.out.println("Nitro cooldown! 5 detik");
        }
        // tentukan timing
        // timing ditentukan berdasarkan kapan tombol ditekan
        double roll = Math.random();
        if (roll < 0.3) {
            lastTiming = NitroTiming.PERFECT;
            System.out.println("PERFECT!");
        } else if (roll < 0.7) {
            lastTiming = NitroTiming.GOOD;
            System.out.println("GOOD!");
        } else {
            lastTiming = NitroTiming.MISS;
            System.out.println("MISS!");
        }
        return lastTiming;
    }
    // dipanggil saat ambil item nitro
    public void addNitro() {
        if (nitroCount < maxNitro) {
            nitroCount++;
        }
    }

    public boolean isOnCooldown() { return onCooldown; }
    public int getNitroCount() { return nitroCount; }
    public double getCooldownTimer() { return cooldownTimer; }
    public NitroTiming getLastTiming() { return lastTiming; }
}