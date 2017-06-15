package com.infinitewing.bombisland2.GameObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;
import com.infinitewing.bombisland2.GameView;
import com.infinitewing.bombisland2.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Random;

/**
 * Created by InfiniteWing on 2016/8/18.
 */
public class Common {
    public static final int GAME_REFRESH = 40, GAME_BT_REFRESH = 3, GAME_WIDTH_UNIT = 20, GAME_HEIGHT_UNIT = 12, MAP_WIDTH_UNIT = 16, MAP_HEIGHT_UNIT = 12;
    public static GameView gameView;
    public static final int BLUR_FRAME=6;
    public static int SCREEN_WIDTH = 1024, SCREEN_HEIGHT = 576, OLD_SCREEN_WIDTH, OLD_SCREEN_HEIGHT, PLAYER_POSITION_RATE = 1000;
    public static int DEFAULT_MONEY = 300000;
    public static String APP_UUID = "00001101-0000-1000-8000-00805f9b34fb", APP_NAME = "BombIsland2";
    public static String APP_GOOGLE_UID = "BombIsland2-26192288328aa4c9954a7982628244c1b84b229c5400caba72ab57273b61b464462b06546785";
    public static String APP_GOOGLE_PLAY_UID = "com.infinitewing.bombisland2";
    public static String STORY_UNITY = "unitychan", DEFAULT_BOMBSKIN = "bomb1";
    public static int MAX_SNAPSHOT_RESOLVE_RETRIES = 10;

    public static String STORE_ITEM_REVIVE = "CashItem_revive",
            STORE_ITEM_SHIELD = "CashItem_shield",
            STORE_BOMBSKIN_YELLOW = "BombSkin_bomb_yellow",
            STORE_BOMBSKIN_PURPLE = "BombSkin_bomb_purple",
            STORE_BOMBSKIN_BLUE = "BombSkin_bomb_blue",
            STORE_BOMBSKIN_RED = "BombSkin_bomb_red";

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
            /*
             *  4.22版更新
             *  盡量避免OOM，所以現在都直接改成較低顏色值繪圖ARGB_4444模式
             *
            if (sp.getBoolean("bitmapOpt", false)) {
                op.inPreferredConfig = Bitmap.Config.ARGB_4444;
            }
            */
            op.inPreferredConfig = Bitmap.Config.ARGB_4444;
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

