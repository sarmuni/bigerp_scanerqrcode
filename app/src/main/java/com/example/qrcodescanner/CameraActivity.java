package com.example.qrcodescanner;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import org.jetbrains.annotations.NotNull;

public class CameraActivity extends AppCompatActivity {

    private CodeScanner mCodeScanner;
    boolean CameraPermission = false;
    final int CAMERA_PERM = 1;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this,scannerView);

        askPermission();
        if (CameraPermission) {
            scannerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCodeScanner.startPreview();
                }
            });
            mCodeScanner.setDecodeCallback(new DecodeCallback() {
                @Override
                public void onDecoded(@NonNull @NotNull Result result) {
                    runOnUiThread(new Runnable() {
                        @SuppressLint("SetJavaScriptEnabled")
                        @Override
                        public void run() {
//                            Toast.makeText(MainActivity.this, result.getText(), Toast.LENGTH_LONG).show();

                            String data1 = result.getText();
                            byte[] encode = Base64.encode(data1.getBytes(), Base64.DEFAULT);
                            String url = ("http://192.168.1.5/qrcode_web/scan.php?id=" + new String(encode));

                            setContentView(R.layout.activity_webview);
                            WebView web = (WebView) findViewById(R.id.web);
                            web.getSettings().setJavaScriptEnabled(true);
                            web.setWebViewClient(new MyBrowser());
                            web.loadUrl(url);
                        }
                    });

                }
            });
        }
    }

    private static class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView web, String url) {
            web.loadUrl(url);
            return true;
        }
    }


    @SuppressLint("ObsoleteSdkInt")
    private void askPermission(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ){
                ActivityCompat.requestPermissions(CameraActivity.this,new String[]{Manifest.permission.CAMERA},CAMERA_PERM);
            }else {
                mCodeScanner.startPreview();
                CameraPermission = true;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        if (requestCode == CAMERA_PERM){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mCodeScanner.startPreview();
                CameraPermission = true;
            }else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)){
                    new AlertDialog.Builder(this)
                            .setTitle("Permission")
                            .setMessage("Please provide the camera permission for using all the features of the app")
                            .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(CameraActivity.this,new String[]{Manifest.permission.CAMERA},CAMERA_PERM);
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                }else {
                    new AlertDialog.Builder(this)
                            .setTitle("Permission")
                            .setMessage("You have denied some permission. Allow all permission at [Settings] > [Permissions]")
                            .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package",getPackageName(),null));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }).setNegativeButton("No, Exit app", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).create().show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onPause() {
        if (CameraPermission){
            mCodeScanner.releaseResources();
        }
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (mCodeScanner != null){
            mCodeScanner.startPreview();
        }
    }




}
