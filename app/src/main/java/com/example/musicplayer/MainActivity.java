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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.SeekBar;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static int PERMISSION_REQUEST_CODE = 1;
    public static List<MusicBean> musicBeanList = new ArrayList<>();
    private static String TAG = "MainActivity";
    private MusicService.MyBinder myBinder;
    public TextView textView;
    private Button playBtn;

    private static SeekBar seekBar;
    private RemoteViews remoteViews;
    private PlayorPauseReceiver playorPauseReceiver;
    private PlayNextReceiver playNextReceiver;
    private PlayPrvReceiver playPrvReceiver;
    private NotificationManager notificationManager = null;
    private NotificationCompat.Builder builder = null;

    public static Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            seekBar.setMax(msg.arg2);
            seekBar.setProgress(msg.arg1);
            Log.d("handleMessageL", msg.arg2 + " " + new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date(msg.arg2)));
            Log.d("handleMessage", msg.arg1 + " " + new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date(msg.arg1)));
            return false;
        }
    });

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
        initBroadCast();
        showNotification();
        EventBus.getDefault().register(this);
    }

    private void initBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        IntentFilter intentFilterNext = new IntentFilter();
        IntentFilter intentFilterPrv = new IntentFilter();
        intentFilter.addAction("playorPauseReceiver.broadcast.receiver");
        intentFilterNext.addAction("playNextReceiver.broadcast.receiver");
        intentFilterPrv.addAction("playPrvReceiver.broadcast.receiver");
        playorPauseReceiver = new PlayorPauseReceiver();
        playNextReceiver = new PlayNextReceiver();
        playPrvReceiver = new PlayPrvReceiver();

        //绑定监听
        registerReceiver(playorPauseReceiver, intentFilter);
        registerReceiver(playNextReceiver, intentFilterNext);
        registerReceiver(playPrvReceiver, intentFilterPrv);
    }


    private void initView() {
        textView = findViewById(R.id.tv_music_name);
        Button playPrvBtn = findViewById(R.id.playprv_btn);
        Button playNextBtn = findViewById(R.id.playnext_btn);
        playBtn = findViewById(R.id.play_btn);
        Button changeBtn = findViewById(R.id.change_btn);
        seekBar = findViewById(R.id.seekbar);
        playBtn.setOnClickListener(this);
        playNextBtn.setOnClickListener(this);
        playPrvBtn.setOnClickListener(this);
        changeBtn.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (myBinder != null)
                    myBinder.seek(seekBar.getProgress());//在当前位置播放
            }
        });

    }

    //接受event后执行
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void Event(MyEventBus myEventBus) {

        int position = myEventBus.getPosition();
        textView.setText(musicBeanList.get(position).getName());
        Log.d("Event", "before" + musicBeanList.get(position).getName());

        if (notificationManager != null) {
            Log.d("Event", musicBeanList.get(position).getName());
            remoteViews.setTextViewText(R.id.ntf_tv, musicBeanList.get(position).getName());
            synchronized (notificationManager) {
                notificationManager.notify(1, builder.build());
            }
        }
        Log.d("Event", myBinder.isPlaying() + "");
        if (myBinder != null) {
            if (!myBinder.isPlaying())
                playBtn.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24);
            else
                playBtn.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24);
        }

    }

    //通知栏
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotification() {
        String channelId = "default";
        String channelName = "默认通知";
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
        NotificationChannel channel = new NotificationChannel(
                getPackageName(),
                "会话消息()",
                NotificationManager.IMPORTANCE_DEFAULT

        );
        Intent intent = new Intent(this, MainActivity.class);
        IntentFilter intentFilterGetMusicName = new IntentFilter();
        intentFilterGetMusicName.addAction("intentFiltergetMusicName.broadcast.receiver");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 3, intent, 0);

        notificationManager.createNotificationChannel
                (new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH));
        notificationManager.createNotificationChannel(channel);
        builder = new NotificationCompat.Builder
                (this, channelId).setSmallIcon(R.drawable.ic_baseline_favorite_24)
                .setContentTitle("title").setContentText("Content").setChannelId(getPackageName()).
                        setPriority(NotificationCompat.PRIORITY_HIGH).setContentIntent(pendingIntent);

        builder.setContent(remoteViews).setSmallIcon(R.drawable.ic_baseline_favorite_24);
        remoteViews.setOnClickPendingIntent(R.id.ntf_layout, pendingIntent);
        initRemoteViews();
        notificationManager.notify(1, builder.build());
    }

    private void initRemoteViews() {
        Intent intent = new Intent("playorPauseReceiver.broadcast.receiver");
        Intent intentNext = new Intent("playNextReceiver.broadcast.receiver");
        Intent intentPrv = new Intent("playPrvReceiver.broadcast.receiver");
        PendingIntent pendButtonPlayIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        PendingIntent pendButtonNextIntent = PendingIntent.getBroadcast(this, 1, intentNext, 0);
        PendingIntent pendButtonPrvIntent = PendingIntent.getBroadcast(this, 2, intentPrv, 0);
        remoteViews.setOnClickPendingIntent(R.id.ntf_play_m_btn, pendButtonPlayIntent);
        remoteViews.setOnClickPendingIntent(R.id.ntf_play_next_btn, pendButtonNextIntent);
        remoteViews.setOnClickPendingIntent(R.id.ntf_btn_left, pendButtonPrvIntent);

    }

    //通知栏权限
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


        ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                myBinder = (MusicService.MyBinder) iBinder;

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
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
        }
    }

    //点击事件
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //播放/暂停
            case R.id.play_btn:
                Intent i = new Intent();
                i.setAction("intentFiltergetMusicName.broadcast.receiver");
                i.putExtra("info", "sticky broadcast has been receiver");
                sendStickyBroadcast(i);

                if (!myBinder.isPlaying())
                    playBtn.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24);
                else
                    playBtn.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24);

                if (myBinder != null) {
                    myBinder.playInMain();
                }
                break;
            //播放下一曲
            case R.id.playnext_btn:
                try {
                    if (myBinder != null)
                        myBinder.playNext();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            //播放前一曲
            case R.id.playprv_btn:
                try {
                    if (myBinder != null)
                        myBinder.playPrv();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            //跳转到列表界面
            case R.id.change_btn:
                Intent intent1 = new Intent(MainActivity.this, MusicListActivity.class);
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
        unregisterReceiver(playNextReceiver);
        unregisterReceiver(playPrvReceiver);

    }

    class PlayorPauseReceiver extends BroadcastReceiver {
        //实现接收到广播的处理
        @Override
        public void onReceive(Context context, Intent intent) {
            if (myBinder != null) {
                myBinder.playInMain();
            }
        }
    }

    class PlayNextReceiver extends BroadcastReceiver {
        //实现接收到广播的处理
        @Override
        public void onReceive(Context context, Intent intent) {
            if (myBinder != null) {
                try {
                    myBinder.playNext();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class PlayPrvReceiver extends BroadcastReceiver {
        //实现接收到广播的处理
        @Override
        public void onReceive(Context context, Intent intent) {
            if (myBinder != null) {
                try {
                    myBinder.playPrv();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
