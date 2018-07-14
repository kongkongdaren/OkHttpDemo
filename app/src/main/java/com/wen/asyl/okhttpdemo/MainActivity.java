package com.wen.asyl.okhttpdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    OkHttpClient okHttpClient=new OkHttpClient();
    private TextView mTvGetResult;
    private ImageView mIvPhoto;
    private String mUrl="http://192.168.1.92:8080/OkHttpServer/";
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvGetResult = (TextView) findViewById(R.id.tv_get_result);
        mIvPhoto= (ImageView) findViewById(R.id.iv_photo);
    }
    public void doGetOnClick(View view){
        //1.拿到OkHttpClient的对象
       // OkHttpClient okHttpClient=new OkHttpClient();
        //2.构造Request
      //  Request request=new Request.Builder().get().url(mUrl+"login?username=jwj&password=123").build();
        Request request=new Request.Builder().get().url("https://blog.csdn.net/wen_haha").build();
        //3.将Request封装为Call
        //4.执行call
        execute(request);
    }
    public void doPostOnClick(View view){
        Request.Builder builder=new Request.Builder();
        //构造RequestBody
        RequestBody formBody=new FormBody.Builder().add("username", "jwj").add("password", "123").build();
        Request request = builder.url(mUrl + "login").post(formBody).build();
        execute(request);
    }
    public void doPostStringOnClick(View view){
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), "{username:jwj,password:123}");
        Request.Builder builder=new Request.Builder();
        Request request = builder.url(mUrl + "postString").post(requestBody).build();
        execute(request);
    }
    public void doPostFileOnClick(View view){
        aboutGainPermission();
       // aboutUpFile();
    }
    public void doPostUpOnClick(View view){
       // aboutGainPermission();
        // aboutUpFile();
        File file=new File(Environment.getExternalStorageDirectory(),"123456.png");
        L.e(file.getAbsolutePath().toString());
        if (!file.exists()){
            L.e("不存在!");
            return;
        }
        MultipartBody.Builder multipartBuilder=new MultipartBody.Builder();

        RequestBody requestBody= multipartBuilder.setType(MultipartBody.FORM)
                .addFormDataPart("username", "jwj")
                .addFormDataPart("password", "123456")
                .addFormDataPart("mPhoto", "jwj.png", RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();
        CountingRequestBody countingRequestBody=new CountingRequestBody(requestBody, new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(long byteWrited, long contentLength) {
                L.e(byteWrited+"/"+contentLength);
            }
        });
        Request.Builder builder=new Request.Builder();
        Request request = builder.url(mUrl + "upLoadInfo").post(countingRequestBody).build();
        execute(request);
    }

    public void doDownLoadOnClick(View view){

        //mUrl+"photo/66666.png"
        Request request=new Request.Builder().get().url("http://img.lanrentuku.com/img/allimg/1707/14988864745279.jpg").build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final long total = response.body().contentLength();
                long sum=0L;
                InputStream is = response.body().byteStream();
                int len=0;
                File file=new File(Environment.getExternalStorageDirectory(),"66666.png");
                byte[] buf=new byte[2018];
                FileOutputStream fos=new FileOutputStream(file);
                while ((len=is.read(buf))!=-1){
                    fos.write(buf,0,len);
                    sum+=len;
                    L.e(sum+"/"+total);
                    final long finalSum = sum;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvGetResult.setText(finalSum +"/"+total);
                        }
                    });
                }
                fos.flush();
                fos.close();
                is.close();
                L.e("success");
            }
        });
    }
    public void doDownLoadShowOnClick(View view){
        Request request=new Request.Builder().get().url("http://img.lanrentuku.com/img/allimg/1707/14988864745279.jpg").build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = response.body().byteStream();
                final Bitmap bitmap = BitmapFactory.decodeStream(is);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIvPhoto.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }
    private void aboutUpFile() {
        File file=new File(Environment.getExternalStorageDirectory(),"123456.png");
        L.e(file.getAbsolutePath().toString());
        if (!file.exists()){
            L.e("不存在!");
            return;
        }
        //application/octet-stream
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        Request.Builder builder=new Request.Builder();
        Request request = builder.url(mUrl + "postFile").post(requestBody).build();
        execute(request);
    }
    public void cancelOnClick(View view){
        final Request request = new Request.Builder()
                .url("http://www.baidu.com")
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build();
        Call call = null;
        call = okHttpClient.newCall(request);
        final Call finalCall = call;
        //100毫秒后取消call
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                finalCall.cancel();
            }
        }, 100, TimeUnit.MILLISECONDS);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null != response.cacheResponse()) {
                    String str = response.cacheResponse().toString();
                    L.e("cache---" + str);
                } else {
                    String str = response.networkResponse().toString();
                    L.e("network---" + str);
                }
            }
        });
    }

    private void aboutGainPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            //如果没有就进行申请
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
        }else {
            aboutUpFile();
        }
    }

    private void execute(Request request) {
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                L.e(result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvGetResult.setText(result);
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode==PERMISSION_REQUEST_CODE){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                aboutUpFile();
            }else{
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();

            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
