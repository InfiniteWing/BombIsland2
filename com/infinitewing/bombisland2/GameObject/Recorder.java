package com.infinitewing.bombisland2.GameObject;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by Administrator on 2016/8/31.
 */
public class Recorder {
    public Context context;
    public Recorder(Context c){
        context=c;
    }
    public void Write(String data,String file){
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput(file, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        OutputStreamWriter osw = new OutputStreamWriter(fileOutputStream);
        BufferedWriter bw = new BufferedWriter(osw);
        try {
            bw.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String Read(String file){
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = context.openFileInput(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(fileInputStream!=null) {
            InputStreamReader isr = new InputStreamReader(fileInputStream);
            char[] input = new char[0];
            try {
                input = new char[fileInputStream.available()];
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                isr.read(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new String(input);
        }else{
            return null;
        }
    }

}
