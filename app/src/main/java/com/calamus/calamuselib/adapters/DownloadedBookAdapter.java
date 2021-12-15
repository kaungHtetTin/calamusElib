package com.calamus.calamuselib.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import com.calamus.calamuselib.R;
import com.calamus.calamuselib.models.BookModel;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static com.calamus.calamuselib.app.AppGloblFunction.isFileExist;
import static com.calamus.calamuselib.app.AppGloblFunction.openBook;


public class DownloadedBookAdapter extends RecyclerView.Adapter<DownloadedBookAdapter.Holder> {

    private final Activity c;
    private final ArrayList<BookModel> data;
    private final LayoutInflater mInflater;
    @SuppressLint("SimpleDateFormat")

    public DownloadedBookAdapter(Activity c, ArrayList<BookModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public DownloadedBookAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_downloaded_books, parent, false);
        return new DownloadedBookAdapter.Holder(view);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final DownloadedBookAdapter.Holder holder, final int i) {
        BookModel model=data.get(i);
        holder.tv_title.setText(model.getTitle());
        holder.tv_author.setText("Author - "+model.getAuthor());
        holder.tv_des.setText(model.getDescription());

    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView tv_title,tv_author,tv_des;

        public Holder(View view) {
            super(view);
            tv_title=view.findViewById(R.id.tv_title);
            tv_author=view.findViewById(R.id.tv_author);
            tv_des=view.findViewById(R.id.tv_des);


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BookModel model=data.get(getAdapterPosition());
                    if(isFileExist(Uri.parse(model.getUrl()),c))
                        openBook(Uri.parse(model.getUrl()),c);
                    else Toast.makeText(c,"File not exist",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}