package com.kirchnersolutions.javabyte.backup;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        Future<String>[] futures = new Future[pages.listFiles().length];
        int count = 0;
        for (File page : pages.listFiles()) {
            futures[count] = threadPoolExecutor.submit(new ZipPage(page, "Database/Backup/" + tableName + "/" + tableName + "-" + CalenderConverter.getMonthDayYearHourMinuteSecond(time, "-", "-") + "/" + page.getName() + ".jbb"));
            count++;
        }
        count = 0;
        boolean failed = false;
        for (Future future : futures) {
            try {
                if (future.get().equals("t")) {
                } else {
                    failed = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                failed = true;
            } finally {
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
        zip(new ArrayList<>(Arrays.asList(bkDir.listFiles())), "Database/Backup/" + tableName + "/" + tableName + "-" + CalenderConverter.getMonthDayYearHourMinuteSecond(time, "-", "-") + ".jbb");
        for(File file : bkDir.listFiles()){
            file.delete();
        }
        bkDir.delete();
        return "true";
    }

    private class ZipPage implements Callable<String>{

        private File rowDir;
        private String zipOutputStream;

        ZipPage(File rowDir, String zipOutputStream){
            this.rowDir = rowDir;
            this.zipOutputStream = zipOutputStream;
        }

        public String call(){
            try{
                zip(new ArrayList<>(Arrays.asList(rowDir.listFiles())), zipOutputStream);
                return "t";
            }catch (Exception e){
                e.printStackTrace();
                return "f";
            }
        }

    }

    public boolean zip(List<File> listFiles, String destZipFile) throws FileNotFoundException,
            IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(new File(destZipFile)));
        for (File file : listFiles) {
            if (file.isDirectory()) {
                zipDirectory(file, file.getName(), zos);
            } else {
                zipFile(file, zos);
            }
        }
        zos.flush();
        zos.close();
        return true;
    }
    /**
     * Compresses files represented in an array of paths
     * @param files a String array containing file paths
     * @param destZipFile The path of the destination zip file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void zip(String[] files, String destZipFile) throws FileNotFoundException, IOException {
        List<File> listFiles = new ArrayList<File>();
        for (int i = 0; i < files.length; i++) {
            listFiles.add(new File(files[i]));
        }
        zip(listFiles, destZipFile);
    }
    /**
     * Adds a directory to the current zip output stream
     * @param folder the directory to be  added
     * @param parentFolder the path of parent directory
     * @param zos the current zip output stream
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void zipDirectory(File folder, String parentFolder,
                              ZipOutputStream zos) throws FileNotFoundException, IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }
            zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            long bytesRead = 0;
            byte[] bytesIn = new byte[1024];
            int read = 0;
            while ((read = bis.read(bytesIn)) != -1) {
                zos.write(bytesIn, 0, read);
                bytesRead += read;
            }
            zos.closeEntry();
        }
    }
    /**
     * Adds a file to the current zip output stream
     * @param file the file to be added
     * @param zos the current zip output stream
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void zipFile(File file, ZipOutputStream zos)
            throws FileNotFoundException, IOException {
        zos.putNextEntry(new ZipEntry(file.getName()));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
                file));
        long bytesRead = 0;
        byte[] bytesIn = new byte[1024];
        int read = 0;
        while ((read = bis.read(bytesIn)) != -1) {
            zos.write(bytesIn, 0, read);
            bytesRead += read;
        }
        zos.closeEntry();
    }

}

