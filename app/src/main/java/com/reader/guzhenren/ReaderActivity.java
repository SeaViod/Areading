package com.reader.guzhenren;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.view.WindowCompat;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ReaderActivity extends AppCompatActivity {

    private String chaptersFile;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String novelTitle = getIntent().getStringExtra("novel_title");
        chaptersFile = getIntent().getStringExtra("chapters_file");
        setTitle(novelTitle != null ? novelTitle : "阅读");

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.setBackgroundColor(Color.parseColor("#f5f1e8"));
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        webView.setWebViewClient(new WebViewClient() {
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                // Intercept any request for chapters.json
                if (url.contains("chapters.json")) {
                    try {
                        InputStream is = getAssets().open(chaptersFile);
                        return new WebResourceResponse("application/json", "UTF-8", is);
                    } catch (Exception e) {
                        return null;
                    }
                }
                return null;
            }
        });

        webView.loadUrl("file:///android_asset/reader.html");
        setContentView(webView);
    }
}
