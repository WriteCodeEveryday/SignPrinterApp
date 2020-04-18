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
import android.text.TextUtils;

import java.lang.reflect.Array;
import java.util.Arrays;

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
        Bitmap rotatedBitmap = bitmap.copy(bitmapConfig, true);

        boolean rotated = bitmap.getHeight() > bitmap.getWidth();
        int textCodeDimension = Math.max(bitmap.getWidth(), bitmap.getHeight());

        if (rotated) {
            // Matrixes for rotations.
            Matrix rotate = new Matrix();
            rotate.postRotate(90);

            rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotate, true);
        }

        //Paints for text and background
        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (PrinterManager.getMode().equals("roll") && PrinterManager.getModel().contains("820")) {
            text.setColor(Color.RED);
        } else {
            text.setColor(Color.BLACK);
        }
        text.setTextSize((int) textCodeDimension / 16);

        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setStyle(Paint.Style.FILL);
        bg.setColor(Color.WHITE);

        String[] outputs = item.getPrintables();
        Canvas canvas = new Canvas(rotatedBitmap);


        // draw text to the Canvas center
        Rect bounds = new Rect();
        if (outputs[2].length() < 20) {
            text.getTextBounds(outputs[1], 0, outputs[1].length(), bounds);
            int x = (rotatedBitmap.getWidth() - bounds.width())/6;
            int y = (rotatedBitmap.getHeight() + bounds.height())/10;

            canvas.drawRect(generateBackground(bounds, x, y, scale), bg);
            canvas.drawText(outputs[1], x * scale, y * scale, text);

            // draw text slight above Canvas center
            text.setTextSize((int) textCodeDimension / 14);

            text.getTextBounds(outputs[2], 0, outputs[2].length(), bounds);
            x = (rotatedBitmap.getWidth() - bounds.width())/6;
            y = (rotatedBitmap.getHeight() + bounds.height())/5;

            canvas.drawRect(generateBackground(bounds, x, y, scale), bg);
            canvas.drawText(outputs[2], x * scale, y * scale, text);
        } else {
            text.getTextBounds(outputs[1], 0, outputs[1].length(), bounds);
            int x = (rotatedBitmap.getWidth() - bounds.width())/6;
            int y = (rotatedBitmap.getHeight() + bounds.height())/15;

            canvas.drawRect(generateBackground(bounds, x, y, scale), bg);
            canvas.drawText(outputs[1], x * scale, y * scale, text);

            // draw text slight above Canvas center
            text.setTextSize((int) textCodeDimension / 14);

            int initX = 6;
            float initY = 6f;

            String[] values = outputs[2].split(" ");
            String[] out = new String[] { "FIRST", "SECOND"};
            out[0] = TextUtils.join(" ", Arrays.copyOfRange(values, 0, values.length / 2));
            out[1] = TextUtils.join(" " , Arrays.copyOfRange(values, values.length / 2, values.length));

            int ySizing = 0;
            for (int i = 0; i < out.length; i++) {
                text.getTextBounds(out[i], 0, out[i].length(), bounds);
                x = (rotatedBitmap.getWidth() - bounds.width())/initX;
                y = (int) ((rotatedBitmap.getHeight() + bounds.height())/initY);

                canvas.drawRect(generateBackground(bounds, x, y, scale), bg);
                canvas.drawText(out[i], x * scale, y * scale, text);
                initY -= 2;
            }
        }


        if (rotated) {
            Matrix counter = new Matrix();
            counter.postRotate(270);
            rotatedBitmap = Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), counter, true);
        }
        return  rotatedBitmap;
    }

    private Rect generateBackground(Rect bounds, int x, int y, int scale) {
        Rect background = new Rect(bounds.left, bounds.top, bounds.right, bounds.bottom);
        background.left += x * scale;
        background.right += x * scale;
        background.top += y * scale;
        background.bottom += y * scale;

        int xSize = background.left - background.right;
        int ySize = background.top - background.bottom;
        background.left += xSize * 0.1;
        background.right -= xSize * 0.1;
        background.top += ySize * 0.3;
        background.bottom -= ySize * 0.3;
        return background;
    }
}
