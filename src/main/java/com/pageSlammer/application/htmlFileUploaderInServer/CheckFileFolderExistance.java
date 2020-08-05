package com.pageSlammer.application.htmlFileUploaderInServer;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class CheckFileFolderExistance {
	int port = 21;
	/*String server = "files.000webhost.com";
	String user = "snehashisunified";
	String pass = "user123";*/
	public Boolean checkFolderExistanceInFTPServer(String host,String user,String password,String folderPath,String folderName) throws IOException {
		Boolean isFolderExist = false;
        FTPClient ftpClient = new FTPClient();
        try {
 
            ftpClient.connect(host, port);
            ftpClient.login(user, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            boolean inputStream = ftpClient.changeWorkingDirectory(folderPath+folderName);
            if (inputStream) {
            	System.out.println("present");
            	isFolderExist = true;
            }else {
            	System.out.println("not present");
            }
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } 
		return isFolderExist;
	}
	
	public Boolean checkFileExistanceInFTPServer(String host,String user,String password,String filePath,String fileName) throws IOException {
		Boolean isFileExist = false;
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(host, port);
            ftpClient.login(user, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            InputStream inputStream = ftpClient.retrieveFileStream(filePath+fileName);
            int returnCode = ftpClient.getReplyCode();
            if (inputStream != null || returnCode != 550) {
            	System.out.println("present");
            	isFileExist = true;
            }else {
            	System.out.println("not present");
            }
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
		return isFileExist;
	}
}
