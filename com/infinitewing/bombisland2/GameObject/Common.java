package com.infinitewing.bombisland2.GameObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.View;
import android.view.Window;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;
import com.infinitewing.bombisland2.GameView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Created by InfiniteWing on 2016/8/18.
 */
public class Common {
    public static final int GAME_REFRESH = 40, GAME_BT_REFRESH = 3, GAME_WIDTH_UNIT = 20, GAME_HEIGHT_UNIT = 12, MAP_WIDTH_UNIT = 16, MAP_HEIGHT_UNIT = 12;
    public static GameView gameView;
    public static int SCREEN_WIDTH = 1024, SCREEN_HEIGHT = 576, OLD_SCREEN_WIDTH, OLD_SCREEN_HEIGHT, PLAYER_POSITION_RATE = 1000;
    public static int DEFAULT_MONEY = 1000000;
    public static String APP_UUID = "00001101-0000-1000-8000-00805f9b34fb", APP_NAME = "BombIsland2";
    public static String APP_GOOGLE_UID = "BombIsland2-26192288328aa4c9954a7982628244c1b84b229c5400caba72ab57273b61b464462b06546785";
    public static String APP_GOOGLE_PLAY_UID="com.infinitewing.bombisland2";
    public static void SetGameView(GameView gameView2) {
        gameView = gameView2;
    }

    public static final int RandomNum(int s) {
        Random random = new Random();
        return random.nextInt(s) + 1;
    }

    public static float transWidth(float w) {
        return w * Common.gameView.screenWidth / 1280;
    }

    public static float transHeight(float h) {
        return h * Common.gameView.screenHeight / 768;
    }

    public static float transFontSize(float s) {
        return s * Common.gameView.screenWidth / 1280;
    }

