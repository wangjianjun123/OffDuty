package com.study.offduty.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.pgyersdk.update.DownloadFileListener;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.pgyersdk.update.javabean.AppBean;
import com.study.offduty.R;
import com.study.offduty.ui.view.FavorLayout;
import com.study.offduty.utils.DateUtil;

import java.io.File;
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
        checkUpdate();
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

    /**
     * 检查更新
     */
    public void checkUpdate() {

        //TODO ProgressDialog过时，下载对话框替换
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("提示");
        progressDialog.setMessage("下载中");
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        new PgyUpdateManager.Builder()
                .setForced(false)
                .setUserCanRetry(true)
                .setUpdateManagerListener(new UpdateManagerListener() {
                    @Override
                    public void onNoUpdateAvailable() {
                        Log.w(TAG, "onNoUpdateAvailable");
                    }

                    @Override
                    public void onUpdateAvailable(final AppBean appBean) {
                        Log.w(TAG, "onUpdateAvailable");

                        new AlertDialog.Builder(MainActivity.this).setIcon(R.mipmap.ic_launcher)
                                .setTitle("提示")
                                .setMessage("有新的更新可用，是否立即更新")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PgyUpdateManager.downLoadApk(appBean.getDownloadURL());
                                        progressDialog.show();
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create().show();
                    }

                    @Override
                    public void checkUpdateFailed(Exception e) {
                        Log.w(TAG, "checkUpdateFailed");
                        Toast.makeText(MainActivity.this, "检查更新失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setDownloadFileListener(new DownloadFileListener() {
                    @Override
                    public void downloadFailed() {
                        Log.w(TAG, "downloadFailed");
                    }

                    @Override
                    public void downloadSuccessful(File file) {
                        Log.w(TAG, "downloadSuccessful");
                        progressDialog.dismiss();
                        PgyUpdateManager.installApk(file);
                    }

                    @Override
                    public void onProgressUpdate(Integer... args) {
                        Log.w(TAG, "onProgressUpdate：" + args[0]);
                        progressDialog.setProgress(args[0]);

                    }
                })
                .register();
    }
}
