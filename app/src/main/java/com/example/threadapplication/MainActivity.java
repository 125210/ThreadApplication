package com.example.threadapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btn_start_async, btn_download_async;
    private TextView tvMsg;
    private ProgressBar progressBar;
    private static final String DOWNLOAD_URL = "https://source.unsplash.com/random/1000x600/?race,car";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_start_async = findViewById(R.id.btn_start_async);
        btn_download_async = findViewById(R.id.btn_download_async);
        btn_start_async.setOnClickListener(this);
        btn_download_async.setOnClickListener(this);
        tvMsg = findViewById(R.id.tv_msg);
        progressBar = findViewById(R.id.progress_bar);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_async:
                new MyAsyncTask(this).execute(100);
                break;
            case R.id.btn_download_async:
                new DownloadImage(this).execute(DOWNLOAD_URL);
                break;

        }

    }

    static class MyAsyncTask extends AsyncTask<Integer, Integer, Integer> {
        private WeakReference<AppCompatActivity> ref;

        public MyAsyncTask(AppCompatActivity activity) {
            this.ref = new WeakReference<>(activity);
        }

        // 执行线程任务前的操作
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MainActivity activity = (MainActivity) this.ref.get();
            activity.progressBar.setVisibility(View.VISIBLE);
        }

        // 接收输入参数、执行任务中的耗时操作、返回线程任务执行的结果
        @Override
        protected Integer doInBackground(Integer... params) {
            int sleep = params[0];
            int result = 0;

            for (int i = 0; i < 101; i++) {
                try {
                    Thread.sleep(sleep);
                    result += i;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (i % 5 == 0) {
                    publishProgress(i);
                }

                if (isCancelled()) {
                    break;
                }
            }
            return result;
        }

        // 在主线程中显示线程任务执行的进度
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            MainActivity activity = (MainActivity) this.ref.get();
            activity.progressBar.setProgress(values[0]);
            activity.tvMsg.setText("计算已完成" + values[0] + "%");
        }

        // 接收线程任务执行结果、将执行结果显示到UI组件
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            MainActivity activity = (MainActivity) this.ref.get();
            activity.tvMsg.setText("已计算完成，结果为：" + result);
            activity.progressBar.setVisibility(View.GONE);
        }

        // 将异步任务设置为：取消状态
        @Override
        protected void onCancelled() {
            super.onCancelled();

            MainActivity activity = (MainActivity) this.ref.get();
            activity.tvMsg.setText("计算已取消");

            activity.progressBar.setProgress(0);
            activity.progressBar.setVisibility(View.GONE);
        }

    }

    static class DownloadImage extends AsyncTask<String, Bitmap, Bitmap> {
        private WeakReference<AppCompatActivity> ref;

        public DownloadImage(AppCompatActivity activity) {
            this.ref = new WeakReference<>(activity);
        }



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MainActivity activity = (MainActivity) this.ref.get();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            return downloadImage(url);
        }

        private Bitmap downloadImage(String strUrl) {
            InputStream stream = null;
            Bitmap bitmap = null;

            MainActivity activity = (MainActivity) this.ref.get();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            try {
                URL url = new URL(strUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int totalLen = connection.getContentLength();
                if (totalLen == 0) {
                    activity.progressBar.setProgress(0);
                }

                if (connection.getResponseCode() == 200) {
                    stream = connection.getInputStream();
//                    bitmap = BitmapFactory.decodeStream(stream);

                    int len = -1;
                    int progress = 0;
                    byte[] tmps = new byte[1024];
                    while ((len = stream.read(tmps)) != -1) {
                        progress += len;
                        activity.progressBar.setProgress(progress);
                        bos.write(tmps, 0, len);
                    }
                    bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.size());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return bitmap;
        }
    }
}
