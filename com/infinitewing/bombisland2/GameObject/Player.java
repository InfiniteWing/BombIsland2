package com.infinitewing.bombisland2.GameObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.infinitewing.bombisland2.GameObject.Parser.HeroParser;
import com.infinitewing.bombisland2.R;

import java.util.Vector;

/**
 * Created by InfiniteWing on 2016/8/18.
 */
public class Player {
    public int player_x, player_y;
    public int teamID;
    public int bubbledTime, wb, wb_now, wb_max, speed, speed_now, speed_max, wb_power, wb_power_max;
    public Boolean IsDead, IsMoving,IsMount,IsBubbled;
    public String id,uid;
    public Player mount;
    public Map map;
    public Character character;
    public String imgName,deadSound,heroName;
    public int price,mountDeadCounter;
    public Vector<String> eatItems;
    public Explosion deadExplosion;
    public MapObject emotion;
    public Player(String id,Context context){
        this.id = id;
        character = new Character(this);
        ParseInfo(context);
    }
    public Player(String id, int x, int y, Map map) {
        this.map = map;
        this.id = id;
        player_x = x * Common.PLAYER_POSITION_RATE;
        player_y = y * Common.PLAYER_POSITION_RATE;
        character = new Character(this);
        mountDeadCounter=0;
        teamID=1;
        mount=null;
        IsMoving = false;
        IsDead = false;
        IsMount=false;
        IsBubbled=false;
        speed_now = 0;
        eatItems=new Vector<>();
        Parse();
    }
    public void InitEmotion(String id){
        emotion=new MapObject(1,1,MapObject.TYPE_OTHER,id,map);
    }

    public void ParseInfo(Context context){
        HeroParser heroParser=new HeroParser(this);
        heroParser.ParseInfo(context);
    }
    public void Parse(){
        HeroParser heroParser=new HeroParser(this);
        heroParser.Parse();
    }
    public void Draw(Canvas canvas) {
        if(mount==null) {
            character.Draw(canvas);
        }else{
            if(character.direction==Location.LOCATION_TOP){
                mount.character.Draw(canvas);
                character.Draw(canvas);
            }else{
                character.Draw(canvas);
                mount.character.Draw(canvas);
            }
        }
    }
    public void DrawEmotion(Canvas canvas){
        if(!IsDead) {
            if (emotion != null) {
                emotion.Draw(player_x, player_y - 777, canvas);
            }
        }
    }

