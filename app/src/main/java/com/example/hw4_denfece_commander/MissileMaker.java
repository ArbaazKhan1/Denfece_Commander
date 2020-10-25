package com.example.hw4_denfece_commander;

import android.animation.AnimatorSet;
import android.util.Log;

import java.util.ArrayList;

import static com.example.hw4_denfece_commander.Interceptor.INTERCEPTOR_BLAST;

public class MissileMaker implements Runnable {
    private static final String TAG = "MissleMaker";
    private MainActivity mainActivity;
    private boolean isRunning;
    private ArrayList<Missile> activeMissles = new ArrayList<>();
    private int screenWidth, screenHeight;
    private int count;
    private long delay = 4000;

    MissileMaker(MainActivity mainActivity, int screenWidth, int screenHeight) {
        this.mainActivity = mainActivity;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }
    @Override
    public void run() {
        setRunning(true);
        try {
            Thread.sleep((long) (delay * .5));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //This delay may change on level
        final long missileSpeed = 4000;
        while (isRunning) {
            try {

                int resId = R.drawable.missile;

                final Missile missile = new Missile(screenWidth, screenHeight, missileSpeed, mainActivity);
                activeMissles.add(missile);
                final AnimatorSet as = missile.setData(resId);

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: INCOMING MISSLES!");
                        SoundPlayer.getInstance().start("launch_missile");
                        count++;
                        if (count > 10){
                            Log.d(TAG, "run: Level UP!");
                            mainActivity.incrementLevel();
                            count= 0;
                            delay-=500;
                            if (delay<=0){
                                delay=100;
                            }
                        }
                        Log.d(TAG, "run: Count: "+count);
                        as.start();
                    }
                });
                Thread.sleep(getSleepTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private long getSleepTime() {
        if (Math.random() < .1){
            return 1;
        }
        else if (Math.random() < .2){
            return (long)  (delay * .5);
        }
        else {
            return delay;
        }
    }


    void setRunning(boolean running) {
        isRunning = running;
        ArrayList<Missile> temp = new ArrayList<>(activeMissles);
        for (Missile m : temp) {
            m.stop();
        }
    }

    void removeMissle(Missile m) {
        activeMissles.remove(m);
    }

    void applyInterceptorBlast(Interceptor interceptor, int id) {
        Log.d(TAG, "applyInterceptorBlast: -------------------------- " + id);

        float x1 = interceptor.getX();
        float y1 = interceptor.getY();

        Log.d(TAG, "applyInterceptorBlast: INTERCEPTOR: " + x1 + ", " + y1);

        ArrayList<Missile> nowGone = new ArrayList<>();
        ArrayList<Missile> temp = new ArrayList<>(activeMissles);

        for (Missile m : temp) {

            float x2 = (int) (m.getX() + (0.5 * m.getWidth()));
            float y2 = (int) (m.getY() + (0.5 * m.getHeight()));

            Log.d(TAG, "applyInterceptorBlast:    Missile: " + x2 + ", " + y2);


            float f = (float) Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
            Log.d(TAG, "applyInterceptorBlast:    DIST: " + f);

            if (f < INTERCEPTOR_BLAST) {

                SoundPlayer.getInstance().start("interceptor_hit_missile");
                mainActivity.incrementScore();
                Log.d(TAG, "applyInterceptorBlast:    Hit: " + f);
                m.interceptorBlast(x2, y2);
                nowGone.add(m);
            }

            Log.d(TAG, "applyInterceptorBlast: --------------------------");


        }

        for (Missile m : nowGone) {
            activeMissles.remove(m);
        }
        mainActivity.decrementInteceptorCount();
    }
}
