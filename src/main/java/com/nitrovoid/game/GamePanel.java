package com.nitrovoid.game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.nitrovoid.entity.Player;
import com.nitrovoid.input.InputHandler;
import com.nitrovoid.ui.GameplayScreen;
import com.nitrovoid.ui.screen.PauseScreen;

public class GamePanel extends JPanel implements Runnable {

    final int width = GameConfig.SCREEN_WIDTH;
    final int height = GameConfig.SCREEN_HEIGHT;
    Thread gameThread;
    private double roadOffset = 0;
    Player player;
    InputHandler input;
    GameController controller;
    private GameplayScreen gameplayScreen;
    private PauseScreen pauseScreen;

    public GamePanel(JFrame frame) {

        this.setPreferredSize(new Dimension(width, height));
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        input = new InputHandler();

        this.addKeyListener(input);
        this.addMouseListener(input);
        this.addMouseMotionListener(input);  

        player = new Player();

        controller = new GameController(frame, player, input, gameplayScreen);
        controller.initSave();
        controller.setCurrentState(GameState.MENU);

        gameplayScreen = new GameplayScreen();
        gameplayScreen.setMap(controller.getSelectedMap());
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            double deltaTime = (currentTime - lastTime) / 1_000_000_000.0;
            lastTime = currentTime;
            update(deltaTime);
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update(double deltaTime) {
        controller.update(deltaTime);
        boolean paused = (
            controller.getCurrentState() == GameState.PAUSE);
        gameplayScreen.update(
            controller.getWorldSpeed(), height, paused);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        switch (controller.getCurrentState()) {
            case MENU:
                drawMenu(g);
                break;
            case CHOOSE_MAP:
                drawChooseMap(g);
                break;
            case STORY:
                break;
            case COUNTDOWN:
                drawCountdown(g);
                break;
            case PLAYING:
                drawGameplay(g);
                break;
            case PAUSE:
                drawGameplay(g);
                controller.getPause().draw(g, width, height);
                break;
            case SCORE:
                drawScore(g);
                break;
            default:
                break;
        }
    }

    private void drawMenu(Graphics g) {
        controller.getHomeScreen().draw(g, width, height);
    }

    private void drawChooseMap(Graphics g) {
        controller.getChooseMap().draw(g, width, height);
    }

    private void drawCountdown(Graphics g) {
        gameplayScreen.drawCountDown(g, controller, width, height);
        gameplayScreen.setMap(controller.getSelectedMap());

    }

    private void drawGameplay(Graphics g) {
        if (controller.getCurrentState() == GameState.PLAYING) {
            Font defaultFont = g.getFont();
            // BACKGROUND
            gameplayScreen.drawLoadMap(g, width, height);
            // ITEM
            gameplayScreen.drawItems(g, controller);
            // ENTITY
            gameplayScreen.drawEntities(g, controller);
            // HUD TOP
            gameplayScreen.drawHUD(g, controller);
            // FEEDBACK TEXT
            gameplayScreen.drawFeedback(g);
        }
    }     
    private void drawScore(Graphics g){
        controller.drawGameOver(g);
    }
}
