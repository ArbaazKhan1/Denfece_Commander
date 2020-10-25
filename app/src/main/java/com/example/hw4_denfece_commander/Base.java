package com.example.hw4_denfece_commander;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.ArrayList;

import static com.example.hw4_denfece_commander.Interceptor.INTERCEPTOR_BLAST;

public class Base {
    private static final String TAG = "Base";
    private ImageView baseView;
    private MainActivity mainActivity;
    private ImageView imageView;
    public Base(final MainActivity mainActivity, ImageView baseView) {
        this.baseView = baseView;
        this.mainActivity=mainActivity;


    }

    //getX and getY return the coordanates of the top left corner
    public float getX() {
        Log.d(TAG, "getX: "+baseView.getX());
        return baseView.getX();
    }
    public float getY() {
        Log.d(TAG, "getY: "+baseView.getY());
        return baseView.getY();
    }
    public float getWidth() {
        Log.d(TAG, "getWidth: "+baseView.getWidth());
        return baseView.getWidth();
    }
    public float getHeight() {
        Log.d(TAG, "getHeight: "+baseView.getHeight());
        return baseView.getHeight();
    }
    public ImageView getBaseView() {
        return baseView;
    }
    public void setBaseView(ImageView baseView) {
        this.baseView = baseView;
    }

    public void baseDestroyed(){
        Log.d(TAG, "baseDestroyed: ");
        SoundPlayer.getInstance().start("base_blast");

        baseView.setImageResource(R.drawable.blast);

        final ObjectAnimator alpha = ObjectAnimator.ofFloat(baseView, "alpha", 0.0f);
        alpha.setInterpolator(new LinearInterpolator());
        alpha.setDuration(3000);
        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainActivity.getLayout().removeView(baseView);
                mainActivity.getLayout().addView(baseView);
            }
        });
        alpha.start();
    }


    void applyInterceptorBlast(Interceptor interceptor, int id) {
    Log.d(TAG, "applyInterceptorBlast: -------------------------- " + id);

    float x1 = interceptor.getX();
    float y1 = interceptor.getY();

    Log.d(TAG, "applyInterceptorBlast: INTERCEPTOR: " + x1 + ", " + y1);

    float x2 = (int) (getX() + (0.5 * getWidth()));
    float y2 = (int) (getY() + (0.5 * getHeight()));

    Log.d(TAG, "applyInterceptorBlast:    Missile: " + x2 + ", " + y2);

    float f = (float) Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    Log.d(TAG, "applyInterceptorBlast:    DIST: " + f);

    if (f < INTERCEPTOR_BLAST) {

        SoundPlayer.getInstance().start("interceptor_hit_missile");
        Log.d(TAG, "applyInterceptorBlast:    Hit: " + f);
        mainActivity.applyMissileBlast(x2,y2);

    }

    Log.d(TAG, "applyInterceptorBlast: --------------------------");


    }
}
