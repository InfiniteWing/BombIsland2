package com.infinitewing.bombisland2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;
import com.infinitewing.bombisland2.GameObject.Common;
import com.infinitewing.bombisland2.GameObject.Recorder;
import com.infinitewing.bombisland2.GameObject.Story;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * Created by InfiniteWing on 2016/8/18.
 */
public class Index extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private Intent intent;
    private Resources res;
    public final int RATING_BOMBISLAND2 = 7997, INSTALL_GOOGLE_PLAY_GMS = 7998, SHOW_BLUETOOTH_GUIDE = 7999, GOOGLE_LOAD_RESOLUTION = 8000, GOOGLE_REQUEST_ACHIEVEMENTS = 8001,
            GOOGLE_REQUEST_LEADERBOARD = 8002, ENDLESS_GAME = 8888;
    public final int REQUEST_ACCESS_COARSE_LOCATION_PERMISSION = 9200;
    public GoogleApiClient mGoogleApiClient;
    List<String> arrayList;
    public String mCurrentSaveName = "snapshotTemp";
    public String mSaveGameData;
    public Boolean NeedToSleepForWelcome = false, HadShowSignIn = false, HadShowInstall = false,
            HadGoogleRecord = false;
    public View nowLoadingView;
    public AlertDialog nowLoadingDialog;
    public int endlessAiCount = 3;
    public String SignIn_Method = "Local";

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.SetFullScreen(getWindow());
        setContentView(R.layout.index);
        res = getResources();
        if (getIntent() != null) {
            SignIn_Method = getIntent().getStringExtra("SignIn_Method");
        }
        if (SignIn_Method == null) {
            SignIn_Method = "Local";
        }
        findViewById(R.id.index_1).setOnClickListener(new ClickListener());
        findViewById(R.id.index_2).setOnClickListener(new ClickListener());
        findViewById(R.id.index_3).setOnClickListener(new ClickListener());
        findViewById(R.id.index_4).setOnClickListener(new ClickListener());
        findViewById(R.id.index_5).setOnClickListener(new ClickListener());
        findViewById(R.id.index_6).setOnClickListener(new ClickListener());
        findViewById(R.id.index_7).setOnClickListener(new ClickListener());
        findViewById(R.id.index_8).setOnClickListener(new ClickListener());
        findViewById(R.id.index_9).setOnClickListener(new ClickListener());
        findViewById(R.id.index_10).setOnClickListener(new ClickListener());
        findViewById(R.id.index_11).setOnClickListener(new ClickListener());
        findViewById(R.id.index_12).setOnClickListener(new ClickListener());
        findViewById(R.id.index_13).setOnClickListener(new ClickListener());
        LayoutInflater inflater = LayoutInflater.from(Index.this);
        nowLoadingView = inflater.inflate(R.layout.form_nowloading, null);
        nowLoadingDialog = new AlertDialog.Builder(Index.this)
                .setView(nowLoadingView).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Common.SetFullScreen(getWindow());
                    }
                }).create();
        nowLoadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        nowLoadingDialog.setCanceledOnTouchOutside(false);
        if (SignIn_Method.equals("Google")) {
            InitGoogleApiClient();
        }
    }

    public void InitGoogleApiClient() {
        String GooglePlayGames = "com.google.android.play.games";
        SharedPreferences sp;
        sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
        HadShowInstall = sp.getBoolean("HadShowInstall", false);
        if (!CheckAppInstalled(GooglePlayGames) && HadShowInstall) {
            Toast.makeText(getApplicationContext(), R.string.index_google_play_game_alert, Toast.LENGTH_LONG).show();
        }
        if (!CheckAppInstalled(GooglePlayGames) && !HadShowInstall) {
            HadShowInstall = true;

            SharedPreferences.Editor spEditor;
            spEditor = sp.edit();
            spEditor.putBoolean("HadShowInstall", HadShowInstall).commit();

            Toast.makeText(getApplicationContext(), R.string.index_google_play_game_alert, Toast.LENGTH_LONG).show();
            try {
                // Open app with Google Play app
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + GooglePlayGames));
                startActivityForResult(intent, INSTALL_GOOGLE_PLAY_GMS);
            } catch (android.content.ActivityNotFoundException anfe) {
                // Open Google Play website
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + GooglePlayGames));
                startActivityForResult(intent, INSTALL_GOOGLE_PLAY_GMS);
            }
        } else {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            if (NeedToSleepForWelcome) {
                                LoadGameFromGoogle();
                                if (nowLoadingDialog != null) {
                                    Common.SetAlertDialog(nowLoadingDialog);
                                }
                            } else {
                                try {
                                    ShowUpdate(false);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
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
                            if (connectionResult.hasResolution() && !HadShowSignIn) {
                                NeedToSleepForWelcome = true;
                                HadShowSignIn = true;
                                try {
                                    connectionResult.startResolutionForResult(Index.this, GOOGLE_LOAD_RESOLUTION);
                                } catch (IntentSender.SendIntentException e) {
                                    e.getCause();
                                }
                            } else if (HadShowSignIn) {
                                Toast.makeText(getApplicationContext(), R.string.index_google_signin_error, Toast.LENGTH_SHORT).show();
                                try {
                                    mGoogleApiClient.disconnect();
                                } catch (Exception e) {
                                    e.getCause();
                                }
                                try {
                                    ShowUpdate(false);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    })
                    .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                    .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                    .build();
            mGoogleApiClient.connect();
        }
    }


    public class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.index_1:
                    intent = new Intent(Index.this, StoryIndex.class);
                    startActivityForResult(intent, 1000);
                    break;
                case R.id.index_2:
                    intent = new Intent(Index.this, GameVersusAi.class);
                    startActivityForResult(intent, 1000);
                    break;
                case R.id.index_3:
                    SharedPreferences sp;
                    sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
                    if (!sp.getBoolean("Guide_Bluetooth", false)) {
                        Intent intent = new Intent(Index.this, GameGuide.class);
                        intent.putExtra("guide", "vs");
                        intent.putExtra("newbe", true);
                        startActivityForResult(intent, SHOW_BLUETOOTH_GUIDE);
                        SharedPreferences.Editor spEditor;
                        spEditor = sp.edit();
                        spEditor.putBoolean("Guide_Bluetooth", true).commit();
                    } else {
                        Boolean ACCESS_COARSE_LOCATION = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                        if (!ACCESS_COARSE_LOCATION) {
                            Request_ACCESS_COARSE_LOCATION_PERMISSION();
                        } else {
                            ShowBTGame();
                        }
                    }
                    break;
                case R.id.index_4:
                    intent = new Intent(Index.this, GameSetting.class);
                    startActivityForResult(intent, 1000);
                    break;
                case R.id.index_5:
                    intent = new Intent(Index.this, GameGuide.class);
                    startActivityForResult(intent, 1000);
                    break;
                case R.id.index_6:
                    try {
                        ShowUpdate(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.index_7:
                    intent = new Intent(Index.this, Store.class);
                    startActivityForResult(intent, 1000);
                    break;
                case R.id.index_8:
                    HadShowSignIn = false;
                    if (mGoogleApiClient == null) {
                        InitGoogleApiClient();
                    } else {
                        if (!mGoogleApiClient.isConnected()) {
                            mGoogleApiClient.connect();
                        } else {
                            intent = new Intent(Index.this, Records.class);
                            startActivityForResult(intent, 1000);
                        }
                    }
                    break;
                case R.id.index_9:
                    HadShowSignIn = false;
                    try {
                        if (mGoogleApiClient == null) {
                            InitGoogleApiClient();
                        } else {
                            if (!mGoogleApiClient.isConnected()) {
                                mGoogleApiClient.connect();
                            } else {
                                startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), GOOGLE_REQUEST_ACHIEVEMENTS);
                            }
                        }
                    } catch (Exception e) {
                        InitGoogleApiClient();
                    }
                    break;
                case R.id.index_10:
                    HadShowSignIn = false;
                    try {
                        if (mGoogleApiClient == null) {
                            InitGoogleApiClient();
                        } else {
                            if (!mGoogleApiClient.isConnected()) {
                                mGoogleApiClient.connect();
                            } else {
                                startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient), GOOGLE_REQUEST_LEADERBOARD);
                            }
                        }
                    } catch (Exception e) {
                        InitGoogleApiClient();
                    }
                    break;
                case R.id.index_11:
                    //EndLess Mode
                    arrayList = new ArrayList<>();
                    arrayList.add(getString(R.string.game_endless_easy));
                    arrayList.add(getString(R.string.game_endless_normal));
                    arrayList.add(getString(R.string.game_endless_hard));
                    AlertDialog alertDialog = new AlertDialog.Builder(Index.this)
                            .setItems(arrayList.toArray(new String[arrayList.size()]), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        endlessAiCount = 3;
                                    } else if (which == 1) {
                                        endlessAiCount = 5;
                                    } else {
                                        endlessAiCount = 7;
                                    }
                                    StartEndlessGame();
                                }
                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    Common.SetFullScreen(getWindow());
                                }
                            })
                            .create();
                    Common.SetAlertDialog(alertDialog);
                    break;
                case R.id.index_12:
                    Index.this.finish();
                    break;
                case R.id.index_13:
                    alertDialog = new AlertDialog.Builder(Index.this)
                            .setTitle(R.string.index_rating)
                            .setMessage(R.string.index_rating_hint)
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    Common.SetFullScreen(getWindow());
                                }
                            })
                            .setPositiveButton(R.string.index_rating_submit, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        // Open app with Google Play app
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Common.APP_GOOGLE_PLAY_UID));
                                        startActivityForResult(intent, RATING_BOMBISLAND2);
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        // Open Google Play website
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + Common.APP_GOOGLE_PLAY_UID));
                                        startActivityForResult(intent, RATING_BOMBISLAND2);
                                    }
                                    Common.SetFullScreen(getWindow());
                                }
                            })
                            .setNegativeButton(R.string.index_rating_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Common.SetFullScreen(getWindow());
                                }
                            })
                            .create();
                    Common.SetAlertDialog(alertDialog);
                    break;
            }
        }
    }

    public void StartEndlessGame() {
        Intent intent = new Intent(Index.this, GameMain.class);
        intent.putExtra("hero", "hero10");
        intent.putExtra("map", "bombisland01");
        intent.putExtra("endless_mode", true);
        intent.putExtra("ai_count", endlessAiCount);
        String ai_info = "";
        boolean c = false;
        String aiPool[] = {"ai01", "ai02", "ai03", "ai04", "ai05"};
        for (int i = 0; i < endlessAiCount; i++) {
            if (c) {
                ai_info += ",";
            }
            int random = Common.RandomNum(1000);
            ai_info += aiPool[random % aiPool.length];
            c = true;
        }
        intent.putExtra("ai_info", ai_info);
        startActivityForResult(intent, ENDLESS_GAME);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ShowBTGame();
                }
                return;
            }

        }
    }

    public void Request_ACCESS_COARSE_LOCATION_PERMISSION() {
        ActivityCompat.requestPermissions(Index.this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQUEST_ACCESS_COARSE_LOCATION_PERMISSION);
    }

    public void ShowBTGame() {
        arrayList = new ArrayList<>();
        arrayList.add(getString(R.string.bt_setting_server));
        arrayList.add(getString(R.string.bt_setting_client));
        AlertDialog alertDialog = new AlertDialog.Builder(Index.this)
                .setItems(arrayList.toArray(new String[arrayList.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            intent = new Intent(Index.this, Bluetooth.class);
                            intent.putExtra("IsServer", true);
                            startActivityForResult(intent, 1000);
                        } else {
                            intent = new Intent(Index.this, Bluetooth.class);
                            intent.putExtra("IsServer", false);
                            startActivityForResult(intent, 1000);
                        }
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Common.SetFullScreen(getWindow());
                    }
                })
                .create();
        Common.SetAlertDialog(alertDialog);
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
            final View update_view = inflater.inflate(R.layout.form_system_update, null);
            ((TextView) update_view.findViewById(R.id.FormSystemUpdate_TextView)).setText(update_msg);
            ((WebView) update_view.findViewById(R.id.Update_WV)).loadUrl("file:///android_asset/icons.html");
            update_view.findViewById(R.id.Update_WV).setBackgroundColor(Color.TRANSPARENT);
            AlertDialog alertDialog;
            if (forceShow) {
                update_view.findViewById(R.id.FormSystemUpdate_CheckBox_Layout).setVisibility(View.GONE);
                update_view.findViewById(R.id.FormSystemUpdate_SV).setPadding(
                        Common.DP2PX(20, getApplicationContext()),
                        Common.DP2PX(0, getApplicationContext()),
                        Common.DP2PX(20, getApplicationContext()),
                        Common.DP2PX(0, getApplicationContext())
                );
                alertDialog = new AlertDialog.Builder(Index.this)
                        .setTitle(R.string.index_about)
                        .setView(update_view)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                Common.SetFullScreen(getWindow());
                            }
                        }).setPositiveButton(R.string.index_copyright_ok, null)
                        .create();
            } else {
                alertDialog = new AlertDialog.Builder(Index.this)
                        .setTitle(R.string.index_about)
                        .setView(update_view)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                Common.SetFullScreen(getWindow());
                            }
                        })
                        .setPositiveButton(R.string.index_copyright_confirm, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (((CheckBox) update_view.findViewById(R.id.FormSystemUpdate_CheckBox)).isChecked()) {
                                    SharedPreferences sp;
                                    sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
                                    SharedPreferences.Editor spEditor;
                                    spEditor = sp.edit();
                                    spEditor.putBoolean("show_update", false).commit();
                                }
                            }
                        }).create();
            }
            Common.SetAlertDialog(alertDialog);
        }
    }

    public void LoadGameFromGoogle() {
        mCurrentSaveName = Common.APP_GOOGLE_UID;
        HadGoogleRecord = false;
        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                // Open the saved game using its name.
                Snapshots.OpenSnapshotResult result = Games.Snapshots.open(mGoogleApiClient,
                        mCurrentSaveName, true).await();
                Snapshot snapshot = Common.ProcessSnapshotOpenResult(mGoogleApiClient, result, 0);
                if(snapshot!=null) {
                    try {
                        mSaveGameData = new String(snapshot.getSnapshotContents().readFully());
                        HadGoogleRecord = true;
                    } catch (IOException e) {
                        e.getCause();
                    }
                }
                return result.getStatus().getStatusCode();
            }

            @Override
            protected void onPostExecute(Integer status) {
                if (HadGoogleRecord && !mSaveGameData.equals("")) {
                    Common.LoadSaveData(mSaveGameData, getApplicationContext());
                }
                Toast.makeText(getApplicationContext(), R.string.index_save_load_success, Toast.LENGTH_SHORT).show();
                Common.SetFullScreen(getWindow());
                nowLoadingDialog.hide();
                try {
                    ShowUpdate(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        task.execute();
    }

    @Override
    protected void onResume() {
        Common.SetFullScreen(getWindow());
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Common.SetFullScreen(getWindow());
        System.gc();
        if (requestCode == SHOW_BLUETOOTH_GUIDE) {
            Boolean ACCESS_COARSE_LOCATION = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (!ACCESS_COARSE_LOCATION) {
                Request_ACCESS_COARSE_LOCATION_PERMISSION();
            } else {
                ShowBTGame();
            }
        } else if (requestCode == GOOGLE_LOAD_RESOLUTION) {
            mGoogleApiClient.connect();
        } else if (requestCode == ENDLESS_GAME) {
            try {
                Common.SaveGameToGoogle(mGoogleApiClient, getApplicationContext());
            } catch (Exception e) {
                e.getCause();
            }
        }
    }


    private boolean CheckAppInstalled(String uri) {
        PackageManager pm = getPackageManager();
        try {
            if (pm.getApplicationInfo(uri, 0).enabled) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.getCause();
        }

        return false;
    }

}
