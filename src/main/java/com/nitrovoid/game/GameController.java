package com.nitrovoid.game;

import java.util.*;
import javax.swing.*;
import java.awt.*;

import com.nitrovoid.entity.*;
import com.nitrovoid.input.InputHandler;
import com.nitrovoid.system.*;
import com.nitrovoid.ui.screen.*;
import com.nitrovoid.util.CollisionDetector;
import com.nitrovoid.ui.GameplayScreen;
import com.nitrovoid.game.GameConfig;

public class GameController {
    // CORE
    private GameState currentState = GameState.MENU;
    private final JFrame frame;
    private Player player;
    private final InputHandler input;
    // UI
    private final HomeScreen homeScreen;
    private final SelectMapScreen selectMapScreen;
    private final StoryScreen storyScreen;
    private final PauseScreen pauseScreen;
    private final GameOverScreen gameOverScreen;
    // SYSTEM
    private final MusicSystem music;
    private TimerSystem timer = new TimerSystem();
    private ScoreManager scoreManager = ScoreManager.getInstance();
    private EnemySpawner enemySpawner = new EnemySpawner();
    private ItemSpawner itemSpawner = new ItemSpawner();
    private NitroSystem nitroSystem = new NitroSystem();
    private SlowMotionSystem slowMotionSystem = new SlowMotionSystem();
    private static final int HITBOX_TOLERANCE = 5;
    private static final double WS_MIN = 0.5;
    private static final double WS_NORM = 2.0;
    private static final double WS_MAX = 3.0;
    private static final double WS_BOOST_BONUS = 1.5;
    private static final double WS_ACCEL = 1.0;
    private static final double WS_PASSIVE = 0.03;
    private final double FEEDBACK_DURATION = 1.5;
//  Runtime Variable
    private double worldSpeed = WS_MIN;   // px/frame awal = 60 KM/h
    private double countdownTimer = 3.0; // 3-2-1
    private int countdownValue = 3;
    private double nitroFeedbackTimer = 0;
    private String itemFeedback = "";
    private double itemFeedbackTimer = 0;

    
    //  Select map
    public enum MapType {
        KTT, LIWET, MGT
    };
    private MapType selectedMap = MapType.KTT;
    private int selectedMapIndex;
    private com.nitrovoid.ui.GameplayScreen gameplayScreen;

// Save Manager
    private SaveManager saveManager = SaveManager.getInstance();
    public void initSave() {
        saveManager.init(); 
    }
    private boolean nitroPressed = false; // biar ga spam
    private boolean slowPressed = false; // biar ga spam
    private boolean pausePressed = false; // untuk pause
    private boolean enterPressed = false;
    private boolean mouseClicked = false;
    private boolean restartPressed = false;
    private boolean backToMenuPressed = false;

