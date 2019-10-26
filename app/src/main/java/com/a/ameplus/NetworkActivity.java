package com.a.ameplus;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NetworkActivity extends Activity implements OnClickListener {

    Button wifiHotSpot;
    Button othWifiHotSpot;
    Button bluethPAN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        wifiHotSpot = (Button) findViewById(R.id.wifi);
        wifiHotSpot.setOnClickListener(this);

        othWifiHotSpot = (Button) findViewById(R.id.wifi2);
        othWifiHotSpot.setOnClickListener(this);

        bluethPAN = (Button) findViewById(R.id.blueth);
        bluethPAN.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.wifi:
                Intent intent = new Intent(this, HotSpotActivity.class);
                startActivity(intent);
                break;
             case   R.id.wifi2:
            Intent intent1 = new Intent(this, WifiActivity.class);
            startActivity(intent1);
            break;
            case   R.id.blueth:
                Intent intent2 = new Intent(this, BluetoothActivity.class);
                startActivity(intent2);
            default:
                break;
        }
    }
}
