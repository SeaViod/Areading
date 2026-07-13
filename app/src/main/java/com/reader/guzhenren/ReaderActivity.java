package com.reader.guzhenren;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.view.WindowCompat;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ReaderActivity extends AppCompatActivity {

    private WebView webView;
    private String chaptersJson;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String novelTitle = getIntent().getStringExtra("novel_title");
        String chaptersFile = getIntent().getStringExtra("chapters_file");

        setTitle(novelTitle != null ? novelTitle : "阅读");

        // Full screen with system bars overlay
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView())
            .setAppearanceLightStatusBars(true);

        // Load chapters data from assets
        try {
            InputStream is = getAssets().open(chaptersFile);
            byte[] buf = new byte[is.available()];
            is.read(buf);
            is.close();
            chaptersJson = new String(buf, StandardCharsets.UTF_8);
        } catch (Exception e) {
            chaptersJson = "{\"total\":0,\"chapters\":[]}";
        }

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setBackgroundColor(Color.parseColor("#f5f1e8"));
        
        // Hide scrollbar
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String js = "window.ANDROID_CHAPTERS_DATA = " + chaptersJson + ";"
                    + "if (typeof onDataReady === 'function') onDataReady();";
                view.evaluateJavascript(js, null);
            }
        });

        webView.loadUrl("file:///android_asset/reader.html");
        setContentView(webView);
    }
}
