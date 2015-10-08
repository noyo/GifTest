package com.practice.noyet.giftest;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    private MyGiftView gif1;
    private MyGiftView gif2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        gif1 = (MyGiftView) findViewById(R.id.gif1);
//        gif2 = (GiftView) findViewById(R.id.gif2);
//        // 设置背景gif图片资源
//        gif1.setMovieResource(R.raw.gift1);
//        gif2.setMovieResource(R.raw.gift2);
        // 设置暂停
        // gif2.setPaused(true);

    }

}
