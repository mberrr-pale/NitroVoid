package com.nitrovoid.system;

public class DifficultyScaler {

    // Dipanggil tiap frame dari GameController
    public static void apply(int score, EnemySpawner enemySpawner) {
        long interval;
        double speedBonus; // dalam persen, dipakai di GameController

        if      (score < 500)  { interval = 2000; }
        else if (score < 1000) { interval = 1850; }
        else if (score < 1500) { interval = 1700; }
        else if (score < 2000) { interval = 1550; }
        else if (score < 2500) { interval = 1400; }
        else if (score < 3000) { interval = 1250; }
        else                   { interval = 1100; }

        enemySpawner.setSpawnInterval(interval);
    }

    // Mengembalikan speed multiplier berdasarkan skor (1.0 = normal) 
    public static double getSpeedMultiplier(int score) {
        if      (score < 500)  return 1.00;
        else if (score < 1000) return 1.05;
        else if (score < 1500) return 1.10;
        else if (score < 2000) return 1.15;
        else if (score < 2500) return 1.20;
        else if (score < 3000) return 1.25;
        else                   return 1.30;
    }
}