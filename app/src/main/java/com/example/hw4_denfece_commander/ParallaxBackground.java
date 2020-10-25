package com.example.hw4_denfece_commander;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import static com.example.hw4_denfece_commander.MainActivity.screenHeight;
import static com.example.hw4_denfece_commander.MainActivity.screenWidth;

public class ParallaxBackground implements Runnable {

    private Context context;
    private ViewGroup layout;
    private ImageView backImageA;
    private ImageView backImageB;
    private long duration;
    private int resId;
    boolean isRunning;
    private static final String TAG = "ParallaxBackground";


    ParallaxBackground(Context context, ViewGroup layout, int resId, long duration, boolean isRunning) {

        this.context = context;
        this.layout = layout;
        this.resId = resId;
        this.duration = duration;
        this.isRunning = isRunning;
        setupBackground();

    }

    private void setupBackground() {
        backImageA = new ImageView(context);
        backImageB = new ImageView(context);

        LinearLayout.LayoutParams params = new LinearLayout
                .LayoutParams(screenWidth + getBarHeight(), screenHeight);
        backImageA.setLayoutParams(params);
        backImageB.setLayoutParams(params);

        layout.addView(backImageA);
        layout.addView(backImageB);

        Bitmap backBitmapA = BitmapFactory.decodeResource(context.getResources(), resId);
        Bitmap backBitmapB = BitmapFactory.decodeResource(context.getResources(), resId);

        backImageA.setImageBitmap(backBitmapA);
        backImageB.setImageBitmap(backBitmapB);

        backImageA.setScaleType(ImageView.ScaleType.FIT_XY);
        backImageB.setScaleType(ImageView.ScaleType.FIT_XY);

        backImageA.setZ(-1);
        backImageB.setZ(-1);

        //This will set the clouds to fade in and out
        int  alaphA = 25 + (int)(Math.random() *75);
        int  alaphB = 25 + (int)(Math.random() *75);
        backImageA.setImageAlpha(alaphA);
        backImageB.setImageAlpha(alaphB);
        animateBack();


    }

    @Override
    public void run() {


        backImageA.setX(0);
        backImageB.setX(-(screenWidth + getBarHeight()));
        double cycleTime = 25.0;

        double cycles = duration / cycleTime;
        double distance = (screenWidth + getBarHeight()) / cycles;


        while (isRunning) {
            long start = System.currentTimeMillis();

            double aX = backImageA.getX() - distance;
            backImageA.setX((float) aX);
            double bX = backImageB.getX() - distance;
            backImageB.setX((float) bX);

            long workTime = System.currentTimeMillis() - start;

            if (backImageA.getX() < -(screenWidth + getBarHeight()))
                backImageA.setX((screenWidth + getBarHeight()));

            if (backImageB.getX() < -(screenWidth + getBarHeight()))
                backImageB.setX((screenWidth + getBarHeight()));

            long sleepTime = (long) (cycleTime - workTime);

            if (sleepTime <= 0) {
                Log.d(TAG, "run: NOT KEEPING UP! " + sleepTime);
                continue;
            }

            try {
                Thread.sleep((long) (cycleTime - workTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }

    }

    private void animateBack() {

        ValueAnimator animator = ValueAnimator.ofFloat(0.0f,1.0f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(duration);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                final float progress = (float) animation.getAnimatedValue();
                float width = screenWidth + getBarHeight();

                float a_translationX = width * progress;
                float b_translationX = width * progress - width;

                backImageA.setTranslationX(a_translationX);
                backImageB.setTranslationX(b_translationX);


                //Log.d(TAG, "onAnimationUpdate: A " + translationX + "   B " + (translationX - width));
                //Log.d(TAG, "onAnimationUpdate: A " + backImageA.getY() + "   B " + backImageB.getY());

            }
        });
        animator.start();
    }


    private int getBarHeight() {
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }
}
