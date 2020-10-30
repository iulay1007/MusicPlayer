package com.example.musicplayer;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.musicplayer.Bean.MusicBean;
import com.example.musicplayer.EventBus.MyEventBus;
import com.example.musicplayer.Service.MusicService;
import com.example.musicplayer.utils.NotificationsUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static int PERMISSION_REQUEST_CODE = 1;
    public static List<MusicBean> musicBeanList = new ArrayList<>();
    private static String TAG = "MainActivity";
    private MediaPlayer player;
    private ServiceConnection conn;
    private MusicService musicService;
    private MusicService.MyBinder myBinder;
    public static List<MusicBean> musicBeans = new ArrayList<>();
    private IntentFilter intentFilter;
    private IntentFilter intentFilternext;
    private IntentFilter intentFilterprv;
    public TextView textView;
    private Button play_btn;
    private Button change_btn;
    private Button playprv_btn;
    private Button playnext_btn;
    private Button ntf_playn_btn;
    private Button ntf_playp_btn;
    private Button getNtf_playn_btn;

    private final static int SCAN_OK = 1;
    private final static int getintent = 2;
    private int position;
    private RemoteViews remoteViews;
    private PlayorPauseReceiver playorPauseReceiver;
    private PlayNextReceiver playNextReceiver;
    private PlayPrvReceiver playPrvReceiver;


    @RequiresApi(api = Build.VERSION_CODES.O)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        checkPermission();
        isNotifyEnabled(this);
        initSong();
        initPlayer();
        intentFilter = new IntentFilter();
        intentFilternext=new IntentFilter();
        intentFilterprv=new IntentFilter();

        intentFilter.addAction("playorPauseReceiver.broadcast.receiver");
        intentFilternext.addAction("playNextReceiver.broadcast.receiver");
        intentFilterprv.addAction("playPrvReceiver.broadcast.receiver");
        playorPauseReceiver = new PlayorPauseReceiver();
        playNextReceiver=new PlayNextReceiver();
        playPrvReceiver=new PlayPrvReceiver();

        //绑定监听
        registerReceiver(playorPauseReceiver,intentFilter);
        registerReceiver(playNextReceiver,intentFilternext);
        registerReceiver(playPrvReceiver,intentFilterprv);
        showNotification();
        EventBus.getDefault().register(this);

    }


    private void initView() {

        textView = (TextView) findViewById(R.id.tv_music_name);
        playprv_btn = (Button) findViewById(R.id.playprv_btn);
        playnext_btn = (Button) findViewById(R.id.playnext_btn);
        play_btn = (Button) findViewById(R.id.play_btn);
        change_btn = (Button) findViewById(R.id.change_btn);
        play_btn.setOnClickListener(this);
        playnext_btn.setOnClickListener(this);
        playprv_btn.setOnClickListener(this);
        change_btn.setOnClickListener(this);


    }

    //接受event后执行
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void Event(MyEventBus myEventBus) {

        position = myEventBus.getPosition();
        textView.setText(musicBeanList.get(position).getName() + "");


    }

    //通知栏
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotification() {
        String channelId = "default";
        String channelName = "默认通知";
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
        NotificationChannel channel = new NotificationChannel(
                getPackageName(),
                "会话消息()",
                NotificationManager.IMPORTANCE_DEFAULT

        );
        notificationManager.createNotificationChannel
                (new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH));

        notificationManager.createNotificationChannel(channel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder
                (this, channelId).setSmallIcon(R.drawable.ic_baseline_favorite_24)
                .setContentTitle("title").setContentText("Content").setChannelId(getPackageName()).
                        setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setContent(remoteViews).setSmallIcon(R.drawable.ic_baseline_favorite_24);
            Intent intent=new Intent(this,MainActivity.class);
        initRemoteViews();
      ////
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,0);



        notificationManager.notify(1, builder.build());

    }
    private void initRemoteViews() {
        //intentFilter = new IntentFilter();
        //intentFilter.addAction("com.sample.test.sticky.broadcast.receiver");
        Intent intent=new Intent("playorPauseReceiver.broadcast.receiver");
        Intent intentnext=new Intent("playNextReceiver.broadcast.receiver");
        Intent intentprv=new Intent("playPrvReceiver.broadcast.receiver");


        PendingIntent pendButtonplayIntent = PendingIntent.getBroadcast(this, 0, intent,0);
        PendingIntent pendButtonnextIntent= PendingIntent.getBroadcast(this, 1, intentnext,0);
        PendingIntent pendButtonprvIntent= PendingIntent.getBroadcast(this, 2, intentprv,0);

        // sendStickyBroadcast(intent);
        remoteViews.setOnClickPendingIntent(R.id.ntf_play_m_btn, pendButtonplayIntent);
        remoteViews.setOnClickPendingIntent(R.id.ntf_play_next_btn,pendButtonnextIntent);
        remoteViews.setOnClickPendingIntent(R.id.ntf_btn_left,pendButtonprvIntent);

    }

    //通知栏权限??
    public boolean isNotifyEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return isEnableV26(context);//下面
        }
        return false;
    }

    private static boolean isEnableV26(Context context) {
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        try {
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            Method sServiceField = notificationManager.getClass().getDeclaredMethod("getService");
            sServiceField.setAccessible(true);
            Object sService = sServiceField.invoke(notificationManager);

            Method method = sService.getClass().getDeclaredMethod("areNotificationsEnabledForPackage"
                    , String.class, Integer.TYPE);
            method.setAccessible(true);
            return (boolean) method.invoke(sService, pkg, uid);
        } catch (Exception e) {
            return true;
        }
    }


    private void initPlayer() {


        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                // musicService=((MusicService.MyBinder)(iBinder)).getService();
                myBinder = (MusicService.MyBinder) iBinder;

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                musicService = null;
            }
        };

        //开启服务
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        startService(intent);
        bindService(intent, conn, BIND_AUTO_CREATE);


    }
    //ContentProvider搜索歌曲
    private void initSong() {
        new Thread(new Runnable() {
            @Override
            public void run() {


                ContentResolver contentResolver = MainActivity.this.getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

                Cursor mCursor = contentResolver.query(uri, new String[]{MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME}, null, null, "date_added DESC");
                String[] columnNames = mCursor.getColumnNames();
                if (mCursor == null) return;
                while (mCursor.moveToNext()) {
                    String path = mCursor.getString(0);
                    String name = mCursor.getString(1);
                    MusicBean musicBean = new MusicBean(path, name);
                    musicBeanList.add(musicBean);
                    for (MusicBean musicBean1 : musicBeanList) {

                        Log.d(TAG, "===" + musicBean1);
                    }


                }
                Log.d(TAG, "qwq");
                mCursor.close();

            }
        }).start();
    }
    //检查权限
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {
        if (!NotificationsUtils.isNotificationEnabled(this)) {
            NotificationsUtils.openPush(this);
        }
        int readExternalStoragePermissionResult = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (readExternalStoragePermissionResult != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            } }
    }
