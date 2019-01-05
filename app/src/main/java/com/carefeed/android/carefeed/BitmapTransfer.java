package com.carefeed.android.carefeed;
import android.graphics.Bitmap;

public class BitmapTransfer {
    public static Bitmap bitmap = null;

    public static Bitmap getBitmap() {
        return bitmap;
    }

    public static void setBitmap(Bitmap bitmap) {
        BitmapTransfer.bitmap = bitmap;
    }
}
