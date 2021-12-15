package com.calamus.calamuselib.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.calamus.calamuselib.BookDetailActivity;
import com.calamus.calamuselib.R;
import com.calamus.calamuselib.models.BookModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;

import me.myatminsoe.mdetect.MDetect;

import static com.calamus.calamuselib.app.AppGloblFunction.countFormat;
import static com.calamus.calamuselib.app.AppGloblFunction.setMyanmar;
import static com.calamus.calamuselib.app.AppGloblFunction.updateCount;


public class BookAdapter extends RecyclerView.Adapter<BookAdapter.Holder> {

    private final Activity c;
    private final ArrayList<BookModel> data;
    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;
    @SuppressLint("SimpleDateFormat")

    public BookAdapter(Activity c, ArrayList<BookModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        MDetect.INSTANCE.init(c);
        sharedPreferences=c.getSharedPreferences("General", Context.MODE_PRIVATE);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public BookAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_book_list, parent, false);
        return new BookAdapter.Holder(view);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final BookAdapter.Holder holder, final int i) {
        BookModel model=data.get(i);
        holder.tv_title.setText(setMyanmar(model.getTitle()));
        holder.tv_author.setText(setMyanmar("Author - "+model.getAuthor()));
        holder.tv_des.setText(setMyanmar(model.getDescription()));
        holder.tv_votes.setText(countFormat(Integer.parseInt(model.getVotes()),"vote"));
        holder.tv_downloadCount.setText(countFormat(Integer.parseInt(model.getDownloadCount()),"download"));
        final int[] vote = {Integer.parseInt(model.getVotes())};
        holder.iv_vote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BookModel model=data.get(i);
                holder.iv_vote.setImageDrawable(c.getResources().getDrawable(R.drawable.ic_voted));
                updateCount(model.getId(),"2");
                vote[0]++;
                holder.tv_votes.setText(countFormat(vote[0],"vote"));
                model.setVotes(vote[0]+"");
            }
        });
        String apiLink=sharedPreferences.getString("apiLink","");

        Picasso.get()
                .load(apiLink+model.getThumbnail())
                .centerInside()
                .fit()
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.iv_book, new Callback() {
                    @Override
                    public void onSuccess() {}
                    @Override
                    public void onError(Exception e) {

                    }
                });

        holder.iv_sell.setVisibility(View.GONE);
        if(model.getSell().equals("1"))holder.iv_sell.setVisibility(View.VISIBLE);
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView tv_title,tv_author,tv_des,tv_downloadCount,tv_votes;
        ImageView iv_book,iv_vote,iv_sell;

        public Holder(View view) {
            super(view);
            tv_title=view.findViewById(R.id.tv_title);
            tv_author=view.findViewById(R.id.tv_author);
            tv_des=view.findViewById(R.id.tv_des);
            tv_downloadCount=view.findViewById(R.id.tv_downloadCount);
            tv_votes=view.findViewById(R.id.tv_votes);
            iv_book=view.findViewById(R.id.iv_book);
            iv_vote=view.findViewById(R.id.iv_vote);
            iv_sell=view.findViewById(R.id.iv_sell);
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