package com.calamus.calamuselib.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.calamuselib.R;
import com.calamus.calamuselib.models.ReviewModel;
import java.util.ArrayList;
import me.myatminsoe.mdetect.MDetect;
import static com.calamus.calamuselib.app.AppGloblFunction.formatTime;
import static com.calamus.calamuselib.app.AppGloblFunction.setMyanmar;


public class BookReviewAdapter extends RecyclerView.Adapter<BookReviewAdapter.Holder> {

    private final Activity c;
    private final ArrayList<ReviewModel> data;
    private final LayoutInflater mInflater;
    @SuppressLint("SimpleDateFormat")

    public BookReviewAdapter(Activity c, ArrayList<ReviewModel> data) {
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
    public BookReviewAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_book_review, parent, false);
        return new BookReviewAdapter.Holder(view);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final BookReviewAdapter.Holder holder, final int i) {
        ReviewModel model=data.get(i);

        holder.tv_name.setText(setMyanmar(model.getName()));
        long time=Long.parseLong(model.getTime());
        holder.tv_time.setText(formatTime(time));

        if(model.getReview().length()>100){
            String sub=model.getReview().substring(0,100);
            holder.tv_Review.setText(setMyanmar(sub));
            holder.tv_readMore.setVisibility(View.VISIBLE);
        }else{
            holder.tv_Review.setText(setMyanmar(model.getReview()));
            holder.tv_readMore.setVisibility(View.GONE);
        }

        holder.tv_readMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.tv_Review.setText(setMyanmar(model.getReview()));
                holder.tv_readMore.setVisibility(View.GONE);
            }
        });

    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView tv_name,tv_Review,tv_readMore,tv_time;

        public Holder(View view) {
            super(view);
            tv_name=view.findViewById(R.id.tv_UserName);
            tv_Review=view.findViewById(R.id.tv_Review);
            tv_readMore=view.findViewById(R.id.tv_ReadMore);
            tv_time=view.findViewById(R.id.tv_time);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }



}