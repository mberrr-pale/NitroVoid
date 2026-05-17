package com.nitrovoid.system;

import com.nitrovoid.entity.Item;
import com.nitrovoid.entity.Item.TipeItem;
import com.nitrovoid.entity.Enemy;
import com.nitrovoid.game.GameConfig;
import java.util.ArrayList;
import java.util.Random;

public class ItemSpawner {

    private ArrayList<Item> items = new ArrayList<>();
    private Random random = new Random();
    private long lastSpawnTime = 0;
    private final long spawnInterval = 4000; 
    private final int ROAD_LEFT = GameConfig.ROAD_LEFT;
    private final int ROAD_RIGHT = GameConfig.ROAD_RIGHT;
    private final int MIN_ITEM_GAP = 100;
    private final int MIN_ITEM_ENEMY_GAP = 60;
    
    public void update(double worldSpeed, int screenHeight, double speedMultiplier, 
                       ArrayList<Enemy>enemies) {
        long now = System.currentTimeMillis();
        if (now - lastSpawnTime > spawnInterval) {
            spawnItem(enemies); 
            lastSpawnTime = now;
        }
        // Update posisi semua item
        for (Item item : items) {
            item.update(worldSpeed, speedMultiplier);
        }
        // Hapus item yang keluar layar
        items.removeIf(i -> i.getY() > screenHeight);
    }

    private void spawnItem(ArrayList<Enemy> enemies) {
        final int ITEM_W = 35;
        final int MAX_TRIES = 10;
 
        for (int t = 0; t < MAX_TRIES; t++) {
            // Spawn hanya dalam batas jalan
            int range = (ROAD_RIGHT - ITEM_W) - ROAD_LEFT;
            int ix = ROAD_LEFT + random.nextInt(range);
            int iy = -50;
 
            if (!tooCloseToOtherItems(ix, iy) &&
                (enemies == null || !tooCloseToEnemies(ix, iy, enemies))) {
 
                // Probabilitas: BOOST 50%, NITRO 30%, SLOWMOTION 20%
                int roll = random.nextInt(100);
                TipeItem tipe;
                if      (roll < 50) tipe = TipeItem.BOOST;
                else if (roll < 80) tipe = TipeItem.NITRO;
                else                tipe = TipeItem.SLOWMOTION;
 
                items.add(new Item(ix, iy, tipe));
                return;
            }
        }
        // Semua percobaan gagal → skip spawn kali ini
    }
 
    private boolean tooCloseToOtherItems(int ix, int iy) {
        for (Item item : items) {
            int dx = Math.abs(item.getX() - ix);
            int dy = Math.abs(item.getY() - iy);
            if (dx < 35 + MIN_ITEM_GAP && dy < 35 + MIN_ITEM_GAP) return true;
        }
        return false;
    }
 
    private boolean tooCloseToEnemies(int ix, int iy, ArrayList<Enemy> enemies) {
        for (Enemy e : enemies) {
            int dx = Math.abs(e.getX() - ix);
            int dy = Math.abs(e.getY() - iy);
            if (dx < 45 + MIN_ITEM_ENEMY_GAP && dy < 80 + MIN_ITEM_ENEMY_GAP) return true;
        }
        return false;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    // hapus item yang sudah diambil player
    public void removeItem(Item item) {
        items.remove(item);
    }
}