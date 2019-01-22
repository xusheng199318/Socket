package com.arthur;

import java.io.*;
import java.net.Socket;

/**
 * Created by xusheng on 2019/1/22.
 */
public class ClientSocket {

    public static void main(String[] args) {
        //readFromServer();
        //writeToServer();
        findTcpServer();
    }

    private static void findTcpServer() {
        Socket socket = null;
        for (int i = 0; i < 1024; i++) {
            try {
                socket = new Socket("localhost", i);
                System.out.println("There is a server on port " + i + " port");
            } catch (IOException e) {

            }
        }
    }

    private static void writeToServer() {
        Socket socket = null;
        try {
            socket = new Socket("dict.org", 2628);
            socket.setSoTimeout(30000);

            OutputStream os = socket.getOutputStream();
            Writer writer = new OutputStreamWriter(os, "utf-8");
            writer = new BufferedWriter(writer);

            InputStream is = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String[] strArr = new String[5];
            strArr[0] = "gold";
            strArr[1] = "uranium";
            strArr[2] = "silver";
            strArr[3] = "copper";
            strArr[4] = "lead";

            for (String s : strArr) {
                define(s, writer, reader);
            }

            writer.write("quit\r\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void define(String s, Writer writer, BufferedReader reader) throws IOException {
        writer.write("DEFINE English-Dutch " + s + "\r\n");
        writer.flush();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            System.out.println(line);
        }
    }

    private static void readFromServer() {
        Socket socket = null;
        try {
            socket = new Socket("time.nist.gov", 13);
            socket.setSoTimeout(15000);
            InputStream is = socket.getInputStream();
            StringBuilder sb = new StringBuilder();
            InputStreamReader isr = new InputStreamReader(is, "ASCII");
            for (int c = isr.read(); c != -1; c = isr.read()) {
                sb.append((char) c);
            }
            System.out.println(sb);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
