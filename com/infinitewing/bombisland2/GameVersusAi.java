package com.infinitewing.bombisland2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    public int AiCount = 1;
    public Vector<ImageView> imageViews;
    public Vector<TextView> textViews;
    public Vector<LinearLayout> linearLayouts;
    public Vector<Player> ais;
    public Boolean aiOns[];
    public Player aiNull;
    public String hero, map;
    public MediaPlayer gamebackgroundsound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_versus_ai);
        res = getResources();
        imageViews = new Vector<>();
        textViews = new Vector<>();
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
        map = "map01";
        hero = "ai01";

        gamebackgroundsound = MediaPlayer.create(this, R.raw.ai_choose);
        gamebackgroundsound.setVolume(0.3f, 0.3f);
        gamebackgroundsound.setLooping(true);
        gamebackgroundsound.start();

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

        textViews.add((TextView) findViewById(R.id.GameVersusAi_TV1));
        textViews.add((TextView) findViewById(R.id.GameVersusAi_TV2));
        textViews.add((TextView) findViewById(R.id.GameVersusAi_TV3));
        textViews.add((TextView) findViewById(R.id.GameVersusAi_TV4));
        textViews.add((TextView) findViewById(R.id.GameVersusAi_TV5));
        textViews.add((TextView) findViewById(R.id.GameVersusAi_TV6));
        textViews.add((TextView) findViewById(R.id.GameVersusAi_TV7));

        linearLayouts.add((LinearLayout) findViewById(R.id.GameVersusAi_LO1));
        linearLayouts.add((LinearLayout) findViewById(R.id.GameVersusAi_LO2));
        linearLayouts.add((LinearLayout) findViewById(R.id.GameVersusAi_LO3));
        linearLayouts.add((LinearLayout) findViewById(R.id.GameVersusAi_LO4));
        linearLayouts.add((LinearLayout) findViewById(R.id.GameVersusAi_LO5));
        linearLayouts.add((LinearLayout) findViewById(R.id.GameVersusAi_LO6));
        linearLayouts.add((LinearLayout) findViewById(R.id.GameVersusAi_LO7));

        findViewById(R.id.GameVersusAi_AddTV2).setOnClickListener(new ClickListener());
        findViewById(R.id.GameVersusAi_AddTV3).setOnClickListener(new ClickListener());
        findViewById(R.id.GameVersusAi_AddTV4).setOnClickListener(new ClickListener());
        findViewById(R.id.GameVersusAi_AddTV5).setOnClickListener(new ClickListener());
        findViewById(R.id.GameVersusAi_AddTV6).setOnClickListener(new ClickListener());
        findViewById(R.id.GameVersusAi_AddTV7).setOnClickListener(new ClickListener());

        findViewById(R.id.GameVersusAi_RemoveTV2).setOnClickListener(new ClickListener());
        findViewById(R.id.GameVersusAi_RemoveTV3).setOnClickListener(new ClickListener());
        findViewById(R.id.GameVersusAi_RemoveTV4).setOnClickListener(new ClickListener());
        findViewById(R.id.GameVersusAi_RemoveTV5).setOnClickListener(new ClickListener());
        findViewById(R.id.GameVersusAi_RemoveTV6).setOnClickListener(new ClickListener());
        findViewById(R.id.GameVersusAi_RemoveTV7).setOnClickListener(new ClickListener());

        findViewById(R.id.GameVersusAi_Submit).setOnClickListener(new ClickListener());
        findViewById(R.id.GameVersusAi_ChooseMapTV).setOnClickListener(new ClickListener());
        findViewById(R.id.GameVersusAi_PlayerIV).setOnClickListener(new ClickListener());
        LoadHero();
        LoadMap();
        AddAi(0);
        for (int i = 1; i < imageViews.size(); i++) {
            RemoveAi(i);
        }
    }

    public class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
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
                    startActivity(intent);
                    gamebackgroundsound.stop();


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
        Map m = new Map(map, getApplicationContext());
        TextView tv = (TextView) findViewById(R.id.GameVersusAi_TitleTV);
        tv.setText(m.title);
        ImageView iv = (ImageView) findViewById(R.id.GameVersusAi_IV);
        Bitmap b = Common.getBitmapFromAsset("minimap/" + m.id + ".png", getApplicationContext());
        iv.setImageBitmap(b);
    }

    public void ChooseAi(int i) {
        if (aiOns[i]) {
            intent = new Intent(GameVersusAi.this, GameChooseHero.class);
            intent.putExtra("hero", ais.elementAt(i).id);
            startActivityForResult(intent, 2 + i + 1);
        }else{
            AddAi(i);
        }
    }

    public void SetAi(int i, String aiID) {
        Player ai = new Player(aiID, getApplicationContext());
        ais.setElementAt(ai, i);
        imageViews.elementAt(i).setImageBitmap(ais.elementAt(i).character.img);
    }

    public void AddAi(int i) {
        aiOns[i] = true;
        if (ais.elementAt(i) == null) {
            Player ai = new Player("ai02", getApplicationContext());
            ais.setElementAt(ai, i);
        }
        imageViews.elementAt(i).setImageBitmap(ais.elementAt(i).character.img);
    }

    public void RemoveAi(int i) {
        aiOns[i] = false;
        imageViews.elementAt(i).setImageBitmap(aiNull.character.img);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        gamebackgroundsound.start();
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            map = data.getStringExtra("map");
            LoadMap();
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            hero = data.getStringExtra("hero");
            LoadHero();
        }
        if (requestCode > 2 && resultCode == RESULT_OK) {
            SetAi(requestCode - 3, data.getStringExtra("hero"));
        }
    }

    @Override
    protected void onDestroy() {
        GameVersusAi.this.finish();
        gamebackgroundsound.stop();
        super.onDestroy();
    }
}