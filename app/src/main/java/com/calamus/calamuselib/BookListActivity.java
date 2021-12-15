package com.calamus.calamuselib;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.calamus.calamuselib.adapters.BookAdapter;
import com.calamus.calamuselib.app.MyHttp;
import com.calamus.calamuselib.models.BookModel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

import static com.calamus.calamuselib.app.AppGloblFunction.changeUnicode;
import static com.calamus.calamuselib.app.AppGloblFunction.setMyanmar;

public class BookListActivity extends AppCompatActivity {

    BookAdapter adapter;
    ArrayList<BookModel> booklist=new ArrayList<>();
    RecyclerView recyclerView;
    ProgressBar pb_loading;
    TextView tv;

    int count=0;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;
    String category,cTitle,searchText,apiLink;
    Executor postExecutor;
    boolean isSearch;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        setTitle("Books");
        sharedPreferences = getSharedPreferences("General", Context.MODE_PRIVATE);
        apiLink=sharedPreferences.getString("apiLink","");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        postExecutor= ContextCompat.getMainExecutor(this);
        category=getIntent().getExtras().getString("category","");
        cTitle=getIntent().getExtras().getString("cTitle","");
        isSearch=getIntent().getExtras().getBoolean("search");
        searchText=getIntent().getExtras().getString("searchText","");

        setUpView();


    }

    private void setUpView(){
        recyclerView=findViewById(R.id.recyclerView);
        pb_loading=findViewById(R.id.pb_loading);
        tv=findViewById(R.id.tv);
        adapter=new BookAdapter(this,booklist);

        LinearLayoutManager lm = new LinearLayoutManager(this){};
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        tv.setText(setMyanmar(cTitle));

        if(!isSearch){
            fetchBookList(0+"",category,apiLink);
        }else{
            loading=false;
            searchBook(changeUnicode(searchText),apiLink);
        }


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                pastVisibleItems=lm.findFirstVisibleItemPosition();
                if(dy>0){
                    visibleItemCount=lm.getChildCount();
                    totalItemCount=lm.getItemCount();

                    if(loading){

                        if((visibleItemCount+pastVisibleItems)>=totalItemCount-7){

                            loading=false;
                            count+=50;
                            pb_loading.setVisibility(View.VISIBLE);
                            fetchBookList(count+"",category,apiLink);

                        }
                    }


                }
            }
        });



    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }



    private void fetchBookList(String count,String category,String apiLink){

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
            }).url(apiLink+"fetchbook.php?count="+count+"&category="+category);
            myHttp.runTask();
        }).start();
    }


    private void searchBook(String searchText,String apiLink){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            doAsResult(response);
                            if(response.equals("false")){
                                tv.setText("No Result Book");
                            }else {
                                tv.setText("Searched Books");
                            }

                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {

                             Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).url(apiLink+"searchbook.php?search="+searchText);
            myHttp.runTask();
        }).start();
    }



    private void doAsResult(String response){
        try {
            pb_loading.setVisibility(View.INVISIBLE);
            loading=true;

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
                booklist.add(new BookModel(id,name,author,description,downloadCount,votes,thumbnail,Url,sell));
            }

            adapter.notifyDataSetChanged();
        }catch (Exception e){
            loading=false;
          // Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }
}