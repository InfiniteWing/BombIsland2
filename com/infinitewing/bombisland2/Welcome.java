package com.infinitewing.bombisland2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.Snapshots;
import com.infinitewing.bombisland2.GameObject.Common;
import com.infinitewing.bombisland2.GameObject.SoundManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class Welcome extends Activity {
    public AlphaAnimation animationAlpha, animationAlphaBlink;
    public MediaPlayer bgm;
    public Boolean BGM, effSound;
    public int state = 0;
    public GoogleApiClient mGoogleApiClient;
    public Boolean GoogleConnected = false;
    public SharedPreferences sp;
    public SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            ((TextView) findViewById(R.id.Welcome_Version)).setText("V " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        findViewById(R.id.Welcome_SignIn_Google).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayCheckSound();
                Intent intent = new Intent(Welcome.this, Index.class);
                intent.putExtra("SignIn_Method", "Google");
                startActivity(intent);
                Welcome.this.finish();
            }
        });
        findViewById(R.id.Welcome_SignIn_Local).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayCheckSound();
                Intent intent = new Intent(Welcome.this, Index.class);
                intent.putExtra("SignIn_Method", "Local");
                startActivity(intent);
                Welcome.this.finish();
            }
        });
        animationAlphaBlink = new AlphaAnimation(1.0f, 0.0f);
        animationAlphaBlink.setDuration(600);
        animationAlphaBlink.setRepeatMode(Animation.REVERSE);
        animationAlphaBlink.setRepeatCount(Animation.INFINITE);
    }

    public void PlayBGM() {
        if (BGM) {
            try {
                if (bgm == null) {
                    bgm = MediaPlayer.create(this, R.raw.opening);
                }
                bgm.setVolume(0.88f, 0.88f);
                bgm.setLooping(true);
                if (!bgm.isPlaying()) {
                    bgm.start();
                }
            } catch (Exception e) {
                e.getCause();
            }
        }
    }

    public void InitGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        GoogleConnected = true;
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        mGoogleApiClient = null;
                    }
                })
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (state) {
            case 1:
                PlayCheckSound();
                if (GoogleConnected) {
                    state = 2;
                    Intent intent = new Intent(Welcome.this, Index.class);
                    intent.putExtra("SignIn_Method", "Google");
                    startActivity(intent);
                    Welcome.this.finish();
                } else {
                    SharedPreferences sp;
                    sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
                    Boolean HadSignIn = sp.getBoolean("HadSignIn", false);
                    if (HadSignIn) {
                        Intent intent = new Intent(Welcome.this, Index.class);
                        intent.putExtra("SignIn_Method", "Same");
                        startActivity(intent);
                        Welcome.this.finish();
                    } else {
                        findViewById(R.id.Welcome_SignIn_Layout).setVisibility(View.VISIBLE);
                        findViewById(R.id.Welcome_Press).clearAnimation();
                        findViewById(R.id.Welcome_Press).setVisibility(View.INVISIBLE);
                    }
                    state = 2;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void PlayCheckSound() {
        if (effSound) {
            soundManager.addSound("check.mp3", getApplicationContext());
        }
    }

    public void Pause() {
        if (bgm != null) {
            if (bgm.isPlaying()) {
                bgm.stop();
            }
            bgm.reset();
            bgm.release();
            bgm = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (soundManager != null) {
                soundManager.Stop();
            }
        } catch (Exception e) {
            e.getCause();
        }
    }

    @Override
    protected void onPause() {
        Pause();
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Common.SetFullScreen(getWindow());
        GoogleConnected = false;
        soundManager = new SoundManager();
        soundManager.start();
        sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
        BGM = sp.getBoolean("BGM", true);
        effSound = sp.getBoolean("effSound", true);
        InitGoogleApiClient();
        animationAlpha = new AlphaAnimation(0.1f, 1.0f);
        animationAlpha.setDuration(3000);
        animationAlpha.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                PlayBGM();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.Welcome_Press).setVisibility(View.VISIBLE);
                findViewById(R.id.Welcome_Press).setAnimation(animationAlphaBlink);
                findViewById(R.id.Welcome_Copyright).setVisibility(View.VISIBLE);
                state = 1;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.Welcome_Logos).setAnimation(animationAlpha);
        super.onResume();
    }
}
