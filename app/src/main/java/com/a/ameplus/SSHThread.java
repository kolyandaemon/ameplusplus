package com.a.ameplus;





final public class SSHThread {
    String [] conf;
    boolean starting = true;
    public String[] coms;
    public String homePath;



    public SSHThread(String[] p, String[] coms, String homePath) {
        conf = p;
        this.coms = coms;
        this.homePath = homePath;
    }
    }
