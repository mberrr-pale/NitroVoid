package com.nitrovoid.ui.components;

import com.nitrovoid.system.SaveManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.text.NumberFormat;
import java.util.Locale;

public class BestScore {

    private Image trophyImg;

    public BestScore() {
        trophyImg = Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/images/trofi.png")
        );
    }

    public void draw(Graphics g, int x, int y) {

        int w = 200;
        int h = 60;

        // BORDER ONLY
        g.setColor(new Color(255, 204, 0));
        g.drawRoundRect(x, y, w, h, 20, 20);

        // TROPHY IMAGE
        int imgSize = (int) (h * 0.8); // 60% dari tinggi box

        g.drawImage(trophyImg, x + 10, y + (h - imgSize) / 2, imgSize, imgSize, null);
        // TEXT
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("BEST SCORE", x + 70, y + 20);

        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.setColor(Color.YELLOW);
        int bestScore = SaveManager.getInstance().getBestScore();
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        String formattedScore = nf.format(bestScore);

        g.drawString(formattedScore, x + 70, y + 50);
    }
}
