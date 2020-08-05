package com.pageSlammer.application.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pageSlammer.application.configuration.PasswordEncryptionDecryption;
import com.pageSlammer.application.model.UserMaster;
import com.pageSlammer.application.repository.UserMasterRepository;
import com.pageSlammer.application.service.UserProfileService;

@Service("userProfileService")
public class UserProfileServiceImpl implements UserProfileService{
	@Autowired
	private UserMasterRepository userMasterRepository;
	PasswordEncryptionDecryption passwordEncryptionDecryption = new PasswordEncryptionDecryption();
	
	@Override
	public List<UserMaster> fetchUserRecord(String userId) {
		List<UserMaster> userRecord = userMasterRepository.findByUserId(userId);
		return userRecord;	
	}

	@Override
	public Boolean updateProfile(String emailId, String fullName, String phone, String postCode, String gender,
			String state, String suburb,String profilePicUrl, String userId) {
		int row = userMasterRepository.updateUserProfile(emailId, fullName, phone, postCode, gender, state, suburb,profilePicUrl, userId);
		if(row > 0) {
			return true;
		}else {
			return false;
		}
	}

	@Override
	public Boolean checkPasswordMatch(String oldPassword, String userId) {
		List<UserMaster> userRecord = userMasterRepository.findByUserId(userId);
		String password = null;
		String dbUserPassword = null;
		for(UserMaster um : userRecord) {
			password = um.getPassword();
		}
		try {
			dbUserPassword = passwordEncryptionDecryption.decrypt(password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(dbUserPassword.equals(oldPassword)) {
			return true;
		}else {
			return false;
		}
	}

	@Override
	public Boolean updatePassword(String newPassword, String userId) {
		String password = null;
		try {
			password = passwordEncryptionDecryption.encrypt(newPassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int row = userMasterRepository.updateOldPassword(password, userId);
		if(row > 0) {
			return true;
		}else {
			return false;
		}
	}
	
}
