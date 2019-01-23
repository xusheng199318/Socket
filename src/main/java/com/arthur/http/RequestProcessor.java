package com.arthur.http;

import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by xusheng on 2019/1/23.
 */
public class RequestProcessor implements Runnable {
    private final Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());

    /**
     * 请求信息，可以从中获取输入、输出流来进行交互
     */
    private Socket request;

    /**
     * 应用根目录
     */
    private File rootDirectory;

    /**
     * 默认主页
     */
    private String indexFileName = "index.html";

    public RequestProcessor(Socket request, File rootDirectory, String indexFileName) throws IOException {
        if (rootDirectory.isFile()) {
            throw new IOException("rootDirectionary must be a directory, not a file");
        }
        this.rootDirectory = rootDirectory.getCanonicalFile();

        if (indexFileName != null) {
            this.indexFileName = indexFileName;
        }

        this.request = request;
    }

    @Override
    public void run() {
        String root = rootDirectory.getPath();
        try {
            OutputStream raw = new BufferedOutputStream(
                    request.getOutputStream());
            Writer writer = new OutputStreamWriter(raw);

            Reader reader = new InputStreamReader(
                            new BufferedInputStream(
                            request.getInputStream()), "US-ASCII");

            StringBuilder requestLine = new StringBuilder();
            while (true) {
                int c = reader.read();
                if (c == '\r' || c == '\n') {
                    break;
                }
                requestLine.append((char) c);
            }

            String get = requestLine.toString();

            logger.info(request.getRemoteSocketAddress() + " " + get);
            String[] tokens = get.split("\\s+");
            String method = tokens[0];
            String version = "";
            if ("GET".equals(method)) {
                String fileName = tokens[1];
                if (fileName.endsWith("/")) {
                    fileName += indexFileName;
                }
                String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
                if (tokens.length > 2) {
                    version = tokens[2];
                }

                File theFile = new File(rootDirectory, fileName.substring(1, fileName.length()));

                if (theFile.canRead() && theFile.getCanonicalPath().startsWith(root)) {
                    byte[] theData = Files.readAllBytes(theFile.toPath());
                    if (version.startsWith("HTTP/")) {
                        sendHeader(writer, "HTTP/1.0 200 OK", contentType, theData.length);
                    }

                    //发送文件，可能是一个图像或其他二进制数据，
                    //所以要使用底层输出流，而不是Writer
                    raw.write(theData);
                    raw.flush();
                } else {
                    String body = new StringBuilder("<HTML>\r\n")
                            .append("<HEAD><TITLE>File Not Found</TITLE>\r\n")
                            .append("</HEAD>\r\n")
                            .append("<BODY>\r\n")
                            .append("<H1>HTTP Error 404：File Not Found</H1>\r\n")
                            .append("</BODY></HTML>\r\n")
                            .toString();
                    if (version.startsWith("HTTP/")) {
                        sendHeader(writer, "HTTP/1.0 404 File Not Found",
                                "text/html; charset=utf-8", body.length());
                    }
                    writer.write(body);
                    writer.flush();
                }
            } else {//不是GET方法
                String body = new StringBuilder("<HTML>\r\n")
                        .append("<HEAD><TITLE>Not Implemented</TITLE>\r\n")
                        .append("</HEAD>\r\n")
                        .append("<BODY>\r\n")
                        .append("<H1>HTTP Error 501：Not Implemented</H1>\r\n")
                        .append("</BODY></HTML>\r\n")
                        .toString();
                if (version.startsWith("HTTP/")) {
                    sendHeader(writer, "HTTP/1.0 501 Not Implemented",
                            "text/html; charset=utf-8", body.length());
                }
                writer.write(body);
                writer.flush();
            }

        } catch (IOException e) {
            logger.log(Level.WARNING,
                    "Error talking to " + request.getRemoteSocketAddress(), e);
        } finally {
            try {
                request.close();
            } catch (IOException e) {
                logger.log(Level.WARNING,
                        "Error to close this socket", e);
            }
        }
    }

    private void sendHeader(Writer writer, String responseCode,
                            String contentType, int length) throws IOException {
        writer.write(responseCode + "\r\n");
        writer.write("Date: " + new Date() + "\r\n");
        writer.write("Server: JHttp 2.0\r\n");
        writer.write("Content-length: " + length + "\r\n");
        writer.write("Content-type: " + contentType + "\r\n\r\n");
        writer.flush();

    }
}
