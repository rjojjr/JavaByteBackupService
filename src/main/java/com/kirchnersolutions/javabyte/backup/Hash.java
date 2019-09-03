package com.kirchnersolutions.javabyte.backup;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

class Hash implements Runnable {

    private String tableName = "";
    private ThreadPoolExecutor threadPoolExecutor;

    Hash(String tableName, ThreadPoolExecutor threadPoolExecutor) {
        this.tableName = tableName;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public void run() {
        File dir = new File("Database/Tables/" + tableName);
        if (dir.exists()) {
            File bkDir = new File("Database/Backup/" + tableName);
            if (!bkDir.exists()) {
                bkDir.mkdirs();
            }
            Future<byte[]>[] futures = new Future[new File(dir, "/TablePages").listFiles().length];
            int count = 0;
            for (File pageDir : new File(dir, "/TablePages").listFiles()) {
                futures[count] = threadPoolExecutor.submit(new HashPage(new File(pageDir, "/Rows")));
                count++;
            }
            byte[] hash = new byte[32];
            boolean first = true;
            for (Future future : futures) {
                try {
                    if (first) {
                        hash = getHash(new String((byte[]) future.get()));
                        first = false;
                    } else {
                        hash = getHash(new String(hash) + new String((byte[]) future.get()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                File hashFile = new File(bkDir, "/" + tableName + CalenderConverter.getMonthDayYearHourMinuteSecond(System.currentTimeMillis(), "-", "-") + ".hash");
                if(!hashFile.exists()){
                    hashFile.createNewFile();
                }
                ByteTools.writeBytesToFile(hashFile, hash);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    byte[] getHash(String string) {
        try {
            return ByteTools.getSHA256(string);
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[32];
        }
    }

    private class HashPage implements Callable<byte[]> {

        File rowDir;

        public HashPage(File rowDir) {
            this.rowDir = rowDir;
        }

        public byte[] call() {
            File bkDir = new File("Database/Backup");
            if (!bkDir.exists()) {
                bkDir.mkdirs();
            }
            byte[] bytes = new byte[32];
            for (File row : rowDir.listFiles()) {
                for (File field : row.listFiles()) {
                    try {
                        byte[] file = ByteTools.readBytesFromFile(field);
                        bytes = ByteTools.getSHA256(new String(bytes) + new String(file));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return bytes;
        }
    }
}

