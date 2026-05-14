package com.nitrovoid.game;

import java.util.ArrayList;

import com.nitrovoid.entity.Enemy;
import com.nitrovoid.entity.Item;
import com.nitrovoid.entity.Player;
import com.nitrovoid.input.InputHandler;
import com.nitrovoid.system.DifficultyScaler;
import com.nitrovoid.system.EnemySpawner;
import com.nitrovoid.system.ItemSpawner;
import com.nitrovoid.system.NitroSystem;
import com.nitrovoid.system.ScoreManager;
import com.nitrovoid.system.SlowMotionSystem;
import com.nitrovoid.system.TimerSystem;
import com.nitrovoid.util.CollisionDetector;

public class GameController {
    private GameState currentState = GameState.MENU;
    private Player player;
    private InputHandler input;
    private TimerSystem timer = new TimerSystem();
    private ScoreManager scoreManager = new ScoreManager();
    private EnemySpawner enemySpawner = new EnemySpawner();
    private ItemSpawner itemSpawner = new ItemSpawner();
    private NitroSystem nitroSystem = new NitroSystem();
    private SlowMotionSystem slowMotionSystem = new SlowMotionSystem();
    private final int screenWidth = 800;
    private final int screenHeight = 600;
    private boolean nitroPressed = false; // biar ga spam
    private boolean slowPressed = false; // biar ga spam
    private boolean pausePressed = false; // untuk pause
    private final int HITBOX_TOLERANCE = 5; //hitbox tolerance -5px semua sisi
    private double worldSpeed        = 3.0;   // px/frame awal = 60 KM/h
    private static final double WS_MIN  = 3.0;   // minimum (tidak bisa lebih lambat)
    private static final double WS_NORM = 6.0;   // target normal tanpa gas
    private static final double WS_MAX  = 9.0;   // maksimal dengan nitro/boost
    private static final double WS_BOOST_BONUS = 2.0; // tambahan saat boost item aktif
    private static final double WS_ACCEL = 0.5; // px/frame² — naik saat gas
    private static final double WS_PASSIVE = 0.02; // naik otomatis tiap frame (pasif)
    private double gameOverTimer = 0;
    private final double GAMEOVER_DELAY = 3.0;
    private double countdownTimer = 3.0; // 3-2-1
    private int countdownValue = 3;      // angka yang ditampilkan
    private double storyTimer = 0;       // untuk track durasi story 
    private boolean enterPressed = false;
    private boolean restartPressed = false;
    private boolean backToMenuPressed = false;
    
    public GameController(Player player, InputHandler input) {
        this.player = player;
        this.input = input;
    }
    public void update(double deltaTime) {
        switch (currentState) {
            case MENU:
                if (input.enter && !enterPressed) {
                    enterPressed = true;
                    startStory();
                }
                if (!input.enter) {enterPressed = false;}
                if (input.exitGame){ 
                    exitGame();
                }
                break;
            case STORY:
                if (input.space) startCountdown(); // skip
                    break;    
            case COUNTDOWN:  updateCountdown(deltaTime);
                break;
            case PLAYING:    updatePlaying(deltaTime);    
                break;
            case PAUSE:      updatePause();               
                break;
            case GAMEOVER:   updateGameOver(deltaTime);   
                break;
            case SCORE:
                if (input.restart && !restartPressed) {
                    restartPressed = true;
                    restartGame();
                }
                if (!input.restart) restartPressed = false;
                if (input.backToMenu && !backToMenuPressed) {
                    backToMenuPressed = true;
                    goToMenu();
                }
                if (!input.backToMenu) backToMenuPressed = false;
                break;  
            default: 
                break; // MENU, STORY, SCORE ditangani UI nanti
        }
    }
    private void updatePlaying(double deltaTime ) {
        timer.update(deltaTime);
        scoreManager.update(deltaTime, player.getCurrentSpeed());
        nitroSystem.update(deltaTime);
        slowMotionSystem.update(deltaTime);
        player.update(input, deltaTime);
//      update world speed
        double targetSpeed = WS_NORM;
        if (input.up)                   targetSpeed = WS_MAX;           // gas → dorong ke max
        if (player.isBoostItemActive()) targetSpeed += WS_BOOST_BONUS; // boost item
        if (player.isNitroActive())     targetSpeed += player.getNitroBoost(); // nitro

        // Clamp target
        if (targetSpeed > WS_MAX + player.getNitroBoost()) 
            targetSpeed = WS_MAX + player.getNitroBoost();

        // Smooth worldSpeed menuju target
        if (worldSpeed < targetSpeed) {
            worldSpeed += WS_ACCEL * deltaTime;
            if (worldSpeed > targetSpeed) worldSpeed = targetSpeed;
        } else {
            worldSpeed -= WS_ACCEL * deltaTime;
            if (worldSpeed < WS_MIN) worldSpeed = WS_MIN; // tidak pernah berhenti total
        }

        // Passive acceleration — makin lama main makin cepat
        worldSpeed += WS_PASSIVE * deltaTime;
        if (worldSpeed > WS_MAX) worldSpeed = WS_MAX; // passive tidak melewati WS_MAX

    
        // Input Pause
        if (input.pause && !pausePressed) {
            pausePressed = true;
            currentState = GameState.PAUSE;
        }
        if (!input.pause) pausePressed = false;

        // input nitro & cek supaya ga spam
        if (input.nitro && !nitroPressed){
            nitroPressed = true;
            NitroSystem.NitroTiming timing = nitroSystem.activate();
            player.applyNitro(timing);
        }
        if (!input.nitro)nitroPressed = false;
        // cek input slow motion
        if (input.slowMotion && !slowPressed){
            slowPressed = true;
            slowMotionSystem.activate();
        }
        if (!input.slowMotion)slowPressed = false;
                
        //Update Spawner
        double difficultySpeed = DifficultyScaler.getSpeedMultiplier(scoreManager.getScore());
        double speedMultiplier = slowMotionSystem.getSpeedMultiplier() * difficultySpeed;
        DifficultyScaler.apply(scoreManager.getScore(), enemySpawner);

        // GANTI keduanya jadi pakai speedMultiplier:
        enemySpawner.update(worldSpeed, screenHeight, speedMultiplier, itemSpawner.getItems());
        itemSpawner.update(worldSpeed, screenHeight, speedMultiplier, enemySpawner.getEnemies());
        
        // Cek collision player vs enemy
        for (Enemy enemy : enemySpawner.getEnemies()) {
            if (CollisionDetector.isColliding(player, enemy, HITBOX_TOLERANCE)) {
                scoreManager.addTimeBonus(timer.getTimeLeft());
                currentState = GameState.GAMEOVER;
                gameOverTimer = 0;
            }
        }

        // Cek waktu habis
        if (timer.isTimeUp()) {
            scoreManager.finalizeScore();
            currentState = GameState.GAMEOVER;
            gameOverTimer = 0;
        }

        // Cek collision player vs item 
        Item toRemove = null;
        for (Item item : itemSpawner.getItems()) {
            if(CollisionDetector.isColliding(player, item, HITBOX_TOLERANCE)){
                toRemove = item;
                break;
            }
        }
        if (toRemove != null){
            handleItemPickup(toRemove);
            itemSpawner.removeItem(toRemove);
            }
        }
    
