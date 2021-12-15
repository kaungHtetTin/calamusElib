package com.calamus.calamuselib;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.calamus.calamuselib.adapters.CategoryAdapter;
import com.calamus.calamuselib.adapters.PopularBookAdapter;
import com.calamus.calamuselib.app.MyDialog;
import com.calamus.calamuselib.app.MyHttp;
import com.calamus.calamuselib.models.BookModel;
import com.calamus.calamuselib.models.CategoryModel;
import com.google.android.material.navigation.NavigationView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    NavigationView navigationView;
    private ActionBarDrawerToggle abdToggle;
    SharedPreferences sharedPreferences;
    String categoryList,apiLink;
    RecyclerView recyclerView;
    ArrayList<CategoryModel> categoryLists = new ArrayList<>();
    CategoryAdapter categoryAdapter;
    RelativeLayout cateContainer;
    RecyclerView recyclerViewP;
    PopularBookAdapter popularBookAdapter;
    ArrayList<BookModel> popBookList=new ArrayList<>();
    Executor postExecutor;

    ProgressBar pb_loading;
    ImageButton bt_search;
    EditText et_search;

    String name;
    String userid;
    long point;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("General", Context.MODE_PRIVATE);
        categoryList = sharedPreferences.getString("cate_app", "");
        apiLink=sharedPreferences.getString("apiLink","");

        name=sharedPreferences.getString("UserName","");
        userid=sharedPreferences.getString("userid","");
        point=sharedPreferences.getLong("point",0);

        if(apiLink.equals("")){
            categoryList=getIntent().getExtras().getString("categoryList");
            apiLink=getIntent().getExtras().getString("apiLink");

        }

        String version=sharedPreferences.getString("version","");
        if(!version.equals("1.02"))confirmUpdate();

        postExecutor= ContextCompat.getMainExecutor(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setUpView();

    }

    private void setUpView() {

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewP=findViewById(R.id.recyclerViewP);
        cateContainer=findViewById(R.id.cate_container);
        pb_loading=findViewById(R.id.pb_loading);
        bt_search=findViewById(R.id.bt_search);
        et_search=findViewById(R.id.et_search);

        View headerView=navigationView.getHeaderView(0);
        TextView tv_name=headerView.findViewById(R.id.tv_header_name);
        TextView tv_userid=headerView.findViewById(R.id.tv_header_userid);
        TextView tv_point=headerView.findViewById(R.id.tv_header_point);

        tv_name.setText(name);
        tv_userid.setText("id - "+userid);
        tv_point.setText(point+" ");

        abdToggle = new ActionBarDrawerToggle(this, drawer, R.string.open_drawer, R.string.close_drawer);
        drawer.addDrawerListener(abdToggle);
        abdToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
        abdToggle.setHomeAsUpIndicator(android.R.drawable.ic_menu_more);

        LinearLayoutManager lm = new LinearLayoutManager(this) {
        };
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(lm);
        categoryAdapter = new CategoryAdapter(this, categoryLists);
        recyclerView.setAdapter(categoryAdapter);
        fetchCategoryList(categoryList);

        popularBookAdapter=new PopularBookAdapter(this,popBookList);
        LinearLayoutManager lmc = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewP.setLayoutManager(lmc);
        recyclerViewP.setItemAnimator(new DefaultItemAnimator());
        recyclerViewP.setAdapter(popularBookAdapter);

        Animation motionUp= AnimationUtils.loadAnimation(this,R.anim.view_up);
        //cateContainer.setAnimation(motionUp);

        fetchPopularBookList(apiLink);

        bt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,BookListActivity.class);
                intent.putExtra("search",true);
                intent.putExtra("searchText",et_search.getText().toString());
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.check_update:
                Intent intent=new Intent(MainActivity.this,CheckUpdateActivity.class);
                startActivity(intent);
                break;

            case R.id.share_app:
                Intent shareingIntent = new Intent(Intent.ACTION_SEND);
                shareingIntent.setType("text/plain");
                String shareBody = "https://play.google.com/store/apps/details?id=" +getPackageName();
                shareingIntent.putExtra(Intent.EXTRA_SUBJECT, "Try out this best E-Library App.");
                shareingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareingIntent, "Share via"));

                break;

            case R.id.about_app:
                Intent intent1=new Intent(MainActivity.this,WebViewActivity.class);
                intent1.putExtra("link","file:///android_asset/about.html");
                startActivity(intent1);
                break;

            case R.id.rate_us:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName())));
                break;

            case R.id.get_point:
                Intent intent2=new Intent(MainActivity.this,WebViewActivity.class);
                intent2.putExtra("link",apiLink+"payment.html");
                startActivity(intent2);
                break;
        }

     //   drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (abdToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchCategoryList(String categoryList) {
        categoryLists.add(new CategoryModel("0","All books"));
        try {
            JSONObject myJo=new JSONObject(categoryList);
            String cates=myJo.getString("category");
            JSONArray ja = new JSONArray(cates);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                String id=jo.getString("id");
                String title = jo.getString("title");
                categoryLists.add(new CategoryModel(id,title));

            }
            categoryAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void fetchPopularBookList(String apiLink){
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
                            // Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).url(apiLink+"fetchpopularbook.php");
            myHttp.runTask();
        }).start();
    }

    private void doAsResult(String response){
        try {
            pb_loading.setVisibility(View.INVISIBLE);
            JSONArray ja=new JSONArray(response);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String id=jo.getString("id");
                String name=jo.getString("name");
                String author=jo.getString("author");
                String description=jo.getString("description");
                String downloadCount=jo.getString("downloadCounts");
                String votes=jo.getString("votes");
                String thumbnail=jo.getString("coverImage");
                String Url=jo.getString("bookUrl");
                String sell=jo.getString("sell");
                popBookList.add(new BookModel(id,name,author,description,downloadCount,votes,thumbnail,Url,sell));
            }
            popularBookAdapter.notifyDataSetChanged();
        }catch (Exception e){
            // Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    public void  confirmUpdate(){

        MyDialog myDialog=new MyDialog(this, "Update Version!", "Do you want to update the app now?", new MyDialog.ConfirmClick() {
            @Override
            public void onConfirmClick() {
                Intent intent=new Intent(MainActivity.this,CheckUpdateActivity.class);
                startActivity(intent);
            }
        });
        myDialog.showMyDialog();
    }
}