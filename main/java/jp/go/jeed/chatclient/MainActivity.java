package jp.go.jeed.chatclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    EditText editSendMessage;
    Button btnSend;
    LinearLayout llMessage;
    ScrollView ssMessage;
    Handler handler = new Handler();

    Socket sock;
    InetSocketAddress isa;
    BufferedReader sockin;
    BufferedWriter sockout;
    boolean isPlay = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            //バージョン表示
            case R.id.itemAbout:
                int versionCode = 0;
                String versionName = "";
                PackageManager packageManager = this.getPackageManager();
                try{
                    PackageInfo packageInfo = packageManager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
                    versionCode = packageInfo.versionCode;
                    versionName = packageInfo.versionName;
                }catch(PackageManager.NameNotFoundException e){
                    Log.e("Error", e.toString());
                }
                AlertDialog ald = new AlertDialog.Builder(this)
                        .setTitle("バージョン情報")
                        .setMessage("VersionCode=" + versionCode + "\nVersion=" + versionName)
                        .setPositiveButton("OK", null)
                        .create();
                ald.show();
                break;
            //設定画面に戻る
            case R.id.itemSetting:
                Thread th = new Thread(new SocketStopClass());
                th.start();
                Intent intent = new Intent(MainActivity.this, Setting.class);
                MainActivity.this.startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //部品の紐づけ
        editSendMessage = findViewById(R.id.editSendMessage);
        llMessage = findViewById(R.id.llMessage);
        ssMessage = findViewById(R.id.ssMessage);
        btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new BtnClickClass());

        //設定画面から受け取ったデータを使ってソケット接続
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Thread th = new Thread(new SocketStartClass(bundle.getString("txtServerAddress"), bundle.getString("txtServerPort"), bundle.getString("txtName")));
        th.start();
    }

    //ソケット接続用クラス
    private class SocketStartClass implements Runnable {

        String serverAddress, name;
        int serverPort;

        SocketStartClass(String serverAddress, String serverPort, String name){
            this.serverAddress = serverAddress;
            this.serverPort = Integer.parseInt(serverPort);
            this.name = name;
        }

        @Override
        public void run() {
            try {
                sock = new Socket();
                isa = new InetSocketAddress(serverAddress, serverPort);
                sock.connect(isa, 1000);
                sockin = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
                sockout = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"));

                sockout.write(name);
                sockout.newLine();
                sockout.flush();

                Thread th = new Thread(new ReceiveMessage());
                th.start();

                isPlay = true;
                updateSelfLog(new MessageClass("接続しました", "System"));
            } catch (Exception ex) {
                //updateLog(ex.getMessage());
            }
        }
    }

    //ソケット切断用クラス
    private class SocketStopClass implements Runnable {
        @Override
        public void run() {
            try {
                isPlay = false;
                sock.shutdownOutput();
                sock.shutdownInput();
                sock.close();
                updateSelfLog(new MessageClass("切断しました", "System"));
            } catch (Exception ex) {
                //updateLog(ex.getMessage());
            }
        }
    }

    //送信ボタンを押したときの処理
    private class BtnClickClass implements View.OnClickListener{
        @Override
        public void onClick(View view){
            if(view.getId() == R.id.btnSend){
                Thread th = new Thread(new SendMessage());
                th.start();
                editSendMessage.setText("");
                editSendMessage.requestFocus();
            }
        }
    }

    //送信処理用クラス
    private class SendMessage implements Runnable {
        @Override
        public void run() {
            try {
                sockout.write(editSendMessage.getText().toString());
                sockout.newLine();
                sockout.flush();
                updateSelfLog(new MessageClass(editSendMessage.getText().toString(), "自分"));
            } catch (Exception ex) {
                //updateLog(ex.getMessage());
            }
        }
    }

    //受信処理用クラス
    private class ReceiveMessage implements Runnable {
        @Override
        public void run() {
            String msg;
            while(isPlay) {
                try {
                    updateLog(new MessageClass(sockin.readLine(), sockin.readLine(), sockin.readLine()));
                }catch(Exception ex) {
                    isPlay = false;
                }
            }
        }
    }

    //左側のメッセージ表示
    private void updateLog(MessageClass mc){
        handler.post(new Runnable(){
            public void run() {
                TextView name = new TextView(MainActivity.this);
                name.setText(mc.name + "(" + mc.date + ")");
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.rightMargin = 512;
                llMessage.addView(name, lp);

                Button bt = new Button(MainActivity.this);
                bt.setBackgroundResource(R.drawable.buttondeco);
                bt.setText(mc.msg);
                bt.setTextColor(Color.BLUE);
                bt.setTextSize(24);
                bt.setAllCaps(false);
                llMessage.addView(bt, lp);
                ssMessage.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    //右側のメッセージ表示
    private void updateSelfLog(MessageClass mc){
        handler.post(new Runnable(){
            public void run() {
                TextView name = new TextView(MainActivity.this);
                name.setText(mc.name + "(" + mc.date + ")");
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.leftMargin = 512;
                lp.gravity = Gravity.RIGHT;
                llMessage.addView(name, lp);

                Button bt = new Button(MainActivity.this);
                bt.setBackgroundResource(R.drawable.buttondecoself);
                bt.setText(mc.msg);
                bt.setTextColor(Color.WHITE);
                bt.setTextSize(24);
                bt.setAllCaps(false);
                llMessage.addView(bt, lp);
                ssMessage.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}