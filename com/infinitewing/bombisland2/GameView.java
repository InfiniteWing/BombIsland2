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
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.infinitewing.bombisland2.GameObject.Ai;
import com.infinitewing.bombisland2.GameObject.Common;
import com.infinitewing.bombisland2.GameObject.Explosion;
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

/**
 * Created by Administrator on 2016/8/19.
 */
public class GameView extends SurfaceView implements
        SurfaceHolder.Callback {
    public Context context;
    public Boolean BT_MODE;
    public Boolean IS_SERVER;
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
    public String playerID, playerMoveStr = "";
    public MediaPlayer gamebackgroundsound;
    public double touchInitX, touchInitY, touchNowX, touchNowY;
    public boolean isControlDirection;
    private SharedPreferences sp;
    public boolean BGM, effSound, effVibrator,FPS, pressAddBomb;
    private int controlMode;
    public Vibrator vibrator;
    public float sleep;

    public GameView(Context context, String playerID, String mapID, int aiCount, String aiInfo, MediaPlayer gamebackgroundsound) {
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

        this.getHolder().addCallback(this);
        this.gamebackgroundsound = gamebackgroundsound;
        screenWidth = Common.SCREEN_WIDTH;
        screenHeight = Common.SCREEN_HEIGHT;
        resources = this.getResources();
        Common.gameView = this;
        soundManager = new SoundManager();
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

        this.getHolder().addCallback(this);
        this.gamebackgroundsound = gamebackgroundsound;
        screenWidth = Common.SCREEN_WIDTH;
        screenHeight = Common.SCREEN_HEIGHT;
        resources = this.getResources();
        Common.gameView = this;
        soundManager = new SoundManager();
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
            gamebackgroundsound.setVolume(0.1f, 0.1f);
            gamebackgroundsound.setLooping(true);
            gamebackgroundsound.start();

        }
        isGameStart = true;
    }

    public void GameEndSignal() {
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
        }
        //Draw background and grass
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
        for (MapObject mapObject : map.mapFlyingObjects) {
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
        if(FPS) {
            map.DrawFPS(canvas);
        }
        if (isGameEnd) {
            gameEndFrameCounter++;
            canvas.drawARGB(60, 0, 0, 0);
            Bitmap flag;
            if (isWin) {
                flag = map.imageCaches.get("win.png");
                canvas.drawBitmap(flag,
                        (screenWidth * 6) / Common.GAME_WIDTH_UNIT,
                        (screenHeight * 2) / Common.GAME_HEIGHT_UNIT,
                        null);
            } else {
                flag = map.imageCaches.get("lose.png");
                canvas.drawBitmap(flag,
                        (screenWidth * 6) / Common.GAME_WIDTH_UNIT,
                        (screenHeight * 2) / Common.GAME_HEIGHT_UNIT,
                        null);
            }
            /*
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStrokeWidth(3);
            paint.setTextSize(screenWidth / 30);
            String str = context.getString(R.string.game_view_get_money) + getMoney;
            paint.setColor(context.getResources().getColor(R.color.theme_color_font));
            paint.setTextAlign(Paint.Align.CENTER);
            if (isWin) {
                canvas.drawText(str, screenWidth / 2, screenHeight * 3 / 4, paint);
            } else {
                canvas.drawText(str, screenWidth / 2, screenHeight * 5 / 6, paint);
            }
            */
            if (gameEndFrameCounter > 5000 / Common.GAME_REFRESH) {
                gameThread.stop = true;
                GameEndSignal();
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

    public void SaveMap() {
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
        for (MapObject mapObject : map.centerObjects) {
            mapObject.Draw(canvas);
        }
        try {
            // 輸出的圖檔位置

            File file = new File(Environment.getExternalStorageDirectory() + "/bombisland2/" + map.id + ".png");
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
        SaveMapAll();
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
        int money = 0;
        if (moneyRecord != null) {
            money += Integer.parseInt(moneyRecord) + getMoney;
        }
        if (BT_MODE) {
            if (isWin) {
                money += 500;
            }
        } else {
            if (isWin) {
                money += 200 * aiCount;
            }
        }
        recorder.Write(String.valueOf(money), file);
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
        if (gameTime > 75) {
            hadPlayExplosion = false;
            if (isGameEnd) {
                if (!hadSaveGameEnd) {
                    hadSaveGameEnd = true;
                    SaveData();
                }
            }
            for (MapObject mapObject : map.mapObjects) {
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
            map.removeExplosions.removeAllElements();
            map.explosionLocations.removeAllElements();
            map.addMapObjects.removeAllElements();
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
            for (MapObject mapObject : map.addMapObjects) {
                map.mapObjects.add(mapObject);
            }
            map.mapObjects.removeAll(map.removeMapObjects);
            map.explosions.removeAll(map.removeExplosions);

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
                for (Ai ai : map.ais) {
                    try {
                        ai.Move();
                        ai.CheckBubble();
                        ai.CheckItem();
                        if (ai.IsDead) {
                            continue;
                        }
                        ai.CheckDead();
                    } catch (Exception e) {
                        e.getCause();
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
                        map.mapObjects.add(obj);
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
                    continue;
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
        if (x > screenWidth / 2) {
            double widthOffset = screenWidth / Common.GAME_WIDTH_UNIT;
            double heightOffset = screenHeight / Common.GAME_HEIGHT_UNIT;
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
                    double widthOffset = screenWidth / Common.GAME_WIDTH_UNIT;
                    double heightOffset = screenHeight / Common.GAME_HEIGHT_UNIT;
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
                touchInitX = (screenWidth * (-1 + (Common.GAME_WIDTH_UNIT - Common.MAP_WIDTH_UNIT) / 2)) / Common.GAME_WIDTH_UNIT;
                touchInitY = (screenHeight * 7) / Common.GAME_HEIGHT_UNIT;
                double widthOffset = screenWidth / Common.GAME_WIDTH_UNIT;
                double heightOffset = screenHeight / Common.GAME_HEIGHT_UNIT;
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
                int controlW = 5 * screenWidth / Common.GAME_WIDTH_UNIT;
                int controlH = 5 * screenHeight / Common.GAME_HEIGHT_UNIT;
                x -= screenWidth / Common.GAME_WIDTH_UNIT;
                y -= 6 * screenHeight / Common.GAME_HEIGHT_UNIT;
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
}
