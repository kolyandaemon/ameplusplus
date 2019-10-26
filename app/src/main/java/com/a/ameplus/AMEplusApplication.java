package com.a.ameplus;


import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


public final class AMEplusApplication extends Application {

    boolean DEBUG = true;
    
    String[] procStdOut = null;
    protected Locale locale;
    Configuration sysConf;
    boolean setupFinished=false;
    
    MainActivity activity = null;
    private static AMEplusApplication singleton;
    int perm = 7;

    String appDir = null;
    File appFileBase = null;
    File binFileBase = null;
    String binDirectory = null;
    String binDirectoryName = "bin";
    String libDirectory = null;
    String libDirectoryName = "lib";
    String libexecDirectory = null;
    String libexecDirectoryName="libexec";
    String shExecPath = null;
    String memUtilDirectory=null;
    String homeDirectoryName = "home";
    String homeDirectory = null;
    String tmpDirectory = null;
    String devDirectory = null;
    String varDirectory = null;
    String configEnvFile=null;
    String sshKeyDirectoryName = ".ssh";
    String sshKeyDirectory = null;
    String sshFileKey=null;
    String sshEtcDir=null;
    String sshdConfFile=null;
    String sshdConfName = null;
    String sshPidFName = null;
    String[] procsStdOut = null;
    String[] procsStdErr = null;

    ConnectivityManager connectManage = null;
    boolean installed = false;
    Runtime execRun = null;


    TreeMap<String, String> sourceEnvir = null;
    String rootPath;
    String nameUser = "";
    String nameBoard = "";
    String libPath = "";
    String termPath = "";
    String nameDevice = "";


