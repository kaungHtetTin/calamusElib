package com.calamus.calamuselib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.calamus.calamuselib.adapters.AppAdapter;
import com.calamus.calamuselib.adapters.BookReviewAdapter;
import com.calamus.calamuselib.app.MyHttp;
import com.calamus.calamuselib.models.AppModel;
import com.calamus.calamuselib.models.ReviewModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import me.myatminsoe.mdetect.MDetect;
import static com.calamus.calamuselib.app.AppGloblFunction.changeUnicode;
import static com.calamus.calamuselib.app.AppGloblFunction.countFormat;
import static com.calamus.calamuselib.app.AppGloblFunction.setMyanmar;
import static com.calamus.calamuselib.app.AppGloblFunction.setPhotoFromRealUrl;
import static com.calamus.calamuselib.app.AppGloblFunction.updateCount;

public class BookDetailActivity extends AppCompatActivity {

    TextView tv_title,tv_author,tv_des,tv_downloadCount,tv_votes,tv_downloadComplete,tv_preparing,tv_bookReview;
    ImageView iv_book,iv_vote,iv_sell;
    Button bt;
    RecyclerView recyclerView;
    AppAdapter adapter;
    ArrayList<AppModel> appModelArrayList=new ArrayList<>();

    String title,author,description,downloadCount,votes,coverImage,dbUrl, bookId;
    final int storageRequestCode=123;
    ProgressBar pb;
    String appList,apiLink,sell;
    SharedPreferences sharedPreferences;

    public static final String CHANNEL_ID="kaung";
    private static final String CHANNEL_NAME="kaung";
    private static final String CHANNEL_DESC="kaung Notification";

    Notification notification;
    NotificationManagerCompat notificationManagerCompat;
    NotificationCompat.Builder mBuilder;

    Executor postExecutor;

    boolean isDownloaded,isSell=false;
    Uri downloadedUri;

