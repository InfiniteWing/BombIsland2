package com.infinitewing.bombisland2.GameObject.Parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.DisplayMetrics;

import com.infinitewing.bombisland2.GameObject.Common;
import com.infinitewing.bombisland2.GameObject.MapObject;
import com.infinitewing.bombisland2.GameObject.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Administrator on 2016/8/25.
 */
public class HeroParser {
    public Player player;

    public HeroParser(Player player) {
        this.player = player;
    }

    public void ParseInfo(Context c) {
        String path = "xml/hero/" + player.id + ".txt";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(c.getAssets().open(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line = null;
        do {
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line == null) {
                break;
            }
            if (line.length() < 2) {
                continue;
            }
            String tag = line.split(":")[0], value = line.split(":")[1];
            if (tag.equals("Image_Name")) {
                player.imgName = value;
            }
            if (tag.equals("Name")) {
                player.heroName = value;
                player.heroName = Common.getStringResourceByName("hero_name_"+player.id,c);
            }
            if (tag.equals("Speed")) {
                player.speed = Integer.parseInt(value);
            }
            if (tag.equals("Speed_Max")) {
                player.speed_max = Integer.parseInt(value);
            }
            if (tag.equals("Wb")) {
                player.wb = Integer.parseInt(value);
                player.wb_now = Integer.parseInt(value);
            }
            if (tag.equals("Wb_Max")) {
                player.wb_max = Integer.parseInt(value);
            }
            if (tag.equals("Power")) {
                player.wb_power = Integer.parseInt(value);
            }
            if (tag.equals("Power_Max")) {
                player.wb_power_max = Integer.parseInt(value);
            }
        } while (line != null);

        Bitmap source = Common.getBitmapFromAsset("hero/" + player.imgName, c);
        Matrix matrix = new Matrix();
        matrix.reset();
        DisplayMetrics dm = c.getResources().getDisplayMetrics();
        float width = (float) (2.5 * dm.widthPixels / Common.GAME_WIDTH_UNIT);
        float height = (float) (2.5 * dm.heightPixels / Common.GAME_HEIGHT_UNIT);
        matrix.postScale(width / source.getWidth(), height / source.getHeight());
        source = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
        player.character.source = source;
        player.character.InitMiniImage();
    }

    public void Parse() {
        String path = "xml/hero/" + player.id + ".txt";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(Common.gameView.getContext().getAssets().open(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line = null;
        do {
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line == null) {
                break;
            }
            if (line.length() < 2) {
                continue;
            }
            String tag = line.split(":")[0], value = line.split(":")[1];

            if (tag.equals("Image_Name")) {
                player.imgName = value;
            }
            if (tag.equals("Name")) {
                player.heroName = value;
            }
            if (tag.equals("DeadSound")) {
                player.deadSound = value;
            }
            if (tag.equals("Speed")) {
                player.speed = Integer.parseInt(value);
            }
            if (tag.equals("Speed_Max")) {
                player.speed_max = Integer.parseInt(value);
            }
            if (tag.equals("Wb")) {
                player.wb = Integer.parseInt(value);
                player.wb_now = Integer.parseInt(value);
            }
            if (tag.equals("Wb_Max")) {
                player.wb_max = Integer.parseInt(value);
            }
            if (tag.equals("Power")) {
                player.wb_power = Integer.parseInt(value);
            }
            if (tag.equals("Power_Max")) {
                player.wb_power_max = Integer.parseInt(value);
            }


        } while (line != null);


        if (player.map.imageCaches.get(player.id) == null) {
            Bitmap source = Common.getBitmapFromAsset("hero/" + player.imgName);
            Matrix matrix = new Matrix();
            matrix.reset();
            float width = 3 * Common.gameView.screenWidth / Common.GAME_WIDTH_UNIT;
            float height = 4 * Common.gameView.screenHeight / Common.GAME_HEIGHT_UNIT;
            matrix.postScale(width / source.getWidth(), height / source.getHeight());
            source = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
            player.map.imageCaches.put(player.id, source);
        }
        player.character.source = player.map.imageCaches.get(player.id);
        player.character.InitImage();
    }
}
