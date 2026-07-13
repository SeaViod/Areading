package com.reader.guzhenren;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
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
        String coverColor = getIntent().getStringExtra("cover_color");

        setTitle(novelTitle != null ? novelTitle : "阅读");

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
        webView.setBackgroundColor(Color.parseColor("#f8f6f0"));

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Inject chapters data after page loads
                String js = "window.ANDROID_CHAPTERS_DATA = " + chaptersJson + ";"
                    + "if (typeof onDataReady === 'function') onDataReady();";
                view.evaluateJavascript(js, null);
            }
        });

        webView.loadUrl("file:///android_asset/reader.html");
        setContentView(webView);
    }
}
