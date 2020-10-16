package com.example.musicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.musicplayer.Bean.MusicBean;
import com.example.musicplayer.Service.MusicService;
import com.example.musicplayer.utils.NotificationsUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static int PERMISSION_REQUEST_CODE=1;
    public static List<MusicBean> musicBeanList=new ArrayList<>();
    private static String TAG="MainActivity";
    private MediaPlayer player;
    private ServiceConnection conn;
    private MusicService musicService;
    private MusicService.MyBinder myBinder;
    public static List<MusicBean> musicBeans=new ArrayList<>();

    public TextView textView;
    private Button play_btn;
    private Button change_btn;
    private Button playprv_btn;
    private Button playnext_btn;
    private final static int SCAN_OK=1;
    private final static int getintent=2;
    private int p;
    private RemoteViews remoteViews;
    @SuppressLint("HandlerLeak")

    private Handler mHandler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case SCAN_OK:
                    Log.d(TAG,"handlermsg");


                   // textView.setText(musicBeanList.get(1).getName());
                    //  MediaApplication.getInstance().setPhotoList(imageBeanList);
              //      recyclerviewAdapter.setData(musicBeans);

             //       recyclerviewAdapter.notifyDataSetChanged();

                    Log.d(TAG,"handlermsg");
                    break;
                case getintent:

             textView.setText(musicBeanList.get(p).getName());

                default:Log.d(TAG,"handler_error");
            }
        }

    };


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
        showNotification();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotification() {
        String channelId = "default";
        String channelName = "默认通知";
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
   remoteViews=new RemoteViews(getPackageName(),R.layout.notification_layout);
      NotificationChannel channel = new NotificationChannel(
                getPackageName(),
                "会话消息()",
                NotificationManager.IMPORTANCE_DEFAULT

        );
        notificationManager.createNotificationChannel(new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH));

        notificationManager.createNotificationChannel(channel);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,channelId).setSmallIcon(R.drawable.ic_baseline_favorite_24)
                .setContentTitle("title").setContentText("Content").setChannelId(getPackageName()) .setPriority(NotificationCompat.PRIORITY_HIGH);
      builder.setContent(remoteViews).setSmallIcon(R.drawable.ic_baseline_favorite_24);


        notificationManager.notify(1,builder.build());

    }
    public  boolean isNotifyEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return isEnableV26(context);
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



    private void initPlayer() {


        conn=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                // musicService=((MusicService.MyBinder)(iBinder)).getService();
                myBinder = (MusicService.MyBinder) iBinder;


            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                musicService=null;
            }
        };


        Intent intent=new Intent(MainActivity.this,MusicService.class);

        startService(intent);
        bindService(intent,conn,BIND_AUTO_CREATE);



    }

    private void initSong() {new Thread(new Runnable() {
        @Override
        public void run() {


            ContentResolver contentResolver = MainActivity.this.getContentResolver();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            Cursor mCursor = contentResolver.query(uri,new String[]{MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.DISPLAY_NAME}, null,null, "date_added DESC");
            String[] columnNames = mCursor.getColumnNames();
            if(mCursor==null) return;
            while (mCursor.moveToNext()) {
                String path=mCursor.getString(0);
                String name=mCursor.getString(1);
                MusicBean musicBean=new MusicBean(path,name);
                musicBeanList.add(musicBean);
               for(MusicBean musicBean1:musicBeanList){

                 Log.d(TAG,"==="+musicBean1);
                }


            }
            Log.d(TAG,"qwq");

         mHandler.sendEmptyMessage(SCAN_OK);
            Intent intent=getIntent();
            p=intent.getIntExtra("p",0);
            mHandler.sendEmptyMessage(getintent);
            mCursor.close();






        }
    }).start();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {
        if(!NotificationsUtils.isNotificationEnabled(this)) {
            NotificationsUtils.openPush(this);
        }
        int readExternalStoragePermissionResult = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if(readExternalStoragePermissionResult != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_REQUEST_CODE){
            if (grantResults.length==0&&grantResults[0]== PackageManager.PERMISSION_GRANTED) {

            }else{

            }


        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.play_btn:
              /*  Intent intent=new Intent(MainActivity.this,MusicService.class);

                startService(intent);
                bindService(intent,conn,BIND_AUTO_CREATE);*/

                if(myBinder!=null) {

                    myBinder.playinmain();

                }

                break;
            case R.id.playnext_btn:
              try {
                if(myBinder!=null)
                    myBinder.playnext();
            } catch (IOException e) {
                e.printStackTrace();
            }


                break;
            case R.id.playprv_btn:    try {
                if(myBinder!=null)
                    myBinder.playprv();
            } catch (IOException e) {
                e.printStackTrace();
            }
            break;
            case R.id.change_btn:
                Intent intent1=new Intent(MainActivity.this,MainActivity2.class);
                startActivity(intent1);
                break;
        }
    }
    class BroadcastReceiverinMain extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            intent=getIntent();
          int q=  intent.getIntExtra("position",0);
          Log.d(TAG,"---onReceive");
          textView.setText(musicBeanList.get(q).getName());



        }
    }

}