//点击事件
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //播放/暂停
            case R.id.play_btn:
                if(!myBinder.isPlaying())
               play_btn.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24);
                else
                    play_btn.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24);


                if (myBinder != null) {
                    myBinder.playinmain();
                }
                break;
             //播放下一曲
            case R.id.playnext_btn:
                try {
                    if (myBinder != null)
                        myBinder.playnext();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            //播放前一曲
            case R.id.playprv_btn:
                try {
                    if (myBinder != null)
                        myBinder.playprv();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
             //跳转到列表界面
            case R.id.change_btn:
                Intent intent1 = new Intent(MainActivity.this, MainActivity2.class);
                startActivity(intent1);
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    //注销eventbus
        EventBus.getDefault().unregister(this);
        unregisterReceiver(playorPauseReceiver);

    }
    class PlayorPauseReceiver extends BroadcastReceiver {
        //实现接收到广播的处理
        @Override
        public void onReceive(Context context, Intent intent) {
            if (myBinder != null) {
            myBinder.playinmain();
        }
            Log.d(TAG,"===NetworkChangeReceiver");
        }
    }
    class PlayNextReceiver extends BroadcastReceiver {
        //实现接收到广播的处理
        @Override
        public void onReceive(Context context, Intent intent) {
            if (myBinder != null) {
                try {
                    myBinder.playnext();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG,"===NetworkChangeReceiver");
        }
    }
    class PlayPrvReceiver extends BroadcastReceiver {
        //实现接收到广播的处理
        @Override
        public void onReceive(Context context, Intent intent) {
            if (myBinder != null) {
                try {
                    myBinder.playprv();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG,"===NetworkChangeReceiver");
        }
    }
}
