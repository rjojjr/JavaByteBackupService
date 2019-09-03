/*
 * kData Performance Database 
 * 
 * Version: 1.0.00b
 * 
 * Robert Kirchner Jr.
 * 2018 Kirchner Solutions
 * 
 * This code is not to be distributed, compiled, decompiled
 * copied, used, recycled, moved or modified in any way without 
 * express written permission from Kirchner Solutions
 */
package com.kirchnersolutions.javabyte.backup;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * MultiClientServer Version 1.0.00b
 *
 * @author Robert Kirchner Jr. 2018 Kirchner Solutions
 */
class MultiClientServer {

    private ServerSocket serverSocket;
    static ThreadPoolExecutor threadpool;
    
    public MultiClientServer(ThreadPoolExecutor threadPoolExecutor){
        threadpool = threadPoolExecutor;
    }

    public void start(int port) {
        
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                threadpool.execute(new IndependentClientHandler(serverSocket.accept()));
            }
        } catch (Exception ex) {
            System.err.println("Failure to start server socket");
            Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (Exception ex) {
            System.err.println("Failure to stop server socket");
            Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static class ClientHandler extends Thread {

        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        private boolean loggedOn = false;
        private String userName = "";

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            //transMan = TransactionManager.getInstance();
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException ex) {
                System.err.println("Failure to open client socket output");
                Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {

                in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException ex) {
                System.err.println("Failure to open client socket input");
                Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, ex);
            }

            String inputLine;
            try {
                while ((inputLine = in.readLine()) != null) {
                }
                in.close();
            } catch (IOException ex) {
                System.err.println("Failure to read client socket");
                Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            out.close();
            try {
                clientSocket.close();
            } catch (Exception ex) {
                System.err.println("Failure to close client socket");
                Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

