package com.infinitewing.bombisland2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.infinitewing.bombisland2.GameObject.Common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class GameGuide extends Activity {
    public String guide = "game";
    public Boolean newbe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.SetFullScreen(getWindow());
        setContentView(R.layout.game_guide);
        newbe = getIntent().getBooleanExtra("newbe", false);
        guide = getIntent().getStringExtra("guide");
        if (!newbe) {
            guide = "game";
            findViewById(R.id.Guide_AI).setOnClickListener(new ClickListener());
            findViewById(R.id.Guide_Hero).setOnClickListener(new ClickListener());
            findViewById(R.id.Guide_Map).setOnClickListener(new ClickListener());
            findViewById(R.id.Guide_Bluetooth).setOnClickListener(new ClickListener());
            findViewById(R.id.Guide_Game).setOnClickListener(new ClickListener());
        } else {
            findViewById(R.id.Guide_TitleTV).setVisibility(View.GONE);
            findViewById(R.id.Guide_HeaderList).setVisibility(View.GONE);
        }
        findViewById(R.id.Guide_Leave).setOnClickListener(new ClickListener());
        findViewById(R.id.Guide_Leave).setVisibility(View.VISIBLE);
        LoadWebView();
    }

    public void LoadWebView() {
        switch (guide) {
            case "ai":
                break;
            case "hero":
                break;
            case "map":
                break;
            case "vs":
                break;
            case "game":
            default:
                guide = "game";
                break;
        }
        String language = Locale.getDefault().getLanguage();
        if (language.equals("zh")) {
            ((WebView) findViewById(R.id.Guide_WV)).loadUrl("file:///android_asset/guide/" + guide + ".html");
        } else {
            ((WebView) findViewById(R.id.Guide_WV)).loadUrl("file:///android_asset/guide/en/" + guide + ".html");
        }
    }

    public class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.Guide_AI:
                    guide = "ai";
                    break;
                case R.id.Guide_Hero:
                    guide = "hero";
                    break;
                case R.id.Guide_Map:
                    guide = "map";
                    break;
                case R.id.Guide_Bluetooth:
                    guide = "vs";
                    break;
                case R.id.Guide_Game:
                    guide = "game";
                    break;
                case R.id.Guide_Leave:
                    GameGuide.this.finish();
                    return;
            }
            LoadWebView();
        }
    }

    @Override
    protected void onResume() {
        Common.SetFullScreen(getWindow());
        super.onResume();
    }
}
