package com.pageSlammer.application.htmlFileUploaderInServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
@Component
@PropertySource("classpath:application.properties")
public class UploadFileInServer {
	@Autowired
	@Value("${uploadedFilePath}")
	private String uploadedFilePath;
	
	int port = 21;
	
	public Boolean  uploadFileInServer(String host,String user,String password,String filePath,String pageName) {
		/*String server = "files.000webhost.com";
		String user = "snehashisunified";
		String pass = "user123";*/
		System.out.println();
		/*System.out.println("inside uploadFileInServer pageName = "+pageName+" "+"uploadedFilePath = "+uploadedFilePath+pageName+".html");*/
		System.out.println("filepath = "+filePath);
		boolean isUploaded = false;
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(host, port);
            ftpClient.login(user, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            File localFile = new File("/home/snehashis/Documents/FTPTempFiles/"+pageName+".html");
            //File localFile = new File("/home/uiplonline/public_html/page-slammer/html/"+pageName+".html");
            
            System.out.println("localFile = "+localFile.toString());
            if(filePath.length() == 0 || filePath.isEmpty() || filePath == null) {
            	filePath = "/";
            }
            String remoteFile = filePath+pageName+".html";
            InputStream inputStream = new FileInputStream(localFile);
            isUploaded = ftpClient.storeFile(remoteFile, inputStream);
            inputStream.close();
            Boolean status = CreateDirectoryAndUploadFilesForCSSJS(host,user,password,filePath,pageName);
            System.out.println("status = "+status);
            
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
		return isUploaded;
    }
	
	public Boolean CreateDirectoryAndUploadFilesForCSSJS(String server,String user,String pass,String filePath,String pageName) {
		int port = 21;
        String localFilePath ="/home/snehashis/Documents/FTPTempFiles";
        System.out.println("localFilePath in CreateDirectoryAndUploadFilesForCSSJS = "+uploadedFilePath);
        //String localFilePath = "/home/uiplonline/public_html/page-slammer/html/";
        UploadFileInServer up = new UploadFileInServer();
        CheckFileFolderExistance check = new CheckFileFolderExistance();
        FTPClient ftpClient = new FTPClient();
        try {
 
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            Thread t1 = new Thread(new Runnable() {
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				try {
    					String folderName = "bootstrap";/*
    					Boolean isFolderExist = check.checkFolderExistanceInFTPServer(server, user, pass, filePath, folderName);*/
    					boolean folderCreated = ftpClient.makeDirectory(filePath+folderName);
    					List<String> folder = new ArrayList<String>();
    					folder.add("js");
    					folder.add("css");
    					folder.add("fonts");
    					if(folderCreated) {
    						Boolean isSubFolderCreated = false;
    						for(String subFolder : folder) {
    							isSubFolderCreated = ftpClient.makeDirectory(filePath+"bootstrap/"+subFolder);
    						}
    						if(isSubFolderCreated) {
    							System.out.println("sub folder created by t1");
    							Boolean status1 = false;
    							File cssFiles = new File(localFilePath+"bootstrap/css/");
    					        File[] css = cssFiles.listFiles();
    					        for(File f: css){
    					        	String firstRemoteFile = filePath+"bootstrap/css/"+f.getName().toString();
        							InputStream inputStream = new FileInputStream(f.getAbsolutePath().toString());
        				            status1 = ftpClient.storeFile(firstRemoteFile, inputStream);
        				            inputStream.close();
    					        }
    							if(status1) {
    								System.out.println("Files written in css in bottstrap subfolder");
    								Boolean status2 = false;
        							File fontsFiles = new File(localFilePath+"bootstrap/fonts/");
        					        File[] fonts = fontsFiles.listFiles();
        					        for(File f: fonts){
        					        	String firstRemoteFile = filePath+"bootstrap/fonts/"+f.getName().toString();
            							InputStream inputStream = new FileInputStream(f.getAbsolutePath().toString());
            				            status2 = ftpClient.storeFile(firstRemoteFile, inputStream);
            				            inputStream.close();
        					        }
        					        if(status2) {
        					        	System.out.println("Files written in fonts in bottstrap subfolder");
        					        	Boolean status3 = false;
            							File jsFiles = new File(localFilePath+"bootstrap/js/");
            					        File[] js = jsFiles.listFiles();
            					        for(File f: js){
            					        	String firstRemoteFile = filePath+"bootstrap/js/"+f.getName().toString();
                							InputStream inputStream = new FileInputStream(f.getAbsolutePath().toString());
                				            status3 = ftpClient.storeFile(firstRemoteFile, inputStream);
                				            inputStream.close();
            					        }
            					        if(status3) {
            					        	System.out.println("Files written in js in bottstrap subfolder");
            					        }else {
            					        	System.out.println("Files written failed in js in bottstrap subfolder");
            					        }
        					        }else {
        					        	System.out.println("Files written failed in fonts in bottstrap subfolder");
        					        }
    								
    							}else {
    								System.out.println("Files written failed in css in bottstrap subfolder");
    							}
    						}else {
    							System.out.println("failed by t1");
    						}
    					}else {
    						System.out.println("failed at t1");
    					}
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}
    		});
            Thread t2 = new Thread(new Runnable() {
    			
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				try {
    					boolean folderCreated = ftpClient.makeDirectory(filePath+"images");
    					if(folderCreated) {
    						System.out.println("created by t2");
    						Boolean status = false;
							File imagesFiles = new File(localFilePath+"images/");
					        File[] images = imagesFiles.listFiles();
					        for(File f: images){
					        	String firstRemoteFile = filePath+"images/"+f.getName().toString();
    							InputStream inputStream = new FileInputStream(f.getAbsolutePath().toString());
    				            status = ftpClient.storeFile(firstRemoteFile, inputStream);
    				            inputStream.close();
					        }
					        if(status) {
					        	System.out.println("Files written in images folder");
					        }else {
					        	System.out.println("Files written failed in images folder");
					        }
    					}else {
    						System.out.println("failed at t2");
    					}
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}
    		});
    		Thread t3 = new Thread(new Runnable() {
    			
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				try {
    					boolean folderCreated = ftpClient.makeDirectory(filePath+"js");
    					if(folderCreated) {
    						System.out.println("created by t3");
    						Boolean status = false;
							File jsFiles = new File(localFilePath+"js/");
					        File[] js = jsFiles.listFiles();
					        for(File f: js){
					        	String firstRemoteFile = filePath+"js/"+f.getName().toString();
    							InputStream inputStream = new FileInputStream(f.getAbsolutePath().toString());
    				            status = ftpClient.storeFile(firstRemoteFile, inputStream);
    				            inputStream.close();
					        }
					        if(status) {
					        	System.out.println("Files written in js folder");
					        }else {
					        	System.out.println("Files written failed in js folder");
					        }
    					}else {
    						System.out.println("failed at t3");
    					}
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}
    		});
    		Thread t4 = new Thread(new Runnable() {
    			
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				try {
    					boolean folderCreated = ftpClient.makeDirectory(filePath+"css");
    					if(folderCreated) {
    						System.out.println("created by t4");
    						Boolean status = false;
							File cssFiles = new File(localFilePath+"css/");
					        File[] css = cssFiles.listFiles();
					        for(File f: css){
					        	String firstRemoteFile = filePath+"css/"+f.getName().toString();
    							InputStream inputStream = new FileInputStream(f.getAbsolutePath().toString());
    				            status = ftpClient.storeFile(firstRemoteFile, inputStream);
    				            inputStream.close();
					        }
					        if(status) {
					        	System.out.println("Files written in css folder");
					        }else {
					        	System.out.println("Files written failed in css folder");
					        }
    					}else {
    						System.out.println("failed at t4");
    					}
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}
    		});
    		Thread t5 = new Thread(new Runnable() {
    			
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				try {
    					boolean folderCreated = ftpClient.makeDirectory(filePath+"fonts");
    					if(folderCreated) {
    						System.out.println("created by t5");
    						Boolean status = false;
							File fontsFiles = new File(localFilePath+"fonts/");
					        File[] fonts = fontsFiles.listFiles();
					        for(File f: fonts){
					        	String firstRemoteFile = filePath+"fonts/"+f.getName().toString();
    							InputStream inputStream = new FileInputStream(f.getAbsolutePath().toString());
    				            status = ftpClient.storeFile(firstRemoteFile, inputStream);
    				            inputStream.close();
					        }
					        if(status) {
					        	System.out.println("Files written in fonts folder");
					        }else {
					        	System.out.println("Files written failed in fonts folder");
					        }
    					}else {
    						System.out.println("failed at t5");
    					}
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}
    		});
            t1.start();
            try {
    			t1.join();
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		t2.start();
    		try {
    			t2.join();
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		t3.start();
    		try {
    			t3.join();
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		t4.start();
    		try {
    			t4.join();
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		t5.start();
    		try {
    			t5.join();
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            if(!t1.isAlive() && !t2.isAlive() && !t3.isAlive() && !t4.isAlive() && !t5.isAlive()) {
            	System.out.println("completed");
            }else {
            	System.out.println("not completed");
            }
            
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
		return null;
	}
	public Boolean checkFileExistanceInFTPServer(String host,String user,String password,String filePath,String pageName) throws IOException {
		Boolean isFileExist = false;
        FTPClient ftpClient = new FTPClient();
        try {
 
            ftpClient.connect(host, port);
            ftpClient.login(user, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
 
            String remoteFile = filePath+pageName+".html";
            InputStream inputStream = ftpClient.retrieveFileStream(remoteFile);
            int returnCode = ftpClient.getReplyCode();
            if (inputStream != null || returnCode != 550) {
            	System.out.println("present");
                isFileExist = true;
            }
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
		return isFileExist;
	}
    
}