package com.pageSlammer.application.service;

import com.pageSlammer.application.model.UserLoginMaster;
public interface UserLoginService {
	public String validateUser(String email, String password);
	public Boolean loginUser(UserLoginMaster ulm);
	public int checkLoginStatus(String userId);
	public Boolean updateLoginUser(String accessTocken,String userId);
	public Boolean checkUserSession(String currentAccessTocken,String userId);
}
