package com.infinitewing.bombisland2.GameObject;

import com.infinitewing.bombisland2.Bluetooth;

/**
 * Created by InfiniteWing on 2016/8/18.
 */
public class Ai extends Player {
    public static final int
            IQ_10 = 1,
            IQ_30 = 2,
            IQ_50 = 3,
            IQ_70 = 4,
            IQ_100 = 5,
            GOAST = 6;
    public Location targetLocation;
    public int type;

    public Ai(String id, int x, int y, Map map, int type) {
        super(id, x, y, map);
        teamID = 2;
        this.type = type;
        targetLocation = new Location(x, y);
    }
    public Boolean CheckBubbleMove(){
        int x = targetLocation.x;
        int y = targetLocation.y;
        for(Ai ai:map.ais){
            if(!ai.IsBubbled){
                continue;
            }
            int pX = (ai.player_x + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            int pY = (ai.player_y + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            int direction;
            if(x==pX){
                if(Math.abs(y-pY)>0){
                    if(y>pY){
                        if(map.mapObstacles[x][y-1]>0)
                        {
                            continue;
                        }
                        direction=Location.LOCATION_TOP;
                        targetLocation.y = y - 1;
                        if (character.direction != direction) {
                            speed_now = 0;
                            character.direction = direction;
                        }
                        return true;
                    }else{
                        if(map.mapObstacles[x][y+1]>0)
                        {
                            continue;
                        }
                        direction=Location.LOCATION_DOWN;
                        targetLocation.y = y + 1;
                        if (character.direction != direction) {
                            speed_now = 0;
                            character.direction = direction;
                        }
                        return true;
                    }
                }
            }else if(y==pY){
                if(Math.abs(x-pX)>0){
                    if(x>pX){
                        if(map.mapObstacles[x-1][y]>0)
                        {
                            continue;
                        }
                        direction=Location.LOCATION_LEFT;
                        targetLocation.x = x - 1;
                        if (character.direction != direction) {
                            speed_now = 0;
                            character.direction = direction;
                        }
                        return true;
                    }else{
                        if(map.mapObstacles[x+1][y]>0)
                        {
                            continue;
                        }
                        direction=Location.LOCATION_RIGHT;
                        targetLocation.x = x + 1;
                        if (character.direction != direction) {
                            speed_now = 0;
                            character.direction = direction;
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public Boolean CheckItemMove() {
        int x = targetLocation.x;
        int y = targetLocation.y;
        if (y > 0) {
            if (map.mapObstacles[x][y - 1] < 0) {
                if (CheckMove(Location.LOCATION_TOP, false))
                    return true;
            }
        }
        if (x > 0) {
            if (map.mapObstacles[x - 1][y] < 0) {
                if (CheckMove(Location.LOCATION_LEFT, false))
                    return true;
            }
        }
        if (y < Common.MAP_HEIGHT_UNIT - 1) {
            if (map.mapObstacles[x][y + 1] < 0) {
                if (CheckMove(Location.LOCATION_DOWN, false))
                    return true;
            }
        }
        if (x < Common.MAP_WIDTH_UNIT - 1) {
            if (map.mapObstacles[x + 1][y] < 0) {
                if (CheckMove(Location.LOCATION_RIGHT, false))
                    return true;
            }
        }
        return false;
    }

    public void CheckDead() {
        switch (type) {
            case IQ_10:
            case IQ_30:
            case IQ_50:
            case IQ_70:
            case IQ_100:
                super.CheckDead(true);
                break;
        }
    }

    public Boolean CheckMove(int direction, Boolean noDanger) {
        int x = targetLocation.x;
        int y = targetLocation.y;
        Boolean check = false;
        switch (direction) {
            case Location.LOCATION_DOWN:
                check = (CanMove(x, y + 1) && (!IsDanger(x, y + 1) || noDanger) && HaveExit(x, y + 1));
                if (check) {
                    targetLocation.y = y + 1;
                    if (character.direction != direction) {
                        speed_now = 0;
                        character.direction = direction;
                    }
                    return true;
                }
                break;
            case Location.LOCATION_LEFT:
                check = (CanMove(x - 1, y) && (!IsDanger(x - 1, y) || noDanger) && HaveExit(x - 1, y));
                if (check) {
                    targetLocation.x = x - 1;
                    if (character.direction != direction) {
                        speed_now = 0;
                        character.direction = direction;
                    }
                    return true;
                }
                break;
            case Location.LOCATION_TOP:
                check = (CanMove(x, y - 1) && (!IsDanger(x, y - 1) || noDanger) && HaveExit(x, y - 1));
                if (check) {
                    targetLocation.y = y - 1;
                    if (character.direction != direction) {
                        speed_now = 0;
                        character.direction = direction;
                    }
                    return true;
                }
                break;
            case Location.LOCATION_RIGHT:
                check = (CanMove(x + 1, y) && (!IsDanger(x + 1, y) || noDanger) && HaveExit(x + 1, y));
                if (check) {
                    targetLocation.x = x + 1;
                    if (character.direction != direction) {
                        speed_now = 0;
                        character.direction = direction;
                    }
                    return true;
                }
                break;
        }
        return false;
    }

    public void Move() {
        if (--mountDeadCounter > 0) {
            if (mount != null) {
                mount.mountDeadCounter--;
            }
            return;
        }
        if (mountDeadCounter == 0) {
            mount = null;
        }
        if (IsBubbled) {
            bubbledTime += Common.GAME_REFRESH;
            return;
        }
        IsMoving = true;
        Boolean haveMove = false;
        int offset_x = targetLocation.x * Common.PLAYER_POSITION_RATE - player_x;
        int offset_y = targetLocation.y * Common.PLAYER_POSITION_RATE - player_y;
        if (offset_x == 0
                && offset_y == 0) {
            //到達目的地重新規劃目標方向，並看能否放炸彈
            int x = (player_x) / Common.PLAYER_POSITION_RATE;
            int y = (player_y) / Common.PLAYER_POSITION_RATE;
            switch (type) {
                case IQ_10:
                    SetBomb();
                    if(CheckBubbleMove()){
                        break;
                    }
                    if (CheckItemMove()) {
                        break;
                    }
                    if (Common.RandomNum(2000) >= 1600) {
                        if (CheckMove(character.direction, false)) {
                            haveMove = true;
                        }
                    } else {
                        for (int i = 0; i < 3; i++) {
                            if (CheckMove(Location.RandomLocation(), false)) {
                                haveMove = true;
                                break;
                            }
                        }
                    }
                    if (haveMove) {
                        break;
                    }
                    if (CheckMove(Location.LOCATION_TOP, false)) {
                        break;
                    }
                    if (CheckMove(Location.LOCATION_RIGHT, false)) {
                        break;
                    }
                    if (CheckMove(Location.LOCATION_LEFT, false)) {
                        break;
                    }
                    if (CheckMove(Location.LOCATION_DOWN, false)) {
                        break;
                    }
                    if (!IsDanger(x, y)) {
                        IsMoving = false;
                        speed_now = 0;
                        break;
                    }
                    if (CheckMove(character.direction, true)) {
                        break;
                    }
                    if (CheckMove(Location.LOCATION_TOP, true)) {
                        break;
                    }
                    if (CheckMove(Location.LOCATION_RIGHT, true)) {
                        break;
                    }
                    if (CheckMove(Location.LOCATION_LEFT, true)) {
                        break;
                    }
                    if (CheckMove(Location.LOCATION_DOWN, true)) {
                        break;
                    }
                    IsMoving = false;
                    speed_now = 0;
                    break;
                case GOAST:
                    if (Common.RandomNum(2000) >= 1990) {
                        if (CheckMove(character.direction, false)) {
                            haveMove = true;
                        }
                    }
                    for (int i = 0; i < 3; i++) {
                        if (CheckMove(Location.RandomLocation(), false)) {
                            haveMove = true;
                            break;
                        }
                    }
                    if (haveMove) {
                        break;
                    }
                    IsMoving = false;
                    speed_now = 0;
                    break;
            }
        }
        SetMountMove();
        //這裡是Player.Move()的Copy版
        if(emotion!=null){
            emotion.Play();
        }
        if (IsMoving && !IsDead) {
            speed_now += 10;
            if (mount != null) {
                if (speed_now > mount.speed) {
                    speed_now = mount.speed;
                }
            } else {
                if (speed_now > speed) {
                    speed_now = speed;
                }
            }
            int tmp_x, tmp_y, offset;
            switch (character.direction) {
                case Location.LOCATION_TOP:
                    player_y -= speed_now;
                    if (player_y < 0) {
                        player_y = 0;
                    }
                    if (targetLocation.y * Common.PLAYER_POSITION_RATE > player_y) {
                        player_y = targetLocation.y * Common.PLAYER_POSITION_RATE;
                    }
                    tmp_x = player_x / Common.PLAYER_POSITION_RATE;
                    tmp_y = player_y / Common.PLAYER_POSITION_RATE;
                    if (tmp_y != (player_y + speed_now) / Common.PLAYER_POSITION_RATE) {
                        //換Y軸才需計算
                        offset = player_x - tmp_x * Common.PLAYER_POSITION_RATE;
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
                    if (targetLocation.x * Common.PLAYER_POSITION_RATE < player_x) {
                        player_x = targetLocation.x * Common.PLAYER_POSITION_RATE;
                    }
                    tmp_x = (player_x + Common.PLAYER_POSITION_RATE - 1) / Common.PLAYER_POSITION_RATE;
                    tmp_y = player_y / Common.PLAYER_POSITION_RATE;
                    if (tmp_x != (player_x - speed_now + Common.PLAYER_POSITION_RATE - 1) / Common.PLAYER_POSITION_RATE) {
                        //換X軸才需計算
                        offset = player_y - tmp_y * Common.PLAYER_POSITION_RATE;
                        if (offset == 0) {
                            if (map.mapObstacles[tmp_x][tmp_y] >= 1) {
                                player_x = (tmp_x - 1) * Common.PLAYER_POSITION_RATE;
                            }
                        } else {
                            if (map.mapObstacles[tmp_x][tmp_y] >= 1
                                    && map.mapObstacles[tmp_x][tmp_y + 1] >= 1) {
                                player_x = (tmp_x - 1) * Common.PLAYER_POSITION_RATE;
                            } else if (map.mapObstacles[tmp_x][tmp_y] >= 1) {
                                player_x = (tmp_x - 1) * Common.PLAYER_POSITION_RATE;
                                player_y += speed_now / 2;
                                if (player_y > (tmp_y + 1) * Common.PLAYER_POSITION_RATE) {
                                    player_y = (tmp_y + 1) * Common.PLAYER_POSITION_RATE;
                                }
                            } else if (map.mapObstacles[tmp_x][tmp_y + 1] >= 1) {
                                player_x = (tmp_x - 1) * Common.PLAYER_POSITION_RATE;
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
                    if (targetLocation.y * Common.PLAYER_POSITION_RATE < player_y) {
                        player_y = targetLocation.y * Common.PLAYER_POSITION_RATE;
                    }
                    tmp_x = player_x / Common.PLAYER_POSITION_RATE;
                    tmp_y = (player_y + Common.PLAYER_POSITION_RATE - 1) / Common.PLAYER_POSITION_RATE;
                    if (tmp_y != (player_y - speed_now + Common.PLAYER_POSITION_RATE - 1) / Common.PLAYER_POSITION_RATE) {
                        //換Y軸才需計算
                        offset = player_x - tmp_x * Common.PLAYER_POSITION_RATE;
                        if (offset == 0) {
                            if (map.mapObstacles[tmp_x][tmp_y] >= 1) {
                                player_y = (tmp_y - 1) * Common.PLAYER_POSITION_RATE;
                            }
                        } else {
                            if (map.mapObstacles[tmp_x][tmp_y] >= 1
                                    && map.mapObstacles[tmp_x + 1][tmp_y] >= 1) {
                                player_y = (tmp_y - 1) * Common.PLAYER_POSITION_RATE;
                            } else if (map.mapObstacles[tmp_x][tmp_y] >= 1) {
                                player_y = (tmp_y - 1) * Common.PLAYER_POSITION_RATE;
                                player_x += speed_now / 2;
                                if (player_x > (tmp_x + 1) * Common.PLAYER_POSITION_RATE) {
                                    player_x = (tmp_x + 1) * Common.PLAYER_POSITION_RATE;
                                }
                            } else if (map.mapObstacles[tmp_x + 1][tmp_y] >= 1) {
                                player_y = (tmp_y - 1) * Common.PLAYER_POSITION_RATE;
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
                    if (targetLocation.x * Common.PLAYER_POSITION_RATE > player_x) {
                        player_x = targetLocation.x * Common.PLAYER_POSITION_RATE;
                    }
                    tmp_x = player_x / Common.PLAYER_POSITION_RATE;
                    tmp_y = player_y / Common.PLAYER_POSITION_RATE;
                    if (tmp_x != (player_x + speed_now) / Common.PLAYER_POSITION_RATE) {
                        //換X軸才需計算
                        offset = player_y - tmp_y * Common.PLAYER_POSITION_RATE;
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

    public Boolean CanMove(int x, int y) {
        if (x >= Common.MAP_WIDTH_UNIT
                || y >= Common.MAP_HEIGHT_UNIT
                || x < 0
                || y < 0) {
            return false;
        }
        return map.mapObstacles[x][y] <= 0;
    }

    public Boolean IsDanger(int x, int y) {
        for (MapObject mapObject : map.bombs) {
            if (x == mapObject.location.x) {
                if (Math.abs(y - mapObject.location.y) <= mapObject.power) {
                    return true;
                }
            } else if (y == mapObject.location.y) {
                if (Math.abs(x - mapObject.location.x) <= mapObject.power) {
                    return true;
                }
            }
        }
        for (Explosion explosion : map.explosions) {
            if (x == explosion.location.x && y == explosion.location.y) {
                if (!explosion.IsEnd) {
                    return true;
                }
            }
        }
        return false;
    }

    public int CheckSafeRoad(int x, int y) {
        int safe = 0;
        if (x > 0) {
            if (map.mapObstacles[x - 1][y] <= 0 && !IsDanger(x - 1, y)) {
                safe++;
            }
        }
        if (y > 0) {
            if (map.mapObstacles[x][y - 1] <= 0 && !IsDanger(x, y - 1)) {
                safe++;
            }
        }
        if (x < Common.MAP_WIDTH_UNIT - 1) {
            if (map.mapObstacles[x + 1][y] <= 0 && !IsDanger(x + 1, y)) {
                safe++;
            }
        }
        if (y < Common.MAP_HEIGHT_UNIT - 1) {
            if (map.mapObstacles[x][y + 1] <= 0 && !IsDanger(x, y + 1)) {
                safe++;
            }
        }
        return safe;
    }

    public void InitBomb(int x, int y) {
        if (map.mapObstacles[x][y] == 0) {
            MapObject bomb = new MapObject(x, y, MapObject.TYPE_BOMB, "bomb1", map);
            bomb.power = wb_power;
            bomb.player = this;
            map.bombs.add(bomb);
            wb--;
            Common.gameView.soundManager.addSound("setbomb.mp3");
            Common.gameView.BTPlayerAddBomb(x, y);
            map.mapObstacles[x][y] = 3;
        }
    }

    public void SetBomb() {
        if (wb > 0) {
            int x = (player_x + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            int y = (player_y + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            if (HaveBox(x - 1, y)
                    || HaveBox(x + 1, y)
                    || HaveBox(x, y + 1)
                    || HaveBox(x, y - 1)) {
                if (CheckSafeRoad(x, y) >= 2 || wb == wb_now) {
                    InitBomb(x, y);
                }
            } else if (HaveEnemy(x, y)) {
                InitBomb(x, y);
            }
        }
    }

    public Boolean HaveEnemy(int x, int y) {
        for (Player player : map.players) {
            int pX = (player.player_x + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            int pY = (player.player_y + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            if (pX == x && pY == y) {
                return true;
            }else if(pX==x){
                if(wb_power>=Math.abs(y-pY)){
                    return true;
                }
            }else if(pY==y){
                if(wb_power>=Math.abs(x-pX)){
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean HaveExit(int x, int y) {
        if (x > 0) {
            if (map.mapObstacles[x - 1][y] <= 0) {
                return true;
            }
        }
        if (y > 0) {
            if (map.mapObstacles[x][y - 1] <= 0) {
                return true;
            }
        }
        if (x < Common.MAP_WIDTH_UNIT - 1) {
            if (map.mapObstacles[x + 1][y] <= 0) {
                return true;
            }
        }
        if (y < Common.MAP_HEIGHT_UNIT - 1) {
            if (map.mapObstacles[x][y + 1] <= 0) {
                return true;
            }
        }
        return false;
    }

    public Boolean HaveBox(int x, int y) {
        if (x >= Common.MAP_WIDTH_UNIT
                || y >= Common.MAP_HEIGHT_UNIT
                || x < 0
                || y < 0) {
            return false;
        }
        return map.mapObstacles[x][y] == 2;
    }
}
