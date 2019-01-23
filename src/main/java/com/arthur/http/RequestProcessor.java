package com.arthur.http;

import java.io.IOException;
import java.io.Writer;
import java.net.Socket;
import java.io.File;
import java.util.Date;
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
