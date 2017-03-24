package com.example.saber.networktest;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Button btnSendRequest;
    private TextView tvResponse;

    /**
     * 处理子线程中发出的消息
     */
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    tvResponse.setText((String)msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSendRequest = (Button) findViewById(R.id.btn_send_request);
        tvResponse = (TextView) findViewById(R.id.tv_response_text);


        btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendRequestWithHttpURLConnection();

                //用OkHttp做网络请求
                sendRequestWithHttpOkHttp();
            }
        });
    }

    /**
     * OkHttp网络请求
     */
    private void sendRequestWithHttpOkHttp() {

        new Thread(){
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
                    Request request = new Request.Builder()//创建Request对象，并设置网络地址
                        .url("http://www.bilibili.com")
                        .build();

                    Response response = client.newCall(request).execute();//创建一个Call对象，并调用execute()方法获取服务器返回的数据
                    String responseData = response.body().string();//body 得到返回的数据

                    //发送消息
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.obj = responseData;
                    handler.sendMessage(msg);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();



    }


    /**
     * 发送请求,网络操作在工作线程中执行
     */
    private void sendRequestWithHttpURLConnection() {
        new Thread(){
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL("http://www.bilibili.com");
                    connection = (HttpURLConnection) url.openConnection();//获取HttpURLConnection对象
                    connection.setRequestMethod("GET");//设置请求方式
                    connection.setConnectTimeout(8000);//设置超时时间
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();//获取输入流

                    //对获取到的输入流进行读取
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while((line = reader.readLine())!=null){
                        sb.append(line);
                    }

                    //发送消息
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.obj = sb.toString();
                    handler.sendMessage(msg);

                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(reader != null){
                        try{
                            reader.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }

                    if(connection != null){
                        connection.disconnect();
                    }
                }
            }


        }.start();
    }











}
