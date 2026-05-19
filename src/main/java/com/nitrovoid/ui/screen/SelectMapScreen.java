package com.nitrovoid.ui.screen;

import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

import com.nitrovoid.system.SaveManager;
import com.nitrovoid.ui.components.BestScore;

public class SelectMapScreen extends JPanel {

    private BestScore bestScoreUI;

    private static class MapItem {

        String name;
        BufferedImage idle;
        BufferedImage selected;
        BufferedImage lock;
        boolean locked;

        MapItem(String name, BufferedImage idle, BufferedImage selected, BufferedImage lock, boolean locked) {
            this.name = name;
            this.idle = idle;
            this.selected = selected;
            this.lock = lock;
            this.locked = locked;
        }
    }

    private MapItem[] maps;

    private int selectedIndex = 0;  // tetap private
    private int hoverIndex = -1;
    private boolean startHover = false;
    private boolean backHover = false;

    private Rectangle[] renderBoxes;
    private Rectangle startButton;
    private Rectangle backButton;

    private BufferedImage background;
    private BufferedImage startImg;
    private BufferedImage backImg;
    private BufferedImage titleImg;

    public SelectMapScreen() {

        setOpaque(true);

        maps = new MapItem[]{
            new MapItem("Ketintang",
            load("/images/ketintang.png"),
            load("/images/ketintang-selected.png"),
            load("/images/ketintang-lock.png"),
            false),
            new MapItem("Liwet",
            load("/images/liwet.png"),
            load("/images/liwet-selected.png"),
            load("/images/liwet-lock.png"),
            true), // default locked
            new MapItem("Magetan",
            load("/images/magetan.png"),
            load("/images/magetan-selected.png"),
            load("/images/magetan-lock.png"),
            true) // default locked
        };
        // ================= BEST SCORE =================
        bestScoreUI = new BestScore();
        int score = SaveManager.getInstance().getBestScore();
        if (score >= 10000) {
            maps[1].locked = false;
        }
        if (score >= 15000) {
            maps[2].locked = false;
        }

        background = load("/images/bg1.png");
        startImg = load("/images/start.png");
        titleImg = load("/images/select-map.png");
        backImg = load("/images/back.png");

        renderBoxes = new Rectangle[maps.length];

        int startX = 50;
        int y = 130;
        int spacing = 40;

        for (int i = 0; i < maps.length; i++) {
            BufferedImage img = maps[i].idle; // ambil ukuran asli
            int width = (img != null) ? (int) (img.getWidth() * 0.55) : 220;
            int height = (img != null) ? (int) (img.getHeight() * 0.55) : 130;

            renderBoxes[i] = new Rectangle(startX, y, width, height);
            startX += width + spacing; // jarak antar map sesuai width
        }

        if (startImg != null) {
            int w = (int) (startImg.getWidth() * 0.13);
            int h = (int) (startImg.getHeight() * 0.13);
            startButton = new Rectangle(620, 530, w, h);
        } else {
            startButton = new Rectangle(620, 530, 130, 60);
        }

        if (backImg != null) {
            int w = (int) (backImg.getWidth() * 0.13);
            int h = (int) (backImg.getHeight() * 0.13);
            backButton = new Rectangle(30, 20, w, h);
        } else {
            backButton = new Rectangle(30, 20, 130, 60);
        }
    }

    private BufferedImage load(String path) {
        try {
            return ImageIO.read(getClass().getResource(path));
        } catch (Exception e) {
            System.out.println("Missing: " + path);
            return null;
        }
    }

