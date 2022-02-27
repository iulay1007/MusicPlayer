package com.example.musicplayer.Service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.musicplayer.EventBus.MyEventBus;
import com.example.musicplayer.MainActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.musicplayer.MainActivity.musicBeanList;

public class MusicService extends Service {
    public Notification notification;
    public MediaPlayer player = new MediaPlayer();
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
            player = new MediaPlayer();
        try {
            if (musicBeanList != null)
                player.setDataSource(musicBeanList.get(position).getData());
            //准备资源
            player.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("服务", "准备播放音乐");
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void Event(MyEventBus myEventBus) {
        position = myEventBus.getPosition();
    }

    public void createNotification() {
        notification = new Notification();
    }

    //该方法包含关于歌曲的操作
    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }

        //判断是否处于播放状态
        public boolean isPlaying() {
            return player.isPlaying();
        }
        //播放或暂停歌曲
        public void play() throws IOException {
            player.pause();
            player = new MediaPlayer();
            player.setDataSource(musicBeanList.get(position).getData());
            player.prepare();
            player.start();

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //实例化一个Message对象
                    Message msg = Message.obtain();
                    //Message对象的arg1参数携带音乐当前播放进度信息，类型是int
                    msg.arg1 = player.getCurrentPosition();
                    msg.arg2 = player.getDuration();
                    //使用MainActivity中的handler发送信息
                    if (player.isPlaying())
                        MainActivity.handler.sendMessage(msg);
                }
            }, 0, 50);
        }

        public void playInMain() {

            if (player.isPlaying()) {
                player.pause();
            } else {
                player.start();
            }
        }

        public void seek(int time){
            player.seekTo(time);
        }

        public void playNext() throws IOException {
            player.pause();
            player = new MediaPlayer();
            position++;
            EventBus.getDefault().postSticky(new MyEventBus(position));
            player.setDataSource(musicBeanList.get(position).getData());
            player.prepare();
            player.start();

        }

        public void playPrv() throws IOException {
            player.pause();
            player = new MediaPlayer();
            if (position != 0)
                position--;
            EventBus.getDefault().postSticky(new MyEventBus(position));
            player.setDataSource(musicBeanList.get(position).getData());
            player.prepare();
            player.start();

        }


        //返回歌曲的长度，单位为毫秒
        public int getDuration() {
            return player.getDuration();
        }

        //返回歌曲目前的进度，单位为毫秒
        public int getCurrentPostion() {
            return player.getCurrentPosition();
        }

        //设置歌曲播放的进度，单位为毫秒
        public void seekTo(int mesc) {
            player.seekTo(mesc);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}