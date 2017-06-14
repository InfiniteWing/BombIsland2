package com.infinitewing.bombisland2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.infinitewing.bombisland2.GameObject.Common;

import java.io.IOException;
import java.util.Set;

/**
 * Created by Administrator on 2017/2/2.
 */
public class GameSetting extends Activity {
    private boolean BGM, effSound, effVibrator, FPS, bitmapOpt;
    private int controlMode, resolutionMode;
    private SharedPreferences sp;
    private String bombSkin;
    public AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.SetFullScreen(getWindow());
        setContentView(R.layout.content_game_setting);

        sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
        BGM = sp.getBoolean("BGM", true);
        effSound = sp.getBoolean("effSound", true);
        effVibrator = sp.getBoolean("effVibrator", true);
        FPS = sp.getBoolean("FPS", false);
        bitmapOpt = sp.getBoolean("bitmapOpt", false);
        controlMode = sp.getInt("controlMode", 0);
        resolutionMode = sp.getInt("resolutionMode", 2);
        bombSkin=sp.getString("bombSkin",Common.DEFAULT_BOMBSKIN);
        ((Switch) findViewById(R.id.Setting_SW_1)).setChecked(BGM);
        ((Switch) findViewById(R.id.Setting_SW_2)).setChecked(effSound);
        ((Switch) findViewById(R.id.Setting_SW_3)).setChecked(effVibrator);
        ((Switch) findViewById(R.id.Setting_SW_4)).setChecked(FPS);
        ((Switch) findViewById(R.id.Setting_SW_5)).setChecked(bitmapOpt);
        if (controlMode == 0) {
            ((RadioGroup) findViewById(R.id.Setting_RG)).check(R.id.Setting_RB_1);
        } else if (controlMode == 1) {
            ((RadioGroup) findViewById(R.id.Setting_RG)).check(R.id.Setting_RB_2);
        } else {
            ((RadioGroup) findViewById(R.id.Setting_RG)).check(R.id.Setting_RB_3);
        }

