package org.example;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        Main main = new Main();
        main.start();
    }

    private final int FILE_TYPE = 0;
    private final int DIR_TYPE = 1;
    private final int SYS_LINK_TYPE = 2;
    private final FTPClient ftpClient = new FTPClient();
    private final String ftpServer = "ftp.rediris.es";
    private final String username = "anonymous";
    private final String password = "anonymous";

    public void start() {
        try {
            // Connect
            System.out.printf("\nConnecting to %s ...\n", ftpServer);
            ftpClient.connect(ftpServer);
            boolean connected = FTPReply.isPositiveCompletion(ftpClient.getReplyCode());
            if (!connected) {
                System.out.println("Connection rejected ...");
                ftpClient.disconnect();
                return;
            }
            ftpClient.enterLocalPassiveMode();

            // Login
            System.out.printf("Login in with %s - %s ...\n", username, password);
            if (!ftpClient.login(username, password)) {
                System.out.println("Incorrect login ...");
                ftpClient.disconnect();
                return;
            }
            System.out.println("Logged in...");

            // Print working directory
            printWorkingDirectory();

            // Downloading first file
            FTPFile[] files = ftpClient.listFiles();
            Arrays.stream(files)
                    .filter(ftpFile -> ftpFile.getType() == FILE_TYPE)
                    .findFirst()
                    .ifPresent(ExceptionUtils.rethrowConsumer(ftpFile -> {
                        String fileName = ftpFile.getName();
                        try(var fis = new FileOutputStream(fileName);
                            var bos = new BufferedOutputStream(fis)) {
                            ftpClient.retrieveFile(fileName, bos);
                        }
                        System.out.printf("Dowloaded %s\n\n", fileName);
                    }));

            // Move to first directory
            Arrays.stream(files)
                    .filter(ftpFile -> ftpFile.getType() == DIR_TYPE)
                    .skip(2) // Skip . and ..
                    .findFirst()
                    .ifPresent(ExceptionUtils.rethrowConsumer(ftpFile -> {
                        String dirPath = "/" + ftpFile.getName();
                        System.out.printf("Moving to %s ...\n", dirPath);
                        ftpClient.changeWorkingDirectory(dirPath);
                        printWorkingDirectory();
                        System.out.println("Moving to parent directory ...\n");
                        ftpClient.changeToParentDirectory();
                        printWorkingDirectory();
                    }));

            // Logout
            if (ftpClient.logout()) {
                System.out.println("Logged out from FTP server ...");
            }

            // Disconnect
            ftpClient.disconnect();
            System.out.println("Disconnected ...\n");
        } catch (IOException ioe) {
            System.out.println("IO error ...\n");
        }

    }

    private void printWorkingDirectory() throws IOException {
        System.out.printf("\nWorking directory: %s\n", ftpClient.printWorkingDirectory());
        FTPFile[] files = ftpClient.listFiles();
        System.out.printf("Files in working directory: %d\n", files.length);
        String[] fileTypes = {"File", "Directory", "Symbolic link"};
        Arrays.stream(files).forEach(ftpFile ->
                System.out.printf("\t%s [%s]\n", ftpFile.getName(), fileTypes[ftpFile.getType()]));
        System.out.println();
    }

}