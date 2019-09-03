package com.kirchnersolutions.javabyte.backup;

public class ServerThread implements Runnable {

    private int port = 0;
    private volatile boolean isRunning = true, stop = false;

    private MultiClientServer server;

    public ServerThread(int por) {
        port = por;
        server = new MultiClientServer();
    }

    public void stopServer() {
        server.stop();
        stop = true;
    }

    public boolean running(){
        return isRunning;
    }

    public void run() {
        synchronized (this) {
            isRunning = true;
        }
        server.start(port);
        while(!stop){

        }
        synchronized (this) {
            isRunning = false;
        }
    }
}
