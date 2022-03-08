package com.demo.jsch;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * SSH连接服务器后，开启shell窗口。该窗口可以交互式执行shell命令。
 */
public class Shell {
    private static final String HOST = "localhost";
    private static final String USER = "root";
    private static final String PASS = "123456";
    private static final Integer PORT = 22;

    public static void main(String[] args) {
        try {
            JSch jsch = new JSch();
            //jsch.setKnownHosts("/home/user/.ssh/known_hosts");

            Session session = jsch.getSession(USER, HOST, PORT);
            session.setPassword(PASS);
            session.setUserInfo(new MyUserInfo());

            // It must not be recommended, but if you want to skip host-key check,
            // invoke following,
            session.setConfig("StrictHostKeyChecking", "no");

            //session.connect();
            session.connect(10 * 1000);// making a connection with timeout.

            //Channel的类型可以查看Channel.getChannel(type)源码
            Channel channel = session.openChannel("shell");
            channel.setInputStream(System.in);
            channel.setOutputStream(System.out);
            channel.connect(10 * 1000);
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

