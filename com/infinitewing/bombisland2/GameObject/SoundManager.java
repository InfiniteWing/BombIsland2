package com.infinitewing.bombisland2.GameObject;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.Vector;

/**
 * Created by Administrator on 2016/8/20.
 */
public class SoundManager {
    public Vector<Integer> removeList;
    public SoundPool soundPool;

    public SoundManager() {
        soundPool = new SoundPool(50, AudioManager.STREAM_MUSIC, 0);
        soundPool
                .setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool,
                                               int sampleId, int status) {
                        soundPool.play(sampleId, 1  , 1, 0, 0, 1);
                    }
                });
    }

    public void addSound(String AssetFilePath) {
        if(!Common.gameView.effSound)
            return;
        AssetFileDescriptor soundAFD = Common.getAssetsFileDescripter("sound/"+AssetFilePath);
        if(soundAFD==null)
            return;
        soundPool.load(soundAFD, 1);
    }
}