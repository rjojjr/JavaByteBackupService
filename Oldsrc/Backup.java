package com.kirchnersolutions.javabyte.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Backup implements Callable<String> {

    private static ThreadPoolExecutor threadPoolExecutor;
    private String tableName;
    private long time;
    private File bkDir;

    Backup(String tableName){
        time = System.currentTimeMillis();
        this.tableName = tableName;
        this.threadPoolExecutor = MultiClientServer.threadpool;
        bkDir = new File("Database/Backup/" + tableName + "/" + tableName + "-" + CalenderConverter.getMonthDayYearHourMinuteSecond(time, "-", "-"));
        if(!bkDir.exists()){
            bkDir.mkdirs();
        }
    }

    public String call() throws Exception {
        File pages = new File("Database/Tables/" + tableName + "/TablePages");
        File table = new File("Database/Tables/" + tableName);
        FileOutputStream[] fosa = new FileOutputStream[pages.listFiles().length];
        ZipOutputStream[] zipOuta = new ZipOutputStream[pages.listFiles().length];
        Future<String>[] futures = new Future[pages.listFiles().length];
        FileOutputStream fos = new FileOutputStream("Database/Backup/" + tableName + "/" + tableName + "-" + CalenderConverter.getMonthDayYearHourMinuteSecond(time, "-", "-") + ".jbb");
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        int count = 0;
        for (File page : pages.listFiles()) {
            fosa[count] = new FileOutputStream(new File(bkDir, "/TablePages/" + page.getName()));
            zipOuta[count] = new ZipOutputStream(fosa[count]);
            futures[count] = threadPoolExecutor.submit(new ZipPage(page, zipOuta[count]));
            count++;
        }
        count = 0;
        boolean failed = false;
        for (Future future : futures) {
            try {
                if (future.get().equals("t")) {
                    zipOuta[count].close();
                    fosa[count].close();
                } else {
                    failed = true;
                    zipOuta[count].close();
                    fosa[count].close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                failed = true;
            } finally {
                zipOuta[count].close();
                fosa[count].close();
            }
            count++;
        }
        if(failed){
            return "false";
        }
        for(File def : table.listFiles()){
            if(def.isFile()){
                try {
                    File newDef = new File(bkDir, "/" + def.getName());
                    if(!newDef.exists()){
                        newDef.createNewFile();
                    }
                    ByteTools.writeBytesToFile(newDef, ByteTools.readBytesFromFile(def));
                } catch (Exception e) {
                    e.printStackTrace();
                    return "false";
                } finally {
                }
            }
        }
        if(zipFileDir(bkDir, bkDir.getName(), zipOut)){
            zipOut.close();
            fos.close();
            return "true";
        }
        zipOut.close();
        fos.close();
        return "false";
    }

    private class ZipPage implements Callable<String>{

        private File rowDir;
        private ZipOutputStream zipOutputStream;

        ZipPage(File rowDir, ZipOutputStream zipOutputStream){
            this.rowDir = rowDir;
            this.zipOutputStream = zipOutputStream;
        }

        public String call(){
            try{
                zipFileDir(rowDir, rowDir.getName(), zipOutputStream);
                return "t";
            }catch (Exception e){
                e.printStackTrace();
                return "f";
            }
        }

    }

    private static boolean zipFileDir(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {

        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFileDir(childFile, fileName + "/" + childFile.getName(), zipOut);
            }

        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
        return true;
    }

}

