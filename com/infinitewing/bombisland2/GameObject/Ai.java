package com.infinitewing.bombisland2.GameObject;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.infinitewing.bombisland2.Bluetooth;
import com.infinitewing.bombisland2.R;

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
            GOAST = 6,
            FLYING_SHIP = 7,
            BOSS_00 = 8,
            BOSS_01 = 9,
            BOSS_02 = 10;
    public Location targetLocation;
    public int type, life;
    public Skill skill;
    public String transID = null;

    public static int GetType(String id) {
        if (id.equals("unitychain_boss00")) {
            return Ai.BOSS_00;
        } else if (id.equals("unitychain_boss01")) {
            return Ai.BOSS_01;
        } else if (id.equals("unitychain_boss02")) {
            return Ai.BOSS_02;
        } else {
            return Ai.IQ_30;
        }
    }

    public Ai(String id, int x, int y, Map map, int type) {
        super(id, x, y, map);
        life = 1;
        if (type == BOSS_00) {
            transID = "unitychain_boss01";
        }
        if (type == BOSS_01) {
            life = 3;
        }
        teamID = 2;
        this.type = type;
        targetLocation = new Location(x, y);
    }

    public Boolean DoSkill() {
        switch (type) {
            case BOSS_01:
                if (Common.RandomNum(1000) < 80) {
                    Skill s = new Skill(this, "skill_01", map, Common.gameView.getContext());
                    skill = s;
                    return true;
                }
                break;
            case BOSS_00:
                if (Common.RandomNum(1000) < 40) {
                    Skill s = new Skill(this, "skill_01", map, Common.gameView.getContext());
                    skill = s;
                    return true;
                }
                break;
            case BOSS_02:
                if (Common.RandomNum(1000) < 40) {
                    Skill s = new Skill(this, "skill_01", map, Common.gameView.getContext());
                    skill = s;
                    return true;
                }
                break;
        }
        return false;
    }

    public Boolean CheckBubbleMove() {
        int x = targetLocation.x;
        int y = targetLocation.y;
        for (Player player : map.players) {
            if (CheckBubbleMove(player, x, y)) {
                return true;
            }
        }
        for (Player ai : map.ais) {
            if (CheckBubbleMove(ai, x, y)) {
                return true;
            }
        }
        return false;
    }

    public Boolean CheckBubbleMove(Player ai, int x, int y) {
        if (!ai.IsBubbled) {
            return false;
        }
        int pX = (ai.player_x + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
        int pY = (ai.player_y + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
        int direction;
        if (x == pX) {
            if (Math.abs(y - pY) > 0) {
                if (y > pY) {
                    if (map.mapObstacles[x][y - 1] > 0) {
                        return false;
                    }
                    direction = Location.LOCATION_TOP;
                    targetLocation.y = y - 1;
                    if (character.direction != direction) {
                        character.InitImage();
                        character.direction = direction;
                    }
                    return true;
                } else {
                    if (map.mapObstacles[x][y + 1] > 0) {
                        return false;
                    }
                    direction = Location.LOCATION_DOWN;
                    targetLocation.y = y + 1;
                    if (character.direction != direction) {
                        character.InitImage();
                        character.direction = direction;
                    }
                    return true;
                }
            }
        } else if (y == pY) {
            if (Math.abs(x - pX) > 0) {
                if (x > pX) {
                    if (map.mapObstacles[x - 1][y] > 0) {
                        return false;
                    }
                    direction = Location.LOCATION_LEFT;
                    targetLocation.x = x - 1;
                    if (character.direction != direction) {
                        character.InitImage();
                        character.direction = direction;
                    }
                    return true;
                } else {
                    if (map.mapObstacles[x + 1][y] > 0) {
                        return false;
                    }
                    direction = Location.LOCATION_RIGHT;
                    targetLocation.x = x + 1;
                    if (character.direction != direction) {
                        character.InitImage();
                        character.direction = direction;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean CheckItemMove() {
        int x = targetLocation.x;
        int y = targetLocation.y;
        while (y > 0) {
            y--;
            if (map.mapObstacles[x][y] < 0) {
                if (CheckMove(Location.LOCATION_TOP, false))
                    return true;
            } else if (map.mapObstacles[x][y] > 0) {
                break;
            }
        }
        x = targetLocation.x;
        y = targetLocation.y;
        while (x > 0) {
            x--;
            if (map.mapObstacles[x][y] < 0) {
                if (CheckMove(Location.LOCATION_LEFT, false))
                    return true;
            } else if (map.mapObstacles[x][y] > 0) {
                break;
            }
        }
        x = targetLocation.x;
        y = targetLocation.y;
        while (y < Common.MAP_HEIGHT_UNIT - 1) {
            y++;
            if (map.mapObstacles[x][y] < 0) {
                if (CheckMove(Location.LOCATION_DOWN, false))
                    return true;
            } else if (map.mapObstacles[x][y] > 0) {
                break;
            }
        }
        x = targetLocation.x;
        y = targetLocation.y;
        while (x < Common.MAP_WIDTH_UNIT - 1) {
            x++;
            if (map.mapObstacles[x][y] < 0) {
                if (CheckMove(Location.LOCATION_RIGHT, false))
                    return true;
            } else if (map.mapObstacles[x][y] > 0) {
                break;
            }
        }
        return false;
    }

    public void CheckDead() {
        switch (type) {
            case IQ_10:
            case IQ_30:
            case BOSS_00:
            case BOSS_01:
            case BOSS_02:
            case IQ_50:
            case IQ_70:
            case IQ_100:
                super.CheckDead(true);
                break;
        }
    }

    public Boolean FollowMoveTo(int x, int y, int pX, int pY, int d) {
        boolean check;
        switch (d) {
            case Location.LOCATION_LEFT:
                if (x > pX) {
                    check = (CanMove(x - 1, y) && (!IsDanger(x - 1, y)) && HaveExit(x - 1, y));
                    if (check && character.direction != Location.LOCATION_RIGHT) {
                        targetLocation.x = x - 1;
                        if (character.direction != Location.LOCATION_LEFT) {
                            character.InitImage();
                            character.direction = Location.LOCATION_LEFT;
                        }
                        return true;
                    }
                }
                break;
            case Location.LOCATION_RIGHT:
                if (x < pX) {
                    check = (CanMove(x + 1, y) && (!IsDanger(x + 1, y)) && HaveExit(x + 1, y));
                    if (check && character.direction != Location.LOCATION_LEFT) {
                        targetLocation.x = x + 1;
                        if (character.direction != Location.LOCATION_RIGHT) {
                            character.InitImage();
                            character.direction = Location.LOCATION_RIGHT;
                        }
                        return true;
                    }
                }
                break;
            case Location.LOCATION_TOP:
                if (y > pY) {
                    check = (CanMove(x, y - 1) && (!IsDanger(x, y - 1)) && HaveExit(x, y - 1));
                    if (check && character.direction != Location.LOCATION_DOWN) {
                        targetLocation.y = y - 1;
                        if (character.direction != Location.LOCATION_TOP) {
                            character.InitImage();
                            character.direction = Location.LOCATION_TOP;
                        }
                        return true;
                    }
                }
                break;
            case Location.LOCATION_DOWN:
                if (y < pY) {
                    check = (CanMove(x, y + 1) && (!IsDanger(x, y + 1)) && HaveExit(x, y + 1));
                    if (check && character.direction != Location.LOCATION_TOP) {
                        targetLocation.y = y + 1;
                        if (character.direction != Location.LOCATION_DOWN) {
                            character.InitImage();
                            character.direction = Location.LOCATION_DOWN;
                        }
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    public Boolean FollowMove() {
        int x = targetLocation.x;
        int y = targetLocation.y;
        boolean check;
        for (Player player : map.players) {
            if (teamID == player.teamID) {
                //如果跟玩家同一隊，就不要尾行玩家了...
                continue;
            }
            int pX = (player.player_x + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            int pY = (player.player_y + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            int direction[] = {Location.LOCATION_TOP, Location.LOCATION_DOWN, Location.LOCATION_LEFT, Location.LOCATION_RIGHT};
            for (int i = 0; i < direction.length; i++) {
                int randomIndex = Common.RandomNum(4) - 1;
                int tmp = direction[i];
                direction[i] = direction[randomIndex];
                direction[randomIndex] = tmp;
            }
            for (int i = 0; i < direction.length; i++) {
                check = FollowMoveTo(x, y, pX, pY, direction[i]);
                if (check) {
                    return true;
                }
            }
        }
        for (Player player : map.ais) {
            if (teamID == player.teamID) {
                //Ai不用尾行Ai
                continue;
            }
            int pX = (player.player_x + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            int pY = (player.player_y + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            int direction[] = {Location.LOCATION_TOP, Location.LOCATION_DOWN, Location.LOCATION_LEFT, Location.LOCATION_RIGHT};
            for (int i = 0; i < direction.length; i++) {
                int randomIndex = Common.RandomNum(4) - 1;
                int tmp = direction[i];
                direction[i] = direction[randomIndex];
                direction[randomIndex] = tmp;
            }
            for (int i = 0; i < direction.length; i++) {
                check = FollowMoveTo(x, y, pX, pY, direction[i]);
                if (check) {
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean CheckMove(int direction, Boolean noDanger) {
        return CheckMove(direction, noDanger, 0);
    }

    public Boolean CheckMove(int direction, Boolean noDanger, int exitCount) {
        int x = targetLocation.x;
        int y = targetLocation.y;
        Boolean check;
        switch (direction) {
            case Location.LOCATION_DOWN:
                check = (CanMove(x, y + 1) && (!IsDanger(x, y + 1) || noDanger) && HaveExit(x, y + 1, exitCount));
                if (check) {
                    targetLocation.y = y + 1;
                    if (character.direction != direction) {
                        character.InitImage();
                        character.direction = direction;
                    }
                    return true;
                }
                break;
            case Location.LOCATION_LEFT:
                check = (CanMove(x - 1, y) && (!IsDanger(x - 1, y) || noDanger) && HaveExit(x - 1, y, exitCount));
                if (check) {
                    targetLocation.x = x - 1;
                    if (character.direction != direction) {
                        character.InitImage();
                        character.direction = direction;
                    }
                    return true;
                }
                break;
            case Location.LOCATION_TOP:
                check = (CanMove(x, y - 1) && (!IsDanger(x, y - 1) || noDanger) && HaveExit(x, y - 1, exitCount));
                if (check) {
                    targetLocation.y = y - 1;
                    if (character.direction != direction) {
                        character.InitImage();
                        character.direction = direction;
                    }
                    return true;
                }
                break;
            case Location.LOCATION_RIGHT:
                check = (CanMove(x + 1, y) && (!IsDanger(x + 1, y) || noDanger) && HaveExit(x + 1, y, exitCount));
                if (check) {
                    targetLocation.x = x + 1;
                    if (character.direction != direction) {
                        character.InitImage();
                        character.direction = direction;
                    }
                    return true;
                }
                break;
        }
        return false;
    }

    public void DefaultMove(int x, int y) {
        if (Common.RandomNum(10000) > 8000 && CheckMove(character.direction, false, 1)) {
            return;
        } else {
            for (int i = 0; i < 3; i++) {
                if (CheckMove(Location.RandomLocation(), false, 1)) {
                    return;
                }
            }
        }
        if (CheckMove(Location.LOCATION_TOP, false, 1)) {
            return;
        }
        if (CheckMove(Location.LOCATION_RIGHT, false, 1)) {
            return;
        }
        if (CheckMove(Location.LOCATION_LEFT, false, 1)) {
            return;
        }
        if (CheckMove(Location.LOCATION_DOWN, false, 1)) {
            return;
        }
        if (CheckMove(Location.LOCATION_TOP, false, 0)) {
            return;
        }
        if (CheckMove(Location.LOCATION_RIGHT, false, 0)) {
            return;
        }
        if (CheckMove(Location.LOCATION_LEFT, false, 0)) {
            return;
        }
        if (CheckMove(Location.LOCATION_DOWN, false, 0)) {
            return;
        }
        if (!IsDanger(x, y)) {
            IsMoving = false;
            speed_now = 0;
            return;
        }
        if (CheckMove(character.direction, true, 1)) {
            return;
        }
        if (CheckMove(Location.LOCATION_TOP, true, 1)) {
            return;
        }
        if (CheckMove(Location.LOCATION_RIGHT, true, 1)) {
            return;
        }
        if (CheckMove(Location.LOCATION_LEFT, true, 1)) {
            return;
        }
        if (CheckMove(Location.LOCATION_DOWN, true, 1)) {
            return;
        }
        if (CheckMove(character.direction, true)) {
            return;
        }
        if (CheckMove(Location.LOCATION_TOP, true)) {
            return;
        }
        if (CheckMove(Location.LOCATION_RIGHT, true)) {
            return;
        }
        if (CheckMove(Location.LOCATION_LEFT, true)) {
            return;
        }
        if (CheckMove(Location.LOCATION_DOWN, true)) {
            return;
        }
        IsMoving = false;
        speed_now = 0;
        return;
    }

    public void Move() {
        UpdateBlur();
        if (mount != null){
            mount.UpdateBlur();
        }
        if (revivalCounter++ < 0) {
            if (emotion != null) {
                emotion.Play();
            }
            if (revivalCounter == 0) {
                if (type == BOSS_01) {
                    InitEmotion("emotion_boss");
                } else if (type == BOSS_00) {
                    InitEmotion("emotion_boss");
                } else {
                    InitEmotion("emotion_ai_0"+teamID);
                }
            } else {
                return;
            }
        }
        if (emotion != null) {
            emotion.Play();
        }
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
            skill = null;
            return;
        }
        //這裡跑技能，會放技能就代表他已經站定點
        if (skill != null) {
            if (skill.effect != null) {
                skill.effect.Play();
                if (skill.effect.IsEnd) {
                    skill.effect = null;
                }
            } else {
                //如果技能特效放完，等待延遲時間後，就會施放技能效果
                if (skill.skillDelay-- < 0) {
                    skill.SkillEffect();
                    invincibleCounter = skill.invincibleTime;
                    //讓主角無敵一段時間，避免被自己的技能殺死...
                    skill = null;
                }
            }
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
                    if (CheckBubbleMove()) {
                        break;
                    }
                    if (CheckItemMove()) {
                        break;
                    }
                    DefaultMove(x, y);
                    break;
                case IQ_30:
                case BOSS_00:
                case BOSS_01:
                case BOSS_02:
                    if (DoSkill()) {
                        //查看是否會使用技能
                        break;
                    }
                    SetBomb();
                    if (CheckBubbleMove()) {
                        break;
                    }
                    if (CheckItemMove()) {
                        break;
                    }
                    if (FollowMove()) {
                        break;
                    }
                    DefaultMove(x, y);
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
        //這裡是Player.Move()的Copy版
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
        }
        SetMountMove();
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
        return map.willExplosionDP[x][y] == 1 || map.explosionDP[x][y] == 1;
        /*
        map.bombLock = true;
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
        map.bombLock = false;
        for (Explosion explosion : map.explosions) {
            if (x == explosion.location.x && y == explosion.location.y) {
                if (!explosion.IsEnd) {
                    return true;
                }
            }
        }
        return false;
        */
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
                    if (Common.RandomNum(1000) > 300) {
                        InitBomb(x, y);
                    }
                }
            } else if (HaveEnemy(x, y)) {
                InitBomb(x, y);
            }
        }
    }

    public Boolean HaveEnemy(int x, int y) {
        for (Player player : map.players) {
            if (player.teamID == teamID) {
                //不要害到自己人
                continue;
            }
            int pX = (player.player_x + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            int pY = (player.player_y + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            if (pX == x && pY == y) {
                return true;
            } else if (pX == x) {
                if (wb_power >= Math.abs(y - pY)) {
                    return true;
                }
            } else if (pY == y) {
                if (wb_power >= Math.abs(x - pX)) {
                    return true;
                }
            }
        }
        //如果電腦跟玩家同一隊的話
        for (Player player : map.ais) {
            if (player.teamID == teamID) {
                //不要害到自己人
                continue;
            }
            int pX = (player.player_x + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            int pY = (player.player_y + Common.PLAYER_POSITION_RATE / 2) / Common.PLAYER_POSITION_RATE;
            if (pX == x && pY == y) {
                return true;
            } else if (pX == x) {
                if (wb_power >= Math.abs(y - pY)) {
                    return true;
                }
            } else if (pY == y) {
                if (wb_power >= Math.abs(x - pX)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean HaveExit(int x, int y) {
        return HaveExit(x, y, 0);
    }

    public Boolean HaveExit(int x, int y, int c) {
        int exitCount = 0;
        if (x > 0) {
            if (map.mapObstacles[x - 1][y] <= 0) {
                exitCount++;
            }
        }
        if (y > 0) {
            if (map.mapObstacles[x][y - 1] <= 0) {
                exitCount++;
            }
        }
        if (x < Common.MAP_WIDTH_UNIT - 1) {
            if (map.mapObstacles[x + 1][y] <= 0) {
                exitCount++;
            }
        }
        if (y < Common.MAP_HEIGHT_UNIT - 1) {
            if (map.mapObstacles[x][y + 1] <= 0) {
                exitCount++;
            }
        }
        return exitCount > c;
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

    @Override
    public void DrawEmotion(Canvas canvas){
        if(teamID==map.players.elementAt(0).teamID){
            //同隊不用畫標記
            return;
        }
        super.DrawEmotion(canvas);
    }
    @Override
    public void Draw(Canvas canvas) {
        super.Draw(canvas);
        if (skill != null) {
            if (skill.effect != null) {
                if (skill.isAllMap) {
                    //地圖技
                } else {
                    //個人技能
                    skill.effect.Draw(player_x, player_y, canvas);
                }
            }
        }
    }
}
