package com.example.musicplayer.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.musicplayer.Service.MusicService;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG ="MyBroadcastReceiver" ;
    private MusicService musicService=new MusicService();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"===MyBroadcastReceiver1");
        musicService.player.start();
        Log.d(TAG,"===MyBroadcastReceiver");

    }
}