    String userName,userid;
    SharedPreferences.Editor editor;
    long currentPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},storageRequestCode);

        MDetect.INSTANCE.init(this);
        title=getIntent().getExtras().getString("title");
        author=getIntent().getExtras().getString("author");
        description=getIntent().getExtras().getString("des");
        downloadCount=getIntent().getExtras().getString("downloadCount");
        votes=getIntent().getExtras().getString("votes");
        coverImage=getIntent().getExtras().getString("coverImage");
        dbUrl=getIntent().getExtras().getString("url");
        bookId=getIntent().getExtras().getString("id");
        sell=getIntent().getExtras().getString("sell");

        sharedPreferences = getSharedPreferences("General", Context.MODE_PRIVATE);
        appList = sharedPreferences.getString("cate_app", "");
        editor=sharedPreferences.edit();
        userName=sharedPreferences.getString("UserName","");
        apiLink=sharedPreferences.getString("apiLink","");
        currentPoint=sharedPreferences.getLong("point",0);
        userid=sharedPreferences.getString("userid","");
        postExecutor= ContextCompat.getMainExecutor(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setUpView();

        getAppList(appList);


    }

    private void setUpView(){
        recyclerView=findViewById(R.id.recyclerView);
        tv_title=findViewById(R.id.tv_title);
        tv_author=findViewById(R.id.tv_author);
        tv_des=findViewById(R.id.tv_des);
        tv_downloadCount=findViewById(R.id.tv_downloadCount);
        tv_votes=findViewById(R.id.tv_votes);
        tv_downloadComplete=findViewById(R.id.tv_downloadComplete);
        tv_preparing=findViewById(R.id.tv_preparing);
        tv_bookReview=findViewById(R.id.tv_book_review);
        iv_book=findViewById(R.id.iv_book);
        iv_vote=findViewById(R.id.iv_vote);
        iv_sell=findViewById(R.id.iv_sell);
        bt=findViewById(R.id.bt_download);
        pb=findViewById(R.id.pb_download);

        if(sell.equals("1")){
            iv_sell.setVisibility(View.VISIBLE);
            isSell=true;
        }

        tv_title.setText(setMyanmar(title));
        tv_author.setText(setMyanmar("Author - "+author));
        tv_des.setText(setMyanmar(description));

        tv_downloadCount.setText(countFormat(Integer.parseInt(downloadCount),"download"));
        tv_votes.setText(countFormat(Integer.parseInt(votes),"vote"));
        setPhotoFromRealUrl(iv_book,apiLink+coverImage);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new AppAdapter(this, appModelArrayList);
        recyclerView.setAdapter(adapter);

        isDownloaded=false;
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isOnline()){
                    if(!isDownloaded){
                        if(isSell){
                            if(currentPoint!=0) showBuyDialog();
                            else showBuyPointDialog();
                        }else{

                            updateCount(bookId,"1");
                            File rootPath = new File(Environment.getExternalStorageDirectory(), "CalamusELib");
                            createFile(rootPath.toURI(),title);
                        }
                    }else {
                        openBook(downloadedUri);
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"Please check your internet connection",Toast.LENGTH_SHORT).show();
                }

            }
        });


        tv_bookReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showBookReviewDialog();

            }
        });

    }

    private void showBuyPointDialog(){
        Button bt_yes, bt_no;
        View v = getLayoutInflater().inflate(R.layout.custom_buy_point_dialog, null);
        v.setAnimation(AnimationUtils.loadAnimation(this, R.anim.transit_up));
        bt_yes = v.findViewById(R.id.bt_yes);
        bt_no = v.findViewById(R.id.bt_no);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);
        final AlertDialog ad = builder.create();
        ad.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ad.getWindow().setGravity(Gravity.BOTTOM);
        ad.show();

        bt_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.dismiss();
            }
        });

        bt_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(BookDetailActivity.this,WebViewActivity.class);
                intent.putExtra("link",apiLink+"payment.html");
                startActivity(intent);
                ad.dismiss();

            }
        });

    }

    private void showBuyDialog() {

        Button bt_yes, bt_no;
        View v = getLayoutInflater().inflate(R.layout.custom_buy_dialog, null);
        v.setAnimation(AnimationUtils.loadAnimation(this, R.anim.transit_up));
        bt_yes = v.findViewById(R.id.bt_yes);
        bt_no = v.findViewById(R.id.bt_no);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);
        final AlertDialog ad = builder.create();
        ad.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ad.getWindow().setGravity(Gravity.BOTTOM);
        ad.show();

        bt_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.dismiss();
            }
        });

        bt_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateCount(bookId,"1");
                File rootPath = new File(Environment.getExternalStorageDirectory(), "CalamusELib");
                createFile(rootPath.toURI(),title);
                currentPoint--;
                reducePoint(currentPoint,userid);

                ad.dismiss();

            }
        });

    }

    private void reducePoint(long point,String userid){
        editor.putLong("point",point);
        editor.apply();
        final DatabaseReference RootRef;
        RootRef= FirebaseDatabase.getInstance().getReference();
        RootRef.child("Learners").child(userid).child("point").setValue(point);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            finish();

        }
        return super.onOptionsItemSelected(item);
    }

    private void openBook(Uri uri){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void downloadBook(final Uri uri){

        Notify(this,"Downloading",setMyanmar(title));
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference bookRef=storageRef.child(dbUrl);

        bookRef.getStream().addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        InputStream input=taskSnapshot.getStream();
                        byte[] data =new byte[1024];
                        int count;
                        long total =0;

                        try {
                            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
                                FileOutputStream fileOutputStream =
                                        new FileOutputStream(pfd.getFileDescriptor());
                                while ((count=input.read(data))!=-1){

                                    total+=count;
                                    int m=(int) (100.0*total/taskSnapshot.getTotalByteCount());
                                    postExecutor.execute(new Runnable() {
                                        @Override
                                        public void run() {

                                            mBuilder.setProgress(100,m,false);
                                            mBuilder.setContentTitle("Download .. "+m+" %");
                                            notificationManagerCompat.notify(title.length(),mBuilder.build());

                                            tv_preparing.setVisibility(View.GONE);
                                            pb.setVisibility(View.VISIBLE);
                                            pb.setProgress(m);

                                        }
                                    });
                                    fileOutputStream.write(data);
                                }

                                fileOutputStream.close();
                                pfd.close();
                                postExecutor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        pb.setVisibility(View.INVISIBLE);
                                        tv_downloadComplete.setVisibility(View.VISIBLE);
                                        Animation ani_downloadComplete= AnimationUtils.loadAnimation(BookDetailActivity.this,R.anim.tv_download_complete);
                                        tv_downloadComplete.setAnimation(ani_downloadComplete);

                                        mBuilder.setProgress(100,100,false);
                                        mBuilder.setContentTitle("Download - "+100+" % ");
                                        mBuilder.setContentText(title+" is completely downloaded");
                                        notificationManagerCompat.notify(title.length(),mBuilder.build());

                                        bt.setText("OPEN");
                                        isDownloaded=true;
                                        downloadedUri=uri;
                                    }
                                });

                            }catch (Exception e){
                            mBuilder.setProgress(100,0,false);
                            mBuilder.setContentTitle("Download");
                            mBuilder.setContentText("Error downloading - "+title);
                            notificationManagerCompat.notify(title.length(),mBuilder.build());
                            }
                        }
                    }).start();
                }
            });


    }

    private static final int CREATE_FILE = 1;

    private void createFile(URI pickerInitialUri, String name) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, name+".pdf");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        }
        startActivityForResult(intent, CREATE_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CREATE_FILE && resultCode==RESULT_OK){
            Uri Doc=data.getData();

            downloadBook(Doc);
            Animation ani_downloadComplete= AnimationUtils.loadAnimation(BookDetailActivity.this,R.anim.tv_download_complete);
            tv_preparing.setVisibility(View.VISIBLE);
            tv_preparing.setAnimation(ani_downloadComplete);

        }
    }


    private void Notify(Context mContext, String title, String message){
        mBuilder=new NotificationCompat.Builder(mContext,CHANNEL_ID);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("message", message);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        0,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel=new NotificationChannel(CHANNEL_ID,CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager=mContext.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }


        notification = mBuilder.setSmallIcon(R.mipmap.eemainicon)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.mipmap.eemainicon)
                .setContentText(message)
                .setSound(null)
                .build();
        notificationManagerCompat=NotificationManagerCompat.from(mContext);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==storageRequestCode){
            if(grantResults.length==2 && grantResults[0]== PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){


            }else{
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||   ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ){
                    Toast.makeText(this,"Storage permission is required.",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Allow storage permission in your phone setting.",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) Objects.requireNonNull(this).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = Objects.requireNonNull(cm).getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void getAppList(String appList){
        try {
            JSONObject jo=new JSONObject(appList);
            JSONArray jsonArray=jo.getJSONArray("apps");

            for(int i=0;i<jsonArray.length();i++){
                JSONObject joApp=jsonArray.getJSONObject(i);
                String name=joApp.getString("name");
                String description=joApp.getString("des");
                String link=joApp.getString("link");
                String thumb=joApp.getString("thumb");
                appModelArrayList.add(new AppModel(name,description,thumb,link));
            }

            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    ArrayList<ReviewModel> reviewList=new ArrayList<>();
    TextView tv;
    RecyclerView recyclerViewR;
    BookReviewAdapter reviewAdapter;
    ProgressBar pb_review;
    private void showBookReviewDialog(){

        final EditText et;
        ImageButton bt;
        View v=getLayoutInflater().inflate(R.layout.custom_book_review_dialog,null);
        v.setAnimation(AnimationUtils.loadAnimation(this,R.anim.transit_up));
        et=v.findViewById(R.id.nf_dia_et);
        bt=v.findViewById(R.id.Review_dia_bt);
        tv=v.findViewById(R.id.tv_noReview);
        pb_review=v.findViewById(R.id.pb_review);
        tv.setVisibility(View.INVISIBLE);
        recyclerViewR=v.findViewById(R.id.recycler);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content=changeUnicode(et.getText().toString());
                if(!TextUtils.isEmpty(content)) {
                    et.setText("");
                    tv.setVisibility(View.INVISIBLE);
                    reviewList.add(new ReviewModel(userName,content,System.currentTimeMillis()+""));
                    reviewAdapter.notifyItemInserted(reviewList.size());
                    recyclerViewR.smoothScrollToPosition(reviewList.size());
                    addReview(bookId,userName,content,System.currentTimeMillis()+"");
                }

            }
        });


        final LinearLayoutManager lm = new LinearLayoutManager(this){};
        recyclerViewR.setLayoutManager(lm);
        // recyclerView.addItemDecoration(new SpacingItemDecoration(2, XUtils.toPx(Objects.requireNonNull(this), 2), true));
        recyclerViewR.setItemAnimator(new DefaultItemAnimator());

        reviewAdapter = new BookReviewAdapter(this,reviewList);
        recyclerViewR.setAdapter(reviewAdapter);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setView(v);
        final AlertDialog ad=builder.create();
        ad.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ad.show();
        fetchReview(bookId);

    }

    private void addReview(String bookId,String name, String review, String time){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                             //Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            })
                    .field("book_id",bookId)
                    .field("name",name)
                    .field("review",review)
                    .field("time",time).url(apiLink+"addbookreview.php");
            myHttp.runTask();
        }).start();
    }


    private void fetchReview(String bookId){
        reviewList.clear();
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            doAsResult(response);
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).url(apiLink+"fetchbookreview.php?book_id="+bookId);
            myHttp.runTask();
        }).start();
    }

    private void doAsResult(String response){
        try {
            pb_review.setVisibility(View.INVISIBLE);

            JSONArray ja=new JSONArray(response);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String name=jo.getString("name");
                String review=jo.getString("review");
                String time=jo.getString("time");
                reviewList.add(new ReviewModel(name,review,time));
            }

            reviewAdapter.notifyDataSetChanged();
        }catch (Exception e){
            tv.setVisibility(View.VISIBLE);
        }
    }
}