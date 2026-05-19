package com.nitrovoid.ui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.Color;
import java.awt.FontMetrics;
import java.util.ArrayList;

import com.nitrovoid.game.GameController;
import com.nitrovoid.entity.Player;
import com.nitrovoid.entity.Enemy;
import com.nitrovoid.entity.Item;

public class GameplayScreen {
    private BufferedImage countdownBg;
    private BufferedImage count3;
    private BufferedImage count2;
    private BufferedImage count1;
    private BufferedImage goText;
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
    private BufferedImage timerFrame;
    private BufferedImage scoreFrame;
    private BufferedImage bestScoreFrame;
    private BufferedImage nitroFrame;
    private BufferedImage slowMotionFrame;
    private BufferedImage speedometerFrame;
    private BufferedImage nitroBarFrame;
    private BufferedImage pauseBtn;
        
    public void setMap (GameController.MapType map){loadMap(map);}
    public void addFeedback(String text, int x, int y, Color color) {
    feedbacks.add(new FeedbackEntry(text, x, y, color)); }
    public GameplayScreen() { 
        loadCountDown();
        loadVehicles(); 
        loadItems(); 
        loadHUD(); }
    
    private BufferedImage load(String path) {
        try {
            return ImageIO.read(getClass().getResource(path));
        }catch (Exception e){
            e.printStackTrace();
        return null;
        }
    }
    private void loadCountDown(){
        countdownBg =
                load("/gameplay/countdown/bg.png");
        count3 = 
                load("/gameplay/countdown/3.png");
        count2 = 
                load("/gameplay/countdown/2.png");
        count1 = 
                load("/gameplay/countdown/1.png");
        goText = 
                load("/gameplay/countdown/go.png");
    }
    