    private void handleItemPickup(Item item){
        switch (item.getTipe()) {
            case BOOST:
                player.applyBoost();
                timer.addTime(5.0); 
                System.out.println("Item BOOST diambil! +5 detik");
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
    // Dipanggil dari MENU (tombol start)
    public void startStory() {
        currentState = GameState.STORY;
    }

    // Dipanggil dari STORY (selesai atau skip)
    public void startCountdown() {
        currentState = GameState.COUNTDOWN;
        countdownTimer = 3.0;
        countdownValue = 3;
    }
    // Dipanggil otomatis saat countdown selesai
    public void startGame() {
        currentState = GameState.PLAYING;
        worldSpeed = WS_MIN;
        timer.start();
        scoreManager.reset();
        nitroSystem.reset();
        slowMotionSystem.reset();
        enemySpawner = new EnemySpawner();
        itemSpawner  = new ItemSpawner();
    }
//  restart
    public void restartGame() {
        timer.reset();
        gameOverTimer = 0;
        scoreManager.reset();
        nitroSystem.reset();
        slowMotionSystem.reset();
        restartPressed = false;
        enemySpawner = new EnemySpawner();
        itemSpawner = new ItemSpawner();
        startCountdown();
    }
    // tombol menu
    public void goToMenu() {
        currentState = GameState.MENU;
        timer.reset();
        gameOverTimer = 0;
        enemySpawner = new EnemySpawner();
        itemSpawner  = new ItemSpawner();
    }
    public void exitGame() {
    System.exit(0);
    }
    private void updateCountdown(double deltaTime) {
        countdownTimer -= deltaTime;
        countdownValue = (int) Math.ceil(countdownTimer); // 3, 2, 1
        if (countdownTimer <= 0) {
            startGame();
        }
    }
    private void updatePause() {
        if (input.pause && !pausePressed) {
            pausePressed = true;
            currentState = GameState.PLAYING;
        }
        if (!input.pause) pausePressed = false;
    }

    private void updateGameOver(double deltaTime) {
        gameOverTimer += deltaTime;
        if (gameOverTimer >= GAMEOVER_DELAY) {
            currentState = GameState.SCORE;
        }
    }
    
    public void setCurrentState(GameState state) {
        this.currentState = state;
    }

//  Getters  
    public double getTimeLeft(){ return timer.getTimeLeft();    }
    public GameState getCurrentState() { return currentState;}
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
    public boolean isBoostActive()   { return player.isBoostActive(); }
    public int getSpeedKmh() { return player.getSpeedKmh(); }
    public int getCountdownValue()  { return countdownValue; }
    public double getGameOverTimer() { return gameOverTimer; }
    public int getBestScore() { return scoreManager.getBestScore(); }

}