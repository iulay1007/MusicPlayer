package com.example.musicplayer.Service;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;

import static com.example.musicplayer.Adapter.RecyclerviewAdapter.position_song;
import static com.example.musicplayer.MainActivity.musicBeanList;

public class MusicService extends Service {
    public Notification notification;
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


           // player.setDataSource("/storage/emulated/0/Huawei/CloudClone/SDCardClone/音乐/에이핑크 (Apink) - U You (Korean Ver.) [mqms2].mp3");
           player.setDataSource(musicBeanList.get(position_song).getData());

            //准备资源
            player.prepare();



     //   player.start();
            Log.d("qwq","start");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("服务", "准备播放音乐");
    }


    public void creatNotification(){
notification=new Notification();


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
        public void play() throws IOException {
          //  if (!player.isPlaying()) {
                player.pause();
                player = new MediaPlayer();
                player.setDataSource(musicBeanList.get(position_song).getData());
                player.prepare();


              player.start();
                //   } else {
                //      player.pause();
                //    }
                //Log.e("服务", "播放音乐");
            //}
        }

        public void playinmain(){
            position_song++;
            Intent intent=new Intent("action.playmusic");
            intent.putExtra("position",position_song);
            //intent.setClassName(new ComponentName("com.example.musicplayer","MainActivity$BroadcastReceiverinMain"));
            intent.setComponent(new ComponentName("com.example.musicplayer","com.example.musicplayerB.roadcastReceiverinMain"));
            sendBroadcast(intent);
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.start();
            }
        }
        public void playnext() throws IOException{
            player.pause();
            player=new MediaPlayer();
            position_song++;
            player.setDataSource(musicBeanList.get(position_song).getData());
            player.prepare();


            player.start();

        }
        public void playprv() throws IOException{
            player.pause();
            player=new MediaPlayer();
            if(position_song!=0)
            position_song--;
            player.setDataSource(musicBeanList.get(position_song).getData());
            player.prepare();


            player.start();

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