    // CONSTRUCTOR
    public GameController(JFrame frame, Player player, InputHandler input, GameplayScreen gameplayScreen) {
        this.frame = frame;
        this.player = player;
        this.input = input;

        music = new MusicSystem();
        homeScreen = new HomeScreen(music);
        selectMapScreen = new SelectMapScreen();
        storyScreen = new StoryScreen(() -> SwingUtilities.invokeLater(this::startCountdown));
        pauseScreen = new PauseScreen(music);
        gameOverScreen = new GameOverScreen(this, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        this.gameplayScreen = gameplayScreen;
        this.gameplayScreen.setMap(selectedMap);
        
        initializeGame();

    }

    // INITIALIZE
    private void initializeGame() {

        music.playLoop("/musics/bgm.wav");

        frame.add(homeScreen, BorderLayout.CENTER);

        frame.revalidate();
        frame.repaint();
    }

    // UPDATE
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
                gameOverScreen.update();
                break;
            default:
                break;
        }
    }

    // MENU
    private void updateMenu() {

        homeScreen.updateHover(input.mouseX, input.mouseY);

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

            handleMenuSelect(homeScreen.getSelectedIndex());
        }

        if (!input.enter) {
            enterPressed = false;
        }

        if (input.mouseLeftPressed && !mouseClicked) {

            mouseClicked = true;

            int clicked = homeScreen.checkMouseClick(
                    input.mouseX,
                    input.mouseY
            );

            handleMenuSelect(clicked);
        }

        if (!input.mouseLeftPressed) {
            mouseClicked = false;
        }
        if (music.isVolumeOn()) {
            music.resumeAfterPause();
        }
    }

    private void handleMenuSelect(int selected) {

        switch (selected) {

            case 0:
                openMapSelection();
                break;

            case 1:
                exitGame();
                break;
        }
    }

    // MAP SELECT
    private void openMapSelection() {

        currentState = GameState.CHOOSE_MAP;

        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            frame.add(selectMapScreen, BorderLayout.CENTER);

            frame.revalidate();
            frame.repaint();
        });
    }

    private void updateChooseMap() {

        // --- update hover based on mouse ---
        selectMapScreen.updateHover(input.mouseX, input.mouseY);

        // --- arrow keys to navigate maps ---
        if (input.left) {
            selectMapScreen.moveUp();
            input.left = false;
        }
        if (input.right) {
            selectMapScreen.moveDown();
            input.right = false;
        }

        // --- ENTER key to select/start ---
// contoh untuk Enter
        if (input.enter && !enterPressed) {
            enterPressed = true;
            int idx = selectMapScreen.getSelectedIndex();

            // cek apakah map unlocked
            if (idx >= 0 && !selectMapScreen.isMapLocked(idx)) {
                triggerStartGame(idx);
            } else {
                System.out.println("[Debug] Map locked! Tidak bisa dimainkan.");
            }
        }

        if (!input.enter) {
            enterPressed = false;
        }

        // --- Mouse left click handling ---
        if (input.mouseLeftPressed && !mouseClicked) {
            mouseClicked = true;

            int clicked = selectMapScreen.checkMouseClick(input.mouseX, input.mouseY);

            if (clicked == -3) { // start button
                int idx = selectMapScreen.getSelectedIndex();
                if (!selectMapScreen.isMapLocked(idx)) {
                    triggerStartGame(idx);
                } else {
                    System.out.println("[Debug] Map locked! Tidak bisa dimainkan.");
                }
            } else if (clicked == -4) {
                // BACK button clicked → return to main menu
                System.out.println("[Debug] BACK clicked. Returning to main menu.");
                goToMenu();
            } else if (clicked >= 0) {
                // Map clicked → select that map
                selectMapScreen.setSelectedIndex(clicked);
                System.out.println("[Debug] Map clicked. New selected index: " + clicked);
            }
        }

        if (!input.mouseLeftPressed) {
            mouseClicked = false;
        }
    }

    private void triggerStartGame(int selected) {
        selectedMapIndex = selected;
        // classic switch
        switch (selected) {
            case 0:
                selectedMap = MapType.KTT;
                break;
            case 1:
                selectedMap = MapType.LIWET;
                break;
            case 2:
                selectedMap = MapType.MGT;
                break;
            default:
                return; // do not start story if index invalid
            }
        startStory();
    }

    // STORY
    public void startStory() {

        currentState = GameState.STORY;
        // music.stopBGM();

        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();

            frame.add(storyScreen, BorderLayout.CENTER);

            frame.revalidate();
            frame.repaint();

            SwingUtilities.invokeLater(() -> {
                storyScreen.playVideo(getStoryVideo(selectedMap));
            });
        });
    }

    private void updateStory() {
    }

    private String getStoryVideo(MapType map) {
        switch (map) {
            case KTT:
                return "/videos/story-game.mp4";
            case LIWET:
                return "/videos/story-game.mp4";
            case MGT:
                return "/videos/story-game.mp4";
            default:
                return "/videos/storyline.mp4";
        }
    }

    public MapType getSelectedMap() {
        return selectedMap;
    }

    public int getSelectedMapIndex() {
        return selectedMapIndex;
    }

    // COUNTDOWN
    public void startCountdown() {
        currentState = GameState.COUNTDOWN;
        countdownTimer = 3.0;
        countdownValue = 3;
        music.stopBGM();
        music.playSFX("/musics/sfx-countdown.wav");
        SwingUtilities.invokeLater(() -> {
            // Hapus StoryScreen dulu
            frame.getContentPane().removeAll();

            // Tambahkan gameplay panel atau biarkan GamePanel repaint
            frame.revalidate();
            frame.repaint();
        });
    }

    private void updateCountdown(double deltaTime) {
        countdownTimer -= deltaTime;
        countdownValue = (int) Math.ceil(countdownTimer); // 3, 2, 1
        if (countdownTimer <= -1) {
            startGame();
        }
    }

    // START GAME
    public void startGame() {
        currentState = GameState.PLAYING;
        music.playLoop("/musics/bgm.wav");

        worldSpeed = WS_MIN;

        timer.start();
        timer.reset();

        scoreManager.reset();
        scoreManager.setMap(selectedMap);

        player.reset();

        nitroSystem.reset();
        slowMotionSystem.reset();

        enemySpawner = new EnemySpawner();
        itemSpawner = new ItemSpawner();
    }

    // PLAYING
    private void updatePlaying(double deltaTime) {
        updateSystems(deltaTime);
        updateWorldSpeed(deltaTime);
        handleGameplayInput(deltaTime);
        updateSpawner();
        checkEnemyCollision();
        checkItemCollision();
        checkGameOver();        
    }

    // SCORE
    private void updateScore() {
        // hover update
        gameOverScreen.updateHover(input.mouseX, input.mouseY);
        // animasi zoom
        gameOverScreen.update();
        // keyboard restart
        if (input.restart && !restartPressed) {
            restartPressed = true;
            restartGame();
        }
        if (!input.restart) {
            restartPressed = false;
        }
        // keyboard back
        if (input.backToMenu && !backToMenuPressed) {
            backToMenuPressed = true;
            goToMenu();
        }
        if (!input.backToMenu) {
            backToMenuPressed = false;
        }
        // mouse click
        if (input.mouseLeftPressed && !mouseClicked) {
            mouseClicked = true;
            int clicked = gameOverScreen.checkMouseClick(
                    input.mouseX,
                    input.mouseY
            );
            switch (clicked) {
                case 0:
                    restartGame();
                    break;
                case 1:
                    goToMenu();
                    break;
            }
        }
        if (!input.mouseLeftPressed) {
            mouseClicked = false;
        }
    }

    // DRAW GameOverScreen
    public void drawGameOver(Graphics g) {
        if (gameOverScreen != null) {
            gameOverScreen.draw(g);
        }
    }

    // Restart
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

    // Pause
    private void updatePause() {
        // Update hover tombol volume dan tombol pauseScreen
        pauseScreen.updateHover(input.mouseX, input.mouseY);

        // Cek klik mouse
        if (input.mouseLeftPressed && !mouseClicked) {
            mouseClicked = true;

            int clickedButton = pauseScreen.checkMouseClick(input.mouseX, input.mouseY);

            switch (clickedButton) {
                case 0: // Resume
                    currentState = GameState.PLAYING;
                    music.resumeAfterPause();
                    break;

                case 1: // Restart
                    restartGame();
                    break;

                case 2: // Back to Menu
                    goToMenu();
                    break;
            }
        }

        if (!input.mouseLeftPressed) {
            mouseClicked = false;
        }

        // Tombol keyboard tetap jalan
        if (input.pause && !pausePressed) {
            pausePressed = true;
            music.resumeAfterPause();
            currentState = GameState.PLAYING;
        }
        if (!input.pause) {
            pausePressed = false;
        }

        if (input.restart && !restartPressed) {
            restartGame();
            restartPressed = true;
        }
        if (!input.restart) {
            restartPressed = false;
        }

        if (input.backToMenu && !backToMenuPressed) {
            goToMenu();
            backToMenuPressed = true;
        }
        if (!input.backToMenu) {
            backToMenuPressed = false;
        }
    }

    // MENU
    public void goToMenu() {
        currentState = GameState.MENU;

        timer.reset();

        player.reset();

        enemySpawner = new EnemySpawner();
        itemSpawner = new ItemSpawner();

        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            frame.add(homeScreen, BorderLayout.CENTER);
            frame.revalidate();
            frame.repaint();
        });
        if (music.isVolumeOn()) {
            music.resumeAfterPause();
        }
    }

    // EXIT
    public void exitGame() {
        System.exit(0);
    }

