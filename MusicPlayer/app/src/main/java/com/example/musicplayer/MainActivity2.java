package com.example.musicplayer;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.Adapter.RecyclerviewAdapter;
import com.example.musicplayer.Service.MusicService;

import java.io.IOException;

import static com.example.musicplayer.Adapter.RecyclerviewAdapter.position_song;
import static com.example.musicplayer.MainActivity.musicBeanList;

public class MainActivity2 extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerviewAdapter recyclerviewAdapter;
    private ServiceConnection conn;
    private MusicService.MyBinder myBinder;
    private MusicService musicService;
    private final static int OK=1;
    private TextView textView;
    private Button button;
    @SuppressLint("HandlerLeak")

    private Handler mHandler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case OK:
                   // Log.d(TAG,"handlermsg");


                    textView.setText(musicBeanList.get(position_song).getName());
                    //  MediaApplication.getInstance().setPhotoList(imageBeanList);
                    //      recyclerviewAdapter.setData(musicBeans);

                    //       recyclerviewAdapter.notifyDataSetChanged();

                   // Log.d(TAG,"handlermsg");
                    break;
               // default:Log.d(TAG,"handler_error");
            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        initView();

    }
    private void initView() {
        recyclerView = findViewById(R.id.image_list_view);

        textView=findViewById(R.id.tv);
        button=findViewById(R.id.play);
        // recyclerviewAdapter;
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity2.this,MainActivity.class);
                intent.putExtra("p",position_song);
                startActivity(intent);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myBinder.playinmain();
            }
        });
        recyclerView.setAdapter(recyclerviewAdapter=new RecyclerviewAdapter());

        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity2.this, LinearLayoutManager.VERTICAL,false));


        if(musicBeanList!=null)
        recyclerviewAdapter.setData(musicBeanList);
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

        recyclerviewAdapter.setOnItemClickListener(new RecyclerviewAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position, RecyclerviewAdapter.ViewHolder holder) {
             // Toast.makeText(MainActivity2.this,"po"+position,Toast.LENGTH_SHORT).show();
                position_song=position;
                Intent intent=new Intent(MainActivity2.this, MusicService.class);

                startService(intent);
                bindService(intent,conn,BIND_AUTO_CREATE);

                if(myBinder!=null) {
                    try {
                        myBinder.play();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mHandler.sendEmptyMessage(OK);

            }


});}}