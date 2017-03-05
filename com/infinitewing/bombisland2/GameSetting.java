package com.infinitewing.bombisland2;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.MotionEvent;
import android.view.View;
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
    private boolean BGM, effSound, effVibrator,FPS,bitmapOpt;
    private int controlMode;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_game_setting);

        sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
        BGM = sp.getBoolean("BGM", true);
        effSound = sp.getBoolean("effSound", true);
        effVibrator = sp.getBoolean("effVibrator", true);
        FPS = sp.getBoolean("FPS", false);
        bitmapOpt = sp.getBoolean("bitmapOpt", false);
        controlMode = sp.getInt("controlMode", 0);
        ((Switch) findViewById(R.id.Setting_SW_1)).setChecked(BGM);
        ((Switch) findViewById(R.id.Setting_SW_2)).setChecked(effSound);
        ((Switch) findViewById(R.id.Setting_SW_3)).setChecked(effVibrator);
        ((Switch) findViewById(R.id.Setting_SW_4)).setChecked(FPS);
        ((Switch) findViewById(R.id.Setting_SW_5)).setChecked(bitmapOpt);
        if(controlMode==0) {
            ((RadioGroup) findViewById(R.id.Setting_RG)).check(R.id.Setting_RB_1);
        }
        else if(controlMode==1) {
            ((RadioGroup) findViewById(R.id.Setting_RG)).check(R.id.Setting_RB_2);
        }
        else {
            ((RadioGroup) findViewById(R.id.Setting_RG)).check(R.id.Setting_RB_3);
        }
        findViewById(R.id.Setting_Submit).setOnClickListener(new ClickListener());
        findViewById(R.id.Setting_Submit).setOnTouchListener(new TouchListener());
        findViewById(R.id.Setting_Cancel).setOnClickListener(new ClickListener());
        findViewById(R.id.Setting_Cancel).setOnTouchListener(new TouchListener());
    }

    private void Save() {
        BGM = ((Switch) findViewById(R.id.Setting_SW_1)).isChecked();
        effSound = ((Switch) findViewById(R.id.Setting_SW_2)).isChecked();
        effVibrator = ((Switch) findViewById(R.id.Setting_SW_3)).isChecked();
        FPS = ((Switch) findViewById(R.id.Setting_SW_4)).isChecked();
        bitmapOpt= ((Switch) findViewById(R.id.Setting_SW_5)).isChecked();
        switch (((RadioGroup) findViewById(R.id.Setting_RG)).getCheckedRadioButtonId()){
            case R.id.Setting_RB_1:
                controlMode=0;
                break;
            case R.id.Setting_RB_2:
                controlMode=1;
                break;
            case R.id.Setting_RB_3:
                controlMode=2;
                break;
            default:
                controlMode=0;
                break;
        }
        SharedPreferences.Editor spEditor;
        spEditor = sp.edit();
        spEditor.putBoolean("BGM", BGM)
                .putBoolean("effSound", effSound)
                .putBoolean("effVibrator", effVibrator)
                .putBoolean("FPS", FPS)
                .putBoolean("bitmapOpt",bitmapOpt)
                .putInt("controlMode", controlMode).commit();
        Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
        GameSetting.this.finish();
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
}