    public static InputStream getInputStream(String path, Context context) throws IOException {
        InputStream is = null;
        AssetManager am = context.getAssets();
        try {
            is = am.open(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return is;
    }

    public static AssetFileDescriptor getAssetsFileDescripter(String path) {
        AssetManager am = gameView.getContext().getAssets();
        try {
            return am.openFd(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static AssetFileDescriptor getAssetsFileDescripter(String path, Context c) {
        AssetManager am = c.getAssets();
        try {
            return am.openFd(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getBitmapFromAsset(String str) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inPreferredConfig = Bitmap.Config.ARGB_8888;
        if (gameView != null) {
            SharedPreferences sp = gameView.getContext().getSharedPreferences(Common.APP_NAME, gameView.getContext().MODE_PRIVATE);
            if (sp.getBoolean("bitmapOpt", false)) {
                op.inPreferredConfig = Bitmap.Config.ARGB_4444;
            }
        }
        AssetManager assetManager = gameView.getContext().getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, op);
        return bitmap;
    }

    public static Bitmap getBitmapFromAsset(String str, Context c) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inPreferredConfig = Bitmap.Config.ARGB_8888;
        if (gameView != null) {
            SharedPreferences sp = gameView.getContext().getSharedPreferences(Common.APP_NAME, gameView.getContext().MODE_PRIVATE);
            if (sp.getBoolean("bitmapOpt", false)) {
                op.inPreferredConfig = Bitmap.Config.ARGB_4444;
            }
        }
        AssetManager assetManager = c.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, op);
        return bitmap;
    }

    public static String getStringResourceByName(String stringTag, Context c) {
        String packageName = c.getPackageName();
        int resId = c.getResources().getIdentifier(stringTag, "string", packageName);
        return c.getString(resId) == null ? "" : c.getString(resId);
    }

    public static void SetFullScreen(Window w) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            w.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /*
     *  Save & Loads
     */

    public static String GenerateSaveData(Context c) {
        /*
         *  存檔之資料結構為：錢;地圖;角色;設定
         */
        String mSaveGameData = "";
        Recorder recorder = new Recorder(c.getApplicationContext());
        String file = "money.bl2";
        String moneyRecord = recorder.Read(file);
        if (moneyRecord != null) {
            mSaveGameData += moneyRecord;
        } else {
            mSaveGameData += String.valueOf(Common.DEFAULT_MONEY);
        }
        mSaveGameData += ";";
        file = "buyedMaps.bl2";
        String buyedMapRecords = recorder.Read(file);
        if (buyedMapRecords != null) {
            mSaveGameData += buyedMapRecords;
        }
        mSaveGameData += ";";
        file = "buyedHeros.bl2";
        String buyedHeroRecords = recorder.Read(file);
        if (buyedHeroRecords != null) {
            mSaveGameData += buyedHeroRecords;
        }
        mSaveGameData += ";";

        boolean BGM, effSound, effVibrator, FPS, bitmapOpt, Guide_Bluetooth, Guide_Game;
        int controlMode, resolutionMode;
        String hero;
        String settingRecords = "";
        SharedPreferences sp;
        sp = c.getSharedPreferences(Common.APP_NAME, c.MODE_PRIVATE);
        BGM = sp.getBoolean("BGM", true);
        effSound = sp.getBoolean("effSound", true);
        effVibrator = sp.getBoolean("effVibrator", true);
        FPS = sp.getBoolean("FPS", false);
        bitmapOpt = sp.getBoolean("bitmapOpt", false);
        controlMode = sp.getInt("controlMode", 0);
        hero = sp.getString("Last_Pick_Hero", "ai01");
        Guide_Bluetooth = sp.getBoolean("Guide_Bluetooth", false);
        Guide_Game = sp.getBoolean("Guide_Game", false);
        resolutionMode = sp.getInt("resolutionMode", 2);
        settingRecords += String.valueOf(BGM) + "," +
                String.valueOf(effSound) + "," +
                String.valueOf(effVibrator) + "," +
                String.valueOf(FPS) + "," +
                String.valueOf(bitmapOpt) + "," +
                String.valueOf(controlMode) + "," +
                hero + "," +
                /*
                 *  3.43版後新增
                 *  Guide_Bluetooth
                 *  Guide_Game
                 *  resolutionMode
                 */
                String.valueOf(Guide_Bluetooth) + "," +
                String.valueOf(Guide_Game) + "," +
                String.valueOf(resolutionMode);
        mSaveGameData += settingRecords;
        return mSaveGameData;
    }

    public static void LoadSaveData(String mSaveGameData, Context c) {
        try {
            String record[] = mSaveGameData.split(";");
            Recorder recorder = new Recorder(c.getApplicationContext());
            String file = "money.bl2";
            recorder.Write(record[0], file);
            file = "buyedMaps.bl2";
            recorder.Write(record[1], file);
            file = "buyedHeros.bl2";
            recorder.Write(record[2], file);

            String settingRecords[] = record[3].split(",");
            boolean BGM, effSound, effVibrator, FPS, bitmapOpt, Guide_Bluetooth, Guide_Game;
            int controlMode, resolutionMode;
            String hero;
            BGM = Boolean.valueOf(settingRecords[0]);
            effSound = Boolean.valueOf(settingRecords[1]);
            effVibrator = Boolean.valueOf(settingRecords[2]);
            FPS = Boolean.valueOf(settingRecords[3]);
            bitmapOpt = Boolean.valueOf(settingRecords[4]);
            controlMode = Integer.parseInt(settingRecords[5]);
            hero = settingRecords[6];
            if (settingRecords.length > 7) {
                try {
                    Guide_Bluetooth = Boolean.valueOf(settingRecords[7]);
                    Guide_Game = Boolean.valueOf(settingRecords[8]);
                    resolutionMode = Integer.parseInt(settingRecords[9]);
                } catch (Exception e) {
                    e.getCause();
                    Guide_Bluetooth = false;
                    Guide_Game = false;
                    resolutionMode = 2;
                }
            } else {
                Guide_Bluetooth = false;
                Guide_Game = false;
                resolutionMode = 2;
            }
            /*
             *  3.43版後新增
             *  Guide_Bluetooth
             *  Guide_Game
             *  resolutionMode
             */
            SharedPreferences sp;
            sp = c.getSharedPreferences(Common.APP_NAME, c.MODE_PRIVATE);
            SharedPreferences.Editor spEditor;
            spEditor = sp.edit();
            spEditor.putBoolean("BGM", BGM)
                    .putBoolean("effSound", effSound)
                    .putBoolean("effVibrator", effVibrator)
                    .putBoolean("FPS", FPS)
                    .putBoolean("bitmapOpt", bitmapOpt)
                    .putInt("controlMode", controlMode)
                    .putString("Last_Pick_Hero", hero)
                    .putBoolean("Guide_Bluetooth", Guide_Bluetooth)
                    .putBoolean("Guide_Game", Guide_Game)
                    .putInt("resolutionMode", resolutionMode)
                    .commit();
        } catch (Exception e) {
            e.getCause();
        }
    }

    public static PendingResult<Snapshots.CommitSnapshotResult> writeSnapshot(GoogleApiClient mGoogleApiClient, Snapshot snapshot,
                                                                              byte[] data, Bitmap coverImage, String desc) {

        // Set the data payload for the snapshot
        snapshot.getSnapshotContents().writeBytes(data);

        // Create the change operation
        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                .setCoverImage(coverImage)
                .setDescription(desc)
                .build();

        // Commit the operation
        return Games.Snapshots.commitAndClose(mGoogleApiClient, snapshot, metadataChange);
    }

    public static PendingResult<Snapshots.CommitSnapshotResult> writeSnapshot(GoogleApiClient mGoogleApiClient, String newSnapshotFilename,
                                                                              byte[] data, Bitmap coverImage, String desc) {

        Snapshots.OpenSnapshotResult result =
                Games.Snapshots.open(mGoogleApiClient, newSnapshotFilename, true).await();
// Check the result of the open operation
        if (result.getStatus().isSuccess()) {
            Snapshot snapshot = result.getSnapshot();
            snapshot.getSnapshotContents().writeBytes(data);

            // Create the change operation
            SnapshotMetadataChange metadataChange = new
                    SnapshotMetadataChange.Builder()
                    .setCoverImage(coverImage)
                    .setDescription(desc)
                    .build();

            // Commit the operation
            return Games.Snapshots.commitAndClose(mGoogleApiClient, snapshot, metadataChange);
        }
        return null;
    }
}
