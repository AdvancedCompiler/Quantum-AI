package com.rf.AIquantum.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ZipUtils {
    private static final Logger log = LoggerFactory.getLogger(ZipUtils.class);

    private ZipUtils() {
    }
    /**
     * 创建ZIP文件
     * @param sourcePath 文件或文件夹路径
     * @param zipPath 生成的zip文件存在路径（包括文件名）
     * @param isDrop  是否删除原文件:true删除、false不删除
     */
    public static void createZip(String sourcePath, String zipPath,Boolean isDrop) {
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipPath);
            zos = new ZipOutputStream(fos);
            //zos.setEncoding("utf8");//此处修改字节码方式。
            //createXmlFile(sourcePath,"293.xml");
            writeZip(new File(sourcePath), "", zos,isDrop);
        } catch (FileNotFoundException e) {
            log.error("创建ZIP文件失败",e);
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
                log.error("创建ZIP文件失败",e);
            }

        }
    }
    /**
     * 清空文件和文件目录
     *
     * @param f
     */
    public static void clean(File f) throws Exception {
        String cs[] = f.list();
        if (cs == null || cs.length <= 0) {
            //System.out.println("delFile:[ " + f + " ]");
            boolean isDelete = f.delete();
            if (!isDelete) {
                //System.out.println("delFile:[ " + f.getName() + "文件删除失败！" + " ]");
                throw new Exception(f.getName() + "文件删除失败！");
            }
        } else {
            for (int i = 0; i < cs.length; i++) {
                String cn = cs[i];
                String cp = f.getPath() + File.separator + cn;
                File f2 = new File(cp);
                if (f2.exists() && f2.isFile()) {
                    //System.out.println("delFile:[ " + f2 + " ]");
                    boolean isDelete = f2.delete();
                    if (!isDelete) {
                        //System.out.println("delFile:[ " + f2.getName() + "文件删除失败！" + " ]");
                        throw new Exception(f2.getName() + "文件删除失败！");
                    }
                } else if (f2.exists() && f2.isDirectory()) {
                    clean(f2);
                }
            }
            //System.out.println("delFile:[ " + f + " ]");
            boolean isDelete = f.delete();
            if (!isDelete) {
                //System.out.println("delFile:[ " + f.getName() + "文件删除失败！" + " ]");
                throw new Exception(f.getName() + "文件删除失败！");
            }
        }
    }
    private static void writeZip(File file, String parentPath, ZipOutputStream zos,Boolean isDrop) {
        if(file.exists()){
            if(file.isDirectory()){//处理文件夹
                parentPath+=file.getName()+File.separator;
                File [] files=file.listFiles();
                if(files.length != 0)
                {
                    for(File f:files){
                        writeZip(f, parentPath, zos,isDrop);
                    }
                }
                else
                {       //空目录则创建当前目录
                    try {
                        zos.putNextEntry(new ZipEntry(parentPath));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }else{
                FileInputStream fis=null;
                try {
                    fis=new FileInputStream(file);
                    ZipEntry ze = new ZipEntry(parentPath + file.getName());
                    zos.putNextEntry(ze);
                    byte [] content=new byte[1024];
                    int len;
                    while((len=fis.read(content))!=-1){
                        zos.write(content,0,len);
                        zos.flush();
                    }

                } catch (FileNotFoundException e) {
                    log.error("创建ZIP文件失败",e);
                } catch (IOException e) {
                    log.error("创建ZIP文件失败",e);
                }finally{
                    try {
                        if(fis!=null){
                            fis.close();
                        }
                        if(isDrop){
                            clean(file);
                        }
                    }catch(IOException e){
                        log.error("创建ZIP文件失败",e);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
