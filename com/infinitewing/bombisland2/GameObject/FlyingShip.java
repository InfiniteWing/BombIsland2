package com.infinitewing.bombisland2.GameObject;

import java.util.Vector;

/**
 * Created by InfiniteWing on 2017/3/12.
 */
public class FlyingShip extends Ai {
    public Vector<Integer> dropRecords;

    public FlyingShip(String id, int x, int y, Map map, int type) {
        super(id, x, y, map, type);
        dropRecords = new Vector<>();
    }

    public Boolean CheckHadDrop(Integer x) {
        for (int i = 0; i < dropRecords.size(); i++) {
            if (x == dropRecords.elementAt(i)) {
                return true;
            }
        }
        return false;
    }

    public void FlyingShipDropItem() {
        int x = player_x / Common.PLAYER_POSITION_RATE, y=0, counter;
        if (x >= 0 && x <= Common.MAP_WIDTH_UNIT - 1) {
            if (Common.RandomNum(1000) > 960) {
                if (!CheckHadDrop(Integer.valueOf(x))) {
                    dropRecords.addElement(Integer.valueOf(x));
                    counter = 8;
                    while (counter-- >= 0) {
                        y = Common.RandomNum(Common.MAP_HEIGHT_UNIT) - 1;
                        if (map.mapObstacles[x][y] == 0 && y > 2) {
                            break;
                        }else{
                            y=-1;
                        }
                    }
                    if(y>=0) {
                        int randNum = Common.RandomNum(920);
                        String itemName = "";
                        if (randNum < 200) {
                            itemName = "item_bomb";
                        } else if (randNum < 400) {
                            itemName = "item_power";
                        } else if (randNum < 600) {
                            itemName = "item_speed";
                        } else if (randNum < 635) {
                            itemName = "item_max";
                        } else if (randNum < 740) {
                            itemName = "item_money50";
                        } else if (randNum < 790) {
                            itemName = "item_money500";
                        } else if (randNum < 820) {
                            itemName = "item_money10000";
                        } else if (randNum < 840) {
                            itemName = "item_mount01";
                        } else if (randNum < 850) {
                            itemName = "item_mount02";
                        } else if (randNum < 875) {
                            itemName = "item_mount03";
                        } else if (randNum < 925) {
                            itemName = "item_mount04";
                        }
                        MapObject obj = new MapObject(x, y, MapObject.TYPE_ITEM, itemName, map);
                        obj.startLocation = new Location(player_x / Common.PLAYER_POSITION_RATE,
                                player_y / Common.PLAYER_POSITION_RATE);
                        map.mapFlyingObjects.add(obj);
                        map.mapObstacles[x][y] = -1;
                        Common.gameView.soundManager.addSound("dropitem.mp3");
                    }
                }
            }
        }
    }

    public void Move() {
        IsMoving = true;
        speed_now = speed;
        player_x += speed_now;
        if (player_x > (Common.MAP_WIDTH_UNIT + 2) * Common.PLAYER_POSITION_RATE) {
            player_x = (Common.MAP_WIDTH_UNIT + 2) * Common.PLAYER_POSITION_RATE;
            IsDead = true;
        }
        character.Move();
    }
}
