package com.nitrovoid.system;

import com.nitrovoid.game.GameController.MapType;

public class ScoreManager {

    private static ScoreManager instance;
    private double score;
    private double scoreAccumulator = 0;

    private MapType currentMap = MapType.KTT;
    
    public void setMap(MapType map) {
        this.currentMap = map; 
    }
    public ScoreManager() {
        score = 0;
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

    switch (currentMap) {
        // MAP KETINTANG
        case KTT:
            scoreAccumulator +=
                    currentSpeed *
                    deltaTime *
                    15;
            break;

        // MAP LIWET
        case LIWET:
            scoreAccumulator +=
                    currentSpeed *
                    deltaTime *
                    20;
            break;

        // MAP MAGETAN
        case MGT:
            scoreAccumulator +=
                    currentSpeed *
                    deltaTime *
                    30;
            break;
    }

    score = (int) scoreAccumulator;
}

    // Bonus sisa waktu — hanya dipanggil saat tabrakan enemy
    public void addTimeBonus(double timeLeft) {
        score += (int) (timeLeft * 20);
        scoreAccumulator = score; // sync accumulator supaya tidak overlap
    }

    public int getScore()     { return (int) score; }
}
