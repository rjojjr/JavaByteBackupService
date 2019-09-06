/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirchnersolutions.javabyte.backup;


import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rjojj
 */
class IndependentClientHandler implements Runnable {

    private Socket clientSocket;
    private BufferedWriter out;
    private BufferedReader in;

    private volatile boolean loggedOn = false;
    private InputParser inputParser;

    private boolean stop;

    public IndependentClientHandler(Socket socket) {
        inputParser = new InputParser();
        this.clientSocket = socket;
        //System.out.println("here");
        stop = false;
    }

    public void stopThread() {
        synchronized (this) {

            this.stop = true;
        }
    }

@Override
    public void run() {
        String ip = clientSocket.getInetAddress().toString();
        int port = clientSocket.getPort();
        //transMan = TransactionManager.getInstance();
        try {
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
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
            while ((inputLine = in.readLine()) != null && !stop) {
                String rt = inputParser.parse(inputLine);
                out.write(rt + "\n");

                out.flush();
                //System.out.println(rt);
            }

            in.close();
            out.close();
        } catch (IOException ex) {
            System.err.println("Failure to read client socket");
            Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception p){
            System.err.println("Failure to parse input");
            Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, p);
        }

        try {
            clientSocket.close();
        } catch (Exception ex) {
            System.err.println("Failure to close client socket");
            Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
