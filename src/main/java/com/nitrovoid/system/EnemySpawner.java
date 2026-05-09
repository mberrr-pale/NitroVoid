package com.nitrovoid.system;

import com.nitrovoid.entity.Enemy;
import java.util.ArrayList;
import java.util.Random;

public class EnemySpawner {

    private ArrayList<Enemy> enemies = new ArrayList<>();
    private Random random = new Random();
    private long lastSpawnTime = 0;
    private final long spawnInterval = 1500; // spawn tiap 1.5 detik
    private final int screenWidth = 800;

    public void update(double worldSpeed, int screenHeight, double speedMultiplier) {
        // Spawn enemy baru
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpawnTime > spawnInterval) {
            spawnEnemy();
            lastSpawnTime = currentTime;
        }
        //kirim speedmultiplier ke tiap enemy
        for (Enemy enemy : enemies){
            enemy.update(worldSpeed, speedMultiplier);
        }
        // Hapus enemy yang keluar layar
        enemies.removeIf(e -> e.getY() > screenHeight);
    }

    private void spawnEnemy() {
        int enemyX = random.nextInt(screenWidth - 60);
        int enemyY = -100;
        enemies.add(new Enemy(enemyX, enemyY));
    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }
}