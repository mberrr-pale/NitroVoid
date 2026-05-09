package com.nitrovoid.system;

import com.nitrovoid.entity.Item;
import com.nitrovoid.entity.Item.TipeItem;
import java.util.ArrayList;
import java.util.Random;

public class ItemSpawner {

    private ArrayList<Item> items = new ArrayList<>();
    private Random random = new Random();
    private long lastSpawnTime = 0;
    private final long spawnInterval = 3000; // spawn tiap 3 detik
    private final int screenWidth = 800;

    public void update(double worldSpeed, int screenHeight, double speedMultiplier) {
        // Spawn item baru
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpawnTime > spawnInterval) {
            spawnItem();
            lastSpawnTime = currentTime;
        }
        // Update posisi semua item
        for (Item item : items) {
            item.update(worldSpeed, speedMultiplier);
        }
        // Hapus item yang keluar layar
        items.removeIf(i -> i.getY() > screenHeight);
    }

    private void spawnItem() {
        int itemX = random.nextInt(screenWidth - 30);
        int itemY = -50;
        // Probabilitas kemunculan item
        // BOOST = 60%, NITRO = 25%, SLOWMOTION = 15%
        int roll = random.nextInt(100);
        TipeItem tipe;
        if (roll < 60) {
            tipe = TipeItem.BOOST;
        } else if (roll < 85) {
            tipe = TipeItem.NITRO;
        } else {
            tipe = TipeItem.SLOWMOTION;
        }
        items.add(new Item(itemX, itemY, tipe));
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    // hapus item yang sudah diambil player
    public void removeItem(Item item) {
        items.remove(item);
    }
}