        if (resolutionMode == 0) {
            ((RadioGroup) findViewById(R.id.Setting_ResolutionRG)).check(R.id.Setting_ResolutionRB_1);
        } else if (resolutionMode == 1) {
            ((RadioGroup) findViewById(R.id.Setting_ResolutionRG)).check(R.id.Setting_ResolutionRB_2);
        } else {
            ((RadioGroup) findViewById(R.id.Setting_ResolutionRG)).check(R.id.Setting_ResolutionRB_3);
        }
        findViewById(R.id.Setting_Submit).setOnClickListener(new ClickListener());
        findViewById(R.id.Setting_Submit).setOnTouchListener(new TouchListener());
        findViewById(R.id.Setting_Cancel).setOnClickListener(new ClickListener());
        findViewById(R.id.Setting_Cancel).setOnTouchListener(new TouchListener());
        findViewById(R.id.Setting_BombSkin_IV).setOnClickListener(new ClickListener());
        ShowBombSkin();
    }
    private void ShowBombSkin(){
        if(bombSkin.equals("bomb1")){
            ((ImageView)findViewById(R.id.Setting_BombSkin_IV)).setImageResource(R.drawable.bomb_black);
        }else if(bombSkin.equals("bomb_yellow")){
            ((ImageView)findViewById(R.id.Setting_BombSkin_IV)).setImageResource(R.drawable.bomb_yellow);
        }else if(bombSkin.equals("bomb_red")){
            ((ImageView)findViewById(R.id.Setting_BombSkin_IV)).setImageResource(R.drawable.bomb_red);
        }else if(bombSkin.equals("bomb_blue")){
            ((ImageView)findViewById(R.id.Setting_BombSkin_IV)).setImageResource(R.drawable.bomb_blue);
        }else if(bombSkin.equals("bomb_purple")){
            ((ImageView)findViewById(R.id.Setting_BombSkin_IV)).setImageResource(R.drawable.bomb_purple);
        }
    }
    private void Save() {
        BGM = ((Switch) findViewById(R.id.Setting_SW_1)).isChecked();
        effSound = ((Switch) findViewById(R.id.Setting_SW_2)).isChecked();
        effVibrator = ((Switch) findViewById(R.id.Setting_SW_3)).isChecked();
        FPS = ((Switch) findViewById(R.id.Setting_SW_4)).isChecked();
        bitmapOpt = ((Switch) findViewById(R.id.Setting_SW_5)).isChecked();
        switch (((RadioGroup) findViewById(R.id.Setting_RG)).getCheckedRadioButtonId()) {
            case R.id.Setting_RB_1:
                controlMode = 0;
                break;
            case R.id.Setting_RB_2:
                controlMode = 1;
                break;
            case R.id.Setting_RB_3:
                controlMode = 2;
                break;
            default:
                controlMode = 0;
                break;
        }
        switch (((RadioGroup) findViewById(R.id.Setting_ResolutionRG)).getCheckedRadioButtonId()) {
            case R.id.Setting_ResolutionRB_1:
                resolutionMode = 0;
                break;
            case R.id.Setting_ResolutionRB_2:
                resolutionMode = 1;
                break;
            case R.id.Setting_ResolutionRB_3:
                resolutionMode = 2;
                break;
            default:
                resolutionMode = 2;
                break;
        }
        SharedPreferences.Editor spEditor;
        spEditor = sp.edit();
        spEditor.putBoolean("BGM", BGM)
                .putBoolean("effSound", effSound)
                .putBoolean("effVibrator", effVibrator)
                .putBoolean("FPS", FPS)
                .putBoolean("bitmapOpt", bitmapOpt)
                .putInt("controlMode", controlMode)
                .putInt("resolutionMode", resolutionMode)
                .putString("bombSkin",bombSkin).commit();
        Toast.makeText(getApplicationContext(), R.string.index_save_save_success, Toast.LENGTH_SHORT).show();
        GameSetting.this.finish();
    }
    public void ChooseBombSkin(String bombSkin){
        this.bombSkin=bombSkin;
        alertDialog.hide();
        ShowBombSkin();
    }
    public void ShowChooseBombSkinDialog() {
        LayoutInflater inflater = LayoutInflater.from(GameSetting.this);
        final View chooseBombSkinView = inflater.inflate(R.layout.setting_choose_bomb_skin, null);
        chooseBombSkinView.findViewById(R.id.Setting_BombSkin_Black).setOnClickListener(new ChooseBombSkinClickListener());
        chooseBombSkinView.findViewById(R.id.Setting_BombSkin_Blue).setOnClickListener(new ChooseBombSkinClickListener());
        chooseBombSkinView.findViewById(R.id.Setting_BombSkin_Yellow).setOnClickListener(new ChooseBombSkinClickListener());
        chooseBombSkinView.findViewById(R.id.Setting_BombSkin_Red).setOnClickListener(new ChooseBombSkinClickListener());
        chooseBombSkinView.findViewById(R.id.Setting_BombSkin_Purple).setOnClickListener(new ChooseBombSkinClickListener());
        if(sp.getBoolean("BombSkin_bomb_blue", false)){
            chooseBombSkinView.findViewById(R.id.Setting_BombSkin_Blue).setVisibility(View.VISIBLE);
        }else{
            chooseBombSkinView.findViewById(R.id.Setting_BombSkin_Blue).setVisibility(View.GONE);
        }
        if(sp.getBoolean("BombSkin_bomb_red", false)){
            chooseBombSkinView.findViewById(R.id.Setting_BombSkin_Red).setVisibility(View.VISIBLE);
        }else{
            chooseBombSkinView.findViewById(R.id.Setting_BombSkin_Red).setVisibility(View.GONE);
        }
        if(sp.getBoolean("BombSkin_bomb_yellow", false)){
            chooseBombSkinView.findViewById(R.id.Setting_BombSkin_Yellow).setVisibility(View.VISIBLE);
        }else{
            chooseBombSkinView.findViewById(R.id.Setting_BombSkin_Yellow).setVisibility(View.GONE);
        }
        if(sp.getBoolean("BombSkin_bomb_purple", false)){
            chooseBombSkinView.findViewById(R.id.Setting_BombSkin_Purple).setVisibility(View.VISIBLE);
        }else{
            chooseBombSkinView.findViewById(R.id.Setting_BombSkin_Purple).setVisibility(View.GONE);
        }
        alertDialog = new AlertDialog.Builder(GameSetting.this).create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setView(chooseBombSkinView);
        alertDialog.setTitle(R.string.game_setting_bomb_skin);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Common.SetFullScreen(getWindow());
            }
        });
        alertDialog.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        alertDialog.show();
        Common.SetFullScreen(alertDialog.getWindow());
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        alertDialog.getWindow().setLayout(
                Common.DP2PX(320, getApplicationContext()),
                alertDialog.getWindow().getAttributes().height);
    }
    public class ChooseBombSkinClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id){
                case R.id.Setting_BombSkin_Black:
                    ChooseBombSkin("bomb1");
                    break;
                case R.id.Setting_BombSkin_Blue:
                    ChooseBombSkin("bomb_blue");
                    break;
                case R.id.Setting_BombSkin_Yellow:
                    ChooseBombSkin("bomb_yellow");
                    break;
                case R.id.Setting_BombSkin_Red:
                    ChooseBombSkin("bomb_red");
                    break;
                case R.id.Setting_BombSkin_Purple:
                    ChooseBombSkin("bomb_purple");
                    break;
            }
        }
    }
    public class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.Setting_Submit:
                    Save();
                    break;
                case R.id.Setting_Cancel:
                    GameSetting.this.finish();
                    break;
                case R.id.Setting_BombSkin_IV:
                    ShowChooseBombSkinDialog();
                    break;
            }
        }
    }

    public class TouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                view.setAlpha((float) 0.6);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view.setAlpha((float) 1.0);
            }
            return false;
        }
    }

    @Override
    protected void onResume() {
        Common.SetFullScreen(getWindow());
        super.onResume();
    }
}
