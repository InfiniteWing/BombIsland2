package com.infinitewing.bombisland2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.infinitewing.bombisland2.GameObject.Common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by InfiniteWing on 2016/8/18.
 */
public class Index extends Activity {
    private Intent intent;
    private Resources res;

    List<String> BT_Setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index);
        res = getResources();
        findViewById(R.id.index_1).setOnClickListener(new ClickListener());
        findViewById(R.id.index_1).setOnTouchListener(new TouchListener());
        findViewById(R.id.index_2).setOnClickListener(new ClickListener());
        findViewById(R.id.index_2).setOnTouchListener(new TouchListener());
        findViewById(R.id.index_3).setOnClickListener(new ClickListener());
        findViewById(R.id.index_3).setOnTouchListener(new TouchListener());
        findViewById(R.id.index_4).setOnClickListener(new ClickListener());
        findViewById(R.id.index_4).setOnTouchListener(new TouchListener());
        findViewById(R.id.index_5).setOnClickListener(new ClickListener());
        findViewById(R.id.index_5).setOnTouchListener(new TouchListener());
        findViewById(R.id.index_6).setOnClickListener(new ClickListener());
        findViewById(R.id.index_6).setOnTouchListener(new TouchListener());
        try {
            ShowUpdate(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.index_1:
                    Toast.makeText(getApplicationContext(), R.string.index_building, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.index_2:
                    intent = new Intent(Index.this, GameVersusAi.class);
                    startActivity(intent);
                    break;
                case R.id.index_3:
                    SharedPreferences sp;
                    sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
                    if(!sp.getBoolean("Guide_Bluetooth",false)){
                        Intent intent = new Intent(Index.this, GameGuide.class);
                        intent.putExtra("guide", "vs");
                        intent.putExtra("newbe", true);
                        startActivityForResult(intent,1);
                        SharedPreferences.Editor spEditor;
                        spEditor = sp.edit();
                        spEditor.putBoolean("Guide_Bluetooth", true).commit();
                    }else {
                        BT_Setting = new ArrayList<>();
                        BT_Setting.add(getString(R.string.bt_setting_server));
                        BT_Setting.add(getString(R.string.bt_setting_client));
                        new AlertDialog.Builder(Index.this)
                                .setItems(BT_Setting.toArray(new String[BT_Setting.size()]), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0) {
                                            intent = new Intent(Index.this, Bluetooth.class);
                                            intent.putExtra("IsServer", true);
                                            startActivity(intent);
                                        } else {
                                            intent = new Intent(Index.this, Bluetooth.class);
                                            intent.putExtra("IsServer", false);
                                            startActivity(intent);
                                        }
                                    }
                                })
                                .show();
                    }
                    break;
                case R.id.index_4:
                    intent = new Intent(Index.this, GameSetting.class);
                    startActivity(intent);
                    break;
                case R.id.index_5:
                    intent = new Intent(Index.this, GameGuide.class);
                    startActivity(intent);
                    break;
                case R.id.index_6:
                    try {
                        ShowUpdate(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    public class TouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    view.setAlpha((float) 0.6);
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    view.setAlpha((float) 1.0);
                }
            }
            return false;
        }
    }

    public void ShowUpdate(boolean forceShow) throws IOException {
        SharedPreferences sp;
        sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
        if (sp.getBoolean("show_update", true) || forceShow) {
            InputStream is;
            is = Common.getInputStream("update.txt", getApplicationContext());
            InputStreamReader reader = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(reader);
            String str = "", update_msg = "";
            while (br.ready()) {
                str = br.readLine();
                if (str == null) {
                    continue;
                }
                update_msg += str + "\n";
            }
            is.close();
            reader.close();
            br.close();
            LayoutInflater inflater = LayoutInflater.from(this);
            final View update_view = inflater.inflate(R.layout.form_system_update,null);
            ((TextView)update_view.findViewById(R.id.FormSystemUpdate_TextView)).setText(update_msg);
            ((WebView) update_view.findViewById(R.id.Update_WV)).loadUrl("file:///android_asset/icons.html");
            update_view.findViewById(R.id.Update_WV).setBackgroundColor(Color.TRANSPARENT);
            if(forceShow){
                update_view.findViewById(R.id.FormSystemUpdate_CheckBox).setVisibility(View.GONE);
                new AlertDialog.Builder(Index.this)
                        .setTitle(R.string.index_about)
                        .setView(update_view)
                        .show();
            }else{
                new AlertDialog.Builder(Index.this)
                        .setTitle(R.string.index_about)
                        .setView(update_view)
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (((CheckBox)update_view.findViewById(R.id.FormSystemUpdate_CheckBox)).isChecked()) {
                                    SharedPreferences sp;
                                    sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
                                    SharedPreferences.Editor spEditor;
                                    spEditor = sp.edit();
                                    spEditor.putBoolean("show_update", false).commit();
                                }
                            }
                        }).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            BT_Setting = new ArrayList<>();
            BT_Setting.add(getString(R.string.bt_setting_server));
            BT_Setting.add(getString(R.string.bt_setting_client));
            new AlertDialog.Builder(Index.this)
                    .setItems(BT_Setting.toArray(new String[BT_Setting.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                intent = new Intent(Index.this, Bluetooth.class);
                                intent.putExtra("IsServer", true);
                                startActivity(intent);
                            } else {
                                intent = new Intent(Index.this, Bluetooth.class);
                                intent.putExtra("IsServer", false);
                                startActivity(intent);
                            }
                        }
                    })
                    .show();
        }
    }
}
