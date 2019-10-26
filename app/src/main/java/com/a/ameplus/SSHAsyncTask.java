package com.a.ameplus;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

public class SSHAsyncTask extends AsyncTask<SSHThread,Void,Void> {

    SSHThread sshWork=null;
    ProcessBuilder pb;
    String[] procsStdOut = null;
    String[] procsStdErr = null;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d("MY_T", "Запус Async Task");
    }

    @Override
    protected Void doInBackground(SSHThread... sshTask) {
        sshWork=sshTask[0];
        Task(sshWork);
        Log.d("MY_T", "Сервак запущен123");
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        Log.d("MY_T", "конец");
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        pb.directory();
        Log.d("MY_T", "Завершен");
    }

    public void Task(SSHThread task) {
        try {

                 pb = new ProcessBuilder(task.coms);
                pb.directory(new File(task.homePath));
                Map<String, String> env = pb.environment();
                env.put("PS1", "[\\u\\@\\h \\w ]\\$ ");
                env.put("SSH_SERVER_PW", "toor");
                env.put("USER", task.conf[0]);
                env.put("LD_LIBRARY_PATH",task.conf[1]);
                env.put("TERMINFO", task.conf[2]);
                env.put("SHELLEXEC",task.conf[3]);
                env.put("SSH_USERNAME",task.conf[4]);
                env.put("SSH_USERPATH",task.conf[5]);
                pb.redirectErrorStream(true);
               Process proc= pb.start();
               processMonitor(proc);


        } catch (Exception e) {

            e.printStackTrace();

        }
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
}
