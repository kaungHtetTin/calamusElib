package com.calamus.calamuselib.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.calamuselib.BookDetailActivity;
import com.calamus.calamuselib.R;
import com.calamus.calamuselib.models.BookModel;
import java.util.ArrayList;
import static com.calamus.calamuselib.app.AppGloblFunction.setPhotoFromRealUrl;


public class PopularBookAdapter extends RecyclerView.Adapter<PopularBookAdapter.Holder> {

    private final Activity c;
    private final ArrayList<BookModel> data;
    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;
    @SuppressLint("SimpleDateFormat")

    public PopularBookAdapter(Activity c, ArrayList<BookModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("General", Context.MODE_PRIVATE);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public PopularBookAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_popular_book, parent, false);
        return new PopularBookAdapter.Holder(view);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final PopularBookAdapter.Holder holder, final int i) {
        BookModel model=data.get(i);

        String apiLink=sharedPreferences.getString("apiLink","");
        setPhotoFromRealUrl(holder.iv_book,apiLink+model.getThumbnail());


        holder.iv_sell.setVisibility(View.GONE);
        if(model.getSell().equals("1"))holder.iv_sell.setVisibility(View.VISIBLE);
    }

    public class Holder extends RecyclerView.ViewHolder {

        ImageView iv_book,iv_sell;

        public Holder(View view) {
            super(view);
            iv_sell=view.findViewById(R.id.iv_sell);
            iv_book=view.findViewById(R.id.iv_book);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BookModel model=data.get(getAdapterPosition());
                    Intent intent=new Intent(c, BookDetailActivity.class);
                    intent.putExtra("id",model.getId());
                    intent.putExtra("title",model.getTitle());
                    intent.putExtra("author",model.getAuthor());
                    intent.putExtra("des",model.getDescription());
                    intent.putExtra("downloadCount",model.getDownloadCount());
                    intent.putExtra("votes",model.getVotes());
                    intent.putExtra("coverImage",model.getThumbnail());
                    intent.putExtra("url",model.getUrl());
                    intent.putExtra("sell",model.getSell());
                    c.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


}