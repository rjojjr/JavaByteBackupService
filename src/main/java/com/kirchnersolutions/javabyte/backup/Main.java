package com.kirchnersolutions.javabyte.backup;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {

    public static void main(String[] args) throws  Exception{
        //ApplicationContext ctx = new AnnotationConfigApplicationContext(ThreadConfig.class, StompConfig.class, HttpSessionConfig.class, MainConfig.class);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)Executors.newCachedThreadPool();
        threadPoolExecutor.setCorePoolSize(50);

    }

    public ThreadPoolExecutor threadPoolTaskExecutor() {


        ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newCachedThreadPool();

        executor.setCorePoolSize(50);

        return executor;

    }

}
