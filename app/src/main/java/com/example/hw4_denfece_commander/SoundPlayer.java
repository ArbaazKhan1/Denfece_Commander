package com.example.hw4_denfece_commander;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SoundPlayer {
    private static final String TAG = "SoundPlayer";
    private static SoundPlayer instance;
    private SoundPool soundPool;
    private static final int MAX_STREAMS = 10;
    private HashSet<Integer> loaded = new HashSet<>();
    private HashSet<String> loopList = new HashSet<>();
    private HashMap<String, Integer> soundNameToResource = new HashMap<>();
    private int currentVolumeLevel;


    private SoundPlayer() {

        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(MAX_STREAMS);
        this.soundPool = builder.build();

        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.d(TAG, "onLoadComplete: #" + sampleId + "  " + status);
                loaded.add(sampleId);
            }
        });

    }

    static SoundPlayer getInstance() {
        if (instance == null)
            instance = new SoundPlayer();
        return instance;
    }

    void setupSound(Context context, String id, int resource, boolean loop) {
        int soundId = soundPool.load(context, resource, 1);
        soundNameToResource.put(id, soundId);
        if (loop)
            loopList.add(id);
        Log.d(TAG, "setupSound: " + id + ": #" + soundId);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    void start(final String id) {

        boolean playSounds = true;
        if (!playSounds)
            return;

        if (!loaded.contains(soundNameToResource.get(id))) {
            Log.d(TAG, "start: SOUND NOT LOADED: " + id);
            return;
        }

        int loop = 0;
        if (loopList.contains(id))
            loop = -1;

        int streamId = soundPool.play(soundNameToResource.get(id), 1f, 1f, 1, loop, 1f);
    }


    public void muteSound(Context context, boolean mute) {
        AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (mute) {
            currentVolumeLevel = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
            mgr.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        } else {
            mgr.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolumeLevel, 0);
        }

    }


    public void startBackgroundSounds(Context context) {
        MediaPlayer mp = MediaPlayer.create(context, R.raw.background);
        mp.setLooping(true);
        mp.start();
    }

}
