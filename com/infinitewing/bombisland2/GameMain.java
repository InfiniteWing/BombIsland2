package com.infinitewing.bombisland2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.event.Event;
import com.google.android.gms.games.event.Events;
import com.infinitewing.bombisland2.GameObject.Common;

import java.io.IOException;

/**
 * Created by Administrator on 2016/8/19.
 */
public class GameMain extends Activity {
    public GameListener gamelistener;
    public GameView gameView;
    public MediaPlayer gamebackgroundsound;
    public Intent intent;
    public Boolean OnActivityResult = false;
    public GoogleApiClient mGoogleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.SetFullScreen(getWindow());
        intent = this.getIntent();
        setScreenSize(getApplicationContext());
        SharedPreferences sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
        //判斷需不需要新手提示
        if (!sp.getBoolean("Guide_Game", false)) {
            Intent intent = new Intent(GameMain.this, GameGuide.class);
            intent.putExtra("guide", "game");
            intent.putExtra("newbe", true);
            startActivityForResult(intent, 1);
            SharedPreferences.Editor spEditor;
            spEditor = sp.edit();
            spEditor.putBoolean("Guide_Game", true).commit();
        } else {
            StartGame();
        }
    }

    public void StartGame() {
        try {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            if (gameView != null) {
                                gameView.mGoogleApiClient = mGoogleApiClient;
                            }
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            mGoogleApiClient = null;
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            mGoogleApiClient = null;
                        }
                    })
                    .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                    .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                    .build();
            mGoogleApiClient.connect();
        } catch (Exception e) {
            e.getCause();
        }
        gamebackgroundsound = new MediaPlayer();
        boolean endLessMode = intent.getBooleanExtra("endless_mode", false);
        gameView = new GameView(this, intent.getStringExtra("hero"),
                intent.getStringExtra("map"), intent.getIntExtra("ai_count", 3),
                intent.getStringExtra("ai_info"), gamebackgroundsound, endLessMode);
        setContentView(gameView);
        gamelistener = new GameListener();
        IntentFilter filter = new IntentFilter();
        filter.addAction("Result");
        try {
            registerReceiver(gamelistener, filter);
        } catch (Exception e) {
            e.getCause();
        }
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
        int settingWidth;
        SharedPreferences sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
        int resolutionMode=sp.getInt("resolutionMode", 2);
        if(resolutionMode==0){
            settingWidth=800;
        }else if(resolutionMode==1){
            settingWidth=1024;
        }else{
            settingWidth=1280;
        }
        if (width < settingWidth) {
            Common.SCREEN_WIDTH = width;
            Common.SCREEN_HEIGHT = height;
        } else {
            Common.SCREEN_WIDTH = settingWidth;
            Common.SCREEN_HEIGHT = (height * settingWidth) / width;
        }
        Common.OLD_SCREEN_WIDTH = width;
        Common.OLD_SCREEN_HEIGHT = height;
    }

    public void Pause() {
        try {
            if(gameView!=null) {
                if (gameView.gamebackgroundsound != null) {
                    if (gameView.gamebackgroundsound.isPlaying()) {
                        gameView.gamebackgroundsound.stop();
                    }
                    gameView.gamebackgroundsound.reset();
                    gameView.gamebackgroundsound.release();
                    gameView.gamebackgroundsound = null;
                }
            }
        } catch (Exception e) {
            e.getCause();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Pause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!OnActivityResult) {
            GameMain.this.finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (gameView != null) {
            try {
                if (gameView.gameThread != null) {
                    gameView.gameThread.stop = true;
                }
                if (gameView.map != null) {
                    gameView.map.Release();
                }
                if(gameView.soundManager!=null){
                    gameView.soundManager.Stop();
                }
            } catch (Exception e) {
                e.getCause();
            }
        }
        try {
            unregisterReceiver(gamelistener);
        } catch (Exception e) {
            e.getCause();
        }
        Pause();
        GameMain.this.finish();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            OnActivityResult = true;
            StartGame();
        }
    }

    private class GameListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            GameMain.this.setResult(RESULT_OK, new Intent());
            GameMain.this.finish();
            unregisterReceiver(gamelistener);
        }
    }
}
