package com.infinitewing.bombisland2.GameObject;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.infinitewing.bombisland2.GameObject.Parser.MapObjectParser;

import java.util.ListIterator;
import java.util.Random;
import java.util.Vector;


/**
 * Created by InfiniteWing on 2016/8/18.
 */
public class MapObject {
    public static final int TYPE_OTHER = 1, TYPE_ITEM = 2,
            TYPE_DESTROYABLE = 3, TYPE_DESTROYABLE_MOVABLE = 4,
            TYPE_BOMB = 5, TYPE_CENTER_OBJ = 6;
    public Location location, startLocation;
    public Animation animation;
    public int type;
    public int totalTime = 0;
    public int power;
    public int addPower, addWaterBall, addSpeed, addMoney, itemInvincible;
    public int randNum;
    public String id;
    public String mountID;
    public Boolean IsEnd;
    public Map map;
    public Player player;
    public String imgName;
    public Vector<Location> centerObstacles;
    public MapObject item;

    public MapObject(int x, int y, int type, String id, Map map) {
        location = new Location(x, y);
        IsEnd = false;
        this.type = type;
        this.map = map;
        this.id = id;
        mountID = null;
        addMoney = 0;
        addWaterBall = 0;
        addPower = 0;
        addSpeed = 0;
        animation = new Animation(map);
        if (type == MapObject.TYPE_DESTROYABLE || type == MapObject.TYPE_DESTROYABLE_MOVABLE) {
            randNum = new Random().nextInt(1000);
            SetExplosionItem();
            map.mapObstacles[x][y] = 2;
        } else {
            randNum = 0;
            if (type == MapObject.TYPE_OTHER) {
                map.mapObstacles[x][y] = 1;
            }
        }
        if (type == MapObject.TYPE_CENTER_OBJ) {
            centerObstacles = new Vector<>();
        }
        if (type == MapObject.TYPE_ITEM) {
            itemInvincible = 4;
        }
        Parse();
    }

    public void SetExplosionItem() {
        if (type == MapObject.TYPE_DESTROYABLE || type == MapObject.TYPE_DESTROYABLE_MOVABLE) {
            if (randNum < 200) {
                item = new MapObject(location.x, location.y, MapObject.TYPE_ITEM, "item_bomb", map);
            } else if (randNum < 400) {
                item = new MapObject(location.x, location.y, MapObject.TYPE_ITEM, "item_power", map);
            } else if (randNum < 600) {
                item = new MapObject(location.x, location.y, MapObject.TYPE_ITEM, "item_speed", map);
            } else if (randNum < 635) {
                item = new MapObject(location.x, location.y, MapObject.TYPE_ITEM, "item_max", map);
            } else if (randNum < 740) {
                item = new MapObject(location.x, location.y, MapObject.TYPE_ITEM, "item_money50", map);
            } else if (randNum < 790) {
                item = new MapObject(location.x, location.y, MapObject.TYPE_ITEM, "item_money500", map);
            } else if (randNum < 820) {
                item = new MapObject(location.x, location.y, MapObject.TYPE_ITEM, "item_money10000", map);
            } else if (randNum < 840) {
                item = new MapObject(location.x, location.y, MapObject.TYPE_ITEM, "item_mount01", map);
            } else if (randNum < 850) {
                item = new MapObject(location.x, location.y, MapObject.TYPE_ITEM, "item_mount02", map);
            } else if (randNum < 875) {
                item = new MapObject(location.x, location.y, MapObject.TYPE_ITEM, "item_mount03", map);
            } else if (randNum < 925) {
                item = new MapObject(location.x, location.y, MapObject.TYPE_ITEM, "item_mount04", map);
            }
        }
    }

    public void Parse() {
        if (type == MapObject.TYPE_ITEM) {
            MapObject cache = map.itemCaches.get(id);
            if (cache != null) {
                imgName = cache.imgName;
                addSpeed = cache.addSpeed;
                addPower = cache.addPower;
                addWaterBall = cache.addWaterBall;
                addMoney = cache.addMoney;
                animation.loop = cache.animation.loop;
                animation.source_width_unit = cache.animation.source_width_unit;
                animation.source_height_unit = cache.animation.source_height_unit;
                animation.source_width = cache.animation.source_width;
                animation.source_height = cache.animation.source_height;
                animation.source_x = cache.animation.source_x;
                animation.source_y = cache.animation.source_y;
                animation.width_unit = cache.animation.width_unit;
                animation.height_unit = cache.animation.height_unit;
                animation.step = cache.animation.step;
                animation.total_step = cache.animation.total_step;
                animation.ms_per_frame = cache.animation.ms_per_frame;
                animation.loop_frame = cache.animation.loop_frame;
                animation.loop_step = cache.animation.loop_step;
                animation.loop_total = cache.animation.loop_total;
                animation.source = map.imageCaches.get(imgName);
                mountID = cache.mountID;
                animation.Init();
                return;
            }
        }
        MapObjectParser mapObjectParser = new MapObjectParser(this);
        mapObjectParser.Parse();
    }

