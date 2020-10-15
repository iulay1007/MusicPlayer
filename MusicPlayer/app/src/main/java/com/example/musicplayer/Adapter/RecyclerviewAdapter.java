package com.example.musicplayer.Adapter;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.Bean.MusicBean;
import com.example.musicplayer.R;

import java.util.ArrayList;
import java.util.List;


public class RecyclerviewAdapter extends RecyclerView.Adapter <RecyclerView.ViewHolder>{
    public static int position_song=0;
    public List<MusicBean> musicBeanList = new ArrayList<>();
    private ItemClickListener mItemClickListener ;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
     //   View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        View view=View.inflate(parent.getContext(), R.layout.list_item,null);

        return new ViewHolder(view);
    }
    public interface ItemClickListener{
        public void onItemClick(int position,RecyclerviewAdapter.ViewHolder holder) ;
    }
    public void setOnItemClickListener(ItemClickListener itemClickListener){
        this.mItemClickListener = itemClickListener ;

    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
      TextView textView= holder.itemView.findViewById(R.id.tv);


      textView.setText(musicBeanList.get(position).getName());


      position_song=position;

        if (mItemClickListener != null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 这里利用回调来给RecyclerView设置点击事件
                    mItemClickListener.onItemClick(position, (ViewHolder) holder);


                }
            });
        }
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
