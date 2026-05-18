package com.nitrovoid.system;

import com.nitrovoid.game.GameController.MapType;

public class ScoreManager {
    private double score;
    private double scoreAccumulator = 0;

    private MapType currentMap = MapType.KTT;
    
    public void setMap(MapType map) {
        this.currentMap = map; 
    }
    public ScoreManager() {
        score = 0;
    }

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
                    10;
            break;

        // MAP LIWET
        case LIWET:
            scoreAccumulator +=
                    currentSpeed *
                    deltaTime *
                    25;
            break;

        // MAP MAGETAN
        case MGT:
            scoreAccumulator +=
                    currentSpeed *
                    deltaTime *
                    50;
            break;
    }

    score = (int) scoreAccumulator;
}

    // Bonus sisa waktu — hanya dipanggil saat tabrakan enemy
    public void addTimeBonus(double timeLeft) {
        score += (int)(timeLeft * 10);
        scoreAccumulator = score; // sync accumulator supaya tidak overlap
    }

    public int getScore()     { return (int) score; }
}