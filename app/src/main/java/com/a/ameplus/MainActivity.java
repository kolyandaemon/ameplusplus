package com.a.ameplus;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;


import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.view.View.OnClickListener;

import java.util.Arrays;


public  class MainActivity extends Activity implements OnClickListener{

    AMEplusApplication app;
    MainActivity activity;
    private TextView textView;
    private Button buttonNext;
    private Button getRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (AMEplusApplication) getApplication();
        activity = this;
        app.activity = this;
        setContentView(R.layout.activity_main);
        textView=findViewById(R.id.textw);
        buttonNext=(Button)findViewById(R.id.bstart);
        buttonNext.setOnClickListener(this);
        getRoot=(Button)findViewById(R.id.butroot);
        getRoot.setOnClickListener(this);
        rootCheck();

        Switch airplaneSwitch = (Switch) findViewById(R.id.switch3);
        Switch brigthnessSwitch = (Switch) findViewById(R.id.switch2);

        Switch sleepSwitch=(Switch) findViewById(R.id.switch4);

        app.sudo("settings put system accelerometer_rotation 0");

        airplaneSwitch.setOnCheckedChangeListener(new  CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    app.sudo("settings put global airplane_mode_on 1");
                    app.sudo("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true");
                    outStr("Режим полета включен");
                } else {
                    app.sudo("settings put global airplane_mode_on 0");
                    app.sudo("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false");
                    outStr("Режим полета выключен");
                }
            }
        });

        sleepSwitch.setOnCheckedChangeListener(new  CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    app.sudo("settings put system screen_off_timeout 999999999");
                    app.sudo("svc power stayon true");
                    outStr("Режим сна выключен");
                } else {
                    app.sudo("settings put system screen_off_timeout 6000");
                    app.sudo("svc power stayon ac");
                    outStr("Режим сна включен");
                }
            }
        });

        brigthnessSwitch.setOnCheckedChangeListener(new  CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    app.sudo("settings put system screen_brightness_mode 0");
                    app.sudo("settings put system screen_brightness 1");
                    outStr("яркость экрана 10%");
                } else {
                    app.sudo("settings put system screen_brightness_mode 1");
                    app.sudo("settings put system screen_brightness 150");
                    outStr("Яркость экрана 58%");
                }
            }
        });
    }

    private void rootCheck() {
        if (RootUtil.isRoot()==true)
        {
            outStr("root is available");
        }
        else
            outStr("root is NOT available\n You can get root in Options Menu");
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bstart) {
                Intent intent = new Intent(this, NetworkActivity.class);
            startActivity(intent);

        }
        if (v.getId()==R.id.butroot)
        {
            app.getRoot();

                app.sudo(app.appDir+"/getroot/script.sh");
                outStr(Arrays.toString(app.procsStdOut));
        }
    }
    public void outStr(String str)
    {
        textView.setText(textView.getText()+"\n"+str);
    }


}
