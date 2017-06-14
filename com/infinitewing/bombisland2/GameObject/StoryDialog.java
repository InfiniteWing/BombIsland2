package com.infinitewing.bombisland2.GameObject;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by InfiniteWing on 2017/4/5.
 */
public class StoryDialog {
    public static final int SYSTEM=0,LEFT=1,CENTER=2,RIGHT=3;
    public static final String SYSTEM_STR="system";
    public String dialog;
    public String nameTag;
    public int direction;
    public Bitmap img;
    public StoryDialog(Story story,String nameTag,int direction,String imgID,String dialog,Context c){
        this.dialog=dialog;
        this.direction=direction;
        this.nameTag=nameTag;
        if(!imgID.equals(SYSTEM_STR)) {
            String imgPath="story/" + imgID + ".png";
            if(story.imageCaches.get(imgPath)==null) {
                img = Common.getBitmapFromAsset(imgPath, c);
                if (direction == LEFT) {
                    img = Common.RotateBitmap(img, c);
                }
                story.imageCaches.put(imgPath,img);
            }else{
                img = story.imageCaches.get(imgPath);
            }
        }else{
            img=null;
        }
    }
}