    // ================= PUBLIC METHODS =================
    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        if (index >= 0 && index < maps.length && !maps[index].locked) {
            selectedIndex = index;
            repaint();
        }
    }

    public void moveUp() {
        selectedIndex--;
        if (selectedIndex < 0) {
            selectedIndex = maps.length - 1;
        }
        repaint();
    }

    public void moveDown() {
        selectedIndex++;
        if (selectedIndex >= maps.length) {
            selectedIndex = 0;
        }
        repaint();
    }

    public void updateHover(int x, int y) {
        hoverIndex = -1;
        startHover = startButton.contains(x, y);

        for (int i = 0; i < renderBoxes.length; i++) {
            if (renderBoxes[i].contains(x, y)) {
                hoverIndex = i;
                // jangan langsung ubah selectedIndex, biar tetap klik/enter untuk memilih
                repaint();
                return;
            }
        }

        repaint();
    }

    public int checkMouseClick(int x, int y) {

        if (backButton.contains(x, y)) {
            return -4;
        }
        if (startButton.contains(x, y)) {
            return -3; // start button
        }
        for (int i = 0; i < renderBoxes.length; i++) {
            if (renderBoxes[i].contains(x, y)) {
                if (maps[i].locked) {
                    return -2; // map terkunci
                }
                return i; // map diklik
            }
        }

        return -1; // klik di tempat kosong
    }

    public boolean isMapLocked(int index) {
        if (index < 0 || index >= maps.length) {
            return true;
        }
        return maps[index].locked;
    }
    
    public void refreshUnlock() {
        int score = SaveManager.getInstance().getBestScore();

        maps[1].locked = score < 10000;
        maps[2].locked = score < 15000;
    }

    // ================= DRAW =================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g, getWidth(), getHeight());
    }

    public void draw(Graphics g, int w, int h) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // background
        if (background != null) {
            g2.drawImage(background, 0, 0, w, h, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);
        }

        // Back
        double backZoom = backHover ? 1.1 : 1.0;
        int backW = (int) backButton.width;
        int backH = (int) backButton.height;
        int backX = backButton.x - (backW - backButton.width) / 2;
        int backY = backButton.y - (backH - backButton.height) / 2;

        if (backImg != null) {
            g2.drawImage(backImg, backX, backY, backW, backH, null);
        } else {
            g2.setColor(Color.RED.darker());
            g2.fillRoundRect(backX, backY, backW, backH, 18, 18);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            String text = "BACK";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, backX + (backW - fm.stringWidth(text)) / 2, backY + backH - 15);
        }

        // Title
        if (titleImg != null) {
            int titleWidth = (int) (titleImg.getWidth() * 0.5); // skala sesuai kebutuhan
            int titleHeight = (int) (titleImg.getHeight() * 0.5);
            int titleX = (w - titleWidth) / 2 - 20; // center horizontal
            int titleY = 10; // jarak dari atas
            g2.drawImage(titleImg, titleX, titleY, titleWidth, titleHeight, null);
        } else {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 36));
            String text = "SELECT MAP";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (w - fm.stringWidth(text)) / 2, 60); // fallback text
        }

        bestScoreUI.draw(g, 580, 20);

        // maps
        for (int i = 0; i < maps.length; i++) {
            MapItem m = maps[i];
            Rectangle b = renderBoxes[i];

            boolean active = (i == selectedIndex);
            boolean hover = (i == hoverIndex);

            BufferedImage img = m.locked ? m.lock : (active ? m.selected : m.idle);

            double zoom = hover ? 1.10 : (active ? 1.05 : 1.0);

            if (img != null) {
                double imgRatio = (double) img.getWidth() / img.getHeight();
                int drawW = (int) (b.width * zoom);
                int drawH = (int) (drawW / imgRatio);
                int x = b.x + (b.width - drawW) / 2;
                int y = b.y + (b.height - drawH) / 2;
                g2.drawImage(img, x, y, drawW, drawH, null);
            }

            // Overlay lock jika map masih terkunci
//            if (m.locked && m.lock != null) {
//                g2.drawImage(m.lock, b.x + b.width - 28, b.y + 12, 22, 22, null);
//            }
        }

        // start button
        double zoom = startHover ? 1.1 : 1.0;

        // hitung posisi dan ukuran sesuai zoom
        int drawW = (int) startButton.width;
        int drawH = (int) startButton.height;
        int drawX = startButton.x - (drawW - startButton.width) / 2;
        int drawY = startButton.y - (drawH - startButton.height) / 2;

        BufferedImage startDraw = startImg; // selalu gunakan gambar normal
        if (startDraw != null) {
            g2.drawImage(startDraw, drawX, drawY, drawW, drawH, null);
        } else {
            g2.setColor(Color.CYAN.darker());
            g2.fillRoundRect(drawX, drawY, drawW, drawH, 18, 18);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            String text = "PLAY";
            FontMetrics sfm = g2.getFontMetrics();
            g2.drawString(text,
                    drawX + (drawW - sfm.stringWidth(text)) / 2,
                    drawY + drawH - 15); // sesuaikan vertikal teks
        }
    }
}