    public AMEplusApplication getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        Resources r = this.getResources();
        execRun = Runtime.getRuntime();
        try {
            appDir = getFilesDir().getParentFile().toString();
            String init = "/system/lib";
            if (new File(init).exists()) {
                init += ":";
            } else {
                init = "";
            }
            Log.d("MY_T", appDir);
            libPath = init + appDir + "/lib";
            termPath = libPath + "/termPath";
            locale = getResources().getConfiguration().locale;
            Log.d("MY_T", libPath);
            Log.d("MY_T", termPath);

            sourceEnvir = new TreeMap<>(System.getenv());
            sysConf = getResources().getConfiguration();

            connectManage = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            nameDevice = Build.MODEL;

            BluetoothAdapter miDevice = BluetoothAdapter.getDefaultAdapter();
            if (miDevice != null) {
                String dn = miDevice.getName();
                if (dn != null) {
                    nameDevice = dn;
                }
            }

            nameDevice = nameDevice.replaceAll(" ", "_");
            homeDirectory = pathBuild(appDir, homeDirectoryName);
            tmpDirectory = pathBuild(appDir, "tmp");
            varDirectory = pathBuild(appDir, "var");
            devDirectory = pathBuild(appDir, "dev");
            sshKeyDirectory = pathBuild(homeDirectory, sshKeyDirectoryName);
            binDirectory = pathBuild(appDir, binDirectoryName);
            libDirectory = pathBuild(appDir, libDirectoryName);
            libexecDirectory = pathBuild(appDir, libexecDirectoryName);
            rootPath = String.format("%s:%s", binDirectory, sourceEnvir.get("PATH"));
            shExecPath = pathBuild(binDirectory, "bash");
            sshFileKey=pathBuild(sshKeyDirectory,"id_rsa");
            sshEtcDir = pathBuild(appDir, "etc");
            sshPidFName = pathBuild(sshEtcDir, "sshd.pid");
            sshdConfName = "sshd_config";
            sshdConfFile = pathBuild(sshEtcDir, sshdConfName);
            configEnvFile=pathBuild(sshKeyDirectory,"environment");

            nameBoard = Build.BOARD;

            boolean res = execCommand(binDirectory + "/id");
            if (res && procStdOut != null && procStdOut.length > 0) {
                String id = procStdOut[0];
                nameUser = id.replaceFirst("(?i).*?\\((.*?)\\).*", "$1");
            }

            instllProcess();
        } catch (Exception e) {

            e.printStackTrace();
        }
        installed = true;
    }

    protected String pathBuild(String parent, String child) {
        return String.format("%s/%s", parent, child);
    }

    private void dirCreate(String path, int perms) {

        File f = new File(path);
        if (!f.exists()) {
            boolean result = f.mkdirs();

            boolean ownerOnly = (perms & 8) == 0;
            f.setExecutable((perms & 1) != 0, ownerOnly);
            f.setWritable((perms & 2) != 0, ownerOnly);
            f.setReadable((perms & 4) != 0, ownerOnly);
        }
    }


    private void instllProcess() {
        Thread it = new Thread() {
            @Override
            public void run() {
                File reinstall = new File(homeDirectory + "/REINSTALL");


                if (reinstall.exists() || DEBUG) {
                    eraseDataApp(appDir);
                    dirCreate(appDir, perm);

                    appFileBase = new File(appDir);
                    binFileBase = new File(binDirectory);
                    dirCreate(homeDirectory, perm);
                    dirCreate(tmpDirectory, perm);
                    dirCreate(binDirectory, perm);
                    dirCreate(libexecDirectory,perm);
                    dirCreate(varDirectory,perm);
                    dirCreate(devDirectory,perm);
                    dirCreate(sshKeyDirectory, perm);
                    dirCreate(libDirectory,perm);

                    installRess();
                    instBusybox();
                    makeSysSymclinks();
                    buildEnvironmentFile();

                    execCommand(binDirectory + "/chmod 777 " + homeDirectory);
                    execCommand(binDirectory + "/chmod 777 " + tmpDirectory);
                    execCommand(binDirectory + "/chmod 755 " + varDirectory);
                    execCommand(binDirectory + "/chmod 640 " + sshdConfFile);
                    execCommand(binDirectory + "/chmod 700 " + sshKeyDirectory);
                    if (DEBUG) {
                        execCommand("chmod 777 " + appDir);
                        execCommand("chmod 777 " + homeDirectory);
                        execCommand("chmod 777 " + binDirectory);
                        execCommand("chmod 777 " + sshEtcDir);
                    }

                }
                postInstall();
            }
        };
        it.start();
    }

    private void eraseDataApp(String base) {

        delFilesDirecs(new File(base));
    }

    private void delFilesDirecs(File file) {
        try {
            if (file != null) {
                if (file.isDirectory()) {
                    String[] list = file.list();
                    if (list != null) {
                        for (String child : list) {
                            File target = new File(file,child);

                            delFilesDirecs(target);
                        }
                    }
                }
            }
            boolean result = file.delete();
        } catch (Exception e) {

        }
    }

    private void installRess() {

        installRess("resources/arm", appDir);
        installRess("resources/all", appDir);
       
    }

    private void installRess(String spath, String dpath) {
        try {

            String[] list = getAssets().list(spath);
            if (list.length > 0) {
                for (String item : list) {
                    String sspath = spath + "/" + item;
                    String ddpath = dpath + "/" + item;

                    if (getAssets().list(sspath).length > 0) {

                        dirCreate(ddpath, perm);
                        installRess(sspath, ddpath);
                    } else {
                        if (!item.equals("placeholder"))
                        {
                            installRes(sspath, ddpath, item);
                        }

                    }
                }
            }
        } catch (Exception e) {

        }
    }

    private void installRes(String ipath, String dpath, String item) {
        try {
            if (item.matches("profile|login")) {
                item = "." + item;
                dpath = homeDirectory + "/" + item;
            }
            File out = new File(dpath);
            installRes(ipath, out);
            out.setReadable(true, false);

            out.setExecutable(true, false);


        } catch (Exception e) {


        }
    }

    protected void installRes(String ipath, File out) {
        try {

            OutputStream os = new FileOutputStream(out);
            InputStream ir = getAssets().open(ipath);
            int segSize = 32768;
            int len;
            byte[] buffer = new byte[segSize];
            while ((len = ir.read(buffer, 0, segSize)) != -1) {
                os.write(buffer, 0, len);
            }
            os.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    protected boolean execCommand(String com) {

        String[] coms = com.split(" +");
        return execCommand(coms);
    }

    protected boolean execCommand(final String... com) {
        boolean result = false;
        try {

            String disp = "";
            for (String s : com) {
                if (disp.length() > 0) {
                    disp += ",";
                }
                disp += "\"" + s + "\"";
            }


            ProcessBuilder pb = new ProcessBuilder(com);

            pb.redirectErrorStream(true);

            Map<String, String> env = pb.environment();
           
            env.put("LD_LIBRARY_PATH", "/data/data/com.a.ameplus/lib");
            env.put("termPath", termPath);


            Process p = pb.start();

            result = processMonitor(p);
            if (p != null) {
                p.destroy();
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        return result;
    }
    protected boolean processMonitor(Process p) {
        boolean result = false;
        ArrayList<String> listStdOut = new ArrayList<>();
        ArrayList<String> listStdErr = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;

            boolean active = true;
            while (active) {
                active = false;
                if ((line = in.readLine()) != null) {

                    listStdOut.add(line);
                    active = true;
                }
                if ((line = err.readLine()) != null) {

                    listStdErr.add(line);
                    active = true;
                }
            }
            p.waitFor();
            in.close();
            err.close();

            result = p.exitValue() == 0;
            p.destroy();

        } catch (Exception ignored) {


        }

        procsStdOut = listStdOut.toArray(new String[]{});
        procsStdErr = listStdErr.toArray(new String[]{});
        return result;
    }

    private void postInstall() {
        installed = true;
        setupFinished=true;
    }

    protected void buildEnvironmentFile() {

        int offset = new GregorianCalendar().getTimeZone().getOffset(System.currentTimeMillis());
        int tz = offset / (1000 * 3600);
        StringBuilder sb = new StringBuilder();
        sourceEnvir.put("AMEPLUS", appDir);
        sourceEnvir.put("LD_LIBRARY_PATH", libPath);
        sourceEnvir.put("termPath", termPath);
        sourceEnvir.put("TZ", String.format(locale, "GMT%d", -tz));
        sourceEnvir.put("PATH", rootPath);
        sourceEnvir.put("ENV", homeDirectory + "/.profile");
        sourceEnvir.put("USER", nameUser);
        sourceEnvir.put("BOARD", nameBoard);
        String osdata = System.getProperty("os.version");
        sourceEnvir.put("OSDATA", nameDevice + ":" + osdata);

        for (String key : sourceEnvir.keySet()) {
            String arg = String.format("%s=%s\n", key, sourceEnvir.get(key));
            sb.append(arg);
        }

        writeTextFile(configEnvFile, sb.toString());
    }

    protected void writeTextFile(String path, String content) {
        try {
            FileWriter fw = new FileWriter(path);
            fw.write(content);
            fw.flush();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void instBusybox() {

        try {
            String com = String.format("%s/busybox --install -s %s", binDirectory,binDirectory );
            execCommand(com);
            Log.d("MY_T", com);
        } catch (Exception e) {

            Log.d("MY_T", "eroor");
        }
    }

    private void makeSysSymclinks() {
        File clink = new File(homeDirectory, "SDCard");
        symclinkProc(new File(Environment.getExternalStorageDirectory().getPath()), clink, true);

        clink = new File(appDir + "/files/usr/bin");
        symclinkProc(new File(binDirectory), clink, true);

        clink = new File(appDir + "/files/usr/etc/ssh");
        symclinkProc(new File(binDirectory + "/ssh"), clink, true);

        clink = new File(appDir + "/files/home");
        symclinkProc(new File(appDir +"/home"), clink, true);

        clink = new File(appDir + "/files/lib");
        symclinkProc(new File(libDirectory), clink, true);
    }

    private boolean symclinkProc(File src, File clink, boolean create) {
        boolean success = false;
        try {
            if (create) {
                if (!clink.exists()) {
                    String com = "ln -s " + src + " " + clink;

                    Process p = execRun.exec(com);
                    p.waitFor();
                    success = (p.exitValue() == 0);
                    p.destroy();


                }
            } else {
                if (clink.exists()) {
                    Process p = execRun.exec("rm " + clink);
                    p.waitFor();
                    success = (p.exitValue() == 0);
                    p.destroy();
                }
            }
        } catch (Exception ignored) {

        }
        return success;
    }

    public boolean sudo(String com)
    {
        String exec="su -c "+"sh "+binDirectory+"/su.sh "+"\""+com+"\"";
        Log.d("MY_T", exec);
        return execCommand(exec);
    }

    public void getRoot()
    {
        installRess("resources/getroot",appDir);
        sudo(binDirectory + "/chmod 777 " + appDir+"/getroot"+"/busybox");
        sudo(binDirectory + "/chmod 777 " + appDir+"/getroot"+"/script.sh");

    }

    public void installAMExtractor()
    {
        installRess("resources/AMExtractor",appDir);
        memUtilDirectory = pathBuild(appDir, "AMExtractor");
        sudo("chmod -R 777 " +memUtilDirectory);
    }

}