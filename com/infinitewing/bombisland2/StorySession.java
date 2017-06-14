package com.infinitewing.bombisland2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.infinitewing.bombisland2.GameObject.Common;
import com.infinitewing.bombisland2.GameObject.Player;
import com.infinitewing.bombisland2.GameObject.Recorder;
import com.infinitewing.bombisland2.GameObject.SoundManager;
import com.infinitewing.bombisland2.GameObject.Story;
import com.infinitewing.bombisland2.GameObject.StoryDialog;

import org.w3c.dom.Text;

import java.util.Vector;

/**
 * Created by InfiniteWing on 2017/4/5.
 */
public class StorySession extends Activity {
    public int currentStoryIndex = -1;
    public Story story;
    public MediaPlayer bgm;
    public Boolean BGM, effSound;
    public SharedPreferences sp;
    public SoundManager soundManager;
    public boolean afterGame = false, isWin = false, isStarting = false, isEnding = false;
    public long lastTouchTime = System.currentTimeMillis();
    public GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.SetFullScreen(getWindow());
        setContentView(R.layout.story_session);
        Intent intent = getIntent();
        findViewById(R.id.StorySession_Skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!afterGame) {
                    StartStory();
                } else {
                    EndStory();
                }
            }
        });
        findViewById(R.id.StorySession_Exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EndStory();
            }
        });
        story = new Story(intent.getStringExtra("story_name"),
                intent.getIntExtra("story_stage", 0),
                intent.getStringExtra("story"), getApplicationContext());
        Play(true);

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void Play(Boolean force) {
        if (System.currentTimeMillis() - lastTouchTime > 400 || force) {
            if (!force) {
                PlayCheckSound();
            }
            lastTouchTime = System.currentTimeMillis();
            currentStoryIndex++;
            if (afterGame) {
                if (isWin) {
                    if (currentStoryIndex < story.endWinDialogs.size()) {
                        SetDialog(story.endWinDialogs);
                    } else {
                        EndStory();
                    }
                } else {
                    if (currentStoryIndex < story.endLossDialogs.size()) {
                        SetDialog(story.endLossDialogs);
                    } else {
                        EndStory();
                    }
                }
            } else {
                if (currentStoryIndex >= story.startDialogs.size()) {
                    if (currentStoryIndex == story.startDialogs.size()) {
                        findViewById(R.id.StorySession_IV_Left).setAlpha(0.0f);
                        findViewById(R.id.StorySession_IV_Center).setAlpha(0.0f);
                        findViewById(R.id.StorySession_IV_Right).setAlpha(0.0f);
                        findViewById(R.id.StorySession_NameTag_Left).setVisibility(View.INVISIBLE);
                        findViewById(R.id.StorySession_NameTag_Right).setVisibility(View.INVISIBLE);
                        findViewById(R.id.StorySession_NameTag_Center).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.StorySession_TV_Center)).setText(R.string.story_session_hint);
                    }
                    if (currentStoryIndex == story.startDialogs.size() + story.hintDialogs.size() + 1) {
                        StartStory();
                    } else if (currentStoryIndex == story.startDialogs.size() + story.hintDialogs.size()) {
                        //前提結束，準備開始遊戲
                        ((TextView) findViewById(R.id.StorySession_TV)).setText(R.string.press_to_continue);
                    } else {
                        //播放提示
                        ((TextView) findViewById(R.id.StorySession_TV)).setText(story.hintDialogs.elementAt(currentStoryIndex - story.startDialogs.size()).dialog);
                    }
                } else {
                    SetDialog(story.startDialogs);
                }
            }
        }
    }

    public void InitGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {

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
    }

    public void StartStory() {
        //開始遊戲
        if (isStarting) {
            return;
        }
        isStarting = true;
        Intent intent = new Intent(StorySession.this, GameMain.class);
        if (story.type.equals(Story.TYPE_SURVIVAL)) {
            //打倒所有敵人
        }
        if (story.type.equals(Story.TYPE_ENDLESS_SURVIVAL)) {
            intent.putExtra("endless_mode", true);
            intent.putExtra("endless_seconds", story.surviveSeconds);
        }
        if (story.type.equals(Story.TYPE_SURVIVAL_WITH_TEAMMATE)) {
            intent.putExtra("survive_with_teammate", true);
        }
        intent.putExtra("teammate_info", story.teammates);
        intent.putExtra("boss_info", story.boss);
        intent.putExtra("hero", story.hero);
        intent.putExtra("map", story.map);
        if (story.ais != null) {
            intent.putExtra("ai_count", story.ais.split(",").length);
        }
        intent.putExtra("ai_info", story.ais);
        startActivityForResult(intent, 8888);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void SetDialog(Vector<StoryDialog> dialogs) {
        if (dialogs.elementAt(currentStoryIndex).direction == StoryDialog.LEFT) {
            findViewById(R.id.StorySession_IV_Left).setAlpha(1.0f);
            findViewById(R.id.StorySession_IV_Center).setAlpha(0.6f);
            findViewById(R.id.StorySession_IV_Right).setAlpha(0.6f);
            ((TextView) findViewById(R.id.StorySession_TV_Left)).setText(dialogs.elementAt(currentStoryIndex).nameTag);
            findViewById(R.id.StorySession_NameTag_Left).setVisibility(View.VISIBLE);
            findViewById(R.id.StorySession_NameTag_Right).setVisibility(View.INVISIBLE);
            findViewById(R.id.StorySession_NameTag_Center).setVisibility(View.INVISIBLE);
            ((ImageView) findViewById(R.id.StorySession_IV_Left)).setImageBitmap(dialogs.elementAt(currentStoryIndex).img);
        } else if (dialogs.elementAt(currentStoryIndex).direction == StoryDialog.RIGHT) {
            findViewById(R.id.StorySession_IV_Left).setAlpha(0.6f);
            findViewById(R.id.StorySession_IV_Center).setAlpha(0.6f);
            findViewById(R.id.StorySession_IV_Right).setAlpha(1.0f);
            ((TextView) findViewById(R.id.StorySession_TV_Right)).setText(dialogs.elementAt(currentStoryIndex).nameTag);
            findViewById(R.id.StorySession_NameTag_Left).setVisibility(View.INVISIBLE);
            findViewById(R.id.StorySession_NameTag_Right).setVisibility(View.VISIBLE);
            findViewById(R.id.StorySession_NameTag_Center).setVisibility(View.INVISIBLE);
            ((ImageView) findViewById(R.id.StorySession_IV_Right)).setImageBitmap(dialogs.elementAt(currentStoryIndex).img);
        } else if (dialogs.elementAt(currentStoryIndex).direction == StoryDialog.CENTER) {
            findViewById(R.id.StorySession_IV_Left).setAlpha(0.6f);
            findViewById(R.id.StorySession_IV_Center).setAlpha(1.0f);
            findViewById(R.id.StorySession_IV_Right).setAlpha(0.6f);
            ((TextView) findViewById(R.id.StorySession_TV_Center)).setText(dialogs.elementAt(currentStoryIndex).nameTag);
            findViewById(R.id.StorySession_NameTag_Left).setVisibility(View.INVISIBLE);
            findViewById(R.id.StorySession_NameTag_Right).setVisibility(View.INVISIBLE);
            findViewById(R.id.StorySession_NameTag_Center).setVisibility(View.VISIBLE);
            ((ImageView) findViewById(R.id.StorySession_IV_Center)).setImageBitmap(dialogs.elementAt(currentStoryIndex).img);
        } else {
            findViewById(R.id.StorySession_IV_Left).setAlpha(0.6f);
            findViewById(R.id.StorySession_IV_Center).setAlpha(0.6f);
            findViewById(R.id.StorySession_IV_Right).setAlpha(0.6f);
            findViewById(R.id.StorySession_NameTag_Left).setVisibility(View.INVISIBLE);
            findViewById(R.id.StorySession_NameTag_Right).setVisibility(View.INVISIBLE);
            findViewById(R.id.StorySession_NameTag_Center).setVisibility(View.INVISIBLE);
        }
        ((TextView) findViewById(R.id.StorySession_TV)).setText(dialogs.elementAt(currentStoryIndex).dialog);
    }

    public void PlayCheckSound() {
        if (effSound) {
            soundManager.addSound("check.mp3", getApplicationContext());
        }
    }

    public void EndStory() {
        if (isEnding) {
            return;
        }
        isEnding = true;
        if (story != null) {
            story.Release();
        }
        if(soundManager!=null){
            soundManager.Stop();
        }
        Intent intent = new Intent(StorySession.this, StoryIndex.class);
        startActivity(intent);
        StorySession.this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //結束戰鬥後會進入結束事件對話
        currentStoryIndex = -1;
        afterGame = true;
        ((ImageView) findViewById(R.id.StorySession_IV_Left)).setImageBitmap(null);
        ((ImageView) findViewById(R.id.StorySession_IV_Right)).setImageBitmap(null);
        ((ImageView) findViewById(R.id.StorySession_IV_Center)).setImageBitmap(null);
        findViewById(R.id.StorySession_NameTag_Left).setVisibility(View.INVISIBLE);
        findViewById(R.id.StorySession_NameTag_Right).setVisibility(View.INVISIBLE);
        findViewById(R.id.StorySession_NameTag_Center).setVisibility(View.INVISIBLE);
        ((TextView) findViewById(R.id.StorySession_TV)).setText("");
        if (data != null) {
            isWin = data.getBooleanExtra("isWin", false);
            if (isWin) {
                SharedPreferences sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
                int currentStage = sp.getInt(Common.STORY_UNITY, 0);
                SharedPreferences.Editor spEditor;
                spEditor = sp.edit();
                if (story != null) {
                    if (currentStage == story.stage) {
                        spEditor.putInt(Common.STORY_UNITY, currentStage + 1).commit();
                        //發送獎勵，主要是金幣
                        if (story.rewards.elementAt(0).type.equals("money")) {
                            int money = 0;
                            String file = "money.bl2";
                            Recorder recorder = new Recorder(getApplicationContext());
                            String moneyRecord = recorder.Read(file);
                            if (moneyRecord != null) {
                                try {
                                    money = Integer.parseInt(moneyRecord);
                                } catch (Exception e) {
                                    e.getCause();
                                    money = Common.DEFAULT_MONEY;
                                }
                            }
                            money += Integer.parseInt(story.rewards.elementAt(0).value);
                            recorder.Write(String.valueOf(money), file);
                        }
                    }
                }
                try {
                    Common.SaveGameToGoogle(mGoogleApiClient, getApplicationContext());
                } catch (Exception e) {
                    e.getCause();
                }
            }
        }
        Play(true);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        Common.SetFullScreen(getWindow());
        soundManager = new SoundManager(0.3f);
        soundManager.start();
        sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
        BGM = sp.getBoolean("BGM", true);
        effSound = sp.getBoolean("effSound", true);
        InitGoogleApiClient();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        EndStory();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        EndStory();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if(event.getAction()==MotionEvent.ACTION_UP) {
                Play(false);
            }
        } catch (Exception e) {
            e.getCause();
        }
        return super.onTouchEvent(event);
    }
}
