package com.calamus.calamuselib.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebAppInterface {
    Context mContext;

    /** Instantiate the interface and set the context */
    public WebAppInterface(Context c) {
        mContext = c;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
       // Toast.makeText(mContext,toast,Toast.LENGTH_SHORT).show();
        Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(toast));
        mContext.startActivity(intent);
    }


}

