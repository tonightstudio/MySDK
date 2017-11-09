package com.tonightstudio.sdktestdemo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.tonightstudio.mysdk.DownLoadService;
import com.tonightstudio.mysdk.FileUtils;
import com.tonightstudio.mysdk.SmsContent;
import com.tonightstudio.mysdk.ToastUtils;

import java.io.File;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private SmsContent mSmsContent;
    private ImageView mQrCodeIv;
    private String mQrCodeUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mQrCodeIv = (ImageView) findViewById(R.id.qrcodeIv);

        findViewById(R.id.showToast).setOnClickListener(this);
        findViewById(R.id.throwExpBtn).setOnClickListener(this);
        findViewById(R.id.downloadBtn).setOnClickListener(this);
        findViewById(R.id.testFileUtilsBtn).setOnClickListener(this);
        findViewById(R.id.smsMonitorBtn).setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);

        mSmsContent = new SmsContent(null, this);
        mSmsContent.register();
        mSmsContent.setOnVerifyCodeReceiverListener(new SmsContent.OnVerifyCodeReceiverListener() {
            @Override
            public void OnVerifyCodeReceiver(String verifyCode) {
                Log.e(TAG, "OnVerifyCodeReceiver: " + verifyCode);
            }
        });
        mQrCodeUrl = "https://raw.githubusercontent.com/vc-pai/vc-pai.github.io/master/release/qrcode/personal/cli_300px.png";

        Glide.with(this)
                .load(mQrCodeUrl)
                .into(mQrCodeIv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.showToast:
//                Toast.makeText(this, "我是测试信息", Toast.LENGTH_SHORT).show();
                ToastUtils.showToastWithShort(getApplicationContext(), "我是SDK中的工具");
                break;
            case R.id.throwExpBtn:
                throw new NullPointerException("测试抛异常43634dsfgdsgsdgsdgsgd");
            case R.id.downloadBtn:

                String apkUrl = "https://apkegg.mumayi.com/cooperation/2017/08/25/0/1/mumayishichangMumayiMarket_V4.1_mumayi_8611e.apk";
                DownLoadService.start(this, apkUrl);
                break;
            case R.id.testFileUtilsBtn:
                String externalAppDir = FileUtils.getExternalAppDir(this);
                String crashLogDir = FileUtils.getCrashLogDir(this);
                String prefSetDir = FileUtils.getPrefSetDir(this);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String sdRootDir = Environment.getExternalStorageDirectory().getPath();
                        String oldPath = sdRootDir + "/qrcode.jpg";
                        String newPath = sdRootDir + "/zzzz/qrcode_" + System.currentTimeMillis() + ".jpg";
                        boolean b = FileUtils.copyFile(oldPath, newPath);
                        String imageMIMEType = FileUtils.getImageMIMEType(oldPath);
                        Log.i(TAG, "文件复制结果: " + b);
                    }
                }).start();
                break;
            case R.id.smsMonitorBtn:

                break;
            case R.id.share:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FutureTarget<File> target = Glide.with(MainActivity.this).download(mQrCodeUrl).submit();
                            String path = target.get().getPath();
                            FileUtils.shareImageByIntent(MainActivity.this, path);
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        mSmsContent.unRegister();
        super.onDestroy();
    }
}
