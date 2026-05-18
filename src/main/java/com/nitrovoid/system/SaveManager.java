package com.nitrovoid.system;

import java.io.*;
import java.util.Properties;

public class SaveManager {

    private static SaveManager instance;

    private static final String FILE_NAME =
            "nitrovoid_save.properties";

    private Properties props = new Properties();
    private File saveFile;

    private SaveManager() {}

    public static SaveManager getInstance() {

        if (instance == null) {
            instance = new SaveManager();
        }

        return instance;
    }
    // ── Init — dipanggil saat game pertama kali dibuka ────────────
    public void init() {
        saveFile = new File(FILE_NAME);

        if (saveFile.exists()) {
            // File sudah ada → load saja, jangan buat ulang
            load();
            System.out.println("Save file ditemukan, best score: " + getBestScore());
        } else {
            // File belum ada → buat baru dengan nilai default
            props.setProperty("device.os",       System.getProperty("os.name") 
                                                + " " + System.getProperty("os.version"));
            props.setProperty("device.java",     System.getProperty("java.version"));
            props.setProperty("device.username", System.getProperty("user.name"));
            props.setProperty("best.score",      "0");
            save();
            System.out.println("Save file dibuat baru.");
        }
    }

    // ── Update best score — dipanggil saat GAMEOVER ───────────────
    public void updateBestScore(int newScore) {
        int current = getBestScore();
        if (newScore > current) {
            props.setProperty("best.score", String.valueOf(newScore));
            save();
            System.out.println("Best score diperbarui: " + newScore);
        }
    }

    // ── Read best score — dipanggil saat tampil di menu/HUD ───────
    public int getBestScore() {
        String val = props.getProperty("best.score", "0");
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0; // fallback kalau file korup
        }
    }

    // ── Read device info ──────────────────────────────────────────
    public String getDeviceOS()       { return props.getProperty("device.os", "-"); }
    public String getDeviceJava()     { return props.getProperty("device.java", "-"); }
    public String getDeviceUsername() { return props.getProperty("device.username", "-"); }

    // ── Internal: load dari file ──────────────────────────────────
    private void load() {
        try (FileInputStream fis = new FileInputStream(saveFile)) {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("Gagal load save file: " + e.getMessage());
        }
    }

    // ── Internal: tulis ke file ───────────────────────────────────
    private void save() {
        try (FileOutputStream fos = new FileOutputStream(saveFile)) {
            props.store(fos, "NitroVoid Save File");
        } catch (IOException e) {
            System.out.println("Gagal menyimpan save file: " + e.getMessage());
        }
    }
}