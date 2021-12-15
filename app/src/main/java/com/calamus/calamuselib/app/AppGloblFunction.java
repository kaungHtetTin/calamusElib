package com.calamus.calamuselib.app;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.ImageView;
import com.calamus.calamuselib.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import me.myatminsoe.mdetect.MDetect;
import me.myatminsoe.mdetect.Rabbit;

public class AppGloblFunction {

    public static void setPhotoFromRealUrl(ImageView iv, String url){
        Picasso.get()
                .load(url)
                .centerInside()
                .fit()
                .error(R.drawable.ic_feather)
                .into(iv, new Callback() {
                    @Override
                    public void onSuccess() {}
                    @Override
                    public void onError(Exception e) {

                    }
                });
    }

    public static  String countFormat(int i,String countName){
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        if(i==0){
            return "No "+countName;

        }else if(i==1){
            return "1 "+countName;
        }else if(i>=1000&&i<1000000){
            double j=(double) i/1000;

            return  decimalFormat.format(j)+"k "+countName+"s";
        }else if(i>=1000000){
            return decimalFormat.format((double)i/1000000) +"M Views";
        }else{
            return  i+" "+countName+"s";
        }
    }

    public static void openBook(Uri uri, Activity c){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        c.startActivity(intent);
    }


    public static boolean isFileExist(Uri uri,Activity c) {
        try {
            ParcelFileDescriptor pfd = c.getContentResolver().
                    openFileDescriptor(uri, "r");
            pfd.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static void updateCount(String id,String action){
        String url="https://www.calamuseducation.com/eLib/updatecount.php";
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Log.e("countUpdate","success");
                }
                @Override
                public void onError(String msg) {
                    Log.e("countUpdate",msg);
                }
            }).field("id",id).field("action",action).url(url);
            myHttp.runTask();
        }).start();
    }


    public static String changeUnicode(String s){

        if(MDetect.INSTANCE.isUnicode()){
            return s;
        }else {
            return Rabbit.zg2uni(s);
        }
    }

    public static String setMyanmar(String s) {

        return MDetect.INSTANCE.getText(s);
    }


    public static String formatTime( long time){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(time);
        long currentTime=System.currentTimeMillis();
        long timeDifference=currentTime-time;
        long s=1000;
        long min=s*60;
        long hour=min*60;
        long day=hour*24;
        if(timeDifference<min)return timeDifference/s+" s ago";
        else if(timeDifference>min&&timeDifference<hour) return timeDifference/min+" min ago";
        else if(timeDifference>hour&&timeDifference<day) return timeDifference/hour+ " h ago";
        else return sdf.format(resultdate);

    }


}
