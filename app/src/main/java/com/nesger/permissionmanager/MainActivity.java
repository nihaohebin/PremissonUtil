package com.nesger.permissionmanager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.nesger.permission.PermissionHelper;
import com.nesger.permission.callback.OnPermissionCallback;

public class MainActivity extends AppCompatActivity {

    private PermissionHelper permissionHelper;
    //值唯一即可,这是为了返回做标识使用
    private final int REQUEST_SETTING = 10;

    private final String[] permissionArrays = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA};

    private final String[] permissionInfo = {"存储", "短信", "定位", "电话", "相机"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        doPermissionCheck();
    }

    /**
     * 检查是否拥有权限
     */
    private void doPermissionCheck() {
        permissionHelper = PermissionHelper.getInstance(this, new OnPermissionCallback() {
            @Override
            public void onPermissionGranted(@NonNull String[] permissionName) {
                //权限点击允许
                String lastPermission = permissionName[permissionName.length - 1];
                if (lastPermission.equals(permissionArrays[permissionArrays.length - 1])) {
                    //权限点击允许
                    premissGrantedSuccess();
                } else {
                    doPermissionCheck();
                }
            }

            @Override
            public void onPermissionDeclined(@NonNull String[] permissionName) {

            }

            @Override
            public void onPermissionPreGranted(@NonNull String permissionsName) {
                //权限已经打开了
                premissGrantedSuccess();
            }

            @Override
            public void onPermissionNeedExplanation(@NonNull String permissionName) {
                //需要申请权限
                permissionHelper.requestAfterExplanation(permissionName);
            }

            @Override
            public void onPermissionReallyDeclined(@NonNull String permissionName) {
                boolean admitAppend = false;
                StringBuilder sb = new StringBuilder();
                for (int i = 0, length = permissionArrays.length; i < length; i++) {
                    if (permissionArrays[i].equals(permissionName) || admitAppend) {
                        if (i < permissionInfo.length) {
                            sb.append(permissionInfo[i]).append(",");
                            admitAppend = true;
                        }
                    }
                }

                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("提示")
                        .setMessage("请开启" + sb.toString().substring(0,sb.toString().lastIndexOf(",")) + "权限!")
                        .setCancelable(false)
                        .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //禁止权限
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.parse("package:" + getPackageName());
                                intent.setData(uri);
                                startActivityForResult(intent, REQUEST_SETTING);
                            }
                        }).setNegativeButton("退出应用", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create().show();
            }

            @Override
            public void onNoPermissionNeeded() {
                premissGrantedSuccess();
            }
        })
                .setForceAccepting(true)
                .request(permissionArrays);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        permissionHelper.onActivityForResult(requestCode);
        //返回时重新进行检查
        if (requestCode == REQUEST_SETTING) {
            doPermissionCheck();
        }
    }

    /**
     * 记得手动重写这个方法
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (null != permissionHelper) {
            permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void premissGrantedSuccess() {
        Toast.makeText(this, "权限全部打开完毕", Toast.LENGTH_SHORT).show();
    }
}
