package com.pageSlammer.application.service;

import com.pageSlammer.application.model.UserMaster;

public interface UserRegistrationService {
	public UserMaster isUserExist(String emailId);
	public UserMaster registerUser(UserMaster um);	
	public Boolean facebookRegistration(UserMaster um);
	public Boolean updateWithFacebook(String facebookId,String picUrl,String emailId);
}
