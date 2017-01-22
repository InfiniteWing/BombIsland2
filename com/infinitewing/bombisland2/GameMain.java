package com.infinitewing.bombisland2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

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
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        Common.SCREEN_WIDTH=dm.widthPixels;
        Common.SCREEN_HEIGHT=dm.heightPixels;
        gameView=new GameView(this,intent.getStringExtra("hero"),
                intent.getStringExtra("map"),intent.getIntExtra("ai_count",3),intent.getStringExtra("ai_info"),gamebackgroundsound);
        setContentView(gameView);
        gamelistener = new GameListener();
        IntentFilter filter = new IntentFilter();
        filter.addAction("Result");
        registerReceiver(gamelistener, filter);
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
