package com.pageSlammer.application.service;

import com.pageSlammer.application.model.ForgetPassword;

public interface ForgetPasswordService {
	public Boolean checkToken(String token);
	public ForgetPassword getTokenDetails(String token);
	public Boolean saveTokenWithemail(ForgetPassword fp);
	public Boolean deleteTokenDetails(ForgetPassword fp);
}
