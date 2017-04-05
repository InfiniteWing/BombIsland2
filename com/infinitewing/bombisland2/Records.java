package com.infinitewing.bombisland2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodSession;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.event.Event;
import com.google.android.gms.games.event.Events;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.quest.Quests;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotContents;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;
import com.infinitewing.bombisland2.GameObject.Common;
import com.infinitewing.bombisland2.GameObject.Player;
import com.infinitewing.bombisland2.GameObject.Recorder;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.Vector;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.*;


public class Records extends Activity {
    public String hero;
    public GoogleApiClient mGoogleApiClient;
    public View nowLoadingView;
    public AlertDialog nowLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.SetFullScreen(getWindow());
        setContentView(R.layout.records);
        SharedPreferences sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
        hero = sp.getString("Last_Pick_Hero", "ai01");
        Player player = new Player(hero, getApplicationContext());
        LayoutInflater inflater = LayoutInflater.from(Records.this);
        nowLoadingView = inflater.inflate(R.layout.form_nowloading, null);
        nowLoadingDialog = new AlertDialog.Builder(Records.this)
                .setView(nowLoadingView).create();
        nowLoadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        nowLoadingDialog.setCanceledOnTouchOutside(false);
        nowLoadingDialog.show();
        findViewById(R.id.Records_Back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Records.this.finish();
            }
        });
        ImageView iv = (ImageView) findViewById(R.id.Records_PlayerIV);
        iv.setImageBitmap(player.character.img);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        LoadRecords();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        nowLoadingDialog.hide();
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        if (connectionResult.hasResolution()) {
                            try {
                                connectionResult.startResolutionForResult(Records.this, 10);
                            } catch (IntentSender.SendIntentException e) {
                                e.getCause();
                                nowLoadingDialog.hide();
                            }
                        }
                    }
                })
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                .build();
        mGoogleApiClient.connect();
    }

    public void LoadRecords() {
        /*
         *  Total 9 records at 2.50 ver.
         *  0->totalBomb    1->totalKill   2->totalItem    3->totalMount    4->totalSave=0
         *  5->totalMove    6->totalTime    7->totalWin     8->totalGame
         */
        try {
            EventCallback ec = new EventCallback();
            com.google.android.gms.common.api.PendingResult<Events.LoadEventsResult>
                    pr = Games.Events.load(mGoogleApiClient, true);
            pr.setResultCallback(ec);
        } catch (Exception e) {
            e.getCause();
            nowLoadingDialog.hide();
        }
    }

    public String round(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return twoDForm.format(d);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 10 && requestCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }

    public class EventCallback implements ResultCallback {
        // Handle the results from the events load call
        public void onResult(com.google.android.gms.common.api.Result result) {
            Events.LoadEventsResult r = (Events.LoadEventsResult) result;
            com.google.android.gms.games.event.EventBuffer eb = r.getEvents();
            long totalWin = 0, totalGame = 0;
            for (int i = 0; i < eb.getCount(); i++) {
                Event event = eb.get(i);
                if (event.getEventId().equals(Common.getStringResourceByName("event_total_bombs", getApplicationContext()))) {
                    TextView tv = (TextView) findViewById(R.id.Records_TotalBombTV);
                    tv.setText(String.valueOf(event.getValue()));
                }
                if (event.getEventId().equals(Common.getStringResourceByName("event_total_destroy", getApplicationContext()))) {
                    TextView tv = (TextView) findViewById(R.id.Records_TotalKillTV);
                    tv.setText(String.valueOf(event.getValue()));
                }
                if (event.getEventId().equals(Common.getStringResourceByName("event_total_items", getApplicationContext()))) {
                    TextView tv = (TextView) findViewById(R.id.Records_TotalItemTV);
                    tv.setText(String.valueOf(event.getValue()));
                }
                if (event.getEventId().equals(Common.getStringResourceByName("event_total_mount", getApplicationContext()))) {
                    TextView tv = (TextView) findViewById(R.id.Records_TotalMountTV);
                    tv.setText(String.valueOf(event.getValue()));
                }
                if (event.getEventId().equals(Common.getStringResourceByName("event_total_saves", getApplicationContext()))) {
                    TextView tv = (TextView) findViewById(R.id.Records_TotalSaveTV);
                    tv.setText(String.valueOf(event.getValue()));
                }
                if (event.getEventId().equals(Common.getStringResourceByName("event_total_distances", getApplicationContext()))) {
                    TextView tv = (TextView) findViewById(R.id.Records_TotalMoveTV);
                    tv.setText(String.valueOf(event.getValue()));
                }
                if (event.getEventId().equals(Common.getStringResourceByName("event_total_exp", getApplicationContext()))) {
                    TextView tv = (TextView) findViewById(R.id.Records_TotalExpTV);
                    tv.setText(String.valueOf(event.getValue()));
                }
                if (event.getEventId().equals(Common.getStringResourceByName("event_total_endless_game", getApplicationContext()))) {
                    TextView tv = (TextView) findViewById(R.id.Records_TotalEndlessTV);
                    tv.setText(String.valueOf(event.getValue()));
                }
                if (event.getEventId().equals(Common.getStringResourceByName("event_total_times", getApplicationContext()))) {
                    TextView tv = (TextView) findViewById(R.id.Records_TotalTimesTV);
                    tv.setText(String.valueOf(event.getValue() / 3600) + ":" + String.format("%02d", event.getValue() % 3600 / 60) + ":" + String.format("%02d", event.getValue() % 60));
                }
                if (event.getEventId().equals(Common.getStringResourceByName("event_total_win", getApplicationContext()))) {
                    TextView tv = (TextView) findViewById(R.id.Records_TotalWinsTV);
                    tv.setText(String.valueOf(event.getValue()));
                    totalWin = event.getValue();
                }
                if (event.getEventId().equals(Common.getStringResourceByName("event_total_games", getApplicationContext()))) {
                    TextView tv = (TextView) findViewById(R.id.Records_TotalGamesTV);
                    tv.setText(String.valueOf(event.getValue()));
                    totalGame = event.getValue();
                }
            }
            TextView tv = (TextView) findViewById(R.id.Records_TotalWinrateTV);
            if (totalGame > 0) {
                tv.setText(round(100 * totalWin / totalGame) + "%");
            } else {
                tv.setText("Null");
            }
            eb.close();
            nowLoadingDialog.hide();
        }
    }

    @Override
    protected void onResume() {
        Common.SetFullScreen(getWindow());
        super.onResume();
    }
}
