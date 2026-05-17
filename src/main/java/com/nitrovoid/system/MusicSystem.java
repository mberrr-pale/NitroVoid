package com.nitrovoid.system;

import javax.sound.sampled.*;
import java.net.URL;

public class MusicSystem {

    // =======================
    // BGM
    // =======================
    private Clip bgmClip;
    private String currentBgmPath;
    private long bgmPosition = 0; // remember current frame for pause

    // =======================
    // GLOBAL STATE
    // =======================
    private boolean volumeOn = true;
    private boolean bgmPaused = false;

    // =======================
    // PLAY BGM LOOP
    // =======================
    public void playLoop(String path) {
        stopBGM(); // stop existing BGM
        currentBgmPath = path;
        bgmPosition = 0;
        bgmPaused = false;

        try {
            URL url = getClass().getResource(path);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);

            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);

            if (volumeOn) {
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgmClip.start();
            }
        } catch (Exception e) {
            System.out.println("BGM error: " + e.getMessage());
        }
    }

    // =======================
    // STOP BGM
    // =======================
    public void stopBGM() {
        if (bgmClip != null) {
            bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
            bgmPosition = 0;
            bgmPaused = false;
        }
    }

    // =======================
    // PAUSE / RESUME BGM
    // =======================
    public void pauseBGM() {
        if (bgmClip != null && bgmClip.isActive()) {
            bgmPosition = bgmClip.getMicrosecondPosition();
            bgmClip.stop();
            bgmPaused = true;
        }
    }

    public void resumeBGM() {
        if (bgmClip != null && bgmPaused && volumeOn) {
            bgmClip.setMicrosecondPosition(bgmPosition);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
            bgmPaused = false;
        }
    }

    // =======================
    // PLAY SFX
    // =======================
    public void playSFX(String path) {
        if (!volumeOn) {
            return;
        }

        try {
            URL url = getClass().getResource(path);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);

            Clip sfxClip = AudioSystem.getClip();
            sfxClip.open(audioStream);
            sfxClip.start();

        } catch (Exception e) {
            System.out.println("SFX error: " + e.getMessage());
        }
    }

    // =======================
    // VOLUME CONTROL
    // =======================
    public void setVolume(boolean on) {
        if (volumeOn == on) return;
        volumeOn = on;

        if (volumeOn) {
            if (bgmClip != null && !bgmPaused) {
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgmClip.start();
            }
        } else {
            if (bgmClip != null) {
                bgmClip.stop();
            }
        }
    }

    public boolean isVolumeOn() {
        return volumeOn;
    }

    public void toggleVolume() {
        setVolume(!volumeOn);
    }

    // =======================
    // STATUS
    // =======================
    public boolean isPlaying() {
        return bgmClip != null && bgmClip.isActive();
    }

    public boolean isPaused() {
        return bgmPaused;
    }
}