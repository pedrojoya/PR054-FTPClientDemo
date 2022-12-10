package org.example;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.example.ExceptionUtils.rethrowConsumer;

public class Main {

    public static void main(String[] args) {
        FTPManager ftpManager = new FTPManager("ftp.rediris.es");
        Main main = new Main(ftpManager);
        main.start();
    }

    private final FTPManager ftpManager;

    public Main(FTPManager ftpManager) {
        this.ftpManager = ftpManager;
    }

    public void start() {
        try {
            // Connect
            System.out.printf("\nConnecting to %s ...\n", ftpManager.getFtpServer());
            if (!ftpManager.connect()) {
                System.out.println("Connection rejected...");
                ftpManager.disconnect();
                return;
            }

            // Login
            System.out.printf("Login in with %s - %s ...\n", ftpManager.getUsername(), ftpManager.getPassword());
            if (!ftpManager.login()) {
                System.out.println("Incorrect login ...");
                ftpManager.disconnect();
                return;
            }
            System.out.println("Logged in...");

            // Print working directory
            System.out.printf("\nWorking directory: %s\n", ftpManager.getWorkingDirectoryName());
            FTPFile[] files = ftpManager.getFiles();
            System.out.printf("Files in working directory: %d\n", files.length);
            String[] fileTypes = {"File", "Directory", "Symbolic link"};
            Arrays.stream(files).forEach(ftpFile ->
                    System.out.printf("\t%s [%s]\n", ftpFile.getName(), fileTypes[ftpFile.getType()]));
            System.out.println();

            // Downloading first file
            Arrays.stream(files)
                    .filter(ftpFile -> ftpFile.getType() == 0)
                    .findFirst()
                    .ifPresent(rethrowConsumer(ftpFile -> {
                        String fileName = ftpFile.getName();
                        ftpManager.downloadFile(fileName, new File(fileName));
                        System.out.printf("Dowloaded %s\n\n", fileName);
                    }));

            // Logout
            if (ftpManager.logout()) {
                System.out.println("Logged out from FTP server...");
            }

            // Disconnect
            ftpManager.disconnect();
            System.out.println("Disconnected...");
        } catch (IOException ioe) {
            System.out.println("IO error...");
        }

    }

}