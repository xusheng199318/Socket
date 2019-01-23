package com.arthur.http;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by xusheng on 2019/1/23.
 */
public class Redirection {
    private final Logger logger = Logger.getLogger("Redirection");

    private final String newSite;

    private final Integer port;

    public Redirection(String newSite, Integer port) {
        this.newSite = newSite;
        this.port = port;
    }

    public void start() {
        try (ServerSocket server = new ServerSocket(this.port)) {
            while (true) {
                try {
                    Socket conn = server.accept();
                    Thread t = new RedirectThread(conn);
                    t.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class RedirectThread extends Thread {
        private Socket conn;

        public RedirectThread(Socket conn) {
            this.conn = conn;
        }

        @Override
        public void run() {
            try {
                Writer writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "US-ASCII"));

                Reader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder request = new StringBuilder(80);
                while (true) {
                    int c = reader.read();
                    if (c == '\r' || c == '\n' || c == -1) {
                        break;
                    }
                    request.append((char) c);
                }
                String get = request.toString();
                if (get.indexOf("HTTP") != -1) {
                    writer.write("HTTP/1.0 302 FOUND\r\n");
                    writer.write("Date: " + new Date() + "\r\n");
                    writer.write("Server: Redirector 1.1\r\n");
                    writer.write("Location: " + newSite + "\r\n");
                    writer.write("Content-type: text/html\r\n\r\n");
                    writer.flush();
                    logger.log(Level.INFO, "Redirected " + conn.getRemoteSocketAddress());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Redirection redirection = new Redirection("https://www.baidu.com", 80);
        redirection.start();
    }
}