    public void Draw(Canvas canvas) {
        if(animation.img==null){
            animation.InitImage();
        }
        if (!IsEnd) {
            if (startLocation != null) {//flyingObject
                int offsetX = Math.abs(location.x - startLocation.x);
                int offsetY = Math.abs(location.y - startLocation.y);
                float timeRate = ((float) (totalTime / Common.GAME_REFRESH) / 25);
                float nowX = startLocation.x > location.x
                        ? startLocation.x - offsetX * timeRate : startLocation.x + offsetX * timeRate;
                float nowY = startLocation.y > location.y
                        ? startLocation.y - offsetY * timeRate : startLocation.y + offsetY * timeRate;
                float x = (nowX + (Common.GAME_WIDTH_UNIT - Common.MAP_WIDTH_UNIT) / 2) + animation.offset_width_unit;
                float y = nowY + animation.offset_height_unit;
                canvas.drawBitmap(animation.img,
                        (Common.gameView.screenWidth * x) / Common.GAME_WIDTH_UNIT,
                        (Common.gameView.screenHeight * y) / Common.GAME_HEIGHT_UNIT,
                        null);
            } else {
                int x = (location.x + (Common.GAME_WIDTH_UNIT - Common.MAP_WIDTH_UNIT) / 2) + animation.offset_width_unit;
                int y = location.y + animation.offset_height_unit;
                canvas.drawBitmap(animation.img,
                        (Common.gameView.screenWidth * x) / Common.GAME_WIDTH_UNIT,
                        (Common.gameView.screenHeight * y) / Common.GAME_HEIGHT_UNIT,
                        null);
            }
        }
    }

    public void Draw(int x, int y, Canvas canvas) {
        if(animation.img==null){
            animation.InitImage();
        }
        int tmp_x = x + (Common.GAME_WIDTH_UNIT - Common.MAP_WIDTH_UNIT) * Common.PLAYER_POSITION_RATE / 2;
        int tmp_y = y;
        tmp_x *= Common.gameView.screenWidth;
        tmp_y *= Common.gameView.screenHeight;
        tmp_x /= Common.PLAYER_POSITION_RATE;
        tmp_y /= Common.PLAYER_POSITION_RATE;
        Paint alphaPaint = new Paint();

        canvas.drawBitmap(animation.img,
                tmp_x / Common.GAME_WIDTH_UNIT,
                tmp_y / Common.GAME_HEIGHT_UNIT,
                alphaPaint);
    }

