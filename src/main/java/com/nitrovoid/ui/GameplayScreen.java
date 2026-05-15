package com.nitrovoid.ui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

import com.nitrovoid.game.GameController;
import com.nitrovoid.entity.Player;
import com.nitrovoid.entity.Enemy;
import com.nitrovoid.entity.Item;

public class GameplayScreen {
    private BufferedImage mapBackground;
    private double mapOffsetY = 0;
    private BufferedImage road;
    private double roadOffsetY = 0;
    private BufferedImage playerCar;
    private BufferedImage[] enemyCars;
    private BufferedImage boostItem;
    private BufferedImage timeItem;
    private BufferedImage nitroItem;
    private BufferedImage slowItem;
    
    public GameplayScreen() {
        try {
            mapBackground = ImageIO.read(
                getClass().getResourceAsStream(
                    "/gameplay/maps/ketintang_map.png"));
            road = ImageIO.read(
                getClass().getResourceAsStream(
                    "/gameplay/maps/ktt_road.png"));
            playerCar = ImageIO.read(
                getClass().getResourceAsStream(
                    "/gameplay/vehicles/player_car.png"));
            enemyCars = new BufferedImage[5];
            enemyCars[0] = ImageIO.read(
                getClass().getResourceAsStream(
                    "/gameplay/vehicles/enemy_1.png"));
            enemyCars[1] = ImageIO.read(
                getClass().getResourceAsStream(
                    "/gameplay/vehicles/enemy_2.png"));
            enemyCars[2] = ImageIO.read(
                getClass().getResourceAsStream(
                    "/gameplay/vehicles/enemy_3.png"));
            enemyCars[3] = ImageIO.read(
                getClass().getResourceAsStream(
                    "/gameplay/vehicles/enemy_4.png"));
            enemyCars[4] = ImageIO.read(
                getClass().getResourceAsStream(
                    "/gameplay/vehicles/enemy_5.png"));   
            boostItem = ImageIO.read(
            getClass().getResourceAsStream(
                "/gameplay/items/boost.png"));
            timeItem = ImageIO.read(
                getClass().getResourceAsStream(
                    "/gameplay/items/time.png"));
            nitroItem = ImageIO.read(
                getClass().getResourceAsStream(
                    "/gameplay/items/nitro.png"));
            slowItem = ImageIO.read(
                getClass().getResourceAsStream(
                    "/gameplay/items/slow.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
    public void update(double worldSpeed, int screenHeight) {
        mapOffsetY += worldSpeed * 0.2;
        if (mapOffsetY >= screenHeight) {
            mapOffsetY = 0;
        }
        roadOffsetY += worldSpeed * 2.0;
        if (roadOffsetY >= screenHeight) {
            roadOffsetY = 0;
        }
    }
    public void drawBackground(Graphics g, int width, int height) {
        // SIDE ENVIRONMENT
        g.drawImage(mapBackground,0,(int) mapOffsetY,
                    width,height,null);
        g.drawImage(mapBackground, 0,(int) mapOffsetY - height,
                    width, height, null);
        // ROAD
        int roadWidth = 300;
        int roadX = (width - roadWidth) / 2;
        g.drawImage(road, roadX, (int) roadOffsetY,
               roadWidth, height, null);
        g.drawImage(road, roadX, (int) roadOffsetY - height,
                roadWidth, height, null);
    }
    public void drawEntities(Graphics g, GameController controller) {
        // PLAYER
        Player player = controller.getPlayer();
        g.drawImage(playerCar,player.getX(),player.getY(),
               player.getWidth(),player.getHeight(),null);
        // ENEMIES
        for (Enemy enemy : controller.getEnemies()) {
            int index = enemy.getVehicleIndex();
        g.drawImage(enemyCars[index],enemy.getX(),enemy.getY(),
               enemy.getWidth(),enemy.getHeight(),null);         
        }
    }
    public void drawItems(Graphics g, GameController controller) {
        for (Item item : controller.getItems()) {
        BufferedImage icon = null;
        switch (item.getTipe()) {
            case BOOST:
                icon = boostItem;
                break;
            case TIME:
                icon = timeItem;
                break;
            case NITRO:
                icon = nitroItem;
                break;
            case SLOWMOTION:
                icon = slowItem;
                break; }
        g.drawImage(icon, item.getX(), item.getY(),
            item.getWidth(), item.getHeight(), null);
        }
    }
}