package com.example.hw4_denfece_commander;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

public class Missile {
    private static final String TAG = "Missle";
    private boolean hit = false;
    private MainActivity mainActivity;
    private ImageView imageView;
    private AnimatorSet aSet = new AnimatorSet();
    private int screenHeight;
    private int screenWidth;
    private long screenTime;

    Missile(int screenWidth, int screenHeight, long screenTime, final MainActivity mainActivity) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.screenTime = screenTime;
        this.mainActivity = mainActivity;
        imageView = new ImageView(mainActivity);
        imageView.setImageResource(R.drawable.missile);
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.getLayout().addView(imageView);
            }
        });
    }

    AnimatorSet setData(final int drawId) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageResource(drawId);
            }
        });

        int startX = (int) (Math.random() * screenWidth);
        final int endX = (int) (Math.random() * screenWidth);

        int startY = -100;
        final int endY = screenHeight;

        /*
        Subtract half the missile ImageView s intrinsic width:
        (imageview.getDrawable(). getIntrinsicWidth())
​
        from the start X and start Y
         */
        startX -= imageView.getDrawable().getIntrinsicWidth() * .5;
        startY -= imageView.getDrawable().getIntrinsicWidth() * .5;

        Float rAngle = calculateAngle(startX,startY,endX,endY);

        imageView.setX(startX);
        imageView.setY(startY);
        imageView.setZ(-10);
        imageView.setRotation(rAngle);

        final ObjectAnimator yAnim = ObjectAnimator.ofFloat(imageView, "y", startY, endY);
        yAnim.setInterpolator(new LinearInterpolator());
        yAnim.setDuration(screenTime);

        final ObjectAnimator xAnim = ObjectAnimator.ofFloat(imageView, "x", startX,endX);
        xAnim.setInterpolator(new LinearInterpolator());
        xAnim.setDuration(screenTime);
        xAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //If the Missle passes through the barrier past the bases it triggers a ground explosion
                if (getY() > screenHeight * 0.85) {
                    Log.d(TAG, "onAnimationUpdate: ANIMATION CANCELLED");
                    yAnim.cancel();
                    xAnim.cancel();
                    makeGroundBlast(endX,endY);
                }
            }
        });

        aSet.playTogether(xAnim, yAnim);
        return aSet;
    }
    void stop() {
        aSet.cancel();
    }

    float getX() {
        return imageView.getX();
    }

    float getY() {
        return imageView.getY();
    }

    float getWidth() {
        return imageView.getWidth();
    }

    float getHeight() {
        return imageView.getHeight();
    }

    void makeGroundBlast(float x, float y) {
        Log.d(TAG, "makeGroundBlast: GROUNDBLAST!!");
        mainActivity.removeMissile(Missile.this);

        final ImageView iv = new ImageView(mainActivity);
        iv.setImageResource(R.drawable.explode);

        iv.setTransitionName("GROUNDBLAST!");

        int w = imageView.getDrawable().getIntrinsicWidth();
        int offset = (int) (w * 0.5);

        iv.setX(x - offset);
        iv.setY(y - offset);
        iv.setRotation((float) (360.0 * Math.random()));

        iv.setZ(-15);

        aSet.cancel();

        mainActivity.getLayout().removeView(imageView);
        mainActivity.getLayout().addView(iv);

        final ObjectAnimator alpha = ObjectAnimator.ofFloat(iv, "alpha", 0.0f);
        alpha.setInterpolator(new LinearInterpolator());
        alpha.setDuration(3000);
        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainActivity.getLayout().removeView(iv);
            }
        });
        alpha.start();

        mainActivity.applyMissileBlast(iv.getX(), iv.getY());
    }

    private float calculateAngle(double x1, double y1, double x2, double y2) {
        double angle = Math.toDegrees(Math.atan2(x2 - x1, y2 - y1));
        // Keep angle between 0 and 360
        angle = angle + Math.ceil(-angle / 360) * 360;
        return (float) (180.0f - angle);

    }

    void interceptorBlast(float x, float y) {

        final ImageView iv = new ImageView(mainActivity);
        iv.setImageResource(R.drawable.explode);

        iv.setTransitionName("Missile Intercepted Blast");

        int w = imageView.getDrawable().getIntrinsicWidth();
        int offset = (int) (w * 0.5);

        iv.setX(x - offset);
        iv.setY(y - offset);
        iv.setRotation((float) (360.0 * Math.random()));

        aSet.cancel();

        mainActivity.getLayout().removeView(imageView);
        mainActivity.getLayout().addView(iv);

        final ObjectAnimator alpha = ObjectAnimator.ofFloat(iv, "alpha", 0.0f);
        alpha.setInterpolator(new LinearInterpolator());
        alpha.setDuration(3000);
        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainActivity.getLayout().removeView(imageView);
            }
        });
        alpha.start();
    }
}
