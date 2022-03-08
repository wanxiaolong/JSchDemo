package com.demo.jsch;

import com.jcraft.jsch.*;

import java.io.InputStream;

/**
 * SSH连接服务器后，程序里执行shell命令，并获取返回结果。
 */
public class Exec {

    private static final String HOST = "localhost";
    private static final String USER = "root";
    private static final String PASS = "123456";
    private static final Integer PORT = 22;

    public static void main(String[] args) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(USER, HOST, PORT);
            session.setPassword(PASS);
            session.setUserInfo(new MyUserInfo());
            session.connect(30 * 1000);

            ChannelExec channel = (ChannelExec)session.openChannel("exec");
            channel.setCommand("ls");
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] tmp = new byte[1024];
            while(true) {
                while(in.available() > 0){
                    int i = in.read(tmp, 0, 1024);
                    //如果stream已经没有数据
                    if(i < 0) break;
                    System.out.print(new String(tmp, 0, i));
                }
                if(channel.isClosed()) {
                    if(in.available() > 0) continue;
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee){}
            }
            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    static class MyUserInfo implements UserInfo {
        @Override
        public String getPassphrase() {
            System.out.println("getPassphrase()");
            return null;
        }
        @Override
        public String getPassword() {
            System.out.println("getPassword()");
            return null;
        }
        @Override
        public boolean promptPassword(String message) {
            System.out.println("promptPassword():" + message);
            return false;
        }
        @Override
        public boolean promptPassphrase(String message) {
            System.out.println("promptPassphrase():" + message);
            return false;
        }
        @Override
        public boolean promptYesNo(String message) {
            System.out.println("promptYesNo():" + message);
            return true;//修改为true
        }
        @Override
        public void showMessage(String message) {
            System.out.println("showMessage():" + message);
        }
    }
}
