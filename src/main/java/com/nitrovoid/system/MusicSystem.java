package com.nitrovoid.system;

import javax.sound.sampled.*;
import java.net.URL;

public class MusicSystem {

    // =======================
    // BGM
    // =======================
    private Clip bgmClip;
    private String currentBgmPath;

    // =======================
    // GLOBAL STATE
    // =======================
    private boolean volumeOn = true;

    // =======================
    // PLAY BGM LOOP
    // =======================
    public void playLoop(String path) {
        stopBGM(); // stop existing BGM

        currentBgmPath = path;

        try {
            URL url = getClass().getResource(path);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);

            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);

            if (volumeOn) {
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgmClip.start();
            }
            // Jika volume off, jangan start, tapi clip siap dimainkan saat unmute

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
            return; // tidak perlu toggle jika sama
        }
        volumeOn = on;

        if (volumeOn) {
            // jika BGM ada dan mute sebelumnya, mulai lagi
            if (bgmClip != null) {
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgmClip.start();
            }
        } else {
            // mute instan dengan stop clip
            if (bgmClip != null) {
                bgmClip.stop();
            }
        }
    }

    public boolean isVolumeOn() {
        return volumeOn;
    }

    // =======================
    // TOGGLE VOLUME
    // =======================
    public void toggleVolume() {
        setVolume(!volumeOn);
    }

    // =======================
    // STATUS
    // =======================
    public boolean isPlaying() {
        return bgmClip != null && bgmClip.isActive();
    }
}
