package com.infinitewing.bombisland2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.infinitewing.bombisland2.GameObject.Common;
import com.infinitewing.bombisland2.GameObject.Player;
import com.infinitewing.bombisland2.GameObject.Recorder;
import com.infinitewing.bombisland2.GameObject.Story;
import com.infinitewing.bombisland2.GameObject.StoryDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * Created by InfiniteWing on 2017/4/5.
 */
public class StoryIndex extends Activity {
    public Vector<Integer> StageIds, FinishTagIds, RewardIconIds;
    public Vector<String> StoryIds;
    public int currentStage = 0;
    public String storyName = Common.STORY_UNITY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.SetFullScreen(getWindow());
        setContentView(R.layout.story_index);
        StoryIds = new Vector<>();
        StageIds = new Vector<>();
        RewardIconIds = new Vector<>();
        FinishTagIds = new Vector<>();
        StageIds.add(R.id.StoryIndex_UnityChan_01);
        StageIds.add(R.id.StoryIndex_UnityChan_02);
        StageIds.add(R.id.StoryIndex_UnityChan_03);
        StageIds.add(R.id.StoryIndex_UnityChan_04);
        StageIds.add(R.id.StoryIndex_UnityChan_05);
        StageIds.add(R.id.StoryIndex_UnityChan_06);
        StageIds.add(R.id.StoryIndex_UnityChan_07);
        StageIds.add(R.id.StoryIndex_UnityChan_08);
        FinishTagIds.add(R.id.StoryIndex_UnityChan_Finish_01);
        FinishTagIds.add(R.id.StoryIndex_UnityChan_Finish_02);
        FinishTagIds.add(R.id.StoryIndex_UnityChan_Finish_03);
        FinishTagIds.add(R.id.StoryIndex_UnityChan_Finish_04);
        FinishTagIds.add(R.id.StoryIndex_UnityChan_Finish_05);
        FinishTagIds.add(R.id.StoryIndex_UnityChan_Finish_06);
        FinishTagIds.add(R.id.StoryIndex_UnityChan_Finish_07);
        FinishTagIds.add(R.id.StoryIndex_UnityChan_Finish_08);

        RewardIconIds.add(R.id.StoryIndex_UnityChan_Reward_01);
        RewardIconIds.add(R.id.StoryIndex_UnityChan_Reward_02);
        RewardIconIds.add(R.id.StoryIndex_UnityChan_Reward_03);
        RewardIconIds.add(R.id.StoryIndex_UnityChan_Reward_04);
        RewardIconIds.add(R.id.StoryIndex_UnityChan_Reward_05);
        RewardIconIds.add(R.id.StoryIndex_UnityChan_Reward_06);
        RewardIconIds.add(R.id.StoryIndex_UnityChan_Reward_07);
        RewardIconIds.add(R.id.StoryIndex_UnityChan_Reward_08);
        for (int id : StageIds) {
            findViewById(id).setOnClickListener(new ClickListener());
        }
        for (int id : RewardIconIds) {
            findViewById(id).setOnClickListener(new ClickListener());
        }
        findViewById(R.id.StoryIndex_Exit).setOnClickListener(new ClickListener());
        LoadCurrentStage();
        LoadInfo();
        //將未接露的關卡隱藏
        for (int i = currentStage + 1; i < StoryIds.size(); i++) {
            findViewById(StageIds.elementAt(i)).setVisibility(View.GONE);
        }
        for (int i = StoryIds.size(); i < StageIds.size(); i++) {
            findViewById(StageIds.elementAt(i)).setVisibility(View.GONE);
        }
        //目前的關卡還未完成
        if (currentStage <= StoryIds.size()) {
            findViewById(FinishTagIds.elementAt(currentStage)).setVisibility(View.GONE);
        }
    }

    public void LoadCurrentStage() {
        SharedPreferences sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
        currentStage = sp.getInt(storyName, 0);
    }

    public void LoadInfo() {
        BufferedReader reader = null;
        String path = "xml/story/" + storyName + ".txt";
        try {
            reader = new BufferedReader(new InputStreamReader(getApplicationContext().getAssets().open(path)));
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
            StoryIds.add(line);
        } while (line != null);
    }


    public class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id==R.id.StoryIndex_Exit){
                StoryIndex.this.finish();
                return;
            }
            Intent intent = new Intent(StoryIndex.this, StorySession.class);
            for (int i = 0; i < StageIds.size(); i++) {
                if (id == StageIds.elementAt(i)) {
                    intent.putExtra("story", StoryIds.elementAt(i));
                    intent.putExtra("story_stage", i);
                    intent.putExtra("story_name", storyName);
                    startActivity(intent);
                    StoryIndex.this.finish();
                    return;
                }
            }
            for (int i = 0; i < RewardIconIds.size(); i++) {
                if (id == RewardIconIds.elementAt(i)) {
                    Story story = new Story(storyName, i, StoryIds.elementAt(i), getApplicationContext());
                    if (story.rewards.size() == 0) {

                    } else {
                        LayoutInflater inflater = LayoutInflater.from(StoryIndex.this);
                        final View rewardView = inflater.inflate(R.layout.story_index_reward_box, null);
                        LinearLayout rewardViewBox = (LinearLayout) rewardView.findViewById(R.id.StoryIndex_Reward_Box);

                        if (story.rewards.elementAt(0).type.equals("hero")) {
                            ImageView imageView = new ImageView(StoryIndex.this);
                            Player player = new Player(story.rewards.elementAt(0).value, getApplicationContext());
                            imageView.setImageBitmap(player.character.img);
                            LinearLayout.LayoutParams layoutParams =
                                    new LinearLayout.LayoutParams(
                                            Common.DP2PX(50, getApplicationContext()),
                                            Common.DP2PX(50, getApplicationContext()));
                            layoutParams.setMargins(0, 0, Common.DP2PX(5, getApplicationContext()), 0);
                            imageView.setLayoutParams(layoutParams);
                            TextView tv = new TextView(StoryIndex.this);
                            tv.setHeight(Common.DP2PX(50, getApplicationContext()));
                            tv.setGravity(Gravity.CENTER);
                            tv.setTextSize(16);
                            tv.setText(player.heroName);
                            rewardViewBox.addView(imageView);
                            rewardViewBox.addView(tv);
                        } else {
                            TextView tv = new TextView(StoryIndex.this);
                            tv.setTextSize(16);
                            tv.setText("$" + story.rewards.elementAt(0).value);
                            rewardViewBox.addView(tv);
                        }
                        AlertDialog alertDialog;
                        alertDialog = new AlertDialog.Builder(StoryIndex.this).create();
                        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        alertDialog.setView(rewardView);
                        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                Common.SetFullScreen(getWindow());
                            }
                        });
                        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alertDialog.getWindow().setFlags(
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                        alertDialog.show();
                        Common.SetFullScreen(alertDialog.getWindow());
                        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                        alertDialog.getWindow().setLayout(
                                Common.DP2PX(360, getApplicationContext()),
                                Common.DP2PX(120, getApplicationContext()));

                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        Common.SetFullScreen(getWindow());
        super.onResume();
    }
}
