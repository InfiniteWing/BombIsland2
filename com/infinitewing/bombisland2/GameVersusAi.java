package com.infinitewing.bombisland2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.event.Event;
import com.google.android.gms.games.event.Events;
import com.infinitewing.bombisland2.GameObject.Common;
import com.infinitewing.bombisland2.GameObject.Map;
import com.infinitewing.bombisland2.GameObject.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by Administrator on 2016/8/25.
 */
public class GameVersusAi extends Activity {
    private Intent intent;
    private Resources res;
    public int AiCount = 1, maxPlayer = 8;
    public Vector<ImageView> imageViews;
    public Vector<TextView> addTVs, removeTVs;
    public Vector<LinearLayout> linearLayouts;
    public Vector<Player> ais;
    public Boolean aiOns[];
    public Player aiNull;
    public String hero, map;
    public Boolean BGM;
    public GoogleApiClient mGoogleApiClient;
    public String mSaveGameData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.SetFullScreen(getWindow());
        setContentView(R.layout.game_versus_ai);
        SharedPreferences sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
        BGM = sp.getBoolean("BGM", true);

        res = getResources();
        imageViews = new Vector<>();
        addTVs = new Vector<>();
        removeTVs = new Vector<>();
        linearLayouts = new Vector<>();
        ais = new Vector<>();
        Player ai = new Player("ai02", getApplicationContext());
        ais.add(ai);//第一個AI
        ais.add(null);//第二個AI，以下類推
        ais.add(null);
        ais.add(null);
        ais.add(null);
        ais.add(null);
        ais.add(null);
        ais.add(null);

        aiNull = new Player("ai_null", getApplicationContext());
        aiOns = new Boolean[7];
        for (int i = 0; i < 7; i++) {
            aiOns[i] = false;
        }
        map = "bombisland01";
        hero = sp.getString("Last_Pick_Hero", "ai01");
        PlayBGM();
        imageViews.add((ImageView) findViewById(R.id.GameVersusAi_IV1));
        imageViews.add((ImageView) findViewById(R.id.GameVersusAi_IV2));
        imageViews.add((ImageView) findViewById(R.id.GameVersusAi_IV3));
        imageViews.add((ImageView) findViewById(R.id.GameVersusAi_IV4));
        imageViews.add((ImageView) findViewById(R.id.GameVersusAi_IV5));
        imageViews.add((ImageView) findViewById(R.id.GameVersusAi_IV6));
        imageViews.add((ImageView) findViewById(R.id.GameVersusAi_IV7));

        for (ImageView imageView : imageViews) {
            imageView.setOnClickListener(new ClickListener());
        }

        addTVs.add((TextView) findViewById(R.id.GameVersusAi_AddTV2));
        addTVs.add((TextView) findViewById(R.id.GameVersusAi_AddTV3));
        addTVs.add((TextView) findViewById(R.id.GameVersusAi_AddTV4));
        addTVs.add((TextView) findViewById(R.id.GameVersusAi_AddTV5));
        addTVs.add((TextView) findViewById(R.id.GameVersusAi_AddTV6));
        addTVs.add((TextView) findViewById(R.id.GameVersusAi_AddTV7));

        removeTVs.add((TextView) findViewById(R.id.GameVersusAi_RemoveTV2));
        removeTVs.add((TextView) findViewById(R.id.GameVersusAi_RemoveTV3));
        removeTVs.add((TextView) findViewById(R.id.GameVersusAi_RemoveTV4));
        removeTVs.add((TextView) findViewById(R.id.GameVersusAi_RemoveTV5));
        removeTVs.add((TextView) findViewById(R.id.GameVersusAi_RemoveTV6));
        removeTVs.add((TextView) findViewById(R.id.GameVersusAi_RemoveTV7));

        linearLayouts.add((LinearLayout) findViewById(R.id.GameVersusAi_LO1));
        linearLayouts.add((LinearLayout) findViewById(R.id.GameVersusAi_LO2));
        linearLayouts.add((LinearLayout) findViewById(R.id.GameVersusAi_LO3));
        linearLayouts.add((LinearLayout) findViewById(R.id.GameVersusAi_LO4));
        linearLayouts.add((LinearLayout) findViewById(R.id.GameVersusAi_LO5));
        linearLayouts.add((LinearLayout) findViewById(R.id.GameVersusAi_LO6));
        linearLayouts.add((LinearLayout) findViewById(R.id.GameVersusAi_LO7));

        for (TextView tv : addTVs) {
            tv.setOnClickListener(new ClickListener());
        }
        for (TextView tv : removeTVs) {
            tv.setOnClickListener(new ClickListener());
        }

