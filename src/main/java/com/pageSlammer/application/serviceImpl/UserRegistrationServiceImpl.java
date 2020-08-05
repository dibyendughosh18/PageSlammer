package com.pageSlammer.application.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pageSlammer.application.model.UserMaster;

import com.pageSlammer.application.repository.UserMasterRepository;
import com.pageSlammer.application.service.UserRegistrationService;

@Service("userRegistrationService")
public class UserRegistrationServiceImpl implements UserRegistrationService{
	
	@Autowired
	private UserMasterRepository userMasterRepository;

	@Override
	public UserMaster isUserExist(String emailId) {
		UserMaster um = userMasterRepository.findByEmailId(emailId);
		if(um != null) {
			return um;
		}else {
			return um;
		}
	}

	@Override
	public UserMaster registerUser(UserMaster um) {
		System.out.println("um = "+um);
		UserMaster res = userMasterRepository.save(um);
		if(res != null) {
			return res;
		}else {
			return res;
		}
	}

	@Override
	public Boolean facebookRegistration(UserMaster um) {
		UserMaster res = userMasterRepository.save(um);
		if(res != null) {
			return true;
		}else {
			return false;
		}
	}

	@Override
	public Boolean updateWithFacebook(String facebookId, String picUrl, String emailId) {
		int row = userMasterRepository.updateUserWithFacebook(facebookId, picUrl, emailId);
		if(row > 0) {
			return true;
		}else {
			return false;
		}
	}
}
