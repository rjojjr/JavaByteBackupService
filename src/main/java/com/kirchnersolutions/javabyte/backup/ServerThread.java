package com.kirchnersolutions.javabyte.backup;

import java.util.concurrent.ThreadPoolExecutor;

public class ServerThread implements Runnable {

    private int port = 0;
    private volatile boolean isRunning = true, stop = false;

    static ThreadPoolExecutor threadPoolExecutor;

    private MultiClientServer server;

    public ServerThread(int port, ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.port = port;
        server = new MultiClientServer();
    }

    public void stopServer() {
        server.stop();
        stop = true;
    }

    public synchronized boolean isRunning(){
        return isRunning;
    }

    public void run() {
        synchronized (this) {
            isRunning = true;
        }
        server.start(port);
        while(!stop){

        }
        server.stop();
        synchronized (this) {
            isRunning = false;
        }
    }
}
