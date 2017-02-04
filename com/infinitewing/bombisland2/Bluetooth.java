package com.infinitewing.bombisland2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.infinitewing.bombisland2.GameObject.Common;
import com.infinitewing.bombisland2.GameObject.Map;
import com.infinitewing.bombisland2.GameObject.Player;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

/**
 * Created by Administrator on 2016/8/20.
 */
public class Bluetooth extends Activity {
    public Boolean IsServer, IsStarting, IsPlaying=false, IsFinishing = false;
    public Intent intent;
    Handler mHandler;
    BluetoothAdapter mBluetoothAdapter;
    Vector<BluetoothDevice> bluetoothDevices;
    ArrayAdapter<String> mArrayAdapter;
    ListView listView;
    // Register the BroadcastReceiver
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

    //GameView
    ConnectedThread connectedThread = null;

    public GameListener gamelistener;
    public GameView gameView;
    public MediaPlayer gamebackgroundsound;
    public Player player1, player2;
    public Map map;
    public View contentView;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "(" + device.getAddress() + ")");
                bluetoothDevices.add(device);
                findViewById(R.id.GameVersusPlayer_BTNowLoading).setVisibility(View.GONE);
            }
            listView.setAdapter(mArrayAdapter);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        IsStarting = false;
        gamebackgroundsound = MediaPlayer.create(this, R.raw.ai_choose);
        gamebackgroundsound.setVolume(0.3f, 0.3f);
        gamebackgroundsound.setLooping(true);
        gamebackgroundsound.start();
        IsServer = getIntent().getBooleanExtra("IsServer", true);
        setContentView(R.layout.game_versus_player);
        if (IsServer) {
            findViewById(R.id.GameVersusPlayer_BTNowLoading).setVisibility(View.GONE);
            findViewById(R.id.GameVersusPlayer_BTListLO).setVisibility(View.GONE);
            map = new Map("map01", getApplicationContext());
            InitialGame();
        } else {

            ((ProgressBar) findViewById(R.id.GameVersusPlayer_ProgressBar1))
                    .getIndeterminateDrawable()
                    .setColorFilter((getResources().getColor(R.color.theme_color)), PorterDuff.Mode.SRC_IN);
            ((ProgressBar) findViewById(R.id.GameVersusPlayer_ProgressBar2))
                    .getIndeterminateDrawable()
                    .setColorFilter((getResources().getColor(R.color.theme_color)), PorterDuff.Mode.SRC_IN);
            bluetoothDevices = new Vector<>();
            listView = (ListView) this.findViewById(R.id.GameVersusPlayer_LV);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String item = mArrayAdapter.getItem(i);
                    if (!IsServer) {
                        ConnectThread connectThread = new ConnectThread(bluetoothDevices.elementAt(i));
                        connectThread.start();
                    }
                }
            });
            mArrayAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_expandable_list_item_1);
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device Not Support BT", Toast.LENGTH_SHORT).show();
            Bluetooth.this.finish();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 10);
        }
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
        startActivityForResult(discoverableIntent, 2);
    }

    public void FindDevice() {
        /*
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "(" + device.getAddress() + ")");
                bluetoothDevices.add(device);
            }
        }
        */
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    public class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(Common.APP_NAME, UUID.fromString(Common.APP_UUID));
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    ManageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(Common.APP_UUID));
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            ManageConnectedSocket(mmSocket);
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public void ManageConnectedSocket(BluetoothSocket socket) {
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    public void ReStartNewGame() {
        setContentView(R.layout.game_versus_player);

        findViewById(R.id.GameVersusPlayer_BTNowLoading).setVisibility(View.GONE);
        findViewById(R.id.GameVersusPlayer_BTListLO).setVisibility(View.GONE);
        ShowMap(true);
        ShowHero(0, true);
        ShowHero(1, true);
        if (IsServer) {
            findViewById(R.id.GameVersusPlayer_PlayerIV1).setOnClickListener(new ClickListener());
            findViewById(R.id.GameVersusPlayer_Submit).setOnClickListener(new ClickListener());
            findViewById(R.id.GameVersusPlayer_ChooseMapTV).setOnClickListener(new ClickListener());
            findViewById(R.id.GameVersusPlayer_IV).setOnClickListener(new ClickListener());
        } else {
            findViewById(R.id.GameVersusPlayer_PlayerIV2).setOnClickListener(new ClickListener());
            findViewById(R.id.GameVersusPlayer_ChooseMapTV).setVisibility(View.GONE);
            findViewById(R.id.GameVersusPlayer_Submit).setVisibility(View.GONE);
        }
    }

    public void InitialGame() {
        try {
            if (IsServer) {
                player1 = new Player("ai01", getApplicationContext());
                player2 = new Player("ai_null", getApplicationContext());
                ((ImageView) findViewById(R.id.GameVersusPlayer_PlayerIV1)).setImageBitmap(player1.character.img);
                ((ImageView) findViewById(R.id.GameVersusPlayer_PlayerIV2)).setImageBitmap(player2.character.img);
                findViewById(R.id.GameVersusPlayer_PlayerIV1).setOnClickListener(new ClickListener());
                findViewById(R.id.GameVersusPlayer_IV).setOnClickListener(new ClickListener());
                findViewById(R.id.GameVersusPlayer_ChooseMapTV).setOnClickListener(new ClickListener());
                findViewById(R.id.GameVersusPlayer_Submit).setOnClickListener(new ClickListener());
                ShowMap();
            } else {
                findViewById(R.id.GameVersusPlayer_BTListLO).setVisibility(View.GONE);
                findViewById(R.id.GameVersusPlayer_ChooseMapTV).setVisibility(View.GONE);
                findViewById(R.id.GameVersusPlayer_Submit).setVisibility(View.GONE);
                LoadServerInitData();
            }
        } catch (Exception e) {
            e.getCause();
        }
    }

    public void LoadServerInitData() {
        try {
            player2 = new Player("ai02", getApplicationContext());
            ((ImageView) findViewById(R.id.GameVersusPlayer_PlayerIV2)).setImageBitmap(player2.character.img);
            findViewById(R.id.GameVersusPlayer_PlayerIV2).setOnClickListener(new ClickListener());
            if (connectedThread != null) {
                String data = "LoadServerInitData@12&";
                connectedThread.write(data.getBytes());
            }
            ShowHero(1, false);
        } catch (Exception e) {
            e.getCause();
        }
    }

    public void ShowMap() {
        this.ShowMap(false);
    }

    public void ShowMap(boolean restart) {
        TextView tv = (TextView) findViewById(R.id.GameVersusPlayer_TitleTV);
        tv.setText(map.title);
        tv = (TextView) findViewById(R.id.GameVersusPlayer_LimitTV);
        tv.setText((String)getText(R.string.game_choose_map_limit) + map.MaxPlayer);
        ImageView iv = (ImageView) findViewById(R.id.GameVersusPlayer_IV);
        Bitmap b = Common.getBitmapFromAsset("minimap/" + map.id + ".png", getApplicationContext());
        iv.setImageBitmap(b);
        if (IsServer && !restart) {
            if (connectedThread != null) {
                String data = "ChangeMap@" + map.id + "&";
                connectedThread.write(data.getBytes());
            }
        }
    }

    public void ShowHero(int i, Boolean hadPassValue) {
        if (i == 0) {
            ((ImageView) findViewById(R.id.GameVersusPlayer_PlayerIV1)).setImageBitmap(player1.character.img);
            if (!hadPassValue) {
                if (connectedThread != null) {
                    String data = "ChangeHero@" + player1.id + "&";
                    connectedThread.write(data.getBytes());
                }
            }
        } else if (i == 1) {
            if (player2.character.img != null)
                ((ImageView) findViewById(R.id.GameVersusPlayer_PlayerIV2)).setImageBitmap(player2.character.img);
            if (!hadPassValue) {
                if (connectedThread != null) {
                    String data = "ChangeHero@" + player2.id + "&";
                    connectedThread.write(data.getBytes());
                }
            }
        }
    }

    public void StartGame() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
            contentView = findViewById(android.R.id.content);
            IsPlaying = true;
            gamebackgroundsound = new MediaPlayer();
            intent = this.getIntent();
            setScreenSize(getApplicationContext());
            if (IsServer) {
                gameView = new GameView(this, player1.id, player2.id, true, true, map.id, 1, gamebackgroundsound);
            } else {
                gameView = new GameView(this, player1.id, player2.id, true, false, map.id, 2, gamebackgroundsound);
            }
            gameView.connectedThread = connectedThread;
            setContentView(gameView);
            gamelistener = new GameListener();
            IntentFilter filter = new IntentFilter();
            filter.addAction("Destroy");
            registerReceiver(gamelistener, filter);
        } catch (Exception e) {
            e.getCause();
        }
    }

    public void setScreenSize(Context context) {
        int x, y, orientation = context.getResources().getConfiguration().orientation;
        WindowManager wm = ((WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point screenSize = new Point();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealSize(screenSize);
                x = screenSize.x;
                y = screenSize.y;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    display.getSize(screenSize);
                }
                x = screenSize.x;
                y = screenSize.y;
            }
        } else {
            x = display.getWidth();
            y = display.getHeight();
        }

        int width = x;
        int height = y;
        Common.SCREEN_WIDTH = width;
        Common.SCREEN_HEIGHT = height;
    }

    private class GameListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ErrorDestroy();
        }
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        protected final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            if (!IsServer) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        InitialGame();
                    }
                });
            }
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    mmInStream.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (buffer[0] != 0) {
                        String datas = new String(buffer).split("\0")[0];
                        if (datas.lastIndexOf("&") > 0) {
                            datas = datas.substring(0, datas.lastIndexOf("&"));
                        }
                        if (datas.length() > 0) {
                            for (String data : datas.split("&")) {
                                String[] dataFrame = data.split("@");
                                if (dataFrame[0] != null && dataFrame[0].length() > 0) {
                                    if (dataFrame[0].equals("PlayerInfo")) {
                                        data = dataFrame[1];
                                        String[] playersInfo = data.split(";");
                                        for (String info : playersInfo) {
                                            String[] infoDetail = info.split(",");
                                            Player player;
                                            player = new Player(infoDetail[0].substring(0, infoDetail[0].length() - 2),
                                                    Integer.parseInt(infoDetail[1]),
                                                    Integer.parseInt(infoDetail[2]), gameView.map);
                                            player.uid = infoDetail[0];
                                            if (player.uid.equals(gameView.playerID)) {
                                                player.InitEmotion("emotion_player");
                                                player.teamID = 2;
                                            } else {
                                                player.InitEmotion("emotion_ai");
                                                player.teamID = 1;
                                            }
                                            gameView.map.players.add(player);
                                        }
                                    } else if (dataFrame[0].equals("PlayerMapInfo")) {
                                        data = dataFrame[1];
                                        String[] mapInfo = data.split(",");
                                        int i = 0;
                                        for (String info : mapInfo) {
                                            gameView.map.mapObjects.elementAt(i).randNum = Integer.parseInt(info);
                                            gameView.map.mapObjects.elementAt(i).item = null;
                                            gameView.map.mapObjects.elementAt(i).SetExplosionItem();
                                            i++;
                                        }
                                        String data2 = "PlayerHadInitBT@1";
                                        data2 += "&";
                                        byte[] data3 = data2.getBytes();
                                        gameView.hadInitPlayer = true;
                                        gameView.hadInitBT = true;
                                        this.write(data3);
                                    } else if (dataFrame[0].equals("PlayerFrameEnd")) {
                                        data = dataFrame[1];
                                        gameView.BTgameTime = Integer.parseInt(data);
                                    } else if (dataFrame[0].equals("PlayerHadInitBT")) {
                                        gameView.hadInitBT = true;
                                    } else if (dataFrame[0].equals("BTPlayerMove")) {
                                        data = dataFrame[1];
                                        String id = data.split(",")[0];
                                        int x = Integer.parseInt(data.split(",")[1]);
                                        int y = Integer.parseInt(data.split(",")[2]);
                                        int d = Integer.parseInt(data.split(",")[3]);
                                        int s = Integer.parseInt(data.split(",")[4]);
                                        gameView.BTPlayerMoveTo(id, x, y, d, s);
                                        if (gameView.IS_SERVER) {
                                            gameView.gameThread.lastDraw = System.currentTimeMillis();
                                        }
                                    } else if (dataFrame[0].equals("BTPlayerAddBomb")) {
                                        data = dataFrame[1];
                                        String id = data.split(",")[0];
                                        int x = Integer.parseInt(data.split(",")[1]);
                                        int y = Integer.parseInt(data.split(",")[2]);
                                        int time = Integer.parseInt(data.split(",")[3]);
                                        gameView.BTPlayerAddBombTo(id, x, y, time);
                                    } else if (dataFrame[0].equals("ChangeHero")) {
                                        String id = dataFrame[1];
                                        if (IsServer) {
                                            player2 = new Player(id, getApplicationContext());
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    ShowHero(1, true);//Client 端換了英雄
                                                }
                                            });
                                        } else {
                                            player1 = new Player(id, getApplicationContext());
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    ShowHero(0, true);//Server 端換了英雄
                                                }
                                            });
                                        }
                                    } else if (dataFrame[0].equals("ChangeMap")) {
                                        final String id = dataFrame[1];
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                map = new Map(id, getApplicationContext());//Server 端換了地圖
                                                ShowMap();
                                            }
                                        });
                                    } else if (dataFrame[0].equals("LoadServerInitData")) {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                ShowMap();
                                                ShowHero(0, false);
                                            }
                                        });
                                    } else if (dataFrame[0].equals("StartGame")) {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                StartGame();
                                            }
                                        });
                                    } else if (dataFrame[0].equals("EndGame")) {
                                        if (IsPlaying) {
                                            IsPlaying = false;
                                            IsStarting = false;
                                            String add = "EndGame@1&";
                                            connectedThread.write(add.getBytes());
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    ReStartNewGame();
                                                }
                                            });
                                        }
                                    } else if (dataFrame[0].equals("FinishGame")) {
                                        ErrorDestroy();
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.getCause();
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                mmOutStream.flush();
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public void FinishGame() {
        if (gamebackgroundsound != null) {
            gamebackgroundsound.stop();
        }
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {

        }
        if (connectedThread != null) {
            String finish = "FinishGame@1&";
            connectedThread.write(finish.getBytes());
            connectedThread.cancel();
        }
    }

    public void ErrorDestroy() {
        if (!IsFinishing) {
            IsFinishing = true;
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.game_versus_player_disconnect, Toast.LENGTH_SHORT).show();
                    FinishGame();
                    Bluetooth.this.finish();
                }
            });
        }
    }

    public class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.GameVersusPlayer_PlayerIV1:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            intent = new Intent(Bluetooth.this, GameChooseHero.class);
                            intent.putExtra("hero", player1.id);
                            startActivityForResult(intent, 3);
                        }
                    });
                    break;
                case R.id.GameVersusPlayer_PlayerIV2:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            intent = new Intent(Bluetooth.this, GameChooseHero.class);
                            intent.putExtra("hero", player2.id);
                            startActivityForResult(intent, 3);
                        }
                    });
                    break;
                case R.id.GameVersusPlayer_ChooseMapTV:
                case R.id.GameVersusPlayer_IV:

                    runOnUiThread(new Runnable() {
                        public void run() {
                            intent = new Intent(Bluetooth.this, GameChooseMap.class);
                            intent.putExtra("map", map.id);
                            startActivityForResult(intent, 1);
                        }
                    });

                    break;
                case R.id.GameVersusPlayer_Submit:
                    if (!IsStarting) {
                        if (connectedThread != null) {
                            IsStarting = true;
                            String data = "StartGame@Start&";
                            connectedThread.write(data.getBytes());
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    StartGame();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.game_versus_player_no_player, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (IsPlaying) {
            if (gameView != null) {
                gameView.BTErrorDestroySignal();
            }
        }
        FinishGame();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        gamebackgroundsound.start();
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == 600) {
            registerReceiver(mReceiver, filter);
            if (IsServer) {
                AcceptThread acceptThread = new AcceptThread();
                acceptThread.start();
            } else {
                FindDevice();
            }
        }
        if (requestCode == 1 && resultCode == RESULT_OK) {
            map = new Map(data.getStringExtra("map"), getApplicationContext());
            ShowMap();
        }
        if (requestCode == 3 && resultCode == RESULT_OK) {
            if (IsServer) {
                player1 = new Player(data.getStringExtra("hero"), getApplicationContext());
                ShowHero(0, false);
            } else {
                player2 = new Player(data.getStringExtra("hero"), getApplicationContext());
                ShowHero(1, false);
            }
        }
    }


    @Override
    protected void onDestroy() {
        FinishGame();
        super.onDestroy();
        Bluetooth.this.finish();
    }

}
