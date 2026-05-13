package com.nitrovoid.system;

public class ScoreManager {

    private double score;

    public ScoreManager() {
        score = 0;
    }
    public void reset() {
        score = 0;
    }
    public void update(double deltaTime) {
        score += (int)(deltaTime*100);
    }
       // --- Bonus event ---
    public void addBoostBonus()         { score += 50;  }
    public void addNitroBonus()         { score += 75;  }
    public void addSlowMotionBonus()    { score += 100; }
    public void addNitroPerfectBonus()  { score += 100; }
    public void addNitroGoodBonus()     { score += 50;  }
 
    //Bonus sisa waktu saat game over: sisaWaktu × 50 
    public void addTimeBonus(double timeLeft) {
        score += (int)(timeLeft * 50);
    }
    public int getScore() {
        return (int) score;
    }
}