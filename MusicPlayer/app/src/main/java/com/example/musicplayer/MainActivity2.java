package com.example.musicplayer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.Adapter.RecyclerviewAdapter;

import static com.example.musicplayer.MainActivity.musicBeanList;

public class MainActivity2 extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerviewAdapter recyclerviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        initView();

    }
    private void initView() {
        recyclerView = findViewById(R.id.image_list_view);

        // recyclerviewAdapter;
        recyclerView.setAdapter(recyclerviewAdapter=new RecyclerviewAdapter());

        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity2.this, LinearLayoutManager.VERTICAL,false));

        if(musicBeanList!=null)
        recyclerviewAdapter.setData(musicBeanList);

    }
}