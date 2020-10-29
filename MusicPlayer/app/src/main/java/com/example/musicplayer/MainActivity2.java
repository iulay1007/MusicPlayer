package com.example.musicplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.Adapter.RecyclerviewAdapter;
import com.example.musicplayer.EventBus.MyEventBus;
import com.example.musicplayer.Service.MusicService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import static com.example.musicplayer.MainActivity.musicBeanList;

public class MainActivity2 extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerviewAdapter recyclerviewAdapter;
    private ServiceConnection conn;
    private MusicService.MyBinder myBinder;
    private MusicService musicService;
    private final static int OK=1;
    private TextView textView;
    private Button play_btn;
    private Button play_next_btn;
    public int second_position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        initView();
        Intent intent = new Intent(MainActivity2.this, MusicService.class);
        startService(intent);
        bindService(intent, conn, BIND_AUTO_CREATE);

        EventBus.getDefault().register(this);

    }
    private void initView() {
        recyclerView = findViewById(R.id.image_list_view);

        textView=findViewById(R.id.tv);
        play_btn=findViewById(R.id.play);
        play_next_btn=findViewById(R.id.play_next);
   //textview点击跳转
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity2.this,MainActivity.class);
                startActivity(intent);
            }
        });
        //播放/暂停
        play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myBinder.playinmain();
            }
        });
        play_next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (myBinder != null)
                    myBinder.playnext();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        recyclerView.setAdapter(recyclerviewAdapter=new RecyclerviewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity2.this, LinearLayoutManager.VERTICAL,false));


        if(musicBeanList!=null)
        recyclerviewAdapter.setData(musicBeanList);


        conn=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

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

              second_position=position;
              textView.setText(musicBeanList.get(second_position).getName());
                EventBus.getDefault().postSticky(new MyEventBus(second_position));
                if (myBinder != null) {
                    myBinder.playinmain();
                }


                if(myBinder!=null) {
                    try {
                        myBinder.play();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }


});}

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void Event(MyEventBus myEventBus) {

        second_position = myEventBus.getPosition();
        textView.setText(musicBeanList.get(second_position).getName() );


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}