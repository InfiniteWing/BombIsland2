package com.infinitewing.bombisland2.GameObject;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by InfiniteWing on 2016/8/18.
 */
public class Character {
    public Bitmap source;
    public Bitmap img;
    public Player player;
    public int step_now, animationDelay;
    public int direction;
    public int source_y;
    public Map map;

    public Character(Player p) {
        player = p;
        step_now = 0;
        direction = Location.LOCATION_DOWN;
        animationDelay = 0;
        this.map = p.map;
    }

    public void InitMiniImage() {
        switch (direction) {
            case Location.LOCATION_TOP:
                source_y = 3;
                break;
            case Location.LOCATION_RIGHT:
                source_y = 2;
                break;
            case Location.LOCATION_DOWN:
                source_y = 0;
                break;
            case Location.LOCATION_LEFT:
                source_y = 1;
                break;
            default:
                source_y = 1;
                break;
        }
        img = Bitmap.createBitmap(source,
                step_now * (source.getWidth() / 3),
                source_y * (source.getHeight() / 4),
                source.getWidth() / 3,
                source.getHeight() / 4);
    }

    public void InitImage() {
        switch (direction) {
            case Location.LOCATION_TOP:
                source_y = 3;
                break;
            case Location.LOCATION_RIGHT:
                source_y = 2;
                break;
            case Location.LOCATION_DOWN:
                source_y = 0;
                break;
            case Location.LOCATION_LEFT:
                source_y = 1;
                break;
            default:
                source_y = 1;
                break;
        }
        String imageCache = source.hashCode() + "."
                + step_now + "," + source_y;
        img = map.imageCaches.get(imageCache);
        if (img == null) {
            img = Bitmap.createBitmap(source,
                    step_now * (source.getWidth() / 3),
                    source_y * (source.getHeight() / 4),
                    source.getWidth() / 3,
                    source.getHeight() / 4);
            map.imageCaches.put(imageCache, img);
        }
    }

    public void Move() {
        animationDelay++;
        if (player.speed_now > 0) {
            if (animationDelay > 500 / player.speed_now) {
                animationDelay = 0;
                step_now++;
                if (step_now > 2) {
                    step_now = 0;
                }
                InitImage();
            }
        }
    }

    public void Draw(Canvas canvas) {
        int tmp_x = player.player_x + (Common.GAME_WIDTH_UNIT - Common.MAP_WIDTH_UNIT) * Common.PLAYER_POSITION_RATE / 2;
        int tmp_y = player.player_y;
        tmp_x *= Common.gameView.screenWidth;
        tmp_y *= Common.gameView.screenHeight;
        tmp_x /= Common.PLAYER_POSITION_RATE;
        tmp_y /= Common.PLAYER_POSITION_RATE;
        Paint alphaPaint = new Paint();

        if (player.IsDead) {
            if(player.IsMount) {
                if(player.mountDeadCounter%6<=2) {
                    alphaPaint.setAlpha(100);
                    canvas.drawBitmap(img,
                            tmp_x / Common.GAME_WIDTH_UNIT,
                            tmp_y / Common.GAME_HEIGHT_UNIT,
                            alphaPaint);
                }else{
                    alphaPaint.setAlpha(42);
                    canvas.drawBitmap(img,
                            tmp_x / Common.GAME_WIDTH_UNIT,
                            tmp_y / Common.GAME_HEIGHT_UNIT,
                            alphaPaint);
                }
            }else{
                if(player.deadExplosion!=null){
                    if(player.deadExplosion.IsEnd){
                        return;
                    }
                    player.deadExplosion.Play();
                    alphaPaint.setAlpha(80);
                    canvas.drawBitmap(img,
                            tmp_x / Common.GAME_WIDTH_UNIT,
                            tmp_y / Common.GAME_HEIGHT_UNIT,
                            alphaPaint);
                    canvas.drawBitmap(player.deadExplosion.animation.img,
                            tmp_x / Common.GAME_WIDTH_UNIT,
                            tmp_y / Common.GAME_HEIGHT_UNIT,
                            alphaPaint);
                }
            }
        }else if(player.IsBubbled){
            alphaPaint.setAlpha(80);
            canvas.drawBitmap(img,
                    tmp_x / Common.GAME_WIDTH_UNIT,
                    tmp_y / Common.GAME_HEIGHT_UNIT,
                    alphaPaint);
            canvas.drawBitmap(map.imageCaches.get("bubble01.png"),
                    tmp_x / Common.GAME_WIDTH_UNIT,
                    tmp_y / Common.GAME_HEIGHT_UNIT,
                    alphaPaint);
        } else {
            canvas.drawBitmap(img,
                    tmp_x / Common.GAME_WIDTH_UNIT,
                    tmp_y / Common.GAME_HEIGHT_UNIT,
                    null);
        }
    }
}
