package com.nitrovoid.game;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import com.nitrovoid.entity.Player;
import com.nitrovoid.entity.Enemy;
import com.nitrovoid.input.InputHandler;
import com.nitrovoid.entity.Item;

public class GamePanel extends JPanel implements Runnable {
    final int width = 800;
    final int height = 600;
    Thread gameThread;
    Player player;
    InputHandler input;
    GameController controller;

    public GamePanel() {
        this.setPreferredSize(new Dimension(width, height));
        this.setDoubleBuffered(true);
        input = new InputHandler();
        this.addKeyListener(input);
        this.setFocusable(true);

        player = new Player();
        // GameController pegang semua logic
        controller = new GameController(player, input);
        controller.startGame();
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
        if (controller.getCurrentState() == GameState.PAUSE && input.pause) {
        // handled di controller via pausePressed flag
        } controller.update(deltaTime);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Render sesuai state
        if (controller.getCurrentState() == GameState.PLAYING) {
            player.draw(g);
            for (Enemy enemy : controller.getEnemies()) {
                enemy.draw(g);
            } for (Item item : controller.getItems()){
                item.draw(g);
            }            
            g.setColor(Color.black);
            g.drawString("Time: " + (int) controller.getTimeLeft(), 10, 20);
            g.drawString("Score: " + controller.getScore(), 10, 40);
            g.drawString("Nitro: " + controller.getNitroCount(), 10, 60);
            if (controller.isNitroCooldown()){
                g.drawString("Cooldown: " + (int) controller.getNitroCooldown() + "s", 10, 80);
            }
            g.drawString("Slow: " + controller.getSlowCharge(), 10, 100);
            if (controller.isSlowActive()) {
                g.drawString("SLOW AKTIF!", 10, 120);
            }
            if (controller.isSlowCooldown()) {
                g.drawString("Slow CD: " + (int) controller.getSlowCooldown() + "s", 10, 120);
            }
            g.drawString("Speed: " + controller.getSpeedKmh() + " KM/h", 10, 140);            
            if (controller.getPlayer().isBoostActive()) {
                g.drawString("BOOST AKTIF!", 10, 160);
            }
        }

        if (controller.getCurrentState() == GameState.GAMEOVER) {
            g.setColor(Color.RED);
            g.drawString("GAME OVER", width / 2 - 30, height / 2);
        }
        if (controller.getCurrentState() == GameState.PAUSE) {
            g.setColor(Color.BLACK);
            g.drawString("PAUSE", width / 2 - 20, height / 2);
            g.drawString("Tekan ESC untuk lanjut", width / 2 - 60, height / 2 + 20);
        }
    }
}