    public static Bitmap RotateBitmap(Bitmap img, Context c) {
        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1);
        Bitmap img2 = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, false);
        img.recycle();
        return img2;
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

        boolean BGM, effSound, effVibrator, FPS, bitmapOpt, Guide_Bluetooth, Guide_Game,
                BombSkinYellow, BombSkinBlue, BombSkinRed, BombSkinPurple;
        int controlMode, resolutionMode, StoryStage_UnityChan, CashItemRevive, CashItemShield;
        String hero, bombSkin;
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
        StoryStage_UnityChan = sp.getInt(Common.STORY_UNITY, 0);
        CashItemRevive = sp.getInt(Common.STORE_ITEM_REVIVE, 0);
        CashItemShield = sp.getInt(Common.STORE_ITEM_SHIELD, 0);
        BombSkinYellow = sp.getBoolean(Common.STORE_BOMBSKIN_YELLOW, false);
        BombSkinBlue = sp.getBoolean(Common.STORE_BOMBSKIN_BLUE, false);
        BombSkinRed = sp.getBoolean(Common.STORE_BOMBSKIN_RED, false);
        BombSkinPurple = sp.getBoolean(Common.STORE_BOMBSKIN_PURPLE, false);
        bombSkin = sp.getString("bombSkin", Common.DEFAULT_BOMBSKIN);
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
                String.valueOf(resolutionMode) + "," +
                /*
                 *  3.80版後新增UnityChan劇情
                 *  StoryStage_UnityChan
                 */
                String.valueOf(StoryStage_UnityChan) + "," +
                /*
                 *  4.20版後新增水球造型、點數道具
                 *  CashItemRevive      [11]
                 *  CashItemShield      [12]
                 *  BombSkinYellow      [13]
                 *  BombSkinBlue        [14]
                 *  BombSkinRed         [15]
                 *  BombSkinPurple      [16]
                 *  bombSkin            [17]    設定使用的炸彈造型
                 */
                String.valueOf(CashItemRevive) + "," +
                String.valueOf(CashItemShield) + "," +
                String.valueOf(BombSkinYellow) + "," +
                String.valueOf(BombSkinBlue) + "," +
                String.valueOf(BombSkinRed) + "," +
                String.valueOf(BombSkinPurple) + "," +
                bombSkin;
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
            boolean BGM, effSound, effVibrator, FPS, bitmapOpt, Guide_Bluetooth, Guide_Game,
                    BombSkinYellow, BombSkinBlue, BombSkinRed, BombSkinPurple;
            int controlMode, resolutionMode, StoryStage_UnityChan, CashItemRevive, CashItemShield;
            String hero, bombSkin;
            BGM = Boolean.valueOf(settingRecords[0]);
            effSound = Boolean.valueOf(settingRecords[1]);
            effVibrator = Boolean.valueOf(settingRecords[2]);
            FPS = Boolean.valueOf(settingRecords[3]);
            bitmapOpt = Boolean.valueOf(settingRecords[4]);
            controlMode = Integer.parseInt(settingRecords[5]);
            hero = settingRecords[6];
            /*
             *  3.43版後新增
             *  Guide_Bluetooth         [7]
             *  Guide_Game              [8]
             *  resolutionMode          [9]
             */

            /*
             *  3.80版後新增UnityChan劇情
             *  StoryStage_UnityChan    [10]
             */

            /*
             *  4.20版後新增水球造型、點數道具
             *  CashItemRevive      [11]
             *  CashItemShield      [12]
             *  BombSkinYellow      [13]
             *  BombSkinBlue        [14]
             *  BombSkinRed         [15]
             *  BombSkinPurple      [16]
             *  bombSkin            [17]    設定使用的炸彈造型
             */
            if (settingRecords.length > 11) {
                try {
                    CashItemRevive = Integer.parseInt(settingRecords[11]);
                    CashItemShield = Integer.parseInt(settingRecords[12]);
                    BombSkinYellow = Boolean.valueOf(settingRecords[13]);
                    BombSkinBlue = Boolean.valueOf(settingRecords[14]);
                    BombSkinRed = Boolean.valueOf(settingRecords[15]);
                    BombSkinPurple = Boolean.valueOf(settingRecords[16]);
                    bombSkin = settingRecords[17];
                } catch (Exception e) {
                    e.getCause();
                    CashItemRevive = 0;
                    CashItemShield = 0;
                    BombSkinYellow = false;
                    BombSkinBlue = false;
                    BombSkinPurple = false;
                    BombSkinRed = false;
                    bombSkin = DEFAULT_BOMBSKIN;
                }
            } else {
                CashItemRevive = 0;
                CashItemShield = 0;
                BombSkinYellow = false;
                BombSkinBlue = false;
                BombSkinPurple = false;
                BombSkinRed = false;
                bombSkin = DEFAULT_BOMBSKIN;
            }

            if (settingRecords.length > 10) {
                try {
                    StoryStage_UnityChan = Integer.parseInt(settingRecords[10]);
                } catch (Exception e) {
                    e.getCause();
                    StoryStage_UnityChan = 0;
                }
            } else {
                StoryStage_UnityChan = 0;
            }
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
                    .putInt(Common.STORY_UNITY, StoryStage_UnityChan)
                    .putInt(Common.STORE_ITEM_REVIVE, CashItemRevive)
                    .putInt(Common.STORE_ITEM_SHIELD, CashItemShield)
                    .putBoolean(Common.STORE_BOMBSKIN_YELLOW, BombSkinYellow)
                    .putBoolean(Common.STORE_BOMBSKIN_BLUE, BombSkinBlue)
                    .putBoolean(Common.STORE_BOMBSKIN_RED, BombSkinRed)
                    .putBoolean(Common.STORE_BOMBSKIN_PURPLE, BombSkinPurple)
                    .putString("bombSkin", bombSkin)
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
        try {
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
        } catch (Exception e) {
            e.getCause();
        }
        return null;
    }

    public static void SaveGameToGoogle(final GoogleApiClient mGoogleApiClient, final Context c) {
        if (mGoogleApiClient == null) {
            return;
        }
        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                // Open the saved game using its name.
                String mSaveGameData = Common.GenerateSaveData(c);
                try {
                    Common.writeSnapshot(mGoogleApiClient, Common.APP_GOOGLE_UID, mSaveGameData.getBytes(),
                            BitmapFactory.decodeResource(c.getResources(), R.drawable.savebg),
                            Common.getStringResourceByName("app_name", c));
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

    public static void SetAlertDialog(AlertDialog alertDialog) {
        if (alertDialog == null) {
            return;
        }
        alertDialog.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        alertDialog.show();
        Common.SetFullScreen(alertDialog.getWindow());
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    public static int DP2PX(int dps, Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    public static String GetLanguagePrefix() {
        String language = Locale.getDefault().getLanguage();
        if (language.equals("zh")) {
            return "";
        } else {
            return "en/";
        }
    }

    public static Snapshot ProcessSnapshotOpenResult(GoogleApiClient mGoogleApiClient, Snapshots.OpenSnapshotResult result, int retryCount) {
        Snapshot mResolvedSnapshot;
        retryCount++;
        int status = result.getStatus().getStatusCode();
        if (status == GamesStatusCodes.STATUS_OK) {
            return result.getSnapshot();
        } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_CONTENTS_UNAVAILABLE) {
            return result.getSnapshot();
        } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_CONFLICT) {
            Snapshot snapshot = result.getSnapshot();
            Snapshot conflictSnapshot = result.getConflictingSnapshot();

            // Resolve between conflicts by selecting the newest of the conflicting snapshots.
            mResolvedSnapshot = snapshot;

            if (snapshot.getMetadata().getLastModifiedTimestamp() <
                    conflictSnapshot.getMetadata().getLastModifiedTimestamp()) {
                mResolvedSnapshot = conflictSnapshot;
            }

            Snapshots.OpenSnapshotResult resolveResult = Games.Snapshots.resolveConflict(
                    mGoogleApiClient, result.getConflictId(), mResolvedSnapshot).await();

            if (retryCount < MAX_SNAPSHOT_RESOLVE_RETRIES) {
                // Recursively attempt again
                return ProcessSnapshotOpenResult(mGoogleApiClient, resolveResult, retryCount);
            }
        }
        return null;
    }
}
