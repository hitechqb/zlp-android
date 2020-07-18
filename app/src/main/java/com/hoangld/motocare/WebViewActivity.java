package com.hoangld.motocare;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;

public class WebViewActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        WebView webView = (WebView) findViewById(R.id.webviewDemo);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://sbgateway.zalopay.vn/openinapp?order=eyJ6cHRyYW5zdG9rZW4iOiIyMDA2MjYwMDAwMDAzNTAzMTgxMDk4IiwiYXBwaWQiOjU1M30");
    }
}
