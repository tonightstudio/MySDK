package com.tonightstudio.sdktestdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import group.tonight.ToastUtils;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.showToast).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.showToast:
//                Toast.makeText(this, "我是测试信息", Toast.LENGTH_SHORT).show();
                ToastUtils.showToastWithShort(getApplicationContext(), "我是SDK中的工具");
                break;
            default:
                break;
        }
    }
}
