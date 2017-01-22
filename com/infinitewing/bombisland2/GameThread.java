package com.infinitewing.bombisland2;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.infinitewing.bombisland2.GameObject.Common;

/**
 * Created by Administrator on 2016/8/19.
 */
public class GameThread extends Thread {
    GameView gameView;
    SurfaceHolder surfaceholder;
    long timestampBefore, timeDiff, sleep;
    boolean stop = false;
    boolean HadDraw = false;

    public GameThread(GameView gv, SurfaceHolder surfaceholder) {
        this.gameView = gv;
        this.surfaceholder = surfaceholder;
    }

    public void run() {
        Canvas canvas = null;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        gameView.StartBGM();
        while (!stop) {
            try {
                HadDraw = false;
                if (gameView.BT_MODE) {
                    if (gameView.hadInitPlayer) {
                        if (gameView.hadInitBT) {
                            if (gameView.FrameBalancer < 1 || gameView.isGameEnd) {
                                timestampBefore = System.currentTimeMillis();
                                HadDraw = true;
                                canvas = surfaceholder.lockCanvas(null);
                                gameView.Draw(canvas);
                            }
                        }
                    } else {
                        gameView.InitBTGame();
                    }
                } else {
                    timestampBefore = System.currentTimeMillis();
                    HadDraw = true;
                    canvas = surfaceholder.lockCanvas(null);
                    gameView.Draw(canvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null && HadDraw) {
                    surfaceholder.unlockCanvasAndPost(canvas);
                }
            }
            try {
                if (HadDraw) {
                    timeDiff = System.currentTimeMillis() - timestampBefore;
                    sleep = Common.GAME_REFRESH - timeDiff;
                    if (sleep > 0) {
                        Thread.sleep(sleep);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}