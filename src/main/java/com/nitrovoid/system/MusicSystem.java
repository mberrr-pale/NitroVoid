package com.nitrovoid.system;

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class MusicSystem {

    private Clip bgmClip;
    private String currentBgmPath;
    private long bgmPosition = 0;

    // =======================
    // GLOBAL STATE
    // =======================
    private boolean volumeOn = true;
    private boolean bgmPaused = false;
    private boolean bgmPausedByGame = false; // TRUE jika pause oleh pauseScreen/game
    private boolean bgmNeverPlayed = true;   // TRUE jika BGM belum pernah dimainkan

    // =======================
    // PLAY BGM LOOP
    // =======================
    public void playLoop(String path) {
        stopBGM();
        currentBgmPath = path;
        bgmPosition = 0;
        bgmPaused = false;
        bgmPausedByGame = false;
        bgmNeverPlayed = true;

        try {
            URL url = getClass().getResource(path);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);

            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);

            if (volumeOn) {
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgmClip.start();
                bgmNeverPlayed = false;
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
        }
        bgmPosition = 0;
        bgmPaused = false;
        bgmPausedByGame = false;
        bgmNeverPlayed = true;
    }

    // =======================
    // PAUSE / RESUME BGM
    // =======================
    public void pauseBGM() {
        if (bgmClip != null && bgmClip.isActive()) {
            bgmPosition = bgmClip.getMicrosecondPosition();
            bgmClip.stop();
            bgmPaused = true;
            bgmPausedByGame = true;
        }
    }

    public void resumeAfterPause() {
        if (bgmClip == null) {
            return; // tidak ada BGM
        }
        if (volumeOn) {
            if (bgmPaused || bgmNeverPlayed) {
                // Kalau sedang pause atau belum pernah play
                bgmClip.setMicrosecondPosition(bgmPosition);
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgmClip.start();
                bgmPaused = false;
                bgmPausedByGame = false;
                bgmNeverPlayed = false;
            }
        } else {
            // volume OFF → tetap pause tapi update flag
            bgmPaused = true;
            bgmPausedByGame = true;
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
        if (volumeOn == on) {
            return;
        }
        volumeOn = on;

        if (bgmClip == null) {
            return;
        }

        if (volumeOn) {
            if (!bgmPausedByGame && !bgmClip.isActive() && !bgmNeverPlayed) {
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgmClip.start();
                bgmPaused = false;
            }
        } else {
            if (bgmClip.isActive()) {
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
