package com.a.ameplus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HotSpotActivity extends Activity  implements View.OnClickListener {

    Button butNext;
    Button butEnable;
    TextView textLogin;
    TextView textPassword;
    TextView textlog;
    HotSpotManager hotSpotTool;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_spot);

        butNext = findViewById(R.id.buttonNext);
        butNext.setOnClickListener(this);

        butEnable = findViewById(R.id.buttonEnable);
        butEnable.setOnClickListener(this);

        textLogin=findViewById(R.id.login);
        textlog=findViewById(R.id.textlog);
        textPassword=findViewById(R.id.pasword);

        hotSpotTool=new HotSpotManager(HotSpotActivity.this);

        textLogin.setText(textLogin.getText()+hotSpotTool.hotName);
        textPassword.setText(textPassword.getText()+hotSpotTool.hotPass);

        state();


    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.buttonEnable:

                if (!hotSpotTool.stateWifi()){

                    butEnable.setText("Disable");
                    hotSpotTool.configStateWifi();
                    state();

                }
                else
                {
                    state();
                    hotSpotTool.configStateWifi();

                    butEnable.setText("Enable");
                }
                break;
            case R.id.buttonNext:
                    Intent intent = new Intent(this, ConnectActivity.class);
                    startActivity(intent);
                break;
            default:
                break;
        }

    }

    void state()
    {
        if (!hotSpotTool.stateWifi())
        {
            textlog.setText("Wifi Hotspot is ON");
        }else
        {
            textlog.setText("Wifi HotSpot is OFF");
        }
    }
}
