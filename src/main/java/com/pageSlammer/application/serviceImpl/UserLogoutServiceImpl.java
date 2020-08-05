package com.pageSlammer.application.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pageSlammer.application.repository.UserLoginRepository;
import com.pageSlammer.application.service.UserLogoutService;

@Service("userLogoutService")
public class UserLogoutServiceImpl implements UserLogoutService{
	@Autowired
	private UserLoginRepository userLoginRepository;
	@Override
	public Boolean logoutUser(String userId, String currentAccessToken) {
		int row = userLoginRepository.deleteLogInDetails(userId, currentAccessToken);
		if(row > 0) {
			return true;
		}else {
			return false;
		}
	}

}
