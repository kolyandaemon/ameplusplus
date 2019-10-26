package com.a.ameplus;


import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;




public class ConnectActivity extends Activity implements View.OnClickListener {

    Button butSsh;
    Button butAdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        butSsh=findViewById(R.id.ssh);
        butSsh.setOnClickListener(this);
        butAdb=findViewById(R.id.adb);
        butAdb.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.adb:
                Intent intent = new Intent(this, AdbActivity.class);
                startActivity(intent);
                break;
            case R.id.ssh:
                Intent intent1 = new Intent(this, SSHserver.class);
                startActivity(intent1);
                break;
            default:
                break;
        }
    }
}

