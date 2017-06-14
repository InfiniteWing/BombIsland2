package com.infinitewing.bombisland2.GameObject;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

/**
 * Created by InfiniteWing on 2016/8/18.
 */
public class Explosion {
    public Animation animation;
    public int delay;
    public Location location;
    public Boolean IsEnd;
    public Map map;

    public Explosion(Location location, MapObject bomb) {
        this.location = location;
        this.map = Common.gameView.map;
        IsEnd = false;
        String imgName = "bomb.png";
        animation = new Animation(map);
        animation.loop = false;
        animation.source_width_unit = 6;
        animation.source_height_unit = 10;
        animation.source_x = 0;
        animation.source_y = 5;
        if (bomb != null) {
            animation.source_y += bomb.animation.source_y;
        }
        animation.step = 1;
        animation.total_step = 6;
        animation.ms_per_frame = Common.GAME_REFRESH * 1;
        animation.width_unit = 1;
        animation.height_unit = 1;
        if (map.imageCaches.get(imgName) == null) {
            Bitmap source = Common.getBitmapFromAsset("map/" + imgName);
            Matrix matrix = new Matrix();
            matrix.reset();
            float width = animation.source_width_unit * Common.gameView.screenWidth / Common.GAME_WIDTH_UNIT;
            float height = animation.source_height_unit * Common.gameView.screenHeight / Common.GAME_HEIGHT_UNIT;
            matrix.postScale(width / source.getWidth(), height / source.getHeight());
            source = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
            map.imageCaches.put(imgName, source);
        }
        animation.source = map.imageCaches.get(imgName);
        animation.Init();
    }

    public void Play() {
        if (!IsEnd) {
            if (delay <= 0) {
                animation.Play();
                if (animation.IsEnd() && !animation.loop) {
                    map.explosionLocations.add(location);
                    IsEnd = true;
                }
                map.explosionDP[location.x][location.y] = 1;
            } else {
                map.willExplosionDP[location.x][location.y] = 1;
            }
            delay -= Common.GAME_REFRESH;
        }
    }

    public void Draw(Canvas canvas) {
        if (!IsEnd && delay <= 0) {
            if (animation.img == null) {
                animation.InitImage();
            }
            canvas.drawBitmap(animation.img,
                    (Common.gameView.screenWidth * (location.x + (Common.GAME_WIDTH_UNIT - Common.MAP_WIDTH_UNIT) / 2)) / Common.GAME_WIDTH_UNIT,
                    (Common.gameView.screenHeight * location.y) / Common.GAME_HEIGHT_UNIT,
                    null);
        }
    }
}
