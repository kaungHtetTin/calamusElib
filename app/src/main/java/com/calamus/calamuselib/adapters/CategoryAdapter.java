package com.calamus.calamuselib.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.calamuselib.BookListActivity;
import com.calamus.calamuselib.R;
import com.calamus.calamuselib.models.CategoryModel;

import java.util.ArrayList;

import me.myatminsoe.mdetect.MDetect;

import static com.calamus.calamuselib.app.AppGloblFunction.setMyanmar;


public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.Holder> {

    private final Activity c;
    private final ArrayList<CategoryModel> data;
    private final LayoutInflater mInflater;
    @SuppressLint("SimpleDateFormat")

    public CategoryAdapter(Activity c, ArrayList<CategoryModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        MDetect.INSTANCE.init(c);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public CategoryAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_category, parent, false);
        return new CategoryAdapter.Holder(view);

    }

    @Override
    public void onBindViewHolder(final CategoryAdapter.Holder holder, final int i) {
        CategoryModel model=data.get(i);
        String category=model.getTitle();
        holder.tv.setText(setMyanmar(category));


    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView tv;

        public Holder(View view) {
            super(view);
            tv=view.findViewById(R.id.tv_categoryTitle);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final CategoryModel model=data.get(getAdapterPosition());
                    Intent intent=new Intent(c, BookListActivity.class);
                    intent.putExtra("category",getAdapterPosition()+"");
                    intent.putExtra("cTitle",model.getTitle());
                    intent.putExtra("search",false);
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