        findViewById(R.id.GameVersusAi_Submit).setOnClickListener(new ClickListener());
        findViewById(R.id.GameVersusAi_Back).setOnClickListener(new ClickListener());
        findViewById(R.id.GameVersusAi_ChooseMapTV).setOnClickListener(new ClickListener());
        findViewById(R.id.GameVersusAi_PlayerIV).setOnClickListener(new ClickListener());
        findViewById(R.id.GameVersusAi_Guide).setOnClickListener(new ClickListener());
        AddAi(0);
        for (int i = 1; i < imageViews.size(); i++) {
            RemoveAi(i);
        }
        LoadHero();
        LoadMap();

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
        //判斷需不需要新手提示
        sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
        if (!sp.getBoolean("Guide_AI", false)) {
            ShowGuide();
            SharedPreferences.Editor spEditor;
            spEditor = sp.edit();
            spEditor.putBoolean("Guide_AI", true).commit();
        }
    }

    public void ShowGuide() {
        Intent intent = new Intent(GameVersusAi.this, GameGuide.class);
        intent.putExtra("guide", "ai");
        intent.putExtra("newbe", true);
        startActivity(intent);
    }

    public class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.GameVersusAi_Guide:
                    ShowGuide();
                    break;
                case R.id.GameVersusAi_AddTV2:
                    AddAi(1);
                    break;
                case R.id.GameVersusAi_AddTV3:
                    AddAi(2);
                    break;
                case R.id.GameVersusAi_AddTV4:
                    AddAi(3);
                    break;
                case R.id.GameVersusAi_AddTV5:
                    AddAi(4);
                    break;
                case R.id.GameVersusAi_AddTV6:
                    AddAi(5);
                    break;
                case R.id.GameVersusAi_AddTV7:
                    AddAi(6);
                    break;

                case R.id.GameVersusAi_IV1:
                    ChooseAi(0);
                    break;
                case R.id.GameVersusAi_IV2:
                    ChooseAi(1);
                    break;
                case R.id.GameVersusAi_IV3:
                    ChooseAi(2);
                    break;
                case R.id.GameVersusAi_IV4:
                    ChooseAi(3);
                    break;
                case R.id.GameVersusAi_IV5:
                    ChooseAi(4);
                    break;
                case R.id.GameVersusAi_IV6:
                    ChooseAi(5);
                    break;
                case R.id.GameVersusAi_IV7:
                    ChooseAi(6);
                    break;

                case R.id.GameVersusAi_RemoveTV2:
                    RemoveAi(1);
                    break;
                case R.id.GameVersusAi_RemoveTV3:
                    RemoveAi(2);
                    break;
                case R.id.GameVersusAi_RemoveTV4:
                    RemoveAi(3);
                    break;
                case R.id.GameVersusAi_RemoveTV5:
                    RemoveAi(4);
                    break;
                case R.id.GameVersusAi_RemoveTV6:
                    RemoveAi(5);
                    break;
                case R.id.GameVersusAi_RemoveTV7:
                    RemoveAi(6);
                    break;


                case R.id.GameVersusAi_ChooseMapTV:
                    intent = new Intent(GameVersusAi.this, GameChooseMap.class);
                    intent.putExtra("map", map);
                    startActivityForResult(intent, 1);
                    break;
                case R.id.GameVersusAi_Back:
                    GameVersusAi.this.finish();
                    break;
                case R.id.GameVersusAi_Submit:
                    intent = new Intent(GameVersusAi.this, GameMain.class);
                    intent.putExtra("hero", hero);
                    intent.putExtra("map", map);
                    intent.putExtra("ai_count", GetAiCount());
                    String ai_info = "";
                    boolean c = false;
                    for (int i = 0; i < 7; i++) {
                        if (aiOns[i]) {
                            if (c) {
                                ai_info += ",";
                            }
                            ai_info += ais.elementAt(i).id;
                            c = true;
                        }
                    }
                    intent.putExtra("ai_info", ai_info);
                    startActivityForResult(intent, 8888);

                    SharedPreferences sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor spEditor;
                    spEditor = sp.edit();
                    spEditor.putString("Last_Pick_Hero", hero).commit();

                    break;

                case R.id.GameVersusAi_PlayerIV:
                    intent = new Intent(GameVersusAi.this, GameChooseHero.class);
                    intent.putExtra("hero", hero);
                    startActivityForResult(intent, 2);
                    break;
            }
        }
    }

    public void LoadHero() {
        Player player = new Player(hero, getApplicationContext());
        ImageView iv = (ImageView) findViewById(R.id.GameVersusAi_PlayerIV);
        iv.setImageBitmap(player.character.img);
    }

    public void LoadMap() {
        //地圖人數限制
        for (int i = 0; i < 8 - maxPlayer; i++) {
            RemoveAi(i + maxPlayer - 1);
            addTVs.elementAt(i + maxPlayer - 2).setVisibility(View.GONE);
        }
        for (int i = 0; i < maxPlayer - 2; i++) {
            if (!aiOns[i + 1]) {
                addTVs.elementAt(i).setVisibility(View.VISIBLE);
            }
        }
        Map m = new Map(map, getApplicationContext());
        TextView tv = (TextView) findViewById(R.id.GameVersusAi_TitleTV);
        tv.setText(m.title);
        ImageView iv = (ImageView) findViewById(R.id.GameVersusAi_IV);
        Bitmap b = Common.getBitmapFromAsset("minimap/" + m.id + ".png", getApplicationContext());
        iv.setImageBitmap(b);
        ((TextView) findViewById(R.id.GameVersusAi_LimitTV)).setText((String) getText(R.string.game_choose_map_limit) + maxPlayer);
    }

    public void ChooseAi(int i) {
        if (aiOns[i]) {
            intent = new Intent(GameVersusAi.this, GameChooseHero.class);
            intent.putExtra("hero", ais.elementAt(i).id);
            startActivityForResult(intent, 2 + i + 1);
        } else {
            AddAi(i);
        }
    }

    public void SetAi(int i, String aiID) {
        Player ai = new Player(aiID, getApplicationContext());
        ais.setElementAt(ai, i);
        imageViews.elementAt(i).setImageBitmap(ais.elementAt(i).character.img);
    }

    public void AddAi(int i) {
        if (i + 2 > maxPlayer) {
            return;
        }
        aiOns[i] = true;
        if (ais.elementAt(i) == null) {
            Player ai = new Player("ai02", getApplicationContext());
            ais.setElementAt(ai, i);
        }
        imageViews.elementAt(i).setImageBitmap(ais.elementAt(i).character.img);
        if (i > 0) {
            addTVs.elementAt(i - 1).setVisibility(View.GONE);
            removeTVs.elementAt(i - 1).setVisibility(View.VISIBLE);
        }
    }

    public void RemoveAi(int i) {
        aiOns[i] = false;
        imageViews.elementAt(i).setImageBitmap(aiNull.character.img);
        if (i > 0) {
            addTVs.elementAt(i - 1).setVisibility(View.VISIBLE);
            removeTVs.elementAt(i - 1).setVisibility(View.GONE);
        }
    }

    public int GetAiCount() {
        int c = 0;
        for (int i = 0; i < aiOns.length; i++) {
            if (aiOns[i]) {
                c++;
            }
        }
        return c;
    }

    public void SaveGameToGoogle() {
        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                // Open the saved game using its name.
                mSaveGameData = Common.GenerateSaveData(getApplicationContext());
                try {
                    Common.writeSnapshot(mGoogleApiClient, Common.APP_GOOGLE_UID, mSaveGameData.getBytes(),
                            BitmapFactory.decodeResource(getResources(), R.drawable.savebg),
                            Common.getStringResourceByName("app_name", getApplicationContext()));
                } catch (Exception e) {
                    e.getCause();
                }
                return 1;
            }

            @Override
            protected void onPostExecute(Integer status) {
            }
        };
        try {
            task.execute();
        } catch (Exception e) {
            e.getCause();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            SaveGameToGoogle();
        } catch (Exception e) {
            e.getCause();
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 8888) {

        } else if (requestCode == 1 && resultCode == RESULT_OK) {
            map = data.getStringExtra("map");
            maxPlayer = data.getIntExtra("maxPlayer", 4);
            LoadMap();
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            hero = data.getStringExtra("hero");
            LoadHero();
        } else if (requestCode > 2 && resultCode == RESULT_OK) {
            SetAi(requestCode - 3, data.getStringExtra("hero"));
        }
    }

    public void PlayBGM() {
        /*
        3.42版後先行移除
        try {
            if (BGM) {
                if (gamebackgroundsound == null) {
                    gamebackgroundsound = MediaPlayer.create(this, R.raw.bgm);
                }
                gamebackgroundsound.setVolume(0.3f, 0.3f);
                gamebackgroundsound.setLooping(true);
                if (!gamebackgroundsound.isPlaying()) {
                    gamebackgroundsound.start();
                }
            }
        } catch (Exception e) {
            e.getCause();
        }
        */
    }


    public void Restart() {
        PlayBGM();
    }

    @Override
    protected void onRestart() {
        Restart();
        super.onRestart();
    }


    @Override
    protected void onDestroy() {
        GameVersusAi.this.finish();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Common.SetFullScreen(getWindow());
        super.onResume();
    }
}