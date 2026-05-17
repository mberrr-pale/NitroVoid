package com.nitrovoid.game;

import java.util.ArrayList;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Graphics;

import com.nitrovoid.entity.Enemy;
import com.nitrovoid.entity.Item;
import com.nitrovoid.entity.Player;
import com.nitrovoid.input.InputHandler;
import com.nitrovoid.system.*;
import com.nitrovoid.ui.screen.HomeScreen;
import com.nitrovoid.ui.screen.SelectMapScreen;
import com.nitrovoid.ui.screen.StoryScreen;
import com.nitrovoid.util.CollisionDetector;

public class GameController {
    // CORE
    private GameState currentState = GameState.MENU;
    private final JFrame frame;
    private final Player player;
    private final InputHandler input;

    // UI
    private final HomeScreen homeScreen;
    private final SelectMapScreen selectMapScreen;
    private final StoryScreen storyScreen;

    // SYSTEM
    private final MusicSystem music;

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
    private double worldSpeed = 3.0;   // px/frame awal = 60 KM/h
    private static final double WS_MIN = 3.0;   // minimum (tidak bisa lebih lambat)
    private static final double WS_NORM = 6.0;   // target normal tanpa gas
    private static final double WS_MAX = 9.0;   // maksimal dengan nitro/boost
    private static final double WS_BOOST_BONUS = 2.0; // tambahan saat boost item aktif
    private static final double WS_ACCEL = 0.5; // px/frame² — naik saat gas
    private static final double WS_PASSIVE = 0.02; // naik otomatis tiap frame (pasif)
    private double gameOverTimer = 0;
    private final double GAMEOVER_DELAY = 3.0;
    private double countdownTimer = 3.0; // 3-2-1
    private int countdownValue = 3;      // angka yang ditampilkan
    private double storyTimer = 0;       // untuk track durasi story 
    private boolean enterPressed = false;
    private boolean mouseClicked = false;
    private boolean restartPressed = false;
    private boolean backToMenuPressed = false;
    // MAP
    private String selectedMap = "Ketintang";

