package com.pageSlammer.application.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pageSlammer.application.configuration.PasswordEncryptionDecryption;
import com.pageSlammer.application.model.UserLoginMaster;
import com.pageSlammer.application.model.UserMaster;
import com.pageSlammer.application.repository.UserLoginRepository;
import com.pageSlammer.application.repository.UserMasterRepository;
import com.pageSlammer.application.service.UserLoginService;


@Service("userLoginService")
public class UserLoginServiceImpl implements UserLoginService{
	@Autowired
	private UserMasterRepository userMasterRepository;
	
	@Autowired
	private UserLoginRepository userLoginRepository;
	
	PasswordEncryptionDecryption passwordEncryptionDecryption = new PasswordEncryptionDecryption();
	
	@Override
	public String validateUser(String email, String password) {
		List<UserMaster> userMaster = userMasterRepository.findUserByEmailId(email);
		String userId = null;
		String userPassword = null;
		String dbUserPassword = null;
		if(userMaster.size() > 0) {
			for(UserMaster user : userMaster) {
				userPassword = user.getPassword().toString();
			}
			try {
				dbUserPassword = passwordEncryptionDecryption.decrypt(userPassword);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(password.equals(dbUserPassword)) {
				for(UserMaster user : userMaster) {
					userId = user.getUserId().toString();
				}
				return userId;
			}else {
				return userId;
			}
		}else {
			return userId;
		}
	}

	@Override
	public Boolean loginUser(UserLoginMaster ulm) {
		UserLoginMaster userLoginMaster = userLoginRepository.save(ulm);
		if(userLoginMaster != null) {
			return true;
		}else {
			return false;
		}
	}

	@Override
	public int checkLoginStatus(String userId) {
		int loginStatus = 0;
		List<UserLoginMaster> ulm = userLoginRepository.findByUserId(userId);
		if(ulm != null) {
			for(UserLoginMaster user : ulm) {
				loginStatus = user.getLoginStatus();
			}
			return loginStatus;
		}else {
			return loginStatus;
		}
	}

	@Override
	public Boolean updateLoginUser(String currentAccessToken,String userId) {
		/*List<UserLoginMaster> ulist = userLoginRepository.findByUserId(userId);
		long id = 0;
		for(UserLoginMaster user : ulist) {
			id = (long) user.getId();
		}*/
		/*UserLoginMaster loggedinUser = userLoginRepository.findOne(id);
		loggedinUser.setcurrentAccessToken(currentAccessToken);
		UserLoginMaster updateLoggedinUser= userLoginRepository.save(loggedinUser);
		if(updateLoggedinUser != null) {
			return true;
		}else {
			return false;
		}*/
		userLoginRepository.updatecurrentAccessToken(currentAccessToken, userId);
		return true;
	}

	@Override
	public Boolean checkUserSession(String currentAccessToken, String userId) {
		List<UserLoginMaster> session = userLoginRepository.checkUserActiveSession(userId, currentAccessToken);
		if(session.size() >0) {
			return true;
		}else {
			return false;
		}		
	}
	
	

}
