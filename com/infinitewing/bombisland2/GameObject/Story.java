package com.infinitewing.bombisland2.GameObject;

import android.content.Context;
import android.graphics.Bitmap;

import com.infinitewing.bombisland2.StorySession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by InfiniteWing on 2017/4/5.
 */
public class Story {
    public static final String TYPE_SURVIVAL = "Survival",
            TYPE_ENDLESS_SURVIVAL = "Endless_Survive",
            TYPE_SURVIVAL_WITH_TEAMMATE = "Survive_With_Teammate";
    public Vector<StoryDialog> startDialogs;
    public Vector<StoryDialog> endLossDialogs;
    public Vector<StoryDialog> endWinDialogs;
    public Vector<StoryDialog> hintDialogs;
    public Vector<Reward> rewards;
    public String type, map, hero, ais, name, teammates, boss;
    public int stage, surviveSeconds;
    public Context c;
    public HashMap<String, Bitmap> imageCaches;

    public Story(String name, int stage, String storyID, Context c) {
        this.c = c;
        this.name = name;
        this.stage = stage;
        startDialogs = new Vector<>();
        endWinDialogs = new Vector<>();
        endLossDialogs = new Vector<>();
        hintDialogs = new Vector<>();
        imageCaches = new HashMap<>();
        rewards = new Vector<>();
        LoadInfo(storyID);
    }

    public void Release() {
        for (String key : imageCaches.keySet()) {
            imageCaches.get(key).recycle();
        }
        System.gc();
    }

    public void LoadInfo(String storyID) {
        BufferedReader reader = null;
        String path = "xml/story/" + Common.GetLanguagePrefix() + storyID + ".txt";
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
            if (tag.equals("START_DIALOG") ||
                    tag.equals("END_DIALOG_WIN") ||
                    tag.equals("END_DIALOG_LOSE")) {
                int direction = 0;
                if (value.split(";")[1].equals("left")) {
                    direction = StoryDialog.LEFT;
                }
                if (value.split(";")[1].equals("center")) {
                    direction = StoryDialog.CENTER;
                }
                if (value.split(";")[1].equals("system")) {
                    direction = StoryDialog.SYSTEM;
                }
                if (value.split(";")[1].equals("right")) {
                    direction = StoryDialog.RIGHT;
                }
                StoryDialog storyDialog = new StoryDialog(this, value.split(";")[0], direction,
                        value.split(";")[2], value.split(";")[3], c);
                if (tag.equals("START_DIALOG")) {
                    startDialogs.add(storyDialog);
                } else if (tag.equals("END_DIALOG_WIN")) {
                    endWinDialogs.add(storyDialog);
                } else if (tag.equals("END_DIALOG_LOSE")) {
                    endLossDialogs.add(storyDialog);
                }
            } else if (tag.equals("EVENT_TYPE")) {
                type = value;
            } else if (tag.equals("EVENT_HINT")) {
                int direction = StoryDialog.SYSTEM;
                StoryDialog storyDialog = new StoryDialog(this, StoryDialog.SYSTEM_STR, direction,
                        StoryDialog.SYSTEM_STR, value, c);
                hintDialogs.add(storyDialog);
            } else if (tag.equals("AIS")) {
                ais = value;
            } else if (tag.equals("TEAMMATES")) {
                teammates = value;
            } else if (tag.equals("BOSS")) {
                boss = value;
            } else if (tag.equals("MAP")) {
                map = value;
            } else if (tag.equals("HERO")) {
                hero = value;
            } else if (tag.equals("EVENT_SECONDS")) {
                surviveSeconds = Integer.parseInt(value);
            } else if (tag.equals("REWARD")) {
                Reward reward = new Reward(value.split(";")[0], value.split(";")[1]);
                rewards.add(reward);
            }
        } while (line != null);
    }
}