    private void loadMap(GameController.MapType map){
        switch(map){
            case KTT :
                mapBackground =
                    load("/gameplay/maps/ktt_map.png");
                road =
                    load("/gameplay/maps/ktt_road.png");
                break;
            case LIWET:
                mapBackground =
                    load("/gameplay/maps/liwet_map.png");
                road =
                    load("/gameplay/maps/liwet_road.png");
                break;

            case MGT:
                mapBackground =
                    load("/gameplay/maps/mgt_map.png");
                road =
                    load("/gameplay/maps/mgt_road.png");
                break;
        }
    }
    private void loadVehicles() { 
        playerCar = 
                load("/gameplay/vehicles/player_car.png"); 
        enemyCars = new BufferedImage[] { 
                load("/gameplay/vehicles/enemy_1.png"), 
                load("/gameplay/vehicles/enemy_2.png"), 
                load("/gameplay/vehicles/enemy_3.png"), 
                load("/gameplay/vehicles/enemy_4.png"), 
                load("/gameplay/vehicles/enemy_5.png") 
        }; 
    }
    private void loadItems() { 
        boostItem = 
                load("/gameplay/items/boost.png"); 
        timeItem = 
                load("/gameplay/items/time.png"); 
        nitroItem = 
                load("/gameplay/items/nitro.png"); 
        slowItem = 
                load("/gameplay/items/slow.png"); 
    }
    private void loadHUD() { 
        timerFrame = 
                load("/gameplay/hud/timer_frame.png"); 
        scoreFrame = 
                load("/gameplay/hud/score_frame.png"); 
        bestScoreFrame = 
                load("/gameplay/hud/bestscore_frame.png"); 
        nitroFrame = 
                load("/gameplay/hud/nitro_frame.png"); 
        slowMotionFrame = 
                load("/gameplay/hud/slowmo_frame.png"); 
        speedometerFrame = 
                load("/gameplay/hud/speedometer_frame.png"); 
        nitroBarFrame = 
                load("/gameplay/hud/timingbar_frame.png");
        pauseBtn = 
                load("/gameplay/hud/pause_frame.png");
    }    
    public void drawCountDown(
            Graphics g,
            GameController controller,
            int width,
            int height
    ) {

        // BACKGROUND
        g.drawImage(
                countdownBg,
                0,
                0,
                width,
                height,
                null
        );

        BufferedImage image = null;

        switch (controller.getCountdownValue()) {

            case 3:
                image = count3;
                break;

            case 2:
                image = count2;
                break;

            case 1:
                image = count1;
                break;

            default:
                image = goText;
                break;
        }

        // RESPONSIVE SIZE
        int imgWidth = width / 4;
        int imgHeight = height / 3;

        // CENTER POSITION
        int x = (width - imgWidth) / 2;
        int y = (height - imgHeight) / 2;

        // DRAW COUNTDOWN IMAGE
        g.drawImage(
                image,
                x,
                y,
                imgWidth,
                imgHeight,
                null
        );
    }
    public void update(double worldSpeed, int screenHeight, boolean paused, double deltaTime) {
        if (paused) {
            return;
        }
        mapOffsetY += worldSpeed * 0.2;
        if (mapOffsetY >= screenHeight) {
            mapOffsetY = 0;
        }
        roadOffsetY += worldSpeed * 2.0;
        if (roadOffsetY >= screenHeight) {
            roadOffsetY = 0;
        }
        updateFeedback(deltaTime);
    }
    public void drawLoadMap(Graphics g, int width, int height) {
        // SIDE ENVIRONMENT
        g.drawImage(
            mapBackground,0,(int) mapOffsetY,
            width,height,null);
        g.drawImage(
            mapBackground, 0,(int) mapOffsetY - height,
            width, height, null);
        // ROAD
        int roadWidth = 300;
        int roadX = (width - roadWidth) / 2;
        g.drawImage(
            road, roadX, (int) roadOffsetY,
            roadWidth, height, null);
        g.drawImage(
            road, roadX, (int) roadOffsetY - height,
            roadWidth, height, null);
    }
    public void drawEntities(Graphics g, GameController controller) {
        // PLAYER
        Player player = controller.getPlayer();
        g.drawImage(
            playerCar,player.getX(),player.getY(),
            player.getWidth(),player.getHeight(),null);
        // ENEMIES
        for (Enemy enemy : new ArrayList<>(controller.getEnemies())) {
            int index = enemy.getVehicleIndex();
        g.drawImage(
            enemyCars[index],enemy.getX(),enemy.getY(),
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

        g.drawImage(
            icon, item.getX(), item.getY(),
            item.getWidth(), item.getHeight(), null);
        }
    }
    public void drawHUD(Graphics g, GameController controller) {
        drawTimerHUD(g, controller);
        drawScoreHUD(g, controller);
        drawBestScoreHUD(g, controller);
        drawNitroHUD(g, controller);
        drawSlowMotionHUD(g, controller);
        drawSpeedometerHUD(g, controller);
        drawNitroTimingBar(g, controller);
        drawPauseBtn(g);
    }
    private void drawTimerHUD(Graphics g, GameController controller){
        int x = 15;
        int y = 15;
        int width = 165;
        int height = 50;
        // Frame
        g.drawImage(timerFrame, x, y, width, height, null);
        // Timer Text 
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(Font.BOLD,20f));
        String timeText = 
            "Time : " + (int)controller.getTimeLeft();
        g.drawString(timeText, x + 48 , y + 32);
    }
    private void drawScoreHUD(Graphics g, GameController controller){
        int x = 300;
        int y = 15;
        int width = 200;
        int height = 50;
        // FRAME
        g.drawImage(scoreFrame, x, y, width, height,null);
        g.setColor(Color.WHITE);
        Font speedFont = g.getFont().deriveFont(Font.BOLD,16f);
        g.setFont(speedFont);
        String scoreText = "Score : " + (int)controller.getScore();
        FontMetrics fm =
            g.getFontMetrics();
        int textX = x + (
            width - fm.stringWidth(scoreText)) / 2 + 10;
        int textY = y + 30;
        g.drawString(scoreText,textX,textY);        

    }
    private void drawBestScoreHUD(Graphics g,GameController controller) {
        int width = 165;
        int height = 55;
        int x = 570;
        int y = 15;
        // FRAME
        g.drawImage(
            bestScoreFrame,x,y,
            width,height,null);
        // BEST SCORE
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(
                Font.BOLD, 14f));
        g.drawString("BEST SCORE", x + 45, y + 27);
        //SCORE
        g.setFont(g.getFont().deriveFont(
            Font.BOLD, 16f));
        String bestScoreText = String.valueOf(controller.getBestScore());
        g.drawString(
            bestScoreText,
             x + 65 , y + 46);
    }
    private void drawNitroHUD(Graphics g,GameController controller) {
        int x = 15;
        int y = 85;
        int width = 165;
        int height = 45;
        // FRAME
        g.drawImage(nitroFrame,x,y,
            width,height,null);
        // NITRO COUNT
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(
                Font.BOLD, 20f));
        String nitroText = 
                "Nitro : " + (int)controller.getNitroCount();
        g.drawString(
            nitroText,
             x + 50 , y + 30);
    }
    private void drawSlowMotionHUD(Graphics g,GameController controller) {
        int x = 15;
        int y = 135;
        int width = 165;
        int height = 45;
        // FRAME
        g.drawImage(
            slowMotionFrame,x,y,
            width,height,null);
        // CHARGE COUNT
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(
            Font.BOLD, 20f));
        String slowmoText = 
                "Slow : " + (int)controller.getSlowCharge();
        g.drawString(
            slowmoText,
            x + 50 , y + 30);
    }
    private void drawSpeedometerHUD(Graphics g,GameController controller) {
        int width = 215;
        int height = 115;
        int x = 575;
        int y = 430;
        // FRAME
        g.drawImage(speedometerFrame,x,y,
            width,height,null);
        // SPEED TEXT
        g.setColor(Color.WHITE);
        Font speedFont = g.getFont().deriveFont(Font.BOLD,22f);
        g.setFont(speedFont);
        String speedText = controller.getSpeedKmh() + " KM/h";
        FontMetrics fm =
            g.getFontMetrics();
        int textX = x + (
                width - fm.stringWidth(speedText)) / 2;
        int textY = y + 100;
        g.drawString(
            speedText,textX,textY);
    }
    private void drawNitroTimingBar(Graphics g,GameController controller) {
        int barX = 200;
        int barY = 545;
        int barWidth = 400;
        int barHeight = 50;
        // PNG BAR
        g.drawImage(nitroBarFrame,barX,barY,
               barWidth,barHeight,null);
        // MOVING INDICATOR
        g.setColor(Color.WHITE);
        int indicatorX =barX +(int)(
                controller.getBarPosition()* barWidth);
        g.fillRect(indicatorX - 2,barY - 4,4,barHeight + 4);
    }
    private void drawPauseBtn( Graphics g) {
        int width = 55;
        int height = 55;
        int x = 740;
        int y = 15;
        g.drawImage(pauseBtn,x,y,width,height,null);
    }
    
    public void updateFeedback(double dt) {
    java.util.Iterator<FeedbackEntry> it = feedbacks.iterator();

    while (it.hasNext()) {
        FeedbackEntry f = it.next();
        f.life -= dt;
        f.alpha = Math.max(0f, f.life / 1.5f);
        f.y -= 1;

        if (f.life <= 0) it.remove();
    }
}
    private static class FeedbackEntry {
    String text;
    int x, y;
    float alpha = 1f;
    float life = 1.5f;
    Color color;

    FeedbackEntry(String text, int x, int y, Color color) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
    }
}
    private java.util.ArrayList<FeedbackEntry> feedbacks = new java.util.ArrayList<>();

    public void drawFeedback(Graphics g) {
        Font base = g.getFont();
        Font fnt = base.deriveFont(Font.BOLD, 22f);

    for (FeedbackEntry e : new ArrayList<>(feedbacks)) {
            g.setFont(fnt);

            Color c = new Color(
                e.color.getRed(),
                e.color.getGreen(),
                e.color.getBlue(),
                (int)(e.alpha * 255)
            );

            // SHADOW
            g.setColor(new Color(0,0,0,(int)(e.alpha * 180)));
            g.drawString(e.text, e.x + 2, e.y + 2);

            // MAIN TEXT
            g.setColor(c);
            g.drawString(e.text, e.x, e.y);
        }
        g.setFont(base);
    }
}


