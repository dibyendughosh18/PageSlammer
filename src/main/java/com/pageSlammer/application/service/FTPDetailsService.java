package com.pageSlammer.application.service;

import java.util.List;

import com.pageSlammer.application.model.FTPCredential;

public interface FTPDetailsService {
	public Boolean isFTPDetailsSaved(FTPCredential ftp);
	public Boolean isFTPDetailsExistInDB(FTPCredential ftp,String userId);
	public List<FTPCredential> getFTPDetails(String userId);
}
