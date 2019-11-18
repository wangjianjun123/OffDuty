package com.study.offduty.ui.activity;

import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.study.offduty.R;
import com.study.offduty.ui.view.FavorLayout;
import com.study.offduty.utils.DateUtil;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wjj
 * Date：2016/12/29  20：00
 * Description：主函数
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tvTimeLeft)
    TextView tvMain;

    @BindView(R.id.tvUnit)
    TextView tvUnit;

    @BindView(R.id.viewFavor)
    FavorLayout viewFavor;

    long timeNow;
    Calendar calendarOff;
    long timeOffWork;
    ShowMode showMode = ShowMode.MINUTE;
    Handler handler = new Handler();

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        timeNow = System.currentTimeMillis();
        calendarOff = Calendar.getInstance();
        String offStr = DateUtil.getDay(timeNow) + " 17:30:00";
        Date tempDate = DateUtil.str2Date(offStr, DateUtil.FORMAT_YMDHMS);
        calendarOff.setTime(tempDate);
        timeOffWork = calendarOff.getTimeInMillis();
        String timeLeftMinutes = getTimeLeft();
        tvMain.setText(timeLeftMinutes);
        tvUnit.setText(R.string.str_min);
        handler.post(runnable);
        tvMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (showMode) {
                    case MINUTE:
                        showMode = ShowMode.SECOND;
                        handler.removeCallbacks(runnable);
                        handler.post(runnable);
                        tvUnit.setText(R.string.str_sec);
                        break;
                    case SECOND:
                    case MILLIS:
                        showMode = ShowMode.MINUTE;
                        handler.removeCallbacks(runnable);
                        handler.post(runnable);
                        tvUnit.setText(R.string.str_min);
                        break;
                }
            }
        });

        //爱心
        tvMain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                viewFavor.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewFavor.addFavor();
                    }
                }, 1000);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    /**
     * 时间刷新runnable
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String timeLeftMinutes = getTimeLeft();
            tvMain.setText(timeLeftMinutes);
            handler.postDelayed(this, 1000);
        }
    };

    /**
     * 获取剩余分钟数
     *
     * @return 分钟数
     */
    private String getTimeLeft() {
        timeNow = System.currentTimeMillis();
        long timeLeft = timeOffWork - timeNow;
        switch (showMode) {
            case MINUTE:
                timeLeft = Long.valueOf(timeLeft / (1000 * 60)).intValue();
                break;
            case SECOND:
                timeLeft = Long.valueOf(timeLeft / 1000).intValue();
                break;
            case MILLIS:
                timeLeft = Long.valueOf(timeLeft).intValue();
        }
        return String.valueOf(timeLeft);
    }

    public enum ShowMode {
        HOUR,
        MINUTE,
        SECOND,
        MILLIS
    }
}
