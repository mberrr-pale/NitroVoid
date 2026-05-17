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

public class GamePanel extends JPanel implements Runnable {

    final int width = GameConfig.SCREEN_WIDTH;
    final int height = GameConfig.SCREEN_HEIGHT;
    Thread gameThread;
    private double roadOffset = 0;
    Player player;
    InputHandler input;
    GameController controller;
    private GameplayScreen gameplayScreen;

    public GamePanel(JFrame frame) {

        this.setPreferredSize(new Dimension(width, height));
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        input = new InputHandler();
        this.addKeyListener(input);
        this.addMouseListener(input);
        this.addMouseMotionListener(input);

        player = new Player();
        controller = new GameController(frame, player, input);
        controller.setCurrentState(GameState.MENU);
        gameplayScreen = new GameplayScreen();
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
        gameplayScreen.update(controller.getWorldSpeed(), height);
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
                drawPause(g);
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
    }

    private void drawGameplay(Graphics g) {
        if (controller.getCurrentState() == GameState.PLAYING) {
            Font defaultFont = g.getFont();
            // BACKGROUND
            gameplayScreen.drawBackground(g, width, height);
            // ITEM
            gameplayScreen.drawItems(g, controller);
            // ENTITY
            gameplayScreen.drawEntities(g, controller);
            // HUD TOP
            gameplayScreen.drawHUD(g, controller);
            // FEEDBACK TEXT
            if (!controller.getNitroFeedback().isEmpty()) {
                g.setFont(defaultFont.deriveFont(Font.BOLD, 24f));
                switch (controller.getNitroFeedback()) {
                    case "PERFECT!":
                        g.setColor(Color.GREEN);
                        break;
                    case "GOOD!":
                        g.setColor(Color.YELLOW);
                        break;
                    case "MISS!":
                        g.setColor(Color.RED);
                        break;
                    default:
                        g.setColor(Color.WHITE);
                        break;
                }
                g.drawString(controller.getNitroFeedback(), width / 2 - 60, height - 120);
            }
        }
    }

    private void drawPause(Graphics g) {
        Font defaultFont = g.getFont();
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE);
        g.setFont(defaultFont.deriveFont(Font.BOLD, 32f));
        g.drawString("PAUSE", width / 2 - 50, height / 2 - 40);
        g.setFont(defaultFont.deriveFont(16f));
        g.drawString("ESC — Resume", width / 2 - 60, height / 2 + 10);
        g.drawString("R   — Restart", width / 2 - 60, height / 2 + 35);
        g.drawString("B   — Back To Menu", width / 2 - 60, height / 2 + 60);
    }

    private void drawScore(Graphics g) {
        Font defaultFont = g.getFont();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE);
        g.setFont(defaultFont.deriveFont(Font.BOLD, 32f));
        g.drawString("GAME OVER", width / 2 - 80, height / 2 - 80);
        g.setFont(defaultFont.deriveFont(18f));
        g.drawString("SCORE      : " + controller.getScore(), width / 2 - 80, height / 2 - 40);
        g.drawString("BEST SCORE : " + controller.getBestScore(), width / 2 - 80, height / 2 - 15);
        g.setFont(defaultFont.deriveFont(16f));
        g.drawString("R — Restart", width / 2 - 60, height / 2 + 30);
        g.drawString("B — Back To Menu", width / 2 - 60, height / 2 + 55);
        g.setFont(defaultFont);
    }
}
