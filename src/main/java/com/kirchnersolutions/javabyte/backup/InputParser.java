package com.kirchnersolutions.javabyte.backup;

import java.util.Base64;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class InputParser {

    ThreadPoolExecutor threadPoolExecutor;

    InputParser(){
        this.threadPoolExecutor = MultiClientServer.threadpool;
    }

    String parse(String input) throws Exception{
        String in = new String(Base64.getDecoder().decode(input.getBytes("UTF-8")), "UTF-8");
        if(in.split(";")[0].equals("h")){
            if(hash(in.split(";")[1])){
                //System.out.println("here");
                return new String(Base64.getEncoder().encode("true".getBytes("UTF-8")), "UTF-8");
            }else{
                return new String(Base64.getEncoder().encode("false".getBytes("UTF-8")), "UTF-8");
            }
        }
        if(in.split(";")[0].equals("bk")){
            return new String(Base64.getEncoder().encode(backup(in.split(";")[1]).getBytes("UTF-8")), "UTF-8");
        }
        return new String(Base64.getEncoder().encode("invalid".getBytes("UTF-8")), "UTF-8");
    }

    boolean hash(String tableName){
        Future<String> fut = threadPoolExecutor.submit(new Hash(tableName));
        try{
            if(fut.get().equals("true")){
                return true;
            }
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    String backup(String tableName){
        Future<String> future = threadPoolExecutor.submit(new Backup(tableName));
        try{
            return (String)future.get();
        }catch (Exception e){
            return "failed";
        }
    }

}