    // CONSTRUCTOR
    public GameController(JFrame frame, Player player, InputHandler input) {
        this.frame = frame;
        this.player = player;
        this.input = input;

        music = new MusicSystem();
        homeScreen = new HomeScreen(music);
        selectMapScreen = new SelectMapScreen();
        storyScreen = new StoryScreen(
                () -> SwingUtilities.invokeLater(this::startCountdown)
        );

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

            case MAP:
                updateMapSelect();
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
            case GAMEOVER:
                updateGameOver(deltaTime);
                break;
            case SCORE:
                updateScore();
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

        currentState = GameState.MAP;

        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            frame.add(selectMapScreen, BorderLayout.CENTER);

            frame.revalidate();
            frame.repaint();
        });
    }

    private void updateMapSelect() {

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
        if (input.enter && !enterPressed) {
            enterPressed = true;
            int idx = selectMapScreen.getSelectedIndex();
            System.out.println("[Debug] Enter pressed. Current selected index: " + idx);

            if (idx >= 0) {
                selectMapScreen.setSelectedIndex(idx); // select map
                triggerStartGame(idx); // start game immediately
            } else {
                System.out.println("[Debug] No map selected yet!");
            }
        }

        if (!input.enter) {
            enterPressed = false;
        }

        // --- Mouse left click handling ---
        if (input.mouseLeftPressed && !mouseClicked) {
            mouseClicked = true;

            int clicked = selectMapScreen.checkMouseClick(input.mouseX, input.mouseY);

            if (clicked == -3) {
                // PLAY button clicked → start game
                int selectedIdx = selectMapScreen.getSelectedIndex();
                if (selectedIdx >= 0) {
                    triggerStartGame(selectedIdx);
                } else {
                    System.out.println("[Debug] PLAY clicked but no map selected!");
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
        // classic switch
        switch (selected) {
            case 0:
                selectedMap = "Ketintang";
                break;
            case 1:
                selectedMap = "Liwet";
                break;
            case 2:
                selectedMap = "Magetan";
                break;
            default:
                return; // do not start story if index invalid
        }
        startStory();
    }

    // STORY
    private void startStory() {

        currentState = GameState.STORY;
        music.stopBGM();

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

        if (input.space) {
            storyScreen.stopVideo();
            startCountdown();
            input.space = false;
        }
    }

    private String getStoryVideo(String map) {
        switch (map) {

            case "Ketintang":
                return "/videos/story-game.mp4";

            case "Liwet":
                return "/videos/story-game.mp4";

            case "Magetan":
                return "/videos/story-game.mp4";

            default:
                return "/videos/storyline.mp4";
        }
    }

    // COUNTDOWN
    public void startCountdown() {
        currentState = GameState.COUNTDOWN;
        countdownTimer = 3.0;
        countdownValue = 3;
    }

    private void updateCountdown(double deltaTime) {
        countdownTimer -= deltaTime;
        countdownValue = (int) Math.ceil(countdownTimer);
        if (countdownTimer <= 0) {
            startGame();
        }
    }

    // START GAME
    public void startGame() {
        currentState = GameState.PLAYING;
        worldSpeed = WS_MIN;
        timer.start();
        scoreManager.reset();
        nitroSystem.reset();
        slowMotionSystem.reset();
        enemySpawner = new EnemySpawner();
        itemSpawner = new ItemSpawner();
    }

    // PLAYING
    private void updatePlaying(double deltaTime) {

        timer.update(deltaTime);

        scoreManager.update(
                deltaTime,
                player.getCurrentSpeed()
        );

        nitroSystem.update(deltaTime);

        slowMotionSystem.update(deltaTime);

        player.update(input, deltaTime);

        // Pause
        if (input.pause && !pausePressed) {

            pausePressed = true;

            currentState = GameState.PAUSE;
        }

        if (!input.pause) {
            pausePressed = false;
        }

        // Enemy collision
        for (Enemy enemy : enemySpawner.getEnemies()) {

            if (CollisionDetector.isColliding(
                    player,
                    enemy,
                    HITBOX_TOLERANCE
            )) {

                currentState = GameState.GAMEOVER;

                gameOverTimer = 0;
            }
        }

        // Time up
        if (timer.isTimeUp()) {

            scoreManager.finalizeScore();

            currentState = GameState.GAMEOVER;

            gameOverTimer = 0;
        }
    }

    // SCORE
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

    // RESTART
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

    // MENU
    public void goToMenu() {

        currentState = GameState.MENU;
        timer.reset();
        gameOverTimer = 0;
        enemySpawner = new EnemySpawner();
        itemSpawner = new ItemSpawner();
        music.playLoop("/musics/bgm.wav");

        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            frame.add(homeScreen, BorderLayout.CENTER);
            frame.revalidate();
            frame.repaint();
        });

    }

    private void updatePause() {
        if (input.pause && !pausePressed) {
            pausePressed = true;
            currentState = GameState.PLAYING;
        }
        if (!input.pause) {
            pausePressed = false;
        }
    }

    private void updateGameOver(double deltaTime) {
        gameOverTimer += deltaTime;
        if (gameOverTimer >= GAMEOVER_DELAY) {
            currentState = GameState.SCORE;
        }
    }

    // EXIT
    public void exitGame() {
        System.exit(0);
    }

    // RENDER
    public void render(Graphics g, int w, int h) {

        switch (currentState) {

            case MENU:
                homeScreen.draw(g, w, h);
                break;

            case MAP:
                selectMapScreen.draw(g, w, h);
                break;

//            case COUNTDOWN:
//                drawCountdown(g, w, h);
//                break;
//
//            case PLAYING:
//                drawPlaying(g);
//                break;
//
//            case PAUSE:
//                drawPause(g, w, h);
//                break;
//
//            case GAMEOVER:
//                drawGameOver(g, w, h);
//                break;
//
//            case SCORE:
//                drawScore(g, w, h);
//                break;
        }
    }

    // SETTER
    public void setCurrentState(GameState state) {
        this.currentState = state;
    }

    // GETTERS
    public GameState getCurrentState() {
        return currentState;
    }

    public JComponent getActiveScreen() {

        switch (currentState) {

            case MENU:
                return homeScreen;

            case MAP:
                return selectMapScreen;

            case STORY:
                return storyScreen;

            default:
                return null;
        }
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

    public double getTimeLeft() {
        return timer.getTimeLeft();
    }

    public int getScore() {
        return scoreManager.getScore();
    }

    public int getBestScore() {
        return scoreManager.getBestScore();
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
        return player.getSpeedKmh();
    }

    public int getCountdownValue() {
        return countdownValue;
    }

    public double getGameOverTimer() {
        return gameOverTimer;
    }
}