    public void BombExplosion() {
        map.mapObstacles[location.x][location.y] = 0;
        IsEnd = true;
        if (player != null) {
            player.wb++;
            if (player.wb > player.wb_max) {
                player.wb = player.wb_max;
            }
        }
        if (!Common.gameView.hadPlayExplosion) {
            Common.gameView.StartVibrator();
            Common.gameView.soundManager.addSound("explosion.mp3");
            Common.gameView.hadPlayExplosion = true;
        }
        Explosion e = new Explosion(new Location(location.x, location.y));
        e.delay = 0;
        map.explosions.add(e);

        for (int x = location.x; x >= 0 && x >= location.x - power; x--) {
            boolean HadBomb = false;
            if (x == location.x) {
                continue;
            }
            MapObject mapObject = map.GetBomb(x, location.y);
            if (mapObject != null) {
                if (!mapObject.IsEnd) {
                    mapObject.BombExplosion();
                    HadBomb = true;
                }
            }
            if (!HadBomb) {
                if (map.mapObstacles[x][location.y] == 2) {
                    Explosion explosion = new Explosion(new Location(x, location.y));
                    explosion.delay = Math.abs(location.x - x) * Common.GAME_REFRESH;
                    map.explosions.add(explosion);
                    break;
                } else if (map.mapObstacles[x][location.y] == 1) {
                    break;
                }
                Explosion explosion = new Explosion(new Location(x, location.y));
                explosion.delay = Math.abs(location.x - x) * Common.GAME_REFRESH;
                map.explosions.add(explosion);
            }

        }
        for (int x = location.x; x < Common.MAP_WIDTH_UNIT && x <= location.x + power; x++) {
            boolean HadBomb = false;
            if (x == location.x) {
                continue;
            }
            MapObject mapObject = map.GetBomb(x, location.y);
            if (mapObject != null) {
                if (!mapObject.IsEnd) {
                    mapObject.BombExplosion();
                    HadBomb = true;
                }
            }
            if (!HadBomb) {
                if (map.mapObstacles[x][location.y] == 2) {
                    Explosion explosion = new Explosion(new Location(x, location.y));
                    explosion.delay = Math.abs(location.x - x) * Common.GAME_REFRESH;
                    map.explosions.add(explosion);
                    break;
                } else if (map.mapObstacles[x][location.y] == 1) {
                    break;
                }
                Explosion explosion = new Explosion(new Location(x, location.y));
                explosion.delay = Math.abs(location.x - x) * Common.GAME_REFRESH;
                map.explosions.add(explosion);
            }
        }

        for (int y = location.y; y < Common.MAP_HEIGHT_UNIT && y <= location.y + power; y++) {
            boolean HadBomb = false;
            if (y == location.y) {
                continue;
            }

            MapObject mapObject = map.GetBomb(location.x, y);
            if (mapObject != null) {
                if (!mapObject.IsEnd) {
                    mapObject.BombExplosion();
                    HadBomb = true;
                }
            }
            if (!HadBomb) {
                if (map.mapObstacles[location.x][y] == 2) {
                    Explosion explosion = new Explosion(new Location(location.x, y));
                    explosion.delay = Math.abs(location.y - y) * Common.GAME_REFRESH;
                    map.explosions.add(explosion);
                    break;
                } else if (map.mapObstacles[location.x][y] == 1) {
                    break;
                }
                Explosion explosion = new Explosion(new Location(location.x, y));
                explosion.delay = Math.abs(location.y - y) * Common.GAME_REFRESH;
                map.explosions.add(explosion);
            }
        }
        for (int y = location.y; y >= 0 && y >= location.y - power; y--) {
            boolean HadBomb = false;
            if (y == location.y) {
                continue;
            }
            MapObject mapObject = map.GetBomb(location.x, y);
            if (mapObject != null) {
                if (!mapObject.IsEnd) {
                    mapObject.BombExplosion();
                    HadBomb = true;
                }
            }
            if (!HadBomb) {
                if (map.mapObstacles[location.x][y] == 2) {
                    Explosion explosion = new Explosion(new Location(location.x, y));
                    explosion.delay = Math.abs(location.y - y) * Common.GAME_REFRESH;
                    map.explosions.add(explosion);
                    break;
                } else if (map.mapObstacles[location.x][y] == 1) {
                    break;
                }
                Explosion explosion = new Explosion(new Location(location.x, y));
                explosion.delay = Math.abs(location.y - y) * Common.GAME_REFRESH;
                map.explosions.add(explosion);
            }
        }
    }

    public void Play() {
        totalTime += Common.GAME_REFRESH;
        if (!IsEnd) {
            itemInvincible--;
            animation.Play();
            if (animation.IsEnd() && !animation.loop) {
                if (type == TYPE_BOMB) {
                    try {
                        BombExplosion();
                    } catch (Exception e) {
                        e.getCause();
                    }
                }
            }
        }
    }

    public void Explosion(Location location) {
        if (!IsEnd) {
            try {
                if (location.x == this.location.x && location.y == this.location.y) {
                    if (type == MapObject.TYPE_DESTROYABLE || type == MapObject.TYPE_DESTROYABLE_MOVABLE) {
                        if (item != null) {
                            map.mapObstacles[location.x][location.y] = -1;
                            map.addMapObjects.add(item);
                        } else {
                            map.mapObstacles[location.x][location.y] = 0;
                        }
                        IsEnd = true;
                    } else if (type == MapObject.TYPE_ITEM) {
                        if (itemInvincible < 0) {
                            IsEnd = true;
                            map.mapObstacles[location.x][location.y] = 0;
                        }
                    }
                }
            } catch (Exception e) {
                e.getCause();
            }
        }
    }
}
