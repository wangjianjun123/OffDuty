package com.study.offduty.ui.activity;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by wjj
 * Date：2016/12/29  20：00
 * Description：主函数
 */
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    public static final int RC_CAMERA_AND_LOCATION = 0;

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
        methodRequiresTwoPermission();
    }

    /**
     * 初始化
     */
    private void init() {
        ButterKnife.bind(this);
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

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.w(TAG, "onPermissionsGranted: " + perms.toString());
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.w(TAG, "onPermissionsDenied: " + perms.toString());
        String[] strings = new String[perms.size()];
        perms.toArray(strings);
        EasyPermissions.requestPermissions(MainActivity.this, "请允许程序获取必要的运行权限", RC_CAMERA_AND_LOCATION, strings);
    }

    public enum ShowMode {
        HOUR,
        MINUTE,
        SECOND,
        MILLIS
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
    private void methodRequiresTwoPermission() {
        String[] perms = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            init();
        } else {
            EasyPermissions.requestPermissions(this, "请允许程序获取必要的运行权限", RC_CAMERA_AND_LOCATION, perms);
        }
    }
}
