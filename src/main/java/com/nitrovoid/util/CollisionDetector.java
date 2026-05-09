package com.nitrovoid.util;

import com.nitrovoid.entity.Kendaraan;

public class CollisionDetector {
    public static boolean isColliding(Kendaraan a, Kendaraan b) {
        // batas kotak A
        int aLeft   = a.getX();
        int aRight  = a.getX() + a.getWidth();
        int aTop    = a.getY();
        int aBottom = a.getY() + a.getHeight();
        // batas kotak B
        int bLeft   = b.getX();
        int bRight  = b.getX() + b.getWidth();
        int bTop    = b.getY();
        int bBottom = b.getY() + b.getHeight();

        boolean tidakTabrakan = aRight  < bLeft   // A di kiri B
                             || aLeft   > bRight  // A di kanan B
                             || aBottom < bTop    // A di atas B
                             || aTop    > bBottom;// A di bawah B
        return !tidakTabrakan;
    }
}