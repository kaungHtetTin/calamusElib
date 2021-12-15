package com.calamus.calamuselib;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.calamus.calamuselib.app.MyHttp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreenActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String categoryList="",apiLink="",userid;
    boolean autoLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        sharedPreferences=getSharedPreferences("General", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        autoLogin=sharedPreferences.getBoolean("autoLogin",false);
        userid=sharedPreferences.getString("userid","");

        getApi();

        if(autoLogin){
            getUpdateUserData(userid);
            setScree();
        }else{
            Intent intent=new Intent(SplashScreenActivity.this,SignUpActivity.class);
            startActivity(intent);
            finish();
        }

    }


    private void setScree(){
        Thread timer=new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    sleep(3000);
                    Intent intent=new Intent(SplashScreenActivity.this,MainActivity.class);
                    intent.putExtra("categoryList",categoryList);
                    intent.putExtra("apiLink",apiLink);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.start();
    }

    private void getCategoryLists(String apiLink){
        String url=apiLink+"fetchcategory.php";
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    categoryList=response;
                    editor.putString("cate_app",response);
                    editor.apply();

                }
                @Override
                public void onError(String msg) {

                }
            }).url(url);
            myHttp.runTask();
        }).start();
    }

    private void getApi(){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("api");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String api= (String) snapshot.child("apiLink").getValue();
                String version=(String) snapshot.child("version").getValue();
                apiLink=api;
                editor.putString("apiLink",api);
                editor.putString("version",version);
                editor.apply();
                getCategoryLists(api);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUpdateUserData(String userid){
        final DatabaseReference RootRef= FirebaseDatabase.getInstance().getReference();
        RootRef.child("Learners").child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long point=(long)snapshot.child("point").getValue();
                editor.putLong("point",point);
                editor.apply();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}