package com.example.musicplayer.Adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.Bean.MusicBean;
import com.example.musicplayer.R;

import java.util.ArrayList;
import java.util.List;


public class RecyclerviewAdapter extends RecyclerView.Adapter <RecyclerView.ViewHolder>{
    public static int position_song=0;
    public List<MusicBean> musicBeanList = new ArrayList<>();
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
     //   View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        View view=View.inflate(parent.getContext(), R.layout.list_item,null);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
      TextView textView= holder.itemView.findViewById(R.id.tv);


      textView.setText(musicBeanList.get(position).getName());


      position_song=position;
/*
      holder.itemView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Intent intent=new Intent(this, MusicService.class);

              startService(intent);
              bindService(intent,conn,BIND_AUTO_CREATE);

              if(myBinder!=null)
                  myBinder.play();
          }
      });*/
    }

    @Override
    public int getItemCount() {
         if(musicBeanList!=null)
            return musicBeanList.size();
        return 10;
    }
    public void setData(List<MusicBean> musicBeans) {

        musicBeanList.clear();


        musicBeanList.addAll(musicBeans);
        notifyDataSetChanged();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
