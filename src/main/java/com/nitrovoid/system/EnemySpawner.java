package com.nitrovoid.system;

import com.nitrovoid.entity.Enemy;
import com.nitrovoid.entity.Item;
import com.nitrovoid.game.GameConfig;
import java.util.ArrayList;
import java.util.Random;

public class EnemySpawner {

    private ArrayList<Enemy> enemies = new ArrayList<>();
    private Random random = new Random();
    private long lastSpawnTime = 0;
    private long spawnInterval = 2000; 
    private final int ROAD_LEFT = GameConfig.ROAD_LEFT;
    private final int ROAD_RIGHT = GameConfig.ROAD_RIGHT;

    // Jarak minimum antar enemy dan enemy vs item
    private final int MIN_ENEMY_GAP = 80;
    private final int MIN_ENEMY_ITEM_GAP = 60;

    public void update(double worldSpeed, int screenHeight, double speedMultiplier,
                       ArrayList<Item> items) {
        long now = System.currentTimeMillis();
        if (now - lastSpawnTime > spawnInterval) {
            spawnEnemy(items);
            lastSpawnTime = now;
        }
        for (Enemy enemy : enemies) {
            enemy.update(worldSpeed, speedMultiplier);
        }
        enemies.removeIf(e -> e.getY() > screenHeight);
    }

    private void spawnEnemy(ArrayList<Item> items) {
        final int MAX_TRIES = 10;
        for (int t = 0; t < MAX_TRIES; t++) {
            int ex = ROAD_LEFT + random.nextInt((ROAD_RIGHT - 80) - ROAD_LEFT); // 45 = enemy width
            int ey = -100;

            if (!tooCloseToOtherEnemies(ex, ey) &&
                (items == null || !tooCloseToItems(ex, ey, items))) {
                enemies.add(new Enemy(ex, ey));
                return;
            }
        }
        // Jika semua percobaan gagal, skip spawn kali ini
    }

    private boolean tooCloseToOtherEnemies(int ex, int ey) {
        for (Enemy e : enemies) {
            int dx = Math.abs(e.getX() - ex);
            int dy = Math.abs(e.getY() - ey);
            if (dx < 45 + MIN_ENEMY_GAP && dy < 80 + MIN_ENEMY_GAP) return true;
        }
        return false;
    }

    private boolean tooCloseToItems(int ex, int ey, ArrayList<Item> items) {
        for (Item item : items) {
            int dx = Math.abs(item.getX() - ex);
            int dy = Math.abs(item.getY() - ey);
            if (dx < 45 + MIN_ENEMY_ITEM_GAP && dy < 80 + MIN_ENEMY_ITEM_GAP) return true;
        }
        return false;
    }

//    Dipanggil oleh DifficultyScaler untuk mengubah spawn interval */
    public void setSpawnInterval(long interval) {
        this.spawnInterval = interval;
    }

    public ArrayList<Enemy> getEnemies() { return enemies; }
}