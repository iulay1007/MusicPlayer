package com.example.musicplayer.Service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.musicplayer.EventBus.MyEventBus;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import static com.example.musicplayer.MainActivity.musicBeanList;

public class MusicService extends Service {
    public Notification notification;
    public MediaPlayer player=new MediaPlayer();
    private int position;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.getDefault().register(this);
        if (player == null)
            //如果为空就new一个
                player = new MediaPlayer();
        //这里只执行一次，用于准备播放器
     //   player = new MediaPlayer();

        try {


           // player.setDataSource("/storage/emulated/0/Huawei/CloudClone/SDCardClone/音乐/에이핑크 (Apink) - U You (Korean Ver.) [mqms2].mp3");
          if(musicBeanList!=null)
           player.setDataSource(musicBeanList.get(position).getData());

            //准备资源
            player.prepare();



     //   player.start();
            Log.d("qwq","start");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("服务", "准备播放音乐");
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void Event(MyEventBus myEventBus){

        position=myEventBus.getPosition();

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
                player.setDataSource(musicBeanList.get(position).getData());
             player.prepare();


              player.start();
                //   } else {
                //      player.pause();
                //    }
                //Log.e("服务", "播放音乐");
            //}
        }

        public void playinmain(){

            if (player.isPlaying()) {
                player.pause();
            } else {
                player.start();
            }
        }
        public void playnext() throws IOException{
            player.pause();
            player=new MediaPlayer();
            position++;
            EventBus.getDefault().postSticky(new MyEventBus(position));
            player.setDataSource(musicBeanList.get(position).getData());
            player.prepare();
            player.start();

        }
        public void playprv() throws IOException{
            player.pause();
            player=new MediaPlayer();
            if(position!=0)
            position--;
            EventBus.getDefault().postSticky(new MyEventBus(position));
            player.setDataSource(musicBeanList.get(position).getData());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}