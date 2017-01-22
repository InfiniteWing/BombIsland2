package com.infinitewing.bombisland2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.infinitewing.bombisland2.GameObject.Common;

/**
 * Created by Administrator on 2016/8/19.
 */
public class GameMain extends Activity {
    public GameListener gamelistener;
    public GameView gameView;
    public MediaPlayer gamebackgroundsound;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        gamebackgroundsound=new MediaPlayer();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent intent=this.getIntent();
        setScreenSize(getApplicationContext());
        gameView=new GameView(this,intent.getStringExtra("hero"),
                intent.getStringExtra("map"),intent.getIntExtra("ai_count",3),intent.getStringExtra("ai_info"),gamebackgroundsound);
        setContentView(gameView);
        gamelistener = new GameListener();
        IntentFilter filter = new IntentFilter();
        filter.addAction("Result");
        registerReceiver(gamelistener, filter);
    }
    public void setScreenSize(Context context) {
        int x, y, orientation = context.getResources().getConfiguration().orientation;
        WindowManager wm = ((WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point screenSize = new Point();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealSize(screenSize);
                x = screenSize.x;
                y = screenSize.y;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    display.getSize(screenSize);
                }
                x = screenSize.x;
                y = screenSize.y;
            }
        } else {
            x = display.getWidth();
            y = display.getHeight();
        }

        int width = x;
        int height = y;
        Common.SCREEN_WIDTH=width;
        Common.SCREEN_HEIGHT=height;
    }
    @Override
    protected void onDestroy() {
        GameMain.this.finish();
        gamebackgroundsound.stop();
        super.onDestroy();
    }

    private class GameListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            GameMain.this.finish();
        }
    }
}