//  GAMEPLAY SUBSYSTEM
    private void updateSystems(double deltaTime) {
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
            music.pauseBGM();
            currentState = GameState.PAUSE;
        }
        if (!input.pause) {
            pausePressed = false;
        }    
        if (input.nitro && !nitroPressed) {
            nitroPressed = true;
        if (!nitroSystem.isOnCooldown()
            && nitroSystem.getNitroCount() > 0) {            
                NitroSystem.NitroTiming timing = nitroSystem.activate();
    
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
        
            if (itemFeedbackTimer > 0) {
                itemFeedbackTimer -= deltaTime;
                if (itemFeedbackTimer <= 0) {
                    itemFeedback = "";
                }
            }
            // RESET INPUT NITRO
            if (!input.nitro) {
                nitroPressed = false;
            }
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

    private void checkItemCollision() {
        Item toRemove = null;
        for (Item item : itemSpawner.getItems()) {
            if (CollisionDetector.isColliding(player, item, HITBOX_TOLERANCE)) {
                toRemove = item;
                break;
            }
        }
        if (toRemove != null) {
            handleItemPickup(toRemove);
            itemSpawner.removeItem(toRemove);
        }
    }

    private void checkGameOver() {
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
                "Move Faster",
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
                "+1 SLOW",
                item.getX(),
                item.getY(),
                Color.CYAN
            );

            break;
    }
}
    
