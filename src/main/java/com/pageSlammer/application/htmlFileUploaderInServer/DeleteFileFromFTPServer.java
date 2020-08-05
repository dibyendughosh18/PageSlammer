package com.pageSlammer.application.htmlFileUploaderInServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;

public class DeleteFileFromFTPServer {
	int port = 21;
	@Value("${uploadedFilePath}")
	private String uploadedFilePath;
	public Boolean  deleteFileInServer(String host,String user,String password,String filePath,String pageName) {
		System.out.println("pageName = "+pageName+" "+"uploadedFilePath = "+uploadedFilePath+pageName+".html");
		System.out.println("filepath = "+filePath);
		boolean isDeleted = false;
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(host, port);
            ftpClient.login(user, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            String remoteFile = filePath+pageName+".html";
            boolean status = ftpClient.deleteFile(remoteFile);
            isDeleted = status;
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
		return isDeleted;
    }
}
