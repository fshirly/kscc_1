package com.fable.kscc.api.skyCloud;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class FtpUtil {

    private static String LOCAL_CHARSET = "utf-8";

    public static Logger logger = LoggerFactory.getLogger(FtpUtil.class);

    public static void main(String[] args) {
        DownFTPParam param = new DownFTPParam();
        param.setHost("140.207.48.50");
        param.setPort(50022);
        param.setUsername("admin");
        param.setPassword("admin123");
        param.setPathDst("E:/test/");
        param.setPathSrc("/tmp/disk/02_02/2017年11月10日/0106654/");
//        param.setHost("192.168.6.30");
//        param.setUsername("duy");
//        param.setPassword("duy@1234");
//        param.setPathDst("E:/test/");
//        param.setPathSrc("/test/");
        System.out.println(FileDownload(param));
    }

    public static Map<String,Object> FileDownload(DownFTPParam param) {

        final List<String> fileNames= new ArrayList<>();

        FTPClient ftp = null;
        try {
            ftp = ftpLogin(param);
            if(ftp==null){
                return new HashMap<String,Object>(){{put("success","0");put("reason","无法连接Ftp");}};
            }
            // 检索ftp目录下所有的文件，利用时间字符串进行过滤
            boolean dir = ftp.changeWorkingDirectory(new String(param.getPathSrc().getBytes("UTF-8"), "ISO8859-1"));
            if (dir) {
                FTPFile[] fs = ftp.listFiles();
                for (FTPFile f : fs) {
                    //三种文件类型.txt .properties .mp4
                    if (!f.getName().contains(".txt")) {
                        downloadLogic(param, ftp, f);
                        if(f!=fs[fs.length-1]){
                            int reply = ftp.getReply();
                            System.out.println("reply:"+reply);
                            System.out.println("ensure connect again or not");
                            if (!FTPReply.isPositiveCompletion(reply)) {
                                System.out.println("need connect again");
                                //   重连
                                ftp.disconnect();
                                ftp = ftpLogin(param);
                                if(ftp==null){
                                    logger.error("ftp connect again happen error");
                                    System.out.println("ftp connect again happen error");
                                    return new HashMap<String,Object>(){{put("success","0");put("reason","connect Ftp again happen error");}};
                                }
                            }
                        }
                    }
                    if (f.getName().contains(".mp4")) {
                        fileNames.add(f.getName().replace("_合成通道",""));
                    }
                }
                //文件下载完成
                //此处需退出登录，以免阻塞
                ftp.logout();
                if(checkFile(param.getPathDst())){
                 return new HashMap<String,Object>(){{put("success","1");put("fileNames",fileNames);}};
                }
                return new HashMap<String,Object>(){{put("success","0");put("reason","file is not complete");}};
            }
            else{
                return new HashMap<String,Object>(){{put("success","0");put("reason","can not find this directory");}};
            }
        } catch (final Exception e) {
            e.printStackTrace();
            logger.error(param.getHospitalName()+"ftp download happen exception",e);
            return new HashMap<String,Object>(){{put("success","0");put("reason",e.getMessage());}};
        } finally {
            try {
                if (ftp != null) {
                    ftp.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static FTPClient ftpLogin(DownFTPParam param) {
        FTPClient ftp;
        try {
            ftp = new FTPClient();

            ftp.connect(param.getHost(), param.getPort());
            if (ftp.isConnected()) {
                if (ftp.login(param.getUsername(), param.getPassword())) {
                    ftp.login(param.getUsername(), param.getPassword());
                    ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
                    // 设置linux环境
                    FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
                    ftp.configure(conf);
                    // 判断是否连接成功
                    int reply = ftp.getReplyCode();
                    if (!FTPReply.isPositiveCompletion(reply)) {
                        ftp.disconnect();
                        System.out.println("FTP server refused connection.");
                        logger.error(param.getHospitalName()+"：FTP server refused connection.");
                        return null;
                    }
                    ftp.setDataTimeout(60000);       //设置传输超时时间为60秒
                    ftp.setControlEncoding(LOCAL_CHARSET);
                    // 设置访问被动模式
                    ftp.setRemoteVerificationEnabled(false);
                    ftp.enterLocalPassiveMode();
                    return ftp;
                }
                else{
                    logger.error(param.getHospitalName()+"FTP SERVER USERNAME OR PASSWORD ERROR");
                }
            }
            else{
                logger.error(param.getHospitalName()+"can not connect to this ftp host and port");
            }
        } catch (Exception e) {
            logger.error(param.getHospitalName()+"can not connect to this ftp host and port connect timeout",e);
            e.printStackTrace();
        }
        return null;
    }

    private static void downloadLogic(DownFTPParam param, FTPClient ftp, FTPFile f) throws IOException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            System.out.println("the file i need is:" + f.getName());
            logger.info("the file i need is:" + f.getName()+"start download");
            File localFile = new File(param.getPathDst() + f.getName().replace("_合成通道",""));
            if (!localFile.getParentFile().exists()) {
                localFile.getParentFile().mkdirs();
            }
            System.out.println(f.getSize());
            bis = new BufferedInputStream(
                    ftp.retrieveFileStream(new String(f.getName().getBytes("UTF-8"), "ISO8859-1")));
            bos = new BufferedOutputStream(new FileOutputStream(localFile));
            byte[] szBuf = new byte[128 * 1024];
            int dwRead;
            while ((dwRead = bis.read(szBuf, 0, 128 * 1024)) != -1) {
                bos.write(szBuf, 0, dwRead);
                bos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null)
                bis.close();
            if (bos != null)
                bos.close();
        }
        System.out.println(f.getName() + "the file location is"+param.getPathDst());
    }

    private static boolean checkFile(String dstPath) {
        BufferedReader in = null;
        List<Boolean> allSuccess = new ArrayList<>();
        try {
            File file = new File(dstPath);
            File[] files = file.listFiles();
            for (File file1 : files) {
                if ("MD5.properties".equals(file1.getName())) {
                    Properties properties = new Properties();
                    in = new BufferedReader(new InputStreamReader(new FileInputStream(dstPath + "MD5.properties"), "UTF-8"));
                    properties.load(in);
                    Iterator<String> it = properties.stringPropertyNames().iterator();
                    while (it.hasNext()) {
                        String fileName = it.next();
                        if (fileName.contains(".mp4")) {
                            String srcMd5 = properties.getProperty(fileName);
                            String dstMd5 = MD5Utils.fileMD5(dstPath + fileName.replace("_合成通道",""));
                            System.out.println("srcMd5:" + srcMd5);
                            System.out.println("dstMd5:" + dstMd5);
                            allSuccess.add(srcMd5.equalsIgnoreCase(dstMd5));
                        }
                    }
                }
            }
            for (Boolean bool : allSuccess) {
                //文件不完整
                if (!bool) {
                    System.out.println("file is not complete");
                    logger.error("file is not complete");
                    return bool;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return true;
    }

}
