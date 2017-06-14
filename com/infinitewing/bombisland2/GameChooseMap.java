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
import com.infinitewing.bombisland2.GameObject.Recorder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * Created by Administrator on 2016/8/25.
 */
public class GameChooseMap extends Activity {
    private Intent intent;
    private Resources res;
    private Vector<Map> mapLists;
    private Vector<String> buyedMaps;
    private String nowMap;
    private int money = Common.DEFAULT_MONEY, maxPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.SetFullScreen(getWindow());
        setContentView(R.layout.game_choose_map);
        mapLists = new Vector<>();
        buyedMaps = new Vector<>();
        nowMap = getIntent().getStringExtra("map");
        res = getResources();
        LoadBuyedMaps();
        LoadMapList();
        findViewById(R.id.GameChooseMap_Guide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowGuide();
            }
        });
        findViewById(R.id.GameChooseMap_Back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameChooseMap.this.finish();
            }
        });
        findViewById(R.id.GameChooseMap_Buy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (final Map map : mapLists) {
                    if (map.id.equals(nowMap)) {
                        String message = "";
                        message += Common.getStringResourceByName("game_choose_map_buy_message01", getApplicationContext()) + "?\n" +
                                Common.getStringResourceByName("game_choose_map_buy_message02", getApplicationContext()) + map.price +
                                Common.getStringResourceByName("game_choose_map_buy_message03", getApplicationContext()) + money;
                       AlertDialog alertDialog= new AlertDialog.Builder(GameChooseMap.this)
                                .setTitle(R.string.game_choose_map_buy_title)
                                .setMessage(message)
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        Common.SetFullScreen(getWindow());
                                    }
                                })
                                .setPositiveButton(R.string.game_choose_map_buy, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (map.price <= money) {
                                            Toast.makeText(getApplicationContext(), R.string.game_choose_map_buy_success, Toast.LENGTH_SHORT).show();
                                            buyedMaps.add(map.id);
                                            SaveBuyedMaps();
                                            money -= map.price;
                                            SaveMoney();
                                            ShowMapInfo(map.id);
                                        } else {
                                            Toast.makeText(getApplicationContext(), R.string.game_choose_map_buy_error, Toast.LENGTH_SHORT).show();
                                        }
                                        Common.SetFullScreen(getWindow());
                                    }
                                })
                                .setNegativeButton(R.string.game_choose_map_buy_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Common.SetFullScreen(getWindow());
                                    }
                                })
                                .create();
                        Common.SetAlertDialog(alertDialog);
                    }
                }
            }
        });
    }

    private void LoadBuyedMaps() {
        String file = "buyedMaps.bl2";
        Recorder recorder = new Recorder(getApplicationContext());
        String buyedRecords = recorder.Read(file);
        if (buyedRecords != null) {
            for (String id : buyedRecords.split(",")) {
                buyedMaps.add(id);
            }
        }
        file = "money.bl2";
        String moneyRecord = recorder.Read(file);
        if (moneyRecord != null) {
            try {
                money = Integer.parseInt(moneyRecord);
            } catch (Exception e) {
                e.getCause();
                money = Common.DEFAULT_MONEY;
            }
        }
    }

    private void SaveMoney() {
        String file = "money.bl2";
        Recorder recorder = new Recorder(getApplicationContext());
        recorder.Write(String.valueOf(money), file);
    }

    private void SaveBuyedMaps() {
        String file = "buyedMaps.bl2";
        Recorder recorder = new Recorder(getApplicationContext());
        boolean c = false;
        String data = "";
        for (String id : buyedMaps) {
            if (c) {
                data += ",";
            }
            data += id;
            c = true;
        }
        recorder.Write(data, file);
    }

    public void LoadMapList() {
        String path = "xml/map_info/map_list.txt";
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
            Map map = new Map(id, getApplicationContext());
            map.price = price;
            if (price == 0) {
                buyedMaps.add(id);
            }
            mapLists.add(map);
        } while (line != null);
        ShowMapList();
    }

    public void ShowMapList() {
        ArrayAdapter<String> listAdapter;
        String[] list = new String[mapLists.size()];
        for (int i = 0; i < mapLists.size(); i++) {
            list[i] = mapLists.elementAt(i).title;
        }
        ListView listView = (ListView) findViewById(R.id.GameChooseMap_LV);
        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                ShowMapInfo(which);
            }
        });
        ShowMapInfo(nowMap);
        findViewById(R.id.GameChooseMap_Submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buyedMaps.indexOf(nowMap) >= 0) {
                    Intent intent = new Intent();
                    intent.putExtra("map", nowMap);
                    intent.putExtra("maxPlayer", maxPlayer);
                    setResult(RESULT_OK, intent);
                    GameChooseMap.this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.game_choose_map_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void ShowMapInfo(int index) {
        nowMap = mapLists.elementAt(index).id;
        ShowMapInfo(nowMap);
    }

    public void ShowMapInfo(String id) {
        for (int index = 0; index < mapLists.size(); index++) {
            if (mapLists.elementAt(index).id.equals(id)) {
                TextView tv = (TextView) findViewById(R.id.GameChooseMap_TitleTV);
                tv.setText(mapLists.elementAt(index).title);
                tv = (TextView) findViewById(R.id.GameChooseMap_TV);
                tv.setText(mapLists.elementAt(index).intro);
                ImageView iv = (ImageView) findViewById(R.id.GameChooseMap_IV);
                Bitmap b = Common.getBitmapFromAsset("minimap/" + mapLists.elementAt(index).id + ".png", getApplicationContext());
                iv.setImageBitmap(b);
                nowMap = mapLists.elementAt(index).id;
                if (buyedMaps.indexOf(nowMap) >= 0) {
                    findViewById(R.id.GameChooseMap_Buy).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.GameChooseMap_Buy).setVisibility(View.VISIBLE);
                }
                break;
            }
        }
        for (Map map : mapLists) {
            if (map.id.equals(nowMap)) {
                ((TextView) findViewById(R.id.GameChooseMap_LimitTV)).setText((String) getText(R.string.game_choose_map_limit) + map.MaxPlayer);
                maxPlayer = map.MaxPlayer;
            }
        }
    }

    public void ShowGuide() {
        Intent intent = new Intent(GameChooseMap.this, GameGuide.class);
        intent.putExtra("guide", "map");
        intent.putExtra("newbe", true);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        Common.SetFullScreen(getWindow());
        super.onResume();
    }
}
