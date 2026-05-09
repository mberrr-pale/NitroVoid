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
        score += (int)(deltaTime*75);
    }
    public int getScore() {
        return (int) score;
    }
}