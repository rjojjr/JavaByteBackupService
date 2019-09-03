package com.kirchnersolutions.javabyte.backup;

import java.util.Base64;
import java.util.concurrent.ThreadPoolExecutor;

public class InputParser {

    ThreadPoolExecutor threadPoolExecutor;

    InputParser(ThreadPoolExecutor threadPoolExecutor){
        this.threadPoolExecutor = threadPoolExecutor;
    }

    String parse(String input) throws Exception{
        String in = new String(Base64.getDecoder().decode(input.getBytes("UTF-8")), "UTF-8");
        if(in.split(";")[0].equals("h")){
            hash(in.split(";")[1]);
            return new String(Base64.getEncoder().encode("true".getBytes("UTF-8")), "UTF-8");
        }
        return new String(Base64.getEncoder().encode("invalid".getBytes("UTF-8")), "UTF-8");
    }

    void hash(String tableName){
        threadPoolExecutor.submit(new Hash(tableName, threadPoolExecutor));
    }

}
