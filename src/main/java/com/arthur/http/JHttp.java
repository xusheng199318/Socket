package com.arthur.http;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.io.*;

/**
 * Created by xusheng on 2019/1/23.
 */
public class JHttp {
    private static final Logger logger = Logger.getLogger(JHttp.class.getCanonicalName());

    /**
     * 默认处理请求的线程数量
     */
    private static final int NUM_THREADS = 50;

    /**
     * 默认主页
     */
    private static final String INDEX_FILE = "index.html";

    /**
     * 根目录
     */
    private final File rootDirectory;

    /**
     * 服务端口号
     */
    private final Integer port;

    public JHttp(File rootDirectory, Integer port) throws IOException {
        if (!rootDirectory.isDirectory()) {
            throw new IOException(rootDirectory + " is not a directory");
        }
        this.rootDirectory = rootDirectory;
        this.port = port;
    }

    public void start() {
        ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
        while (true) {
            try (ServerSocket server = new ServerSocket(port)) {
                Socket request = server.accept();
                Runnable requestProcesser = new RequestProcessor(request,rootDirectory, INDEX_FILE);
                threadPool.submit(requestProcesser);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String path = "D:\\ideaWorkspace2.5\\Socket\\src\\main\\WebRoot";
        File rootDirectory = new File(path);
        try {
            JHttp httpServer = new JHttp(rootDirectory, 80);
            httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
