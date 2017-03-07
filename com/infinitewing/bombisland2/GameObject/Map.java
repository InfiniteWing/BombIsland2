package com.infinitewing.bombisland2.GameObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.infinitewing.bombisland2.GameView;
import com.infinitewing.bombisland2.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

/**
 * Created by InfiniteWing on 2016/8/18.
 */
public class Map {
    public Vector<MapObject> mapObjects;
    public Vector<MapObject> addMapObjects;
    public Vector<MapObject> removeMapObjects;
    public Vector<MapObject> mapFlyingObjects;
    public Vector<MapObject> bombs;
    public Vector<MapObject> centerObjects;
    public Vector<Explosion> explosions;
    public Vector<Explosion> removeExplosions;
    public Vector<Player> players;
    public Vector<Ai> ais;
    public Vector<Location> startLocations;
    public Vector<Location> explosionLocations;
    public HashMap<String, MapObject> itemCaches;
    public HashMap<String, Bitmap> imageCaches;
    public int[][] mapObstacles;
    public String id, playerID1, playerID2, playerUID;
    public String BGM, title, intro;
    public Vector<String> aiInfos;
    public int MaxPlayer=0, aiCount, autoBombCounter = 0, price;
    public boolean haveAI = false,bombLock=false;

    public Map(String id, Context c) {
        this.id = id;
        LoadMapInfo(c);
    }

    public Map(String id, String playerID, int aiCount, String aiInfo) {
        this.id = id;
        this.playerID1 = playerID;
        this.playerUID = playerID;
        this.aiCount = aiCount;
        aiInfos = new Vector<>();
        haveAI = true;

        for (String s : aiInfo.split(",")) {
            aiInfos.add(s);
        }
        Init(false, false);
    }

    public Map(String id, String playerID1, String playerID2, Boolean BT_MODE, Boolean IS_SERVER, int PlayerIndex) {
        this.id = id;
        this.playerID1 = playerID1;
        this.playerID2 = playerID2;
        this.playerUID = playerID1 + "_" + String.valueOf(PlayerIndex);
        Init(BT_MODE, IS_SERVER);
    }

    public void Init(Boolean BT_MODE, Boolean IS_SERVER) {
        mapObjects = new Vector<>();
        addMapObjects = new Vector<>();
        removeMapObjects = new Vector<>();
        mapFlyingObjects=new Vector<>();
        centerObjects = new Vector<>();
        explosions = new Vector<>();
        removeExplosions=new Vector<>();
        players = new Vector<>();
        bombs = new Vector<>();
        ais = new Vector<>();
        startLocations = new Vector<>();
        explosionLocations=new Vector<>();
        imageCaches = new HashMap<>();
        itemCaches = new HashMap<>();

        InitMapObstacle();
        InitSystemBitmap();
        InitExplosion();
        InitItem();
        LoadMap();
        if (BT_MODE) {
            if (IS_SERVER) {
                InitPlayer(BT_MODE, IS_SERVER);
            }
        } else {
            InitPlayer(BT_MODE, IS_SERVER);
        }
    }

    public void InitMapObstacle() {
        mapObstacles = new int[Common.MAP_WIDTH_UNIT][Common.MAP_HEIGHT_UNIT];
        for (int x = 0; x < Common.MAP_WIDTH_UNIT; x++) {
            for (int y = 0; y < Common.MAP_HEIGHT_UNIT; y++) {
                mapObstacles[x][y] = 0;
            }
        }
    }

    public void InitSystemBitmap() {
        Bitmap source = Common.getBitmapFromAsset("map/direction.png");
        Matrix matrix = new Matrix();
        matrix.reset();
        float width = 5 * Common.gameView.screenWidth / Common.GAME_WIDTH_UNIT;
        float height = 5 * Common.gameView.screenHeight / Common.GAME_HEIGHT_UNIT;
        matrix.postScale((width / source.getWidth()), height / source.getHeight());

        source = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight(), matrix, false);
        imageCaches.put("direction.png", source);

        source = Common.getBitmapFromAsset("map/three.png");
        matrix.reset();
        width = 6 * Common.gameView.screenWidth / Common.GAME_WIDTH_UNIT;
        height = 6 * Common.gameView.screenHeight / Common.GAME_HEIGHT_UNIT;
        matrix.postScale((width / source.getWidth()), height / source.getHeight());

