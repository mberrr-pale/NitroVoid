package com.nitrovoid.game;

import com.nitrovoid.entity.Player;
import com.nitrovoid.entity.Enemy;
import com.nitrovoid.entity.Item;
import com.nitrovoid.input.InputHandler;
import com.nitrovoid.util.CollisionDetector;
import com.nitrovoid.system.TimerSystem;
import com.nitrovoid.system.ScoreManager;
import com.nitrovoid.system.EnemySpawner;
import com.nitrovoid.system.ItemSpawner;
import com.nitrovoid.system.NitroSystem;
import com.nitrovoid.system.SlowMotionSystem;
import java.util.ArrayList;

public class GameController {
    private GameState currentState = GameState.MENU;
    private Player player;
    private InputHandler input;
    private TimerSystem timer = new TimerSystem();
    private ScoreManager scoreManager = new ScoreManager();
    private EnemySpawner enemySpawner = new EnemySpawner();
    private ItemSpawner itemSpawner = new ItemSpawner();
    private NitroSystem nitroSystem = new NitroSystem();
    private boolean nitroPressed = false; // biar ga spam
    private SlowMotionSystem slowMotionSystem = new SlowMotionSystem();
    private boolean slowPressed = false; // biar ga spam
    private final int screenWidth = 800;
    private final int screenHeight = 600;

    public GameController(Player player, InputHandler input) {
        this.player = player;
        this.input = input;
    }
    public void update(double deltaTime) {
        if (currentState == GameState.PLAYING) {
            updatePlaying(deltaTime);
        }
    }
    private void updatePlaying(double deltaTime ) {
        timer.update(deltaTime);
        scoreManager.update(deltaTime);
        player.update(input, deltaTime);
        nitroSystem.update(deltaTime);
        slowMotionSystem.update(deltaTime);

        if (timer.isTimeUp()){
            currentState = GameState.GAMEOVER;
            System.out.println("WAKTU HABIS - GAME OVER");
        }
        // cek supaya ga spam
        if (input.nitro && !nitroPressed){
            nitroPressed = true;
            NitroSystem.NitroTiming timing = nitroSystem.activate();
            player.applyNitro(timing);
        }
        if (!input.nitro){
            nitroPressed = false;
        }
        // cek input slow motion
        if (input.slowMotion && !slowPressed){
            slowPressed = true;
            slowMotionSystem.activate();
        }
        if (!input.slowMotion){
            slowPressed = false;
        }
        //Update Spawner
        enemySpawner.update(player.getcurrentSpeed(), screenHeight, slowMotionSystem.getSpeedMultiplier());
        itemSpawner.update(player.getcurrentSpeed(), screenHeight, slowMotionSystem.getSpeedMultiplier());
        // Cek collision player vs enemy
        for (Enemy enemy : enemySpawner.getEnemies()) {
            if (CollisionDetector.isColliding(player, enemy)) {
                currentState = GameState.GAMEOVER;
                System.out.println("GAME OVER");
            }
        }
        // Cek collision player vs item 
        for (Item item : itemSpawner.getItems()) {
            if(CollisionDetector.isColliding(player, item)){
                handleItemPickup(item);
                itemSpawner.removeItem(item);
                break;
            }
        }
    }
    
    private void handleItemPickup(Item item){
        switch (item.getTipe()) {
            case BOOST:
                System.out.println("Item BOOST diambil!");
                break;
            case NITRO:
                nitroSystem.addNitro();
                System.out.println("Item NITRO diambil!");
                break;
            case SLOWMOTION:
                slowMotionSystem.addCharge();
                System.out.println("Item SLOWMOTION diambil!");
                break;
        }
    }
    
    public double getTimeLeft(){
        return timer.getTimeLeft();
    }

    // GETTER — dipakai GamePanel untuk render
    public GameState getCurrentState() { return currentState; }
    public Player getPlayer() { return player; }
    public ArrayList<Enemy> getEnemies() { return enemySpawner.getEnemies(); }
    public ArrayList<Item> getItems() { return itemSpawner.getItems(); }
    public int getScore() { return scoreManager.getScore(); }
    public int getNitroCount() { return nitroSystem.getNitroCount(); }
    public boolean isNitroCooldown() { return nitroSystem.isOnCooldown(); }
    public double getNitroCooldown() { return nitroSystem.getCooldownTimer(); }
    public int getSlowCharge() { return slowMotionSystem.getCharge(); }
    public boolean isSlowActive() { return slowMotionSystem.isActive(); }
    public boolean isSlowCooldown() { return slowMotionSystem.isOnCooldown(); }
    public double getSlowCooldown() { return slowMotionSystem.getCooldownTimer(); }

    // Untuk testing — nanti tombol ini bisa dipanggil dari menu
    public void startGame() {
        currentState = GameState.PLAYING;
        timer.start();
        scoreManager.reset();
        nitroSystem.reset();
        slowMotionSystem.reset();
    }
}