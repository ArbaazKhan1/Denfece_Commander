package com.example.hw4_denfece_commander;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static int screenHeight;
    public static int screenWidth;
    private ConstraintLayout layout;
    private ImageView base1;
    private ImageView base2;
    private ImageView base3;
    private ImageView launcher;
    private ImageView splash;
    private TextView points;
    private TextView level;
    private TextView leaderboard;
    private ArrayList<Base> baseList = new ArrayList<>();
    private int scoreValue=0;
    private int levelValue=1;
    private MissileMaker missileMaker;
    private Guideline region1;
    private Guideline region2;
    private Guideline midRegion;
    private Base firstBase;
    private Base secondBase;
    private Base thirdBase;
    private int interceptorCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = findViewById(R.id.layout);
        base1 = findViewById(R.id.Base1_ImageView);
        base2 = findViewById(R.id.Base2_ImageView);
        base3 = findViewById(R.id.Base3_ImageView);
        points = findViewById(R.id.Points_TextView);
        level = findViewById(R.id.Level_TextView);
        region1 = findViewById(R.id.Region1);
        region2 = findViewById(R.id.Region2);
        midRegion = findViewById(R.id.Mid_Guideline);
        splash = findViewById(R.id.SplashScreen_ImageView);
        leaderboard = findViewById(R.id.LeaderBoard_TextView);
        firstBase = new Base(this,base1);
        secondBase = new Base(this,base2);
        thirdBase = new Base(this,base3);
        baseList.add(firstBase);
        baseList.add(secondBase);
        baseList.add(thirdBase);
        setupFullScreen();
        getScreenDimensions();

        SoundPlayer.getInstance().setupSound(this, "background", R.raw.background, true);
        SoundPlayer.getInstance().setupSound(this, "base_blast", R.raw.base_blast, false);
        SoundPlayer.getInstance().setupSound(this, "interceptor_blast", R.raw.interceptor_blast, false);
        SoundPlayer.getInstance().setupSound(this, "interceptor_hit_missile", R.raw.interceptor_hit_missile, false);
        SoundPlayer.getInstance().setupSound(this, "launch_interceptor", R.raw.launch_interceptor, false);
        SoundPlayer.getInstance().setupSound(this, "launch_missile", R.raw.launch_missile, false);

        loadScreen();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void startGame() {
        ViewGroup groupLayout = layout;
        new ParallaxBackground(this, groupLayout, R.drawable.clouds, 4000, true);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    handleTouch(motionEvent.getX(), motionEvent.getY());
                }
                return false;
            }
        });
        SoundPlayer.getInstance().startBackgroundSounds(this);
        missileMaker = new MissileMaker(this, screenWidth, screenHeight);
        new Thread(missileMaker).start();
    }

    private void loadScreen() {
        SoundPlayer.getInstance().startBackgroundSounds(this);
        final ObjectAnimator alpha = ObjectAnimator.ofFloat(splash, "alpha", 0f, 1f);
        alpha.setInterpolator(new LinearInterpolator());
        alpha.setDuration(5000);
        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                splash.setVisibility(View.INVISIBLE);
                points.setVisibility(View.VISIBLE);
                level.setVisibility(View.VISIBLE);
                base1.setVisibility(View.VISIBLE);
                base2.setVisibility(View.VISIBLE);
                base3.setVisibility(View.VISIBLE);
                startGame();
            }
        });
        alpha.start();
    }


    private void endScreen() {
        splash.setImageResource(R.drawable.game_over);
        splash.setVisibility(View.VISIBLE);
        final ObjectAnimator alpha = ObjectAnimator.ofFloat(splash, "alpha", 0f, 1f);
        alpha.setInterpolator(new LinearInterpolator());
        alpha.setDuration(3000);
        alpha.start();

        try {
            Thread.sleep(5000);
        }catch (InterruptedException e){e.printStackTrace();}
       new DatabaseTopScoreAsyncTask(this).execute();
    }
    
    private void highScoreDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //THis is for a single line text
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        int maxLength = 3;
        et.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
        builder.setView(et);
        builder.setTitle("You are a Top Player!");
        builder.setMessage("Please enter you initials (up to 3 characters):");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (et.getText()==null){
                    return;
                }
                else{
                    new DatabaseAddScoreAsyncTask(MainActivity.this).execute("AAA",
                            String.format(Locale.getDefault(),"%d",scoreValue),
                            String.format(Locale.getDefault(),"%d",levelValue));
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "onClick: You do not want to input score");
                //startGame();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    

    protected void setLeaderboard(String s, List<Integer> scoreList){
        Log.d(TAG, "setLeaderboard:\n" +
                "#  Init    Level   Score   Date\n"+s);
        leaderboard.setText(s);
        if (s==null){
            new DatabaseAddScoreAsyncTask(this).execute("AAA",
                    String.format(Locale.getDefault(),"%d",scoreValue),
                    String.format(Locale.getDefault(),"%d",levelValue));
        }
        for (int i=0; i<scoreList.size();i++){
            if (scoreValue > scoreList.get(i)){
                highScoreDialog();
            }
        }
        leaderboard.setVisibility(View.VISIBLE);
        splash.setVisibility(View.INVISIBLE);
        points.setVisibility(View.INVISIBLE);
        level.setVisibility(View.INVISIBLE);
        base1.setVisibility(View.INVISIBLE);
        base2.setVisibility(View.INVISIBLE);
        base3.setVisibility(View.INVISIBLE);
    }

    protected void updateLeaderBoard(String s){
        Log.d(TAG, "updateLeaderBoard: "+s);
        leaderboard.setText(s);
        leaderboard.setVisibility(View.VISIBLE);
    }

    public ConstraintLayout getLayout() {
        return layout;
    }

    private void getScreenDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
    }

    private void setupFullScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void applyInterceptorBlast(Interceptor interceptor, int id) {
        missileMaker.applyInterceptorBlast(interceptor, id);
        ArrayList<Base> temp =  new ArrayList<>(baseList) ;
        for (Base b : temp) {
            Log.d(TAG, "applyInterceptorBlast: YOU ARE KILLING YOURSELF");
            b.applyInterceptorBlast(interceptor,id);
        }
    }

    public void applyMissileBlast (float x1, float y1) {
        ArrayList<Base> temp =  new ArrayList<>(baseList) ;
        for (Base b : temp) {
            float xleft = b.getX();
            float yleft = b.getY();

            float xright = (float) (xleft + (b.getWidth() * .5));
            float yright = (float) (yleft + (b.getY() * .5));

            if ((x1 > (xleft - 250)) && (x1 < (xright + 250))) {
                b.baseDestroyed();
                baseList.remove(b);
                if (baseList.isEmpty()){
                    missileMaker.setRunning(false);
                    endScreen();
                    Log.d(TAG, "onTouch: Game Over Bitch");
                }

            }
        }
    }
    // ScoreValue and levelValue are global variables
    public void incrementScore() {
        scoreValue++;
        points.setText(String.format(Locale.getDefault(), "%d", scoreValue));
    }

    public int getScore() {
        return scoreValue;
    }
    public void incrementLevel() {
        levelValue++;
        level.setText(String.format(Locale.getDefault(), "Level %d",levelValue));
    }
    public int getLevelValue() {
        return levelValue;
    }

    public void removeMissile(Missile p) {
        missileMaker.removeMissle(p);
    }

    public void inrementInteceptorCount() {
        interceptorCount++;
    }
    public void decrementInteceptorCount() {
        interceptorCount--;
    }


    public void handleTouch(float x1, float y1) {
        if (interceptorCount >= 3){ //limits the number of interceptors to 3
            return;
        }
        if(baseList.size() == 3) {
            if (x1 < region1.getX()) {
                Log.d(TAG, "handleTouch: Base1 Fire!");
                launcher = base1;
            } else if (x1 < region2.getX()) {
                Log.d(TAG, "handleTouch: Base2 Fire!");
                launcher = base2;
            } else {
                Log.d(TAG, "handleTouch: Base3 Fire!");
                launcher = base3;
            }
        }
        else if(baseList.size() == 2) {
            if(baseList.contains(firstBase) && baseList.contains(secondBase)) {
                if(x1 < region1.getX()) {
                    Log.d(TAG, "handleTouch: There is only 2 of us left! Base1 Fire!");
                    launcher = base1;
                }
                else {
                    Log.d(TAG, "handleTouch: There is only 2 of us left! Base2 Fire!");
                    launcher = base2;
                }
            }
            else if(baseList.contains(firstBase) && baseList.contains(thirdBase)) {
                if(x1 < midRegion.getX()) {
                    Log.d(TAG, "handleTouch: There is only 2 of us left! Base1 Fire!");
                    launcher = base1;
                }
                else {
                    Log.d(TAG, "handleTouch: There is only 2 of us left! Base3 Fire!");
                    launcher = base3;
                }
            }
            else if(baseList.contains(secondBase) && baseList.contains(thirdBase)) {
                if(x1 < region2.getX()) {
                    Log.d(TAG, "handleTouch: There is only 2 of us left! Base2 Fire!");
                    launcher = base2;
                }
                else {
                    Log.d(TAG, "handleTouch: There is only 2 of us left! Base3 Fire!");
                    launcher = base3;
                }
            }
        }
        else if(baseList.size() == 1) {
            if(baseList.contains(firstBase)) {
                Log.d(TAG, "handleTouch: Were all that's left! Base1 Fire!");
                launcher = base1;
            }
            else if(baseList.contains(secondBase)) {
                Log.d(TAG, "handleTouch: Were all that's left! Base2 Fire!");
                launcher = base2;
            }
            else if (baseList.contains(thirdBase)){
                Log.d(TAG, "handleTouch: Were all that's left! Base3 Fire!");
                launcher = base3;
            }
        }
            double startX = launcher.getX() + (0.5 * launcher.getWidth());
            double startY = launcher.getY() + (0.5 * launcher.getHeight());

            Interceptor i = new Interceptor(this,  (float) (startX - 10), (float) (startY - 30), x1, y1);
            SoundPlayer.getInstance().start("launch_interceptor");
            i.launch();
            inrementInteceptorCount();
    }

}
