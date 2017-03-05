package com.infinitewing.bombisland2.GameObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.infinitewing.bombisland2.GameView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Created by InfiniteWing on 2016/8/18.
 */
public class Common {
    public static final int GAME_REFRESH=40,GAME_BT_REFRESH=3,GAME_WIDTH_UNIT=20,GAME_HEIGHT_UNIT=12,MAP_WIDTH_UNIT=16,MAP_HEIGHT_UNIT=12;
    public static GameView gameView;
    public static int SCREEN_WIDTH,SCREEN_HEIGHT,PLAYER_POSITION_RATE=1000;
    public static String APP_UUID = "00001101-0000-1000-8000-00805f9b34fb",APP_NAME="BombIsland2";
    public static void SetGameView(GameView gameView2) {
        gameView = gameView2;
    }
    public static final int RandomNum(int s){
        Random random=new Random();
        return random.nextInt(s)+1;
    }
    public static float transWidth(float w){
        return w*Common.gameView.screenWidth/1280;
    }
    public static float transHeight(float h){
        return h*Common.gameView.screenHeight/768;
    }
    public static float transFontSize(float s){
        return s*Common.gameView.screenWidth/1280;
    }
    public static InputStream getInputStream(String path, Context context) throws IOException {
        InputStream is = null;
        AssetManager am = context.getAssets();
        try {
            is = am.open(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return is;
    }
    public static AssetFileDescriptor getAssetsFileDescripter(String path) {
        AssetManager am = gameView.getContext().getAssets();
        try {
            return am.openFd(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
    public static Bitmap getBitmapFromAsset(String str)
    {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inPreferredConfig = Bitmap.Config.ARGB_8888;
        if(gameView!=null) {
            SharedPreferences sp = gameView.getContext().getSharedPreferences(Common.APP_NAME, gameView.getContext().MODE_PRIVATE);
            if (sp.getBoolean("bitmapOpt", false)) {
                 op.inPreferredConfig = Bitmap.Config.ARGB_4444;
            }
        }
        AssetManager assetManager = gameView.getContext().getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream,null,op);
        return bitmap;
    }
    public static Bitmap getBitmapFromAsset(String str,Context c)
    {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inPreferredConfig = Bitmap.Config.ARGB_8888;
        if(gameView!=null) {
            SharedPreferences sp = gameView.getContext().getSharedPreferences(Common.APP_NAME, gameView.getContext().MODE_PRIVATE);
            if (sp.getBoolean("bitmapOpt", false)) {
                op.inPreferredConfig = Bitmap.Config.ARGB_4444;
            }
        }
        AssetManager assetManager = c.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream,null,op);
        return bitmap;
    }
}
