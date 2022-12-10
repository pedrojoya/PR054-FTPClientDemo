package org.example;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

public class FTPManager {

    private final String ftpServer;
    private final int port;
    private final String username;
    private final String password;
    private final FTPClient ftpClient = new FTPClient();

    public FTPManager(String ftpServer, int port, String username, String password) {
        this.ftpServer = ftpServer;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public FTPManager(String ftpServer) {
        this.ftpServer = ftpServer;
        this.port = 21;
        this.username = "anonymous";
        this.password = "anonymous";
    }

    public String getFtpServer() {
        return ftpServer;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean connect() throws IOException {
        ftpClient.connect(ftpServer);
        boolean connected = FTPReply.isPositiveCompletion(ftpClient.getReplyCode());
        if (connected) {
            ftpClient.enterLocalPassiveMode();
        }
        return connected;
    }

    public void disconnect() throws IOException {
        ftpClient.disconnect();
    }

    public boolean login() throws IOException {
        return ftpClient.login(username, password);
    }

    public boolean logout() throws IOException {
        return ftpClient.logout();
    }

    public String getWorkingDirectoryName() throws IOException {
        return ftpClient.printWorkingDirectory();
    }

    public FTPFile[] getFiles() throws IOException {
        return ftpClient.listFiles();
    }

    public void downloadFile(String inputFileName, File outputFile) throws IOException {
        try(var fis = new FileOutputStream(outputFile);
            var bos = new BufferedOutputStream(fis)) {
            ftpClient.retrieveFile(inputFileName, bos);
        }
    }

}
