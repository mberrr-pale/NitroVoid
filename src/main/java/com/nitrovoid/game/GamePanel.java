package com.nitrovoid.game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

import com.nitrovoid.entity.Enemy;
import com.nitrovoid.entity.Item;
import com.nitrovoid.entity.Player;
import com.nitrovoid.input.InputHandler;
import com.nitrovoid.game.GameConfig;

public class GamePanel extends JPanel implements Runnable {
    final int width  = GameConfig.SCREEN_WIDTH;
    final int height = GameConfig.SCREEN_HEIGHT;
    Thread gameThread;
    Player player;
    InputHandler input;
    GameController controller;

    public GamePanel() {
        this.setPreferredSize(new Dimension(width, height));
        this.setDoubleBuffered(true);

        input      = new InputHandler();
        this.addKeyListener(input);
        this.setFocusable(true);

        player     = new Player();
        controller = new GameController(player, input);
        controller.setCurrentState(GameState.MENU);
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
    }
    
// RENDERING 
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
                drawStory(g);
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
        Font defaultFont = g.getFont();
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        
        g.setColor(Color.WHITE);
        g.setFont(defaultFont.deriveFont(24f));
        
        g.drawString(
            "PILIH MAP",
            width / 2 - 55,
            height / 2 - 60
        );
        g.setFont(defaultFont.deriveFont(14f));
        g.drawString(
            "← / → — Pilih Map",
            width / 2 - 60,
            height / 2 + 40
        );
        g.drawString(
            "ENTER — Mulai",
            width / 2 - 60,
            height / 2 + 60
        );
    }
    private void drawStory(Graphics g) {
        Font defaultFont = g.getFont();
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        
        g.setColor(Color.WHITE);
        g.setFont(defaultFont.deriveFont(16f));
        
        g.drawString(
            "Kota dikuasai pembalap ilegal...",
            width / 2 - 120,
            height / 2 - 20
        );
        g.drawString(
            "Tekan SPACE untuk lanjut",
            width / 2 - 90,
            height / 2 + 20
        );
    }
    private void drawCountdown(Graphics g) {
        Font defaultFont = g.getFont();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.WHITE);
        g.setFont(defaultFont.deriveFont(72f));

        g.drawString(
            String.valueOf(controller.getCountdownValue()),
            width / 2 - 20,
            height / 2
        );
    }
    private void drawGameplay(Graphics g) {
        if (controller.getCurrentState() == GameState.PLAYING) {
            Font defaultFont = g.getFont();
            // BACKGROUND
            g.setColor(new Color(25, 25, 25));
            g.fillRect(0, 0, width, height);

            g.setColor(new Color(60, 60, 60));
            g.fillRect(GameConfig.ROAD_LEFT,0,GameConfig.ROAD_WIDTH, height);

            g.setColor(Color.WHITE);
            g.fillRect(GameConfig.ROAD_LEFT,0,5,height);
            g.fillRect(GameConfig.ROAD_RIGHT - 5,0,5,height);
            
            int roadOffset = (int)(System.currentTimeMillis() / 10 % 60);
            g.setColor(Color.WHITE);
            for (int i = -60; i < height; i += 60) {
                g.fillRect(width/2 - 5, i + roadOffset, 10, 40);
            }
            // ENTITY
            player.draw(g);

            for (Enemy enemy : controller.getEnemies()) {
                enemy.draw(g);
            }

            for (Item item : controller.getItems()) {
                item.draw(g);
            }
            // HUD TOP
            g.setColor(Color.WHITE);
            g.setFont(defaultFont.deriveFont(Font.BOLD, 18f));
            g.drawString("TIME : "  + (int) controller.getTimeLeft(), 20,           30);
            g.drawString("SCORE : " + controller.getScore(),           width/2 - 70, 30);
            g.drawString("BEST : "  + controller.getBestScore(),       width - 180,  30);
            // HUD LEFT
            g.setFont(defaultFont.deriveFont(
                     Font.BOLD, 16f));
            // Nitro
            g.setColor(Color.WHITE);
            g.drawString("NITRO : " + controller.getNitroCount(), 20, 100);
            if (controller.isNitroCooldown()) {
                g.setColor(Color.RED);
                g.drawString("CD : " + (int) controller.getNitroCooldown() + "s", 20, 120);
            }
            // Slow
            g.setColor(Color.WHITE);
            g.drawString("SLOW : " + controller.getSlowCharge(), 20, 160);
            if (controller.isSlowActive()) {
                g.setColor(Color.CYAN);
                g.drawString("SLOW ACTIVE!", 20, 180);
            }
            if (controller.isSlowCooldown()) {
                g.setColor(Color.RED);
                g.drawString("CD : " + (int) controller.getSlowCooldown() + "s", 20, 200);
            }
            // Boost
            g.setColor(Color.WHITE);
            if (controller.getPlayer().isBoostActive()) {
                g.setColor(Color.ORANGE);
                g.drawString("BOOST ACTIVE!", 20, 240);
            }
            // SPEED HUD
            g.setColor(Color.WHITE);
            g.setFont(defaultFont.deriveFont(Font.BOLD, 28f));
            g.drawString(controller.getSpeedKmh() + " KM/H", 560, 520);
            // NITRO TIMING BAR
            final int barX      = 220;
            final int barY      = 520;
            final int barWidth  = 360;
            final int barHeight = 22;

            g.setColor(Color.DARK_GRAY);
            g.fillRect(barX, barY, barWidth, barHeight);
            // GOOD zone
            g.setColor(Color.YELLOW);
            g.fillRect(barX + (int)(0.30 * barWidth), barY,
                       (int)(0.40 * barWidth), barHeight);
            // PERFECT zone
            g.setColor(Color.GREEN);
            g.fillRect(barX + (int)(0.45 * barWidth), barY,
                       (int)(0.10 * barWidth), barHeight);
            // Indicator
            g.setColor(Color.WHITE);
            g.fillRect(barX + (int)(controller.getBarPosition() * barWidth) - 2,
                       barY - 4, 4, barHeight + 8);
            // Border & label
            g.setColor(Color.BLACK);
            g.drawRect(barX, barY, barWidth, barHeight);
            g.setFont(defaultFont.deriveFont(Font.BOLD, 14f));
            g.drawString("NITRO", barX - 55, barY + 16);
            // FEEDBACK TEXT
            if (!controller.getNitroFeedback().isEmpty()) {
                g.setFont(defaultFont.deriveFont(Font.BOLD, 24f));
                switch (controller.getNitroFeedback()) {
                    case "PERFECT!": g.setColor(Color.GREEN);  break;
                    case "GOOD!":    g.setColor(Color.YELLOW); break;
                    case "MISS!":    g.setColor(Color.RED);    break;
                    default:         g.setColor(Color.WHITE);  break;
                }
                g.drawString(controller.getNitroFeedback(), width/2 - 60, height - 120);
            }
        }
    }    
    private void drawPause(Graphics g){
        Font defaultFont = g.getFont();
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE);
        g.setFont(defaultFont.deriveFont(Font.BOLD, 32f));
        g.drawString("PAUSE",              width/2 - 50,  height/2 - 40);
        g.setFont(defaultFont.deriveFont(16f));
        g.drawString("ESC — Resume",       width/2 - 60,  height/2 + 10);
        g.drawString("R   — Restart",      width/2 - 60,  height/2 + 35);
        g.drawString("B   — Back To Menu", width/2 - 60,  height/2 + 60);
    }   
    private void drawScore(Graphics g){
        Font defaultFont = g.getFont();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE);
        g.setFont(defaultFont.deriveFont(Font.BOLD, 32f));
        g.drawString("GAME OVER",              width/2 - 80,  height/2 - 80);
        g.setFont(defaultFont.deriveFont(18f));
        g.drawString("SCORE      : " + controller.getScore(),      width/2 - 80, height/2 - 40);
        g.drawString("BEST SCORE : " + controller.getBestScore(),  width/2 - 80, height/2 - 15);
        g.setFont(defaultFont.deriveFont(16f));
        g.drawString("R — Restart",      width/2 - 60, height/2 + 30);
        g.drawString("B — Back To Menu", width/2 - 60, height/2 + 55);
        g.setFont(defaultFont);
        }
}