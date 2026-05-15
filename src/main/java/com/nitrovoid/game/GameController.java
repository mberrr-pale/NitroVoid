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
import com.nitrovoid.game.screen.HomeScreen;
import com.nitrovoid.game.GameConfig;
import java.awt.Color;

public class GameController {
    private GameState currentState = GameState.MENU;
    private Player player;
    private InputHandler input;
    private HomeScreen homeScreen;
    private TimerSystem timer = new TimerSystem();
    private ScoreManager scoreManager = new ScoreManager();
    private NitroSystem nitroSystem = new NitroSystem();
    private SlowMotionSystem slowMotionSystem = new SlowMotionSystem();
    private EnemySpawner enemySpawner = new EnemySpawner();
    private ItemSpawner itemSpawner = new ItemSpawner();
//  Config
    private static final int HITBOX_TOLERANCE = 5; 
    private static final double WS_MIN  = 0.5;   
    private static final double WS_NORM = 2.0;   
    private static final double WS_MAX  = 3.0;
    private static final double WS_BOOST_BONUS = 1.5; 
    private static final double WS_ACCEL = 1.0; 
    private static final double WS_PASSIVE = 0.03;
    private final double FEEDBACK_DURATION = 1.5;
//  Runtime Variable
    private double worldSpeed = WS_MIN;   // px/frame awal = 60 KM/h
    private double countdownTimer = 3.0; // 3-2-1
    private int countdownValue = 3;  
    private double nitroFeedbackTimer = 0;
//  Input Flag    
    private boolean nitroPressed = false; // biar ga spam
    private boolean slowPressed = false; // biar ga spam
    private boolean pausePressed = false; // untuk pause
    private boolean enterPressed = false;
    private boolean restartPressed = false;
    private boolean backToMenuPressed = false;
//  UI Feedback
    private String nitroFeedback = "";
    private String itemFeedback = "";
    private Color itemFeedbackColor = Color.WHITE;
    private double itemFeedbackTimer = 0;

    
    public GameController(Player player, InputHandler input) {
        this.player = player;
        this.input = input;
        homeScreen = new HomeScreen();
    }
//  Public Entry
    public void update(double deltaTime) {
        switch (currentState) {
            case MENU:
                updateMenu();
                break;
            case CHOOSE_MAP:
                updateChooseMap();
                break;
            case STORY:
                updateStory();
                break;
            case COUNTDOWN:
                updateCountdown(deltaTime);
                break;
            case PLAYING:
                updatePlaying(deltaTime);
                break;
            case PAUSE:
                updatePause();
                break;
            case SCORE:
                updateScore();
                break;
            default:
                break;
        }
    }
//  STATE UPDATE  
    private void updateMenu() {
        if (input.up) {
            homeScreen.moveUp();
            input.up = false;
        }
        if (input.down) {
            homeScreen.moveDown();
            input.down = false;
        }
        if (input.enter && !enterPressed) {
            enterPressed = true;
            switch (homeScreen.getSelectedIndex()) {
                case 0:
                    currentState = GameState.CHOOSE_MAP;
                    break;
                case 1:
                    System.out.println("SETTINGS");
                    break;
                case 2:
                    exitGame();
                    break;
            }
        }
        if (!input.enter) {
            enterPressed = false;
        }
    }
    private void updateChooseMap() {
        if (input.enter && !enterPressed) {
            enterPressed = true;
            startStory();
        }
        if (!input.enter) {
            enterPressed = false;
        }
        if (input.backToMenu && !backToMenuPressed) {
            backToMenuPressed = true;
            goToMenu();
        }
        if (!input.backToMenu) {
            backToMenuPressed = false;
        }
    }
    private void updateStory() {
        if (input.space) {
            startCountdown();
        }
    }
    private void updateCountdown(double deltaTime) {
        countdownTimer -= deltaTime;
        countdownValue = (int) Math.ceil(countdownTimer); // 3, 2, 1
        if (countdownTimer <= -1) {
            startGame();
        }
    }
    private void updatePlaying(double deltaTime) {
        updateSystems(deltaTime);
        updateWorldSpeed(deltaTime);
        handleGameplayInput(deltaTime);
        updateSpawner();
        checkEnemyCollision();
        checkItemCollision();
        checkGameOver();
    }
    private void updatePause() {
        if (input.pause && !pausePressed) {
            pausePressed = true;
            currentState = GameState.PLAYING;
        }
        if (!input.pause) pausePressed = false;
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
    }
    private void updateScore() {
        if (input.restart && !restartPressed) {
            restartPressed = true;
            restartGame();
        }
        if (!input.restart) {
            restartPressed = false;
        }
        if (input.backToMenu && !backToMenuPressed) {
            backToMenuPressed = true;
            goToMenu();
        }
        if (!input.backToMenu) {
            backToMenuPressed = false;
        }
    }
//  GAMEPLAY SUBSYSTEM
    private void updateSystems(double deltaTime ) {        
        timer.update(deltaTime);
        scoreManager.update(
                deltaTime, 
      worldSpeed);
        nitroSystem.update(deltaTime);
        slowMotionSystem.update(deltaTime);
        player.update(input, deltaTime);
    }
    private void updateWorldSpeed(double deltaTime) {
        double targetSpeed = WS_NORM;
        // GAS
        if (input.up) {
            targetSpeed = WS_MAX;
        }
        // BOOST ITEM
        if (player.isBoostItemActive()) {
            targetSpeed += WS_BOOST_BONUS;
        }
        // NITRO
        if (player.isNitroActive()) {
            double nitroBoost = player.getNitroBoost();
            worldSpeed += nitroBoost * deltaTime * 20;
            worldSpeed += 0.08;
            targetSpeed += nitroBoost;
        }
        // MAX ALLOWED SPEED
        double maxAllowedSpeed = WS_MAX;
        if (player.isBoostItemActive()) {
            maxAllowedSpeed += WS_BOOST_BONUS;
        }
        if (player.isNitroActive()) {
            maxAllowedSpeed += player.getNitroBoost();
        }
        // SMOOTH ACCELERATION
        if (worldSpeed < targetSpeed) {
            worldSpeed += WS_ACCEL * deltaTime;
            if (worldSpeed > targetSpeed) {
                worldSpeed = targetSpeed;
            }
        } else {
            worldSpeed -= WS_ACCEL * deltaTime;
            if (worldSpeed < WS_MIN) {
                worldSpeed = WS_MIN;
            }
        }
        // PASSIVE SPEED
        worldSpeed += WS_PASSIVE * deltaTime;
        // FINAL CLAMP
        if (worldSpeed > maxAllowedSpeed) {
            worldSpeed = maxAllowedSpeed;
        }
    }
    private void handleGameplayInput(double deltaTime) {   
        // Input Pause
        if (input.pause && !pausePressed) {
            pausePressed = true;
            currentState = GameState.PAUSE;
        }
        if (!input.pause) pausePressed = false;
        // input nitro & cek supaya ga spam
        if (nitroFeedbackTimer > 0) {
            nitroFeedbackTimer -= deltaTime;
            if (nitroFeedbackTimer <= 0) nitroFeedback = "";
        }
        if (input.nitro && !nitroPressed){
            nitroPressed = true;
            NitroSystem.NitroTiming timing = nitroSystem.activate();
            player.applyNitro(timing);
            switch (timing) {
            case PERFECT:
                nitroFeedback      = "PERFECT!";
                nitroFeedbackTimer = FEEDBACK_DURATION;
                break;
            case GOOD:
                nitroFeedback      = "GOOD!";
                nitroFeedbackTimer = FEEDBACK_DURATION;
                break;
            case MISS:
                nitroFeedback      = "MISS!";
                nitroFeedbackTimer = FEEDBACK_DURATION;
                break;
            }
        }
        if (itemFeedbackTimer > 0) {
            itemFeedbackTimer -= deltaTime;
            if (itemFeedbackTimer <= 0){
                itemFeedback = "";
            }
        }
        if (!input.nitro)nitroPressed = false;
        // cek input slow motion
        if (input.slowMotion && !slowPressed){
            slowPressed = true;
            slowMotionSystem.activate();
        }
        if (!input.slowMotion)slowPressed = false;
    }         
    private void updateSpawner() {
        double difficultySpeed = DifficultyScaler.getSpeedMultiplier(scoreManager.getScore());
        double speedMultiplier = slowMotionSystem.getSpeedMultiplier() * difficultySpeed;
        DifficultyScaler.apply(scoreManager.getScore(), enemySpawner);
        enemySpawner.update(worldSpeed, GameConfig.SCREEN_HEIGHT, speedMultiplier, itemSpawner.getItems());
        itemSpawner.update(worldSpeed, GameConfig.SCREEN_HEIGHT, speedMultiplier, enemySpawner.getEnemies());   
    }
    private void checkEnemyCollision() {
        for (Enemy enemy : enemySpawner.getEnemies()) {
            if (CollisionDetector.isColliding(player, enemy, HITBOX_TOLERANCE)) {
                scoreManager.addTimeBonus(timer.getTimeLeft());
                currentState = GameState.SCORE;
            }
        }    
    }
    private void checkItemCollision(){
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
    private void checkGameOver(){
        if (timer.isTimeUp()) {
            scoreManager.finalizeScore();
            currentState = GameState.SCORE;
        }
    }
    private void handleItemPickup(Item item){
       switch (item.getTipe()) {
           case BOOST:
               player.applyBoost();
               itemFeedback = "BOOST!";
               itemFeedbackColor = Color.ORANGE;
               itemFeedbackTimer = 1.2;
               break;
           case TIME:
               timer.addTime(5.0);
               itemFeedback = "+5 SEC";
               itemFeedbackColor = Color.GREEN;
               itemFeedbackTimer = 1.2;
               break;
           case NITRO:
               nitroSystem.addNitro();
               itemFeedback = "+1 NITRO";
               itemFeedbackColor = Color.CYAN;
               itemFeedbackTimer = 1.2;
               break;
           case SLOWMOTION:
               slowMotionSystem.addCharge();
               itemFeedback = "SLOW READY";
               itemFeedbackColor = Color.CYAN;
               itemFeedbackTimer = 1.2;
               break;
       }
   }
//  GAME FLOW
    public void startStory() {
        currentState = GameState.STORY;
    }
    public void startCountdown() {
        currentState = GameState.COUNTDOWN;
        countdownTimer = 3.0;
        countdownValue = 3;
    }
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
    public void restartGame() {
        timer.reset();
        scoreManager.reset();
        nitroSystem.reset();
        slowMotionSystem.reset();
        restartPressed = false;
        enemySpawner = new EnemySpawner();
        itemSpawner = new ItemSpawner();
        startCountdown();
    }
    public void goToMenu() {
        currentState = GameState.MENU;
        timer.reset();
        scoreManager.reset();
        enemySpawner = new EnemySpawner();
        itemSpawner  = new ItemSpawner();
    }
    public void exitGame() {
    System.exit(0);
    }
    
//  SETTTERS
    public void setCurrentState(GameState state) {
        this.currentState = state;
    }

//  GETTERS  
    public HomeScreen getHomeScreen() {
        return homeScreen;
    }
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
    public int getSpeedKmh() {
        double maxPossibleSpeed = WS_MAX + WS_BOOST_BONUS + 3.0;
        double ratio = (worldSpeed - WS_MIN) / (maxPossibleSpeed - WS_MIN);
                        ratio = Math.max(0, Math.min(1, ratio));
        return (int)(60 + ratio * 180);
    }
    public int getCountdownValue()  { return countdownValue; }
    public int getBestScore() { return scoreManager.getBestScore(); }
    public String getNitroFeedback()   { return nitroFeedback; }
    public double getBarPosition()     { return nitroSystem.getBarPosition(); }
    public double getWorldSpeed() { return worldSpeed; }
    public String getItemFeedback() { return itemFeedback; }
    public Color getItemFeedbackColor() { return itemFeedbackColor; }
}