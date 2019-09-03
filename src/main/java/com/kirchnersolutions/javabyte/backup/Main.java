package com.kirchnersolutions.javabyte.backup;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {

    public static void main(String[] args) throws  Exception{
        //ApplicationContext ctx = new AnnotationConfigApplicationContext(ThreadConfig.class, StompConfig.class, HttpSessionConfig.class, MainConfig.class);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)Executors.newCachedThreadPool();
        threadPoolExecutor.setCorePoolSize(50);
        System.out.println("Starting server on port " + args[0]);
        ServerThread server = new ServerThread(Integer.parseInt(args[0]), threadPoolExecutor);
        threadPoolExecutor.execute(server);
        System.out.println("Server running on port " + args[0]);
        while (server.isRunning()){

        }
        System.out.println("Server stopped");
    }

}
