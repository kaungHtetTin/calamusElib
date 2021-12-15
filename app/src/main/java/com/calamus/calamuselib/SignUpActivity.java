package com.calamus.calamuselib;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    EditText et_name;
    Button bt_signUp;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ProgressBar loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        sharedPreferences=getSharedPreferences("General", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();

        setUpView();
    }

    private void setUpView(){
        et_name=findViewById(R.id.et_name);
        bt_signUp=findViewById(R.id.bt_SignUp);
        loading=findViewById(R.id.pb_loading);
        loading.setVisibility(View.GONE);
        bt_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name=et_name.getText().toString();
                if(!TextUtils.isEmpty(name)){
                    registerNow(name,System.currentTimeMillis()+"");
                }else{
                    Toast.makeText(getApplicationContext(),"Please enter your name",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void registerNow(String name, String userid){
        bt_signUp.setEnabled(false);
        loading.setVisibility(View.VISIBLE);
        final DatabaseReference RootRef;
        RootRef= FirebaseDatabase.getInstance().getReference();
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!(snapshot.child("Users").child(userid).exists())){
                    HashMap<String,Object> userdataMap=new HashMap<>();
                    userdataMap.put("name",name);
                    userdataMap.put("userid",userid);
                    userdataMap.put("point",5);
                    RootRef.child("Learners").child(userid).updateChildren(userdataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        editor.putString("UserName",name);
                                        editor.putString("userid",userid);
                                        editor.putLong("point",5);
                                        editor.putBoolean("autoLogin",true);
                                        editor.apply();

                                        Intent intent=new Intent(SignUpActivity.this,MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else {
                                        bt_signUp.setEnabled(true);
                                        loading.setVisibility(View.INVISIBLE);
                                        Toast.makeText(getApplicationContext(),"Network error, please try again",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else{
                    bt_signUp.setEnabled(true);
                    loading.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bt_signUp.setEnabled(true);
                loading.setVisibility(View.INVISIBLE);
            }
        });

    }

}