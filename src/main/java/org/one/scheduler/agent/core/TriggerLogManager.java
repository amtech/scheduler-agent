package org.one.scheduler.agent.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 日志操作
 * Created by bin on 14-5-23.
 */
public class TriggerLogManager {

    private static final Logger logger = LoggerFactory.getLogger(TriggerLogManager.class);
    public static ConcurrentLinkedQueue<String> concurrentLinkedQueue = new ConcurrentLinkedQueue<String>();
    private static byte[] temp = new byte[]{};


    /**
     * 写日志
     */
    public static void write(){
        String path = mkdirs();
        //synchronized (concurrentLinkedQueue){
            //if(!concurrentLinkedQueue.isEmpty()){
        String data = concurrentLinkedQueue.poll();
        if(data!=null){
            long length = data.getBytes().length+temp.length;
            if(length>=2048){
                if(temp.length!=0){
                    RandomAccessFile randomAccessFile = null;
                    FileChannel fileChannel = null;
                    try {
                        randomAccessFile = new RandomAccessFile(path+File.separator+(new Date()).toString()+".log","rw");
                        fileChannel = randomAccessFile.getChannel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(temp.length);
                        byteBuffer.put(temp);
                        byteBuffer.flip();
                        while(byteBuffer.hasRemaining()) {
                            fileChannel.write(byteBuffer);
                        }
                        fileChannel.force(true);
                    } catch (FileNotFoundException e) {
                        logger.error(e.getMessage());
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }finally {
                        if(fileChannel!=null){
                            try {
                                fileChannel.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }
                temp = data.getBytes();
            }else{
                byte[] bytes = new byte[temp.length+data.getBytes().length];
                for(int i=0;i<temp.length;i++){
                    bytes[i] = temp[i];
                }
                for(int i=0;i<data.getBytes().length;i++){
                    bytes[temp.length+i] = data.getBytes()[i];
                }
                temp = bytes;
            }
        }

           // }
        //}
    }

    /**
     * 读日志
     * @param path
     */
    public static String read(String path){
        File file = new File(path);
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.endsWith(".log")){
                    return  true;
                }
                return false;
            }
        };
        String[] fileList = file.list(filenameFilter);
        String fileName = null;
        if(fileList.length!=0){
            Date currentDate = new Date();
            fileName = path+File.separator+AgentConstant.APP_NAME+currentDate+".zip";
            FileOutputStream f = null;
            try {
                f = new FileOutputStream(fileName);
                CheckedOutputStream cos = new CheckedOutputStream(f, new Adler32());
                ZipOutputStream zos = new ZipOutputStream(cos);
                BufferedOutputStream bos = new BufferedOutputStream(zos);
                int temp = 5;
                if(temp>fileList.length){
                    temp = fileList.length;
                }
                for (int i=0;i<temp;i++){
                    if(!fileList[i].endsWith(".zip")){
                        String name = path+File.separator+fileList[i];
                        BufferedReader br = new BufferedReader(new FileReader(name));
                        //ZipEntry ZIP 文件条目
                        //putNextEntry 写入新条目，并定位到新条目开始处
                        zos.putNextEntry(new ZipEntry(fileList[i]));
                        int c;
                        while ((c = br.read()) != -1) {
                            bos.write(c);
                        }
                        br.close();
                        bos.flush();
                        TriggerLogManager.delete(name);
                    }
                }
                bos.close();
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage());
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        return fileName;
    }

    /**
     * 删除日志
     */
    public static void delete(String fileName){
        File file = new File(fileName);
        file.delete();
    }

    public static String mkdirs(){
        File logFile = null;

        String path = System.getProperty("user.home");
        logFile = new File(path+File.separator+"log"+File.separator+AgentConstant.APP_NAME);
        if(!logFile.exists()){
            logFile.mkdirs();
        }

        return logFile.getAbsolutePath();
    }
}
