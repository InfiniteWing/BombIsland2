package com.infinitewing.bombisland2.GameObject;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by InfiniteWing on 2017/4/5.
 */
public class SoundThread extends Thread {
    public SoundPool soundPool;
    public boolean stop = false;
    public BlockingQueue<AssetFileDescriptor> sounds = new LinkedBlockingQueue<>();

    public SoundThread() {
        soundPool = new SoundPool(50, AudioManager.STREAM_MUSIC, 0);
        soundPool
                .setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool,
                                               int sampleId, int status) {
                        soundPool.play(sampleId, 1, 1, 0, 0, 1);
                    }
                });
    }

    @Override
    /**
     * Wait for sounds to play
     */
    public void run() {

        try {
            while (!this.stop) {
                AssetFileDescriptor soundAFD = this.sounds.take();
                soundPool.load(soundAFD, 1);
            }

        } catch (InterruptedException e) {
            e.getCause();
        }
    }

    public void addSound(String AssetFilePath) {
        if (!Common.gameView.effSound)
            return;
        AssetFileDescriptor soundAFD = Common.getAssetsFileDescripter("sound/" + AssetFilePath);
        if (soundAFD == null)
            return;
        sounds.add(soundAFD);
    }

    public void addSound(String AssetFilePath, Context c) {
        AssetFileDescriptor soundAFD = Common.getAssetsFileDescripter("sound/" + AssetFilePath, c);
        if (soundAFD == null)
            return;
        sounds.add(soundAFD);
    }
}
