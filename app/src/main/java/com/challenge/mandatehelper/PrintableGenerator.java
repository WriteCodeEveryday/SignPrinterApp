package com.challenge.mandatehelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;

public class PrintableGenerator {
    private String file = "";

    public static HashMap<String, Typeface> fonts = new HashMap<String, Typeface>();
    public static int fontIndex = -1;
    public static String fontName;
    public static Typeface font;

    public PrintableGenerator(Context ctx) {
        build();

        loadFonts(ctx, "fonts/");
        loadFontSettings(ctx);
    }

    private static boolean loadFonts(Context ctx, String path) {
        String[] list;
        try {
            list = ctx.getAssets().list(path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (list.length > 0) {
            // This is a folder
            for (String file : list) {
                if (!loadFonts(ctx,path + "/" + file))
                    return false;
                else {
                    String name = file.split("\\.")[0];
                    name = name.replace("_", " ");
                    addFont(ctx, name, file);
                }
            }
        }

        return true;
    }

    private static void loadFontSettings(Context ctx) {
        SharedPreferences prefs = ctx
                .getSharedPreferences("font_settings", Context.MODE_PRIVATE);
        String name = prefs.getString("font", null);
        if (fonts.containsKey(name)) {
            fontName = name;
            font = fonts.get(name);
        }
    }

    public static void loadNextFont(Context ctx) {
        fontIndex++;
        if (fontIndex >= fonts.size()) {
            fontIndex = 0;
        }
        fontName = fonts.keySet().toArray(new String[1])[fontIndex];
        font = fonts.get(fontName);
        saveFontSettings(ctx);
    }

    private static void saveFontSettings(Context ctx) {
        SharedPreferences prefs = ctx
                .getSharedPreferences("font_settings",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String name = fontName;
        editor.putString("font", name);
        editor.commit();
    }

    private static void addFont(Context ctx, String name, String file) {
        fonts.put(name, Typeface.createFromAsset(ctx.getAssets(), "fonts/" + file));
    }

    private void build() {
        String printerModel = PrinterManager.getModel();
        String printerMode = PrinterManager.getMode();
        if (printerMode != null && printerModel != null) {
            file = PrinterManager.dashToLower(PrinterManager.getModel().toLowerCase()) + "_" + PrinterManager.getMode();
        } else if (printerModel != null) {
            file = PrinterManager.dashToLower(PrinterManager.getModel().toLowerCase()) + "_label";
        } else {
            file = PrinterManager.dashToLower(PrinterManager.getSupportedModels()[0].toLowerCase()) + "_label";
        }
    }

    public Bitmap buildOutput(PrintableItem item, Context ctx) {
        Resources resources = ctx.getResources();
        float scale = 3;//resources.getDisplayMetrics().density;
        int resource = ctx.getResources().getIdentifier(file, "drawable", ctx.getPackageName());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resource, options);
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
        if (PrinterManager.getMode() != null &&
                PrinterManager.getMode().equals("roll") &&
                PrinterManager.getModel() != null &&
                PrinterManager.getModel().contains("820")) {
            text.setColor(Color.RED);
        } else {
            text.setColor(Color.BLACK);
        }
        text.setTextSize((int) textCodeDimension / 16);
        if (fontName != null) {
            text.setTypeface(font);
        }

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

            System.out.println("First Line: " + x + ":" + y);

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

                System.out.println("Other Lines: " + x + ":" + y);
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

    private Rect generateBackground(Rect bounds, int x, int y, float scale) {
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
