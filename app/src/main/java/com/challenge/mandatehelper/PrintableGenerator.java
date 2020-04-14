package com.challenge.mandatehelper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

public class PrintableGenerator {
    private String file = "";
    public PrintableGenerator(String printerModel, String printerMode) {
        file = PrinterManager.dashToLower(printerModel.toLowerCase()) + "_" + printerMode;
    }

    public Bitmap buildOutput(PrintableItem item, Context ctx) {
        Resources resources = ctx.getResources();
        int scale = (int) resources.getDisplayMetrics().density;
        int resource = ctx.getResources().getIdentifier(file, "drawable", ctx.getPackageName());
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resource);
        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        // Matrixes for rotations.
        Matrix rotate = new Matrix();
        rotate.postRotate(90);

        Matrix counter = new Matrix();
        counter.postRotate(270);

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotate, true);

        //Paints for text and background
        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (PrinterManager.getMode().equals("roll")) {
            text.setColor(Color.RED);
        } else {
            text.setColor(Color.BLACK);
        }
        text.setTextSize((int) bitmap.getHeight() / 20);

        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setStyle(Paint.Style.FILL);
        bg.setColor(Color.WHITE);

        String[] outputs = item.getPrintables();
        Canvas canvas = new Canvas(rotatedBitmap);


        // draw text to the Canvas center
        Rect bounds = new Rect();
        text.getTextBounds(outputs[1], 0, outputs[1].length(), bounds);
        int x = (rotatedBitmap.getWidth() - bounds.width())/6;
        int y = (rotatedBitmap.getHeight() + bounds.height())/10;

        canvas.drawRect(generateBackground(bounds, x, y, scale), bg);
        canvas.drawText(outputs[1], x * scale, y * scale, text);

        // draw text slight above Canvas center
        text.setTextSize((int) bitmap.getHeight() / 16);
        text.getTextBounds(outputs[2], 0, outputs[2].length(), bounds);
        x = (rotatedBitmap.getWidth() - bounds.width())/6;
        y = (rotatedBitmap.getHeight() + bounds.height())/5;

        canvas.drawRect(generateBackground(bounds, x, y, scale), bg);
        canvas.drawText(outputs[2], x * scale, y * scale, text);

        return Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), counter, true);
    }

    private Rect generateBackground(Rect bounds, int x, int y, int scale) {
        Rect background = new Rect(bounds.left, bounds.top, bounds.right, bounds.bottom);
        background.left += x * scale * 0.90;
        background.top += y * scale * 0.90;
        background.right += x * scale * 1.10;
        background.bottom += y * scale * 1.10;
        return background;
    }
}
