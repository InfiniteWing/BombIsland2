package com.infinitewing.bombisland2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.infinitewing.bombisland2.GameObject.Common;
import com.infinitewing.bombisland2.GameObject.Recorder;
import com.infinitewing.bombisland2.GameObject.SoundManager;

import org.w3c.dom.Text;

/**
 * Created by InfiniteWing on 2017/4/19.
 */
public class Store extends Activity {
    public int currentItemIndex = 0;
    public int money = 0;
    public int price = 0;
    public int quantity = 1;
    public SharedPreferences sp;
    public SharedPreferences.Editor spEditor;
    public AlertDialog alertDialog;
    public View buyConfirmView;
    public SoundManager soundManager;
    public boolean effSound;
    public long lastTouchTimeMillis = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.SetFullScreen(getWindow());
        setContentView(R.layout.store);
        findViewById(R.id.Store_Item_Revive).setOnClickListener(new ClickListener());
        findViewById(R.id.Store_Item_Shield).setOnClickListener(new ClickListener());
        findViewById(R.id.Store_Item_Bomb_Yellow).setOnClickListener(new ClickListener());
        findViewById(R.id.Store_Item_Bomb_Blue).setOnClickListener(new ClickListener());
        findViewById(R.id.Store_Item_Bomb_Red).setOnClickListener(new ClickListener());
        findViewById(R.id.Store_Item_Bomb_Purple).setOnClickListener(new ClickListener());
        findViewById(R.id.Store_Exit).setOnClickListener(new ClickListener());
        findViewById(R.id.Store_Buy).setOnClickListener(new ClickListener());
        sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
        spEditor = sp.edit();
    }

    public void ShowCurrentMoney() {
        ((TextView) findViewById(R.id.Store_Money_TV)).setText(String.valueOf(money));
    }

    public void LoadMoney() {
        String file = "money.bl2";
        Recorder recorder = new Recorder(getApplicationContext());
        String moneyRecord = recorder.Read(file);
        if (moneyRecord != null) {
            try {
                money = Integer.parseInt(moneyRecord);
            } catch (Exception e) {
                e.getCause();
                money = Common.DEFAULT_MONEY;
            }
        }
        ShowCurrentMoney();
    }

    public class touchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (System.currentTimeMillis() - lastTouchTimeMillis > 75) {
                int id = v.getId();
                switch (id) {
                    case R.id.Store_Buy_Confirm_Quantity_Add:
                        if (quantity < 99) {
                            quantity++;
                        }
                        break;
                    case R.id.Store_Buy_Confirm_Quantity_Minus:
                        if (quantity > 1) {
                            quantity--;
                        }
                        break;
                }
                RefreshTotalPrice();
                lastTouchTimeMillis = System.currentTimeMillis();
            }
            return true;
        }
    }

    public class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            String currentInfo = "";
            if (System.currentTimeMillis() - lastTouchTimeMillis < 100) {
                return;
            }
            lastTouchTimeMillis = System.currentTimeMillis();
            int count = 0;
            switch (id) {
                case R.id.Store_Exit:
                    Store.this.finish();
                    break;
                case R.id.Store_Buy:
                    Buy(false);
                    break;
                case R.id.Store_Item_Revive:
                    currentItemIndex = 1;
                    count = sp.getInt(Common.STORE_ITEM_REVIVE, 0);
                    currentInfo += count;
                    ShowItemInfo(R.string.store_item_revive, currentInfo);
                    break;
                case R.id.Store_Item_Shield:
                    currentItemIndex = 2;
                    count = sp.getInt(Common.STORE_ITEM_SHIELD, 0);
                    currentInfo += count;
                    ShowItemInfo(R.string.store_item_shield, currentInfo);
                    break;
                case R.id.Store_Item_Bomb_Yellow:
                    currentItemIndex = 3;
                    if (sp.getBoolean(Common.STORE_BOMBSKIN_YELLOW, false)) {
                        currentInfo = getString(R.string.store_greet_already_have_skin);
                    } else {
                        currentInfo = getString(R.string.store_greet_dont_have_skin);
                    }
                    ShowItemInfo(R.string.store_item_bomb_yellow, currentInfo);
                    break;
                case R.id.Store_Item_Bomb_Blue:
                    currentItemIndex = 4;
                    if (sp.getBoolean(Common.STORE_BOMBSKIN_BLUE, false)) {
                        currentInfo = getString(R.string.store_greet_already_have_skin);
                    } else {
                        currentInfo = getString(R.string.store_greet_dont_have_skin);
                    }
                    ShowItemInfo(R.string.store_item_bomb_blue, currentInfo);
                    break;
                case R.id.Store_Item_Bomb_Red:
                    currentItemIndex = 5;
                    if (sp.getBoolean(Common.STORE_BOMBSKIN_RED, false)) {
                        currentInfo = getString(R.string.store_greet_already_have_skin);
                    } else {
                        currentInfo = getString(R.string.store_greet_dont_have_skin);
                    }
                    ShowItemInfo(R.string.store_item_bomb_red, currentInfo);
                    break;
                case R.id.Store_Item_Bomb_Purple:
                    currentItemIndex = 6;
                    if (sp.getBoolean(Common.STORE_BOMBSKIN_PURPLE, false)) {
                        currentInfo = getString(R.string.store_greet_already_have_skin);
                    } else {
                        currentInfo = getString(R.string.store_greet_dont_have_skin);
                    }
                    ShowItemInfo(R.string.store_item_bomb_purple, currentInfo);
                    break;
            }
        }
    }

    public void ShowItemInfo(int rid, String currentInfo) {
        PlayCheckSound();
        String itemInfo = getString(rid);
        String priceString = itemInfo.split("\\$")[1];
        price = Integer.parseInt(priceString);
        ((TextView) findViewById(R.id.Store_TV)).setText(itemInfo);
        findViewById(R.id.Store_Stock_Layout).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.Store_Stock_TV)).setText(currentInfo);
    }

    public void HideStockInfo() {
        findViewById(R.id.Store_Stock_Layout).setVisibility(View.INVISIBLE);
    }

    public void RefreshTotalPrice() {
        ((EditText) buyConfirmView.findViewById(R.id.Store_Buy_Confirm_Quantity)).setText(String.valueOf(quantity));
        ((TextView) buyConfirmView.findViewById(R.id.Store_Buy_Confirm_TotalPrice)).setText(String.valueOf(price * quantity));
    }

    public void ShowBuyDialog(boolean showQantity, int imageID) {
        LayoutInflater inflater = LayoutInflater.from(Store.this);
        buyConfirmView = inflater.inflate(R.layout.store_buy_confirm, null);

        alertDialog = new AlertDialog.Builder(Store.this)
                .setPositiveButton(R.string.store_buy_confirm_submit,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Buy(true);
                            }
                        })
                .setNegativeButton(R.string.store_buy_confirm_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                .create();
        if (!showQantity) {
            buyConfirmView.findViewById(R.id.Store_Buy_Confirm_QuantityLayout).setVisibility(View.GONE);
        }
        ((ImageView) buyConfirmView.findViewById(R.id.Store_Buy_Confirm_IV)).setImageResource(imageID);
        ((TextView) buyConfirmView.findViewById(R.id.Store_Buy_Confirm_UnitPrice)).setText(String.valueOf(price));
        quantity = 1;
        RefreshTotalPrice();
        buyConfirmView.findViewById(R.id.Store_Buy_Confirm_Quantity_Add).setOnTouchListener(new touchListener());
        buyConfirmView.findViewById(R.id.Store_Buy_Confirm_Quantity_Minus).setOnTouchListener(new touchListener());
        ((EditText) buyConfirmView.findViewById(R.id.Store_Buy_Confirm_Quantity)).setText(String.valueOf(quantity));
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setView(buyConfirmView);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Common.SetFullScreen(getWindow());
            }
        });
        alertDialog.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        alertDialog.show();
        Common.SetFullScreen(alertDialog.getWindow());
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        alertDialog.getWindow().setLayout(
                Common.DP2PX(320, getApplicationContext()),
                alertDialog.getWindow().getAttributes().height);
    }

    public void SaveMoney() {
        String file = "money.bl2";
        Recorder recorder = new Recorder(getApplicationContext());
        recorder.Write(String.valueOf(money), file);
    }

    public void ShowAlreadyHaveBombSkin() {
        currentItemIndex = 0;
        ((TextView) findViewById(R.id.Store_TV)).setText(R.string.store_greet_already_have_skin_alert);
        ShowCurrentMoney();
        PlayCheckSound();
        HideStockInfo();
    }

    public void Buy(boolean confirm) {
        if (currentItemIndex == 0) {
            ((TextView) findViewById(R.id.Store_TV)).setText(R.string.store_greet_error_chooseitem);
            PlayCheckSound();
            return;
        }
        if (confirm) {
            if (quantity * price > money) {
                currentItemIndex = 0;
                ((TextView) findViewById(R.id.Store_TV)).setText(R.string.store_greet_error_nomoney);
                PlayCheckSound();
                HideStockInfo();
                return;
            } else {
                money -= quantity * price;
                SaveMoney();
            }
        }
        switch (currentItemIndex) {
            case 1:
                if (confirm) {
                    spEditor.putInt(Common.STORE_ITEM_REVIVE,
                            quantity + sp.getInt(Common.STORE_ITEM_REVIVE, 0)).commit();
                } else {
                    ShowBuyDialog(true, R.drawable.revive);
                }
                break;
            case 2:
                if (confirm) {
                    spEditor.putInt(Common.STORE_ITEM_SHIELD,
                            quantity + sp.getInt(Common.STORE_ITEM_SHIELD, 0)).commit();
                } else {
                    ShowBuyDialog(true, R.drawable.shield);
                }
                break;
            case 3:
                if (!sp.getBoolean(Common.STORE_BOMBSKIN_YELLOW, false)) {
                    if (confirm) {
                        spEditor.putBoolean(Common.STORE_BOMBSKIN_YELLOW, true).commit();
                    } else {
                        ShowBuyDialog(false, R.drawable.bomb_yellow);
                    }
                } else {
                    ShowAlreadyHaveBombSkin();
                    return;
                }
                break;
            case 4:
                if (!sp.getBoolean(Common.STORE_BOMBSKIN_BLUE, false)) {
                    if (confirm) {
                        spEditor.putBoolean(Common.STORE_BOMBSKIN_BLUE, true).commit();
                    } else {
                        ShowBuyDialog(false, R.drawable.bomb_blue);
                    }
                } else {
                    ShowAlreadyHaveBombSkin();
                    return;
                }
                break;
            case 5:
                if (!sp.getBoolean(Common.STORE_BOMBSKIN_RED, false)) {
                    if (confirm) {
                        spEditor.putBoolean(Common.STORE_BOMBSKIN_RED, true).commit();
                    } else {
                        ShowBuyDialog(false, R.drawable.bomb_red);
                    }
                } else {
                    ShowAlreadyHaveBombSkin();
                    return;
                }
                break;
            case 6:
                if (!sp.getBoolean(Common.STORE_BOMBSKIN_PURPLE, false)) {
                    if (confirm) {
                        spEditor.putBoolean(Common.STORE_BOMBSKIN_PURPLE, true).commit();
                    } else {
                        ShowBuyDialog(false, R.drawable.bomb_purple);
                    }
                } else {
                    ShowAlreadyHaveBombSkin();
                    return;
                }
                break;
        }
        if (confirm) {
            currentItemIndex = 0;
            ((TextView) findViewById(R.id.Store_TV)).setText(R.string.store_greet_thanks);
            ShowCurrentMoney();
            PlayCheckSound();
            HideStockInfo();
        }else{
            PlayCheckSound();
        }
    }

    public void PlayCheckSound() {
        if (effSound) {
            soundManager.addSound("check.mp3", getApplicationContext());
        }
    }

    @Override
    protected void onResume() {
        Common.SetFullScreen(getWindow());
        LoadMoney();
        soundManager = new SoundManager(0.3f);
        soundManager.start();
        sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
        effSound = sp.getBoolean("effSound", true);
        super.onResume();
    }

    //這是讓虛擬按鍵不見的程式，還需要修改..
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            delayedHide(300);
        } else {
            mHideHandler.removeMessages(0);
        }
    }

    private final Handler mHideHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Common.SetFullScreen(getWindow());
        }
    };

    private void delayedHide(int delayMillis) {
        mHideHandler.removeMessages(0);
        mHideHandler.sendEmptyMessageDelayed(0, delayMillis);
    }
}
