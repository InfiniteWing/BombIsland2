package com.infinitewing.bombisland2.GameObject;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by InfiniteWing on 2017/4/5.
 */
public class SoundThread extends Thread {
    public SoundPool soundPool;
    public boolean stop = false;
    public float volume = 1.0f;
    public Context context;
    public BlockingQueue<String> soundsPath = new LinkedBlockingQueue<>();
    public HashMap<String, Integer> soundsPathAndIDHashMap = new HashMap<>();

    public SoundThread() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder().setAudioAttributes(audioAttrib).setMaxStreams(200).build();
        } else {
            soundPool = new SoundPool(200, AudioManager.STREAM_MUSIC, 0);
        }
        soundPool
                .setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool,
                                               int sampleId, int status) {
                        soundPool.play(sampleId, 1, 1, 0, 0, 1);
                    }
                });
    }
    public SoundThread(final float volume) {
        this.volume = volume;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder().setAudioAttributes(audioAttrib).setMaxStreams(200).build();
        } else {
            soundPool = new SoundPool(200, AudioManager.STREAM_MUSIC, 0);
        }
        soundPool
                .setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool,
                                               int sampleId, int status) {
                        soundPool.play(sampleId, volume, volume, 0, 0, 1);
                    }
                });
    }
    public void Release(){
        for(String soundPath:soundsPathAndIDHashMap.keySet()){
            int soundID = soundsPathAndIDHashMap.get(soundPath);
            soundPool.unload(soundID);
        }
        soundPool.release();
    }
    @Override
    /**
     * Wait for sounds to play
     */
    public void run() {

        try {
            while (!this.stop) {
                String soundPath = this.soundsPath.take();
                if (soundsPathAndIDHashMap.get(soundPath) != null) {
                    int soundID = soundsPathAndIDHashMap.get(soundPath);
                    soundPool.play(soundID, volume, volume, 0, 0, 1);
                } else {
                    AssetFileDescriptor soundAFD;
                    if (context == null) {
                        soundAFD = Common.getAssetsFileDescripter("sound/" + soundPath);
                    } else {
                        soundAFD = Common.getAssetsFileDescripter("sound/" + soundPath, context);
                    }
                    if (soundAFD == null)
                        continue;
                    int soundID = soundPool.load(soundAFD, 1);
                    if (soundsPathAndIDHashMap.get(soundPath) == null) {
                        soundsPathAndIDHashMap.put(soundPath, soundID);
                    }
                }
            }

        } catch (InterruptedException e) {
            e.getCause();
        }
    }

    public void addSound(String AssetFilePath) {
        if (!Common.gameView.effSound)
            return;
        soundsPath.add(AssetFilePath);
    }

    public void addSound(String AssetFilePath, Context c) {
        this.context = c;
        soundsPath.add(AssetFilePath);
    }
}
