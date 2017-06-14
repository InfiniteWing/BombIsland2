package com.infinitewing.bombisland2.GameObject;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by InfiniteWing on 2017/4/11.
 */
public class Skill {
    public MapObject effect;
    public Context c;
    public String effectID, skillType;
    public int skillDelay, invincibleTime;
    public boolean isAllMap;
    public Map map;
    public Player player;
    public Skill(Player player,String skillID, Map map, Context c) {
        this.c = c;
        this.map=map;
        this.player=player;
        LoadInfo(skillID);
    }

    public void LoadInfo(String skillID) {
        BufferedReader reader = null;
        String path = "xml/skill/" + skillID + ".txt";
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
            if (y == 0) {
                y++;
                continue;
            }
            String tag = line.split(":")[0], value = line.split(":")[1];
            if (tag.equals("Effect_ID")) {
                effectID = value;
            } else if (tag.equals("Skill_Type")) {
                skillType = value;
            } else if (tag.equals("Skill_Delay")) {
                skillDelay = Integer.parseInt(value);
            } else if (tag.equals("Invincible_Time")) {
                invincibleTime = Integer.parseInt(value);
            } else if (tag.equals("All_Map")) {
                if(value.equals("true")){
                    isAllMap=true;
                }else {
                    isAllMap=false;
                }
            }
        } while (line != null);
        effect = new MapObject(1, 1, MapObject.TYPE_EMOTION, skillID, map);
    }
    public void SkillEffect(){
        if(skillType.equals("Cross_Explosion")){
            //以人物為中心，十字型的爆炸效果
            MapObject bomb = new MapObject(player.player_x/Common.PLAYER_POSITION_RATE,
                    player.player_y/Common.PLAYER_POSITION_RATE, MapObject.TYPE_BOMB, "bomb1", map);
            bomb.power=20;
            bomb.player=null;
            bomb.BombExplosion();
        }
    }
}