    public static void AutoAddBomb(int x, int y,Map map,int AutoBombCounter){
        MapObject bomb = new MapObject(x, y, MapObject.TYPE_BOMB, "bomb1", map);
        bomb.power = 1;
        map.bombs.add(bomb);
        map.mapObstacles[x][y] = 3;
    }
    public void AddBomb() {
        if(!IsDead&&!Common.gameView.isGameEnd) {
            if (wb > 0) {
                int x = (player_x + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
                int y = (player_y + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
                if (map.mapObstacles[x][y] == 0) {
                    MapObject bomb = new MapObject(x, y, MapObject.TYPE_BOMB, "bomb1", map);
                    bomb.power = wb_power;
                    bomb.player = this;
                    while(map.bombLock){
                    }
                    map.bombs.add(bomb);
                    wb--;
                    Common.gameView.soundManager.addSound("setbomb.mp3");
                    Common.gameView.BTPlayerAddBomb(x, y);
                    map.mapObstacles[x][y] = 3;
                }
            }
        }
    }
    public void AddBombBT(int x, int y,int timeOffset) {
        MapObject bomb = new MapObject(x, y, MapObject.TYPE_BOMB, "bomb1", map);
        bomb.power = wb_power;
        bomb.player = this;
        while(timeOffset>0){
            bomb.Play();
            timeOffset--;
        }
        while(map.bombLock){
        }
        map.bombs.add(bomb);
        Common.gameView.soundManager.addSound("setbomb.mp3");
        map.mapObstacles[x][y] = 3;
    }

    public void AddItem(MapObject mapObject) {
        wb_power += mapObject.addPower;
        if (wb_power > wb_power_max) {
            wb_power = wb_power_max;
        }
        speed += mapObject.addSpeed;
        if (speed > speed_max) {
            speed = speed_max;
        }
        wb += mapObject.addWaterBall;
        wb_now += mapObject.addWaterBall;
        if (wb_now > wb_max) {
            wb -= wb_now - wb_max;
            wb_now = wb_max;
        }
        if(uid!=null) {
            if (uid.equals(Common.gameView.playerID)) {
                Common.gameView.getMoney += mapObject.addMoney;
            }
        }
        if(mount==null&&mapObject.mountID!=null){
            mount = new Player("mount_"+mapObject.mountID, 0, 0, map);
            mount.player_x=player_x;
            mount.player_y=player_y;
            mount.character.direction=character.direction;
            mount.IsMount=true;
            mount.character.InitImage();
        }
        mapObject.IsEnd = true;
        map.mapObstacles[mapObject.location.x][mapObject.location.y]=0;
        Common.gameView.soundManager.addSound("pick.mp3");
        if(mapObject.type==MapObject.TYPE_ITEM){
            eatItems.add(mapObject.id);
        }
    }
    public String GetIntro(Context context){
        String intro="";
        intro+=context.getString(R.string.game_choose_hero_wb);
        for(int i=0;i<wb_max;i++){
            if(i<wb){
                intro+=context.getString(R.string.game_choose_hero_now);
            }else{
                intro+=context.getString(R.string.game_choose_hero_max);
            }
        }
        intro+="\n";
        intro+=context.getString(R.string.game_choose_hero_power);
        for(int i=0;i<wb_power_max;i++){
            if(i<wb_power){
                intro+=context.getString(R.string.game_choose_hero_now);
            }else{
                intro+=context.getString(R.string.game_choose_hero_max);
            }
        }

        intro+="\n";
        intro+=context.getString(R.string.game_choose_hero_speed);
        for(int i=40;i<=speed_max;i+=20){
            if(i<=speed){
                intro+=context.getString(R.string.game_choose_hero_now);
            }else{
                intro+=context.getString(R.string.game_choose_hero_max);
            }
        }
        return intro;
    }
    public void BTMove() {
        if(emotion!=null){
            emotion.Play();
        }
        character.Move();
        if(--mountDeadCounter > 0){
            if(mount!=null){
                mount.mountDeadCounter--;
            }
        }
        if(IsBubbled){
            bubbledTime+=Common.GAME_REFRESH;
            return;
        }
        if(mountDeadCounter==0){
            mount=null;
        }
        SetMountMove();
    }

    public void CheckDead(Boolean IsAi) {
        if(IsDead){
            return;
        }
        if(mountDeadCounter>0){//坐騎死亡時無敵
            return;
        }
        if(IsBubbled){
            if(bubbledTime>Common.GAME_REFRESH*130){
                PlayDead();
            }
            return;
        }
        for (Explosion explosion : map.explosions) {
            int x = (player_x + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            int y = (player_y + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            if (explosion.delay <= 0 && !explosion.IsEnd) {
                if (explosion.location.x == x && explosion.location.y == y) {
                    IsDead = true;
                }
            }
        }
        if(!IsAi) {
            //鬼魂系列死亡宣告
            for (Ai ai : map.ais) {
                if (ai.type == Ai.GOAST) {
                    int offset_x = Math.abs(player_x - ai.player_x);
                    int offset_y = Math.abs(player_y - ai.player_y);
                    if (offset_x <= Common.PLAYER_POSITION_RATE / 2
                            && offset_y <= Common.PLAYER_POSITION_RATE / 2) {
                        PlayDead();
                        return;
                    }
                }
            }
        }
        if(IsDead){
            if(mount!=null){
                Common.gameView.soundManager.addSound(deadSound);
                IsDead=false;
                mount.IsDead=true;
                mountDeadCounter=2000/Common.GAME_REFRESH;
                mount.mountDeadCounter=mountDeadCounter;
            }else {
                Common.gameView.soundManager.addSound("bubble_show.mp3");
                IsBubbled=true;
                bubbledTime=0;
                IsDead=false;
            }
        }
    }
    public void PlayDead(){
        deadExplosion = new Explosion(new Location(player_x/Common.PLAYER_POSITION_RATE,
                player_y/Common.PLAYER_POSITION_RATE));
        deadExplosion.delay = 0;
        IsDead=true;
        Common.gameView.soundManager.addSound(deadSound);
        Common.gameView.StartVibrator();
        ExplodeItems();
    }
    public void CheckItem() {
        for (MapObject mapObject : map.mapObjects) {
            int x = (player_x + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            int y = (player_y + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            if (mapObject.type == MapObject.TYPE_ITEM) {
                if (mapObject.location.x == x && mapObject.location.y == y && !mapObject.IsEnd) {
                    AddItem(mapObject);
                }
            }
        }
    }
    public void SetMountMove(){
        if(mount!=null){
            mount.speed_now=speed_now;
            mount.player_x=player_x;
            mount.player_y=player_y+350;
            mount.character.direction=character.direction;
            mount.character.Move();
            mount.character.InitImage();
        }
    }
    public void ExplodeItems(){
        int x,y;
        for(String id : eatItems){
            while(true) {
                x = Common.RandomNum(Common.MAP_WIDTH_UNIT) - 1;
                y = Common.RandomNum(Common.MAP_HEIGHT_UNIT) - 1;
                if(map.mapObstacles[x][y]==0){
                    if(player_x/Common.PLAYER_POSITION_RATE!=x
                            ||player_y/Common.PLAYER_POSITION_RATE!=y) {
                        break;
                    }
                }
            }
            MapObject obj=new MapObject(x,y,MapObject.TYPE_ITEM,id,map);
            obj.startLocation=new Location(player_x/Common.PLAYER_POSITION_RATE,
                    player_y/Common.PLAYER_POSITION_RATE);
            map.mapFlyingObjects.add(obj);
        }
    }
    public void CheckBubble(){
        if(IsBubbled||IsDead){
            return;
        }
        for (Ai ai: map.ais) {
            if(ai.IsBubbled&&!ai.IsDead){
                if(ai.bubbledTime<=80){
                    continue;
                }
                if(Math.abs(player_x-ai.player_x)<=Common.PLAYER_POSITION_RATE / 2
                        && Math.abs(player_y-ai.player_y)<=Common.PLAYER_POSITION_RATE / 2){
                    ai.IsBubbled=false;
                    if(teamID==ai.teamID){
                        Common.gameView.soundManager.addSound("bubble_save.mp3");
                    }else{
                        ai.PlayDead();
                    }
                }
            }
        }
        for (Player player: map.players) {
            if(player.IsBubbled){
                if(player.bubbledTime<=80){
                    continue;
                }
                if(Math.abs(player_x-player.player_x)<=Common.PLAYER_POSITION_RATE / 2
                        && Math.abs(player_y-player.player_y)<=Common.PLAYER_POSITION_RATE / 2){
                    if(teamID==player.teamID){
                        player.IsBubbled=false;
                        Common.gameView.soundManager.addSound("bubble_save.mp3");
                    }else{
                        player.PlayDead();
                    }
                }
            }
        }
    }
    public void Move() {
        if(emotion!=null){
            emotion.Play();
        }
        if(--mountDeadCounter>0){
            if(mount!=null){
                mount.mountDeadCounter--;
            }
            return;
        }
        if(mountDeadCounter==0){
            mount=null;
        }
        if(IsBubbled){
            bubbledTime+=Common.GAME_REFRESH;
            return;
        }
        if (IsMoving && !IsDead) {
            speed_now += 10;
            if(mount!=null){
                if (speed_now > mount.speed) {
                    speed_now = mount.speed;
                }
            }else {
                if (speed_now > speed) {
                    speed_now = speed;
                }
            }
            int tmp_x, tmp_y,offset;
            switch (character.direction) {
                case Location.LOCATION_TOP:
                    player_y -= speed_now;
                    if (player_y < 0) {
                        player_y = 0;
                    }
                    tmp_x = player_x / Common.PLAYER_POSITION_RATE;
                    tmp_y = player_y / Common.PLAYER_POSITION_RATE;
                    if (tmp_y != (player_y + speed_now) / Common.PLAYER_POSITION_RATE) {
                        //換Y軸才需計算
                        offset= player_x - tmp_x * Common.PLAYER_POSITION_RATE;
                        if (offset == 0) {
                            if (map.mapObstacles[tmp_x][tmp_y] >= 1) {
                                player_y = (tmp_y + 1) * Common.PLAYER_POSITION_RATE;
                            }
                        } else {
                            if (map.mapObstacles[tmp_x][tmp_y] >= 1
                                    && map.mapObstacles[tmp_x + 1][tmp_y] >= 1) {
                                player_y = (tmp_y + 1) * Common.PLAYER_POSITION_RATE;
                            } else if (map.mapObstacles[tmp_x][tmp_y] >= 1) {
                                player_y = (tmp_y + 1) * Common.PLAYER_POSITION_RATE;
                                player_x += speed_now / 2;
                                if (player_x > (tmp_x + 1) * Common.PLAYER_POSITION_RATE) {
                                    player_x = (tmp_x + 1) * Common.PLAYER_POSITION_RATE;
                                }
                            } else if (map.mapObstacles[tmp_x + 1][tmp_y] >= 1) {
                                player_y = (tmp_y + 1) * Common.PLAYER_POSITION_RATE;
                                player_x -= speed_now / 2;
                                if (player_x < (tmp_x) * Common.PLAYER_POSITION_RATE) {
                                    player_x = (tmp_x) * Common.PLAYER_POSITION_RATE;
                                }
                            }
                        }
                    }
                    break;
                case Location.LOCATION_RIGHT:
                    player_x += speed_now;
                    if (player_x > (Common.MAP_WIDTH_UNIT - 1) * Common.PLAYER_POSITION_RATE) {
                        player_x = (Common.MAP_WIDTH_UNIT - 1) * Common.PLAYER_POSITION_RATE;
                    }
                    tmp_x = (player_x+Common.PLAYER_POSITION_RATE-1) / Common.PLAYER_POSITION_RATE;
                    tmp_y =player_y / Common.PLAYER_POSITION_RATE;
                    if (tmp_x != (player_x - speed_now+Common.PLAYER_POSITION_RATE-1) / Common.PLAYER_POSITION_RATE) {
                        //換X軸才需計算
                        offset = player_y - tmp_y * Common.PLAYER_POSITION_RATE;
                        if (offset == 0) {
                            if (map.mapObstacles[tmp_x][tmp_y] >= 1) {
                                player_x = (tmp_x-1) * Common.PLAYER_POSITION_RATE;
                            }
                        } else {
                            if (map.mapObstacles[tmp_x][tmp_y] >= 1
                                    && map.mapObstacles[tmp_x ][tmp_y+ 1] >= 1) {
                                player_x = (tmp_x-1) * Common.PLAYER_POSITION_RATE;
                            } else if (map.mapObstacles[tmp_x][tmp_y] >= 1) {
                                player_x = (tmp_x-1) * Common.PLAYER_POSITION_RATE;
                                player_y += speed_now / 2;
                                if (player_y > (tmp_y + 1) * Common.PLAYER_POSITION_RATE) {
                                    player_y = (tmp_y + 1) * Common.PLAYER_POSITION_RATE;
                                }
                            } else if (map.mapObstacles[tmp_x][tmp_y + 1] >= 1) {
                                player_x = (tmp_x-1) * Common.PLAYER_POSITION_RATE;
                                player_y -= speed_now / 2;
                                if (player_y < (tmp_y) * Common.PLAYER_POSITION_RATE) {
                                    player_y = (tmp_y) * Common.PLAYER_POSITION_RATE;
                                }
                            }
                        }
                    }

                    break;
                case Location.LOCATION_DOWN:
                    player_y += speed_now;
                    if (player_y >= (Common.MAP_HEIGHT_UNIT - 1) * Common.PLAYER_POSITION_RATE) {
                        player_y = (Common.MAP_HEIGHT_UNIT - 1) * Common.PLAYER_POSITION_RATE;
                        break;
                    }
                    tmp_x = player_x / Common.PLAYER_POSITION_RATE;
                    tmp_y =(player_y+Common.PLAYER_POSITION_RATE-1) / Common.PLAYER_POSITION_RATE;
                    if (tmp_y != (player_y - speed_now+Common.PLAYER_POSITION_RATE-1) / Common.PLAYER_POSITION_RATE) {
                        //換Y軸才需計算
                        offset = player_x - tmp_x * Common.PLAYER_POSITION_RATE;
                        if (offset == 0) {
                            if (map.mapObstacles[tmp_x][tmp_y] >= 1) {
                                player_y = (tmp_y-1) * Common.PLAYER_POSITION_RATE;
                            }
                        } else {
                            if (map.mapObstacles[tmp_x][tmp_y] >= 1
                                    && map.mapObstacles[tmp_x + 1][tmp_y] >= 1) {
                                player_y = (tmp_y-1) * Common.PLAYER_POSITION_RATE;
                            } else if (map.mapObstacles[tmp_x][tmp_y] >= 1) {
                                player_y = (tmp_y-1) * Common.PLAYER_POSITION_RATE;
                                player_x += speed_now / 2;
                                if (player_x > (tmp_x + 1) * Common.PLAYER_POSITION_RATE) {
                                    player_x = (tmp_x + 1) * Common.PLAYER_POSITION_RATE;
                                }
                            } else if (map.mapObstacles[tmp_x + 1][tmp_y] >= 1) {
                                player_y = (tmp_y-1) * Common.PLAYER_POSITION_RATE;
                                player_x -= speed_now / 2;
                                if (player_x < (tmp_x) * Common.PLAYER_POSITION_RATE) {
                                    player_x = (tmp_x) * Common.PLAYER_POSITION_RATE;
                                }
                            }
                        }
                    }
                    break;
                case Location.LOCATION_LEFT:
                    player_x -= speed_now;
                    if (player_x < 0) {
                        player_x = 0;
                    }
                    tmp_x = player_x / Common.PLAYER_POSITION_RATE;
                    tmp_y = player_y / Common.PLAYER_POSITION_RATE;
                    if (tmp_x != (player_x + speed_now) / Common.PLAYER_POSITION_RATE) {
                        //換X軸才需計算
                        offset= player_y - tmp_y * Common.PLAYER_POSITION_RATE;
                        if (offset == 0) {
                            if (map.mapObstacles[tmp_x][tmp_y] >= 1) {
                                player_x = (tmp_x + 1) * Common.PLAYER_POSITION_RATE;
                            }
                        } else {
                            if (map.mapObstacles[tmp_x][tmp_y] >= 1
                                    && map.mapObstacles[tmp_x][tmp_y + 1] >= 1) {
                                player_x = (tmp_x + 1) * Common.PLAYER_POSITION_RATE;
                            } else if (map.mapObstacles[tmp_x][tmp_y] >= 1) {
                                player_x = (tmp_x + 1) * Common.PLAYER_POSITION_RATE;
                                player_y += speed_now / 2;
                                if (player_y > (tmp_y + 1) * Common.PLAYER_POSITION_RATE) {
                                    player_y = (tmp_y + 1) * Common.PLAYER_POSITION_RATE;
                                }
                            } else if (map.mapObstacles[tmp_x][tmp_y + 1] >= 1) {
                                player_x = (tmp_x + 1) * Common.PLAYER_POSITION_RATE;
                                player_y -= speed_now / 2;
                                if (player_y < (tmp_y) * Common.PLAYER_POSITION_RATE) {
                                    player_y = (tmp_y) * Common.PLAYER_POSITION_RATE;
                                }
                            }
                        }
                    }
                    break;
            }

            character.Move();
            SetMountMove();
        }
    }
}