//  SETTTERS
    public void setCurrentState(GameState state) {
        this.currentState = state;
    }

    // GETTERS
    public GameState getCurrentState() {
        return currentState;
    }

    public HomeScreen getHomeScreen() {
        return homeScreen;
    }

    public SelectMapScreen getChooseMap() {
        return selectMapScreen;
    }

    public GameOverScreen getGameOver() {
        return gameOverScreen;
    }

    public PauseScreen getPause() {
        return pauseScreen;
    }

    public JComponent getActiveScreen() {

        switch (currentState) {

            case MENU:
                return homeScreen;

            case CHOOSE_MAP:
                return selectMapScreen;

            case STORY:
                return storyScreen;

            default:
                return null;
        }
    }

    public double getTimeLeft() {
        return timer.getTimeLeft();
    }

    public Player getPlayer() {
        return player;
    }

    public ArrayList<Enemy> getEnemies() {
        return enemySpawner.getEnemies();
    }

    public ArrayList<Item> getItems() {
        return itemSpawner.getItems();
    }

    public int getScore() {
        return scoreManager.getScore();
    }

    public int getNitroCount() {
        return nitroSystem.getNitroCount();
    }

    public boolean isNitroCooldown() {
        return nitroSystem.isOnCooldown();
    }

    public double getNitroCooldown() {
        return nitroSystem.getCooldownTimer();
    }

    public int getSlowCharge() {
        return slowMotionSystem.getCharge();
    }

    public boolean isSlowActive() {
        return slowMotionSystem.isActive();
    }

    public boolean isSlowCooldown() {
        return slowMotionSystem.isOnCooldown();
    }

    public double getSlowCooldown() {
        return slowMotionSystem.getCooldownTimer();
    }

    public boolean isBoostActive() {
        return player.isBoostActive();
    }

    public int getSpeedKmh() {
        double maxPossibleSpeed = WS_MAX + WS_BOOST_BONUS + 3.0;
        double ratio = (worldSpeed - WS_MIN) / (maxPossibleSpeed - WS_MIN);
        ratio = Math.max(0, Math.min(1, ratio));
        return (int) (60 + ratio * 180);
    }

    public int getCountdownValue() {
        return countdownValue;
    }

    public int getBestScore() {
        return saveManager.getBestScore();
    }

    public double getBarPosition() {
        return nitroSystem.getBarPosition();
    }

    public double getWorldSpeed() {
        return worldSpeed;
    }  
}
