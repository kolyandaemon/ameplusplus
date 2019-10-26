package com.a.ameplus;



import android.content.Intent;
import android.provider.Settings;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class WifiActivity extends Activity implements View.OnClickListener {

    Button butSetting;
    Button  butNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        butSetting=findViewById(R.id.wifisetting);
        butSetting.setOnClickListener(this);
        butNext=findViewById(R.id.buttonNext);
        butNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (R.id.wifisetting ==v.getId())
        {

          startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }

        if (R.id.buttonNext==v.getId())
        {
            Intent intent = new Intent(this, ConnectActivity.class);
            startActivity(intent);
            this.onDestroy();
        }

    }
}
