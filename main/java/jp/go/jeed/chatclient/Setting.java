package jp.go.jeed.chatclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Setting extends AppCompatActivity {
    EditText editName, editServerAddress, editServerPort;
    Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        editName = findViewById(R.id.editName);
        editServerAddress = findViewById(R.id.editServerAddress);
        editServerPort = findViewById(R.id.editServerPort);
        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new BtnClickClass());
    }

    private class BtnClickClass implements View.OnClickListener {
        @Override
        public void onClick(View view){
            Intent intent = new Intent(Setting.this, MainActivity.class);
            intent.putExtra("txtName", editName.getText().toString());
            intent.putExtra("txtServerAddress", editServerAddress.getText().toString());
            intent.putExtra("txtServerPort", editServerPort.getText().toString());
            Setting.this.startActivity(intent);
        }
    }
}