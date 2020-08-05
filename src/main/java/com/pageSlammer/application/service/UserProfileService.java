package com.pageSlammer.application.service;

import java.util.List;

import com.pageSlammer.application.model.UserMaster;

public interface UserProfileService {
	public List<UserMaster> fetchUserRecord(String userId);
	public Boolean updateProfile(String emailId,String fullName,String phone,String postCode,String gender,String state,String suburb,String profilePicUrl,String userId );
	public Boolean checkPasswordMatch(String oldPassword,String userId);
	public Boolean updatePassword(String newPassword,String userId);
}
