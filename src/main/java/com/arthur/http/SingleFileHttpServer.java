package com.arthur.http;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleFileHttpServer {

    private static final Logger logger = Logger.getLogger("SingleFileHttpServer");

    private final byte[] content;

    private final byte[] header;

    private final int port;

    private final String encoding;

    public SingleFileHttpServer(byte[] content, String mimeType, int port, String encoding) {
        this.content = content;
        this.port = port;
        this.encoding = encoding;
        String header = "HTTP/1.0 200 OK\r\n" +
                "Server: OneFile 2.0\r\n" +
                "Content-length: " + content.length +"\r\n" +
                "Content-type: " + mimeType + "; chatset = " + encoding + "\r\n\r\n";
        this.header = header.getBytes(Charset.forName("US-ASCII"));
    }

    public SingleFileHttpServer(String content, String encoding, String mimeType, int port) {
        this(content.getBytes(), mimeType, port, encoding);
    }

    public void start() {
        ExecutorService threadPool = Executors.newFixedThreadPool(100);
        try (ServerSocket server = new ServerSocket(this.port)) {
            logger.info("Accepting connections on port " + server.getLocalPort());
            logger.info("Data to be sent: ");
            logger.info(new String(content, encoding));

            while (true) {
                try {
                    Socket conn = server.accept();
                    threadPool.submit(new HttpHandler(conn));
                } catch (RuntimeException e) {
                    logger.log(Level.SEVERE, "Unexpected error", e);
                }
            }

        } catch (IOException e) {
            logger.log(Level.WARNING, "Exception accepting connection", e);
        }


    }

    private class HttpHandler implements Callable<Void> {

        private final Socket conn;

        public HttpHandler(Socket conn) {
            this.conn = conn;
        }

        @Override
        public Void call() {
            try {
                OutputStream os = new BufferedOutputStream(conn.getOutputStream());

                InputStream is = new BufferedInputStream(conn.getInputStream());

                StringBuilder request = new StringBuilder(80);
                while (true) {
                    int c = is.read();
                    if (c == '\r' || c == '\n' || c == -1) {
                        break;
                    }
                    request.append((char) c);
                }

                if (request.toString().indexOf("HTTP/") != -1) {
                    os.write(header);
                }
                os.write(content);
                os.flush();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error writing to client", e);
            } finally {
                try {
                    conn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    public static void main(String[] args) {
        int port = 80;
        /*try {
            port = Integer.parseInt(args[1]);
            if (port < 1 || port > 65535) {
                port = 80;
            }
        } catch (RuntimeException e) {
            port = 80;
        }*/

        String encoding = "UTF-8";
        /*if (args.length > 3) {
            encoding = args[2];
        }*/

        try {
            String c = "localhost";
            Path path = Paths.get(c);
            byte[] content = Files.readAllBytes(path);

            String contentType = URLConnection.getFileNameMap().getContentTypeFor(c);
            SingleFileHttpServer server = new SingleFileHttpServer(content, contentType, port, encoding);
            server.start();

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Usage: java SingleFileHttpServer filename port encoding");
        } catch (IOException ex) {
            logger.severe(ex.getMessage());
        }
    }
}
