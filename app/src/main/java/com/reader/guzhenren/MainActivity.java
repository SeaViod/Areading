package com.reader.guzhenren;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 48, 32, 32);
        root.setBackgroundColor(Color.parseColor("#f8f6f0"));

        TextView title = new TextView(this);
        title.setText("📖 我的书架");
        title.setTextSize(24);
        title.setTextColor(Color.parseColor("#8b5a2b"));
        title.setPadding(0, 0, 0, 28);
        root.addView(title);

        try {
            InputStream is = getAssets().open("novels_config.json");
            byte[] buf = new byte[is.available()];
            is.read(buf);
            is.close();
            JSONObject config = new JSONObject(new String(buf, StandardCharsets.UTF_8));
            JSONArray novels = config.getJSONArray("novels");

            for (int i = 0; i < novels.length(); i++) {
                JSONObject novel = novels.getJSONObject(i);

                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(28, 24, 28, 24);
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(Color.WHITE);
                bg.setCornerRadius(16);
                card.setBackground(bg);
                
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
                cardParams.bottomMargin = 16;
                card.setLayoutParams(cardParams);

                TextView novelTitle = new TextView(this);
                novelTitle.setText(novel.getString("title"));
                novelTitle.setTextSize(18);
                novelTitle.setTextColor(Color.parseColor("#2c2c2c"));

                TextView novelAuthor = new TextView(this);
                novelAuthor.setText("作者：" + novel.getString("author"));
                novelAuthor.setTextSize(13);
                novelAuthor.setTextColor(Color.parseColor("#999999"));
                novelAuthor.setPadding(0, 6, 0, 0);

                card.addView(novelTitle);
                card.addView(novelAuthor);

                final JSONObject fn = novel;
                card.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(MainActivity.this, ReaderActivity.class);
                        intent.putExtra("novel_title", fn.getString("title"));
                        intent.putExtra("chapters_file", fn.getString("chapters_file"));
                        intent.putExtra("cover_color", fn.getString("cover_color"));
                        startActivity(intent);
                    } catch (Exception ignored) {}
                });

                root.addView(card);
            }
        } catch (Exception e) {
            TextView err = new TextView(this);
            err.setText("加载失败：" + e.getMessage());
            err.setTextColor(Color.RED);
            root.addView(err);
        }

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }
}
