package com.study.offduty.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.study.offduty.R;

import butterknife.BindView;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tvTimeLeft)
    TextView tvMain;
    //TODO测试下提交

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
