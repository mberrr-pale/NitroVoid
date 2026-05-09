package com.nitrovoid.system;

public class TimerSystem {

    private double timeLeft;       // sisa waktu (detik)
    private final double startTime = 60.0; // waktu awal 60 detik
    private boolean running = false;

    public TimerSystem() {
        timeLeft = startTime;
    }
    public void start() {
        running = true;
    }
    public void stop() {
        running = false;
    }
    public void reset() {
        timeLeft = startTime;
        running = false;
    }
    // deltaTime = waktu antar frame (supaya timer akurat meski FPS tidak stabil)
    public void update(double deltaTime) {
        if (!running) return;
        timeLeft -= deltaTime;
        // pastikan tidak minus
        if (timeLeft < 0) {
            timeLeft = 0;
        }
    }

    // nanti dipanggil saat player ambil item
    public void addTime(double seconds) {
        timeLeft += seconds;
    }
    public boolean isTimeUp() {
        return timeLeft <= 0;
    }
    public double getTimeLeft() {
        return timeLeft;
    }
}