package com.pageSlammer.application.htmlFileUploaderInServer;

import org.apache.commons.net.ftp.FTPClient;

public class CheckFTPConnection {
	public Boolean checkConnection(String Host, String userName, String password){
		FTPClient client = new FTPClient();
		Boolean isConnected = false;
		try {
			client.connect(Host);
			boolean login = client.login(userName,password);
			if(login) {
				System.out.println("Connection established...");
				isConnected = login;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				client.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return isConnected;
	}
	
}
