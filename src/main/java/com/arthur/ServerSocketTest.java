package com.arthur;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Created by xusheng on 2019/1/22.
 */
public class ServerSocketTest {
    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(8888);
            while (true) {
                Socket connection = server.accept();
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(new Date().toString() + "\r\n");
                writer.flush();
                writer.close();
                connection.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
