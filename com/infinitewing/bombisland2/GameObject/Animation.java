package com.infinitewing.bombisland2.GameObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by InfiniteWing on 2016/8/18.
 */
public class Animation {
    public int step, total_step, total_ms, ms_per_frame, width_unit, height_unit;
    public int source_width_unit, source_height_unit, source_width, source_height;
    public int source_x, source_y, source_direction;
    public int offset_height_unit = 0,offset_width_unit = 0;
    public int loop_step = 0, loop_frame, loop_total, loop_now;
    public Boolean loop;
    public Bitmap source;
    public Bitmap img;
    public Map map;
    public Boolean IsEnd() {
        return total_ms > total_step * ms_per_frame;
    }
    public Animation (Map map){
        this.map=map;
    }
    public void InitImage() {
        String imageCache=source.hashCode()+"."
                +source_x+","+source_y
                +step;
        img=map.imageCaches.get(imageCache);
        if(img==null){
            try {
                img = Bitmap.createBitmap(source,
                        (source_x + (step - 1)*width_unit) * source_width / source_width_unit,
                        source_y * source_height / source_height_unit,
                        width_unit * source_width / source_width_unit,
                        height_unit * source_height / source_height_unit);
            }catch (Exception e){
                img=Bitmap.createBitmap(source,
                        (source_x + (step - 1)*width_unit) * source_width / source_width_unit,
                        source_y * source_height / source_height_unit,
                        source_width,
                        source_height);
            }
            map.imageCaches.put(imageCache,img);
        }
    }

    public void Init() {
        step = 1;
        total_ms = 0;

        loop_now = 0;

        if (source != null) {
            source_width = source.getWidth();
            source_height = source.getHeight();
        }
        InitImage();
    }

    public void Reset() {
        if (loop) {
            step = 1;
            total_ms = 0;
            loop_now = 0;
            if (total_step > 1) {
                InitImage();
            }
        }
    }

    public void Play() {
        if (total_ms > step * ms_per_frame) {
            NextStep();
        }
        total_ms += Common.GAME_REFRESH;
    }

    public void NextStep() {
        if (step == total_step) {
            Reset();
        }else {
            step++;
            if (step == loop_step + loop_frame) {
                loop_now++;
                if (loop_now < loop_total) {
                    step = loop_step;
                    total_ms = (step - 1) * ms_per_frame;
                }
            }
            InitImage();
        }
    }
}
