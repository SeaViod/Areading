package com.reader.guzhenren;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.view.WindowCompat;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONObject;

public class ReaderActivity extends AppCompatActivity {

    private JSONArray chapters;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String novelTitle = getIntent().getStringExtra("novel_title");
        String chaptersFile = getIntent().getStringExtra("chapters_file");

        setTitle(novelTitle != null ? novelTitle : "阅读");

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView())
            .setAppearanceLightStatusBars(true);

        try {
            InputStream is = getAssets().open(chaptersFile);
            byte[] buf = new byte[is.available()];
            is.read(buf);
            is.close();
            String json = new String(buf, "UTF-8");
            JSONObject root = new JSONObject(json);
            chapters = root.getJSONArray("chapters");
        } catch (Exception e) {
            chapters = new JSONArray();
        }

        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setBackgroundColor(Color.parseColor("#f5f1e8"));
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        webView.addJavascriptInterface(this, "Android");
        webView.loadUrl("file:///android_asset/reader.html");
        setContentView(webView);
    }

    @JavascriptInterface
    public int getChapterCount() {
        return chapters.length();
    }

    @JavascriptInterface
    public String getChapterTitle(int index) {
        try {
            return chapters.getJSONObject(index).getString("title");
        } catch (Exception e) {
            return "";
        }
    }

    @JavascriptInterface
    public String getChapterContent(int index) {
        try {
            return chapters.getJSONObject(index).getString("content");
        } catch (Exception e) {
            return "";
        }
    }
}
