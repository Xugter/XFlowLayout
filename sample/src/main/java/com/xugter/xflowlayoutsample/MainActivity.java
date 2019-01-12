package com.xugter.xflowlayoutsample;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xugter.xflowlayout.XFlowLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TagAdapter adapter;

    private List<String> content = new ArrayList<>();

    private XFlowLayout xFlowLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        xFlowLayout = findViewById(R.id.flow_layout);
        adapter = new TagAdapter();
        xFlowLayout.setAdapter(adapter);
        initData();
        adapter.setContent(content);
//        xFlowLayout.setMaxLine(3);
        xFlowLayout.setClickListener(new XFlowLayout.ClickListener() {
            @Override
            public void onClickOnPos(int pos) {
                Log.i("XFlowLayout", "============Item " + pos + " click");
            }
        });
    }

    private void initData() {
        for (int i = 0; i < 10; i++) {
            content.add("aaaaaaaaaaaaaa");
            content.add("bbbbbbbbbbbbb");
            content.add("cccccc");
            content.add("dddddddddd");
            content.add("ffffffffffffffffffff");
            content.add("ggggggggg");
        }
    }

    class TagAdapter extends XFlowLayout.Adapter {

        private List<String> content = new ArrayList<>();

        public void setContent(List<String> content) {
            this.content = content;
            notifyDataChanged();
        }

        @Override
        public int getItemCount() {
            return content.size();
        }

        @Override
        public View getItemViewByPos(int pos) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 10, 10, 10);

            if (pos % 5 == 0) {
                ImageView imageView = new ImageView(MainActivity.this);
                imageView.setImageResource(R.mipmap.ic_launcher);
                imageView.setLayoutParams(layoutParams);
                return imageView;
            }

            TextView tv = new TextView(MainActivity.this);
            tv.setPadding(30, 5, 30, 5);
            tv.setText(content.get(pos));
            tv.setTextSize(20);
            tv.setMaxEms(10);
            tv.setSingleLine();
            tv.setLayoutParams(layoutParams);
            tv.setBackgroundResource(R.drawable.bg_text_tag);
            if (pos % 3 == 0) {
                tv.setTextSize(30);
                tv.setTextColor(Color.RED);
            } else if (pos % 3 == 1) {
                tv.setTextColor(Color.BLUE);
            }
            return tv;
        }
    }
}
