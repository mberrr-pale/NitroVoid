package com.nitrovoid.util;

import com.nitrovoid.entity.Kendaraan;

public class CollisionDetector {

    //Collision standar tanpa toleransi
    public static boolean isColliding(Kendaraan a, Kendaraan b) {
        return isColliding(a, b, 0);
    }

//    Collision dengan toleransi hitbox.
//    tolerance > 0 → hitbox sedikit lebih kecil dari ukuran visual (lebih fair).
//    Gunakan nilai positif, misal 5 untuk shrink 5px tiap sisi.
    public static boolean isColliding(Kendaraan a, Kendaraan b, int tolerance) {
        int aLeft   = a.getX()              + tolerance;
        int aRight  = a.getX() + a.getWidth()  - tolerance;
        int aTop    = a.getY()              + tolerance;
        int aBottom = a.getY() + a.getHeight() - tolerance;

        int bLeft   = b.getX()              + tolerance;
        int bRight  = b.getX() + b.getWidth()  - tolerance;
        int bTop    = b.getY()              + tolerance;
        int bBottom = b.getY() + b.getHeight() - tolerance;

        boolean tidakTabrakan = aRight  < bLeft
                             || aLeft   > bRight
                             || aBottom < bTop
                             || aTop    > bBottom;
        return !tidakTabrakan;
    }
}