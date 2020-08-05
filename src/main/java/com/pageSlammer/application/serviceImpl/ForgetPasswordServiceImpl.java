package com.pageSlammer.application.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pageSlammer.application.model.ForgetPassword;
import com.pageSlammer.application.repository.ForgetPasswordRepository;
import com.pageSlammer.application.service.ForgetPasswordService;

@Service("userForgetPasswordService")
public class ForgetPasswordServiceImpl implements ForgetPasswordService{
	@Autowired
	private ForgetPasswordRepository forgetPasswordRepository;
	@Override
	public Boolean checkToken(String token) {
		ForgetPassword fp = forgetPasswordRepository.findByPasswordToken(token);
		if(fp != null) {
			if(fp.isExpired()) {
				return false;
			}else {
				return true;
			}
		}else {
			return false;
		}
	}
	@Override
	public ForgetPassword getTokenDetails(String token) {
		return forgetPasswordRepository.findByPasswordToken(token);
	}
	@Override
	public Boolean saveTokenWithemail(ForgetPassword fp) {
		ForgetPassword result = forgetPasswordRepository.save(fp);
		if(result != null) {
			return true;
		}else {
			return false;
		}
	}
	@Override
	public Boolean deleteTokenDetails(ForgetPassword fp) {
		forgetPasswordRepository.delete(fp);
		return true;
	}

}
