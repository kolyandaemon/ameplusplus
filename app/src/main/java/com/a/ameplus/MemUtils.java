package com.a.ameplus;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MemUtils extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    Button amex;
    Button bootimg;
    Button dump;
    TextView amexlog;
    AMEplusApplication app;
    Spinner spinner;
    String stdout;
    String ipAdress;
    Thread myThread;
    private String nameMemUtils[]={"NEXUS5","I9500","NEXUS4","GALAXY_NEXUS","SM_G3508I"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       app = (AMEplusApplication) getApplication();
       app.installAMExtractor();
        setContentView(R.layout.activity_mem_utils);
        amexlog=(TextView) findViewById(R.id.amexlog);
        stdOut();

        amex=(Button)findViewById(R.id.amex);
        amex.setOnClickListener(this);

        bootimg=(Button)findViewById(R.id.bootimg);
        bootimg.setOnClickListener(this);
        dump=(Button)findViewById(R.id.amexdump);
        dump.setOnClickListener(this);

        ipAdress=IpUtils.getIPAddress(true);


       amexlog.setMovementMethod(new ScrollingMovementMethod());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, nameMemUtils);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

         spinner = (Spinner) findViewById(R.id.spinner2);
        spinner.setAdapter(adapter);

        spinner.setPrompt("Конфигурация");

        spinner.setSelection(0);


        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.amex:

                app.sudo(app.memUtilDirectory+"/"+spinner.getSelectedItem().toString());
                stdOut();
                app.sudo("dmesg\"|grep Hello\"");
                stdOut();
                break;
            case R.id.amexdump:
                amexlog.setText("On computer :\nadb forward tcp:31415 tcp:31415\nnc "+ipAdress+" 31415 > dump");


                app.sudo(app.memUtilDirectory+"/"+spinner.getSelectedItem().toString()+" -d");
                stdOut();
                break;
            case R.id.bootimg:
                amexlog.setText("On computer :\nadb forward tcp:8888 tcp:8888\n nc  "+ipAdress+" 8888 > boot.img");

                app.sudo("dd if=/dev/block/bootdevice/by-name/boot \"| " + app.binDirectory + "/nc -l -p 8888\"");
                stdOut();
                break;
            default:
                break;
        }
    }



    private void stdOut() {
        stdout =Arrays.toString(app.procsStdOut);
        amexlog.append("\n"+stdout);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position)
        {
            case 0:
                amexlog.setText("FLAT_MEM\nSTRUCT_PAGE_SIZE 32\nUSE_SYNC_PTMX");

                break;
            case 1:
                amexlog.setText("FLAT_MEM\nSTRUCT_PAGE_SIZE 36\nUSE_SYNC_PTMX");
                break;
            case 2:
                amexlog.setText("SPARSE_MEM\nSTRUCT_PAGE_SIZE 32\nUSE_SYNC_PTMX");
                break;
            case 3:
                amexlog.setText("FLAT_MEM\nSTRUCT_PAGE_SIZE 32\nUSE_SEEK_ZERO");
                break;
            case 4:
                amexlog.setText("FLAT_MEM\nCONFIG_IOMEM 0xC4C04668\nCONFIG_MEMMAP 0xC4D6C9E0\nSTRUCT_PAGE_SIZE 32\nUSE_SEEK_ZERO");
                break;
                default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }




}
