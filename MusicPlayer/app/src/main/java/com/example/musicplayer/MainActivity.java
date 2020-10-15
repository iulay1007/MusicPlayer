package com.example.musicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.Bean.MusicBean;
import com.example.musicplayer.Service.MusicService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static int PERMISSION_REQUEST_CODE=1;
    public static List<MusicBean> musicBeanList=new ArrayList<>();
    private static String TAG="MainActivity";
    private MediaPlayer player;
    private ServiceConnection conn;
    private MusicService musicService;
    private MusicService.MyBinder myBinder;
    public static List<MusicBean> musicBeans=new ArrayList<>();

    private TextView textView;
    private Button play_btn;
    private Button change_btn;
    private final static int SCAN_OK=1;
    @SuppressLint("HandlerLeak")

    private Handler mHandler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case SCAN_OK:
                    Log.d(TAG,"handlermsg");


                    textView.setText(musicBeanList.get(1).getName());
                    //  MediaApplication.getInstance().setPhotoList(imageBeanList);
              //      recyclerviewAdapter.setData(musicBeans);

             //       recyclerviewAdapter.notifyDataSetChanged();

                    Log.d(TAG,"handlermsg");
                    break;
                default:Log.d(TAG,"handler_error");
            }
        }

    };



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();


        checkPermission();
        initSong();
        initPlayer();

    }

    private void initView() {
        textView=(TextView)findViewById(R.id.tv_music_name);
       // if(position_song!=0){

      //  if(musicBeanList!=null)
         //
      //  }
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


        play_btn=(Button)findViewById(R.id.play_btn);
        change_btn=(Button)findViewById(R.id.change_btn);

        play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(MainActivity.this,MusicService.class);

                startService(intent);
                bindService(intent,conn,BIND_AUTO_CREATE);

                if(myBinder!=null)
                myBinder.play();

            }
        });
        change_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,MainActivity2.class);
                startActivity(intent);


            }
        });

      //  MusicService.MyBinder myBinder=(MusicService.MyBinder) binder;



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
            mCursor.close();






        }
    }).start();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {
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

}