package com.infinitewing.bombisland2.GameObject;

import java.util.Random;

/**
 * Created by InfiniteWing on 2016/8/18.
 */
public class Location {
    public static final int LOCATION_TOP=1,LOCATION_RIGHT=2,LOCATION_DOWN=3,LOCATION_LEFT=4,LOCATION_DEFAULT=5;
    public int x,y;
    public Location(int x,int y){
        this.x=x;
        this.y=y;
    }
    public static Boolean Collapse(Location l1,Location l2,int d){
        switch(d){
            case LOCATION_TOP:
                return l1.y-1==l2.y&&l1.x==l2.x;
            case LOCATION_RIGHT:
                return l1.y==l2.y&&l1.x+1==l2.x;
            case LOCATION_DOWN:
                return l1.y+1==l2.y&&l1.x==l2.x;
            case LOCATION_LEFT:
                return l1.y==l2.y&&l1.x-1==l2.x;
            default:
                return false;
        }
    }
    public static final int RandomLocation(){

        return Common.RandomNum(4);
    }
}
