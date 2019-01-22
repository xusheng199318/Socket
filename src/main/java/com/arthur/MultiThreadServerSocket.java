package com.arthur;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Created by xusheng on 2019/1/22.
 */
public class MultiThreadServerSocket {
    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(8888);
            while (true) {
                Socket conn = server.accept();
                Thread task = new DaytimeThreadServer(conn);
                task.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class DaytimeThreadServer extends Thread {
        Socket conn;

        public DaytimeThreadServer (Socket conn) {
            this.conn = conn;
        }
        @Override
        public void run() {
            try {
                Writer writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(new Date().toString() + "\r\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    conn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
