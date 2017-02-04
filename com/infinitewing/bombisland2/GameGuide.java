package com.infinitewing.bombisland2;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class GameGuide extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_guide);
        ((WebView) findViewById(R.id.Guide_WV)).loadUrl("file:///android_asset/guide/guide.html");
    }
}
