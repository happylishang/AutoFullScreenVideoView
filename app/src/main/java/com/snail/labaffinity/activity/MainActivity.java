package com.snail.labaffinity.activity;

import android.content.Intent;
import android.os.Bundle;

import com.snail.labaffinity.R;
import com.snail.labaffinity.service.BackGroundService;

import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(MainActivity.this, BackGroundService.class);
        startService(intent);
        first();
    }


    @OnClick(R.id.first)
    void first() {
        Intent intent = new Intent(MainActivity.this, VideoPlayActivity.class);
        startActivity(intent);
    }

}
