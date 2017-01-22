package com.infinitewing.bombisland2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.infinitewing.bombisland2.GameObject.Common;
import com.infinitewing.bombisland2.GameObject.Map;
import com.infinitewing.bombisland2.GameObject.Player;
import com.infinitewing.bombisland2.GameObject.Recorder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * Created by Administrator on 2016/8/25.
 */
public class GameChooseHero extends Activity {
    private Intent intent;
    private Resources res;
    private Vector<Player> heroLists;
    private String nowHero;
    private Vector<String> buyedHeros;
    private int money = 1000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_choose_hero);
        heroLists = new Vector<>();
        buyedHeros = new Vector<>();
        res = getResources();
        nowHero = getIntent().getStringExtra("hero");
        LoadBuyedHeros();
        LoadHeroList();
        findViewById(R.id.GameChooseHero_Buy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (final Player hero : heroLists) {
                    if (hero.id.equals(nowHero)) {
                        String message = "";
                        message += "請問是否購買本英雄(" + hero.heroName + ")\n" +
                                "英雄售價 $" + hero.price + "，您目前持有 $" + money;
                        new AlertDialog.Builder(GameChooseHero.this)
                                .setTitle(R.string.game_choose_hero_buy_title)
                                .setMessage(message)
                                .setPositiveButton(R.string.game_choose_hero_buy, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (hero.price <= money) {
                                            Toast.makeText(getApplicationContext(), R.string.game_choose_hero_buy_success, Toast.LENGTH_SHORT).show();
                                            buyedHeros.add(hero.id);
                                            SaveBuyedHeros();
                                            money -= hero.price;
                                            SaveMoney();
                                            ShowHeroInfo(hero.id);
                                        } else {
                                            Toast.makeText(getApplicationContext(), R.string.game_choose_hero_buy_error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .setNegativeButton(R.string.game_choose_hero_buy_cancel, null)
                                .show();
                    }
                }
            }
        });
    }


    private void LoadBuyedHeros() {
        String file = "buyedHeros.bl2";
        Recorder recorder = new Recorder(getApplicationContext());
        String buyedRecords = recorder.Read(file);
        if (buyedRecords != null) {
            for (String id : buyedRecords.split(",")) {
                buyedHeros.add(id);
            }
        }
        file = "money.bl2";
        String moneyRecord = recorder.Read(file);
        if (moneyRecord != null) {
            money = Integer.parseInt(moneyRecord);
        }
    }

    private void SaveMoney() {
        String file = "money.bl2";
        Recorder recorder = new Recorder(getApplicationContext());
        recorder.Write(String.valueOf(money), file);
    }

    private void SaveBuyedHeros() {
        String file = "buyedHeros.bl2";
        Recorder recorder = new Recorder(getApplicationContext());
        boolean c = false;
        String data = "";
        for (String id : buyedHeros) {
            if (c) {
                data += ",";
            }
            data += id;
            c = true;
        }
        recorder.Write(data, file);
    }


    public void LoadHeroList() {
        String path = "xml/hero/hero_list.txt";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open(path)));
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
            String id = line.split(",")[0];
            int price = Integer.parseInt(line.split(",")[1]);
            Player player = new Player(id, getApplicationContext());
            player.price = price;
            if (price == 0) {
                buyedHeros.add(id);
            }
            heroLists.add(player);
        } while (line != null);
        ShowHeroList();
    }

    public void ShowHeroList() {
        ArrayAdapter<String> listAdapter;
        String[] list = new String[heroLists.size()];
        for (int i = 0; i < heroLists.size(); i++) {
            list[i] = heroLists.elementAt(i).heroName;
        }
        ListView listView = (ListView) findViewById(R.id.GameChooseHero_LV);
        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                ShowHeroInfo(which);
            }
        });
        ShowHeroInfo(nowHero);
        findViewById(R.id.GameChooseHero_Submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buyedHeros.indexOf(nowHero)>=0) {
                    Intent intent = new Intent();
                    intent.putExtra("hero", nowHero);
                    setResult(RESULT_OK, intent);
                    GameChooseHero.this.finish();
                }else{
                    Toast.makeText(getApplicationContext(), R.string.game_choose_hero_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void ShowHeroInfo(int index) {
        TextView tv = (TextView) findViewById(R.id.GameChooseHero_TitleTV);
        tv.setText(heroLists.elementAt(index).heroName);
        tv = (TextView) findViewById(R.id.GameChooseHero_TV);
        tv.setText(heroLists.elementAt(index).GetIntro(getApplicationContext()));
        ImageView iv = (ImageView) findViewById(R.id.GameChooseHero_IV);
        Bitmap b = heroLists.elementAt(index).character.img;
        iv.setImageBitmap(b);
        nowHero = heroLists.elementAt(index).id;
        if (buyedHeros.indexOf(nowHero) >= 0) {
            findViewById(R.id.GameChooseHero_Buy).setVisibility(View.GONE);
        } else {
            findViewById(R.id.GameChooseHero_Buy).setVisibility(View.VISIBLE);
        }
    }

    public void ShowHeroInfo(String id) {
        for (int index = 0; index < heroLists.size(); index++) {
            if (heroLists.elementAt(index).id.equals(id)) {
                TextView tv = (TextView) findViewById(R.id.GameChooseHero_TitleTV);
                tv.setText(heroLists.elementAt(index).heroName);
                tv = (TextView) findViewById(R.id.GameChooseHero_TV);
                tv.setText(heroLists.elementAt(index).GetIntro(getApplicationContext()));
                ImageView iv = (ImageView) findViewById(R.id.GameChooseHero_IV);
                Bitmap b = heroLists.elementAt(index).character.img;
                iv.setImageBitmap(b);
                nowHero = heroLists.elementAt(index).id;
                if (buyedHeros.indexOf(nowHero) >= 0) {
                    findViewById(R.id.GameChooseHero_Buy).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.GameChooseHero_Buy).setVisibility(View.VISIBLE);
                }
                break;
            }
        }
    }
}
