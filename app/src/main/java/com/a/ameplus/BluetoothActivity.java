package com.a.ameplus;

import android.content.Intent;
import android.provider.Settings;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class BluetoothActivity extends Activity implements View.OnClickListener {

    Button buttSetting;
    Button  butNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        buttSetting=findViewById(R.id.bluesetting);
        buttSetting.setOnClickListener(this);
        butNext=findViewById(R.id.buttonNext);
        butNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (R.id.bluesetting ==v.getId())
        {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.TetherSettings");
            startActivity(intent);
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
        }

        if (R.id.buttonNext==v.getId())
        {
            Intent intent = new Intent(this, ConnectActivity.class);
            startActivity(intent);
        }


    }




}
