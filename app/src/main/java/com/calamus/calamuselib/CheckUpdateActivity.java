package com.calamus.calamuselib;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.calamus.calamuselib.app.MyHttp;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

import me.myatminsoe.mdetect.MDetect;

import static com.calamus.calamuselib.app.AppGloblFunction.setMyanmar;

public class CheckUpdateActivity extends AppCompatActivity {

    TextView tv,tv2;
    String available,link,apiLink;
    Button bt,bt2;
    ViewGroup main;
    ProgressBar pb_loading;
    final String checkOnline="Please check your internet connection";
    final String noUpdate="No update version is available now";
    Executor postExecutor;
    SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_update);
        sharedPreferences = getSharedPreferences("General", Context.MODE_PRIVATE);
        apiLink=sharedPreferences.getString("apiLink","");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        MDetect.INSTANCE.init(this);
        setUpView();
        getUpdateDataFromHostinger();
    }


    private void setUpView(){
        tv=findViewById(R.id.tv_update);
        tv2=findViewById(R.id.tv_status);
        bt2=findViewById(R.id.get_playStore);
        bt=findViewById(R.id.get_cupid);
        main=(ViewGroup)findViewById(R.id.layout_update);
        postExecutor= ContextCompat.getMainExecutor(this);
        pb_loading=findViewById(R.id.pb_loading);


        if(!isOnline())tv.setText(checkOnline);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline()){
                    if(available.equals("true")){
                        goPlayStore();

                    }else{
                        setSnackBar(noUpdate);
                    }
                }else{
                    setSnackBar(checkOnline);
                }
            }
        });

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isOnline()){
                    if(available.equals("true")){
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                        startActivity(browserIntent);

                    }else{
                        setSnackBar(noUpdate);
                    }
                }else{
                    setSnackBar(checkOnline);
                }
            }
        });


    }


    private void getUpdateDataFromHostinger(){
        pb_loading.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            getUpdateData(response);
                            pb_loading.setVisibility(View.GONE);
                        }
                    });

                }
                @Override
                public void onError(String msg) {}
            }).url(apiLink+"versioncontrol.php");
            myHttp.runTask();
        }).start();

    }

    private void getUpdateData(String response){
        pb_loading.setVisibility(View.GONE);
        try {
            JSONObject jo=new JSONObject(response);
            String status=jo.getString("status");

            available=jo.getString("available");
            tv.setText(setMyanmar(status));
            link=jo.getString("link");


        }catch (Exception e){}
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setSnackBar(String s){
        final Snackbar sb=Snackbar.make(main,s,Snackbar.LENGTH_INDEFINITE);
        sb.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sb.dismiss();
            }
        }).setActionTextColor(Color.WHITE)
                .show();
    }

    public void goPlayStore(){
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName())));
    }


    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) Objects.requireNonNull(this).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = Objects.requireNonNull(cm).getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}