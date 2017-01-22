package com.infinitewing.bombisland2.GameObject.Parser;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.infinitewing.bombisland2.GameObject.Common;
import com.infinitewing.bombisland2.GameObject.Explosion;
import com.infinitewing.bombisland2.GameObject.MapObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Administrator on 2016/8/25.
 */
public class MapObjectParser {
    public MapObject mapObject;

    public MapObjectParser(MapObject mapObject) {
        this.mapObject = mapObject;
    }

    public void Parse() {
        String path = "xml/map/" + mapObject.id + ".txt";
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
                mapObject.imgName = value;
            }
            if (tag.equals("Add_Speed")) {
                mapObject.addSpeed = Integer.parseInt(value);
            }
            if (tag.equals("Add_Power")) {
                mapObject.addPower = Integer.parseInt(value);
            }
            if (tag.equals("Add_Wb")) {
                mapObject.addWaterBall = Integer.parseInt(value);
            }
            if (tag.equals("Add_Money")) {
                mapObject.addMoney = Integer.parseInt(value);
            }
            if (tag.equals("Mount")) {
                mapObject.mountID = value;
            }
            if (tag.equals("Loop")) {
                if (value.equals("true")) {
                    mapObject.animation.loop = true;
                } else {
                    mapObject.animation.loop = false;
                }
            }
            if (tag.equals("SWU")) {
                mapObject.animation.source_width_unit = Integer.parseInt(value);
            }
            if (tag.equals("SHU")) {
                mapObject.animation.source_height_unit = Integer.parseInt(value);
            }
            if (tag.equals("SW")) {
                mapObject.animation.source_width = Integer.parseInt(value);
            }
            if (tag.equals("SH")) {
                mapObject.animation.source_height = Integer.parseInt(value);
            }
            if (tag.equals("SX")) {
                mapObject.animation.source_x = Integer.parseInt(value);
            }
            if (tag.equals("SY")) {
                mapObject.animation.source_y = Integer.parseInt(value);
            }
            if (tag.equals("WU")) {
                mapObject.animation.width_unit = Integer.parseInt(value);
            }
            if (tag.equals("HU")) {
                mapObject.animation.height_unit = Integer.parseInt(value);
            }
            if (tag.equals("Step")) {
                mapObject.animation.step = Integer.parseInt(value);
            }
            if (tag.equals("Total_Step")) {
                mapObject.animation.total_step = Integer.parseInt(value);
            }
            if (tag.equals("MPF")) {
                mapObject.animation.ms_per_frame = Common.GAME_REFRESH * Integer.parseInt(value);
            }

            if (tag.equals("Loop_Frame")) {
                mapObject.animation.loop_frame = Integer.parseInt(value);
            }
            if (tag.equals("Loop_Step")) {
                mapObject.animation.loop_step = Integer.parseInt(value);
            }
            if (tag.equals("Loop_Total")) {
                mapObject.animation.loop_total = Integer.parseInt(value);
            }

            //center_obj專有
            if (tag.equals("Obstacle")) {
                int x = mapObject.location.x + Integer.parseInt(value.split(",")[0]);
                int y = mapObject.location.y + Integer.parseInt(value.split(",")[1]);
                mapObject.map.mapObstacles[x][y] = 1;
            }
            if (tag.equals("OWU")) {
                mapObject.animation.offset_width_unit = Integer.parseInt(value);
            }
            if (tag.equals("OHU")) {
                mapObject.animation.offset_height_unit = Integer.parseInt(value);
            }


        } while (line != null);

        if (mapObject.map.imageCaches.get(mapObject.imgName) == null) {
            Bitmap source;
            if (mapObject.type == MapObject.TYPE_ITEM) {
                source = Common.getBitmapFromAsset("item/" + mapObject.imgName);
            } else {
                source = Common.getBitmapFromAsset("map/" + mapObject.imgName);
            }
            try {
                Matrix matrix = new Matrix();
                matrix.reset();
                float width = mapObject.animation.source_width_unit * Common.gameView.screenWidth / Common.GAME_WIDTH_UNIT;
                float height = mapObject.animation.source_height_unit * Common.gameView.screenHeight / Common.GAME_HEIGHT_UNIT;
                matrix.postScale(width / source.getWidth(), height / source.getHeight());
                source = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
                mapObject.map.imageCaches.put(mapObject.imgName, source);
            } catch (Exception e) {
                e.getCause();
            }
        }
        mapObject.animation.source = mapObject.map.imageCaches.get(mapObject.imgName);
        mapObject.animation.Init();
    }
}
