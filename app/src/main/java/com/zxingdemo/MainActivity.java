package com.zxingdemo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.zxingdemo.camera.CameraManager;
import com.zxingdemo.decode.CaptureActivityHandler;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, SurfaceHolder.Callback {
    public final static int REQUEST_CAMERA_PERMISSIONS = 1;

    int h = 0;
    @BindView(R.id.capture_preview)
    SurfaceView capturePreview;
    @BindView(R.id.capture_scan_line)
    ImageView captureScanLine;
    @BindView(R.id.capture_crop_layout)
    RelativeLayout captureCropLayout;
    boolean hasSurface;
    QrReceiver receiver;
    private CaptureActivityHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        captureCropLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                h = captureCropLayout.getHeight();
                TranslateAnimation animation = new TranslateAnimation(0, 0, 0, h);
                animation.setRepeatCount(-1);
                animation.setRepeatMode(Animation.RESTART);
                animation.setInterpolator(new LinearInterpolator());
                animation.setDuration(2000);
                captureScanLine.startAnimation(animation);
                captureCropLayout.getViewTreeObserver()
                        .removeGlobalOnLayoutListener(this);
            }
        });
        CameraManager.init(getApplication());
       receiver=new QrReceiver();
        IntentFilter itf=new IntentFilter();
        itf.addAction("QRCODE");
        registerReceiver(receiver,itf);

    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceHolder surfaceHolder=capturePreview.getHolder();
        if(hasSurface){
            openCamera(surfaceHolder);

        }else{
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }
    private void openCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler=new CaptureActivityHandler(this);
        handler.startDecode(handler);
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestCodeQRCodePermissions();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_CAMERA_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需要打开相机和散光灯的权限", REQUEST_CAMERA_PERMISSIONS, perms);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
          if(!hasSurface){
              hasSurface=true;
              openCamera(holder);
          }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
             hasSurface=false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.quitSynchronously();
        CameraManager.get().closeDriver();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);

    }
    class QrReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String result=intent.getStringExtra("result");
            Log.v("二维码/条形码 扫描结果", result);
            Toast.makeText(MainActivity.this,result,Toast.LENGTH_LONG).show();
            // 连续扫描，不发送此消息扫描一次结束后就不能再次扫描
            handler.sendEmptyMessage(R.id.restart_preview);
        }
    }

}
