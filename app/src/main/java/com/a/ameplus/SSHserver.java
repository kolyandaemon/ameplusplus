package com.a.ameplus;

import android.app.Activity;

//ВНИМАНИЕ!!!!!

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


public class SSHserver extends Activity implements View.OnClickListener {


    Button sshbut;
    TextView sshlog;
    AMEplusApplication app;
    SSHThread sshProc;
    SSHAsyncTask sshTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (AMEplusApplication) getApplication();
        setContentView(R.layout.activity_sshserver);
        sshbut=(Button)findViewById(R.id.sshbut);
        sshlog=(TextView)findViewById(R.id.sshlog);

        sshlog.setMovementMethod(new ScrollingMovementMethod());
        sshbut.setOnClickListener(this);
        sshbut.setText("start");

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sshbut:
                if (sshbut.getText()=="start")
                {
                    sshbut.setText("stop");

                    sshServerManager();
                    sshlog.setText("SSH is working\n Port: 2222\n IP Adress: "+IpUtils.getIPAddress(true));
                    sshlog.append("\n\nLogin:root; Password:toor ");


                } else
                    if (sshbut.getText()=="stop")
                {
                    sshbut.setText("start");
                    sshTask.cancel(true);
                    sshlog.setText("SSH is stoped");

                }
                break;
            default:
                break;
        }
    }

    private void sshServerManager() {
        genKeys(true);

        ArrayList<String> coms = new ArrayList<>();

        addToList(coms, app.binDirectory + "/" + "sshd");
        addToList(coms, "-D");
        addToList(coms, "-p", String.format(app.locale, "%d", 2222));
        addToList(coms, "-h", app.sshFileKey);
        addToList(coms, "-o PidFile " + app.sshPidFName);
        addToList(coms, "-f", app.sshdConfFile);
        addToList(coms, "-o StrictModes no");

        String[] scoms = coms.toArray(new String[]{});

        String [] conf={app.nameUser,app.libPath,app.termPath,app.shExecPath,app.nameUser,app.homeDirectory};
        sshProc=new SSHThread(conf,scoms,app.appDir);
       sshTask=new SSHAsyncTask();
        sshTask.execute(sshProc);
    }

    protected <T> void addToList(ArrayList<T> list, T... args) {
        for (T arg : args) {
            list.add(arg);
        }
    }

     boolean genKeys(boolean eraseOld) {
        boolean result = false;
         try {

             String[] types = new String[]{"rsa", "ecdsa", "ed25519"};
             for (String type : types) {
                 if (eraseOld) {
                     app.execCommand("rm -f %s/%s %s/%s.pub", app.sshKeyDirectory, type, app.sshKeyDirectory, type);

                 }


                 if (!(new File(String.format("%s/id_%s", app.sshKeyDirectory, type)).exists())) {
                     String scom = String.format(app.binDirectory + "/ssh-keygen -t %s -f %s/id_%s -Y -q", type, app.sshKeyDirectory, type);
                     String[] comarray = {app.binDirectory + "/ssh-keygen", "-q", "-t", type, "-f", app.sshKeyDirectory + "/id_" + type, "-N", ""};

                     result = app.execCommand(comarray);

                     if (result) {
                         app.execCommand(String.format(app.binDirectory + "/chmod 600 %s/id_%s",app.sshKeyDirectory, type));

                         app.execCommand(String.format(app.binDirectory + "/chmod 644 %s/id_%s.pub", app.sshKeyDirectory, type));

                     }
                 } else {

                 }
             }
         } catch (Exception e) {

             e.printStackTrace();
         }
        return result;
    }


}
