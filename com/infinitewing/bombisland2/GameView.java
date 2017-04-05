package com.infinitewing.bombisland2;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.event.Event;
import com.google.android.gms.games.event.Events;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.infinitewing.bombisland2.GameObject.Ai;
import com.infinitewing.bombisland2.GameObject.Common;
import com.infinitewing.bombisland2.GameObject.Explosion;
import com.infinitewing.bombisland2.GameObject.FlyingShip;
import com.infinitewing.bombisland2.GameObject.Location;
import com.infinitewing.bombisland2.GameObject.Map;
import com.infinitewing.bombisland2.GameObject.MapObject;
import com.infinitewing.bombisland2.GameObject.Player;
import com.infinitewing.bombisland2.GameObject.Recorder;
import com.infinitewing.bombisland2.GameObject.SoundManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import javax.xml.transform.stream.StreamSource;

/**
 * Created by Administrator on 2016/8/19.
 */
public class GameView extends SurfaceView implements
        SurfaceHolder.Callback {
    public Context context;
    public Boolean BT_MODE = false;
    public Boolean IS_SERVER = false;
    public Boolean ENDLESS_MODE = false;
    public GameThread gameThread;
    public Map map;
    public Resources resources;
    public int screenWidth, screenHeight;
    public SoundManager soundManager;
    public Bluetooth.ConnectedThread connectedThread;
    public int gameTime = 0, BTgameTime = 0, getMoney = 0, aiCount = 0;
    public int FrameBalancer = 0, gameEndFrameCounter = 0;
    public boolean hadInitPlayer = false, hadInitBT = false, isGameStart = false, isGameEnd = false, isWin = false;
    public boolean hadSave = false, hadSaveGameEnd = false, hadPlayExplosion = false;
    public boolean isLoadingEvent = false, isLoadingEventSuccess = false;
    public String playerID, playerMoveStr = "";
    public MediaPlayer gamebackgroundsound;
    public double touchInitX, touchInitY, touchNowX, touchNowY;
    public boolean isControlDirection;
    private SharedPreferences sp;
    public boolean BGM, effSound, effVibrator, FPS, pressAddBomb;
    private int controlMode;
    public Vibrator vibrator;
    public float sleep;
    public GoogleApiClient mGoogleApiClient;
    public int AchievementUnlockCount = 0;
    public int getExp = 0;
    public int AiDeadCounter = 0;
    public String mSaveGameData;
    public Boolean HadPreesEnd = false;
    public Bitmap bg;

    public GameView(Context context, String playerID, String mapID, int aiCount, String aiInfo, MediaPlayer gamebackgroundsound, Boolean endlessMode) {
        super(context);
        this.context = context;
        Common.SetGameView(this);
        ENDLESS_MODE = endlessMode;
        vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        sp = context.getSharedPreferences(Common.APP_NAME, context.MODE_PRIVATE);
        BGM = sp.getBoolean("BGM", true);
        effSound = sp.getBoolean("effSound", true);
        effVibrator = sp.getBoolean("effVibrator", true);
        FPS = sp.getBoolean("FPS", false);
        controlMode = sp.getInt("controlMode", 0);

        this.gamebackgroundsound = gamebackgroundsound;
        screenWidth = Common.SCREEN_WIDTH;
        screenHeight = Common.SCREEN_HEIGHT;
        this.getHolder().setFixedSize(screenWidth, screenHeight);
        this.getHolder().addCallback(this);
        resources = this.getResources();
        Common.gameView = this;
        soundManager = new SoundManager();
        soundManager.start();
        BT_MODE = false;
        IS_SERVER = false;
        gameThread = new GameThread(this, this.getHolder());
        this.aiCount = aiCount;
        map = new Map(mapID, playerID, aiCount, aiInfo);
        this.playerID = playerID;
    }

    public GameView(Context context, String playerID1, String playerID2, Boolean BT_MODE, Boolean IS_SERVER, String mapID, int PlayerIndex, MediaPlayer gamebackgroundsound) {
        super(context);
        this.context = context;
        Common.SetGameView(this);

        vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        sp = context.getSharedPreferences(Common.APP_NAME, context.MODE_PRIVATE);
        BGM = sp.getBoolean("BGM", true);
        effSound = sp.getBoolean("effSound", true);
        effVibrator = sp.getBoolean("effVibrator", true);
        FPS = sp.getBoolean("FPS", false);
        controlMode = sp.getInt("controlMode", 0);
        this.gamebackgroundsound = gamebackgroundsound;
        screenWidth = Common.SCREEN_WIDTH;
        screenHeight = Common.SCREEN_HEIGHT;
        this.getHolder().setFixedSize(screenWidth, screenHeight);
        this.getHolder().addCallback(this);
        resources = this.getResources();
        Common.gameView = this;
        soundManager = new SoundManager();
        soundManager.start();
        gameThread = new GameThread(this, this.getHolder());
        map = new Map(mapID, playerID1, playerID2, BT_MODE, IS_SERVER, PlayerIndex);
        this.BT_MODE = BT_MODE;
        this.IS_SERVER = IS_SERVER;
        if (IS_SERVER) {
            this.playerID = playerID1 + "_" + String.valueOf(PlayerIndex);
        } else {
            this.playerID = playerID2 + "_" + String.valueOf(PlayerIndex);
        }
    }

    public void StartVibrator() {
        StartVibrator(300);
    }

    public void StartVibrator(int ms) {
        if (effVibrator) {
            vibrator.vibrate(ms);
        }
    }

    public void StartBGM() {
        if (BGM) {
            /*
            3.42 板後先將不同地圖有不同BGM的特色移除，避免檔案過大導致下載人數減少
            String filename = "sound/bgm/" + map.BGM;
            AssetFileDescriptor afd = Common.getAssetsFileDescripter(filename);
            try {
                gamebackgroundsound.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                gamebackgroundsound.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
            gamebackgroundsound = MediaPlayer.create(context, R.raw.bgm);
            gamebackgroundsound.setVolume(0.3f, 0.3f);
            gamebackgroundsound.setLooping(true);
            gamebackgroundsound.start();

        }
        isGameStart = true;
    }

    public void GameEndSignal() {
        map.Release();
        if (!BT_MODE) {
            Intent intent = new Intent("Result");
            context.sendBroadcast(intent);
        } else {
            String add = "EndGame@1&";
            connectedThread.write(add.getBytes());
        }
    }

    public void BTErrorDestroySignal() {
        Intent intent = new Intent("Destroy");
        context.sendBroadcast(intent);
    }

    public void Draw(Canvas canvas) {
        if (!hadSave) {
            //SaveMap();
            SaveBackground();
            hadSave = true;
        }
        if (bg != null) {
            canvas.drawBitmap(bg, 0, 0, null);
        }
        //Draw background and grass
        Paint alphaPaint = new Paint();
        alphaPaint.setAlpha(100);
        for (MapObject mapObject : map.bombs) {
            try {
                mapObject.Draw(canvas);
            } catch (Exception e) {
                e.getCause();
            }
        }
        for (MapObject mapObject : map.mapItems) {
            try {
                mapObject.Draw(canvas);
            } catch (Exception e) {
                e.getCause();
            }
        }
        for (Ai ai : map.ais) {
            try {
                ai.Draw(canvas);
            } catch (Exception e) {
                e.getCause();
            }
        }
        for (Player player : map.players) {
            try {
                player.Draw(canvas);
            } catch (Exception e) {
                e.getCause();
            }
        }

        for (MapObject mapObject : map.mapObjects) {
            try {
                mapObject.Draw(canvas);
            } catch (Exception e) {
                e.getCause();
            }
        }

        for (Explosion explosion : map.explosions) {
            try {
                explosion.Draw(canvas);
            } catch (Exception e) {
                e.getCause();
            }
        }
        for (MapObject mapObject : map.centerObjects) {
            try {
                mapObject.Draw(canvas);
            } catch (Exception e) {
                e.getCause();
            }
        }
        for (Ai ai : map.ais) {
            try {
                ai.DrawEmotion(canvas);
            } catch (Exception e) {
                e.getCause();
            }
        }
        for (Player player : map.players) {
            try {
                player.DrawEmotion(canvas);
            } catch (Exception e) {
                e.getCause();
            }
        }

        for (MapObject mapObject : map.mapFlyingObjects) {
            try {
                mapObject.Draw(canvas);
            } catch (Exception e) {
                e.getCause();
            }
        }
        if (map.flyingShip != null) {
            map.flyingShip.Draw(canvas);
        }
        if (controlMode == 2) {
            if (isControlDirection) {
                double widthOffset = screenWidth / Common.GAME_WIDTH_UNIT;
                double heightOffset = screenHeight / Common.GAME_HEIGHT_UNIT;
                try {
                    Bitmap directionOut = map.imageCaches.get("direction_out.png");
                    alphaPaint.setAlpha(120);
                    canvas.drawBitmap(directionOut,
                            (float) (touchInitX - widthOffset * 2),
                            (float) (touchInitY - heightOffset * 2),
                            alphaPaint);
                    Bitmap directionIn = map.imageCaches.get("direction_in.png");
                    alphaPaint.setAlpha(60);
                    canvas.drawBitmap(directionIn,
                            (float) (touchNowX - widthOffset * 1),
                            (float) (touchNowY - heightOffset * 1),
                            alphaPaint);
                } catch (Exception e) {
                    e.getCause();
                }
            }
        } else if (controlMode == 1) {
            try {
                Bitmap directionOut = map.imageCaches.get("direction_out.png");
                alphaPaint.setAlpha(120);
                canvas.drawBitmap(directionOut,
                        (screenWidth * (-1 + (Common.GAME_WIDTH_UNIT - Common.MAP_WIDTH_UNIT) / 2)) / Common.GAME_WIDTH_UNIT,
                        (screenHeight * 7) / Common.GAME_HEIGHT_UNIT,
                        alphaPaint);
            } catch (Exception e) {
                e.getCause();
            }
            if (isControlDirection) {
                double widthOffset = screenWidth / Common.GAME_WIDTH_UNIT;
                double heightOffset = screenHeight / Common.GAME_HEIGHT_UNIT;
                try {
                    Bitmap directionIn = map.imageCaches.get("direction_in.png");
                    alphaPaint.setAlpha(60);
                    canvas.drawBitmap(directionIn,
                            (float) (touchNowX - widthOffset * 1),
                            (float) (touchNowY - heightOffset * 1),
                            alphaPaint);
                } catch (Exception e) {
                    e.getCause();
                }
            }
        } else {
            alphaPaint.setAlpha(120);
            try {
                Bitmap direction = map.imageCaches.get("direction.png");
                canvas.drawBitmap(direction,
                        (screenWidth * (-1 + (Common.GAME_WIDTH_UNIT - Common.MAP_WIDTH_UNIT) / 2)) / Common.GAME_WIDTH_UNIT,
                        (screenHeight * 6) / Common.GAME_HEIGHT_UNIT,
                        alphaPaint);
            } catch (Exception e) {
                e.getCause();
            }
        }
        //右側功能列

        try {
            //炸彈
            Bitmap directionOut = map.imageCaches.get("target.png");
            alphaPaint.setAlpha(120);
            canvas.drawBitmap(directionOut,
                    (screenWidth * (13 + (Common.GAME_WIDTH_UNIT - Common.MAP_WIDTH_UNIT) / 2)) / Common.GAME_WIDTH_UNIT,
                    (screenHeight * 7) / Common.GAME_HEIGHT_UNIT,
                    alphaPaint);
        } catch (Exception e) {
            e.getCause();
        }
        if (FPS) {
            map.DrawFPS(canvas);
        }
        map.DrawNowKill(canvas);
        map.DrawTotalTime(canvas);

        if (isGameEnd) {
            gameEndFrameCounter++;
            canvas.drawARGB(60, 0, 0, 0);
            Bitmap flag;
            if (isWin) {
                flag = map.imageCaches.get("win.png");
                canvas.drawBitmap(flag,
                        (screenWidth * 6) / Common.GAME_WIDTH_UNIT,
                        (screenHeight * 1) / Common.GAME_HEIGHT_UNIT,
                        null);
            } else {
                flag = map.imageCaches.get("lose.png");
                canvas.drawBitmap(flag,
                        (screenWidth * 6) / Common.GAME_WIDTH_UNIT,
                        (screenHeight * 1) / Common.GAME_HEIGHT_UNIT,
                        null);
            }
            map.DrawTotalExp(canvas);
            map.DrawTotalMoney(canvas);
            if (gameEndFrameCounter > (AchievementUnlockCount * 1500 + 5000) / Common.GAME_REFRESH) {
                if (isLoadingEvent) {
                    if (isLoadingEventSuccess && !HadPreesEnd) {
                        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                        paint.setTextSize(screenHeight / 18);
                        paint.setColor(Color.parseColor("#ffd6fffd"));
                        paint.setTextAlign(Paint.Align.CENTER);
                        paint.setTypeface(Typeface.DEFAULT_BOLD);
                        if (gameEndFrameCounter % 20 < 10) {
                            canvas.drawText(Common.getStringResourceByName("press_to_continue", context), screenWidth / 2, (float) (screenHeight * 11.5 / 12), paint);
                        }
                    }
                } else if (!HadPreesEnd) {
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setTextSize(screenHeight / 18);
                    paint.setColor(Color.parseColor("#ffd6fffd"));
                    paint.setTextAlign(Paint.Align.CENTER);
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                    if (gameEndFrameCounter % 20 < 10) {
                        canvas.drawText(Common.getStringResourceByName("press_to_continue", context), screenWidth / 2, (float) (screenHeight * 11.5 / 12), paint);
                    }
                }
            }
        }
        //開始前倒數
        if (gameTime <= 25) {
            Bitmap img = map.imageCaches.get("three.png");
            canvas.drawBitmap(img,
                    (screenWidth * 7) / Common.GAME_WIDTH_UNIT,
                    (screenHeight * 3) / Common.GAME_HEIGHT_UNIT,
                    null);
        } else if (gameTime <= 50) {
            Bitmap img = map.imageCaches.get("two.png");
            canvas.drawBitmap(img,
                    (screenWidth * 7) / Common.GAME_WIDTH_UNIT,
                    (screenHeight * 3) / Common.GAME_HEIGHT_UNIT,
                    null);
        } else if (gameTime <= 75) {
            Bitmap img = map.imageCaches.get("one.png");
            canvas.drawBitmap(img,
                    (screenWidth * 7) / Common.GAME_WIDTH_UNIT,
                    (screenHeight * 3) / Common.GAME_HEIGHT_UNIT,
                    null);
        }
    }

    public void SaveBackground() {
        //這是將背景(不會改變的物件如樹、房子、地板)等優先繪製，之後便只需要繪製一張大圖
        bg = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);

        canvas.drawColor(Color.rgb(0, 0, 0));
        for (int x = 0; x < Common.MAP_WIDTH_UNIT; x++) {
            for (int y = 0; y < Common.MAP_HEIGHT_UNIT; y++) {
                Bitmap bitmap = map.imageCaches.get("grass.png");
                if (bitmap != null) {
                    try {
                        canvas.drawBitmap(bitmap,
                                (screenWidth * (x + (Common.GAME_WIDTH_UNIT - Common.MAP_WIDTH_UNIT) / 2)) / Common.GAME_WIDTH_UNIT,
                                (screenHeight * y) / Common.GAME_HEIGHT_UNIT,
                                null);
                    } catch (Exception e) {
                        e.getCause();
                    }
                }
            }
        }
        //這裡畫邊界的地方，為避免留黑所以畫上地圖，同時加上樹木
        int diffX = (Common.GAME_WIDTH_UNIT - Common.MAP_WIDTH_UNIT) / 2;

        //左邊界
        for (int x = 0; x < diffX; x++) {
            for (int y = 0; y < Common.MAP_HEIGHT_UNIT; y++) {
                Bitmap bitmap = map.imageCaches.get("grass.png");
                if (bitmap != null) {
                    try {
                        canvas.drawBitmap(bitmap,
                                (screenWidth * x) / Common.GAME_WIDTH_UNIT,
                                (screenHeight * y) / Common.GAME_HEIGHT_UNIT,
                                null);
                    } catch (Exception e) {
                        e.getCause();
                    }
                }
                bitmap = map.imageCaches.get("tree1.png");
                if (bitmap != null) {
                    try {
                        canvas.drawBitmap(bitmap,
                                (screenWidth * x) / Common.GAME_WIDTH_UNIT,
                                (screenHeight * y) / Common.GAME_HEIGHT_UNIT,
                                null);
                    } catch (Exception e) {
                        e.getCause();
                    }
                }
            }
        }
        //右邊界
        for (int x = diffX + Common.MAP_WIDTH_UNIT; x < Common.GAME_WIDTH_UNIT; x++) {
            for (int y = 0; y < Common.MAP_HEIGHT_UNIT; y++) {
                Bitmap bitmap = map.imageCaches.get("grass.png");
                if (bitmap != null) {
                    try {
                        canvas.drawBitmap(bitmap,
                                (screenWidth * x) / Common.GAME_WIDTH_UNIT,
                                (screenHeight * y) / Common.GAME_HEIGHT_UNIT,
                                null);
                    } catch (Exception e) {
                        e.getCause();
                    }
                }
                bitmap = map.imageCaches.get("tree1.png");
                if (bitmap != null) {
                    try {
                        canvas.drawBitmap(bitmap,
                                (screenWidth * x) / Common.GAME_WIDTH_UNIT,
                                (screenHeight * y) / Common.GAME_HEIGHT_UNIT,
                                null);
                    } catch (Exception e) {
                        e.getCause();
                    }
                }
            }
        }
    }

    public void SaveMap() {
        Bitmap b = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        canvas.drawColor(Color.rgb(0, 0, 0));
        Paint alphaPaint = new Paint();
        alphaPaint.setAlpha(100);
        for (int x = 0; x < Common.MAP_WIDTH_UNIT; x++) {
            for (int y = 0; y < Common.MAP_HEIGHT_UNIT; y++) {
                Bitmap bitmap = map.imageCaches.get("grass.png");
                if (bitmap != null) {
                    try {
                        canvas.drawBitmap(bitmap,
                                (screenWidth * (x + (Common.GAME_WIDTH_UNIT - Common.MAP_WIDTH_UNIT) / 2)) / Common.GAME_WIDTH_UNIT,
                                (screenHeight * y) / Common.GAME_HEIGHT_UNIT,
                                null);
                    } catch (Exception e) {
                        e.getCause();
                    }
                }
            }
        }
        //這裡畫邊界的地方，為避免留黑所以畫上地圖，同時加上樹木
        int diffX = (Common.GAME_WIDTH_UNIT - Common.MAP_WIDTH_UNIT) / 2;

        //左邊界
        for (int x = 0; x < diffX; x++) {
            for (int y = 0; y < Common.MAP_HEIGHT_UNIT; y++) {
                Bitmap bitmap = map.imageCaches.get("grass.png");
                if (bitmap != null) {
                    try {
                        canvas.drawBitmap(bitmap,
                                (screenWidth * x) / Common.GAME_WIDTH_UNIT,
                                (screenHeight * y) / Common.GAME_HEIGHT_UNIT,
                                null);
                    } catch (Exception e) {
                        e.getCause();
                    }
                }
                bitmap = map.imageCaches.get("tree1.png");
                if (bitmap != null) {
                    try {
                        canvas.drawBitmap(bitmap,
                                (screenWidth * x) / Common.GAME_WIDTH_UNIT,
                                (screenHeight * y) / Common.GAME_HEIGHT_UNIT,
                                null);
                    } catch (Exception e) {
                        e.getCause();
                    }
                }
            }
        }
        //右邊界
        for (int x = diffX + Common.MAP_WIDTH_UNIT; x < Common.GAME_WIDTH_UNIT; x++) {
            for (int y = 0; y < Common.MAP_HEIGHT_UNIT; y++) {
                Bitmap bitmap = map.imageCaches.get("grass.png");
                if (bitmap != null) {
                    try {
                        canvas.drawBitmap(bitmap,
                                (screenWidth * x) / Common.GAME_WIDTH_UNIT,
                                (screenHeight * y) / Common.GAME_HEIGHT_UNIT,
                                null);
                    } catch (Exception e) {
                        e.getCause();
                    }
                }
                bitmap = map.imageCaches.get("tree1.png");
                if (bitmap != null) {
                    try {
                        canvas.drawBitmap(bitmap,
                                (screenWidth * x) / Common.GAME_WIDTH_UNIT,
                                (screenHeight * y) / Common.GAME_HEIGHT_UNIT,
                                null);
                    } catch (Exception e) {
                        e.getCause();
                    }
                }
            }
        }
        for (MapObject mapObject : map.bombs) {
            try {
                mapObject.Draw(canvas);
            } catch (Exception e) {
                e.getCause();
            }
        }
        for (MapObject mapObject : map.mapObjects) {
            try {
                mapObject.Draw(canvas);
            } catch (Exception e) {
                e.getCause();
            }
        }
        for (MapObject mapObject : map.centerObjects) {
            try {
                mapObject.Draw(canvas);
            } catch (Exception e) {
                e.getCause();
            }
        }
        try {
            // 輸出的圖檔位置

            File file = new File("/sdcard/bombisland2/" + map.id + ".png");
            FileOutputStream fos = new FileOutputStream(file);

            // 將 Bitmap 儲存成 PNG / JPEG 檔案格式
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            // 釋放
            fos.close();
            file.setReadable(true, false);
            file.setExecutable(true, false);
            file.setWritable(true, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        hadSave = true;
        //SaveMapAll();
    }

    public void SaveMapAll() {
        Bitmap b = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        canvas.drawColor(Color.rgb(0, 0, 0));
        Paint alphaPaint = new Paint();
        alphaPaint.setAlpha(100);
        for (int x = 0; x < Common.MAP_WIDTH_UNIT; x++) {
            for (int y = 0; y < Common.MAP_HEIGHT_UNIT; y++) {
                Bitmap bitmap = map.imageCaches.get("grass.png");
                if (bitmap != null)
                    canvas.drawBitmap(bitmap,
                            (screenWidth * (x + (Common.GAME_WIDTH_UNIT - Common.MAP_WIDTH_UNIT) / 2)) / Common.GAME_WIDTH_UNIT,
                            (screenHeight * y) / Common.GAME_HEIGHT_UNIT,
                            null);
            }
        }
        for (MapObject mapObject : map.mapObjects) {
            mapObject.Draw(canvas);
        }

        for (Player player : map.players) {
            player.Draw(canvas);
        }
        for (Ai ai : map.ais) {
            ai.Draw(canvas);
        }

        for (MapObject mapObject : map.centerObjects) {
            mapObject.Draw(canvas);
        }
        try {
            // 輸出的圖檔位置

            File file = new File(Environment.getExternalStorageDirectory() + "/bombisland2/" + map.id + "_all.png");
            FileOutputStream fos = new FileOutputStream(file);

            // 將 Bitmap 儲存成 PNG / JPEG 檔案格式
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            // 釋放
            fos.close();
            file.setReadable(true, false);
            file.setExecutable(true, false);
            file.setWritable(true, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        hadSave = true;
    }

    public void InitBTGame() {
        if (IS_SERVER) {
            hadInitPlayer = true;
            String playerInfo = "PlayerInfo@";
            Boolean IsFirst = true;
            for (Player player : map.players) {
                if (!IsFirst) {
                    playerInfo += ";";
                }
                playerInfo += player.uid + ","
                        + player.player_x / Common.PLAYER_POSITION_RATE + ","
                        + player.player_y / Common.PLAYER_POSITION_RATE;
                IsFirst = false;
            }
            playerInfo += "&";
            byte[] data = playerInfo.getBytes();
            connectedThread.write(data);
            IsFirst = true;
            playerInfo = "PlayerMapInfo@";
            try {
                for (MapObject mapObject : map.mapObjects) {
                    if (!IsFirst) {
                        playerInfo += ",";
                    }
                    playerInfo += mapObject.randNum;
                    IsFirst = false;
                }
                playerInfo += "&";
                connectedThread.write(playerInfo.getBytes());
            } catch (Exception e) {
                e.getCause();
            }
        }
    }

    public void SaveData() {
        String file = "money.bl2";
        Recorder recorder = new Recorder(context);
        String moneyRecord = recorder.Read(file);
        int money;
        if (moneyRecord != null) {
            try {
                money = Integer.parseInt(moneyRecord);
            } catch (Exception e) {
                e.getCause();
                money = Common.DEFAULT_MONEY;
            }
        } else {
            money = Common.DEFAULT_MONEY;
        }
        if (BT_MODE) {
            if (isWin) {
                getMoney += 500;
            }
        } else {
            if (isWin) {
                getMoney += 200 * aiCount;
            }
        }
        money += getMoney;
        recorder.Write(String.valueOf(money), file);

        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                try {
                    for (Player player : map.players) {
                        if (player.uid.equals(playerID)) {
                            isLoadingEvent = true;
                            Games.Events.increment(mGoogleApiClient, Common.getStringResourceByName("event_total_bombs", context), player.totalBomb);
                            Games.Events.increment(mGoogleApiClient, Common.getStringResourceByName("event_total_destroy", context), AiDeadCounter);
                            if (!ENDLESS_MODE) {
                                //死鬥不會贏...
                                Games.Events.increment(mGoogleApiClient, Common.getStringResourceByName("event_total_win", context), isWin ? 1 : 0);
                                Games.Events.increment(mGoogleApiClient, Common.getStringResourceByName("event_total_games", context), 1);
                            } else {
                                Games.Events.increment(mGoogleApiClient, Common.getStringResourceByName("event_total_endless_game", context), 1);
                            }
                            Games.Events.increment(mGoogleApiClient, Common.getStringResourceByName("event_total_items", context), player.totalItem);
                            Games.Events.increment(mGoogleApiClient, Common.getStringResourceByName("event_total_times", context), gameTime / 25);
                            Games.Events.increment(mGoogleApiClient, Common.getStringResourceByName("event_total_mount", context), player.totalMount);
                            Games.Events.increment(mGoogleApiClient, Common.getStringResourceByName("event_total_saves", context), player.totalSave);
                            Games.Events.increment(mGoogleApiClient, Common.getStringResourceByName("event_total_distances", context), player.totalMove / Common.PLAYER_POSITION_RATE);
                            Games.Events.increment(mGoogleApiClient, Common.getStringResourceByName("event_total_exp", context), getExp);

                            Games.Achievements.unlock(mGoogleApiClient, Common.getStringResourceByName("achievement_beginners", context));
                            Games.Achievements.increment(mGoogleApiClient, Common.getStringResourceByName("achievement_bronze_badge", context), 1);
                            Games.Achievements.increment(mGoogleApiClient, Common.getStringResourceByName("achievement_silver_badge", context), 1);
                            Games.Achievements.increment(mGoogleApiClient, Common.getStringResourceByName("achievement_gold_badge", context), 1);
                            Games.Achievements.increment(mGoogleApiClient, Common.getStringResourceByName("achievement_master_badge", context), 1);
                            Games.Achievements.increment(mGoogleApiClient, Common.getStringResourceByName("achievement_king_badge", context), 1);

                            if (ENDLESS_MODE) {
                                Games.Leaderboards.submitScore(mGoogleApiClient,
                                        Common.getStringResourceByName("leaderboard_endless_mode", context), AiDeadCounter);
                            }

                            EventCallback ec = new EventCallback();

                            // Load all events tracked for your game
                            com.google.android.gms.common.api.PendingResult<Events.LoadEventsResult>
                                    pr = Games.Events.load(mGoogleApiClient, true);
                            pr.setResultCallback(ec);
                        }
                    }
                } catch (Exception e) {
                    e.getCause();
                }
            }
        }
           
        /*
         *  Total 9 records at 2.50 ver.
         *  0->totalBomb    1->totalKill   2->totalItem    3->totalMount    4->totalSave=0
         *  5->totalMove    6->totalTime    7->totalWin     8->totalGame
         */
        /*
         *  2.50 ver uses local record, 3.00 up change to Google Game Service
         */
    }

    public void Play() {
        gameTime++;
        //前面先倒數
        if (gameTime == 1) {
            soundManager.addSound("countdown.mp3");
        } else if (gameTime == 26) {
            soundManager.addSound("countdown.mp3");
        } else if (gameTime == 51) {
            soundManager.addSound("countdown.mp3");
        } else if (gameTime == 76) {
            soundManager.addSound("gamestart.mp3");
        }
        if (gameTime <= 75) {
            gameThread.startTime = System.currentTimeMillis();
        } else {
            if (gameTime % (25 * 30) == 0 && map.flyingShip == null && !BT_MODE) {
                if (Common.RandomNum(1000) > 500) {
                    map.flyingShip = new FlyingShip("flyingship01", -3, 1, map, Ai.FLYING_SHIP);
                } else {
                    map.flyingShip = new FlyingShip("flyingship02", -3, 1, map, Ai.FLYING_SHIP);
                }
                map.flyingShip.character.direction = Location.LOCATION_RIGHT;
                map.flyingShip.character.InitImage();
            }
            hadPlayExplosion = false;
            if (isGameEnd) {
                if (!hadSaveGameEnd) {
                    hadSaveGameEnd = true;
                    gameThread.endTime = System.currentTimeMillis();
                    if (isWin) {
                        getExp = 150 + AiDeadCounter * 50 + gameTime / 25;
                    } else {
                        getExp = 20 + gameTime / 25 + AiDeadCounter * 20;
                    }
                    SaveData();
                }
            }
            //重設炸彈的陣列DP，方便之後快速運算
            map.ResetDP();
            for (MapObject mapObject : map.mapObjects) {
                try {
                    mapObject.Play();
                } catch (Exception e) {
                    e.getCause();
                }
            }
            for (MapObject mapObject : map.mapItems) {
                try {
                    mapObject.Play();
                } catch (Exception e) {
                    e.getCause();
                }
            }
            map.bombLock = true;
            for (MapObject mapObject : map.bombs) {
                try {
                    mapObject.Play();
                } catch (Exception e) {
                    e.getCause();
                }
            }
            map.removeMapObjects.removeAllElements();
            for (MapObject mapObject : map.bombs) {
                try {
                    if (mapObject.IsEnd) {
                        map.removeMapObjects.add(mapObject);
                    }
                } catch (Exception e) {
                    e.getCause();
                }
            }
            map.bombs.removeAll(map.removeMapObjects);
            map.bombLock = false;
            //將 Vector 轉換成 HashMap，加速連鎖爆炸時的資料提取速度
            map.SetBombsHashMap();
            map.removeExplosions.removeAllElements();
            map.explosionLocations.removeAllElements();
            map.removeMapObjects.removeAllElements();
            for (Explosion explosion : map.explosions) {
                try {
                    explosion.Play();
                    if (explosion.IsEnd) {
                        map.removeExplosions.add(explosion);
                    }
                } catch (Exception e) {
                    e.getCause();
                }
            }
            for (Location location : map.explosionLocations) {
                map.Explosion(location);
            }

            for (MapObject mapObject : map.mapObjects) {
                try {
                    if (mapObject.IsEnd) {
                        map.removeMapObjects.add(mapObject);
                    }
                } catch (Exception e) {
                    e.getCause();
                }
            }
            map.mapObjects.removeAll(map.removeMapObjects);
            map.explosions.removeAll(map.removeExplosions);
            map.removeMapObjects.removeAllElements();
            for (MapObject mapObject : map.mapItems) {
                try {
                    if (mapObject.IsEnd) {
                        map.removeMapObjects.add(mapObject);
                    }
                } catch (Exception e) {
                    e.getCause();
                }
            }
            map.mapItems.removeAll(map.removeMapObjects);
            if (map.flyingShip != null) {
                map.flyingShip.Move();
                map.flyingShip.FlyingShipDropItem();
                if (map.flyingShip.IsDead) {
                    map.flyingShip = null;
                }
            }
            if (!isGameEnd) {

                for (Player player : map.players) {
                    try {
                        if (player.uid.equals(playerID)) {
                            player.Move();
                            if (pressAddBomb) {
                                player.AddBomb();
                            }
                        } else {
                            player.BTMove();
                        }
                    } catch (Exception e) {
                        e.getCause();
                    }
                    try {
                        player.CheckItem();
                    } catch (Exception e) {
                        e.getCause();
                    }
                    try {
                        player.CheckBubble();
                    } catch (Exception e) {
                        e.getCause();
                    }
                    try {
                        player.CheckDead(false);
                    } catch (Exception e) {
                        e.getCause();
                    }
                }
                Vector<Ai> removeAis = new Vector<>();
                for (Ai ai : map.ais) {
                    try {
                        ai.Move();
                        ai.CheckBubble();
                        ai.CheckItem();
                        if (ai.IsDead) {
                            AiDeadCounter++;
                            removeAis.add(ai);
                            continue;
                        }
                        ai.CheckDead();
                    } catch (Exception e) {
                        e.getCause();
                    }
                }
                if (ENDLESS_MODE) {
                    //死鬥模式下不會回收AI，而是等待10秒後於隨機位置重生
                    for (Ai ai : removeAis) {
                        ai.revivalCounter = -25 * 10;
                        ai.invincibleCounter = 25 * 3;//復活後3秒內無敵
                        ai.IsDead = false;
                        ai.IsBubbled = false;
                        ai.deadExplosion = null;
                        for (int i = ai.eatItems.size() - 1; i >= 10; i--) {
                            //ai.eatItems.removeElementAt(i);
                        }
                        int tmp_x, tmp_y;
                        while (true) {
                            tmp_x = Common.RandomNum(Common.MAP_WIDTH_UNIT) - 1;
                            tmp_y = Common.RandomNum(Common.MAP_HEIGHT_UNIT) - 1;
                            if (map.mapObstacles[tmp_x][tmp_y] == 0) {
                                break;
                            }
                        }
                        ai.player_x = tmp_x * Common.PLAYER_POSITION_RATE;
                        ai.player_y = tmp_y * Common.PLAYER_POSITION_RATE;
                        ai.targetLocation.x = tmp_x;
                        ai.targetLocation.y = tmp_y;
                        ai.InitEmotion("emotion_revival");
                    }
                } else {
                    map.ais.removeAll(removeAis);
                }
            } else {
                if (gameEndFrameCounter > (AchievementUnlockCount * 1500 + 5000) / Common.GAME_REFRESH) {
                    if (isLoadingEvent) {
                        if (isLoadingEventSuccess && HadPreesEnd) {
                            gameThread.stop = true;
                            GameEndSignal();
                        }
                    } else if (HadPreesEnd) {
                        gameThread.stop = true;
                        GameEndSignal();
                    }
                }
            }
            for (MapObject mapObject : map.centerObjects) {
                mapObject.Play();
            }
            for (MapObject mapObject : map.mapFlyingObjects) {
                mapObject.Play();
                try {
                    if (mapObject.totalTime > Common.GAME_REFRESH * 25 && !mapObject.IsEnd) {
                        MapObject obj = new MapObject(mapObject.location.x, mapObject.location.y, mapObject.type, mapObject.id, mapObject.map);
                        map.mapItems.add(obj);
                        mapObject.IsEnd = true;
                    }
                } catch (Exception e) {
                    e.getCause();
                }
            }
            map.removeMapObjects.removeAllElements();
            for (MapObject mapObject : map.mapFlyingObjects) {
                try {
                    if (mapObject.IsEnd) {
                        map.removeMapObjects.add(mapObject);
                    }
                } catch (Exception e) {
                    e.getCause();
                }
            }
            map.mapFlyingObjects.removeAll(map.removeMapObjects);
        }
        FrameBalancer++;
        BTFrameEnd();
        BTPlayerMove();
        map.CheckGameEnd(this);
    }

    public void StopThread() {
        if (gameThread != null) {
            gameThread.stop = true;
            gameThread = null;
        }
    }

    public void BTFrameEnd() {
        if (BT_MODE) {
            try {
                String playerInfo = "PlayerFrameEnd@" + gameTime;
                playerInfo += "&";
                byte[] data = playerInfo.getBytes();
                connectedThread.write(data);
                if (playerMoveStr.length() > 1) {
                    data = playerMoveStr.getBytes();
                    connectedThread.write(data);
                }
            } catch (Exception e) {
                BTFrameEnd();
            }
        }
    }

    public void BTPlayerAddBomb(int x, int y) {
        if (BT_MODE) {
            try {
                String add = "BTPlayerAddBomb@" + playerID + "," + x + "," + y + "," + gameTime + "&";
                connectedThread.write(add.getBytes());
            } catch (Exception e) {
                BTPlayerAddBomb(x, y);
            }
        }
    }

    public void BTPlayerMove() {
        if (BT_MODE) {
            try {
                for (Player player : map.players) {
                    if (player.uid.equals(playerID)) {
                        String move = "BTPlayerMove@" + playerID
                                + "," + player.player_x
                                + "," + player.player_y
                                + "," + player.character.direction
                                + "," + player.speed_now
                                + "&";
                        connectedThread.write(move.getBytes());
                    }
                }
            } catch (Exception e) {
                BTPlayerMove();
            }
        }
    }

    public void BTPlayerMoveTo(String id, int x, int y, int d, int s) {
        for (Player player : map.players) {
            if (player.uid.equals(id)) {
                player.speed_now = s;
                player.character.direction = d;
                player.player_x = x;
                player.player_y = y;
                player.character.InitImage();
            }
        }
    }

    public void BTPlayerAddBombTo(String id, int x, int y, int time) {
        for (Player player : map.players) {
            if (player.uid.equals(id)) {
                player.AddBombBT(x, y, gameTime - time);
            }
        }
    }

    public void PlayerMove(int direction, String id) {
        for (Player player : map.players) {
            if (player.uid.equals(id)) {
                if (player.mountDeadCounter > 0) {
                    return;
                }
                if(player.IsBubbled){
                    return;
                }
                try {
                    player.IsMoving = true;
                    player.character.direction = direction;
                    player.character.InitImage();
                } catch (Exception e) {
                    e.getCause();
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!gameThread.isAlive()) {
            gameThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        StopThread();
    }

    //Touch Event
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameEndFrameCounter > (AchievementUnlockCount * 1500 + 5000) / Common.GAME_REFRESH) {
            if (isLoadingEvent) {
                if (isLoadingEventSuccess && !HadPreesEnd) {
                    HadPreesEnd = true;
                }
            } else if (!HadPreesEnd) {
                HadPreesEnd = true;
            }
        }
        if (isGameEnd || !isGameStart) {
            return true;
        }
        if (gameTime <= 75) {
            return true;
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                for (Player player : map.players) {
                    if (player.uid.equals(playerID)) {
                        player.IsMoving = false;
                        player.speed_now = 0;
                        isControlDirection = false;
                    }
                }
                pressAddBomb = false;
                break;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    Touch(event.getX(i), event.getY(i), event.getPointerCount() == 1);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Touch(event.getX(1), event.getY(1), false);
                break;
        }
        return true;
    }

    public void Touch(double x, double y, boolean Single) {
        if (x > Common.OLD_SCREEN_WIDTH / 2) {
            double widthOffset = Common.OLD_SCREEN_WIDTH / Common.GAME_WIDTH_UNIT;
            double heightOffset = Common.OLD_SCREEN_HEIGHT / Common.GAME_HEIGHT_UNIT;
            if (x >= widthOffset * 15 && x <= widthOffset * 19) {
                if (y >= heightOffset * 7 && y <= heightOffset * 11) {
                    pressAddBomb = true;
                    if (Single && controlMode != 0) {
                        isControlDirection = false;
                        for (Player player : map.players) {
                            if (player.uid.equals(playerID)) {
                                player.IsMoving = false;
                                player.speed_now = 0;
                                isControlDirection = false;
                            }
                        }
                    }
                    return;
                }
            }
            pressAddBomb = false;
        } else {
            if (Single) {
                pressAddBomb = false;
            }
            if (controlMode == 2) {
                if (isControlDirection) {
                    touchNowX = x;
                    touchNowY = y;
                    double widthOffset = Common.OLD_SCREEN_WIDTH / Common.GAME_WIDTH_UNIT;
                    double heightOffset = Common.OLD_SCREEN_HEIGHT / Common.GAME_HEIGHT_UNIT;
                    double touchLength =
                            Math.sqrt(Math.pow(touchNowX - touchInitX, 2) + Math.pow(touchNowY - touchInitY, 2));
                    double limitLength = widthOffset + heightOffset;
                    if (touchLength > limitLength) {
                        double offsetX = touchNowX - touchInitX;
                        double offsetY = touchNowY - touchInitY;
                        offsetX = offsetX * limitLength / touchLength;
                        offsetY = offsetY * limitLength / touchLength;
                        touchNowX = touchInitX + offsetX;
                        touchNowY = touchInitY + offsetY;
                    }
                    double touchX = x - touchInitX;
                    double touchY = y - touchInitY;
                    if (touchX > 0) {
                        if (touchY > 0) {
                            if (Math.abs(touchX) > Math.abs(touchY)) {
                                PlayerMove(Location.LOCATION_RIGHT, playerID);
                            } else {
                                PlayerMove(Location.LOCATION_DOWN, playerID);
                            }
                        } else if (touchY < 0) {
                            if (Math.abs(touchX) > Math.abs(touchY)) {
                                PlayerMove(Location.LOCATION_RIGHT, playerID);
                            } else {
                                PlayerMove(Location.LOCATION_TOP, playerID);
                            }
                        }
                    } else if (touchX < 0) {
                        if (touchY > 0) {
                            if (Math.abs(touchX) > Math.abs(touchY)) {
                                PlayerMove(Location.LOCATION_LEFT, playerID);
                            } else {
                                PlayerMove(Location.LOCATION_DOWN, playerID);
                            }
                        } else if (touchY < 0) {
                            if (Math.abs(touchX) > Math.abs(touchY)) {
                                PlayerMove(Location.LOCATION_LEFT, playerID);
                            } else {
                                PlayerMove(Location.LOCATION_TOP, playerID);
                            }
                        }
                    }
                } else {
                    isControlDirection = true;
                    touchInitX = x;
                    touchInitY = y;
                    touchNowX = x;
                    touchNowY = y;
                }
            } else if (controlMode == 1) {
                isControlDirection = true;
                touchInitX = (Common.OLD_SCREEN_WIDTH * (-1 + (Common.GAME_WIDTH_UNIT - Common.MAP_WIDTH_UNIT) / 2)) / Common.GAME_WIDTH_UNIT;
                touchInitY = (Common.OLD_SCREEN_HEIGHT * 7) / Common.GAME_HEIGHT_UNIT;
                double widthOffset = Common.OLD_SCREEN_WIDTH / Common.GAME_WIDTH_UNIT;
                double heightOffset = Common.OLD_SCREEN_HEIGHT / Common.GAME_HEIGHT_UNIT;
                touchInitX += widthOffset * 2;
                touchInitY += heightOffset * 2;
                touchNowX = x;
                touchNowY = y;
                double touchLength =
                        Math.sqrt(Math.pow(touchNowX - touchInitX, 2) + Math.pow(touchNowY - touchInitY, 2));
                double limitLength = widthOffset + heightOffset;
                if (touchLength > limitLength) {
                    double offsetX = touchNowX - touchInitX;
                    double offsetY = touchNowY - touchInitY;
                    offsetX = offsetX * limitLength / touchLength;
                    offsetY = offsetY * limitLength / touchLength;
                    touchNowX = touchInitX + offsetX;
                    touchNowY = touchInitY + offsetY;
                }
                double touchX = x - touchInitX;
                double touchY = y - touchInitY;
                if (touchX > 0) {
                    if (touchY > 0) {
                        if (Math.abs(touchX) > Math.abs(touchY)) {
                            PlayerMove(Location.LOCATION_RIGHT, playerID);
                        } else {
                            PlayerMove(Location.LOCATION_DOWN, playerID);
                        }
                    } else if (touchY < 0) {
                        if (Math.abs(touchX) > Math.abs(touchY)) {
                            PlayerMove(Location.LOCATION_RIGHT, playerID);
                        } else {
                            PlayerMove(Location.LOCATION_TOP, playerID);
                        }
                    }
                } else if (touchX < 0) {
                    if (touchY > 0) {
                        if (Math.abs(touchX) > Math.abs(touchY)) {
                            PlayerMove(Location.LOCATION_LEFT, playerID);
                        } else {
                            PlayerMove(Location.LOCATION_DOWN, playerID);
                        }
                    } else if (touchY < 0) {
                        if (Math.abs(touchX) > Math.abs(touchY)) {
                            PlayerMove(Location.LOCATION_LEFT, playerID);
                        } else {
                            PlayerMove(Location.LOCATION_TOP, playerID);
                        }
                    }
                }
            } else {
                int controlW = 5 * Common.OLD_SCREEN_WIDTH / Common.GAME_WIDTH_UNIT;
                int controlH = 5 * Common.OLD_SCREEN_HEIGHT / Common.GAME_HEIGHT_UNIT;
                x -= Common.OLD_SCREEN_WIDTH / Common.GAME_WIDTH_UNIT;
                y -= 6 * Common.OLD_SCREEN_HEIGHT / Common.GAME_HEIGHT_UNIT;
                int centerX = controlW / 2, centerY = controlH / 2;
                int touchX = (int) x - centerX;
                int touchY = (int) y - centerY;
                if (touchX > 0 && touchX < centerX) {
                    if (touchY > 0 && touchY < centerY) {
                        if (Math.abs(touchX) > Math.abs(touchY)) {
                            PlayerMove(Location.LOCATION_RIGHT, playerID);
                        } else {
                            PlayerMove(Location.LOCATION_DOWN, playerID);
                        }
                    } else if (touchY < 0) {
                        if (Math.abs(touchX) > Math.abs(touchY)) {
                            PlayerMove(Location.LOCATION_RIGHT, playerID);
                        } else {
                            PlayerMove(Location.LOCATION_TOP, playerID);
                        }
                    }
                } else if (touchX < 0) {
                    if (touchY > 0 && touchY < centerY) {
                        if (Math.abs(touchX) > Math.abs(touchY)) {
                            PlayerMove(Location.LOCATION_LEFT, playerID);
                        } else {
                            PlayerMove(Location.LOCATION_DOWN, playerID);
                        }
                    } else if (touchY < 0) {
                        if (Math.abs(touchX) > Math.abs(touchY)) {
                            PlayerMove(Location.LOCATION_LEFT, playerID);
                        } else {
                            PlayerMove(Location.LOCATION_TOP, playerID);
                        }
                    }
                }
            }
        }
    }

    public class EventCallback implements ResultCallback {
        // Handle the results from the events load call
        public void onResult(com.google.android.gms.common.api.Result result) {
            Events.LoadEventsResult r = (Events.LoadEventsResult) result;
            com.google.android.gms.games.event.EventBuffer eb = r.getEvents();
            long totalExp = 0, totalTime = 0, totalMove = 0;
            int limit[] = {300, 700, 1200, 2000,
                    3000, 5000, 7000,
                    10000, 15000, 20000,
                    30000, 40000, 50000,
                    70000, 90000, 110000,
                    150000, 300000};
            int timeLimit[] = {3600, 3600 * 24};
            String timeAchievementIDs[] = {
                    "achievement_amateur_gamers",
                    "achievement_hardcore_gamers"};
            String achievementIDs[] = {
                    "achievement_green_i",
                    "achievement_green_ii",
                    "achievement_green_iii",
                    "achievement_green_iv",
                    "achievement_red_v",
                    "achievement_red_vi",
                    "achievement_red_vii",
                    "achievement_blue_viii",
                    "achievement_blue_ix",
                    "achievement_blue_x",
                    "achievement_grown_xi",
                    "achievement_grown_xii",
                    "achievement_grown_xiii",
                    "achievement_yellow_xiv",
                    "achievement_yellow_xv",
                    "achievement_yellow_vi",
                    "achievement_bomber_knight",
                    "achievement_bomber_king"};
            for (int i = 0; i < eb.getCount(); i++) {
                Event event = eb.get(i);
                if (event.getEventId().equals(Common.getStringResourceByName("event_total_exp", context))) {
                    totalExp = event.getValue();
                }
                if (event.getEventId().equals(Common.getStringResourceByName("event_total_times", context))) {
                    totalTime = event.getValue();
                }
                if (event.getEventId().equals(Common.getStringResourceByName("event_total_distances", context))) {
                    totalMove = event.getValue();
                }

            }
            Games.Leaderboards.submitScore(mGoogleApiClient,
                    Common.getStringResourceByName("leaderboard_top_players", context), totalExp);
            Games.Leaderboards.submitScore(mGoogleApiClient,
                    Common.getStringResourceByName("leaderboard_loyal_players", context), totalTime * 1000);
            Games.Leaderboards.submitScore(mGoogleApiClient,
                    Common.getStringResourceByName("leaderboard_top_runners", context), totalMove);
            for (int i = 0; i < limit.length && i < achievementIDs.length; i++) {
                if (limit[i] <= totalExp) {
                    if (totalExp - getExp < limit[i]) {
                        AchievementUnlockCount++;
                    }
                    Games.Achievements.unlock(mGoogleApiClient, Common.getStringResourceByName(achievementIDs[i], context));
                } else {
                    break;
                }
            }
            for (int i = 0; i < timeLimit.length && i < timeAchievementIDs.length; i++) {
                if (timeLimit[i] <= totalTime) {
                    if (totalTime - gameTime / 25 < timeLimit[i]) {
                        AchievementUnlockCount++;
                    }
                    Games.Achievements.unlock(mGoogleApiClient, Common.getStringResourceByName(timeAchievementIDs[i], context));
                } else {
                    break;
                }
            }

            eb.close();
            isLoadingEventSuccess = true;
        }
    }
}
