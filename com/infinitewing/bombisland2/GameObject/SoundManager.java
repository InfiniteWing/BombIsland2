package com.infinitewing.bombisland2.GameObject;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 2016/8/20.
 */
public class SoundManager extends Thread {
    public Vector<SoundThread> soundThreads;
    public int soundThreadIndex;
    public final int SoundThreadCount=8;

    public SoundManager() {
        soundThreads=new Vector<>();
        soundThreadIndex=0;
        for (int i=0;i<SoundThreadCount;i++){
            SoundThread soundThread=new SoundThread();
            soundThread.start();
            soundThreads.add(soundThread);
        }
    }
    public void Stop(){
        try{
            for (int i=0;i<SoundThreadCount;i++){
                soundThreads.elementAt(i).stop=true;
            }
        }catch (Exception e){
            e.getCause();
        }
    }
    public void addSound(String AssetFilePath) {
        soundThreadIndex++;
        if(soundThreadIndex>=soundThreads.size()){
            soundThreadIndex=0;
        }
        soundThreads.elementAt(soundThreadIndex).addSound(AssetFilePath);
    }

    public void addSound(String AssetFilePath, Context c) {
        soundThreadIndex++;
        if(soundThreadIndex>=soundThreads.size()){
            soundThreadIndex=0;
        }
        soundThreads.elementAt(soundThreadIndex).addSound(AssetFilePath,c);
    }
}