        source = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight(), matrix, false);
        imageCaches.put("three.png", source);

        source = Common.getBitmapFromAsset("map/two.png");
        matrix.reset();
        width = 6 * Common.gameView.screenWidth / Common.GAME_WIDTH_UNIT;
        height = 6 * Common.gameView.screenHeight / Common.GAME_HEIGHT_UNIT;
        matrix.postScale((width / source.getWidth()), height / source.getHeight());

        source = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight(), matrix, false);
        imageCaches.put("two.png", source);

        source = Common.getBitmapFromAsset("map/one.png");
        matrix.reset();
        width = 6 * Common.gameView.screenWidth / Common.GAME_WIDTH_UNIT;
        height = 6 * Common.gameView.screenHeight / Common.GAME_HEIGHT_UNIT;
        matrix.postScale((width / source.getWidth()), height / source.getHeight());

        source = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight(), matrix, false);
        imageCaches.put("one.png", source);

        source = Common.getBitmapFromAsset("map/target.png");
        matrix.reset();
        width = 4 * Common.gameView.screenWidth / Common.GAME_WIDTH_UNIT;
        height = 4 * Common.gameView.screenHeight / Common.GAME_HEIGHT_UNIT;
        matrix.postScale((width / source.getWidth()), height / source.getHeight());

        source = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight(), matrix, false);
        imageCaches.put("target.png", source);

        source = Common.getBitmapFromAsset("map/direction_out.png");
        matrix.reset();
        width = 4 * Common.gameView.screenWidth / Common.GAME_WIDTH_UNIT;
        height = 4 * Common.gameView.screenHeight / Common.GAME_HEIGHT_UNIT;
        matrix.postScale((width / source.getWidth()), height / source.getHeight());

        source = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight(), matrix, false);
        imageCaches.put("direction_out.png", source);

        source = Common.getBitmapFromAsset("map/direction_in.png");
        matrix.reset();
        width = 2 * Common.gameView.screenWidth / Common.GAME_WIDTH_UNIT;
        height = 2 * Common.gameView.screenHeight / Common.GAME_HEIGHT_UNIT;
        matrix.postScale((width / source.getWidth()), height / source.getHeight());

        source = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight(), matrix, false);
        imageCaches.put("direction_in.png", source);

        source = Common.getBitmapFromAsset("map/lose.png");
        matrix.reset();
        width = 8 * Common.gameView.screenWidth / Common.GAME_WIDTH_UNIT;
        height = 8 * Common.gameView.screenHeight / Common.GAME_HEIGHT_UNIT;
        matrix.postScale((width / source.getWidth()), height / source.getHeight());
        source = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight(), matrix, false);
        imageCaches.put("lose.png", source);

        source = Common.getBitmapFromAsset("map/win.png");
        matrix.reset();
        width = 8 * Common.gameView.screenWidth / Common.GAME_WIDTH_UNIT;
        height = 8 * Common.gameView.screenHeight / Common.GAME_HEIGHT_UNIT;
        matrix.postScale((width / source.getWidth()), height / source.getHeight());
        source = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight(), matrix, false);
        imageCaches.put("win.png", source);

        source = Common.getBitmapFromAsset("map/bubble01.png");
        matrix.reset();
        width = 1 * Common.gameView.screenWidth / Common.GAME_WIDTH_UNIT;
        height = 1 * Common.gameView.screenHeight / Common.GAME_HEIGHT_UNIT;
        matrix.postScale((width / source.getWidth()), height / source.getHeight());
        source = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight(), matrix, false);
        imageCaches.put("bubble01.png", source);

        source = Common.getBitmapFromAsset("map/tree1.png");
        matrix.reset();
        width = 1 * Common.gameView.screenWidth / Common.GAME_WIDTH_UNIT;
        height = 1 * Common.gameView.screenHeight / Common.GAME_HEIGHT_UNIT;
        matrix.postScale((width / source.getWidth()), height / source.getHeight());
        source = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight(), matrix, false);
        imageCaches.put("tree1.png", source);
    }
    public void InitMapGrass(String grassID) {
        Bitmap source = Common.getBitmapFromAsset("map/" + grassID);
        Matrix matrix = new Matrix();
        matrix.reset();
        float width = (float) (Common.gameView.screenWidth / Common.GAME_WIDTH_UNIT + 0.5);
        float height = (float) (Common.gameView.screenHeight / Common.GAME_HEIGHT_UNIT + 0.5);
        matrix.postScale((width / source.getWidth()), height / source.getHeight());
        source = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight(), matrix, false);
        imageCaches.put("grass.png", source);
    }

    public void InitItem() {
        String path = "xml/map/item_list.txt";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(Common.gameView.getContext().getAssets().open(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line = null;
        do {
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line == null) {
                break;
            }
            if (line.length() <= 2) {
                continue;
            }
            MapObject mapObject = new MapObject(1, 1, MapObject.TYPE_ITEM, line, this);
            itemCaches.put(mapObject.id, mapObject);
        } while (line != null);
    }

    public void InitExplosion() {
        String imgName = "map/bomb.png";
        Animation animation = new Animation(this);
        animation.loop = false;
        animation.source_width_unit = 6;
        animation.source_height_unit = 10;
        animation.source_x = 0;
        animation.source_y = 5;
        animation.step = 1;
        animation.total_step = 6;
        animation.ms_per_frame = Common.GAME_REFRESH * 1;
        animation.width_unit = 1;
        animation.height_unit = 1;
        if (imageCaches.get(imgName) == null) {
            Bitmap source = Common.getBitmapFromAsset(imgName);
            Matrix matrix = new Matrix();
            matrix.reset();
            float width = animation.source_width_unit * Common.gameView.screenWidth / Common.GAME_WIDTH_UNIT;
            float height = animation.source_height_unit * Common.gameView.screenHeight / Common.GAME_HEIGHT_UNIT;
            matrix.postScale(width / source.getWidth(), height / source.getHeight());
            source = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
            imageCaches.put(imgName, source);
        }
        animation.source = imageCaches.get(imgName);
        animation.Init();
        while (!animation.IsEnd()) {
            animation.Play();
        }
    }

    public void LoadMapInfo(Context c) {
        BufferedReader reader = null;
        String path = "xml/map_info/" + id + ".txt";
        try {
            reader = new BufferedReader(new InputStreamReader(c.getAssets().open(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line = null;
        int y = 0;
        do {
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line == null) {
                break;
            }
            if (y < Common.MAP_HEIGHT_UNIT) {
                for (int x = 0; x < line.length();x++ ) {
                    switch (line.charAt(x)) {
                        case 'Ｓ'://Player Start Point
                            MaxPlayer++;
                            break;
                        default:
                            break;
                    }
                }
            } else {
                String tag = line.split(":")[0], value = line.split(":")[1];
                if (tag.equals("TITLE")) {
                    title = value;
                    title = Common.getStringResourceByName("map_info_title_"+id,c);
                }
                if (tag.equals("INTRO")) {
                    intro = value;
                    intro = Common.getStringResourceByName("map_info_intro_"+id,c);
                }
            }
            if (line.length() > 10) {
                y++;
            }
        } while (line != null);
    }

    public void LoadMap() {
        BufferedReader reader = null;
        String path = "xml/map_info/" + id + ".txt";
        try {
            reader = new BufferedReader(new InputStreamReader(Common.gameView.getContext().getAssets().open(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line = null;
        int y = 0;
        do {
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line == null) {
                break;
            }
            if (y < Common.MAP_HEIGHT_UNIT) {
                for (int x = 0; x < line.length(); x++) {
                    switch (line.charAt(x)) {
                        case 'Ｓ'://Player Start Point
                            MaxPlayer++;
                            Location location = new Location(x, y);
                            startLocations.add(location);
                            break;
                        case '０'://empty
                            break;
                        case 'Ｇ'://Goast
                            Ai ai = new Ai("goast01", x, y, this, Ai.GOAST);
                            ais.add(ai);
                            break;
                        case 'Ｂ'://Box
                            MapObject box = new MapObject(x, y, MapObject.TYPE_DESTROYABLE, "box01", this);
                            mapObjects.add(box);
                            break;
                        case 'Ｔ'://Tree
                            MapObject tree = new MapObject(x, y, MapObject.TYPE_OTHER, "tree1", this);
                            mapObjects.add(tree);
                            break;
                        case 'Ｈ'://house1
                            MapObject house = new MapObject(x, y, MapObject.TYPE_OTHER, "house1", this);
                            mapObjects.add(house);
                            break;
                        case 'ｈ'://Tree
                            MapObject house2 = new MapObject(x, y, MapObject.TYPE_OTHER, "house2", this);
                            mapObjects.add(house2);
                            break;
                        case 'ｍ'://Mushroom01
                            MapObject mushroom1 = new MapObject(x, y, MapObject.TYPE_DESTROYABLE, "mushroom1", this);
                            mapObjects.add(mushroom1);
                            break;
                        case 'Ｍ'://Tree
                            MapObject mushroom2 = new MapObject(x, y, MapObject.TYPE_DESTROYABLE, "mushroom2", this);
                            mapObjects.add(mushroom2);
                            break;
                        case 'Ｃ'://CenterObj02
                            MapObject center_obj02 = new MapObject(x, y, MapObject.TYPE_CENTER_OBJ, "center_obj02", this);
                            centerObjects.add(center_obj02);
                            break;
                        case 'Ａ'://CenterObj09
                            MapObject center_obj09 = new MapObject(x, y, MapObject.TYPE_CENTER_OBJ, "center_obj09", this);
                            centerObjects.add(center_obj09);
                            break;
                        case 'ｃ'://CenterObj01
                            MapObject center_obj01 = new MapObject(x, y, MapObject.TYPE_CENTER_OBJ, "center_obj01", this);
                            centerObjects.add(center_obj01);
                            break;
                        case 'c'://CenterObj03
                            MapObject center_obj03 = new MapObject(x, y, MapObject.TYPE_CENTER_OBJ, "center_obj03", this);
                            centerObjects.add(center_obj03);
                            break;
                        case 'C'://CenterObj04_1
                            MapObject center_obj04_1 = new MapObject(x, y, MapObject.TYPE_CENTER_OBJ, "center_obj04_1", this);
                            centerObjects.add(center_obj04_1);
                            break;
                        case 'd'://CenterObj04_2
                            MapObject center_obj04_2 = new MapObject(x, y, MapObject.TYPE_CENTER_OBJ, "center_obj04_2", this);
                            centerObjects.add(center_obj04_2);
                            break;
                        case 'D'://CenterObj04_3
                            MapObject center_obj04_3 = new MapObject(x, y, MapObject.TYPE_CENTER_OBJ, "center_obj04_3", this);
                            centerObjects.add(center_obj04_3);
                            break;
                        case 'ｄ'://CenterObj04_4
                            MapObject center_obj04_4 = new MapObject(x, y, MapObject.TYPE_CENTER_OBJ, "center_obj04_4", this);
                            centerObjects.add(center_obj04_4);
                            break;
                        case 't'://centerObg05
                            MapObject center_obj05 = new MapObject(x, y, MapObject.TYPE_CENTER_OBJ, "center_obj05", this);
                            centerObjects.add(center_obj05);
                            break;
                        case 'o'://centerObg06_1
                            MapObject center_obj06_1 = new MapObject(x, y, MapObject.TYPE_CENTER_OBJ, "center_obj06_1", this);
                            centerObjects.add(center_obj06_1);
                            break;
                        case 'O'://centerObg06_2
                            MapObject center_obj06_2 = new MapObject(x, y, MapObject.TYPE_CENTER_OBJ, "center_obj06_2", this);
                            centerObjects.add(center_obj06_2);
                            break;
                        case 'ｏ'://centerObg07
                            MapObject center_obj07 = new MapObject(x, y, MapObject.TYPE_CENTER_OBJ, "center_obj07", this);
                            centerObjects.add(center_obj07);
                            break;
                        case 'Ｕ'://underbrush02
                            MapObject underbrush02 = new MapObject(x, y, MapObject.TYPE_DESTROYABLE, "underbrush02", this);
                            mapObjects.add(underbrush02);
                            break;
                        case 'ｇ'://centerObg08
                            MapObject center_obj08 = new MapObject(x, y, MapObject.TYPE_CENTER_OBJ, "center_obj08", this);
                            centerObjects.add(center_obj08);
                            break;
                        case 'ｔ'://table01
                            MapObject table01 = new MapObject(x, y, MapObject.TYPE_DESTROYABLE, "table01", this);
                            mapObjects.add(table01);
                            break;
                        case 'ｉ'://intree01
                            MapObject intree01 = new MapObject(x, y, MapObject.TYPE_OTHER, "intree01", this);
                            mapObjects.add(intree01);
                            break;
                        case 'ａ'://auto_bomb
                            Player.AutoAddBomb(x, y, this, autoBombCounter++);
                            break;
                    }
                }
            } else {
                String tag = line.split(":")[0], value = line.split(":")[1];
                if (tag.equals("TITLE")) {
                    title = value;
                }
                if (tag.equals("INTRO")) {
                    intro = value;
                }
                if (tag.equals("GRASS")) {
                    InitMapGrass(value);
                }
                if (tag.equals("BGM")) {
                    BGM = value;
                }
            }
            if (line.length() > 10) {
                y++;
            }
        } while (line != null);
    }

    public void InitPlayer(Boolean BT_MODE, Boolean IS_SERVER) {
        Random ran = new Random();
        int randomNum = ran.nextInt(startLocations.size());
        Vector<Integer> randomNumLimit = new Vector<>();
        Player player;
        Ai ai;
        player = new Player(playerID1, startLocations.elementAt(randomNum).x, startLocations.elementAt(randomNum).y, this);
        player.uid = playerUID;
        player.teamID=1;
        if(BT_MODE) {
            if (IS_SERVER)
                player.InitEmotion("emotion_player");
            else
                player.InitEmotion("emotion_ai");
        }else{
            player.InitEmotion("emotion_player");
        }

        players.add(player);
        randomNumLimit.add(randomNum);
        if (BT_MODE) {
            while (true) {
                randomNum = ran.nextInt(startLocations.size());
                if (randomNumLimit.indexOf(randomNum) < 0) {
                    break;
                }
            }
            player = new Player(playerID2, startLocations.elementAt(randomNum).x, startLocations.elementAt(randomNum).y, this);
            player.uid = player.id + "_2";
            player.teamID=2;
            if(IS_SERVER)
                player.InitEmotion("emotion_ai");
            else
                player.InitEmotion("emotion_player");
            players.add(player);
        } else {
            if (aiCount > startLocations.size() - 1) {
                aiCount = startLocations.size() - 1;
            }
            int aiIndex = 0;
            while (aiCount > 0) {
                while (true) {
                    randomNum = ran.nextInt(startLocations.size());
                    if (randomNumLimit.indexOf(randomNum) < 0) {
                        break;
                    }
                }
                int aiLevel=Ai.IQ_10;
                if(Common.RandomNum(1000)>800){
                    aiLevel=Ai.IQ_30;
                }
                ai = new Ai(aiInfos.elementAt(aiIndex++), startLocations.elementAt(randomNum).x, startLocations.elementAt(randomNum).y, this, aiLevel);
                ai.InitEmotion("emotion_ai");
                ais.add(ai);
                randomNumLimit.add(randomNum);
                aiCount--;
            }
        }
    }

    public MapObject GetBomb(int x, int y) {
        bombLock=true;
        for (MapObject mapObject : bombs) {
            if (mapObject.location.x == x && mapObject.location.y == y) {
                return mapObject;
            }
        }
        bombLock=false;
        return null;
    }

    public Boolean FindMapObject(int x, int y, int direction) {
        if (direction == Location.LOCATION_DOWN && y + 1 == Common.MAP_HEIGHT_UNIT) {
            return true;
        }
        if (direction == Location.LOCATION_TOP && y == 0) {
            return true;
        }
        if (direction == Location.LOCATION_LEFT && x == 0) {
            return true;
        }
        if (direction == Location.LOCATION_RIGHT && x + 1 == Common.MAP_WIDTH_UNIT) {
            return true;
        }
        if (direction == Location.LOCATION_DOWN) {
            y++;
        }
        if (direction == Location.LOCATION_TOP) {
            y--;
        }
        if (direction == Location.LOCATION_LEFT) {
            x--;
        }
        if (direction == Location.LOCATION_RIGHT) {
            x++;
        }

        for (MapObject mapObject : mapObjects) {
            if (mapObject.location.x == x && mapObject.location.y == y) {
                if (mapObject.type == MapObject.TYPE_BOMB
                        || mapObject.type == MapObject.TYPE_OTHER
                        || mapObject.type == MapObject.TYPE_DESTROYABLE
                        || mapObject.type == MapObject.TYPE_DESTROYABLE_MOVABLE) {
                    return true;
                }
            }
        }
        return false;
    }
    public void DrawFPS(Canvas canvas){
        float startX = 10, startY = 10;
        float height = 30;
        float width = 240;
        float offsetY = 5;
        float fontSize = 26;
        float strokeWidth = 1;
        startX = Common.transWidth(startX);
        startY = Common.transHeight(startY);
        width = Common.transWidth(width);
        height = Common.transHeight(height);
        offsetY = Common.transHeight(offsetY);
        strokeWidth = Common.transWidth(strokeWidth);
        fontSize = Common.transFontSize(fontSize);
        offsetY += height + strokeWidth;
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        for (int i = 0; i < 1; i++) {
            paint.setAlpha(192);
            paint.setColor(Color.rgb(179, 181, 181));
            canvas.drawRect(startX - strokeWidth * 3, startY - strokeWidth * 3 + offsetY * i, startX + width + strokeWidth * 3, startY + height + strokeWidth * 3 + offsetY * i, paint);
            paint.setColor(Color.rgb(255, 251, 240));
            canvas.drawRect(startX - strokeWidth * 2, startY - strokeWidth * 2 + offsetY * i, startX + width + strokeWidth * 2, startY + height + offsetY * i + strokeWidth * 2, paint);
            paint.setColor(Color.rgb(2, 165, 156));
            float scale;
            scale = (float) Common.GAME_REFRESH / Common.gameView.sleep;
            if (scale > 1) {
                scale = 1;
            }
            if (scale < 0) {
                scale = 0;
            }
            float length = width * scale;
            canvas.drawRect(startX, startY + offsetY * i, startX + length, startY + height + offsetY * i, paint);
            paint.setAlpha(228);
            paint.setColor(Color.rgb(82, 92, 109));
            float FPS = (1000 / Common.gameView.sleep);
                DecimalFormat df = new DecimalFormat("#.##");
                canvas.drawText(df.format(FPS), startX + width / 2, startY + offsetY * i + fontSize, paint);
        }
    }
    public void Explosion(Location location) {
        for (MapObject mapObject : mapObjects) {
            mapObject.Explosion(location);
        }
    }

    public void CheckGameEnd(GameView gameView) {
        if (gameView.isGameEnd) {
            return;
        }
        if (gameView.BT_MODE) {
            for (Player player : players) {
                if (player.uid.equals(gameView.playerID)) {
                    if (player.IsDead) {
                        gameView.isWin = false;
                        gameView.isGameEnd = true;
                    }
                } else {
                    if (player.IsDead) {
                        gameView.isWin = true;
                        gameView.isGameEnd = true;
                    }
                }
            }
        } else {
            for (Player player : players) {
                if (player.uid.equals(gameView.playerID)) {
                    if (player.IsDead) {
                        gameView.isWin = false;
                        gameView.isGameEnd = true;
                    }
                }
            }
            int aliveAiCount = 0;
            for (Ai ai : ais) {
                if (ai.type != Ai.GOAST) {
                    if (!ai.IsDead) {
                        aliveAiCount++;
                    }
                }
            }
            if (aliveAiCount == 0) {
                gameView.isWin = true;
                gameView.isGameEnd = true;
            }
        }
        if (gameView.isGameEnd) {
            gameView.gamebackgroundsound.stop();
            if (gameView.isWin) {
                gameView.soundManager.addSound("win.mp3");
            } else {
                gameView.soundManager.addSound("lose.mp3");
            }
        }
    }
}
