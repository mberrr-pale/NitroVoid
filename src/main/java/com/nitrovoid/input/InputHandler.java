package com.nitrovoid.input;

import java.awt.event.*;

public class InputHandler extends KeyAdapter implements MouseListener, MouseMotionListener {
    public boolean left, right, up, down;
    public boolean nitro, slowMotion;
    public boolean pause, enter, space, restart, exitGame, backToMenu;
    public int mouseX, mouseY;
    public boolean mouseLeftPressed;

    @Override
    public void keyPressed(KeyEvent e) {
        // Gerak — Arrow keys
        if (e.getKeyCode() == KeyEvent.VK_LEFT)  left = true;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = true;
        if (e.getKeyCode() == KeyEvent.VK_UP)    up = true;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) down = true;
        // Gerak — WASD
        if (e.getKeyCode() == KeyEvent.VK_A) left = true;
        if (e.getKeyCode() == KeyEvent.VK_D) right = true;
        if (e.getKeyCode() == KeyEvent.VK_W) up = true;
        // Aksi
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) nitro = true;
        if (e.getKeyCode() == KeyEvent.VK_C)     slowMotion = true;
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) pause = true;
        if (e.getKeyCode() == KeyEvent.VK_ENTER) enter = true;
        if (e.getKeyCode() == KeyEvent.VK_SPACE) space = true;
        if (e.getKeyCode() == KeyEvent.VK_R)     restart = true;
        if (e.getKeyCode() == KeyEvent.VK_Q)     exitGame = true;
        if (e.getKeyCode() == KeyEvent.VK_B)    backToMenu = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Gerak — Arrow keys
        if (e.getKeyCode() == KeyEvent.VK_LEFT)  left = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = false;
        if (e.getKeyCode() == KeyEvent.VK_UP)    up = false;
        // Gerak — WASD
        if (e.getKeyCode() == KeyEvent.VK_A) left = false;
        if (e.getKeyCode() == KeyEvent.VK_D) right = false;
        if (e.getKeyCode() == KeyEvent.VK_W) up = false;
        // Aksi
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) nitro = false;
        if (e.getKeyCode() == KeyEvent.VK_C)     slowMotion = false;
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) pause = false;
        if (e.getKeyCode() == KeyEvent.VK_ENTER) enter = false;
        if (e.getKeyCode() == KeyEvent.VK_SPACE) space = false;
        if (e.getKeyCode() == KeyEvent.VK_R)     restart = false;
        if (e.getKeyCode() == KeyEvent.VK_Q)     exitGame = false;
        if (e.getKeyCode() == KeyEvent.VK_B)    backToMenu = false;
    }
    
    // ----- MOUSE -----
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) mouseLeftPressed = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) mouseLeftPressed = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }
    
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}