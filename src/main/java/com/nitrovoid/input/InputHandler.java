package com.nitrovoid.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputHandler extends KeyAdapter {
    public boolean left, right, up;
    public boolean nitro, slowMotion;
    public boolean pause, enter, space, restart, exitGame, backToMenu; 

    @Override
    public void keyPressed(KeyEvent e) {
        // Gerak — Arrow keys
        if (e.getKeyCode() == KeyEvent.VK_LEFT)  left = true;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = true;
        if (e.getKeyCode() == KeyEvent.VK_UP)    up = true;
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
}