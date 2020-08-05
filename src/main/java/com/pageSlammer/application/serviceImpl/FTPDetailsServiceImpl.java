package com.pageSlammer.application.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pageSlammer.application.model.FTPCredential;
import com.pageSlammer.application.repository.FTPDetailsRepository;
import com.pageSlammer.application.service.FTPDetailsService;
@Service("ftpDetailsService")
public class FTPDetailsServiceImpl implements FTPDetailsService{
	@Autowired
	FTPDetailsRepository ftpDetailsRepository;
	@Override
	public Boolean isFTPDetailsSaved(FTPCredential ftp) {
		FTPCredential result = ftpDetailsRepository.save(ftp);
		if(result != null) {
			return true;
		}else {
			return false;
		}
	}
	@Override
	public Boolean isFTPDetailsExistInDB(FTPCredential ftp,String userId) {
		String host = ftp.getHostAddress();
		String user = ftp.getUserName();
		String password = ftp.getPassword();
		String filePath = ftp.getFtpFilePath();
		FTPCredential result = ftpDetailsRepository.getFTPDetails(host, user, password, filePath, userId);
		if(result != null) {
			return true;
		}else {
			return false;
		}
	}
	@Override
	public List<FTPCredential> getFTPDetails(String userId) {
		List<FTPCredential> ftp = ftpDetailsRepository.findByUserId(userId);
		return ftp;
	}
}
