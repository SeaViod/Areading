package com.reader.guzhenren;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class ReaderActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String novelTitle = getIntent().getStringExtra("novel_title");
        String chaptersFile = getIntent().getStringExtra("chapters_file");
        setTitle(novelTitle != null ? novelTitle : "阅读");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=zh-CN><head><meta charset=UTF-8><meta name=viewport content=\"width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no,viewport-fit=cover\"><script id=dataScript type=application/json>");

        try {
            InputStream is = getAssets().open(chaptersFile);
            byte[] buf = new byte[is.available()];
            is.read(buf); is.close();
            html.append(new String(buf, StandardCharsets.UTF_8));
        } catch (Exception e) {
            html.append("{\"chapters\":[]}");
        }

        html.append("</script>");

        try {
            InputStream is = getAssets().open("reader.html");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192]; int n;
            while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
            is.close();
            String rh = new String(bos.toByteArray(), StandardCharsets.UTF_8);
            int bi = rh.indexOf("<body>");
            html.append(bi > 0 ? rh.substring(bi + 6) : rh);
        } catch (Exception e) {
            html.append("</head><body><p>加载失败</p></body></html>");
        }

        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.setBackgroundColor(Color.parseColor("#f5f1e8"));
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        webView.loadDataWithBaseURL("file:///android_asset/", html.toString(), "text/html", "UTF-8", null);
        setContentView(webView);
    }
}
