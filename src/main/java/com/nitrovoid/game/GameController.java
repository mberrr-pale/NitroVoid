package com.nitrovoid.game;

import java.util.ArrayList;

import com.nitrovoid.entity.Enemy;
import com.nitrovoid.entity.Item;
import com.nitrovoid.entity.Player;
import com.nitrovoid.input.InputHandler;
import com.nitrovoid.system.*;
import com.nitrovoid.util.CollisionDetector;
import com.nitrovoid.game.screen.HomeScreen;
import com.nitrovoid.ui.GameplayScreen;
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
//  Select map
    public enum MapType {KTT, LIWET, MGT};
    private MapType selectedMap = MapType.KTT;
    private int selectedMapIndex;
    private String currentMapName = "ketintang"; // default
// Save Manager
    private SaveManager saveManager = SaveManager.getInstance();
    private com.nitrovoid.ui.GameplayScreen gameplayScreen;
    
    public void initSave() {
    saveManager.init(); }
    
    public GameController(Player player, InputHandler input, GameplayScreen gameplayScreen) {
        this.player = player;
        this.input = input;
        homeScreen = new HomeScreen();
        this.gameplayScreen = gameplayScreen;
        this.gameplayScreen.setMap(selectedMap);
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

    // MOVE UP
    if (input.up) {

        selectedMapIndex--;

        if (selectedMapIndex < 0) {
            selectedMapIndex = 2;
        }

        input.up = false;
    }

    // MOVE DOWN
    if (input.down) {

        selectedMapIndex++;

        if (selectedMapIndex > 2) {
            selectedMapIndex = 0;
        }

        input.down = false;
    }

    // APPLY SELECTED MAP
    switch (selectedMapIndex) {

        case 0:
            selectedMap = MapType.KTT;
            break;

        case 1:
            selectedMap = MapType.LIWET;
            break;

        case 2:
            selectedMap = MapType.MGT;
            break;
    }

    // ENTER
    if (input.enter && !enterPressed) {
        enterPressed = true;
        startStory();
    }

    if (!input.enter) {
        enterPressed = false;
    }

    // BACK
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
        scoreManager.update(deltaTime,worldSpeed);
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

    // INPUT PAUSE
    if (input.pause && !pausePressed) {

        pausePressed = true;
        currentState = GameState.PAUSE;
    }

    if (!input.pause) {
        pausePressed = false;
    }

    // INPUT NITRO
    if (input.nitro && !nitroPressed) {

        nitroPressed = true;

        if (!nitroSystem.isOnCooldown()
                && nitroSystem.getNitroCount() > 0) {

            NitroSystem.NitroTiming timing =
                    nitroSystem.activate();

            player.applyNitro(timing);

            switch (timing) {

                case PERFECT:

                    gameplayScreen.addFeedback(
                        "PERFECT!",
                        GameConfig.SCREEN_WIDTH / 2 - 70,
                        GameConfig.SCREEN_HEIGHT - 120,
                        Color.GREEN
                    );
                    break;

                case GOOD:

                    gameplayScreen.addFeedback(
                        "GOOD!",
                        GameConfig.SCREEN_WIDTH / 2 - 50,
                        GameConfig.SCREEN_HEIGHT - 120,
                        Color.YELLOW
                    );
                    break;

                case MISS:

                    gameplayScreen.addFeedback(
                        "MISS!",
                        GameConfig.SCREEN_WIDTH / 2 - 40,
                        GameConfig.SCREEN_HEIGHT - 120,
                        Color.RED
                    );
                    break;
            }
        }
    }

    // RESET INPUT NITRO
    if (!input.nitro) {
        nitroPressed = false;
    }

    // INPUT SLOW MOTION
    if (input.slowMotion && !slowPressed) {

        slowPressed = true;

        slowMotionSystem.activate();
    }

    // RESET INPUT SLOW
    if (!input.slowMotion) {
        slowPressed = false;
    }
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
                saveManager.updateBestScore(scoreManager.getScore());
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
            saveManager.updateBestScore(scoreManager.getScore());
            currentState = GameState.SCORE;
        }
    }
private void handleItemPickup(Item item){

    switch (item.getTipe()) {

        case BOOST:

            player.applyBoost();

            gameplayScreen.addFeedback(
                "BOOST!",
                item.getX(),
                item.getY(),
                Color.ORANGE
            );

            gameplayScreen.addFeedback(
                "+10 SEC",
                item.getX(),
                item.getY() - 20,
                Color.YELLOW
            );

            break;

        case TIME:

            timer.addTime(10);

            gameplayScreen.addFeedback(
                "+10 SEC",
                item.getX(),
                item.getY(),
                Color.GREEN
            );

            break;

        case NITRO:

            nitroSystem.addNitro();

            gameplayScreen.addFeedback(
                "+1 NITRO",
                item.getX(),
                item.getY(),
                Color.CYAN
            );

            break;

        case SLOWMOTION:

            slowMotionSystem.addCharge();

            gameplayScreen.addFeedback(
                "SLOW READY",
                item.getX(),
                item.getY(),
                Color.CYAN
            );

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
        timer.reset();
        timer.start();
        scoreManager.reset();
        scoreManager.setMap(selectedMap);
        player.reset();
        currentMapName = selectedMap.name().toLowerCase(); 
        nitroSystem.reset();
        slowMotionSystem.reset();
        enemySpawner = new EnemySpawner();
        itemSpawner  = new ItemSpawner();
    }
    public void restartGame() {
        timer.reset();
        player.reset();
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
        player.reset();
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
    public int getBestScore() { return saveManager.getBestScore(); }
    public double getBarPosition()     { return nitroSystem.getBarPosition(); }
    public double getWorldSpeed() { return worldSpeed; }
    public MapType getSelectedMap() {return selectedMap;}
    public int getSelectedMapIndex() {return selectedMapIndex;}    
}