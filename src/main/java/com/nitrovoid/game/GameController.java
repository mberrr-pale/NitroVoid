package com.nitrovoid.game;

import java.util.ArrayList;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Graphics;

import com.nitrovoid.entity.Enemy;
import com.nitrovoid.entity.Item;
import static com.nitrovoid.entity.Item.TipeItem.BOOST;
import static com.nitrovoid.entity.Item.TipeItem.NITRO;
import static com.nitrovoid.entity.Item.TipeItem.SLOWMOTION;
import static com.nitrovoid.entity.Item.TipeItem.TIME;
import com.nitrovoid.entity.Player;
import static com.nitrovoid.game.GameState.CHOOSE_MAP;
import static com.nitrovoid.game.GameState.COUNTDOWN;
import static com.nitrovoid.game.GameState.MENU;
import static com.nitrovoid.game.GameState.PAUSE;
import static com.nitrovoid.game.GameState.PLAYING;
import static com.nitrovoid.game.GameState.SCORE;
import static com.nitrovoid.game.GameState.STORY;
import com.nitrovoid.input.InputHandler;
import com.nitrovoid.system.*;
import static com.nitrovoid.system.NitroSystem.NitroTiming.GOOD;
import static com.nitrovoid.system.NitroSystem.NitroTiming.MISS;
import static com.nitrovoid.system.NitroSystem.NitroTiming.PERFECT;
import com.nitrovoid.ui.screen.HomeScreen;
import com.nitrovoid.ui.screen.SelectMapScreen;
import com.nitrovoid.ui.screen.StoryScreen;
import com.nitrovoid.util.CollisionDetector;
import java.awt.Color;

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

    // SYSTEM
    private final MusicSystem music;

    private TimerSystem timer = new TimerSystem();
    private ScoreManager scoreManager = new ScoreManager();
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
    //  UI Feedback
    private String nitroFeedback = "";
    private String itemFeedback = "";
    private Color itemFeedbackColor = Color.WHITE;
    private double itemFeedbackTimer = 0;

    private boolean nitroPressed = false; // biar ga spam
    private boolean slowPressed = false; // biar ga spam
    private boolean pausePressed = false; // untuk pause
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
        storyScreen = new StoryScreen(() -> SwingUtilities.invokeLater(this::startCountdown));

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
    public void startStory() {

        currentState = GameState.STORY;
//        music.stopBGM();

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

    // START GAME
    public void startGame() {
        currentState = GameState.PLAYING;
        music.playLoop("/musics/bgm.wav");
        worldSpeed = WS_MIN;
        timer.start();
        scoreManager.reset();
        nitroSystem.reset();
        slowMotionSystem.reset();
        enemySpawner = new EnemySpawner();
        itemSpawner = new ItemSpawner();
    }

    private void updateCountdown(double deltaTime) {
        countdownTimer -= deltaTime;
        countdownValue = (int) Math.ceil(countdownTimer); // 3, 2, 1
        if (countdownTimer <= -1) {
            startGame();
        }
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

    // Restart
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

    // Pause
    private void updatePause() {
        if (input.pause && !pausePressed) {
            pausePressed = true;
            music.resumeBGM();
            currentState = GameState.PLAYING;
        }
        if (!input.pause) {
            pausePressed = false;
        }
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

    // MENU
    public void goToMenu() {

        currentState = GameState.MENU;
        timer.reset();
        enemySpawner = new EnemySpawner();
        itemSpawner = new ItemSpawner();

        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            frame.add(homeScreen, BorderLayout.CENTER);
            frame.revalidate();
            frame.repaint();
        });

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
        // input nitro & cek supaya ga spam
        if (nitroFeedbackTimer > 0) {
            nitroFeedbackTimer -= deltaTime;
            if (nitroFeedbackTimer <= 0) {
                nitroFeedback = "";
            }
        }
        if (input.nitro && !nitroPressed) {
            nitroPressed = true;
            NitroSystem.NitroTiming timing = nitroSystem.activate();
            player.applyNitro(timing);
            switch (timing) {
                case PERFECT:
                    nitroFeedback = "PERFECT!";
                    nitroFeedbackTimer = FEEDBACK_DURATION;
                    break;
                case GOOD:
                    nitroFeedback = "GOOD!";
                    nitroFeedbackTimer = FEEDBACK_DURATION;
                    break;
                case MISS:
                    nitroFeedback = "MISS!";
                    nitroFeedbackTimer = FEEDBACK_DURATION;
                    break;
            }
        }
        if (itemFeedbackTimer > 0) {
            itemFeedbackTimer -= deltaTime;
            if (itemFeedbackTimer <= 0) {
                itemFeedback = "";
            }
        }
        if (!input.nitro) {
            nitroPressed = false;
        }
        // cek input slow motion
        if (input.slowMotion && !slowPressed) {
            slowPressed = true;
            slowMotionSystem.activate();
        }
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
            scoreManager.finalizeScore();
            currentState = GameState.SCORE;
        }
    }

    private void handleItemPickup(Item item) {
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

    // SETTER
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
        return scoreManager.getBestScore();
    }

    public String getNitroFeedback() {
        return nitroFeedback;
    }

    public double getBarPosition() {
        return nitroSystem.getBarPosition();
    }

    public double getWorldSpeed() {
        return worldSpeed;
    }

    public String getItemFeedback() {
        return itemFeedback;
    }

    public Color getItemFeedbackColor() {
        return itemFeedbackColor;
    }
}
