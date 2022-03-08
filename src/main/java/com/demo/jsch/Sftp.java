package com.demo.jsch;

import com.jcraft.jsch.*;

import java.io.*;
import java.util.Properties;

public class Sftp {

    private static final String USER = "user";
    private static final String PASS = "pass";
    private static final String HOST = "localhost";
    private static final Integer PORT = 22;
    private static final Integer TIME_OUT = 10;

    public static void main(String[] args) {
        SftpUtil util = new SftpUtil(USER, PASS, HOST, PORT, TIME_OUT);

        String localFilePath = "/Users/wanxiaolong/Tmp/123.txt";
        String sftpFilePath = "upload/123.txt";
        String localFilePathNew = "/Users/wanxiaolong/Tmp/123_new.txt";

        //上传文件
        util.uploadFile(localFilePath, sftpFilePath, ChannelSftp.OVERWRITE);
        //下载文件
        util.downloadFile(sftpFilePath, localFilePathNew);
    }

    private static class SftpUtil {
        //配置项
        private String user;
        private String pass;
        private String host;
        private int port;
        private int timeout;
        public SftpUtil(String user, String pass, String host, int port, int timeout) {
            this.user = user;
            this.pass = pass;
            this.host = host;
            this.port = port;
            this.timeout = timeout;
        }

        private Session session = null;
        private ChannelSftp channelSftp = null;

        private void connect() throws JSchException {
            JSch jsch = new JSch();
            //根据用户名，主机ip，端口获取一个Session对象
            session = jsch.getSession(user, host, port);
            session.setPassword(pass);

            Properties props = new Properties();
            props.put("StrictHostKeyChecking", "no");
            //为Session对象设置properties
            session.setConfig(props);
            session.setTimeout(timeout * 1000);
            session.connect();
            System.out.println("Session connected.");

            channelSftp = (ChannelSftp)session.openChannel("sftp");
            channelSftp.connect(timeout * 1000);
            System.out.println("SFTP connection established.");
        }

        public void disconnect() {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }

        public boolean uploadFile(String src, String dst, int mode) {
            System.out.println("=======Uploading file=======");
            byte[] data = null;
            try {
                data = readBytes(new FileInputStream(src));
            } catch (Exception e) {
                System.err.println("Exception when reading file: " + src);
                return false;
            }
            return writeToSftp(data, dst, mode);
        }

        public boolean writeToSftp(byte[] data, String dst, int mode) {
            try {
                connect();
                System.out.println("Upload file started. TargetPath=" + dst);
                //注意：这里的put的参数dst是从FTP服务器中/home/[user]目录后的路径，否则会报no such file的错误。
                //因为atmoz/sftp这个镜像提供的SFTP登录到后会自动切换到用户主目录下。
                OutputStream out = channelSftp.put(dst, mode);
                out.write(data);
                out.flush();
                System.out.println("Upload file completed. TargetPath=" + dst + ", length=" + data.length);
                disconnect();
                return true;
            } catch (Exception e) {
                System.err.println("Exception when uploading file: " + e.getMessage());
                return false;
            }
        }

        public boolean downloadFile(String src, String dst) {
            System.out.println("======Downloading file======");
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(dst);
            } catch (Exception e) {
                System.err.println("Exception when creating file: " + dst);
                return false;
            }
            byte[] data = readFromSftp(src);
            try {
                out.write(data);
                out.flush();
                return true;
            } catch (Exception e) {
                System.err.println("Exception when writing file: " + dst);
                return false;
            }
        }

        public byte[] readFromSftp(String src) {
            try {
                connect();
                System.out.println("Download file started. filepath=" + src);
                InputStream in = channelSftp.get(src);
                byte[] data = readBytes(in);
                System.out.println("Download file completed. filepath=" + src + ", length=" + data.length);
                disconnect();
                return data;
            } catch (Exception e) {
                System.err.println("Exception when downloading file: " + e.getMessage());
                return null;
            }
        }

        // read data from InputStream into the byte array.
        private byte[] readBytes(InputStream is) throws IOException {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[0xFFFF];
            for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
                os.write(buffer, 0, len);
            }
            return os.toByteArray();
        }
    }
}
