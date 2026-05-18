package com.nitrovoid.system;

public class ScoreManager {

    private static ScoreManager instance;
    private double score;
    private double scoreAccumulator;
    private int bestScore;

    // Constructor
    private ScoreManager() {
        score = 0;
        scoreAccumulator = 0;
        bestScore = 0;
    }

    // Singleton
    public static ScoreManager getInstance() {

        if (instance == null) {
            instance = new ScoreManager();
        }

        return instance;
    }

    // Reset score saat game restart
    public void reset() {
        score = 0;
        scoreAccumulator = 0;
    }

    public void update(double deltaTime, double currentSpeed) {
        scoreAccumulator += currentSpeed * deltaTime * 15;
        score = (int) scoreAccumulator;
    }

    // Bonus sisa waktu — hanya dipanggil saat tabrakan enemy
    public void addTimeBonus(double timeLeft) {
        score += (int) (timeLeft * 10);
        scoreAccumulator = score; // sync accumulator supaya tidak overlap
        updateBestScore();
    }

    // Dipanggil saat waktu habis (tanpa bonus)
    public void finalizeScore() {
        updateBestScore();
    }

    private void updateBestScore() {
        if ((int) score > bestScore) {
            bestScore = (int) score;
            // TODO: unlock map — aktifkan saat sudah siap
            // saveBestScore();
            // MapManager.checkUnlock(bestScore);
        }
    }

    // TODO: implementasi save/load best score ke file — aktifkan saat sudah siap
    // private void saveBestScore() {
    //     try {
    //         java.util.prefs.Preferences prefs = 
    //             java.util.prefs.Preferences.userNodeForPackage(ScoreManager.class);
    //         prefs.putInt("bestScore", bestScore);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
    //
    // public void loadBestScore() {
    //     try {
    //         java.util.prefs.Preferences prefs = 
    //             java.util.prefs.Preferences.userNodeForPackage(ScoreManager.class);
    //         bestScore = prefs.getInt("bestScore", 0);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    public int getScore() {
        return (int) score;
    }

    public int getBestScore() {
        return bestScore;
    }
}
