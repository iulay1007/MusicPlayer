package com.example.musicplayer.Service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.security.Provider;

import static com.example.musicplayer.MainActivity.musicBeanList;

public class MusicService extends Service {
    public MediaPlayer player=new MediaPlayer();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }


    @Override
    public void onCreate() {
        super.onCreate();

        if (player == null)
            //如果为空就new一个
            player = new MediaPlayer();
        //这里只执行一次，用于准备播放器
     //   player = new MediaPlayer();

        try {
            player.setDataSource("/storage/emulated/0/Huawei/CloudClone/SDCardClone/音乐/에이핑크 (Apink) - U You (Korean Ver.) [mqms2].mp3");
            //准备资源
            player.prepare();



       //    player.start();
            Log.d("qwq","start");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("服务", "准备播放音乐");
    }

    //该方法包含关于歌曲的操作
    public class MyBinder extends Binder {
        public MusicService getService(){

            return MusicService.this;

        }

        //判断是否处于播放状态
        public boolean isPlaying(){
            return player.isPlaying();
        }

        //播放或暂停歌曲
        public void play() {
            if (!player.isPlaying()) {
                player.start();
            } else {
                player.pause();
            }
            Log.e("服务", "播放音乐");
        }

       /* public void playMusic() {
            if(player==null)
                player = new MediaPlayer();

            if (!player.isPlaying()&&player!=null) {
                //如果还没开始播放，就开始
                player.start();
            }
        }*/

        //返回歌曲的长度，单位为毫秒
        public int getDuration(){
            return player.getDuration();
        }

        //返回歌曲目前的进度，单位为毫秒
        public int getCurrenPostion(){
            return player.getCurrentPosition();
        }

        //设置歌曲播放的进度，单位为毫秒
        public void seekTo(int mesc){
            player.seekTo(mesc);
        }
    }
}